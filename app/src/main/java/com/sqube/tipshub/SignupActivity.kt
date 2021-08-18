package com.sqube.tipshub

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import com.hbb20.CountryCodePicker
import com.sqube.tipshub.databinding.ActivitySignup2Binding
import com.sqube.tipshub.databinding.ActivitySignupBinding
import com.theartofdev.edmodo.cropper.CropImage
import models.Profile
import utils.FirebaseUtil.firebaseAuthentication
import utils.FirebaseUtil.firebaseFirestore
import utils.FirebaseUtil.firebaseStorage
import utils.Reusable.Companion.getNetworkAvailability
import utils.Reusable.Companion.grabImage
import utils.Reusable.Companion.updateAlgoliaIndex
import java.util.*

class SignupActivity : AppCompatActivity(), View.OnClickListener {
    private var _binding :ActivitySignupBinding? = null
    private val binding get()= _binding!!
    private var _bindingComplete: ActivitySignup2Binding? = null
    private val bindingComplete get() = _bindingComplete!!
    private var progressDialog: ProgressDialog? = null
    private var user: FirebaseUser? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var filePath: Uri? = null
    private var openMainActivity = false
    private var editor: SharedPreferences.Editor? = null
    private var userId: String? = null
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var confirmEmail: String? = null
    private var password: String? = null
    private var provider: String? = null
    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignupBinding.inflate(layoutInflater)
        _bindingComplete = ActivitySignup2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        val callingIntent = intent
        openMainActivity = callingIntent.getBooleanExtra("openMainActivity", false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle("Sign Up")
        binding.gSignIn.setOnClickListener(this)
        binding.btnSignup.setOnClickListener(this)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        editor = prefs.edit()
        progressDialog = ProgressDialog(this)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this@SignupActivity, gso)
    }

    override fun onClick(v: View) {
        when (v) {
            bindingComplete.imgDp -> grabImage()
            binding.btnSignup -> {
                if (!getNetworkAvailability(applicationContext)) {
                    Snackbar.make(binding.edtEmail, "No Internet connection", Snackbar.LENGTH_SHORT).show()
                    return
                }
                registerUserWithEmail()
            }
            binding.gSignIn -> {
                binding.btnSignup.isEnabled = false
                binding.edtPassword.isEnabled = false
                binding.edtEmail.isEnabled = false
                binding.edtFirstName.isEnabled = false
                binding.edtLastName.isEnabled = false
                binding.edtPassword.isEnabled = false
                binding.edtEmailAgain.isEnabled = false
                binding.prgLogin.visibility = View.VISIBLE
                signInWithGoogle()
            }
        }
    }

    private fun signInWithGoogle() {
        val intent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(intent, RC_SIGN_IN)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.signup_menu, menu)
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_contact -> startActivity(Intent(this@SignupActivity, ContactActivity::class.java))
            R.id.nav_guide -> startActivity(Intent(this@SignupActivity, GuideActivity::class.java))
            else -> {
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
                finish()
            }
        }
        return true
    }

    private fun grabImage() {
        CropImage.activity()
                .setFixAspectRatio(true)
                .start(this)
    }

    private fun uploadImage() {
        progressDialog!!.setTitle("Uploading...")
        progressDialog!!.show()
        firebaseStorage!!.reference.child("profile_images").child(userId!!).putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri: Uri ->
                                val url = uri.toString()
                                firebaseFirestore!!.collection("profiles").document(userId!!).update("b2_dpUrl", url)
                                progressDialog!!.dismiss()
                                Toast.makeText(this@SignupActivity, "Image uploaded", Toast.LENGTH_SHORT).show()
                                bindingComplete.imgDp.setImageURI(filePath)
                            }
                }
                .addOnFailureListener { e ->
                    progressDialog!!.dismiss()
                    Toast.makeText(this@SignupActivity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog!!.setMessage(String.format("${progress.toInt()}% completed"))
                }
    }

    private fun enableViews(message: String) {
        binding.prgLogin.visibility = View.GONE
        binding.btnSignup.isEnabled = true
        binding.edtPassword.isEnabled = true
        binding.edtEmail.isEnabled = true
        binding.edtFirstName.isEnabled = true
        binding.edtLastName.isEnabled = true
        binding.edtPassword.isEnabled = true
        binding.edtEmailAgain.isEnabled = true
        Snackbar.make(binding.edtEmail, message, Snackbar.LENGTH_SHORT).show()
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

    private fun authWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuthentication!!.signInWithCredential(credential).addOnCompleteListener { task: Task<AuthResult?> ->
            if (task.isSuccessful) {
                user = firebaseAuthentication!!.currentUser
                userId = user!!.uid
                firebaseFirestore!!.collection("profiles").document(userId!!).get()
                        .addOnCompleteListener { task1: Task<DocumentSnapshot> ->
                            Log.i("onComplete", "get() successful " + task1.result.toString())
                            if (task1.isSuccessful) {
                                if (!task1.result.exists()) {
                                    val displayName = user!!.displayName
                                    val names = displayName!!.split(" ").toTypedArray()
                                    firstName = names[0]
                                    lastName = if (names.size > 1) names[1] else ""
                                    email = user!!.email
                                    provider = "google.com"
                                    firebaseFirestore!!.collection("profiles").document(userId!!)
                                            .set(Profile(firstName, lastName, email, provider))
                                    grabImage(user!!.photoUrl.toString())
                                    completeProfile()
                                } else {
                                    finish()
                                    if (openMainActivity) startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                                }
                            }
                        }
            } else {
                val message = "Login unsuccessful. " + task.exception!!.message
                enableViews(message)
                firebaseAuthentication!!.signOut()
                mGoogleSignInClient!!.signOut()
            }
        }
    }

    private fun registerUserWithEmail() {
        firstName = binding.edtFirstName.text.toString()
        lastName = binding.edtLastName.text.toString()
        email = binding.edtEmail.text.toString()
        confirmEmail = binding.edtEmailAgain.text.toString()
        password = binding.edtPassword.text.toString()
        if (TextUtils.isEmpty(firstName)) {
            binding.edtFirstName.error = "Enter first name"
            return
        }
        if (TextUtils.isEmpty(lastName)) {
            binding.edtLastName.error = "Enter last name"
            return
        }
        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.error = "Enter email"
            return
        }
        if (TextUtils.isEmpty(confirmEmail)) {
            binding.edtEmail.error = "Confirm email"
            return
        }
        if (email != confirmEmail) {
            binding.edtEmail.error = "Email doesn't match"
            return
        }
        if (TextUtils.isEmpty(password)) {
            binding.edtPassword.error = "Enter password"
            return
        }
        if (password!!.length < 5) {
            binding.edtPassword.error = "password too small"
            return
        }
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Registering...")
        progressDialog!!.show()
        firebaseAuthentication!!.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                    progressDialog!!.dismiss()
                    if (task.isSuccessful) {
                        editor!!.putString("PASSWORD", password)
                        editor!!.putString("EMAIL", email)
                        editor!!.apply()
                        Snackbar.make(binding.edtEmail, "Registration successful.", Snackbar.LENGTH_SHORT).show()
                        Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT).show()
                        user = firebaseAuthentication!!.currentUser
                        userId = user!!.uid
                        provider = "email"
                        firebaseFirestore!!.collection("profiles").document(userId!!)
                                .set(Profile(firstName, lastName, email, provider))
                        completeProfile()
                    } else Snackbar.make(binding.edtEmail, "Registration failed. Check your details.", Snackbar.LENGTH_LONG).show()
                }
    }

    private fun completeProfile() {
        val builder = AlertDialog.Builder(this@SignupActivity)
        builder.setView(bindingComplete.root)
        val dialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()

        //Initialize variables
        val numberValid = BooleanArray(1)
        bindingComplete.imgDp.setOnClickListener(this)
        val ccp = dialog.findViewById<CountryCodePicker>(R.id.ccp)
        ccp!!.registerCarrierNumberEditText(bindingComplete.edtPhone)
        if (user!!.photoUrl == null) Glide.with(this).load(R.drawable.dummy).into(bindingComplete.imgDp)
        else Glide.with(this).load(user!!.photoUrl).into(bindingComplete.imgDp)
        ccp.setPhoneNumberValidityChangeListener { isValidNumber: Boolean -> numberValid[0] = isValidNumber }
        bindingComplete.btnSave.setOnClickListener {
            val username = bindingComplete.edtUsername.text.toString().trim()
            val phone = ccp.fullNumber
            val country = ccp.selectedCountryName
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
            val finalGender = gender
            firebaseFirestore!!.collection("profiles")
                    .whereEqualTo("a2_username", username).limit(1).get()
                    .addOnCompleteListener { task: Task<QuerySnapshot?> ->
                        if (task.result == null || !task.result!!.isEmpty) {
                            bindingComplete.edtUsername.error = "Username already exist"
                            Toast.makeText(this@SignupActivity, "Username already exist. Try another one", Toast.LENGTH_SHORT).show()
                            return@addOnCompleteListener
                        }

                        //Map new user datails, and ready to save to db
                        val url: MutableMap<String, String?> = HashMap()
                        url["a2_username"] = username
                        url["a4_gender"] = finalGender
                        url["b0_country"] = country
                        url["b1_phone"] = phone

                        //set the new username to firebase auth user
                        val profileUpdate = UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .build()
                        user!!.updateProfile(profileUpdate)

                        //save username, phone number, and gender to database
                        firebaseFirestore!!.collection("profiles").document(userId!!)[url] = SetOptions.merge()
                        updateAlgoliaIndex(firstName, lastName, username, userId, 0, true) //add to Algolia index
                        dialog.cancel()
                        finish()
                        if (openMainActivity) startActivity(Intent(this@SignupActivity, MainActivity::class.java))
                        val intent = Intent(this@SignupActivity, AboutActivity::class.java)
                        intent.putExtra("showCongratsImage", true)
                        startActivity(intent)
                    }
        }
    }
}