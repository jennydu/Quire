package com.facebook.quire.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.quire.CircleTransform;
import com.facebook.quire.R;
import com.facebook.quire.fragments.ComposeChoicesFragment;
import com.facebook.quire.fragments.ComposeOptionsFragment;
import com.facebook.quire.fragments.ComposeQuestionFragment;
import com.facebook.quire.models.User;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ComposeActivity extends AppCompatActivity {

    @BindView(R.id.llHeader)
    LinearLayout llHeader;
    @BindView(R.id.ivProfileImage)
    ImageView ivProfileImage;
    @BindView(R.id.tvScreenName)
    TextView tvScreenName;
    @BindView(R.id.flContent)
    FrameLayout flContent;


    private String mUserId;
    User user;

    String data;
    String question;
    ArrayList<String> choices;

    // counts how you have gone from screen to screen
    int questionVisit;
    int choicesVisit;
    int optionsVisit;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_compose);
        ButterKnife.bind(this);

        questionVisit = 0;
        choicesVisit = 0;
        optionsVisit = 0;



// Set up custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

// load first fragment
        if (savedInstanceState == null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContent, new ComposeQuestionFragment());
            ft.commit();

        }

        user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
        mUserId = user.getUser_id();

        populateComposeHeader(user);


// profile on click
        ivProfileImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onProfileClick(view, mUserId);
            }
        });

        tvScreenName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onProfileClick(view, mUserId);
            }
        });

    } // end of onCreate



    private void populateComposeHeader(User user) {
        String screenname = user.getScreen_name();
        String url = user.getProfile_image_url();


        tvScreenName.setText(screenname);
        Picasso.with(this)
                .load(url)
                .resize(250,250)
                .transform(new CircleTransform())
                .into(ivProfileImage);
    }


    public void onProfileClick(View view, String uid) {
        Intent intent = new Intent (this, ProfileActivity.class);
        intent.putExtra("uid", uid);
        startActivity(intent);
    }

    // Return back to FeedActivity
    public void onXReturn(View view) {

        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected){
            //Toast.makeText(this, "Post created!", Toast.LENGTH_SHORT).show();
        }
        else{
            //Toast.makeText(this, "Your quire will be posted the next time you connect to the internet", Toast.LENGTH_SHORT).show();

        }
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    public void onForward(View view) {
        Fragment currentFrag = getSupportFragmentManager().findFragmentById(R.id.flContent);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (currentFrag instanceof ComposeQuestionFragment){ // TODO
            question = ((ComposeQuestionFragment) currentFrag).getQuestion();

            if (question.trim().length() > 0) {

                ArrayList<String> choicess = new ArrayList<>(); // empty arraylist for now
                ComposeChoicesFragment newFrag = ((ComposeQuestionFragment) currentFrag).goNext(choicess);

                ft.replace(R.id.flContent, newFrag);
                ft.commit();
            }
            else{
                Toast.makeText(ComposeActivity.this, "Please enter a non-empty question", Toast.LENGTH_SHORT).show();
            }

            // closing keyboard on click
            View v = this.getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        }

        else if (currentFrag instanceof ComposeChoicesFragment){ // TODO
            // question is question
            choices = ((ComposeChoicesFragment) currentFrag).getChoicesText();

            ArrayList<String> finalChoices = new ArrayList<>();

            for (String c : choices){
                if (c.trim().length() > 0) {
                    finalChoices.add(c);
                }
            }

            if (finalChoices.size() >= 2) {

                ComposeOptionsFragment newFrag = ((ComposeChoicesFragment) currentFrag).goNext(finalChoices);
                ft.replace(R.id.flContent, newFrag);
                ft.commit();
            }
            else {
                Toast.makeText(getApplicationContext(), "Please add more than 2 non-empty choices", Toast.LENGTH_SHORT).show();
            }

        }

        else if (currentFrag instanceof ComposeOptionsFragment){
            // gotta submit here
            // and also go to feed or new quire view
            ((ComposeOptionsFragment) currentFrag).submit(question, choices);
            //Toast.makeText(this, "submit part", Toast.LENGTH_SHORT).show();

            // go to feed since theres no more detail view
            Intent i = new Intent(this, FeedActivity.class);
            i.putExtra("uid", user.getUser_id());
            startActivity(i);

        }

    }


    public void onBack(View view) {
        // get current fragment
        final Fragment currentFrag = getSupportFragmentManager().findFragmentById(R.id.flContent);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (currentFrag instanceof ComposeQuestionFragment){
            // on back, go to feed
            new AlertDialog.Builder(ComposeActivity.this)
                    .setTitle("")
                    .setMessage("Are you sure you want to go back? You will lose your progress.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                            Intent i = new Intent(getApplicationContext(), FeedActivity.class);
                            i.putExtra("uid", user.getUser_id());
                            startActivity(i);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
        else if (currentFrag instanceof ComposeChoicesFragment){

            new AlertDialog.Builder(ComposeActivity.this)
                    .setTitle("")
                    .setMessage("Are you sure you want to go back? You will lose your progress.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            question = ((ComposeChoicesFragment)currentFrag).getQuestionText();

                            ComposeQuestionFragment newFrag = ((ComposeChoicesFragment) currentFrag).goBack();
                            EditText et = newFrag.getEt();
                            newFrag.populateQuestion(et, question);
                            ft.replace(R.id.flContent, newFrag);

                            ft.commit();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();



        }
        else if (currentFrag instanceof ComposeOptionsFragment){

            new AlertDialog.Builder(ComposeActivity.this)
                    .setTitle("")
                    .setMessage("Are you sure you want to go back? You will lose your progress.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ComposeChoicesFragment newFrag = ((ComposeOptionsFragment) currentFrag).goBack();
                            ft.replace(R.id.flContent, newFrag);
                            ft.commit();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
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


