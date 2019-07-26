package utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseUtil {
    private static FirebaseFirestore mFirebaseFirestore;
    private static FirebaseStorage mFirebaseStorage;
    private static FirebaseAuth mFirebaseAuthentication;

    public static FirebaseFirestore getFirebaseFirestore() {
        if(mFirebaseFirestore==null)
            setFirebaseFirestore();
        return mFirebaseFirestore;
    }

    private static void setFirebaseFirestore() {
        FirebaseUtil.mFirebaseFirestore = FirebaseFirestore.getInstance();
    }

    public static FirebaseStorage getFirebaseStorage() {
        if(mFirebaseStorage==null)
            setFirebaseStorage();
        return mFirebaseStorage;
    }

    private static void setFirebaseStorage() {
        FirebaseUtil.mFirebaseStorage = FirebaseStorage.getInstance();
    }

    public static FirebaseAuth getFirebaseAuthentication() {
        if(mFirebaseAuthentication==null)
            setFirebaseAuthentication();
        return mFirebaseAuthentication;
    }

    private static void setFirebaseAuthentication() {
        FirebaseUtil.mFirebaseAuthentication = FirebaseAuth.getInstance();
    }
}
