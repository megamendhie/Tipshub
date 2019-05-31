package com.sqube.tipshub;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import models.ProfileShort;
import models.Subscription;
import models.UserNetwork;
import utils.Calculations;

public class SubscriptionActivity extends AppCompatActivity implements View.OnClickListener {
    private String userId, username, myId, myUsername, email, narration= "For 2 weeks", firstName, lastName;
    private FirebaseFirestore database;
    DatabaseReference dbRef;
    String countryCode, currencyCode, userDefaultCountry = "nigeria";
    private String TAG = "SubscriptionActivity";
    private String duration;
    private CircleImageView imgDp;
    private ProfileShort profile;
    private TextView txtUsername;
    private TextView txtPost, txtWon, txtAccuracy;
    private TextView txtAmount,txtAmount2, txtBenefitOne, txtBenefitTwo;
    private Button btnSubscribe, btnSubscribe2;
    String amt;
    int[] amount = {0,0,0,0};
    int value;
    boolean acceptAccount, acceptCard, acceptMpesa, accpetGhMobile, accpetUgMobile;
    final String[] currencySymbol = {"&#8358;", "&#36;", "&#8364;", "&#xa3;", "&#8373;", "KES ", "UGX ", "TZS ", "ZAR " };
    int cash=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        }

        //initialize views
        imgDp = findViewById(R.id.imgDp); imgDp.setOnClickListener(this);
        txtUsername = findViewById(R.id.txtUsername); txtUsername.setOnClickListener(this);
        txtPost = findViewById(R.id.txtPost);
        txtWon = findViewById(R.id.txtWon);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        txtAmount = findViewById(R.id.txtAmount);
        txtAmount2 = findViewById(R.id.txtAmount2);
        txtBenefitOne = findViewById(R.id.txtBenefitOne);
        txtBenefitTwo = findViewById(R.id.txtBenefitTwo);
        btnSubscribe = findViewById(R.id.btnSubscribe); btnSubscribe.setOnClickListener(this);
        btnSubscribe2 = findViewById(R.id.btnSubscribe2); btnSubscribe2.setOnClickListener(this);
        database = FirebaseFirestore.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference().child("SystemConfig").child("Subscription");
        dbRef.keepSynced(true);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        myId = user.getUid();
        myUsername = user.getDisplayName();
        userId = getIntent().getStringExtra("userId");

        firstName = UserNetwork.getProfile().getA0_firstName();
        lastName = UserNetwork.getProfile().getA1_lastName();
        email = UserNetwork.getProfile().getA3_email();
        displayViews();
    }

    private void displayViews(){
        database.collection("profiles").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(!documentSnapshot.exists())
                    return;
                btnSubscribe.setOnClickListener(SubscriptionActivity.this);
                profile = documentSnapshot.toObject(ProfileShort.class);
                username = profile.getA2_username();
                txtUsername.setText(profile.getA2_username());
                txtPost.setText(profile.getE6a_NOG() + " banker tips  • ");
                txtWon.setText(profile.getE6b_WG()+ " won");
                String a = String.format(Locale.ENGLISH,"%d",profile.getE6c_WGP()); // accuracy of baner
                txtAccuracy.setText("Banker Accuracy: " + a + "%");
                txtBenefitOne.setText("See all banker tips from "+ username);
                txtBenefitTwo.setText("Receive notification when "+ username +" posts banker tips");
                if(UserNetwork.getProfile()!=null && !UserNetwork.getProfile().getB0_country().isEmpty())
                    userDefaultCountry = UserNetwork.getProfile().getB0_country();
                currencyCode = getCurrency(userDefaultCountry);
                setPrice(currencyCode);
            }
        });
    }

    private void setPrice(final String currency){
        dbRef.child(currency).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("SubAct", "onDataChange: " + currency);
                ProgressBar pg = findViewById(R.id.prgPost);
                pg.setVisibility(View.GONE);
                amount[0] = dataSnapshot.child("sub1").getValue(Integer.class)!=null? dataSnapshot.child("sub1").getValue(int.class):0;
                amount[1] = dataSnapshot.child("sub1").getValue(Integer.class)!=null? dataSnapshot.child("sub1").getValue(int.class):0;
                amount[2] = dataSnapshot.child("sub1").getValue(Integer.class)!=null? dataSnapshot.child("sub1").getValue(int.class):0;
                amount[3] = dataSnapshot.child("sub1").getValue(Integer.class)!=null? dataSnapshot.child("sub1").getValue(int.class):0;

                //update amount tipster charges
                if(String.valueOf(profile.getD0_subAmount()).toLowerCase().equals("null"))
                    cash = amount[0];
                else
                    cash = amount[profile.getD0_subAmount()];
                String r = String.format(Locale.ENGLISH,"%s%d - 2 weeks", currencySymbol[value],cash);
                String r2 = String.format(Locale.ENGLISH,"%s%d - a month", currencySymbol[value],cash*2);
                txtAmount.setText(Html.fromHtml(r));
                txtAmount2.setText(Html.fromHtml(r2));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.txtUsername:
            case R.id.imgDp:
                Intent intent = new Intent(this, MemberProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                break;
            case R.id.btnSubscribe:
                duration="2 weeks";
                amt = String.format(Locale.ENGLISH,"%s%d", currencySymbol[value],cash);
                pay(cash);
                break;
            case R.id.btnSubscribe2:
                duration="1 month";
                amt = String.format(Locale.ENGLISH,"%s%d", currencySymbol[value],cash*2);
                pay(cash * 2);
                break;
        }
    }

    private String getCurrency(String country){
        acceptAccount = acceptCard = acceptMpesa = accpetGhMobile = accpetUgMobile = false;
        switch (country.toLowerCase()){
            case "nigeria":
                value=0;
                countryCode = "NG";
                acceptAccount = true;
                acceptCard = true;
                return "NGN";
            case "ghana":
                value=4;
                countryCode = "GH";
                accpetGhMobile = true;
                return "GHS";
            case "kenya":
                value = 5;
                countryCode = "KE";
                acceptMpesa =true;
                return "KES";
            case "uganda":
                value = 6;
                countryCode = "NG";
                accpetUgMobile = true;
                return "UGX";
            case "tanzania":
                value = 7;
                countryCode = "NG";
                acceptCard = true;
                return "TZS";
            case "south africa":
                value=8;
                countryCode = "NG";
                acceptCard = true;
                return "ZAR";
            case "united kingdom":
                value=3;
                countryCode = "NG";
                acceptCard = true;
                return "GBP";
            case "austria":
            case "belgium":
            case "cyprus":
            case "estonia":
            case "finland":
            case "france":
            case "germany":
            case "greece":
            case "ireland":
            case "italy":
            case "latvia":
            case "lithuania":
            case "luxembourg":
            case "malta":
            case "portugal":
            case "slovakia":
            case "slovenia":
            case "spain":
                value=2;
                countryCode = "NG";
                acceptCard = true;
                return "EUR";
            default:
                value=1;
                countryCode = "NG";
                acceptCard = true;
                return "USD";
        }
    }

    private void pay(int cash){
        Log.i("SubAct", "pay: ");
        String txRef = myId + UUID.randomUUID().toString();
        final String PUBLIC_KEY = "FLWPUBK-6135390cafde2808e6aaf6b9869bf5b9-X";
        final String ENCRYPTION_KEY = "9ed4313390654a2c61f4a2b3";
        new RavePayManager(SubscriptionActivity.this).setAmount(cash)
                .setCountry(countryCode)
                .setCurrency(currencyCode)
                .setEmail(email)
                .setfName(firstName)
                .setlName(lastName)
                .setNarration(narration)
                .setPublicKey(PUBLIC_KEY)
                .setEncryptionKey(ENCRYPTION_KEY)
                .setTxRef(txRef)
                        .acceptAccountPayments(acceptAccount)
                        .acceptCardPayments(acceptCard)
                        .acceptMpesaPayments(acceptMpesa)
                        .acceptGHMobileMoneyPayments(accpetGhMobile)
			            .acceptUgMobileMoneyPayments(accpetUgMobile)
                        .onStagingEnv(false)
                        .allowSaveCardFeature(true)
                .withTheme(R.style.DefaultTheme)
                .initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: fire");
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                Log.i(TAG, "onActivityResult: Success");
                updateDatabase();
                popUp1();
            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Log.i(TAG, "onActivityResult: "+ message);
                Snackbar.make(imgDp, "CANCELLED", Snackbar.LENGTH_SHORT).show();
            }
            else {
                Log.i(TAG, "onActivityResult: "+ message);
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle("FAILED")
                        .setCancelable(false)
                        .setMessage("Your subscribtion failed.\nTry again or contact us for help.")
                        .setNegativeButton("Try again", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //finish();
                            }
                        })
                        .setPositiveButton("Contact us", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startWhatsApp();
                            }
                        })
                        .show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
            Log.i(TAG, "onActivityResult: returned null data");
        }
    }

    public void popUp1(){
        //Display success dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle("SUCCESSFUL")
                .setCancelable(false)
                .setMessage("Your subscribtion to "+ username + " was successful.\nYou can subscribe to other tipsters you like \uD83D\uDE09 \uD83D\uDE09")
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    public void startWhatsApp(){
        String mssg = "Hello Tipshub";
        String toNumber = "2348132014755";
        PackageManager pkMgt = getPackageManager();
        Uri uri = Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+mssg);
        try {
            Intent whatsApp = new Intent(Intent.ACTION_VIEW);
            whatsApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            whatsApp.setData(uri);
            PackageInfo info = pkMgt.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            startActivity(whatsApp);
        }
        catch (PackageManager.NameNotFoundException e){
            Toast.makeText(this, "No whatsapp installed", Toast.LENGTH_LONG).show();
        }
    }

    private void updateDatabase(){
        Subscription sub = new Subscription(amt, myUsername, myId, userId, username, duration);
        Calculations calculations = new Calculations(SubscriptionActivity.this);
        database.collection("subscriptions").add(sub);
        calculations.increaseSubcriptions(myId);
        calculations.increaseSubcribers(userId);
        //send notification
    }
}
