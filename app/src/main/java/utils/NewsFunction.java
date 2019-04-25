package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewsFunction {

    public static String excuteGet(String targetURL, String urlParameters) {
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("content-type", "application/json;  charset=utf-8");


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
            return null;

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
