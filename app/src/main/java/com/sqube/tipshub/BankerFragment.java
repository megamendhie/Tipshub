package com.sqube.tipshub;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import adapters.TestAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class BankerFragment extends Fragment {
    FloatingActionButton fabPost;
    ListView testList;


    public BankerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_banker, container, false);
        testList = rootView.findViewById(R.id.testList);
        fabPost = rootView.findViewById(R.id.fabPost);
        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "You cannot post banker games yet", Toast.LENGTH_LONG).show();
            }
        });

        String[] testString = {"A", "B", "C", "D", "E", "F", "G", "H"};
        ArrayAdapter<String> adapter = new TestAdapter(getContext(), testString);
        testList.setAdapter(adapter);

        return rootView;
    }

}
