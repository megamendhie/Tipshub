package utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AboutUtil {

    private static String getAboutJsonString() {
        return  "{\n" +
                "  \"about\": [\n" +
                "    {\n" +
                "      \"heading\": \"Welcome to Tipshub\",\n" +
                "      \"body\": \"Tipshub is a social network for sports fans. It is an interactive platform where users discuss on trending sports events and good tipsters share their predictions with their followers and subscribers.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"What can you do on Tipshub?\",\n" +
                "      \"body\": \"<ul><li>Get latest and accurate prediction from Tipshub</li><li>Discuss any sports event</li><li>Share your predictions with others</li><li>Follow people you like to see their tips.</li><li>Subscribe to good tipsters for their Banker tips.</li><li>Get the latest sports news.</li></ul>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"How does Tipshub predict games?\",\n" +
                "      \"body\": \"<p>We use high quality machine learning software to produce accurate predictions based on the teams' current stats, their h2h, players lineup, and winning capability.</p>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"What kind of post is allowed on Tipshub?\",\n" +
                "      \"body\": \"<p>Since Tipshub is dedicated for sports, we expect sports gist, analysis, and predictions.</p><p>&nbsp;</p><p>DO NOT post anything that is profane, pornographic, fraudulent, false, or insulting as you will be immediately suspended or blocked.</p>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"How may posts per day?\",\n" +
                "      \"body\": \"<p>You can post as many normal posts as you like.</p><p>But you can only <strong>predict</strong> only <strong>4 times</strong> in a day.</p><p>And only 1 <strong>banker tip</strong> in a day.</p>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"What is BANKER?\",\n" +
                "      \"body\": \"<p><strong>BANKER</strong> tip means that is the <strong>surest tip</strong> from the tipster for that day. The tipster must be very sure before posting it.</p><p>Only people that subscribe to the tipster can view his Banker tips.</p>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"How much is Banker subscription?\",\n" +
                "      \"body\": \"<p>The amount is determined by the tipster, not us. But it is between $10 and $30 for 2 weeks.</p>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"Qualification to post Banker\",\n" +
                "      \"body\": \"<p>Before you can post Banker tips, we must ensure that you are qualified. You can deliver consistent winning and you will not defraud your subscribers.</p><ul><li>You must have posted at least 40 free tips.</li><li>Won at least 60% of them</li><li>And you are very active on the platform</li></ul>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"How to submit won games\",\n" +
                "      \"body\": \"<p>Once your prediction has played and won, simply click the prediction, and select <strong>WON</strong>. It will be recorded instantly in our system.</p><p>We review these tips from time to time to ensure sincerity. You will be instantly suspended or blocked if you lie about your prediction</p>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"How to subscribe\",\n" +
                "      \"body\": \"<p>Simply go to the profile of the person you want to subscribe to, if the person is qualified to post banker tips, you will see a &ldquo;SUBSCRIBE&rdquo; button. Click on the button and subscribe. We accept ATM card transaction, Mpesa, and Mobile Money.</p>\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"heading\": \"How to receive your cash from subscribers\",\n" +
                "      \"body\": \"<p>Once someone subscribes to you, the money will be transferred to the account details on your profile within 48 hours. Tipshub charges 25% of every subscription for operations.</p>\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    public static ArrayList<Map<String, String>> getAboutList(){
        ArrayList<Map<String, String>> aboutList = new ArrayList<>();
        try {
            JSONObject aboutJsonObject = new JSONObject(getAboutJsonString());
            JSONArray jsonArray = aboutJsonObject.getJSONArray("about");
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Map<String, String> about = new HashMap<>();

                String heading = jsonObject.optString("heading");
                String body = jsonObject.optString("body");

                about.put("heading", heading);
                about.put("body", body);
                aboutList.add(about);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return aboutList;
    }
}
