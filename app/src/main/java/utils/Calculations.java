package utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Calculations {
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private String TAG = "Calculations";
    Context context;

    public Calculations(Context context){
        this.context = context;
    }

    public double getPostRelevance(long like, long dislike, long repost){
        return (1+like + repost +(like*repost))/(1+dislike);
    }

    public double getCommentRelevance(long like, long dislike){
        return (like + (0.5* dislike));
    }

    public double getUserRelevance(long followers, long following, long subscribers, long subscribedTo){
        return (2*subscribers)+ subscribedTo + followers + (0.5* following);
    }

    public void onLike(final String postId, final String userId){
        final DocumentReference postPath =  database.collection("posts").document(postId);
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Log.i(TAG, "apply: likes entered");
                DocumentSnapshot snapshot = transaction.get(postPath);
                //check if post still exists
                if(!snapshot.exists()){
                    Log.i(TAG, "apply: like doesn't exist");
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long likesCount = snapshot.getLong("likesCount");
                long dislikesCount = snapshot.getLong("dislikesCount");
                long repostCount = snapshot.getLong("repostCount");
                List<String> likes = (List) snapshot.get("likes");
                List<String> dislikes = (List) snapshot.get("dislikes");
                Map<String, Object> upd = new HashMap<>();
                if(dislikes.contains(userId)){
                    dislikesCount -=1;
                    likesCount +=1;
                    dislikes.remove(userId);
                    likes.add(userId);
                }
                else{
                    if(likes.contains(userId)){
                        likesCount -=1;
                        likes.remove(userId);
                    }
                    else{
                        likesCount +=1;
                        likes.add(userId);
                    }
                }
                double postRelevance = getPostRelevance(likesCount, dislikesCount, repostCount);
                upd.put("likesCount", likesCount);
                upd.put("dislikesCount", dislikesCount);
                upd.put("likes", likes);
                upd.put("dislikes", dislikes);
                upd.put("relevance", postRelevance);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
            }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });

        //send notification
    }

    public void onDislike(final String postId, final String userId){
        final DocumentReference postPath =  database.collection("posts").document(postId);
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(postPath);
                //Check if post exist first
                if(!snapshot.exists()){
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long likesCount = snapshot.getLong("likesCount");
                long dislikesCount = snapshot.getLong("dislikesCount");
                long repostCount = snapshot.getLong("repostCount");
                List<String> likes = (List) snapshot.get("likes");
                List<String> dislikes = (List) snapshot.get("dislikes");
                Map<String, Object> upd = new HashMap<>();
                if(likes.contains(userId)){
                    likesCount -=1;
                    dislikesCount +=1;
                    likes.remove(userId);
                    dislikes.add(userId);
                }
                else{
                    if(dislikes.contains(userId)){
                        dislikesCount -=1;
                        dislikes.remove(userId);
                    }
                    else{
                        dislikesCount +=1;
                        dislikes.add(userId);
                    }
                }
                double postRelevance = getPostRelevance(likesCount, dislikesCount, repostCount);
                upd.put("likesCount", likesCount);
                upd.put("dislikesCount", dislikesCount);
                upd.put("likes", likes);
                upd.put("dislikes", dislikes);
                upd.put("relevance", postRelevance);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
        //send notification
    }

    public void onCommentLike(final DocumentReference commentRef, final String userId){
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Log.i(TAG, "apply: likes entered");
                DocumentSnapshot snapshot = transaction.get(commentRef);
                //check if post still exists
                if(!snapshot.exists()){
                    Log.i(TAG, "apply: like doesn't exist");
                    return null;
                }

                //retrieve likes, likesCount, dislikes and dislikesCount from snapshot
                long likesCount = snapshot.getLong("likesCount");
                long dislikesCount = snapshot.getLong("dislikesCount");
                List<String> likes = (List) snapshot.get("likes");
                List<String> dislikes = (List) snapshot.get("dislikes");
                Map<String, Object> upd = new HashMap<>();
                if(dislikes.contains(userId)){
                    dislikesCount -=1;
                    likesCount +=1;
                    dislikes.remove(userId);
                    likes.add(userId);
                }
                else{
                    if(likes.contains(userId)){
                        likesCount -=1;
                        likes.remove(userId);
                    }
                    else{
                        likesCount +=1;
                        likes.add(userId);
                    }
                }
                double postRelevance = getCommentRelevance(likesCount, dislikesCount);
                upd.put("likesCount", likesCount);
                upd.put("dislikesCount", dislikesCount);
                upd.put("likes", likes);
                upd.put("dislikes", dislikes);
                upd.put("relevance", postRelevance);
                transaction.update(commentRef, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });

        //send notification
    }

    public void onCommentDislike(final DocumentReference commentRef, final String userId){
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(commentRef);
                //Check if post exist first
                if(!snapshot.exists()){
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long likesCount = snapshot.getLong("likesCount");
                long dislikesCount = snapshot.getLong("dislikesCount");
                List<String> likes = (List) snapshot.get("likes");
                List<String> dislikes = (List) snapshot.get("dislikes");
                Map<String, Object> upd = new HashMap<>();
                if(likes.contains(userId)){
                    likesCount -=1;
                    dislikesCount +=1;
                    likes.remove(userId);
                    dislikes.add(userId);
                }
                else{
                    if(dislikes.contains(userId)){
                        dislikesCount -=1;
                        dislikes.remove(userId);
                    }
                    else{
                        dislikesCount +=1;
                        dislikes.add(userId);
                    }
                }
                double postRelevance = getCommentRelevance(likesCount, dislikesCount);
                upd.put("likesCount", likesCount);
                upd.put("dislikesCount", dislikesCount);
                upd.put("likes", likes);
                upd.put("dislikes", dislikes);
                upd.put("relevance", postRelevance);
                transaction.update(commentRef, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
        //send notification
    }

}