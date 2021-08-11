package utils

import android.content.Context
import java.io.*
import java.lang.Exception
import java.net.URLEncoder

object CacheHelper {
    var cacheLifeHour = 7 * 24
    private fun getCacheDirectory(context: Context): String {
        return context.cacheDir.path
    }

    fun save(context: Context, key: String, value: String?) {
        var key = key
        try {
            key = URLEncoder.encode(key, "UTF-8")
            val cache = File(getCacheDirectory(context) + "/" + key + ".srl")
            val out: ObjectOutput = ObjectOutputStream(FileOutputStream(cache))
            out.writeUTF(value)
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun save(context: Context, key: String, value: String?, identifier: String) {
        save(context, key + identifier, value)
    }

    fun retrieve(context: Context, key: String, identifier: String): String {
        return retrieve(context, key + identifier)
    }

    fun retrieve(context: Context, key: String): String {
        var key = key
        try {
            key = URLEncoder.encode(key, "UTF-8")
            val cache = File(getCacheDirectory(context) + "/" + key + ".srl")
            if (cache.exists()) {
                val `in` = ObjectInputStream(FileInputStream(cache))
                val value = `in`.readUTF()
                `in`.close()
                return value
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}