package com.sqube.tipshub.utils

import org.json.JSONException
import org.json.JSONObject
import java.util.*

object AboutUtil {
    private val aboutJsonString: String
        get() = """{
  "about": [
    {
      "heading": "Welcome to Tipshub",
      "body": "Tipshub is a social com.sqube.tipshub.network for sports fans. It is an interactive platform where users discuss on trending sports events and good tipsters share their predictions with their followers and subscribers."
    },
    {
      "heading": "What can you do on Tipshub?",
      "body": "<ul><li>Get latest and accurate prediction from Tipshub</li><li>Discuss any sports event</li><li>Share your predictions with others</li><li>Follow people you like to see their tips.</li><li>Subscribe to good tipsters for their Banker tips.</li><li>Get the latest sports news.</li></ul>"
    },
    {
      "heading": "How does Tipshub predict games?",
      "body": "<p>We use high quality machine learning software to produce accurate predictions based on the teams' current stats, their h2h, players lineup, and winning capability.</p>"
    },
    {
      "heading": "What kind of post is allowed on Tipshub?",
      "body": "<p>Since Tipshub is dedicated for sports, we expect sports gist, analysis, and predictions.</p><p>&nbsp;</p><p>DO NOT post anything that is profane, pornographic, fraudulent, false, or insulting as you will be immediately suspended or blocked.</p>"
    },
    {
      "heading": "How may posts per day?",
      "body": "<p>You can post as many normal posts as you like.</p><p>But you can only <strong>predict</strong> only <strong>4 times</strong> in a day.</p><p>And only 1 <strong>banker tip</strong> in a day.</p>"
    },
    {
      "heading": "What is BANKER?",
      "body": "<p><strong>BANKER</strong> tip means that is the <strong>surest tip</strong> from the tipster for that day. The tipster must be very sure before posting it.</p><p>Only people that subscribe to the tipster can view his Banker tips.</p>"
    },
    {
      "heading": "How much is Banker subscription?",
      "body": "<p>The amount is determined by the tipster, not us. But it is between $10 and $30 for 2 weeks.</p>"
    },
    {
      "heading": "Qualification to post Banker",
      "body": "<p>Before you can post Banker tips, we must ensure that you are qualified. You can deliver consistent winning and you will not defraud your subscribers.</p><ul><li>You must have posted at least 40 free tips.</li><li>Won at least 60% of them</li><li>And you are very active on the platform</li></ul>"
    },
    {
      "heading": "How to submit won games",
      "body": "<p>Once your prediction has played and won, simply click the prediction, and select <strong>WON</strong>. It will be recorded instantly in our system.</p><p>We review these tips from time to time to ensure sincerity. You will be instantly suspended or blocked if you lie about your prediction</p>"
    },
    {
      "heading": "How to subscribe",
      "body": "<p>Simply go to the profile of the person you want to subscribe to, if the person is qualified to post banker tips, you will see a &ldquo;SUBSCRIBE&rdquo; button. Click on the button and subscribe. We accept ATM card transaction, Mpesa, and Mobile Money.</p>"
    },
    {
      "heading": "How to receive your cash from subscribers",
      "body": "<p>Once someone subscribes to you, the money will be transferred to the account details on your profile within 48 hours. Tipshub charges 25% of every subscription for operations.</p>"
    }
  ]
}"""
    @JvmStatic
    val aboutList: ArrayList<Map<String, String>>
        get() {
            val aboutList = ArrayList<Map<String, String>>()
            try {
                val aboutJsonObject = JSONObject(aboutJsonString)
                val jsonArray = aboutJsonObject.getJSONArray("about")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val about: MutableMap<String, String> = HashMap()
                    val heading = jsonObject.optString("heading")
                    val body = jsonObject.optString("body")
                    about["heading"] = heading
                    about["body"] = body
                    aboutList.add(about)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return aboutList
        }
}