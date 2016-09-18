package com.facebook.quire.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.quire.QuiresAdapter;
import com.facebook.quire.R;
import com.facebook.quire.models.Quire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TwoPartQuiresListFragment extends Fragment{
    private ArrayList<Quire> quires;
    private RecyclerView rvQuires;
    protected TextView tvHeader;
    DatabaseReference mDatabase;
    String mUserId;
    QuiresAdapter qAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_quires_list, container, false);

        rvQuires = (RecyclerView) v.findViewById(R.id.rvQuires);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //check Authentication
        try {
            mUserId = mAuth.getCurrentUser().getUid(); // gets the user id
        } catch (Exception e) {
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();
        //quires = Quire.fromJSONArray(20);
        //quires = Quire.fromJSONArray(array);
//        QuiresAdapter adapter = new QuiresAdapter(getContext(), quires);
//        rvQuires.setAdapter(adapter);
//        rvQuires.setLayoutManager(new LinearLayoutManager(getContext()));

//        tvHeader = (TextView) v.findViewById(R.id.tvHeader);

        //rvQuires.setAdapter(aQuires);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        quires = new ArrayList<>();
//        qAdapter = new QuiresAdapter(getActivity(),quires);
    }

    public void addAll(List<Quire> quires) {
        //Add things to the adapter
        //qAdapter.addAll(quires);
        qAdapter.notifyDataSetChanged();

    }
}
