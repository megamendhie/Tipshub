package utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.sqube.tipshub.R;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.ProfileMedium;

public final class Reusable {
    public Reusable(){}

    public static String getTime(long messageTime){
        // Format the date before showing it
        final long diff = (new Date().getTime())  - messageTime;
        final long diffSeconds = diff / 1000 % 60;
        final long diffMinutes = diff / (60 * 1000) % 60;
        final long diffHours = diff / (60 * 60 * 1000) % 24;
        final long diffDays = diff / (24 * 60 * 60 * 1000);

        String time;
        if(diffDays > 0)
            time = DateFormat.format("dd MMM  (h:mm a)", messageTime).toString();
        else if(diffHours > 23)
            time = DateFormat.format("dd MMM  (h:mm a)", messageTime).toString();
        else if(diffHours > 0)
            time = diffHours+"h ago";
        else if(diffMinutes >0)
            time = diffMinutes==1? "1min":diffMinutes+"mins";
        else if(diffSeconds > 8)
            time = diffSeconds+"s";
        else
            time = "now";
        return time;
    }

    public static String getSignature(){
        String Signature = "";
        String s = DateFormat.format("HH", new Date().getTime()).toString();
        int sign = Integer.parseInt(s);
        if(sign>=18){
            Signature = "d";
        }else if(sign>=12){
            Signature = "c";
        }else if(sign>=6){
            Signature = "b";
        }else{
            Signature = "a";
        }
        return Signature;
    }

    public static int getPlaceholderImage(char symbol){
        if((symbol >= 'a' && symbol <= 'e')||(symbol >= 'A' && symbol <= 'E')||(symbol >= '0' && symbol <= '1')){
            return R.drawable.pic_a;
        }
        else if((symbol >= 'f' && symbol <= 'j')||(symbol >= 'F' && symbol <= 'J')||(symbol >= '2' && symbol <= '3')){
            return R.drawable.pic_b;
        }
        else if((symbol >= 'k' && symbol <= 'o')||(symbol >= 'K' && symbol <= 'O')||(symbol >= '4' && symbol <= '5')){
            return R.drawable.pic_c;
        }
        else if((symbol >= 'p' && symbol <= 't')||(symbol >= 'P' && symbol <= 'T')||(symbol >= '6' && symbol <= '7')){
            return R.drawable.pic_d;
        }
        else if((symbol >= 'u' && symbol <= 'z')||(symbol >= 'U' && symbol <= 'Z')||(symbol >= '8' && symbol <= '9')){
            return R.drawable.pic_e;
        }

        return R.drawable.pic_a;
    }

    public static void applyLinkfy(Context context, String a, TextView textView) {
        //Pattern urlPattern = Patterns.WEB_URL;
        Pattern mentionPattern = Pattern.compile("(@[A-Za-z0-9_-]+)");
        Pattern hashtagPattern = Pattern.compile("#(\\w+|\\W+)");

        Matcher o = hashtagPattern.matcher(a);
        Matcher mention = mentionPattern.matcher(a);
        //Matcher weblink = urlPattern.matcher(a);

        SpannableString spannableString = new SpannableString(a);
        //#hashtags

        while (o.find()) {
            spannableString.setSpan(new NonUnderlinedClickableSpan(context, o.group(),
                            0), o.start(), o.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // --- @mention
        while (mention.find()) {
            spannableString.setSpan(
                    new NonUnderlinedClickableSpan(context, mention.group(), 1), mention.start(), mention.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        /*
        //@weblink
        while (weblink.find()) {
            spannableString.setSpan(
                    new NonUnderlinedClickableSpan(context, weblink.group(), 2), weblink.start(), weblink.end(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        */
        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void shareTips(Context context, String username, String post){
        String output = "@"+username+"'s post on Tipshub:\n\n"+ post + "\n\nDownload Tipshub: http://bit.ly/tipshub" ;

        Intent share = new Intent(Intent.ACTION_SEND);share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, output);
        share.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(Intent.createChooser(share, "Share via:"));
    }
    public static String getNewDate(String oldDate){
        try {
            Date date= new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(oldDate);
            return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void shareComment(Context context, String username, String post){
        String output = "@"+username+"'s comment on Tipshub:\n\n"+ post + "\n\nDownload Tipshub: http://bit.ly/tipshub" ;

        Intent share = new Intent(Intent.ACTION_SEND);share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, output);
        share.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(Intent.createChooser(share, "Share via:"));
    }

    public  static  boolean getNetworkAvailability(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public static void grabImage(String photoUrl){
        ImageDownloader getBitmapfromUrl = new ImageDownloader();
        getBitmapfromUrl.execute(photoUrl);
    }

    private static void uploadImage(Bitmap bitmap){
        if(bitmap==null){
            Log.i("Reusable", "onClick: bitmap is null");
            return;
        }
        //convert Bitmap to output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId= auth.getCurrentUser().getUid();

        //upload image as bytes to Firebase Storage
        storage.getReference().child("profile_images").child(userId).putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storage.getReference().child("profile_images").child(userId).getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        //update user database with dp_url
                                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                                        database.collection("profiles").document(userId)
                                                .update("b2_dpUrl", uri.toString());

                                    }
                                });
                        bitmap.recycle();
                        Log.i("Reusable", "onSuccess: ");
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                bitmap.recycle();
                Log.i("Reusable", "onFailure: ");
            }
        });
    }

    static long[] getStatsForDelete(ProfileMedium profile, int type, boolean won){
        long tPost = 0;
        long wGames = 0;
        switch (type){
            case 1:
                tPost = profile.getE1a_NOG();
                wGames = profile.getE1b_WG();
                break;
            case 2:
                tPost = profile.getE2a_NOG();
                wGames = profile.getE2b_WG();
                break;
            case 3:
                tPost = profile.getE3a_NOG();
                wGames = profile.getE3b_WG();
                break;
            case 4:
                tPost = profile.getE4a_NOG();
                wGames = profile.getE4b_WG();
                break;
            case 5:
                tPost = profile.getE5a_NOG();
                wGames = profile.getE5b_WG();
                break;
            case 6:
                tPost = profile.getE6a_NOG();
                wGames = profile.getE6b_WG();
                break;
        }
        tPost -= 1;
        wGames = won? wGames - 1: wGames;
        final long wPercentage = tPost>0? ((wGames*100)/tPost) : 0;
        return new long[]{tPost, wGames, wPercentage};
    }

    public static long[] getStatsForPost(ProfileMedium profile, int type){
        long tPost = 0;
        long wGames = 0;
        switch (type){
            case 1:
                tPost = profile.getE1a_NOG();
                wGames = profile.getE1b_WG();
                break;
            case 2:
                tPost = profile.getE2a_NOG();
                wGames = profile.getE2b_WG();
                break;
            case 3:
                tPost = profile.getE3a_NOG();
                wGames = profile.getE3b_WG();
                break;
            case 4:
                tPost = profile.getE4a_NOG();
                wGames = profile.getE4b_WG();
                break;
            case 5:
                tPost = profile.getE5a_NOG();
                wGames = profile.getE5b_WG();
                break;
            case 6:
                tPost = profile.getE6a_NOG();
                wGames = profile.getE6b_WG();
                break;
        }
        tPost += 1;
        final long wPercentage = tPost>0? ((wGames*100)/tPost) : 0;
        return new long[]{tPost, wPercentage};
    }

    static long[] getStatsForWonPost(ProfileMedium profile, int type){
        long tPost = 0;
        long wGames = 0;
        switch (type){
            case 1:
                tPost = profile.getE1a_NOG();
                wGames = profile.getE1b_WG();
                break;
            case 2:
                tPost = profile.getE2a_NOG();
                wGames = profile.getE2b_WG();
                break;
            case 3:
                tPost = profile.getE3a_NOG();
                wGames = profile.getE3b_WG();
                break;
            case 4:
                tPost = profile.getE4a_NOG();
                wGames = profile.getE4b_WG();
                break;
            case 5:
                tPost = profile.getE5a_NOG();
                wGames = profile.getE5b_WG();
                break;
            case 6:
                tPost = profile.getE6a_NOG();
                wGames = profile.getE6b_WG();
                break;
        }
        wGames += 1;
        long wPercentage = tPost>0? ((wGames*100)/tPost) : 0;
        return new long[]{wGames, wPercentage};
    }

    static class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            uploadImage(bitmap);
        }

        @Override
        protected Bitmap doInBackground(String... imageUrl) {
            //link to image
            String link = imageUrl[0].replace("s96-c/photo.jpg", "s400-c/photo.jpg");

            //open Http connection to image and retrieve image
            try {
                URL url = new URL(link);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();

                //convert image to bitmap
                return BitmapFactory.decodeStream(input);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void updateAlgoliaIndex(String firstName, String lastName, String username, String objectID, long score, boolean newEntry){
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("a0_firstName",  firstName);
        userDetails.put("a1_lastName",lastName);
        userDetails.put("a2_username", username);
        userDetails.put("c2_score", score);
        userDetails.put("objectID", objectID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        if(newEntry){
            ref = ref.child("algolia_add");
            try {
                String email = FirebaseUtil.getFirebaseAuthentication().getCurrentUser().getEmail();
                userDetails.put("a3_email", email);
            }
            catch (NullPointerException e){
                Log.i("ReusableClass", "updateAlgoliaIndex error: "+ e.getMessage());
            }
        }
        else
            ref = ref.child("algolia_update");
        ref.push().setValue(userDetails);
    }
}