package com.sqube.tipshub

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.sqube.tipshub.databinding.ActivityLoginBinding
import com.sqube.tipshub.databinding.ActivitySignup2Binding
import com.theartofdev.edmodo.cropper.CropImage
import models.Profile
import models.ProfileMedium
import utils.FirebaseUtil.firebaseAuthentication
import utils.FirebaseUtil.firebaseFirestore
import utils.FirebaseUtil.firebaseStorage
import utils.IS_VERIFIED
import utils.PROFILE
import utils.PROFILES
import utils.Reusable.Companion.getNetworkAvailability
import utils.Reusable.Companion.grabImage
import utils.Reusable.Companion.updateAlgoliaIndex
import java.util.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() =  _binding!!
    private var _bindingComplete: ActivitySignup2Binding? = null
    private val bindingComplete get() = _bindingComplete!!
    private var user: FirebaseUser? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var progressDialog: ProgressDialog? = null
    private var userId: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var provider: String? = null
    private var filePath: Uri? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val gson = Gson()
    private var openMainActivity = false
    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        _bindingComplete = ActivitySignup2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        prefs= getSharedPreferences("${applicationContext.packageName}_preferences", MODE_PRIVATE)
        editor = prefs.edit()
        openMainActivity = intent.getBooleanExtra("openMainActivity", false)
        binding.btnLogin.setOnClickListener(this)
        binding.btnSignup.setOnClickListener(this)
        binding.gSignIn.setOnClickListener(this)
        progressDialog = ProgressDialog(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
        if (prefs.getString("PASSWORD", "X%p8kznAA1") != "X%p8kznAA1") {
            binding.edtEmail.setText(prefs.getString("EMAIL", "email@domain.com"))
            binding.edtPassword.setText(prefs.getString("PASSWORD", "X%p8kznAA1"))
        }
    }

    override fun onClick(v: View) {
        when (v) {
            bindingComplete.imgDp -> grabImage()
            binding.btnLogin -> {
                if (!getNetworkAvailability(applicationContext)) {
                    Snackbar.make(binding.btnLogin, "No Internet connection", Snackbar.LENGTH_SHORT).show()
                    return
                }
                signInWithEmail()
            }
            binding.btnSignup -> {
                val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                intent.putExtra("openMainActivity", openMainActivity)
                startActivity(intent)
                finish()
            }
            binding.gSignIn -> {
                binding.edtPassword.isEnabled = false
                binding.edtEmail.isEnabled = false
                binding.prgLogin.visibility = View.VISIBLE
                signInWithGoogle()
            }
        }
    }

    private fun enableViews(message: String) {
        binding.prgLogin.visibility = View.GONE
        binding.edtPassword.isEnabled = true
        binding.edtEmail.isEnabled = true
        Snackbar.make(binding.btnLogin, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                authWithGoogle(account)
                Log.i("LoginActivity", "onActivityResult: account Retrieved successfully")
            } catch (e: ApiException) {
                enableViews("Cannot login at this time")
                e.printStackTrace()
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                filePath = result.uri
                uploadImage()
                bindingComplete.imgDp.setImageURI(filePath)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.error
            }
        }
    }

    private fun signInWithGoogle() {
        val intent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun signInWithEmail() {
        email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.error = "Enter email"
            return
        }
        if (TextUtils.isEmpty(password)) {
            binding.edtPassword.error = "Enter password"
            return
        }
        binding.prgLogin.visibility = View.VISIBLE
        progressDialog!!.setTitle("Signing in...")
        progressDialog!!.show()
        firebaseAuthentication!!.signInWithEmailAndPassword(email!!, password)
                .addOnCompleteListener(this) { task ->
                    progressDialog!!.dismiss()
                    binding.prgLogin.visibility = View.GONE
                    if (task.isSuccessful) {
                        editor.putString("PASSWORD", binding.edtPassword.text.toString().trim())
                        editor.putString("EMAIL", binding.edtEmail.text.toString().trim())
                        editor.apply()
                        Snackbar.make(binding.btnLogin, "Login successful", Snackbar.LENGTH_SHORT).show()
                        user = firebaseAuthentication!!.currentUser
                        saveProfileToPref(user!!.uid, true)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog!!.dismiss()
                    enableViews("Login failed. " + e.message)
                    if (firebaseAuthentication?.currentUser != null) firebaseAuthentication!!.signOut()
                }
    }

    private fun saveProfileToPref(userId: String, closePage: Boolean) {
        firebaseFirestore!!.collection(PROFILES).document(userId).get()
                .addOnCompleteListener(this@LoginActivity) {
                    if (it.isSuccessful && it.result != null && it.result.exists()) {
                        val snapshot = it.result
                        val json = gson.toJson(snapshot.toObject(ProfileMedium::class.java))
                        editor.putBoolean(IS_VERIFIED, snapshot.toObject(ProfileMedium::class.java)!!.isC0_verified)
                        editor.putString(PROFILE, json)
                        editor.apply()
                        if(closePage){
                            finish()
                            if (openMainActivity) startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        }
                    }
                }
    }

    private fun authWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuthentication!!.signInWithCredential(credential)
                .addOnCompleteListener { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        user = firebaseAuthentication!!.currentUser
                        userId = user!!.uid
                        firebaseFirestore!!.collection(PROFILES).document(userId!!).get()
                                .addOnCompleteListener { task1: Task<DocumentSnapshot?> ->
                                    if (task1.isSuccessful) {
                                        if (task1.result == null || !task1.result!!.exists()) {
                                            val displayName: String? = user!!.displayName
                                            val names: Array<String> = displayName!!.split(" ").toTypedArray()
                                            firstName = names[0]
                                            lastName = if (names.size > 1) names[1] else ""
                                            email = user!!.email
                                            provider = "google.com"
                                            firebaseFirestore!!.collection("profiles").document(userId!!)
                                                    .set(Profile(firstName, lastName, email, provider))
                                            grabImage(user!!.photoUrl.toString())
                                            saveProfileToPref(userId!!, false)
                                            completeProfile()
                                        } else saveProfileToPref(userId!!, true)
                                    }
                                }
                    } else {
                        val message: String = "Login unsuccessful. " + task.exception!!.message
                        enableViews(message)
                        firebaseAuthentication!!.signOut()
                        mGoogleSignInClient!!.signOut()
                    }
                }
    }

    fun passwordReset(v: View?) {
        /*
        Build a dialogView for user to enter email
         */
        val builder = AlertDialog.Builder(this@LoginActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_reset_password, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.show()
        val edtPassResetEmail = dialog.findViewById<EditText>(R.id.edtEmail)
        val btnSendPassword = dialog.findViewById<Button>(R.id.btnSendPassword)
        val txtHeading = dialog.findViewById<TextView>(R.id.txtHeading)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.prgPasswordReset)
        edtPassResetEmail!!.setText(binding.edtEmail.text.toString().trim())
        btnSendPassword!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                if ((btnSendPassword.text.toString() == "CLOSE")) {
                    dialog.cancel()
                    return
                }
                progressBar!!.visibility = View.VISIBLE
                val email = edtPassResetEmail.text.toString().trim { it <= ' ' }
                firebaseAuthentication!!.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressBar.visibility = View.GONE
                        edtPassResetEmail.visibility = View.GONE
                        txtHeading!!.text = "Password link has been sent to your email"
                        btnSendPassword.text = "CLOSE"
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Error " + task.exception.toString(),
                                Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun completeProfile() {
        val builder = AlertDialog.Builder(this@LoginActivity)
        builder.setView(bindingComplete.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
        val numberValid = booleanArrayOf(false)

        bindingComplete.imgDp.setOnClickListener(this)
        bindingComplete.ccp.registerCarrierNumberEditText(bindingComplete.edtPhone)
        Glide.with(this).load(user!!.photoUrl).into(bindingComplete.imgDp)
        bindingComplete.ccp.setPhoneNumberValidityChangeListener{ isValidNumber: Boolean -> numberValid[0] = isValidNumber }
        bindingComplete.btnSave.setOnClickListener{
            val username = bindingComplete.edtUsername.text.toString().replace("\\s".toRegex(), "")
            val phone: String = bindingComplete.ccp.fullNumber
            val country: String = bindingComplete.ccp.selectedCountryName
            var gender = ""
            when (bindingComplete.rdbGroupGender.checkedRadioButtonId) {
                R.id.rdbMale -> gender = "male"
                R.id.rdbFemale -> gender = "female"
            }
            //verify fields meet requirement
            if (TextUtils.isEmpty(username)) {
                bindingComplete.edtUsername.error = "Enter username"
                bindingComplete.txtError.text = "Enter username"
                bindingComplete.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (username.length < 3) {
                bindingComplete.edtUsername.error = "Username too short"
                bindingComplete.txtError.text = "Username too short"
                bindingComplete.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(phone)) {
                bindingComplete.txtError.text = "Enter phone number"
                bindingComplete.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (!numberValid[0]) {
                bindingComplete.txtError.text = "Phone number is incorrect"
                bindingComplete.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(gender)) {
                bindingComplete.txtError.text = "Select gender (M/F)"
                bindingComplete.txtError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            bindingComplete.txtError.visibility = View.GONE
            val finalGender: String? = gender
            firebaseFirestore!!.collection("profiles")
                    .whereEqualTo("a2_username", username).limit(1).get()
                    .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                        if (task.result == null || !task.result!!.isEmpty) {
                            bindingComplete.edtUsername.error = "Username already exist"
                            Toast.makeText(this@LoginActivity, "Username already exist. Try another one", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }

                        //Map new user datails, and ready to save to db
                        val url: MutableMap<String, String?> = HashMap()
                        url["a2_username"] = username
                        url["a4_gender"] = finalGender
                        url["b0_country"] = country
                        url["b1_phone"] = phone

                        //set the new username to firebase auth user
                        val profileUpdate: UserProfileChangeRequest = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build()
                        firebaseAuthentication!!.currentUser!!.updateProfile(profileUpdate)

                        //save username, phone number, and gender to database
                        firebaseFirestore!!.collection("profiles").document((userId)!!).set(url, SetOptions.merge())
                        updateAlgoliaIndex(firstName, lastName, username, userId, 0, true) //add to Algolia index

                        firebaseFirestore!!.collection(PROFILES).document(userId!!).get()
                                .addOnCompleteListener(this@LoginActivity) {
                                    if (it.isSuccessful && it.result != null && it.result.exists()) {
                                        val snapshot = it.result
                                        val json = gson.toJson(snapshot.toObject(ProfileMedium::class.java))
                                        editor.putBoolean(IS_VERIFIED, snapshot.toObject(ProfileMedium::class.java)!!.isC0_verified)
                                        editor.putString(PROFILE, json)
                                        editor.apply()
                                        dialog.cancel()
                                        finish()
                                        if (openMainActivity) startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        val intent = Intent(this@LoginActivity, AboutActivity::class.java)
                                        intent.putExtra("showCongratsImage", true)
                                        startActivity(intent)
                                    }
                                }
                    }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun grabImage() {
        CropImage.activity()
                .setFixAspectRatio(true)
                .start(this)
    }

    private fun uploadImage() {
        progressDialog!!.setTitle("Uploading...")
        progressDialog!!.show()
        firebaseStorage!!.reference.child("profile_images").child((userId)!!).putFile((filePath)!!)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri: Uri ->
                                val url: String = uri.toString()
                                firebaseFirestore!!.collection("profiles").document((userId)!!).update("b2_dpUrl", url)
                                progressDialog!!.dismiss()
                                Toast.makeText(this@LoginActivity, "Image uploaded", Toast.LENGTH_SHORT).show()
                                bindingComplete.imgDp.setImageURI(filePath)
                            }
                }
                .addOnFailureListener { e ->
                    progressDialog!!.dismiss()
                    Toast.makeText(this@LoginActivity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    progressDialog!!.setMessage(String.format("${progress.toInt()}% completed"))
                }
    }
}