package com.facebook.quire.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.quire.Constants;
import com.facebook.quire.R;
import com.facebook.quire.models.Choice;
import com.facebook.quire.models.Quire;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetResultActivity2 extends AppCompatActivity {
    private static final String TAG = "SetResultActivity";

    @BindView(R.id.llContent) LinearLayout llContent;
    @BindView(R.id.llWrite) LinearLayout llWrite;
    @BindView(R.id.etText) EditText etText;
    @BindView(R.id.llOptions) LinearLayout llOptions;
    @BindView(R.id.ivPicture) ImageView ivPicture;
    @BindView(R.id.rlFinish) RelativeLayout rlFinish;
    @BindView(R.id.llPictureSpace) LinearLayout llPictureSpace;
    @BindView(R.id.ivGallery) ImageView ivGallery;
    @BindView(R.id.ivBackward) ImageView ivBack;


    Quire quire;
    String qid;
    Choice choice;
    String outcomeText;
    Uri imageUri;
    InputStream imageStream;
    private static final int CAMERA_REQUEST = 1888;


    Firebase base = new Firebase(Constants.FIREBASE_URL);
    StorageReference storageRef;
    UploadTask upTask;

    ArrayList<Uri> pics = new ArrayList<>();
    ArrayList<String> picUrls = new ArrayList<>();
    ArrayList<String> picIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_result2);
        ButterKnife.bind(this);

        // create storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl(Constants.STORAGE_URL);

        // Initialize calligraphy library for custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // get extras
        quire = Parcels.unwrap(getIntent().getParcelableExtra("quire"));
        //outcomeText = getIntent().getStringExtra("outcomeText");
        qid = quire.getQid();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // SUBMIT
        // on click listener
        rlFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // on submit

                // store info, turn into finished
                base.child("quires").child(qid).child("status").setValue("closed");
                base.child("quires").child(qid).child("outcomeText").setValue(etText.getText().toString());
                //quire.setOutcomeText(etText.getText().toString());

                // go to result activity
                finishActivity(quire);
            }
        });

        // on click listener for adding pictures
        ivPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePicture, CAMERA_REQUEST);

            }
        });

        // on click listener for adding pictures from gallery
        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , 1);
            }
        });


        // if we came from the edit activity
//        if (outcomeText != null){
//            etText.setText(outcomeText);
//        }

    }

    protected void finishActivity(Quire quire){
        Intent i = new Intent(getApplicationContext(), ResultActivity.class);
        i.putExtra("quire", Parcels.wrap(quire));
        i.putExtra("TAG",TAG);
        startActivity(i);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        ImageView ivAddedPic = new ImageView(getApplicationContext());
        final LinearLayout rlnewPic = new LinearLayout(getApplicationContext());
        ImageView ivDelete = new ImageView(getApplicationContext());
        rlnewPic.setOrientation(LinearLayout.HORIZONTAL);

        int ivId = ivAddedPic.getId();

        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == CAMERA_REQUEST) {
                    Bitmap cameraPhoto = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    imageUri = bitmapToUriConverter(cameraPhoto);
                    imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    selectedImage = getResizedBitmap(selectedImage, 800);
                    imageUri = bitmapToUriConverter(selectedImage);
                } else {
                    imageUri = imageReturnedIntent.getData();
                    if (getContentResolver().openInputStream(imageUri) != null) {
                        imageStream = getContentResolver().openInputStream(imageUri);
                    }
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    selectedImage = getResizedBitmap(selectedImage, 800);// 400 is for example, replace with desired size

                    imageUri = bitmapToUriConverter(selectedImage);
                }
                //Read selectedImage into a Bitmap and update selectedImage URI to make a smaller copy of the image
                //Scale to 400x400

                pics.add(imageUri);
                ivAddedPic.setImageURI(imageUri);
//                ivAddedPic.setMaxHeight(40);
//                ivAddedPic.setMaxWidth(40);
                Picasso.with(getApplicationContext())
                        .load(imageUri)
//                        .resize(300, 300)
                        .transform(new RoundedCornersTransformation(10,10))
                        .into(ivAddedPic);
                ivDelete.setImageResource(R.drawable.ic_x_icon);

                rlnewPic.addView(ivAddedPic);
                rlnewPic.addView(ivDelete);

                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        pics.remove(imageUri);
                        rlnewPic.setVisibility(View.GONE);
                    }
                });

                llPictureSpace.addView(rlnewPic);

                // get the unique id for the picture
                String picId = base.child("quires").child(quire.getQid()).child("result_pictures").push().getKey();
                picIds.add(picId);

                StorageReference picRef = storageRef.child("images/" + picId);

                // upload pic
                upTask = picRef.putFile(imageUri);
                upTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "upload unsuccessful. please try agian.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        ArrayList<String> urls = quire.getResultPicUrls();
                        urls.add(downloadUrl.toString());

                        base.child("quires").child(quire.getQid()).child("resultPicUrls").setValue(urls);

                    }
                });
            } catch (FileNotFoundException e) {

            }
        }

//        switch(requestCode) {
//            case 0:
//                if(resultCode == RESULT_OK){
//                    selectedImage = imageReturnedIntent.getData();
//                    pics.add(selectedImage);
//                    ivAddedPic.setImageURI(selectedImage);
//                    ivAddedPic.setMaxHeight(40);
//                    ivAddedPic.setMaxWidth(40);
//                    ivDelete.setImageResource(R.drawable.ic_x_icon);
//
//                    rlnewPic.addView(ivAddedPic);
//                    rlnewPic.addView(ivDelete);
//
//                    final Uri finalSelectedImage = selectedImage;
//                    ivDelete.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            pics.remove(finalSelectedImage);
//                            rlnewPic.setVisibility(View.GONE);
//                        }
//                    });
//
//                    llPictureSpace.addView(rlnewPic);
//
//                    //llPictureSpace.addView(ivAddedPic);
//                }
//
//                break;
//            case 1:
//                if(resultCode == RESULT_OK){
//                    selectedImage = imageReturnedIntent.getData();
//                    pics.add(selectedImage);
//                    ivAddedPic.setImageURI(selectedImage);
//                    ivAddedPic.setMaxHeight(40);
//                    ivAddedPic.setMaxWidth(40);
//                    ivDelete.setImageResource(R.drawable.ic_x_icon);
//
//                    rlnewPic.addView(ivAddedPic);
//                    rlnewPic.addView(ivDelete);
//
//                    final Uri finalSelectedImage = selectedImage;
//                    ivDelete.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            pics.remove(finalSelectedImage);
//                            rlnewPic.setVisibility(View.GONE);
//                        }
//                    });
//
//                    llPictureSpace.addView(rlnewPic);
//
//                    //llPictureSpace.addView(ivAddedPic);
//                }
//                break;
//        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
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
            options.inSampleSize = calculateInSampleSize(options, 391, 521);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 391, 521,
                    true);
            File file = new File(SetResultActivity2.this.getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out = SetResultActivity2.this.openFileOutput(file.getName(),
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

    private Uri getImageUri() {


        File file1 = new File(Environment.getExternalStorageDirectory() + "/Camerafolder");
        if (!file1.exists())
        {
            file1.mkdirs();
        }

        File file = new File(Environment.getExternalStorageDirectory() + "/Camerafolder/"+"img"+".png");

        Uri imgUri = Uri.fromFile(file);

        return imgUri;
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
