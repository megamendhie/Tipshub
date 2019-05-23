package models;

import com.google.firebase.firestore.DocumentSnapshot;

public final class UserNetwork {
    private static DocumentSnapshot followers;
    private static DocumentSnapshot following;
    private static DocumentSnapshot subscibers;
    private static DocumentSnapshot subscibed;
    private static ProfileMedium profile;
    public static DocumentSnapshot getFollowers() {
        return followers;
    }

    public static void setFollowers(DocumentSnapshot mFollowers) {
        followers = followers;
    }

    public static DocumentSnapshot getFollowing() {
        return following;
    }

    public static void setFollowing(DocumentSnapshot mFollowing) {
        following = mFollowing;
    }

    public static DocumentSnapshot getSubscibers() {
        return subscibers;
    }

    public static void setSubscibers(DocumentSnapshot subscibers) {
        UserNetwork.subscibers = subscibers;
    }

    public static DocumentSnapshot getSubscibed() {
        return subscibed;
    }

    public static void setSubscibed(DocumentSnapshot mSubscibed) {
        subscibed = mSubscibed;
    }

    public static ProfileMedium getProfile() {
        return profile;
    }

    public static void setProfile(ProfileMedium profile) {
        UserNetwork.profile = profile;
    }
}
