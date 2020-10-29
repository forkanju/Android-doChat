package com.example.dochat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.dochat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVfCodeButton;
    private Button submitVfCodeButton;
    private EditText inputPhone;
    private EditText inputVfCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog mDialog;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        actionBar = getSupportActionBar();
        getSupportActionBar().setTitle("Phone Login");

        initializeFields();

        sendVerificationCode();
        submitVerificationCode();
    }

    //Back navigation
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    private void initializeFields() {

        mAuth = FirebaseAuth.getInstance();

        sendVfCodeButton = findViewById(R.id.send_verification_button);
        submitVfCodeButton = findViewById(R.id.verify_button);
        inputPhone = findViewById(R.id.phone_number_input);
        inputVfCode = findViewById(R.id.verification_code_input);

        mDialog = new ProgressDialog(this);
    }

    private void sendVerificationCode() {
        sendVfCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phone = inputPhone.getText().toString().trim();
                if (TextUtils.isEmpty(phone)) {
                    inputPhone.setError("Please enter your phone number!");
                    return;
                } else {

                    mDialog.setTitle("Phone Verifying..");
                    mDialog.setMessage("Please wait, while authenticating your phone!");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();

//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Authentication Timeout!", Toast.LENGTH_SHORT).show();
//                            mDialog.dismiss();
//                        }
//                    }, 30000);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phone,
                            60,
                            TimeUnit.SECONDS,
                            PhoneLoginActivity.this,
                            mCallbacks);
                }

            }
        });
    }

    private void submitVerificationCode() {
        submitVfCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVfCodeButton.setVisibility(View.INVISIBLE);
                inputPhone.setVisibility(View.INVISIBLE);

                String verificationCode = inputVfCode.getText().toString().trim();

                if (TextUtils.isEmpty(verificationCode)) {
                    Toast.makeText(getApplicationContext(), "Please enter verification code here", Toast.LENGTH_SHORT).show();
                } else {
                    mDialog.setTitle("Code Verifying..");
                    mDialog.setMessage("Please wait, while verifying your code!");
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();

                    //Here the submit verification code functionality
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });


        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(getApplicationContext(), "Invalid Phone Number!", Toast.LENGTH_SHORT).show();
                mDialog.dismiss();

                sendVfCodeButton.setVisibility(View.VISIBLE);
                inputPhone.setVisibility(View.VISIBLE);

                inputVfCode.setVisibility(View.INVISIBLE);
                submitVfCodeButton.setVisibility(View.INVISIBLE);
            }

            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                //Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(getApplicationContext(), "Code has been sent to the entered number.", Toast.LENGTH_SHORT).show();
                mDialog.dismiss();

                sendVfCodeButton.setVisibility(View.INVISIBLE);
                inputPhone.setVisibility(View.INVISIBLE);

                inputVfCode.setVisibility(View.VISIBLE);
                submitVfCodeButton.setVisibility(View.VISIBLE);

            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Phone login successfully!", Toast.LENGTH_SHORT).show();
                            send2HomeActivity();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(getApplicationContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void send2HomeActivity() {
        Intent phoneLoginIntent = new Intent(PhoneLoginActivity.this, HomeActivity.class);
        startActivity(phoneLoginIntent);
        finish();
    }
}