package com.facebook.quire.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.facebook.quire.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;

public class FullscreenPictureActivity extends AppCompatActivity {

    @BindView(R.id.ivPicture)
    ImageView ivPicture;
    @BindView(R.id.ivClose)
    ImageView ivClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_picture);

        Intent i = getIntent();
        String url = i.getStringExtra("url");

        ivPicture.setTag(0, url);

        Picasso.with(getApplicationContext())
                .load(url)
                .into(ivPicture);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
