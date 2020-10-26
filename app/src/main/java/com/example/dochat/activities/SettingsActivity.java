package com.example.dochat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dochat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button mUpdateAccountSettings;
    private EditText mUserName, mUserStatus;
    private CircleImageView mUserProfile;

    private String currentUid;
    private FirebaseAuth mAuth;
    private DatabaseReference mDBRef;

    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        actionBar = getSupportActionBar();
        getSupportActionBar().setTitle("Settings");

        initializeFields();

        mUpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAccount();
            }
        });

        retrieveUserInfo();

    }

 //   Back navigation
    @Override
    public boolean onSupportNavigateUp() {
      //  onBackPressed();
        send2HomeActivity();
        return true;
    }


    private void initializeFields() {
        //mUserName.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        mDBRef = FirebaseDatabase.getInstance().getReference();

        mUpdateAccountSettings = findViewById(R.id.update_settings_btn);
        mUserName = findViewById(R.id.set_user_name);
        mUserStatus = findViewById(R.id.set_user_status);
        mUserProfile = findViewById(R.id.profile_img);
    }

    private void updateAccount() {

        String setUserName = mUserName.getText().toString().trim();
        String setStatus = mUserStatus.getText().toString().trim();

        if (TextUtils.isEmpty(setUserName)) {
            mUserName.setError("Please set user name!");
            return;
        }
        if (TextUtils.isEmpty(setStatus)) {
            mUserStatus.setError("Please set user status!");
            return;
        } else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUid);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            mDBRef.child("Users").child(currentUid).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                send2HomeActivity();
                            } else {
                                String message = task.getException().toString();
                                Log.d("PROFILE_UPDATE: ", "" + message);
                                Toast.makeText(getApplicationContext(), "Profile upgrade failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void send2HomeActivity() {
        Intent homeIntent = new Intent(SettingsActivity.this, HomeActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }

    private void retrieveUserInfo(){
        mDBRef.child("Users").child(currentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") &&
                                (dataSnapshot.hasChild("image")))){

                            String userNameR = dataSnapshot.child("name").getValue().toString(); //R mean retrieve here
                            String userStatusR = dataSnapshot.child("status").getValue().toString();//R mean retrieve here
                            String userImgR = dataSnapshot.child("image").getValue().toString();//R mean retrieve here

                            mUserName.setText(userNameR);
                            mUserStatus.setText(userStatusR);


                        }else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){

                            String userNameR = dataSnapshot.child("name").getValue().toString(); //R mean retrieve here
                            String userStatusR = dataSnapshot.child("status").getValue().toString();//R mean retrieve here

                            mUserName.setText(userNameR);
                            mUserStatus.setText(userStatusR);
                        }else {
                            mUserName.setVisibility(View.VISIBLE);
                            Toast.makeText(getApplicationContext(), "Please set & update your profile info!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}