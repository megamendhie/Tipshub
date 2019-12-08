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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView txtDisplay1 = findViewById(R.id.txtDipsplay1);
        TextView txtDisplay2 = findViewById(R.id.txtDipsplay2);
        RecyclerView listSubscribers = findViewById(R.id.listSubscribers);
        RecyclerView listSubscriptions = findViewById(R.id.listSubscriptions);

        listSubscribers.setLayoutManager(new LinearLayoutManager(this));
        listSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        Query querySubscribers = database.collection("subscriptions").orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("subToId", userId);
        Query querySubscriptions = database.collection("subscriptions").orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("subFromId", userId);

        SubscriberAdapter subscriberAdapter = new SubscriberAdapter(querySubscribers, getApplicationContext());
        SubscriptionAdapter subscriptionAdapter = new SubscriptionAdapter(querySubscriptions, getApplicationContext());
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
