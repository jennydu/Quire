package com.facebook.quire.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.quire.CommentsAdapter;
import com.facebook.quire.Constants;
import com.facebook.quire.R;
import com.facebook.quire.models.Comment;
import com.facebook.quire.models.Quire;
import com.facebook.quire.models.User;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class FullscreenDetailActivity extends AppCompatActivity {
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
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    //ArrayList<Parcelable> quires;
    Quire quire;
    
    @BindView(R.id.ivProfileImage)
    ImageView ivProfileImage;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.tvUsername) TextView tvUsername;
    @BindView(R.id.tvTime) TextView tvTime;
    @BindView(R.id.tvDescription) TextView tvDescription;
    @BindView(R.id.tvQuestion) TextView tvQuestion;
    @BindView(R.id.tvNumComments) TextView tvNumComments;
    @BindView(R.id.rlPoll) RelativeLayout rlPoll;

    private DatabaseReference mDatabase;
    private String mUserId;
    DataSnapshot snapshot;
    RecyclerView rvComments;
    SwipeRefreshLayout swipeContainer;
    CommentsAdapter cAdapter;
    LinearLayoutManager mManager;
    User user;
    String qid;
    private DatabaseReference mChoices;

    // Also instantiate like, comment, share, poll options, upvote count

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen_detail);
        ButterKnife.bind(this);

        quire = Parcels.unwrap(getIntent().getParcelableExtra("quire"));
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserId = quire.getUid();
        qid = quire.getQid();

        mVisible = true;
        mContentView = findViewById(R.id.flContent);
        //Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Grab the corresponding user to the quire object

        // Load only comments whose quire ID matches current quire
        mDatabase.child("quires").child(qid).child("comments").orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                snapshot = dataSnapshot;
                cAdapter = new CommentsAdapter(FullscreenDetailActivity.this);
                Comment.fromIdDataSnapshot(dataSnapshot, cAdapter);

                mDatabase.child("users").child(mUserId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get user value
                                user = dataSnapshot.getValue(User.class);
                                tvName.setText(user.getFull_name());
                                tvUsername.setText(user.getScreen_name());
                                Picasso.with(getBaseContext()).load(user.getProfile_image_url()).transform(new RoundedCornersTransformation(10,10)).fit().into(ivProfileImage);

                                // load into the views
                                tvTime.setText(quire.getCreatedAt());
                                tvDescription.setText(quire.getDescriptionText());
                                tvQuestion.setText(quire.getQuestionText());

                                tvNumComments.setText(quire.getCommentCount() + "");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                            }
                        });

                rvComments.setAdapter(cAdapter);
                //aComments.notifyDataSetChanged();
                //lvComments.smoothScrollToPosition(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setUpViews();

        //load the choices, including the functionality for the user to vote on them from here.

        mChoices = mDatabase.child("choices");
        final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Get the data model based on position
        final String quireID = quire.getQid();
        //temporary: check if user has voted before or not.
        mDatabase.child("quires").child(quireID).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != 0) {
                    //then "USERS" exists, someone has voted on this poll before
                    boolean userHasVoted = false;
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        //currently iterating through all users who have voted
                        if (s.getKey().equals(currentUserId)) {
                            userHasVoted = true;
                            loadFinishedPoll(quire, currentUserId, quireID);
                        }
                    }
                    if (!userHasVoted) {
                        loadPoll(quire, currentUserId, quireID);
                    }
                } else {
                    //then no one has ever voted on this poll before
                    Log.d("lalala", "null snapshot");
                    loadPoll(quire, currentUserId, quireID);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void loadFinishedPoll(final Quire quire, final String currentUserId, final String quireID) {

        rlPoll.removeAllViews();
        final RadioGroup rgPoll = new RadioGroup(this);
        rgPoll.setOnCheckedChangeListener(null);
        rgPoll.clearCheck();

        //get the user's vote
        mDatabase.child("users").child(currentUserId).child("answeredQuires").child(quireID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("old vote id is ", (String) dataSnapshot.getValue());
                String userChoiceId = (String) dataSnapshot.getValue();

                //then populate radiogroup while checking if it is matching
                int choicesCount = (int) quire.getChoicesCount();
                for (int i = 0; i < choicesCount; i++) {
                    String currentChoiceId = quire.getChoices().get(i).getCid();

                    final LinearLayout llButton = new LinearLayout(getApplicationContext());
                    llButton.setOrientation(LinearLayout.HORIZONTAL);

                    //add a checkbox for every option present
                    final RadioButton option = new RadioButton(getApplicationContext());
                    option.setId(i);
                    option.setText(quire.getChoices().get(i).getText());
                    option.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColorPrimary));
                    option.setPadding(0, 5, 0, 0);
                    llButton.addView(option);

                    Log.d("current choice", currentChoiceId);
                    Log.d("user choice", userChoiceId);
                    //if this choice is the voted on one, then make it checked
                    if (currentChoiceId.equals(userChoiceId)) {
                        Log.d("choices match", "yay");
                        option.setChecked(true);
                    }

                    //disable the poll
                    option.setEnabled(false);

                    //to print the vote results

                    final TextView etVotes = new TextView(getApplicationContext());

                    //get number of votes
                    mChoices.child(currentChoiceId).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long votes = (long) dataSnapshot.getValue();
                            etVotes.setText("       :" + votes + " votes");
                            etVotes.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColorPrimary));
                            llButton.addView(etVotes);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    rgPoll.addView(llButton);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //then add textviews to display the total number of votes so far, or their usernames
        rlPoll.addView(rgPoll);
    }

    private void loadPoll(final Quire quire, final String currentUserId, final String quireID) {

        RadioGroup rgPoll = new RadioGroup(this);
        rgPoll.setOnCheckedChangeListener(null);
        rgPoll.clearCheck();

        int choicesCount = (int) quire.getChoicesCount();
        for (int i = 0; i < choicesCount; i++) {
            //add a checkbox for every option present
            final RadioButton option = new RadioButton(getApplicationContext());
            option.setId(i);
            option.setText(quire.getChoices().get(i).getText());
            option.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.textColorPrimary));

            rgPoll.addView(option);
        }

        //LISTENER FOR VOTING
        rgPoll.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                //1. put user in list of users for this choice
                final String choiceId = quire.getChoices().get(checkedId).getCid();
                Map<String, Object> users = new HashMap<String, Object>();
                users.put(currentUserId, "hi");
                mChoices.child(choiceId).child("users").updateChildren(users);
                //2. update total # of votes for choice object
                //get current number of votes, and add one
                //TODO: and take one away from the one they changed from, if that's the case

                mChoices.child(choiceId).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mChoices.child(choiceId).child("votes").setValue((long) dataSnapshot.getValue() + (long) 1);

                        //3. add user to list of users who have voted in this quire
                        Map<String, Object> votedUsers = new HashMap<String, Object>();
                        votedUsers.put(currentUserId, "hi");
                        mDatabase.child("quires").child(quireID).child("users").updateChildren(votedUsers);

                        //4. add qid, cid under user.
                        Map<String, Object> answeredQuires = new HashMap<String, Object>();
                        answeredQuires.put(quireID, choiceId);
                        mDatabase.child("users").child(currentUserId).child("answeredQuires").updateChildren(answeredQuires);

                        //5. then update the ui

                        loadFinishedPoll(quire, currentUserId, quireID);
                        //update votes
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
        rlPoll.addView(rgPoll);
    }

    public void setUpViews() {
        rvComments = (RecyclerView) findViewById(R.id.rvComments);
        mManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(mManager);

        // Swipe to Refresh
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                refreshComments();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void refreshComments() {
        // Look under quires/comments for CommentID's then load the comments with those ID's

        mDatabase.child("quires").child(qid).child("comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (snapshot != dataSnapshot) {
                    Comment.fromIdDataSnapshot(dataSnapshot, cAdapter);
                    //aComments.addAll(comments);
                    swipeContainer.setRefreshing(false);
                    rvComments.setAdapter(cAdapter);
                    //aComments.notifyDataSetChanged();
                } else {
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void onCommentClick(View view) {
        final Comment comment = new Comment();

        //Toast.makeText(getBaseContext(), "Clicked a button",Toast.LENGTH_LONG).show();
        String commentsUrl = Constants.FIREBASE_URL + "/comments/";
        final String quiresUrl = Constants.FIREBASE_URL + "/quires/";
        String key = new Firebase(commentsUrl).push().getKey();

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        comment.setQuireId(quire.getQid());
        comment.setUserId(currentUserId);
        comment.setInvertedTimestamp(0 - System.currentTimeMillis());

        //comment.setCreatedAt(ServerValue.TIMESTAMP);

        // Store a new Comment object into database

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, "comment");

        new Firebase(quiresUrl).child(quire.getQid()).child("comments").updateChildren(map);
        comment.setCommentId(key);
        new Firebase(commentsUrl).child(key).setValue(comment);
        new Firebase(commentsUrl).child(key).child("timestamp").setValue(ServerValue.TIMESTAMP);


        //why not incrementing?
        //increment comments count
        mDatabase.child("quires").child(quire.getQid()).child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDatabase.child("quires").child(quire.getQid()).child("commentCount").setValue((long) dataSnapshot.getValue() + (long) 1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //refresh comment # instantaneously
        int currentComments = Integer.parseInt(tvNumComments.getText().toString()) + 1;
        tvNumComments.setText(currentComments + "");

    }

}
