package com.facebook.quire.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.facebook.quire.CircleTransform;
import com.facebook.quire.Constants;
import com.facebook.quire.ItemClickSupport;
import com.facebook.quire.QuiresAdapter;
import com.facebook.quire.R;
import com.facebook.quire.models.Quire;
import com.facebook.quire.models.User;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FeedActivity extends AppCompatActivity {
    private static final String TAG = "FeedActivity";

    private int REQUEST_CODE = 50;

    protected DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private ArrayList<Quire> quires;
    private RecyclerView rvQuires;
    private QuiresAdapter adapter;

    private Firebase mRef = new Firebase(Constants.FIREBASE_URL);

    private DatabaseReference mDatabase;
    private DatabaseReference mQuireRef;

    private String mUserId;

    String user_id;
    String profile_image_url;
    String screen_name;
    String full_name;
    String email;
    String password;
    String description;

    User user;

    ImageView ivHeaderPhoto;
    TextView tvLayoutHandle;
    TextView tvLayoutName;
    TextView tvLayoutEmail;
    String uid;

    //private FirebaseRecyclerAdapter<Quire, QuiresAdapter. lder> mAdapter;

    private LinearLayoutManager mManager;
    private SwipeRefreshLayout swipeContainer;

    ArrayList<Quire> newQuires;

    DataSnapshot snapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_feed);
        ButterKnife.bind(this);

        // Initialize calligraphy library for custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");

        uid = getIntent().getStringExtra("uid");
        setUpViews();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mQuireRef = mDatabase.child("quires");

        newQuires = new ArrayList<>();

        //load data the first time
        mQuireRef.orderByChild("invertedTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                snapshot = dataSnapshot;
                //Toast.makeText(getBaseContext(), "TESTING", Toast.LENGTH_SHORT).show();
                quires = Quire.fromDataSnapshot(dataSnapshot);
                adapter = new QuiresAdapter(FeedActivity.this);
                adapter.setQuires(quires);
                rvQuires.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //what I want:
        //first time: gets and loads all the quires
        //on a change: temporarily store the info
        //on pull to refresh: if there is info stored, then reload the page with new info
        //else don't do anything

        //problems: LOADED THREE TIMES
        //FIRSTTIME IS NOT  BEING CHECKED CORRECTLY

//                if (firstTime) {
//                    firstTime = false;

//                }
//                //any other time that there is a change
//                else {
//                    //hasChanges = true;
//                    Log.d(TAG, dataSnapshot.toString());
//                    quires.clear();
//                    //add the new quires to our quires arraylist
//                    for (Quire q : Quire.fromDataSnapshot(dataSnapshot)) {
//                        quires.add(q); //add the quire to the arraylist
//                    }
//
//                    //this is an arraylist of the changed quires
//                    ArrayList<Quire> newQ = Quire.fromDataSnapshot(dataSnapshot);
//                    for (Quire q : newQ) {
//                        newQuires.add(0, q);
//                    }
        //load it here

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //check Authentication
        try {
            mUserId = mAuth.getCurrentUser().getUid(); // gets the user id
        } catch (Exception e) {
            loadLoginView();
        }

        //single value event only triggers once.
        mDatabase.child("users").child(uid).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        user = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "User: " + user);

                        profile_image_url = user.getProfile_image_url();
                        screen_name = user.getScreen_name();
                        full_name = user.getFull_name();
                        email = user.getEmail();
                        password = user.getPassword();
                        description = user.getDescription();

                        //Populate nav_header in drawer
                        tvLayoutHandle.setText(user.getScreen_name());
                        tvLayoutName.setText(user.getFull_name());
                        tvLayoutEmail.setText(user.getEmail());
                        Picasso.with(FeedActivity.this).load(user.getProfile_image_url()).transform(new CircleTransform()).fit().into(ivHeaderPhoto);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });

    }

    public void setUpViews() {

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        //View headerLayout = nvDrawer.inflateHeaderView(R.layout.nav_header);

        View headerLayout = nvDrawer.getHeaderView(0);
        ivHeaderPhoto = (ImageView) headerLayout.findViewById(R.id.ivPic);
        tvLayoutHandle = (TextView) headerLayout.findViewById(R.id.layout_handle);
        tvLayoutName = (TextView) headerLayout.findViewById(R.id.layout_name);
        tvLayoutEmail = (TextView) headerLayout.findViewById(R.id.layout_email);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        final ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
        {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_hamburger);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        rvQuires = (RecyclerView) findViewById(R.id.rvQuires);

        ItemClickSupport.addTo(rvQuires).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // do it
                        //Toast.makeText(getBaseContext(),"Clicked a quire", Toast.LENGTH_LONG).show();

                        //get the article to display
                        Quire quire = quires.get(position);

                        // see status of the quire
                        String status = quire.getStatus();
                        String authorId = quire.getUid();
                        rvQuires.scrollToPosition(position);
                        // case work

                        if (status.equals("closed")) {
                            Intent i = new Intent(FeedActivity.this, ResultActivity.class);
                            i.putExtra("quire",Parcels.wrap(quire));
                            startActivity(i);
                        }
                    }
                }
        );
        mManager = new LinearLayoutManager(this);
        rvQuires.setLayoutManager(mManager);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


    }

    public void fetchTimelineAsync() {
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Quire> quires2 = Quire.fromDataSnapshot(dataSnapshot);
                if (snapshot != dataSnapshot) {
                    adapter.clear();
                    //Toast.makeText(getBaseContext(), "REFRESHING", Toast.LENGTH_SHORT).show();

                    //adapter = new QuiresAdapter(FeedActivity.this, quires2);
                    adapter.addAll(quires2);
                    snapshot = dataSnapshot;
                    rvQuires.setAdapter(adapter);
                    swipeContainer.setRefreshing(false);
                }
                else {
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mQuireRef.orderByChild("invertedTimestamp").addListenerForSingleValueEvent(listener);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        menuItem.setVisible(true);
        Fragment fragment = null;
        Class fragmentClass = null;
        int id = menuItem.getItemId();
        if (id == R.id.nav_profile) {
            Intent i = new Intent(this, ProfileActivity.class);
            i.putExtra("uid",getUid());
            startActivity(i);
        }
        if (id == R.id.nav_settings) {
            Intent i = new Intent(this,EditActivity.class);
            //Toast.makeText(getBaseContext(),"Starting Edit Profile Activity",Toast.LENGTH_LONG).show();
            i.putExtra("uid",getUid());
            startActivity(i);
        }
        if (id == R.id.nav_logout) {
            // If user logged in through facebook
            if (getIntent().getStringExtra("fb_id") != null) {
                LoginManager.getInstance().logOut();
            } else {
                // else if user logged in through firebase
                mRef.unauth();
            }
            //then log out here and go to login screen
            Intent i = new Intent(this, LoginActivity.class);
            //Toast.makeText(this, "logging out", Toast.LENGTH_SHORT).show();
            startActivity(i);
            finish();
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        //setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //onclicks

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


//    public void onProfileClick(MenuItem item) {
//        Intent i = Q Intent(FeedActivity.this, ProfileActivity.class);
//        startActivity(i);
//    }

    public void onComposeClick(View view) {
        Intent i = new Intent(this, ComposeActivity.class);
        i.putExtra("user", Parcels.wrap(user));
        startActivity(i);
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



    public String getUid() {
        return uid;
    }
}
