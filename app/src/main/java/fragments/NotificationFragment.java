package fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.sqube.tipshub.R;

import adapters.TestAdapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {
    ListView testList;


    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_notification, container, false);
        testList = rootView.findViewById(R.id.testList);
        String[] testString = {"A", "B", "C", "D", "E", "F", "G", "H"};
        ArrayAdapter<String> adapter = new TestAdapter(getContext(), testString);
        testList.setAdapter(adapter);

        return rootView;
    }

}
