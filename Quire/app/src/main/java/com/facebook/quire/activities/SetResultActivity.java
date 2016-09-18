package com.facebook.quire.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.quire.Constants;
import com.facebook.quire.R;
import com.facebook.quire.models.Choice;
import com.facebook.quire.models.Quire;
import com.facebook.quire.views.ChoiceView;
import com.firebase.client.Firebase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

import java.text.MessageFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SetResultActivity extends AppCompatActivity {

    @BindView(R.id.tvQuestion) TextView tvQuestion;
    @BindView(R.id.tvDescription) TextView tvDescription;
//    @BindView(R.id.llResultBar) LinearLayout llResultBar; // add choices to this
    @BindView(R.id.ivNext) ImageView ivNext;
    @BindView(R.id.ivBackward) ImageView ivBack;
    @BindView(R.id.llResults) LinearLayout llResults;
//    @BindView(R.id.llChoices) LinearLayout llChoices;

    Quire quire;
    Choice choice;
    String cid;
    boolean somethingIsClicked;

    Firebase base = new Firebase(Constants.FIREBASE_URL);
    DatabaseReference mDatabase;
    ArrayList<Drawable> array = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_result);
        ButterKnife.bind(this);

        // Initialize calligraphy library for custom font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Dosis-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        array.add(getApplicationContext().getResources().getDrawable(R.drawable.progressbar_green));
        array.add(getApplicationContext().getResources().getDrawable(R.drawable.progressbar));
        array.add(getApplicationContext().getResources().getDrawable(R.drawable.progressbar_red));
        array.add(getApplicationContext().getResources().getDrawable(R.drawable.progressbar_yellow));

        mDatabase = FirebaseDatabase.getInstance().getReference();

        somethingIsClicked = false;
        // pass in a quire through intent (detail activity?)\
        quire = Parcels.unwrap(getIntent().getParcelableExtra("quire"));
        final String qid = quire.getQid();

        // load quire into the layout
        String text = quire.getQuestionText();
        String description = quire.getDescriptionText();

        tvQuestion.setText(text);
        tvDescription.setText(description);

        final ArrayList<ChoiceView> cvList = new ArrayList<>();
        ArrayList<Choice> choicesList = quire.getChoices();
        int count = choicesList.size();
        for (int i = 0 ; i < count; i ++){
            final int j = i;
            Choice choice = choicesList.get(i);
            final ChoiceView choiceView = new ChoiceView(this);
            choiceView.setChoice(choice);
            choiceView.setChoiceText(choice.getText());
            //choiceView.setVotes((int)choice.getVotes());
            choiceView.displayProgress(true);
            choiceView.setPollEnabled(false);
            choiceView.setBackgroundColor(false);

            //Setting colors for percentage bars

            // Getting percentage
            mDatabase.child("choices").child(choice.getCid()).child("votes").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long votes = (long) dataSnapshot.getValue();
                    choiceView.setMax((int) quire.getTotalVotes());
                    choiceView.setVotes((int) votes);

                    if (quire.getTotalVotes() == 0) {
                        choiceView.setPercentage(MessageFormat.format("{0, number, #.##%}", 0));
                    } else {
                        String percentage = MessageFormat.format("{0, number, #.##%}", (double) votes/ (double) quire.getTotalVotes());
                        choiceView.setPercentage(percentage);
                        Drawable d = array.get(j % 4);
                        choiceView.setProgressDrawable(d);
                    }

//                    if (votes == 0) {
//                        choiceView.displayProgress(false);
//                    } else {
//                        choiceView.displayProgress(true);
//                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            cvList.add(choiceView);
            llResults.addView(choiceView);
        }

        for (final ChoiceView cv : cvList) {
            cv.setBackgroundColor(getResources().getColor(R.color.grey));
            cv.setChosen(false);
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!cv.getChosen()) {
                        for (final ChoiceView otherCV : cvList) {
                            otherCV.setChosen(false);
                            otherCV.setBackgroundColor(false);
                            otherCV.setOptionChecked(false);
                        }
                        //if no votes and this choice is selected, then should display blank
//                        if (cv.getChoice().getVotes() == 0) {
////                            Drawable d = getResources().getDrawable(R.drawable.progressbar_empty_setresult);
////                            cv.setProgressDrawable(d);
//
//                        }

                        cv.setChosen(true);
                        somethingIsClicked = true;
                        Choice clickedChoice = cv.getChoice();
                        cid = clickedChoice.getCid();
                        quire.setOutcome(cid);
                        cv.setOptionChecked(true);
                        cv.setBackgroundColor(true);
                    } else {
                        cv.setChosen(false);
                        somethingIsClicked = false;
                        cv.setOptionChecked(false);
                        cv.setBackgroundColor(false);
//                        if (cv.getChoice().getVotes() == 0) {
//                            Drawable d = getResources().getDrawable(R.drawable.progressbar);
//                            cv.setProgressDrawable(d);
//                        }

                    }
                    //somethingIsClicked = true;
                }
            });
        }


        // on click next, pass intent to go to next screen

        // Checks with user if they meant to go back to the FeedActivity
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (somethingIsClicked) {
                    new AlertDialog.Builder(SetResultActivity.this)
                            .setTitle("Discard result")
                            .setMessage("Are you sure you want to discard your result?")
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

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!somethingIsClicked) {
                    new AlertDialog.Builder(SetResultActivity.this)
                            .setTitle("Error!")
                            .setMessage("Please set a result")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with result
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    // store the final result into quire>(qid)>outcome=value
                    base.child("quires").child(qid).child("outcome").setValue(cid);
                    startNewActivity(quire); // goes to second page
                }
            }
        });
    }

    protected void startNewActivity(Quire quire){
        Intent i = new Intent(getApplicationContext(), SetResultActivity2.class);
        i.putExtra("quire", Parcels.wrap(quire));
        startActivity(i);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


}
