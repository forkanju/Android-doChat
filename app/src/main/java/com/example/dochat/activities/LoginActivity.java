package com.example.dochat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.dochat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private ProgressDialog mProgressDialog;

    private TextView createAccTxt, forgotPassTxt;
    private Button mLoginButton, phoneLoginButton;
    private EditText userEmail, userPass;


    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        actionBar = getSupportActionBar();
        getSupportActionBar().setTitle("Login");

        initializeFields();

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUser2Login();
            }
        });

        createAccTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createAccIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(createAccIntent);
            }
        });


    }

    private void initializeFields() {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgressDialog = new ProgressDialog(this);

        createAccTxt = findViewById(R.id.create_account_text);
        forgotPassTxt = findViewById(R.id.forgot_pass_text);
        mLoginButton = findViewById(R.id.login_btn);
        phoneLoginButton = findViewById(R.id.phone_login);
        userEmail = findViewById(R.id.login_email_edt_txt);
        userPass = findViewById(R.id.login_pass_edt_txt);
    }

    //Back navigation
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void allowUser2Login() {
        String email = userEmail.getText().toString().trim();
        String pass = userPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            userEmail.setError("Enter your email!");
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            userPass.setError("Enter your password!");
            return;

        } else {
            mProgressDialog.setTitle("Sign In!");
            mProgressDialog.setMessage("Please wait while sign in..");
            mProgressDialog.setCanceledOnTouchOutside(true);
            mProgressDialog.show();

            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                String currentUid = mAuth.getCurrentUser().getUid();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                userRef.child(currentUid).child("device_token")
                                        .setValue(deviceToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    send2HomeActivity();
                                                    Toast.makeText(getApplicationContext(), "User Login successful", Toast.LENGTH_SHORT).show();
                                                    mProgressDialog.dismiss();
                                                }
                                            }
                                        });


                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(getApplicationContext(), "User not found!", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                            }

                        }
                    });
        }
    }

    private void send2HomeActivity(){
        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}