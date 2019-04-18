package services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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
        database.collection("followers").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onEvent: followers");
                UserNetwork.setFollowers(documentSnapshot);
            }
        });
        database.collection("followings").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onEvent: followings");
                UserNetwork.setFollowing(documentSnapshot);
            }
        });
        database.collection("subscribers").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onEvent: subscribers");
                UserNetwork.setSubscibers(documentSnapshot);
            }
        });
        database.collection("subscribed_to").document(userID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.i(TAG, "onEvent: subscribed_to");
                UserNetwork.setSubscibed(documentSnapshot);
            }
        });
    }
}