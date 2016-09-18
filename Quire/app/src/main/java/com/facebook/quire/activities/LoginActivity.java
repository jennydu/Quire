package com.facebook.quire.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.quire.Constants;
import com.facebook.quire.R;
import com.facebook.quire.models.User;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @BindView(R.id.email) EditText etEmail;
    @BindView(R.id.password) EditText etPassword;
    @BindView(R.id.btn_login) Button btnLogin;
    @BindView(R.id.link_signup) TextView tvSignup;
    @BindView(R.id.logo) ImageView ivLogo;

    private LoginButton btnFBLogin;

    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    String uid;
    User quireUser;
    String usersUrl;

    private String facebook_id,full_name,profile_image,email;

    ArrayList<String> existingFB;
    File myDir;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.facebook.quire",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/quire/");

        // Initialize calligraphy library for custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mDatabase = FirebaseDatabase.getInstance().getReference();

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        existingFB = new ArrayList<>();

        // Checking existing FB ids
//        mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for (DataSnapshot item : dataSnapshot.getChildren()) {
//                    Log.d("Key", item.getKey());
//                    Log.d("Value", item.getValue().toString());
//
//                    User currUser = item.getValue(User.class);
//                    String fbid = currUser.getFb_id();
//                    if (!fbid.equals("")) {
//                        existingFB.add(fbid);
//                    }
//
////                    mDatabase.child("users").child(item.getKey()).child("fb_id").addListenerForSingleValueEvent(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(DataSnapshot dataSnapshot) {
////                            String fbid = dataSnapshot.getValue().toString();
////                            existingFB.add(fbid);
////                        }
////
////                        @Override
////                        public void onCancelled(DatabaseError databaseError) {
////
////                        }
////                    });
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        // Setting up permissions for Facebook login
        btnFBLogin = (LoginButton) findViewById(R.id.btn_fblogin);
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
        //btnFBLogin.setReadPermissions("public_profile");

        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        // Get the shared instance of FirebaseAuth object
        mAuth = FirebaseAuth.getInstance();
        usersUrl = Constants.FIREBASE_URL + "/users";
        quireUser = new User();

        //Toast.makeText(getBaseContext(), "About to do facebook login",Toast.LENGTH_LONG).show();
        btnFBLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                //Toast.makeText(getBaseContext(), "Facebook Success",Toast.LENGTH_LONG).show();

                // Register Facebook user into auth
                AccessToken token= AccessToken.getCurrentAccessToken();
                handleFacebookAccessToken(token);

                // Grab user's info
                facebook_id = Profile.getCurrentProfile().getId();
                full_name = Profile.getCurrentProfile().getName();
                profile_image = Profile.getCurrentProfile().getProfilePictureUri(400,400).toString();
                Log.d("FB ID",facebook_id);
                Log.d("full name", full_name);
                Log.d("image URL",profile_image);
                //request.executeAsync();
                quireUser.setFull_name(full_name);
                quireUser.setFb_id(facebook_id);
                quireUser.setProfile_image_url(profile_image);
                quireUser.setScreen_name("");
                quireUser.setEmail("");

                // If facebook id already exists with an account, use existing Firebase
                // Else set up a new account
                if (existingFB.contains(facebook_id)) {
                    // Grab Firebase id for user with this facebook id
                    mDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                Log.d("Key", item.getKey());
                                Log.d("Value", item.getValue().toString());

                                User currUser = item.getValue(User.class);
                                String fbid = currUser.getFb_id();
                                if (fbid.equals(facebook_id)) {
                                    uid = currUser.getUser_id();
                                }
                                //uid = item.getKey();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    new Firebase(usersUrl).child(uid).setValue(quireUser);
                } else {
                    uid = new Firebase(usersUrl).push().getKey();
                    quireUser.setUser_id(uid);
                    new Firebase(usersUrl).child(uid).setValue(quireUser);
                }
                //onLoginSuccess: pass in facebook id
                Intent i = new Intent(LoginActivity.this, FeedActivity.class);
                i.putExtra("uid",uid);
                startActivity(i);
            }

            @Override
            public void onCancel() {
                //Toast.makeText(getBaseContext(),"facebook:onCancel",Toast.LENGTH_LONG).show();
                Log.d("FB: ", "Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                //Toast.makeText(getBaseContext(),"facebook:onError",Toast.LENGTH_LONG).show();
                Log.d("FB: ", "Error");
            }
        });

        // Set up AuthStateListener to respond to changes in user sign-in state
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    uid = user.getUid();
                    quireUser.setFb_id("");
                    quireUser.setUser_id(uid);
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        tvSignup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),SignUpActivity.class);
                startActivityForResult(i, REQUEST_SIGNUP);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void login() {

        if (!validate()) {
            validate();
            return;
        }

        btnLogin.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        // Authentication sign-in logic here
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            //Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            onLoginFailed();
                        } else {
                            //Toast.makeText(LoginActivity.this, "Authentication succeeded.",Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            onLoginSuccess();
                        }
                    }
                });
    }



    public void onLoginSuccess() {
        //Toast.makeText(getApplicationContext(),"onLoginSuccess",Toast.LENGTH_SHORT).show();
        btnLogin.setEnabled(true);

        Intent i = new Intent(this, FeedActivity.class);
        i.putExtra("uid",uid);
        startActivity(i);
    }

    public void onLoginFailed() {
        //Toast.makeText(getBaseContext(),"Login failed",Toast.LENGTH_LONG).show();
        btnLogin.setEnabled(true);
        etEmail.setError("Incorrect email or password");
        etPassword.setError("Incorrect email or password");
    }

    // Gets access token for user, exchange for Firebase credential, and authenticate using Firebase
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            //Toast.makeText(LoginActivity.this, "FB Authentication failed.",Toast.LENGTH_SHORT).show();
                            onLoginFailed();
                        } else {
                            Log.w(TAG, "signInWithCredentialSuccess");
                            //Toast.makeText(LoginActivity.this, "FB Authentication succeeded.",Toast.LENGTH_SHORT).show();
                            mDatabase.child("users").child(uid).setValue(quireUser);
                            onLoginSuccess();
                        }
                    }
                });
    }

    // Verify that the information typed is accurate
    public boolean validate() {
        boolean valid = true;

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            valid = false;
            etEmail.setError("Not a valid email address");
        } else {
            etEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 5) {
            valid = false;
            etPassword.setError("Password must be at least 5 alphanumeric characters");
        } else {
            etPassword.setError(null);
        }

        return valid;
    }


    // hide keyboard if you touch outside it
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
