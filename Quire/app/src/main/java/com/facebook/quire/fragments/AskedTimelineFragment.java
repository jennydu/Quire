package com.facebook.quire.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.quire.ItemClickSupport;
import com.facebook.quire.QuiresAdapter;
import com.facebook.quire.R;
import com.facebook.quire.activities.ResultActivity;
import com.facebook.quire.models.Quire;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcels;

public class AskedTimelineFragment extends Fragment {
    QuiresAdapter qAdapter;
    RecyclerView rvQuires;
    String mUserId;
    DatabaseReference mDatabase;
    DataSnapshot snapshot;
    LinearLayoutManager mManager;
    SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //tvHeader.setText("My active Quires");
        mUserId = getActivity().getIntent().getStringExtra("uid");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        populateAskedQuires();
    }

    public static AskedTimelineFragment newInstance(String uid) {
        AskedTimelineFragment askedTimelineFragment = new AskedTimelineFragment();
        Bundle args = new Bundle();
        args.putString("uid",uid);
        askedTimelineFragment.setArguments(args);
        return askedTimelineFragment;
    }

    public void populateAskedQuires() {
        mDatabase.child("users").child(mUserId).child("quires").orderByChild("timestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                snapshot = dataSnapshot;
                if (qAdapter != null) {
                    qAdapter.clear();
                }
                qAdapter = new QuiresAdapter(getContext());
                Quire.fromIdDataSnapshot(dataSnapshot, qAdapter);
                rvQuires.setAdapter(qAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_quires_list, parent,false);

        rvQuires = (RecyclerView) v.findViewById(R.id.rvQuires);
        mManager = new LinearLayoutManager(this.getContext());
        //amManager.setReverseLayout(true);
        rvQuires.setLayoutManager(mManager);

        // Sets up onClick listeners to take user to Detail, Result, or SetResult Activities
        ItemClickSupport.addTo(rvQuires).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        // do it
                        //Toast.makeText(getContext(),"Clicked a quire", Toast.LENGTH_LONG).show();

                        //get the article to display
                        Quire quire = qAdapter.getQuireAt(position);

                        // see status of the quire
                        String status = quire.getStatus();
                        String authorId = quire.getUid();

                        if (status.equals("closed")) {
                            Intent i = new Intent(getContext(), ResultActivity.class);
                            i.putExtra("quire",Parcels.wrap(quire));
                            startActivity(i);
                        }
                    }
                }
        );

        // Swipe to Refresh
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                refreshMyQuires();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        return v;
    }

    public void refreshMyQuires() {
        mDatabase.child("users").child(mUserId).child("quires").orderByChild("invertedTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (snapshot != dataSnapshot) {
                    //qAdapter.clear();
                    Quire.fromIdDataSnapshot(dataSnapshot, qAdapter);
                    //qAdapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                    rvQuires.setAdapter(qAdapter);
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
