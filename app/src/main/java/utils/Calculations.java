package utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import models.Post;

public final class Calculations {
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    public double getPostRelevance(long like, long dislike, long repost){
        long postRelevance = (1+like + repost +(like*repost))/(1+dislike);
        return postRelevance;
    }

    public double getUserRelevance(long followers, long following, long subscribers, long subscribedTo){
        double userRelevance = (2*subscribers)+ subscribedTo + followers + (0.5* following);
        return userRelevance;
    }

    public void Like(final boolean disliked, final String postId, final String userId){
        //Check if post exist first
        final DocumentReference postPath =  database.collection("posts").document(postId);
        postPath.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Post post = documentSnapshot.toObject(Post.class);
                    if(disliked){
                        post.setDislikesCount(post.getDislikesCount() - 1);
                        Map<String, Object> dislike = new HashMap<>();
                        dislike.put(userId, FieldValue.delete());
                        database.collection("dislikes").document(postId).update(dislike);
                    }
                    post.setLikesCount(post.getLikesCount()+1);
                    double postRelevance = getPostRelevance(post.getLikesCount(), post.getDislikesCount(), post.getRepostCount());
                    //add user to Likes list
                    Map<String, Object> update = new HashMap<>();
                    update.put("relevance", postRelevance);
                    update.put("likesCount", post.getLikesCount());
                    update.put("dislikesCount", post.getDislikesCount());
                    postPath.set(update, SetOptions.merge());
                    Map<String, Object> like = new HashMap<>();
                    like.put(userId, true);
                    database.collection("likes").document(postId).set(like, SetOptions.merge());
                    if(disliked){
                        Map<String, Object> dislike = new HashMap<>();
                        dislike.put(userId, FieldValue.delete());
                        database.collection("dislikes").document(postId).update(dislike);
                    }
                }
            }
        });
        //send notification
    }

    public void Unlike(final String postId, final String userId){
        final DocumentReference postPath =  database.collection("posts").document(postId);
        postPath.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Post post = documentSnapshot.toObject(Post.class);
                post.setLikesCount(post.getLikesCount()-1);
                double postRelevance = getPostRelevance(post.getLikesCount(), post.getDislikesCount(), post.getRepostCount());
                //add user to Likes list
                Map<String, Object> update = new HashMap<>();
                update.put("relevance", postRelevance);
                update.put("likesCount", post.getLikesCount());
                postPath.set(update, SetOptions.merge());
                Map<String, Object> like = new HashMap<>();
                like.put(userId, FieldValue.delete());
                database.collection("likes").document(postId).update(like);
            }
        });
    }

    public void Dislike(final boolean liked, final String postId, final String userId){
        //Check if post exist first
        final DocumentReference postPath =  database.collection("posts").document(postId);
        postPath.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Post post = documentSnapshot.toObject(Post.class);
                    if(liked){
                        post.setLikesCount(post.getLikesCount() - 1);
                        Map<String, Object> like = new HashMap<>();
                        like.put(userId, FieldValue.delete());
                        database.collection("likes").document(postId).update(like);
                    }
                    post.setDislikesCount(post.getDislikesCount()+1);
                    double postRelevance = getPostRelevance(post.getLikesCount(), post.getDislikesCount(), post.getRepostCount());
                    //add user to Likes list
                    Map<String, Object> update = new HashMap<>();
                    update.put("relevance", postRelevance);
                    update.put("likesCount", post.getLikesCount());
                    update.put("dislikesCount", post.getDislikesCount());
                    postPath.set(update, SetOptions.merge());
                    Map<String, Object> dislike = new HashMap<>();
                    dislike.put(userId, true);
                    database.collection("dislikes").document(postId).set(dislike, SetOptions.merge());
                }
                else{
                }
            }
        });
        //send notification
    }

    public void Undislike(final String postId, final String userId){
        final DocumentReference postPath =  database.collection("posts").document(postId);
        postPath.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Post post = documentSnapshot.toObject(Post.class);
                post.setDislikesCount(post.getDislikesCount()-1);
                double postRelevance = getPostRelevance(post.getLikesCount(), post.getDislikesCount(), post.getRepostCount());
                //add user to Likes list
                Map<String, Object> update = new HashMap<>();
                update.put("relevance", postRelevance);
                update.put("dislikesCount", post.getDislikesCount());
                postPath.set(update, SetOptions.merge());
                Map<String, Object> dislike = new HashMap<>();
                dislike.put(userId, FieldValue.delete());
                database.collection("dislikes").document(postId).update(dislike);
            }
        });
    }

    public boolean postExist(DocumentReference postRef, final Context context){
        final boolean[] status = {true};
        postRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    if (!task.getResult().exists()) {
                        Toast.makeText(context, "Post has been deleted", Toast.LENGTH_LONG).show();
                        status[0] = false;
                    }
                }
            }
        });
        return status[0];
    }

}
