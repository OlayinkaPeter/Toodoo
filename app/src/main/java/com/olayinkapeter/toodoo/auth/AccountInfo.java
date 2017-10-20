package com.olayinkapeter.toodoo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.olayinkapeter.toodoo.R;
import com.olayinkapeter.toodoo.models.UserClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountInfo extends AppCompatActivity {
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabase;

    private String mUserId, mUserDisplayNameFromCredentialsAccount, mUserDisplayNameFromDatabase, mUserEmail;
    private static final String TAG = "AccountInfo";

    TextView accountName, accountEmail;
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        accountName = (TextView) findViewById(R.id.account_name);
        accountEmail = (TextView) findViewById(R.id.account_email);
        logoutBtn = (Button) findViewById(R.id.logoutBtn);

        getUserDetailsFromFirebase();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                kickUserOut();
            }
        });
    }

    // Get Authenticated user name and email
    public void getUserDetailsFromFirebase() {
        mUserId = mFirebaseUser.getUid();
        mUserDisplayNameFromCredentialsAccount = mFirebaseUser.getDisplayName();
        mUserEmail = mFirebaseUser.getEmail();

        accountName.setText(mUserDisplayNameFromCredentialsAccount);
        accountEmail.setText(mUserEmail);

        mDatabase.child("users").child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserClass UserClass = dataSnapshot.getValue(UserClass.class);
                Log.d(TAG, "UserClass name: " + UserClass.getEmail() + ", email " + UserClass.getName());
                mUserDisplayNameFromDatabase = UserClass.getName();
                if (mUserDisplayNameFromCredentialsAccount!= null) {

                }
                else {
                    accountName.setText(mUserDisplayNameFromDatabase);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    //Log out UserClass and go to LoginActivity
    private void kickUserOut() {
        mFirebaseAuth.signOut();
        Intent intent = new Intent(this, Welcome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
