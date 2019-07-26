package services;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.util.ArrayList;

import javax.annotation.Nullable;

import models.ProfileMedium;
import models.UserNetwork;

public class UserDataFetcher extends IntentService {
    FirebaseFirestore database;
    FirebaseAuth auth;
    String userID;
    private SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private String TAG = "UserDataFetcher";
    private FirebaseMessaging FCM;

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
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();
        FCM = FirebaseMessaging.getInstance();
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
                if(documentSnapshot==null || !documentSnapshot.exists())
                    return;

                FCM.subscribeToTopic(userID); //subscribe user to corresponding channel with userId

                //set user profile to SharePreference
                Gson gson = new Gson();
                String json = gson.toJson(documentSnapshot.toObject(ProfileMedium.class));
                editor.putString("profile", json);
                editor.apply();
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

                //subscribe for notification from people you have subscribed to
                if(((ArrayList<String>) documentSnapshot.get("list"))==null || (((ArrayList<String>) documentSnapshot.get("list")).isEmpty()))
                    return;
                for(String s: ((ArrayList<String>) documentSnapshot.get("list"))){
                    FCM.subscribeToTopic("sub_"+s);
                }

            }
        });
    }
}