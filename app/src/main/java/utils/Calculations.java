package utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Notification;
import models.UserNetwork;

public final class Calculations {
    private FirebaseFirestore database = FirebaseFirestore.getInstance();
    private String TAG = "Calculations";
    private Context context;
    private long count = 0;

    public Calculations(Context context){
        this.context = context;
    }

    public void recommend(final String myId, final String yourId){
        if(UserNetwork.getFollowing()==null){
            return;
        }
        if(!UserNetwork.getFollowing().contains(yourId)){
            final CollectionReference ref =  database.collection("recommended").document(myId)
                    .collection("rec");
            ref.document(yourId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    Map<String, Object> update = new HashMap<>();
                    if(snapshot.exists()){
                        update.put("count", snapshot.get("count", long.class) + 1);
                    }
                    else{
                        update.put("count", 1);
                        update.put("id", yourId);
                        update.put("dateAdded", new Date().getTime());
                    }
                    ref.document(yourId).set(update, SetOptions.merge()).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "onFailure: " + e.getMessage());
                        }
                    });
                }
            });
            ref.orderBy("dateAdded", Query.Direction.ASCENDING).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(queryDocumentSnapshots.size() >= 50){
                                int counter = queryDocumentSnapshots.size() - 40;
                                long time = new Date().getTime();
                                for(DocumentSnapshot snapshot: queryDocumentSnapshots.getDocuments()){
                                    counter--;
                                    if((time - snapshot.get("dateAdded", long.class))> 604800000)
                                        snapshot.getReference().delete();
                                    else
                                        break;
                                    if(counter>=10)
                                        break;
                                }
                            }

                        }
                    });
        }
    }

    public void unrecommend(final String myId, final String yourId){
        database.collection("recommended").document(myId)
                .collection("rec").document(yourId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult()!=null && task.getResult().exists()){
                    task.getResult().getReference().delete();
                }
            }
        });
    }

    public double getPostRelevance(long like, long dislike, long repost){
        return (1+like + repost +(like*repost))/(1+dislike);
    }

    public double getTimeRelevance(double relevance, long time){
        return ((time * relevance)/1000);
    }

    private double getCommentRelevance(long like, long dislike){
        return (like + (0.5* dislike));
    }

    private double getUserRelevance(long followers, long following, long subscribers, long subscribedTo){
        return (2*subscribers)+ subscribedTo + followers + (0.5* following);
    }

    public void onLike(final String postId, final String userId, final String postOwnerId, final String subString){
        final DocumentReference postPath =  database.collection("posts").document(postId);
        final boolean[] like = {true};
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
                long time = snapshot.getLong("time");
                List<String> likes = (List) snapshot.get("likes");
                List<String> dislikes = (List) snapshot.get("dislikes");
                Map<String, Object> upd = new HashMap<>();
                if(dislikes.contains(userId)){
                    like[0] = false;
                    dislikesCount -=1;
                    likesCount +=1;
                    dislikes.remove(userId);
                    likes.add(userId);
                }
                else{
                    if(likes.contains(userId)){
                        like[0] = false;
                        likesCount -=1;
                        likes.remove(userId);
                    }
                    else{
                        setCount(likesCount);
                        likesCount +=1;
                        likes.add(userId);
                    }
                }
                double postRelevance = getPostRelevance(likesCount, dislikesCount, repostCount);
                double timeRelevance = getTimeRelevance(postRelevance, time);
                upd.put("likesCount", likesCount);
                upd.put("dislikesCount", dislikesCount);
                upd.put("likes", likes);
                upd.put("dislikes", dislikes);
                upd.put("relevance", postRelevance);
                upd.put("timeRelevance", timeRelevance);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
                if(like[0] && !userId.equals(postOwnerId)){
                    Log.i(TAG, "onSuccess: recommeded started" + like[0] +" "+ userId.equals(postOwnerId));
                    sendPushNotification(true, userId, postOwnerId, postId, "liked", "post", subString);
                    recommend(userId, postOwnerId);
                }
            }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onDislike(final String postId, final String userId, final String postOwnerId, final String subString){
        final DocumentReference postPath =  database.collection("posts").document(postId);
        final boolean[] dislike = {true};
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
                long time = snapshot.getLong("time");
                List<String> likes = (List) snapshot.get("likes");
                List<String> dislikes = (List) snapshot.get("dislikes");
                Map<String, Object> upd = new HashMap<>();
                if(likes.contains(userId)){
                    dislike[0] = false;
                    likesCount -=1;
                    dislikesCount +=1;
                    likes.remove(userId);
                    dislikes.add(userId);
                }
                else{
                    if(dislikes.contains(userId)){
                        dislike[0] = false;
                        dislikesCount -=1;
                        dislikes.remove(userId);
                    }
                    else{
                        setCount(dislikesCount);
                        dislikesCount +=1;
                        dislikes.add(userId);
                    }
                }
                double postRelevance = getPostRelevance(likesCount, dislikesCount, repostCount);
                double timeRelevance = getTimeRelevance(postRelevance, time);
                upd.put("likesCount", likesCount);
                upd.put("dislikesCount", dislikesCount);
                upd.put("likes", likes);
                upd.put("dislikes", dislikes);
                upd.put("relevance", postRelevance);
                upd.put("timeRelevance", timeRelevance);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");Log.d(TAG, "Transaction success!");
                        if(dislike[0] && !userId.equals(postOwnerId)){
                            Log.i(TAG, "onSuccess: recommeded started" + dislike[0] +" "+ userId.equals(postOwnerId));
                            sendPushNotification(true, userId, postOwnerId, postId, "disliked", "comment", subString);
                        }
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

    public void onCommentLike(final DocumentReference commentRef, final String userId, final String postOwnerId, final String postId, final String mainPostId, final String subString){
        final boolean[] like = {true};
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
                long time = snapshot.getLong("time");
                List<String> likes = (List) snapshot.get("likes");
                List<String> dislikes = (List) snapshot.get("dislikes");
                Map<String, Object> upd = new HashMap<>();
                if(dislikes.contains(userId)){
                    like[0] = false;
                    dislikesCount -=1;
                    likesCount +=1;
                    dislikes.remove(userId);
                    likes.add(userId);
                }
                else{
                    if(likes.contains(userId)){
                        like[0] = false;
                        likesCount -=1;
                        likes.remove(userId);
                    }
                    else{
                        setCount(likesCount);
                        likesCount +=1;
                        likes.add(userId);
                    }
                }
                double postRelevance = getCommentRelevance(likesCount, dislikesCount);
                double timeRelevance = getTimeRelevance(postRelevance, time);
                upd.put("likesCount", likesCount);
                upd.put("dislikesCount", dislikesCount);
                upd.put("likes", likes);
                upd.put("dislikes", dislikes);
                upd.put("relevance", postRelevance);
                upd.put("timeRelevance", timeRelevance);
                transaction.update(commentRef, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                        if(!userId.equals(postOwnerId) && like[0]){
                            sendPushNotification(true, userId, postOwnerId, mainPostId, "liked", "comment", subString);
                            recommend(userId, postOwnerId);
                        }
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

    public void onCommentDislike(final DocumentReference commentRef, final String userId, final String postOwnerId, final String postId, final String mainPostId, final String subString){
        final boolean[] dislike = {true};
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
                long time = snapshot.getLong("time");
                List<String> likes = (List) snapshot.get("likes");
                List<String> dislikes = (List) snapshot.get("dislikes");
                Map<String, Object> upd = new HashMap<>();
                if(likes.contains(userId)){
                    dislike[0] = false;
                    likesCount -=1;
                    dislikesCount +=1;
                    likes.remove(userId);
                    dislikes.add(userId);
                }
                else{
                    if(dislikes.contains(userId)){
                        dislike[0] = false;
                        dislikesCount -=1;
                        dislikes.remove(userId);
                    }
                    else{
                        setCount(dislikesCount);
                        dislikesCount +=1;
                        dislikes.add(userId);
                    }
                }
                double postRelevance = getCommentRelevance(likesCount, dislikesCount);
                double timeRelevance = getTimeRelevance(postRelevance, time);
                upd.put("likesCount", likesCount);
                upd.put("dislikesCount", dislikesCount);
                upd.put("likes", likes);
                upd.put("dislikes", dislikes);
                upd.put("relevance", postRelevance);
                upd.put("timeRelevance", timeRelevance);
                transaction.update(commentRef, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                        if(!userId.equals(postOwnerId) && dislike[0]){
                            sendPushNotification(true, userId, postOwnerId, mainPostId, "liked", "comment", subString);
                            recommend(userId, postOwnerId);
                        }
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

    public void increaseSubcriptions(String myId, String yourId){
        final DocumentReference postPath =  database.collection("profile").document(myId);
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(postPath);
                //Check if post exist first
                if(!snapshot.exists()){
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long followers = snapshot.getLong("c4_followers");
                long following = snapshot.getLong("c5_following");
                long subscribers = snapshot.getLong("c6_subscribers");
                long subscriptions = snapshot.getLong("c7_subscriptions") + 1;
                double score = getUserRelevance(followers, following, subscribers, subscriptions);
                Map<String, Object> upd = new HashMap<>();
                upd.put("c2_score", score);
                upd.put("c4_followers", followers);
                upd.put("c5_following", following);
                upd.put("c6_subscribers", subscribers);
                upd.put("c7_subscriptions", subscriptions);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Add person's id to user's subscription list
                        database.collection("subscribed_to").document(myId)
                                .update("list", FieldValue.arrayUnion(yourId));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void increaseSubcribers(String myId, String yourId){
        final DocumentReference postPath =  database.collection("profile").document(yourId);
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(postPath);
                //Check if post exist first
                if(!snapshot.exists()){
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long followers = snapshot.getLong("c4_followers");
                long following = snapshot.getLong("c5_following");
                long subscribers = snapshot.getLong("c6_subscribers")+1;
                long subscriptions = snapshot.getLong("c7_subscriptions");
                double score = getUserRelevance(followers, following, subscribers, subscriptions);
                Map<String, Object> upd = new HashMap<>();
                upd.put("c2_score", score);
                upd.put("c4_followers", followers);
                upd.put("c5_following", following);
                upd.put("c6_subscribers", subscribers);
                upd.put("c7_subscriptions", subscriptions);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");

                        //Add user to subscribed person's list
                        database.collection("subscribers").document(yourId)
                                .update("list", FieldValue.arrayUnion(myId));
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
    }

    private void sendPushNotification(boolean addToInbox, final String myId, final String posterId, String intentUrl,
                                      final String action, final String postType, final String message){
        String title;
        switch (getCount()){
            case 0:
            case 1:
                title = String.format("%s %s your %s", UserNetwork.getProfile().getA2_username(), action, postType);
                break;
            default:
                title = String.format("%s and %d others %s your %s", UserNetwork.getProfile().getA2_username(), getCount(), action, postType);
                break;
        }
        final Notification notification = new Notification(action, title, message, postType, intentUrl, UserNetwork.getProfile().getB3_dpTmUrl(), posterId, myId);
        database.collection("notifications").whereEqualTo("intentUrl", intentUrl)
                .whereEqualTo("action", action).whereEqualTo("type", postType).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(!task.getResult().isEmpty()){
                    for(DocumentSnapshot snapshot: task.getResult().getDocuments()){
                        snapshot.getReference().delete();
                    }
                }
                database.collection("notifications").add(notification);
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dbReference = db.getReference();
                dbReference.child("notifications").push().setValue(notification);

            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: Failed right here");
            }
        });
        //if addToInbox is true, then send to user's inbox;
    }

    private void sendNotification(String myId, String userId){
        //sends notification to tipster
        String title = UserNetwork.getProfile().getA2_username() + " followed you";
        String message = "view profile";
        Notification notification = new Notification("followed", title, message, "following",
                myId, UserNetwork.getProfile().getB2_dpUrl(), userId, myId);
        database.collection("notifications").whereEqualTo("sendTo", userId)
           .whereEqualTo("type", "following").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult() !=null && !task.getResult().isEmpty()){
                    if(task.getResult().getDocuments().size() > 4){
                        int c = 0;
                        for(DocumentSnapshot snapshot: task.getResult().getDocuments()){
                            c++;
                            if(c<5)
                                continue;
                            snapshot.getReference().delete();
                        }
                    }
                }
                database.collection("notifications").add(notification);
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference dbReference = db.getReference();
                dbReference.child("notifications").push().setValue(notification);

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: Failed right here");
                    }
                });
    }

    public int getCount() {
        return (int) count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void followMember(View v, String myId, String yourId){
        final DocumentReference postPath = database.collection("profiles").document(myId);
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(postPath);
                //Check if post exist first
                if(!snapshot.exists()){
                    Log.i(TAG, "apply: snapshot is empty");
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long followers = snapshot.getLong("c4_followers");
                long following = snapshot.getLong("c5_following") + 1;
                long subscribers = snapshot.getLong("c6_subscribers");
                long subscriptions = snapshot.getLong("c7_subscriptions");
                double score = getUserRelevance(followers, following, subscribers, subscriptions);
                Map<String, Object> upd = new HashMap<>();
                upd.put("c2_score", score);
                upd.put("c5_following", following);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                        Snackbar.make(v, "Followed", Snackbar.LENGTH_SHORT).show();

                        //Add the person's id to user's following list
                        database.collection("followings").document(myId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot dS = task.getResult();
                                if(dS==null||!dS.exists()){
                                    ArrayList<String> list = new ArrayList<>();
                                    list.add(yourId);
                                    Map<String, Object> upd = new HashMap<>();
                                    upd.put("list", list);
                                    database.collection("followings").document(myId).set(upd);
                                }
                                else
                                    database.collection("followings").document(myId)
                                            .update("list", FieldValue.arrayUnion(yourId));
                            }
                        });

                        //Add user to person's follower list
                        database.collection("followers").document(yourId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                DocumentSnapshot dS = task.getResult();
                                if(dS==null||!dS.exists()){
                                    ArrayList<String> list = new ArrayList<>();
                                    list.add(myId);
                                    Map<String, Object> upd = new HashMap<>();
                                    upd.put("list", list);
                                    database.collection("followers").document(yourId).set(upd);
                                }
                                else
                                    database.collection("followers").document(yourId)
                                            .update("list", FieldValue.arrayUnion(myId));
                            }
                        });
                        updateMember();
                        sendNotification(myId, yourId);
                        unrecommend(myId, yourId);
                    }
                    private void updateMember() {
                        final DocumentReference postPath =  database.collection("profiles").document(yourId);
                        database.runTransaction(new Transaction.Function<Void>() {
                            @Override
                            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot snapshot = transaction.get(postPath);
                                //Check if post exist first
                                if(!snapshot.exists()){
                                    return null;
                                }
                                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                                long followers = snapshot.getLong("c4_followers") + 1;
                                long following = snapshot.getLong("c5_following");
                                long subscribers = snapshot.getLong("c6_subscribers");
                                long subscriptions = snapshot.getLong("c7_subscriptions");
                                double score = getUserRelevance(followers, following, subscribers, subscriptions);
                                Map<String, Object> upd = new HashMap<>();
                                upd.put("c2_score", score);
                                upd.put("c4_followers", followers);
                                transaction.update(postPath, upd);
                                return null;
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void unfollowMember(ImageView v, String myId, String yourId){
        final DocumentReference postPath =  database.collection("profiles").document(myId);
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(postPath);
                //Check if post exist first
                if(!snapshot.exists()){
                    return null;
                }

                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                long followers = snapshot.getLong("c4_followers");
                long following = snapshot.getLong("c5_following") - 1;
                long subscribers = snapshot.getLong("c6_subscribers");
                long subscriptions = snapshot.getLong("c7_subscriptions");
                double score = getUserRelevance(followers, following, subscribers, subscriptions);
                Map<String, Object> upd = new HashMap<>();
                upd.put("c2_score", score);
                upd.put("c5_following", following);
                transaction.update(postPath, upd);
                return null;
            }
        })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Transaction success!");
                        Snackbar.make(v, "Unfollowed", Snackbar.LENGTH_SHORT).show();

                        //Add followed id to current user's following list
                        database.collection("followings").document(myId)
                                .update("list", FieldValue.arrayRemove(yourId));

                        //Add current user to followed person's list
                        database.collection("followers").document(yourId)
                                .update("list", FieldValue.arrayRemove(myId));
                        updateMember();
                    }

                    private void updateMember() {
                        final DocumentReference postPath =  database.collection("profiles").document(yourId);
                        database.runTransaction(new Transaction.Function<Void>() {
                            @Override
                            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot snapshot = transaction.get(postPath);
                                //Check if post exist first
                                if(!snapshot.exists()){
                                    return null;
                                }
                                //retrieve likes, likesCount, dislikes, dislikesCount, and repostCount from snapshot
                                long followers = snapshot.getLong("c4_followers")-1;
                                long following = snapshot.getLong("c5_following");
                                long subscribers = snapshot.getLong("c6_subscribers");
                                long subscriptions = snapshot.getLong("c7_subscriptions");
                                double score = getUserRelevance(followers, following, subscribers, subscriptions);
                                Map<String, Object> upd = new HashMap<>();
                                upd.put("c2_score", score);
                                upd.put("c4_followers", subscriptions);
                                transaction.update(postPath, upd);
                                return null;
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                        Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}