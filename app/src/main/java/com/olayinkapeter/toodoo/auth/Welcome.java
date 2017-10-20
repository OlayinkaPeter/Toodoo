package com.olayinkapeter.toodoo.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.Resource;
import com.olayinkapeter.toodoo.MainActivity;
import com.olayinkapeter.toodoo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class Welcome extends AppCompatActivity implements View.OnClickListener {
    static boolean calledAlready = false;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (!calledAlready) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            calledAlready = true;
        }

        super.onCreate(savedInstanceState);
        // Initialize Firebase Auth and Database Reference
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser!= null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Not logged in, Therefore remain.
        ImageView welcomeLogo = (ImageView) findViewById(R.id.welcome_logo);
        Button logInButton = (Button) findViewById(R.id.loginButton);
        Button signInButton = (Button) findViewById(R.id.signinButton);

        Glide.with(this).load(R.drawable.ic_web)
                .placeholder(R.drawable.ic_web)
                .into(welcomeLogo);

        logInButton.setOnClickListener(this);
        signInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginButton:
                intent = new Intent(Welcome.this, Login.class);
                startActivity(intent);
                break;
            case R.id.signinButton:
                intent = new Intent(Welcome.this, SignUp.class);
                startActivity(intent);
                break;
        }
    }
}