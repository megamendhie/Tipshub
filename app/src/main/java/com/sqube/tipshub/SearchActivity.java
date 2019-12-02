package com.sqube.tipshub;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import adapters.PeopleAdapter;
import utils.FirebaseUtil;

public class SearchActivity extends AppCompatActivity {
    private static final String TAG = "SearchActivity";
    private RecyclerView searchList;
    private ProgressBar prgSearch;
    private TextView txtPrompt;
    private SearchView.OnQueryTextListener onQueryTextListener;
    private ArrayList<String> listOfUsers = new ArrayList<>();
    private String userId;
    private Index index;
    private PeopleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        searchList = findViewById(R.id.searchList);
        prgSearch = findViewById(R.id.prgSearch);
        txtPrompt = findViewById(R.id.txtPrompt);

        final FirebaseUser user = FirebaseUtil.getFirebaseAuthentication().getCurrentUser();
        userId = user.getUid();

        adapter = new PeopleAdapter(SearchActivity.this, getApplicationContext(), userId, listOfUsers);
        searchList.setLayoutManager(new LinearLayoutManager(this));
        searchList.setAdapter(adapter);

        Client client = new Client("P7943LORA3", "2ec0eeece3780ab740c8fb87f75a4d84");
        index = client.getIndex("dev_USERS");
        setOnQuery();
    }

    private void setOnQuery() {
        onQueryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchTerm) {
                listOfUsers.clear();
                adapter.notifyDataSetChanged();
                prgSearch.setVisibility(View.VISIBLE);
                txtPrompt.setVisibility(View.GONE);
                Query query = new Query(searchTerm)
                        .setAttributesToRetrieve("objectID")
                        .setHitsPerPage(50);
                index.searchAsync(query, new CompletionHandler() {
                            @Override
                            public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                                if (jsonObject==null)
                                    return;
                                try {
                                    listOfUsers.clear();
                                    JSONArray usersArray = jsonObject.getJSONArray("hits");
                                    for(int i = 0; i < usersArray.length(); i++){
                                        JSONObject userObject = usersArray.getJSONObject(i);
                                        String userId = userObject.optString("objectID");
                                        listOfUsers.add(userId);
                                    }

                                    prgSearch.setVisibility(View.GONE);
                                    if(listOfUsers.isEmpty())
                                        txtPrompt.setVisibility(View.VISIBLE);
                                    else
                                        searchList.setAdapter(new PeopleAdapter(SearchActivity.this, getApplicationContext(), userId, listOfUsers));

                                } catch (JSONException e1) {
                                    prgSearch.setVisibility(View.GONE);
                                    e1.printStackTrace();
                                    Toast.makeText(SearchActivity.this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
                searchList.setAdapter(new PeopleAdapter(SearchActivity.this,
                        getApplicationContext(),userId, listOfUsers));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getSystemService(SearchActivity.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.requestFocus();
        searchView.setOnQueryTextListener(onQueryTextListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
