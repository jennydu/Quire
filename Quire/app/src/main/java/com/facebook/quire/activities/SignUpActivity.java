package com.facebook.quire.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.quire.R;
import com.facebook.quire.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    @BindView(R.id.name) EditText etName;
    @BindView(R.id.email) EditText etEmail;
    @BindView(R.id.screen_name) EditText etScreenName;
    @BindView(R.id.password) EditText etPassword;
    @BindView(R.id.btn_signup) Button btnSignup;
    @BindView(R.id.link_login) TextView tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    ProgressDialog progressDialog;

    String uid;
    User quireUser;

    boolean isUsernameExist;
    boolean isEmailExist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        // Initialize calligraphy library for custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        quireUser = new User();
        isUsernameExist = false;
        isEmailExist = false;

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish registration screen and return to the Login activity
                finish();
            }
        });

        // Get shared instance of the FirebaseAuth object
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Setting up an AuthStateListener that responds to user's sign-in state
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    quireUser.setUser_id(user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
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

    public void signup() {

        if (!validate()) {
            validate();
            return;
        }

        btnSignup.setEnabled(false);

        progressDialog = new ProgressDialog(SignUpActivity.this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Creating Account...");
        progressDialog.show();

        String name = etName.getText().toString();
        final String screen_name = etScreenName.getText().toString();
        //isUsernameExist = isUsernameExists(screen_name);
        final String email = etEmail.getText().toString();
        //isEmailExist = isEmailExists(email);
        String password = etPassword.getText().toString();

        quireUser.setFull_name(name);
        quireUser.setScreen_name(screen_name);
        quireUser.setEmail(email);
        quireUser.setPassword(password);
        //quireUser.setFb_id("");
        quireUser.setProfile_image_url("https://www.hsjaa.com/images/joomlart/demo/default.jpg");

        Log.d(TAG, "quireUser: " + quireUser);

        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String existingUsername = (String) userSnapshot.child("screen_name").getValue();
                    if (existingUsername.equals(screen_name)) {
                        isUsernameExist = true;
                        if (isUsernameExist) {
                            etScreenName.setError("Username already in use");
                            return;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //some error thrown here
            }
        });

        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String existingEmail = (String) userSnapshot.child("email").getValue();
                    if (existingEmail.equals(email)) {
                        isEmailExist = true;
                        if (isEmailExist) {
                            etEmail.setError("Email already in use");
                            return;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                //some error thrown here
            }
        });

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Toast.makeText(getBaseContext(),TAG + "createUserWithEmail:onComplete:" + task.isSuccessful(),Toast.LENGTH_LONG);
                        if (!task.isSuccessful()) {
                            //Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            onSignUpFailed();
                        } else {
                            progressDialog.dismiss();
                            onSignUpSuccess();
                        }
                    }
                });
    }

    public void onSignUpSuccess() {
        progressDialog.dismiss();
        //Toast.makeText(getBaseContext(),"Signup success",Toast.LENGTH_LONG).show();
        btnSignup.setEnabled(true);
        writeNewUser(quireUser);
        //Log.d(TAG, "uid: " + uid);
//        String key = new Firebase(Constants.FIREBASE_URL).child("users").push().getKey();
//        quireUser.setUser_id(key);
//        new Firebase(Constants.FIREBASE_URL).child("users").child(key).setValue(quireUser);
        setResult(RESULT_OK,null);
        finish();
    }

    public void onSignUpFailed() {
        progressDialog.dismiss();
        //Toast.makeText(getBaseContext(),"Signup failed", Toast.LENGTH_LONG).show();
        btnSignup.setEnabled(true);
        //onResume();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void writeNewUser(User user) {
        mDatabase.child("users").child(user.getUser_id()).setValue(user);
    }

    // Validation steps to verify information is correctly typed
    public boolean validate() {
        boolean valid = true;

        String name = etName.getText().toString();
        String screen_name = etScreenName.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (name.isEmpty()) {
            etName.setError("Name field is empty");
            valid = false;
        } else {
            etName.setError(null);
        }

        if (screen_name.isEmpty()) {
            etScreenName.setError("Username field is empty");
            valid = false;
        } else {
            etScreenName.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            valid = false;
        } else {
            etEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 5) {
            etPassword.setError("Password must be at least 5 alphanumeric characters");
            valid = false;
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
