package com.example.dochat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.dochat.R;
import com.example.dochat.adapters.TabsAccessorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private TabsAccessorAdapter mTabsAccessorAdapter;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDBRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFields();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            sendUser2LoginActivity();
        } else {
            verifyUserExistance();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.home_sign_out) {
            mAuth.signOut();
            sendUser2LoginActivity();
        }
        if (item.getItemId() == R.id.home_settings) {
            sendUser2SettingsActivity();
        }
        if (item.getItemId() == R.id.home_create_group) {
            request2CreateNewGroup();
        }

        if (item.getItemId() == R.id.home_find_friends) {
            send2FindFriendsActivity();

        }
        return true;
    }

    private void sendUser2LoginActivity() {
        Intent loginIntent = new Intent(HomeActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void sendUser2SettingsActivity() {
        Intent settingsIntent = new Intent(HomeActivity.this, SettingsActivity.class);
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingsIntent);
        finish();
    }

    private void verifyUserExistance() {
        String currentUid = mAuth.getCurrentUser().getUid();
        mDBRef.child("Users").child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())) {
                    // Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "set your name on settings", Toast.LENGTH_SHORT).show();
                    //sendUser2SettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void initializeFields() {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDBRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.second_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("doChat");

        mViewPager = findViewById(R.id.main_tab_pager);

        mTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsAccessorAdapter);
        mTabLayout = findViewById(R.id.main_tabs_layout);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void request2CreateNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this, R.style.AlertDialog);
        builder.setTitle("Create Group");

        final EditText groupNameField = new EditText(HomeActivity.this);
        groupNameField.setHint("Enter group name");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupName)) {
                    Toast.makeText(getApplicationContext(), "Must need group name!", Toast.LENGTH_SHORT).show();
                } else {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String groupName) {
        mDBRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), groupName + "Group is created.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void send2FindFriendsActivity() {
        Intent findFriendsIntent = new Intent(HomeActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }


}