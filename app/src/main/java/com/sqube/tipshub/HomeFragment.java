package com.sqube.tipshub;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {
    FloatingActionButton fabPost;
    Button btnSignout;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_home, container, false);
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        btnSignout = rootView.findViewById(R.id.btnSignout); btnSignout.setOnClickListener(this);
        fabPost = rootView.findViewById(R.id.fabPost); fabPost.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSignout:
                if(auth.getCurrentUser()!=null)
                    signOut();
                else
                    Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_LONG).show();
                break;
            case R.id.fabPost:
                startActivity(new Intent(getActivity().getApplicationContext(), RepostActivity.class));
                break;
        }
    }

    public void signOut(){
        final List<String> providers = auth.getCurrentUser().getProviders();
        if(providers.get(0).equals("google.com")){
            auth.signOut();
            mGoogleSignInClient.signOut();
        }
        else{
            auth.signOut();
        }
    }
}
