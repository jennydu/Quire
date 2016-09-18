package com.facebook.quire.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.quire.CircleTransform;
import com.facebook.quire.Constants;
import com.facebook.quire.R;
import com.facebook.quire.models.User;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EditActivity extends AppCompatActivity{
    private static final String TAG = "EditActivity";

    @BindView(R.id.etName) EditText etName;
    @BindView(R.id.etPassword) EditText etPassword;
    @BindView(R.id.etUsername) EditText etUsername;
    @BindView(R.id.etEmail) EditText etEmail;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.ivEditImage) ImageView ivEditImage;
    Toolbar toolbar;

    String mUserId;
    String name;
    String password;
    String username;
    String email;
    String profile_url;

    User user;
    DatabaseReference mDatabase;
    String newProfileImage;
    StorageReference storageRef;
    UploadTask upTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        // toolbar stuff
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        // Set up custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        // Setting back arrow to return to ProfileActivity
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_light));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl(Constants.STORAGE_URL);

        mUserId = getIntent().getStringExtra("uid");

        mDatabase.child("users").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                name = user.getFull_name();
                username = user.getScreen_name();
                password = user.getPassword();
                email = user.getEmail();
                profile_url = user.getProfile_image_url();

                etName.setText(name);
                etUsername.setText(username);
                etPassword.setText(password);
                etEmail.setText(email);
                Picasso.with(EditActivity.this).load(profile_url).transform(new CircleTransform()).into(ivProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ivEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto,1);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        Uri imageUri;
        String picId = new Firebase(Constants.FIREBASE_URL).child("users").child(mUserId).push().getKey();
        StorageReference picRef = storageRef.child("images").child(picId);

        if (resultCode == RESULT_OK) {
            try {
                imageUri = imageReturnedIntent.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                selectedImage = getResizedBitmap(selectedImage, 400);// 400 is for example, replace with desired size

                imageUri = bitmapToUriConverter(selectedImage);
                //Read selectedImage into a Bitmap and update selectedImage URI to make a smaller copy of the image
                //Scale to 400x400

                upTask = picRef.putFile(imageUri);
                upTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Upload unsuccessful, please try again", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();

                        //Saving new profile image
                        new Firebase(Constants.FIREBASE_URL).child("users").child(mUserId).child("profile_image_url").setValue(downloadUrl.toString());
                        profile_url = downloadUrl.toString();
                        Picasso.with(EditActivity.this).load(profile_url).transform(new CircleTransform()).fit().into(ivProfileImage);
                        user.setProfile_image_url(profile_url);
                    }
                });
            } catch (FileNotFoundException e) {

            }
        }
    }

    public void saveUserChanges(View view) {
        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

        currUser.updateEmail(etEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User email address updated.");
                        }
                    }
                });
        currUser.updatePassword(etPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User password updated.");
                        }
                    }
                });

        name = etName.getText().toString();
        password = etPassword.getText().toString();
        username = etUsername.getText().toString();
        email = etEmail.getText().toString();

        user.setFull_name(name);
        user.setPassword(password);
        user.setScreen_name(username);
        user.setEmail(email);
        Picasso.with(EditActivity.this).load(profile_url).transform(new CircleTransform()).fit().into(ivProfileImage);

        DatabaseReference mRef = mDatabase.child("users").child(mUserId);
        mRef.child("name").setValue(name);
        mRef.child("password").setValue(password);
        mRef.child("screen_name").setValue(username);
        mRef.child("email").setValue(email);
        finish();
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public Uri bitmapToUriConverter(Bitmap mBitmap) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 200, 200,
                    true);
            File file = new File(EditActivity.this.getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out = EditActivity.this.openFileOutput(file.getName(),
                    Context.MODE_WORLD_READABLE);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //get absolute path
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;
    }

    public int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
