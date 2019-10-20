package com.sqube.tipshub;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import adapters.SubscriberAdapter;
import adapters.SubscriptionAdapter;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccActivity";
    private RecyclerView listSubscribers, listSubscriptions;
    private ActionBar actionBar;
    private FirebaseFirestore database;
    private FirebaseUser user;
    private TextView txtDisplay1, txtDisplay2;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        txtDisplay1 = findViewById(R.id.txtDipsplay1);
        txtDisplay2 = findViewById(R.id.txtDipsplay2);
        listSubscribers = findViewById(R.id.listSubscribers);
        listSubscribers.setLayoutManager(new LinearLayoutManager(this));
        listSubscriptions = findViewById(R.id.listSubscriptions);
        listSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        database = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        Query querySubscribers = database.collection("subscriptions").orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("subToId", userId);
        Query querySubscriptions = database.collection("subscriptions").orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("subFromId", userId);

        SubscriberAdapter subscriberAdapter = new SubscriberAdapter(querySubscribers, userId, getApplicationContext());
        SubscriptionAdapter subscriptionAdapter = new SubscriptionAdapter(querySubscriptions, userId, getApplicationContext());
        listSubscribers.setAdapter(subscriberAdapter);
        listSubscriptions.setAdapter(subscriptionAdapter);
        if(subscriberAdapter!=null){
            Log.i(TAG, "subscriberAdapter: started listening");
            subscriberAdapter.startListening();
        }
        if(subscriptionAdapter!=null){
            Log.i(TAG, "subscriptionAdapter: started listening");
            subscriptionAdapter.startListening();
        }

        if(listSubscribers.getChildCount()==0)
            txtDisplay1.setVisibility(View.VISIBLE);
        else
            txtDisplay1.setVisibility(View.GONE);

        if(listSubscriptions.getChildCount()==0)
            txtDisplay2.setVisibility(View.VISIBLE);
        else
            txtDisplay2.setVisibility(View.GONE);


    }
}
