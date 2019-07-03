package services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

import javax.annotation.Nullable;

import models.UserNetwork;

public class UserDataFetcher extends IntentService {
    FirebaseFirestore database;
    FirebaseAuth auth;
    String userID;
    private String TAG = "UserDataFetcher";
    public UserDataFetcher() {
        super("UserDataFetcher");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

    @Override
    protected void onHandleIntent(@android.support.annotation.Nullable Intent intent) {

        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        Log.i(TAG, "onCreate: ");
        if(auth.getCurrentUser()==null){
            onDestroy();
            return;
        }
        userID = auth.getCurrentUser().getUid();

        //Set user profile
        database.collection("profiles").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onEvent: profile");
                UserNetwork.setProfile(documentSnapshot);
            }
        });

        //set user followers
        database.collection("followers").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onEvent: followers");
                if(documentSnapshot==null)
                    return;
                if(documentSnapshot.exists() && documentSnapshot.contains("list")){
                    UserNetwork.setFollowers((ArrayList<String>) documentSnapshot.get("list"));
                }
            }
        });

        //set user followings
        database.collection("followings").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot==null)
                    return;
                if(documentSnapshot.exists() && documentSnapshot.contains("list")){
                    UserNetwork.setFollowing((ArrayList<String>) documentSnapshot.get("list"));
                }
            }
        });

        //set user subscribers
        database.collection("subscribers").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onEvent: subscribers");
                if(documentSnapshot==null)
                    return;
                if(documentSnapshot.exists() && documentSnapshot.contains("list")){
                    UserNetwork.setSubscribers((ArrayList<String>) documentSnapshot.get("list"));
                }
            }
        });

        //set user subsrciptions
        database.collection("subscribed_to").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onEvent: subscribed_to");
                if(documentSnapshot==null)
                    return;
                if(documentSnapshot.exists() && documentSnapshot.contains("list")){
                    UserNetwork.setSubscribed((ArrayList<String>) documentSnapshot.get("list"));
                }
            }
        });
    }
}