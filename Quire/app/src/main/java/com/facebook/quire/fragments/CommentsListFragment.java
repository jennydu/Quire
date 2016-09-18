package com.facebook.quire.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.quire.CommentsAdapter;
import com.facebook.quire.R;
import com.facebook.quire.models.Comment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by claudiawu on 7/20/16.
 */
public class CommentsListFragment extends Fragment {
    private DatabaseReference mDatabase;
    CommentsAdapter cAdapter;
    RecyclerView rvComments;
    SwipeRefreshLayout swipeContainer;
    DataSnapshot snapshot;
    String qid;
    LinearLayoutManager mManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        qid = getArguments().getString("qid");

        // Load only comments whose quire ID matches current quire
        mDatabase.child("quires").child(qid).child("comments").orderByChild("invertedTimestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                snapshot = dataSnapshot;
                cAdapter = new CommentsAdapter(getContext());
                Comment.fromIdDataSnapshot(dataSnapshot, cAdapter);
                rvComments.setAdapter(cAdapter);
                mManager.setStackFromEnd(true);
                //mManager.scrollToPosition(cAdapter.getItemCount());
                //aComments.notifyDataSetChanged();
                //lvComments.smoothScrollToPosition(0);


//                if (snapshot != dataSnapshot) {
//                    snapshot = dataSnapshot;
//                    Comment.fromIdDataSnapshot(dataSnapshot, cAdapter);
//                    //aComments.addAll(comments);
//                    swipeContainer.setRefreshing(false);
//                    rvComments.setAdapter(cAdapter);
//                    //aComments.notifyDataSetChanged();
//                } else {
//                    swipeContainer.setRefreshing(false);
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_comments_list,parent,false);

        rvComments = (RecyclerView) v.findViewById(R.id.rvComments);
        mManager = new LinearLayoutManager(this.getContext());
//        mManager.setReverseLayout(true);
        //mManager.scrollToPosition(cAdapter.getItemCount());
        rvComments.setLayoutManager(mManager);

        // Swipe to Refresh
        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                refreshComments();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        return v;
    }

    public static CommentsListFragment newInstance(String qid) {
        CommentsListFragment commentsListFragment =  new CommentsListFragment();
        Bundle args = new Bundle();
        args.putString("qid",qid);
        commentsListFragment.setArguments(args);
        return commentsListFragment;
    }

    private void refreshComments() {
        // Look under quires/comments for CommentID's then load the comments with those ID's

        mDatabase.child("quires").child(qid).child("comments").orderByChild("invertedTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (snapshot != dataSnapshot) {
                    Comment.fromIdDataSnapshot(dataSnapshot, cAdapter);
                    //aComments.addAll(comments);
                    swipeContainer.setRefreshing(false);
                    rvComments.setAdapter(cAdapter);
                    //aComments.notifyDataSetChanged();
                } else {
                    swipeContainer.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
