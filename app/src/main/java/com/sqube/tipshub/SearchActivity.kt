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
import com.sqube.tipshub.databinding.ActivitySearchBinding
import org.json.JSONException
import utils.Calculations
import utils.FirebaseUtil.firebaseAuthentication
import utils.apiKey
import utils.applicationID
import java.util.*

class SearchActivity : AppCompatActivity() {
    private var _binding: ActivitySearchBinding? = null
    private val binding get() = _binding!!
    private var onQueryTextListener: SearchView.OnQueryTextListener? = null
    private val listOfUsers = ArrayList<String>()
    private lateinit var userId: String
    private var index: Index? = null
    private var adapter: PeopleAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        val user = firebaseAuthentication!!.currentUser
        userId = user!!.uid
        adapter = PeopleAdapter(userId, listOfUsers)
        binding.searchList.layoutManager = LinearLayoutManager(this)
        binding.searchList.adapter = adapter
        val client = Client(applicationID, apiKey)
        index = client.getIndex("dev_USERS")
        setOnQuery()
    }

    private fun setOnQuery() {
        onQueryTextListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchTerm: String): Boolean {
                listOfUsers.clear()
                adapter!!.notifyDataSetChanged()
                binding.prgSearch.visibility = View.VISIBLE
                binding.txtPrompt.visibility = View.GONE
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
                        binding.prgSearch.visibility = View.GONE
                        if (listOfUsers.isEmpty()) binding.txtPrompt.visibility = View.VISIBLE else binding.searchList.adapter = PeopleAdapter(userId, listOfUsers)
                    } catch (e1: JSONException) {
                        binding.prgSearch.visibility = View.GONE
                        e1.printStackTrace()
                        Toast.makeText(this@SearchActivity, e1.message, Toast.LENGTH_SHORT).show()
                    }
                }
                )
                binding.searchList.adapter = PeopleAdapter(userId, listOfUsers)
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (listOfUsers.isNotEmpty()) binding.searchList.requestFocus()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
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