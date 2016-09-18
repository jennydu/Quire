package com.facebook.quire.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.quire.R;
import com.facebook.quire.VotersAdapter;
import com.facebook.quire.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by claudiawu on 7/27/16.
 */
public class VotersListFragment extends Fragment {
    DatabaseReference mDatabase;
    VotersAdapter vAdapter;
    RecyclerView rvVoters;
    LinearLayoutManager mManager;
    ArrayList<User> voters;
    String qid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        qid = getArguments().getString("qid");
        voters = new ArrayList<>();
        vAdapter = new VotersAdapter(getContext());

        mDatabase.child("quires").child(qid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                //get the users and listen for them, and then add to the arraylist of users
                for (DataSnapshot s : snapshot.getChildren()) {
                    String uid = s.getKey();
                    Log.d("user", uid);

                    //gets each user object and adds it to the arraylist
                    mDatabase.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //get the user object, add to arraylist

                            User user = dataSnapshot.getValue(User.class);
                            Log.d("user obj", user.toString());

                            voters.add(user);
//                            if (voters.size() == snapshot.getChildrenCount()) {
//                                Log.d("size is equal", voters.size() + "");
                                vAdapter.setVoters(voters);
//                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    rvVoters.setAdapter(vAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_voters_list,parent,false);

        rvVoters = (RecyclerView) v.findViewById(R.id.rvVoters);
        mManager = new LinearLayoutManager(this.getContext());
        rvVoters.setLayoutManager(mManager);

        return v;
    }

    public static VotersListFragment newInstance(String qid) {
        VotersListFragment votersListFragment = new VotersListFragment();
        Bundle args = new Bundle();
        args.putString("qid",qid);
        votersListFragment.setArguments(args);
        return votersListFragment;
    }
}
