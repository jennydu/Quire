package com.facebook.quire.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by amao on 7/7/16.
 */
public class FriendsTimelineFragment extends QuiresListFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //client = TwitterApplication.getRestClient(); //singleton client
        //need to use the same client, so just retrieve from this activity...
        populateTimeline();
    }

    public void populateTimeline() {
        //do our API requests here and add them to array
    }
}
