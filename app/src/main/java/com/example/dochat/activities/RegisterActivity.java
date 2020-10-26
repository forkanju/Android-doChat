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
import android.widget.TextView;
import android.widget.Toast;

import com.example.dochat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    ActionBar actionBar;

    private TextView loginHereTxt;
    private Button createAccountButton;
    private EditText userEmail, userPass;

    private FirebaseAuth mAuth;
    private DatabaseReference mDBReference;

    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        actionBar = getSupportActionBar();
        getSupportActionBar().setTitle("Registration");

        initializeFields();

        loginHereTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              send2LoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

    }

    //Back navigation
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initializeFields() {
        //firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mDBReference = FirebaseDatabase.getInstance().getReference();
        mProgressDialog = new ProgressDialog(this);

        loginHereTxt = findViewById(R.id.login_here_txt);
        createAccountButton = findViewById(R.id.reg_btn);
        userEmail = findViewById(R.id.reg_email_edt_txt);
        userPass = findViewById(R.id.reg_pass_edt_text);
    }


    private void createNewAccount() {

        String email = userEmail.getText().toString().trim();
        String pass = userPass.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            userEmail.setError("Enter your email!");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            userPass.setError("Enter your password!");
        } else {
            mProgressDialog.setTitle("Creating new account");
            mProgressDialog.setMessage("Please wait while creating new account..");
            mProgressDialog.setCanceledOnTouchOutside(true);
            mProgressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                mDBReference.child("Users").child(currentUserID).setValue("");

                                Toast.makeText(getApplicationContext(), "Account created Successfully!", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                                send2HomeActivity();

                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void send2LoginActivity(){
        Intent createAccIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(createAccIntent);
    }

    private void send2HomeActivity(){
        Intent homeIntent = new Intent(RegisterActivity.this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}