package com.facebook.quire.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.facebook.quire.CircleTransform;
import com.facebook.quire.R;
import com.facebook.quire.fragments.AnsweredTimelineFragment;
import com.facebook.quire.fragments.AskedTimelineFragment;
import com.facebook.quire.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener{

    String mUserId;
    DatabaseReference mDatabase;
    User user;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.tvUsername) TextView tvUsername;
    @BindView(R.id.ivProfileImage) ImageView ivProfileImage;
    @BindView(R.id.ivEdit) ImageView ivEdit;
    @BindView(R.id.tvEmail) TextView tvEmail;
    @BindView(R.id.main_textview_title) TextView tvTitle;
    @BindView(R.id.ivImage) CircleImageView ivImage;
    @BindView(R.id.llTransition) LinearLayout llTransition;
    Toolbar toolbar;

    AskedTimelineFragment askedTimeline;
    AnsweredTimelineFragment answeredTimeline;
//    String numAskedQuires;
//    String numAnsweredQuires;
    //CollapsingToolbarLayout collapsingToolbar;
    PagerSlidingTabStrip tabStrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        // Setting back arrow to return to ProfileActivity
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back_light));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        llTransition.setVisibility(View.GONE);

        // Initialize calligraphy library for custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mUserId = getIntent().getStringExtra("uid");
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Picasso.with(ProfileActivity.this).load(R.drawable.ic_edit).transform(new CircleTransform()).into(ivEdit);

        // Populates profile header
        mDatabase.child("users").child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //snapshot = dataSnapshot;
                user = dataSnapshot.getValue(User.class);
                tvName.setText(user.getFull_name());

                //collapsingToolbar.setTitle(user.getFull_name());

                tvUsername.setText(user.getScreen_name());
                tvEmail.setText(user.getEmail());
                tvTitle.setText(user.getFull_name());
                Picasso.with(ProfileActivity.this).load(user.getProfile_image_url()).transform(new CircleTransform()).fit().into(ivProfileImage);
                Picasso.with(ProfileActivity.this).load(user.getProfile_image_url()).transform(new CircleTransform()).fit().into(ivImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        setUpViews();
        bindActivity();
        mAppBarLayout.addOnOffsetChangedListener(this);
        startAlphaAnimation(mTitle,0,View.INVISIBLE);
    }

    public void setUpViews() {
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

//        // Gets number of quires user has asked
//        mDatabase.child("users").child(mUserId).child("quires").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    long askedQuires = dataSnapshot.getChildrenCount();
//                    numAskedQuires = String.valueOf(askedQuires);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        // Gets number of quires user has answered
//        mDatabase.child("users").child(mUserId).child("answeredQuires").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() != null) {
//                    long answeredQuires = dataSnapshot.getChildrenCount();
//                    numAnsweredQuires = String.valueOf(answeredQuires);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        viewPager.setAdapter(new ProfilePagerAdapter(getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabStrip.setTextColor(Color.parseColor("#1abc9c"));
        // Attach the view pager to the tab strip
        tabStrip.setViewPager(viewPager);

        askedTimeline = AskedTimelineFragment.newInstance(mUserId);
        answeredTimeline = AnsweredTimelineFragment.newInstance(mUserId);
    }

    public void onEditClick(View view) {
        Intent i = new Intent(ProfileActivity.this, EditActivity.class);
        i.putExtra("uid",mUserId);
        startActivity(i);
    }

    public void onDoneClick(View view) {
        finish();
    }

    public class ProfilePagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[] { "Asked Quires", "Answered Quires" };

        String numAnsweredQuires;
        String numAskedQuires;

        public ProfilePagerAdapter(FragmentManager fm) {
            super(fm);


            // Gets number of quires user has asked
            mDatabase.child("users").child(mUserId).child("quires").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        long askedQuires = dataSnapshot.getChildrenCount();
                        numAskedQuires = String.valueOf(askedQuires);
                        Log.d("asked", numAskedQuires);
//                        tabStrip.notifyDataSetChanged();
//
//                        LinearLayout mTabsLinearLayout = ((LinearLayout) tabStrip.getChildAt(0));
//                            TextView tv = (TextView) mTabsLinearLayout.getChildAt(0);
//                            tv.setText(numAskedQuires + " Asked Quires");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            // Gets number of quires user has answered
            mDatabase.child("users").child(mUserId).child("answeredQuires").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        long answeredQuires = dataSnapshot.getChildrenCount();
                        numAnsweredQuires = String.valueOf(answeredQuires);
                        Log.d("answered", numAnsweredQuires);
                        tabStrip.notifyDataSetChanged();

//                        LinearLayout mTabsLinearLayout = ((LinearLayout) tabStrip.getChildAt(1));
//                            TextView tv = (TextView) mTabsLinearLayout.getChildAt(1);
//                            tv.setText(numAnsweredQuires + " Answered Quires");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return askedTimeline;
            } else if (position == 1) {
                return answeredTimeline;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            String tabTitle = "";
            if (position == 0) {
                tabTitle = tabTitles[position];
                Log.d("tab", tabTitle);
            } else if (position == 1) {
                tabTitle = tabTitles[position];
                Log.d("tab", tabTitle);
            }
            return tabTitle;
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheLayoutVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private RelativeLayout mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private ImageView mImage;
    private LinearLayout mHeaderLayout;

    private void bindActivity() {
        mToolbar        = (Toolbar) findViewById(R.id.toolbar);
        mTitle          = (TextView) findViewById(R.id.main_textview_title);
        mTitleContainer = (RelativeLayout) findViewById(R.id.rlProfileHeader);
        mAppBarLayout   = (AppBarLayout) findViewById(R.id.appbar);
        mImage          = (ImageView) findViewById(R.id.ivImage);
        mHeaderLayout   = (LinearLayout) findViewById(R.id.llTransition);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        //handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
        //handleAlphaOnPicture(percentage);
        handleAlphaOnLayout(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    private void handleAlphaOnLayout(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheLayoutVisible) {
                llTransition.setVisibility(View.VISIBLE);
                toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                startAlphaAnimation(mImage, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheLayoutVisible = true;
            }

        } else {

            if (mIsTheLayoutVisible) {
                llTransition.setVisibility(View.GONE);
                toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                startAlphaAnimation(mImage, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheLayoutVisible = false;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
}
