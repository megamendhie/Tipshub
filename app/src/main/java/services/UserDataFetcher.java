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
    protected void onHandleIntent(@androidx.annotation.Nullable Intent intent) {
        auth = FirebaseAuth.getInstance();
        Log.i(TAG, "onCreate: ");
        if(auth.getCurrentUser()==null)
            onDestroy();
        else {
            userID = auth.getCurrentUser().getUid();
            setUserData();
        }
    }

    private void setUserData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();
        database = FirebaseFirestore.getInstance();
        FCM = FirebaseMessaging.getInstance();

        database.collection("profiles").document(userID).addSnapshotListener((documentSnapshot, e) -> {
            Log.i(TAG, "onEvent: profile");
            if(documentSnapshot==null || !documentSnapshot.exists())
                return;

            FCM.subscribeToTopic(userID); //subscribe user to corresponding channel with userId

            //set user profile to SharePreference
            Gson gson = new Gson();
            Log.i(TAG, "onEvent: happended now");
            String json = gson.toJson(documentSnapshot.toObject(ProfileMedium.class));
            editor.putBoolean("isVerified", documentSnapshot.toObject(ProfileMedium.class).isC0_verified());
            editor.putString("profile", json);
            editor.apply();
        });

        //set user followers
        database.collection("followers").document(userID).addSnapshotListener((documentSnapshot, e) -> {
            Log.i(TAG, "onEvent: followers");
            if(documentSnapshot==null)
                return;
            if(!documentSnapshot.exists()){
                UserNetwork.setFollowers(new ArrayList<>());
                return;
            }
            if(documentSnapshot.exists() && documentSnapshot.contains("list")){
                UserNetwork.setFollowers((ArrayList<String>) documentSnapshot.get("list"));
            }
        });

        //set user followings
        database.collection("followings").document(userID).addSnapshotListener((documentSnapshot, e) -> {
            Log.i(TAG, "onEvent: followings");
            if(documentSnapshot==null)
                return;
            if(!documentSnapshot.exists()){
                UserNetwork.setFollowing(new ArrayList<>());
                return;
            }
            if(documentSnapshot.exists() && documentSnapshot.contains("list")){
                UserNetwork.setFollowing((ArrayList<String>) documentSnapshot.get("list"));
            }
        });

        //set user subscriptions
        database.collection("subscribed_to").document(userID).addSnapshotListener((documentSnapshot, e) -> {
            Log.i(TAG, "onEvent: subscribed_to ");
            if(documentSnapshot==null)
                return;
            if(!documentSnapshot.exists()){
                UserNetwork.setSubscribed(new ArrayList<>());
                return;
            }
            if(documentSnapshot.exists() && documentSnapshot.contains("list")){
                UserNetwork.setSubscribed((ArrayList<String>) documentSnapshot.get("list"));
            }

            //subscribe for notification from people you have subscribed to
            if(UserNetwork.getSubscribed()==null || UserNetwork.getSubscribed().isEmpty())
                return;
            for(String s: ((ArrayList<String>) documentSnapshot.get("list"))){
                FCM.subscribeToTopic("sub_"+s);
            }

        });
    }
}