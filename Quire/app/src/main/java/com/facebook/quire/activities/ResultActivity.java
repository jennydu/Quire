package com.facebook.quire.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.quire.CircleTransform;
import com.facebook.quire.Constants;
import com.facebook.quire.R;
import com.facebook.quire.models.Choice;
import com.facebook.quire.models.Quire;
import com.facebook.quire.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ResultActivity extends AppCompatActivity {


    private static final String TAG = "DetailActivity";
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;


    Quire quire;

    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.tvUsername) TextView tvScreenName;
    @BindView(R.id.tvDescription) TextView tvDescription;
    @BindView(R.id.tvQuestion) TextView tvQuestion;
    @BindView(R.id.tvResult) TextView tvResult;
    @BindView(R.id.tvResultHeader) TextView tvResultHeader;
    //@BindView(R.id.llChoices) LinearLayout llChoices; // add the choices dynamically to here from the database
//    @BindView(R.id.llResult) LinearLayout llResult;
    @BindView(R.id.llDisplayPics) LinearLayout llPictureSpace;
    @BindView(R.id.tvResultDescription) TextView tvResultDescription;
//    @BindView(R.id.cvVoters) VotersIcon cvVoters;
//    @BindView(R.id.cvComments) CommentsIcon cvComments;

    private DatabaseReference mDatabase;
    private StorageReference storageRef;

    User user;
    private String mUserId;
    String finishedQid;

    String finalChoiceID;
    String choiceText;

    ArrayList<Choice> choices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setting up views
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        // Setting back arrow to return to ProfileActivity
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_x_icon));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check to make sure comment box is not blank before letting user close
                if (getIntent().getStringExtra("TAG") != null && getIntent().getStringExtra("TAG").equals("SetResultActivity")) {
                    Intent i = new Intent(ResultActivity.this,FeedActivity.class);
                    i.putExtra("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    startActivity(i);
                } else {
                    finish();
                }
            }
        });

        // Initialize calligraphy library for custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // setting up firebase and storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReferenceFromUrl(Constants.STORAGE_URL);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        try {
            mUserId = mAuth.getCurrentUser().getUid(); // gets the user id
        } catch (Exception e) {
            loadLoginView();
        }

        // getting intent and extras
        quire = Parcels.unwrap(getIntent().getParcelableExtra("quire"));
        final String qid = quire.getQid();
        String status = quire.getStatus();

        final ArrayList<ImageView> ivPics = new ArrayList<>();

        mVisible = true;
        mContentView = findViewById(R.id.flContent);


        // Grab the corresponding user to the quire object
        Log.d(TAG, "quire object" + quire);
        String user_id = quire.getUid();

        String quiresUrl = Constants.FIREBASE_URL + "/quires/" + qid;

        //String user_id = getIntent().getStringExtra("uid");
        Log.d(TAG, "user_id" + user_id);
        //Log.d(TAG, "quire_id: " + quire.getQid());

        mDatabase.child("users").child(user_id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        user = dataSnapshot.getValue(User.class);
                        // load into the views
                        tvScreenName.setText(user.getScreen_name());

                        //only display description if it is present
                        if (!quire.getDescriptionText().isEmpty()) {
                            tvDescription.setText(quire.getDescriptionText());
                        } else {
                            tvDescription.setVisibility(View.GONE);
                        }

                        tvQuestion.setText(quire.getQuestionText());
                        tvResultHeader.setText(user.getScreen_name() + " chose:");
                        Picasso.with(ResultActivity.this).load(user.getProfile_image_url()).transform(new CircleTransform()).into(ivProfileImage);
                        Log.d(TAG, "user: " + user);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

        mDatabase.child("quires").child(qid).child("outcomeText").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String outcomeText = dataSnapshot.getValue().toString();
                if (!outcomeText.isEmpty()) {
                    tvResultDescription.setText(outcomeText);
                } else {
                    tvResultDescription.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // load any result pictures
        mDatabase.child("quires").child(qid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot s : dataSnapshot.child("resultPicUrls").getChildren()){

                    ImageView ivPic = new ImageView(getApplicationContext());
//                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                            ViewGroup.LayoutParams.MATCH_PARENT,
//                            ViewGroup.LayoutParams.MATCH_PARENT);
//                    ivPic.setLayoutParams(layoutParams);

//                    ivPic.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                    String url = s.getValue().toString();
                    ivPic.setTag(url);

                    Picasso.with(getApplicationContext())
                            .load(url)
//                            .fit()
                            .resize(1134, 1512)
//                            .centerInside()
                            .centerCrop() //fills requested bounds of imageview, and crops the extra.
//                            .onlyScaleDown() //only resize image if bigger than resize size
                            .transform(new RoundedCornersTransformation(10,10))
                            .into(ivPic);

                    ivPics.add(ivPic);
                    llPictureSpace.addView(ivPic);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        mDatabase.child("quires").child(qid).child("totalVotes").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                cvVoters.setValue((long) dataSnapshot.getValue() + "");
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        for (final ImageView iv : ivPics){
            final String url = iv.getTag().toString();
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getApplicationContext(), FullscreenPictureActivity.class);
                    i.putExtra("url", url);
                    startActivity(i);
                }
            });
        }

        // Grab the final result
        choices = quire.getChoices();

        finalChoiceID = quire.getOutcome();
        mDatabase.child("choices").child(finalChoiceID).child("text").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String finalChoice = dataSnapshot.getValue().toString();
                tvResult.setText(finalChoice);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);


    }

    private void loadLoginView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}