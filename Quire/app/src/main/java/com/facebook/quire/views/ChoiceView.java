package com.facebook.quire.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.quire.R;
import com.facebook.quire.models.Choice;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by amao on 7/29/16.
 */
public class ChoiceView extends RelativeLayout {

    View rootView;
    DatabaseReference mDatabase;
    @BindView(R.id.llChoice) LinearLayout llChoice;
    @BindView(R.id.rlBar) LinearLayout rlBar;
    @BindView(R.id.rbOption) AppCompatRadioButton rbOption;
    @BindView(R.id.pbBar) ProgressBar pbBar;
    @BindView(R.id.tvPercentage) TextView tvPercentage;

    public ChoiceView(Context context) {
        super(context);
        init(context);
    }

    public ChoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //do setup work here
        rootView = inflate(context, R.layout.choice_view, this);
        ButterKnife.bind(this);

        pbBar.setProgressDrawable(getResources().getDrawable(R.drawable.progressbar));
    }

    //set choice text
    public void setChoiceText(String text) {
        rbOption.setText(text);
    }

    //set radio button checked
    public void setOptionChecked(boolean b) {
        rbOption.setChecked(b);
    }

    //set radio button id
    public void setButtonId(int i) {
        rbOption.setId(i);
    }

    //disable the poll
    public void setPollEnabled(boolean b) {
        rbOption.setEnabled(b);
    }

    //set progress bar max
    public void setMax(int i) {
        pbBar.setMax(i);
    }

    //set progress bar votes
    public void setVotes(int i) {
        pbBar.setProgress(i);
    }

    //set progress bar drawable
    public void setProgressDrawable(Drawable d) {
        Rect bounds = pbBar.getProgressDrawable().getBounds();
        pbBar.setProgressDrawable(d);
        pbBar.getProgressDrawable().setBounds(bounds);
    }

    //set choice percentage
    public void setPercentage(String s) {
        tvPercentage.setText(s);
    }

    //set progress bar visibility
    public void displayProgress(boolean b) {
        if (b) {
            pbBar.setVisibility(View.VISIBLE);
        }
        else {
            pbBar.setVisibility(View.GONE);
        }
    }

    public void setBackgroundColor(int i) {
        llChoice.setBackgroundColor(i);
    }

    public void setButtonColor(ColorStateList list) {
        rbOption.setSupportButtonTintList(list);
    }

    //TODO: how to ensure that all choices are in same radiogroup?

    public RadioButton getButton() {
        return rbOption;
    }

    // This part is just for the ResultActivity

    boolean isChosen;
    //Choice result;
    Choice choice;

    public void setChosen(boolean isChosen) {
        this.isChosen = isChosen;
    }

    public boolean getChosen() {
        return this.isChosen;
    }

//    public void setResult(Choice result) {
//        this.result = result;
//    }
//
//    public Choice getResult() {
//        return this.result;
//    }


    public void setBackgroundColor(boolean wantBackground) {
        // Fill the choice view with colorPrimary when it is selected
        if (wantBackground) {
            llChoice.setPadding(0,0,0,50);
            llChoice.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        } else {
            llChoice.setPadding(0,0,0,50);
            llChoice.setBackgroundColor(getResources().getColor(R.color.radiogroupBackground));
        }
    }

    public void setChoice(Choice choice) {
        this.choice = choice;
    }

    public Choice getChoice() {
        return this.choice;
    }

}
