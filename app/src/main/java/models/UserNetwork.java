package models;

import java.util.ArrayList;

public final class UserNetwork {
    private static ArrayList<String> followers = new ArrayList<String>();
    private static ArrayList<String> following;
    private static ArrayList<String> subscribed;

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

    public static ArrayList<String> getSubscribed() {
        return subscribed;
    }

    public static void setSubscribed(ArrayList<String> mSubscibed) {
        subscribed = mSubscibed;
    }
}
