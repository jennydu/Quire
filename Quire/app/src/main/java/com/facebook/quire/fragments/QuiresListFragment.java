package com.facebook.quire.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.quire.QuiresAdapter;
import com.facebook.quire.R;
import com.facebook.quire.models.Quire;

import java.util.ArrayList;


public class QuiresListFragment extends Fragment {

    private ArrayList<Quire> quires;
    private RecyclerView rvQuires;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_quires_list, container, false);

        rvQuires = (RecyclerView) v.findViewById(R.id.rvQuires);
        //quires = Quire.fromJSONArray(20);
        //quires = Quire.fromJSONArray(array);
        quires = new ArrayList<>();
        QuiresAdapter adapter = new QuiresAdapter(getContext());
        rvQuires.setAdapter(adapter);
        rvQuires.setLayoutManager(new LinearLayoutManager(getContext()));


        //rvQuires.setAdapter(aQuires);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quires = new ArrayList<>();
    }


}
