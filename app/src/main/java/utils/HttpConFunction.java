package utils;

import android.util.Log;

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

            if(requestingFragment.equals("HOME")){
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json;  charset=utf-8");
                connection.setRequestProperty("x-rapidapi-host",  "football-prediction-api.p.rapidapi.com");
                connection.setRequestProperty("x-rapidapi-key", "894508aa72msha7ba8fc97347ac4p13bc3djsn981a951e01ff");
            }
            else{
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "application/json;  charset=utf-8");
                connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            }

            //connection.setUseCaches(true);
            connection.setDoInput(true);
            connection.setDoOutput(false);
            InputStream inputStream;

            Log.i("HttpConFunction", "ResponseCode: " + connection.getResponseCode());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                inputStream = connection.getInputStream();
            else
                inputStream = connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append("\n");
            }
            reader.close();
            Log.i("HttpConFunction", response.toString());
            if(response.length()==0)
                return null;
            else
                return response.toString();

        } catch (Exception e) {
            Log.i("HttpConFunction", "exception: " + e.getMessage());
            e.printStackTrace();
            return null;

        } finally {
            if (connection != null)
                connection.disconnect();
        }
    }
}
