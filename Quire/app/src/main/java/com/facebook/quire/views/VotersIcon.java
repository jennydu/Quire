package com.facebook.quire.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.quire.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by amao on 7/29/16.
 */
public class VotersIcon extends RelativeLayout {

    View rootView;
    ImageView voters;
    TextView numberVoters;
    DatabaseReference mDatabase;
    String quireID;

    public VotersIcon(Context context) {
        super(context);
        init(context);
    }

    public VotersIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //do setup work here
        rootView = inflate(context, R.layout.voters_icon, this);
        numberVoters = (TextView) rootView.findViewById(R.id.tvNumVoters);

        voters = (ImageView) rootView.findViewById(R.id.ivVoters);

    }

    public int getValue() {
        return Integer.valueOf(numberVoters.getText().toString());
    }

    public void setValue(String text) {
        numberVoters.setText(text);
    }

    public void setQuireID(String quireID) {
        this.quireID = quireID;
    }

    public void setDrawable(Drawable d) {
        voters.setImageDrawable(d);
    }

}
