package utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseUtil {
    private var mFirebaseFirestore: FirebaseFirestore? = null
    private var mFirebaseStorage: FirebaseStorage? = null
    private var mFirebaseAuthentication: FirebaseAuth? = null
    @JvmStatic
    val firebaseFirestore: FirebaseFirestore?
        get() {
            if (mFirebaseFirestore == null) setFirebaseFirestore()
            return mFirebaseFirestore
        }

    private fun setFirebaseFirestore() {
        mFirebaseFirestore = FirebaseFirestore.getInstance()
    }

    @JvmStatic
    val firebaseStorage: FirebaseStorage?
        get() {
            if (mFirebaseStorage == null) setFirebaseStorage()
            return mFirebaseStorage
        }

    private fun setFirebaseStorage() {
        mFirebaseStorage = FirebaseStorage.getInstance()
    }

    @JvmStatic
    val firebaseAuthentication: FirebaseAuth?
        get() {
            if (mFirebaseAuthentication == null) setFirebaseAuthentication()
            return mFirebaseAuthentication
        }

    private fun setFirebaseAuthentication() {
        mFirebaseAuthentication = FirebaseAuth.getInstance()
    }

    @JvmStatic
    val storageReference: StorageReference
        get() = firebaseStorage!!.reference.child("profile_images")
}