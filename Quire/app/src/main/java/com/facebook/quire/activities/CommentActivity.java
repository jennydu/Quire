package com.facebook.quire.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.quire.CircleTransform;
import com.facebook.quire.Constants;
import com.facebook.quire.R;
import com.facebook.quire.fragments.CommentsListFragment;
import com.facebook.quire.models.Comment;
import com.facebook.quire.models.User;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by claudiawu on 7/26/16.
 */
public class CommentActivity extends AppCompatActivity{
    @BindView(R.id.etComment) EditText etComment;
    @BindView(R.id.ivCommentImage) ImageView ivImage;

    DatabaseReference mDatabase;

    String qid;
    String uid;
    int commentCount;
//    ArrayList<Comment> comments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
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
                if (!etComment.getText().toString().equals("")) {
                    new AlertDialog.Builder(CommentActivity.this)
                            .setTitle("Discard comment")
                            .setMessage("Are you sure you want to discard your comment?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with exit
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
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

        qid = getIntent().getStringExtra("qid");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Picasso.with(CommentActivity.this).load(user.getProfile_image_url()).transform(new CircleTransform()).fit().into(ivImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (savedInstanceState == null) {
            CommentsListFragment comments = CommentsListFragment.newInstance(qid);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flCommentsContainer,comments);
            ft.commit();
        }
    }

    public void onCommentClick(View view) {
        Comment comment = new Comment();

        //Toast.makeText(getBaseContext(), "Clicked a button",Toast.LENGTH_LONG).show();
        String commentText = etComment.getText().toString();
        if (commentText.equals("")) {
            new AlertDialog.Builder(CommentActivity.this)
                    .setTitle("Invalid comment")
                    .setMessage("Make sure your comment is valid")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // stop comment from submitting
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            String commentsUrl = Constants.FIREBASE_URL + "/comments/";
            final String quiresUrl = Constants.FIREBASE_URL + "/quires/";
            String usersUrl = Constants.FIREBASE_URL + "/users/";
            String key = new Firebase(commentsUrl).push().getKey();

            //String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            comment.setText(commentText);
            comment.setQuireId(qid);
            comment.setUserId(uid);
            comment.setInvertedTimestamp(0 - System.currentTimeMillis());
//        comments.add(comment);
//        commentCount = comments.size();

            //quire.setCommentCount(commentCount+1);
            //comment.setCreatedAt(ServerValue.TIMESTAMP);

            // Store a new Comment object into database

            Map<String, Object> map = new HashMap<String, Object>();
            map.put(key, "comment");

            Map<String,Object> userMap = new HashMap<>();
            userMap.put(qid,key);

            new Firebase(quiresUrl).child(qid).child("comments").updateChildren(map);
//        new Firebase(quiresUrl).child(qid).child("commentCount").setValue(commentCount);
            new Firebase(usersUrl).child(uid).child("commentedQuires").updateChildren(userMap);
            comment.setCommentId(key);
            new Firebase(commentsUrl).child(key).setValue(comment);
            new Firebase(commentsUrl).child(key).child("timestamp").setValue(ServerValue.TIMESTAMP);
            etComment.setText("");

            new Firebase(quiresUrl).child(qid).child("commentCount").addListenerForSingleValueEvent(new com.firebase.client.ValueEventListener() {
                @Override
                public void onDataChange(com.firebase.client.DataSnapshot dataSnapshot) {
                    new Firebase(quiresUrl).child(qid).child("commentCount").setValue((long) dataSnapshot.getValue() + 1);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }

    }

    // setting up font
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // hide keyboard if touch outside et
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
