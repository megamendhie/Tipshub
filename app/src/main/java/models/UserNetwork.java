package models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public final class UserNetwork {
    private static ArrayList<String> followers;
    private static ArrayList<String> following;
    private static ArrayList<String> subscribers;
    private static ArrayList<String> subscribed;
    private static DocumentSnapshot profile;

    public static ArrayList<String> getFollowers() {
        return followers;
    }

    public static void setFollowers(ArrayList<String> mFollowers) {
        followers = followers;
    }

    public static ArrayList<String> getFollowing() {
        return following;
    }

    public static void setFollowing(ArrayList<String> mFollowing) {
        following = mFollowing;
    }

    public static ArrayList<String> getSubscribers() {
        return subscribers;
    }

    public static void setSubscribers(ArrayList<String> mSubscibers) {
        subscribers = mSubscibers;
    }

    public static ArrayList<String> getSubscribed() {
        return subscribed;
    }

    public static void setSubscribed(ArrayList<String> mSubscibed) {
        subscribed = mSubscibed;
    }

    public static ProfileMedium getProfile() {
        return profile.toObject(ProfileMedium.class);
    }

    public static void setProfile(DocumentSnapshot profile) {
        UserNetwork.profile = profile;
    }
}
