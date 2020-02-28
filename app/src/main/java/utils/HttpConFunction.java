package utils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpConFunction {

    public String executeGet(String targetURL, String requestingFragment) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("content-type", "application/json;  charset=utf-8");

            if(requestingFragment.equals("HOME")){
                connection.setRequestMethod("GET");
                connection.setRequestProperty("x-rapidapi-host",  "football-prediction-api.p.rapidapi.com");
                connection.setRequestProperty("x-rapidapi-key", "894508aa72msha7ba8fc97347ac4p13bc3djsn981a951e01ff");
            }
            else
                connection.setRequestProperty("Content-Language", "en-US");


            //connection.setUseCaches(true);
            connection.setDoInput(true);
            connection.setDoOutput(false);

            InputStream is;

            int status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK)
                is = connection.getErrorStream();
            else
                is = connection.getInputStream();


            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();
            return response.toString();

        } catch (Exception e) {
            Log.i("HttpConFunction", "exception: " + e.getMessage());
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static JSONObject getFlags(InputStream inputStream){
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String eachLine;
        StringBuffer response = new StringBuffer();
        JSONObject flagsJSONObject = null;

        try {
            while ((eachLine = reader.readLine()) != null) {
                response.append(eachLine);
                response.append('\r');
            }
            reader.close();
            flagsJSONObject = new JSONObject(response.toString());
        }
        catch (Exception e){
            Log.i("getStream", "exception: " + e.getMessage());
        }
        return flagsJSONObject;
    }
}