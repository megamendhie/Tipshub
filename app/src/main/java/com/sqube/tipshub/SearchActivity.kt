package com.sqube.tipshub

import adapters.PeopleAdapter
import android.app.SearchManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.algolia.search.saas.*
import org.json.JSONException
import utils.Calculations
import utils.FirebaseUtil.firebaseAuthentication
import java.util.*

class SearchActivity : AppCompatActivity() {
    private var searchList: RecyclerView? = null
    private var prgSearch: ProgressBar? = null
    private var txtPrompt: TextView? = null
    private var onQueryTextListener: SearchView.OnQueryTextListener? = null
    private val listOfUsers = ArrayList<String>()
    private var userId: String? = null
    private var index: Index? = null
    private var adapter: PeopleAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        searchList = findViewById(R.id.searchList)
        prgSearch = findViewById(R.id.prgSearch)
        txtPrompt = findViewById(R.id.txtPrompt)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        val user = firebaseAuthentication!!.currentUser
        userId = user!!.uid
        adapter = PeopleAdapter(userId, listOfUsers)
        searchList.setLayoutManager(LinearLayoutManager(this))
        searchList.setAdapter(adapter)
        val client = Client(Calculations.applicationID, Calculations.apiKey)
        index = client.getIndex("dev_USERS")
        setOnQuery()
    }

    private fun setOnQuery() {
        onQueryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchTerm: String): Boolean {
                listOfUsers.clear()
                adapter!!.notifyDataSetChanged()
                prgSearch!!.visibility = View.VISIBLE
                txtPrompt!!.visibility = View.GONE
                val query = Query(searchTerm)
                        .setAttributesToRetrieve("objectID")
                        .setHitsPerPage(50)
                index!!.searchAsync(query, CompletionHandler { jsonObject, e ->
                    if (jsonObject == null) return@CompletionHandler
                    try {
                        listOfUsers.clear()
                        val usersArray = jsonObject.getJSONArray("hits")
                        for (i in 0 until usersArray.length()) {
                            val userObject = usersArray.getJSONObject(i)
                            val userId = userObject.optString("objectID")
                            listOfUsers.add(userId)
                        }
                        prgSearch!!.visibility = View.GONE
                        if (listOfUsers.isEmpty()) txtPrompt!!.visibility = View.VISIBLE else searchList!!.adapter = PeopleAdapter(userId, listOfUsers)
                    } catch (e1: JSONException) {
                        prgSearch!!.visibility = View.GONE
                        e1.printStackTrace()
                        Toast.makeText(this@SearchActivity, e1.message, Toast.LENGTH_SHORT).show()
                    }
                }
                )
                searchList!!.adapter = PeopleAdapter(userId, listOfUsers)
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!listOfUsers.isEmpty()) searchList!!.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = searchItem.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.isIconified = false
        searchView.requestFocus()
        searchView.setOnQueryTextListener(onQueryTextListener)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}