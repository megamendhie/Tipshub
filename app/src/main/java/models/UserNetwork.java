package models;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public final class UserNetwork {
    private static ArrayList<String> followers;
    private static ArrayList<String> following;
    private static ArrayList<String> subscibers;
    private static ArrayList<String> subscibed;
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

    public static ArrayList<String> getSubscibers() {
        return subscibers;
    }

    public static void setSubscibers(ArrayList<String> mSubscibers) {
        subscibers = mSubscibers;
    }

    public static ArrayList<String> getSubscibed() {
        return subscibed;
    }

    public static void setSubscibed(ArrayList<String> mSubscibed) {
        subscibed = mSubscibed;
    }

    public static ProfileMedium getProfile() {
        return profile.toObject(ProfileMedium.class);
    }

    public static void setProfile(DocumentSnapshot profile) {
        UserNetwork.profile = profile;
    }
}
