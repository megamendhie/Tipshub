package com.sqube.tipshub.activities

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.sqube.tipshub.databinding.ActivityLeaguesBinding

class LeaguesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLeaguesBinding
    private val customTabBuilder = CustomTabsIntent.Builder()
    private lateinit var customTab: CustomTabsIntent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaguesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Stats, Results & Fixtures"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Build CustomTabsIntent
        val colorScheme = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(Color.parseColor("#1E73F4"))
            .build()
        customTabBuilder.setDefaultColorSchemeParams(colorScheme)
        customTab = customTabBuilder.build()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    private fun openSite(url: String){
        customTab.launchUrl(this, Uri.parse(url))
    }

    fun leagueSelected(view: View){
        when(view){
            binding.crdCham -> openSite("https://www.uefa.com/uefachampionsleague/fixtures-results/")
            binding.crdEuropa -> openSite("https://www.uefa.com/uefaeuropaleague/fixtures-results/")
            binding.crdEpl -> openSite("https://www.livescore.com/en/football/england/premier-league/")
            binding.crdLaliga -> openSite("https://www.livescore.com/en/football/spain/laliga-santander/")
            binding.crdSeriea -> openSite("https://www.livescore.com/en/football/italy/serie-a/")
            binding.crdBundesliga -> openSite("https://www.livescore.com/en/football/germany/bundesliga/")
            binding.crdLeagueone -> openSite("https://www.livescore.com/en/football/france/ligue-1/")
            binding.crdErendevisie -> openSite("https://www.livescore.com/en/football/holland/eredivisie/")
            binding.crdFacup -> openSite("https://www.livescore.com/en/football/england/fa-cup/")
            binding.crdCarabao -> openSite("https://www.livescore.com/en/football/england/efl-cup/")

        }
    }
}