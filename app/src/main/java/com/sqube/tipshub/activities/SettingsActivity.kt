package com.sqube.tipshub.activities

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.sqube.tipshub.R
import com.sqube.tipshub.databinding.ActivitySettingsBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.sqube.tipshub.models.Profile
import com.sqube.tipshub.utils.FirebaseUtil.firebaseFirestore
import com.sqube.tipshub.utils.Reusable.Companion.getPlaceholderImage
import com.sqube.tipshub.utils.Reusable.Companion.updateAlgoliaIndex
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private var _binding: ActivitySettingsBinding? = null
    private val binding get() = _binding!!
    private var profile: Profile? = null
    private var progressDialog: ProgressDialog? = null
    private var dbRef: DatabaseReference? = null
    private var userId: String? = null
    private var filePath: Uri? = null
    private val amount = intArrayOf(0, 0, 0, 0)
    private var numberValid = false
    private var allowChat = false
    private var currency: String? = null
    private var currencyRef: String? = null
    private val currencySymbol = arrayOf("&#8358;", "&#36;", "&#8364;", "&#xa3;", "&#8373;", "KES ", "UGX ", "TZS ",
            "ZAR ", "ZMW ", "RWF ", "XAF ", "XOF ")

    private fun setCurrency(country: String): String {
        return when (country.toLowerCase()) {
            "nigeria" -> {
                currency = currencySymbol[0]
                "NGN"
            }
            "ghana" -> {
                currency = currencySymbol[4]
                "GHS"
            }
            "kenya" -> {
                currency = currencySymbol[5]
                "KES"
            }
            "uganda" -> {
                currency = currencySymbol[6]
                "UGX"
            }
            "tanzania" -> {
                currency = currencySymbol[7]
                "TZS"
            }
            "south africa" -> {
                currency = currencySymbol[8]
                "ZAR"
            }
            "zambia" -> {
                currency = currencySymbol[9]
                "ZMW"
            }
            "rwanda" -> {
                currency = currencySymbol[10]
                "RWF"
            }
            "cameroon" -> {
                currency = currencySymbol[11]
                "XAF"
            }
            "mali", "ivory coast", "senegal" -> {
                currency = currencySymbol[12]
                "XOF"
            }
            "northern ireland", "wales", "england", "scotland", "united kingdom" -> {
                currency = currencySymbol[3]
                "GBP"
            }
            "austria", "belgium", "cyprus", "estonia", "finland", "france", "germany", "greece", "ireland", "italy", "latvia", "lithuania", "luxembourg", "malta", "portugal", "slovakia", "slovenia", "spain" -> {
                currency = currencySymbol[2]
                "EUR"
            }
            else -> {
                currency = currencySymbol[1]
                "USD"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.imgDp.setOnClickListener { grabImage() }

        binding.ccp.registerCarrierNumberEditText(binding.edtPhone)
        binding.ccp.setPhoneNumberValidityChangeListener { isValidNumber: Boolean -> numberValid = isValidNumber }

        progressDialog = ProgressDialog(this)
        val user = FirebaseAuth.getInstance().currentUser
        userId = user!!.uid
        dbRef = FirebaseDatabase.getInstance().reference.child("SystemConfig").child("Subscription")
        dbRef!!.keepSynced(true)
        updateView()
    }

    private fun updateView() {
        firebaseFirestore!!.collection("profiles").document(userId!!).get()
                .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    if (!documentSnapshot.exists()) return@addOnSuccessListener
                    profile = documentSnapshot.toObject(Profile::class.java)
                    binding.edtFirstName.setText(profile?.a0_firstName)
                    binding.edtLastName.setText(profile?.a1_lastName)
                    binding.edtUsername.setText(profile?.a2_username)
                    binding.edtEmail.setText(profile?.a3_email)
                    binding.edtBio.setText(profile?.a5_bio)
                    binding.edtBankDetails.setText(profile?.a9_bank)
                    binding.ccp.fullNumber = profile?.b1_phone

                    //set Display picture
                    Glide.with(applicationContext).load(profile!!.b2_dpUrl).placeholder(R.drawable.dummy)
                            .error(getPlaceholderImage(userId!![0])).into(binding.imgDp)
                    when (profile!!.a4_gender) {
                        "male" -> binding.rdbMale.toggle()
                        "female" -> binding.rdbFemale.toggle()
                    }
                    if (profile!!.isD5_allowChat) binding.rdbYes.toggle() else binding.rdbNo.toggle()
                    when (profile!!.d0_subAmount.toString()) {
                        "1" -> binding.rdbSub1.toggle()
                        "2" -> binding.rdbSub2.toggle()
                        "3" -> binding.rdbSub3.toggle()
                        else -> binding.rdbSub0.toggle()
                    }
                    currencyRef = setCurrency(profile!!.b0_country)
                    dbRef!!.child(currencyRef!!).addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                binding.rdbSub0.text = "check back later"
                                binding.rdbSub1.text = "check back later"
                                binding.rdbSub2.text = "check back later"
                                binding.rdbSub3.text = "check back later"
                            }
                            else {
                                amount[0] = dataSnapshot.child("sub1").getValue(Int::class.java) ?: 0
                                amount[1] = dataSnapshot.child("sub2").getValue(Int::class.java) ?: 0
                                amount[2] = dataSnapshot.child("sub3").getValue(Int::class.java) ?: 0
                                amount[3] = dataSnapshot.child("sub4").getValue(Int::class.java) ?: 0
                                val r0 = String.format(Locale.ENGLISH, "%s%d", currency, amount[0])
                                val r1 = String.format(Locale.ENGLISH, "%s%d", currency, amount[1])
                                val r2 = String.format(Locale.ENGLISH, "%s%d", currency, amount[2])
                                val r3 = String.format(Locale.ENGLISH, "%s%d", currency, amount[3])
                                binding.rdbSub0.text = Html.fromHtml(r0)
                                binding.rdbSub1.text = Html.fromHtml(r1)
                                binding.rdbSub2.text = Html.fromHtml(r2)
                                binding.rdbSub3.text = Html.fromHtml(r3)
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_save) {
            save()
        } else {
            finish()
        }
        return true
    }

    private fun save() {
        val firstName = binding.edtFirstName.text.toString()
        val lastName = binding.edtLastName.text.toString()
        val bio = binding.edtBio.text.toString()
        val account = binding.edtBankDetails.text.toString()
        val phone = binding.ccp.fullNumber
        val country = binding.ccp.selectedCountryName
        val sub: Int = when (binding.rdbGroupSub.checkedRadioButtonId) {
            R.id.rdbSub1 -> 1
            R.id.rdbSub2 -> 2
            R.id.rdbSub3 -> 3
            else -> 0
        }
        when (binding.rdbGroupChat.checkedRadioButtonId) {
            R.id.rdbYes -> allowChat = true
            R.id.rdbNo -> allowChat = false
        }

        //verify fields meet requirement
        if (TextUtils.isEmpty(firstName)) {
            binding.edtFirstName.error = "Enter name"
            return
        }
        if (firstName.length < 3) {
            binding.edtFirstName.error = "Name too short"
            return
        }
        if (TextUtils.isEmpty(binding.edtPhone.text.toString())) {
            binding.edtPhone.error = "Enter phone number"
            return
        }
        if (!numberValid) {
            binding.edtPhone.error = "Invalid phone number"
            return
        }

        //Map new user datails, and ready to save to db
        val updatedObject: MutableMap<String, Any> = HashMap()
        updatedObject["a0_firstName"] = firstName
        updatedObject["a1_lastName"] = lastName
        updatedObject["a5_bio"] = bio
        updatedObject["a9_bank"] = account
        updatedObject["b0_country"] = country
        updatedObject["b1_phone"] = phone
        updatedObject["d0_subAmount"] = sub
        updatedObject["d5_allowChat"] = allowChat
        val builder = AlertDialog.Builder(this@SettingsActivity, R.style.CustomMaterialAlertDialog)
        builder.setMessage("Save changes?")
                .setPositiveButton("Yes") { _: DialogInterface?, _: Int ->
                    //update profile with edited user information
                    firebaseFirestore!!.collection("profiles").document(userId!!).set(updatedObject, SetOptions.merge())
                            .addOnCompleteListener(this@SettingsActivity) { task: Task<Void?> ->
                                if (task.isSuccessful) {
                                    Snackbar.make(binding.edtBio, "Saved", Snackbar.LENGTH_SHORT).show()
                                    if (profile!!.a0_firstName != firstName || profile!!.a1_lastName != lastName) updateAlgoliaIndex(firstName, lastName, profile!!.a2_username,
                                            userId, profile!!.c2_score, false)
                                    updateView()
                                } else Snackbar.make(binding.edtBio, "Failed to save", Snackbar.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> }
                .show()
    }

    private fun grabImage() {
        CropImage.activity()
                .setFixAspectRatio(true)
                .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                filePath = result.uri
                uploadImage()
                binding.imgDp.setImageURI(filePath)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.error
            }
        }
    }

    private fun uploadImage() {
        progressDialog!!.setTitle("Uploading...")
        progressDialog!!.show()
        val storage = FirebaseStorage.getInstance()
        storage.reference.child("profile_images").child(userId!!).putFile(filePath!!)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri: Uri ->
                                val url = uri.toString()
                                firebaseFirestore!!.collection("profiles")
                                        .document(userId!!).update("b2_dpUrl", url)
                                progressDialog!!.dismiss()
                                Toast.makeText(this@SettingsActivity, "Image uploaded", Toast.LENGTH_SHORT).show()
                                binding.imgDp.setImageURI(filePath)
                            }
                }
                .addOnFailureListener { e: Exception ->
                    progressDialog!!.dismiss()
                    Toast.makeText(this@SettingsActivity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                    progressDialog!!.setMessage(String.format("${progress.toInt()}% completed"))
                }
    }
}