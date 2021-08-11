package utils

import android.content.Context
import utils.FirebaseUtil.firebaseAuthentication
import android.content.Intent
import android.os.AsyncTask
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.sqube.tipshub.R
import android.widget.TextView
import android.text.SpannableString
import utils.NonUnderlinedClickableSpan
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.text.format.DateFormat
import android.util.Log
import utils.Reusable.ImageDownloader
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.OnFailureListener
import models.ProfileMedium
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import utils.FirebaseUtil
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.lang.NullPointerException
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class Reusable {
    fun shareComment(context: Context, username: String, post: String) {
        val output = "@$username's comment on Tipshub:\n\n$post\n\nDownload Tipshub: http://bit.ly/tipshub"
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.putExtra(Intent.EXTRA_TEXT, output)
        share.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        context.startActivity(Intent.createChooser(share, "Share via:"))
    }

    internal class ImageDownloader : AsyncTask<String, Void, Bitmap>() {
        override fun onPostExecute(bitmap: Bitmap?) {
            uploadImage(bitmap)
        }

        override fun doInBackground(vararg imageUrl: String): Bitmap? {
            //link to image
            val link = imageUrl[0].replace("s96-c/photo.jpg", "s400-c/photo.jpg")

            //open Http connection to image and retrieve image
            return try {
                val url = URL(link)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream

                //convert image to bitmap
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    companion object {
        @JvmStatic
        fun getTime(messageTime: Long): String {
            // Format the date before showing it
            val diff = Date().time - messageTime
            val diffSeconds = diff / 1000 % 60
            val diffMinutes = diff / (60 * 1000) % 60
            val diffHours = diff / (60 * 60 * 1000) % 24
            val diffDays = diff / (24 * 60 * 60 * 1000)
            val time: String
            time = if (diffDays > 0) DateFormat.format("dd MMM  (h:mm a)", messageTime).toString() else if (diffHours > 23) DateFormat.format("dd MMM  (h:mm a)", messageTime).toString() else if (diffHours > 0) diffHours.toString() + "h ago" else if (diffMinutes > 0) if (diffMinutes == 1L) "1min" else diffMinutes.toString() + "mins" else if (diffSeconds > 8) diffSeconds.toString() + "s" else "now"
            return time
        }

        @JvmStatic
        val signature: String
            get() {
                var Signature = ""
                val s = DateFormat.format("HH", Date().time).toString()
                val sign = s.toInt()
                Signature = if (sign >= 18) {
                    "d"
                } else if (sign >= 12) {
                    "c"
                } else if (sign >= 6) {
                    "b"
                } else {
                    "a"
                }
                return Signature
            }

        @JvmStatic
        fun getPlaceholderImage(symbol: Char): Int {
            if (symbol >= 'a' && symbol <= 'e' || symbol >= 'A' && symbol <= 'E' || symbol >= '0' && symbol <= '1') {
                return R.drawable.pic_a
            } else if (symbol >= 'f' && symbol <= 'j' || symbol >= 'F' && symbol <= 'J' || symbol >= '2' && symbol <= '3') {
                return R.drawable.pic_b
            } else if (symbol >= 'k' && symbol <= 'o' || symbol >= 'K' && symbol <= 'O' || symbol >= '4' && symbol <= '5') {
                return R.drawable.pic_c
            } else if (symbol >= 'p' && symbol <= 't' || symbol >= 'P' && symbol <= 'T' || symbol >= '6' && symbol <= '7') {
                return R.drawable.pic_d
            } else if (symbol >= 'u' && symbol <= 'z' || symbol >= 'U' && symbol <= 'Z' || symbol >= '8' && symbol <= '9') {
                return R.drawable.pic_e
            }
            return R.drawable.pic_a
        }

        @JvmStatic
        fun applyLinkfy(context: Context?, a: String?, textView: TextView) {
            //Pattern urlPattern = Patterns.WEB_URL;
            val mentionPattern = Pattern.compile("(@[A-Za-z0-9_-]+)")
            val hashtagPattern = Pattern.compile("#(\\w+|\\W+)")
            val o = hashtagPattern.matcher(a)
            val mention = mentionPattern.matcher(a)
            //Matcher weblink = urlPattern.matcher(a);
            val spannableString = SpannableString(a)
            //#hashtags
            while (o.find()) {
                spannableString.setSpan(NonUnderlinedClickableSpan(context!!, o.group(),
                        0), o.start(), o.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            // --- @mention
            while (mention.find()) {
                spannableString.setSpan(
                        NonUnderlinedClickableSpan(context!!, mention.group(), 1), mention.start(), mention.end(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            /*
        //@weblink
        while (weblink.find()) {
            spannableString.setSpan(
                    new NonUnderlinedClickableSpan(context, weblink.group(), 2), weblink.start(), weblink.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        */textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        @JvmStatic
        fun shareTips(context: Context, username: String, post: String) {
            val output = "@$username's post on Tipshub:\n\n$post\n\nDownload Tipshub: http://bit.ly/tipshub"
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.putExtra(Intent.EXTRA_TEXT, output)
            share.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            context.startActivity(Intent.createChooser(share, "Share via:"))
        }

        @JvmStatic
        fun getNewDate(oldDate: String?): String {
            try {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(oldDate)
                return SimpleDateFormat("dd MMM", Locale.getDefault()).format(date.time)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return ""
        }

        @JvmStatic
        fun getNetworkAvailability(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

        @JvmStatic
        fun grabImage(photoUrl: String?) {
            val getBitmapfromUrl = ImageDownloader()
            getBitmapfromUrl.execute(photoUrl)
        }

        private fun uploadImage(bitmap: Bitmap?) {
            if (bitmap == null) {
                Log.i("Reusable", "onClick: bitmap is null")
                return
            }
            //convert Bitmap to output stream
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
            val data = baos.toByteArray()
            val storage = FirebaseStorage.getInstance()
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser!!.uid

            //upload image as bytes to Firebase Storage
            storage.reference.child("profile_images").child(userId).putBytes(data)
                    .addOnSuccessListener {
                        storage.reference.child("profile_images").child(userId).downloadUrl
                                .addOnSuccessListener { uri -> //update user database with dp_url
                                    val database = FirebaseFirestore.getInstance()
                                    database.collection("profiles").document(userId)
                                            .update("b2_dpUrl", uri.toString())
                                }
                        bitmap.recycle()
                        Log.i("Reusable", "onSuccess: ")
                    }.addOnFailureListener {
                        bitmap.recycle()
                        Log.i("Reusable", "onFailure: ")
                    }
        }

        @JvmStatic
        fun getStatsForDelete(profile: ProfileMedium, type: Int, won: Boolean): LongArray {
            var tPost: Long = 0
            var wGames: Long = 0
            when (type) {
                1 -> {
                    tPost = profile.e1a_NOG
                    wGames = profile.e1b_WG
                }
                2 -> {
                    tPost = profile.e2a_NOG
                    wGames = profile.e2b_WG
                }
                3 -> {
                    tPost = profile.e3a_NOG
                    wGames = profile.e3b_WG
                }
                4 -> {
                    tPost = profile.e4a_NOG
                    wGames = profile.e4b_WG
                }
                5 -> {
                    tPost = profile.e5a_NOG
                    wGames = profile.e5b_WG
                }
                6 -> {
                    tPost = profile.e6a_NOG
                    wGames = profile.e6b_WG
                }
            }
            tPost -= 1
            wGames = if (won) wGames - 1 else wGames
            val wPercentage = if (tPost > 0) wGames * 100 / tPost else 0
            return longArrayOf(tPost, wGames, wPercentage)
        }

        @JvmStatic
        fun getStatsForPost(profile: ProfileMedium, type: Int): LongArray {
            var tPost: Long = 0
            var wGames: Long = 0
            when (type) {
                1 -> {
                    tPost = profile.e1a_NOG
                    wGames = profile.e1b_WG
                }
                2 -> {
                    tPost = profile.e2a_NOG
                    wGames = profile.e2b_WG
                }
                3 -> {
                    tPost = profile.e3a_NOG
                    wGames = profile.e3b_WG
                }
                4 -> {
                    tPost = profile.e4a_NOG
                    wGames = profile.e4b_WG
                }
                5 -> {
                    tPost = profile.e5a_NOG
                    wGames = profile.e5b_WG
                }
                6 -> {
                    tPost = profile.e6a_NOG
                    wGames = profile.e6b_WG
                }
            }
            tPost += 1
            val wPercentage = if (tPost > 0) wGames * 100 / tPost else 0
            return longArrayOf(tPost, wPercentage)
        }

        @JvmStatic
        fun getStatsForWonPost(profile: ProfileMedium, type: Int): LongArray {
            var tPost: Long = 0
            var wGames: Long = 0
            when (type) {
                1 -> {
                    tPost = profile.e1a_NOG
                    wGames = profile.e1b_WG
                }
                2 -> {
                    tPost = profile.e2a_NOG
                    wGames = profile.e2b_WG
                }
                3 -> {
                    tPost = profile.e3a_NOG
                    wGames = profile.e3b_WG
                }
                4 -> {
                    tPost = profile.e4a_NOG
                    wGames = profile.e4b_WG
                }
                5 -> {
                    tPost = profile.e5a_NOG
                    wGames = profile.e5b_WG
                }
                6 -> {
                    tPost = profile.e6a_NOG
                    wGames = profile.e6b_WG
                }
            }
            wGames += 1
            val wPercentage = if (tPost > 0) wGames * 100 / tPost else 0
            return longArrayOf(wGames, wPercentage)
        }

        @JvmStatic
        fun updateAlgoliaIndex(firstName: String?, lastName: String?, username: String?, objectID: String?, score: Long, newEntry: Boolean) {
            val userDetails: MutableMap<String, Any?> = HashMap()
            userDetails["a0_firstName"] = firstName
            userDetails["a1_lastName"] = lastName
            userDetails["a2_username"] = username
            userDetails["c2_score"] = score
            userDetails["objectID"] = objectID
            var ref = FirebaseDatabase.getInstance().reference
            if (newEntry) {
                ref = ref.child("algolia_add")
                try {
                    val email = firebaseAuthentication!!.currentUser!!.email
                    userDetails["a3_email"] = email
                } catch (e: NullPointerException) {
                    Log.i("ReusableClass", "updateAlgoliaIndex error: " + e.message)
                }
            } else ref = ref.child("algolia_update")
            ref.push().setValue(userDetails)
        }
    }
}