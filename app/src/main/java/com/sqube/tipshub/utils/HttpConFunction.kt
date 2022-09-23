package com.sqube.tipshub.utils

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpConFunction {
    fun executeGet(targetURL: String?, requestingFragment: String): String? {
        val url: URL
        var connection: HttpURLConnection? = null
        return try {
            //Create connection
            url = URL(targetURL)
            connection = url.openConnection() as HttpURLConnection
            if (requestingFragment == "HOME") {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json;  charset=utf-8")
                connection.setRequestProperty("x-rapidapi-host", "football-prediction-api.p.rapidapi.com")
                connection.setRequestProperty("x-rapidapi-key", "894508aa72msha7ba8fc97347ac4p13bc3djsn981a951e01ff")
            } else {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Content-Type", "application/json;  charset=utf-8")
                connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)")
            }

            //connection.setUseCaches(true);
            connection.doInput = true
            connection.doOutput = false
            val inputStream: InputStream
            Log.i("HttpConFunction", "ResponseCode: " + connection.responseCode)
            inputStream = if (connection.responseCode == HttpURLConnection.HTTP_OK) connection.inputStream else connection.errorStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            val response = StringBuilder()
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
                response.append("\n")
            }
            reader.close()
            Log.i("HttpConFunction", response.toString())
            if (response.length == 0) null else response.toString()
        } catch (e: Exception) {
            Log.i("HttpConFunction", "exception: " + e.message)
            e.printStackTrace()
            null
        } finally {
            connection?.disconnect()
        }
    }
}