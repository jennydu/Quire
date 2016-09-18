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
public class CommentsIcon extends RelativeLayout {

    View rootView;
    ImageView comments;
    TextView numberComments;
    DatabaseReference mDatabase;
    String quireID;

    public CommentsIcon(Context context) {
        super(context);
        init(context);
    }

    public CommentsIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //do setup work here
        rootView = inflate(context, R.layout.comments_icon, this);
        numberComments = (TextView) rootView.findViewById(R.id.tvNumComments);

        comments = (ImageView) rootView.findViewById(R.id.ivComments);

    }

    public int getValue() {
        return Integer.valueOf(numberComments.getText().toString());
    }

    public void setValue(String text) {
        numberComments.setText(text);
    }

    public void setQuireID(String quireID) {
        this.quireID = quireID;
    }

    public void setDrawable(Drawable d) {
        comments.setImageDrawable(d);
    }

}
