package com.facebook.quire.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.facebook.quire.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by claudiawu on 7/29/16.
 */
public class StartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        final ProgressDialog progressDialog = new ProgressDialog(StartActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Launching Quire...");
        progressDialog.show();

        mAuth = FirebaseAuth.getInstance();
        // Set up AuthStateListener to respond to changes in user sign-in state
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    uid = user.getUid();
                    progressDialog.dismiss();
                    onLoginSuccess();
                } else {
                    progressDialog.dismiss();
                    onLoginFailed();
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void onLoginSuccess() {
        //Toast.makeText(getApplicationContext(),"onLoginSuccess",Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, FeedActivity.class);
        i.putExtra("uid",uid);
        startActivity(i);
    }

    public void onLoginFailed() {
        //Toast.makeText(getApplicationContext(),"onLoginFailed",Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}
