package com.sqube.tipshub;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RaveUiManager;
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

import adapters.SubscriberAdapter;
import adapters.SubscriptionAdapter;
import adapters.TransactionAdapter;
import de.hdodenhof.circleimageview.CircleImageView;
import models.Notification;
import models.ProfileMedium;
import models.Transaction;
import utils.Calculations;
import utils.FirebaseUtil;

import static com.sqube.tipshub.NgSubActivity.NG_SUB_ACTIVITY;
import static utils.Calculations.NOTIFICATIONS;
import static utils.Calculations.PROFILES;
import static utils.Calculations.SUBSCRIPTIONS;
import static utils.Calculations.TRANSACTIONS;
import static utils.Reusable.getPlaceholderImage;

public class AccountActivity extends AppCompatActivity {
    private static final String TAG = "AccActivity";
    private SubscriberAdapter subscriberAdapter;
    private SubscriptionAdapter subscriptionAdapter;
    private TransactionAdapter transactionAdapter;
    private String userId;
    private String username;
    private String email;
    private String firstName, lastName;
    private String currency;
    private CircleImageView imgDp;
    private TextView txtWelcome;
    private TextView txtBalWallet, txtBalSubs;
    private int value, amount;
    private long bal_wallet;
    private long bal_subs;
    private String selected;
    private String method;
    private boolean acceptAccount, acceptCard, acceptMpesa, acceptGhMobile, acceptUgMobile, acceptZmMobile,
            acceptRwfMobile, acceptBankTransfer, acceptFracMobile;
    final String[] currencySymbol = {"&#8358;", "&#36;", "&#8364;", "&#xa3;", "&#8373;", "KES ", "UGX ", "TZS ",
            "ZAR ", "ZMW ", "RWF ", "XAF ", "XOF "};

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView txtDisplay1 = findViewById(R.id.txtDipsplay1);
        TextView txtDisplay2 = findViewById(R.id.txtDipsplay2);
        TextView txtDisplay3 = findViewById(R.id.txtDipsplay3);
        RecyclerView listSubscribers = findViewById(R.id.listSubscribers);
        RecyclerView listSubscriptions = findViewById(R.id.listSubscriptions);
        RecyclerView listTransactions = findViewById(R.id.listTransactions);
        imgDp = findViewById(R.id.imgDp);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtBalWallet = findViewById(R.id.txtBalWallet);
        txtBalSubs = findViewById(R.id.txtBalSubs);

        listSubscribers.setLayoutManager(new LinearLayoutManager(this));
        listSubscriptions.setLayoutManager(new LinearLayoutManager(this));
        listTransactions.setLayoutManager(new LinearLayoutManager(this));
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();
        username = user.getDisplayName();
        email = user.getEmail();

        Query querySubscribers = database.collection(SUBSCRIPTIONS).orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("subToId", userId);
        Query querySubscriptions = database.collection(SUBSCRIPTIONS).orderBy("timestamp", Query.Direction.DESCENDING)
                .whereEqualTo("subFromId", userId);
        Query queryTransactions = FirebaseUtil.getFirebaseFirestore().collection(TRANSACTIONS)
                .orderBy("time", Query.Direction.DESCENDING).whereArrayContains("userIds", userId).limit(20);


        querySubscribers.get().addOnCompleteListener(task -> {
            if(task.getResult()==null || task.getResult().isEmpty())
                txtDisplay1.setVisibility(View.VISIBLE);

        });
        querySubscriptions.get().addOnCompleteListener(task -> {
            if(task.getResult()==null || task.getResult().isEmpty())
                txtDisplay2.setVisibility(View.VISIBLE);

        });
        queryTransactions.get().addOnCompleteListener(task -> {
            if(task.getResult()==null || task.getResult().isEmpty())
                txtDisplay3.setVisibility(View.VISIBLE);

        });

        subscriberAdapter = new SubscriberAdapter(querySubscribers);
        subscriptionAdapter = new SubscriptionAdapter(querySubscriptions);
        transactionAdapter = new TransactionAdapter(queryTransactions);

        listSubscribers.setAdapter(subscriberAdapter);
        listSubscriptions.setAdapter(subscriptionAdapter);
        listTransactions.setAdapter(transactionAdapter);
        Log.i(TAG, "subscriberAdapter: started listening");
        subscriberAdapter.startListening();
        Log.i(TAG, "subscriptionAdapter: started listening");
        subscriptionAdapter.startListening();
        Log.i(TAG, "transactionAdapter: started listening");
        transactionAdapter.startListening();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        FirebaseUtil.getFirebaseFirestore().collection("profiles").document(userId)
                .addSnapshotListener(AccountActivity.this, (snapshot, error) -> {
                    if(snapshot==null||!snapshot.exists())
                        return;
                    ProfileMedium profile = snapshot.toObject(ProfileMedium.class);
                    currency = getCurrency(profile.getB0_country());
                    firstName = profile.getA0_firstName();
                    lastName = profile.getA1_lastName();


                    //set Display picture
                    Glide.with(AccountActivity.this)
                            .load(profile.getB2_dpUrl())
                            .placeholder(R.drawable.dummy)
                            .error(getPlaceholderImage(userId.charAt(0)))
                            .into(imgDp);

                    txtWelcome.setText(String.format("Welcome %s", firstName));

                    bal_wallet = profile.getD6_balance_wallet();
                    bal_subs = profile.getD7_balance_sub();
                    txtBalWallet.setText(Html.fromHtml(String.format("%s%s", currencySymbol[value], getFormattedAmount(bal_wallet))));
                    txtBalSubs.setText(Html.fromHtml(String.format("%s%s", currencySymbol[value], getFormattedAmount(bal_subs))));
                });
    }

    private String getFormattedAmount(long amount){
        return NumberFormat.getIntegerInstance(Locale.US).format(amount);
    }

    public void displayDepositDialog(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
        LayoutInflater inflater = LayoutInflater.from(AccountActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_deposit, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        TextView txtCurrency = dialog.findViewById(R.id.txtCurrency);
        txtCurrency.setText(Html.fromHtml(currencySymbol[value]));
        MaterialButton btnDeposit = dialog.findViewById(R.id.btnDeposit);
        EditText edtAmount = dialog.findViewById(R.id.edtAmount);
        btnDeposit.setOnClickListener(view1 -> {
            String amountInString = edtAmount.getText().toString().trim();
            int amount = amountInString.isEmpty()? 0: Integer.parseInt(amountInString);
            if(amount<=0){
                edtAmount.setError("Enter amount");
                return;
            }
            this.amount = amount;
            dialog.cancel();
            if(value==0){
                Intent intentSub = new Intent(AccountActivity.this, NgSubActivity.class);
                intentSub.putExtra("amount", amount);
                intentSub.putExtra("userId", userId);
                intentSub.putExtra("email", email);
                startActivityForResult(intentSub, NG_SUB_ACTIVITY);
            }
            else
                pay();
        });
    }

    private int getMinimumAmount(){
        switch (value){
            case 1:
            case 2:
            case 3:
            case 4:
                return 10;
            case 5:
                return 300;
            default:
                return 1000;
        }
    }

    public void displayWithdrawalDialog(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
        LayoutInflater inflater = LayoutInflater.from(AccountActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_withdrawal, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        TextView txtCurrency = dialog.findViewById(R.id.txtCurrency);
        TextView txtError = dialog.findViewById(R.id.txtError);
        TextView txtBalance = dialog.findViewById(R.id.txtBalance);
        Spinner spnType = dialog.findViewById(R.id.spnType);

        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch (position){
                    case 0:
                        selected = "select";break;
                    case 1:
                        selected = "Bank";break;
                    case 2:
                        selected = "mPesa";break;
                    case 3:
                        selected = "Mobile money";break;
                }
                method = selected;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        txtCurrency.setText(Html.fromHtml(currencySymbol[value]));
        txtBalance.setText(Html.fromHtml(currencySymbol[value] + getBal_subs()));
        MaterialButton btnWithdraw = dialog.findViewById(R.id.btnWithdraw);
        EditText edtAmount = dialog.findViewById(R.id.edtAmount);
        EditText edtAccDetails = dialog.findViewById(R.id.edtAccDetails);

        btnWithdraw.setOnClickListener(view1 -> {
            txtError.setVisibility(View.GONE);
            String amountInString = edtAmount.getText().toString().trim();
            int amount = amountInString.isEmpty()? 0: Integer.parseInt(amountInString);
            if(amount==0){
                edtAmount.setError("Enter amount");
                return;
            }

            if(selected==null||selected.isEmpty()||selected.equals("select")){
                txtError.setText("Select account type" );
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            String accDetails = edtAccDetails.getText().toString();
            if(accDetails.isEmpty()|| accDetails.length() < 6){
                edtAccDetails.setError("Enter account details");
                txtError.setText("Enter account details" );
                txtError.setVisibility(View.VISIBLE);
                return;
            }

            if(amount>getBal_subs()){
                txtError.setText("The amount is more than your balance." );
                txtError.setVisibility(View.VISIBLE);
                return;
            }
            if(getMinimumAmount()>amount){
                txtError.setText(Html.fromHtml("Minimum withdrawal is " + currencySymbol[value] + getMinimumAmount()));
                txtError.setVisibility(View.VISIBLE);
                return;
            }
            this.amount = amount;
            dialog.cancel();
            updateDatabase(false, accDetails);
            popUp(false);
            sendNotification(false);

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                Log.i(TAG, "onActivityResult from Flutterwave: Success");
                method = "Flutterwave";
                updateDatabase(true, "");
                sendNotification(true);
                popUp(true);
            }
            else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Log.i(TAG, "onActivityResult: "+ message);
                Snackbar.make(imgDp, "CANCELLED", Snackbar.LENGTH_SHORT).show();
            }
            else {
                Log.i(TAG, "onActivityResult: "+ message);
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomMaterialAlertDialog);
                builder.setTitle("FAILED")
                        .setCancelable(false)
                        .setMessage("Your subscription failed.\nTry again or contact us for help.")
                        .setNegativeButton("Try again", (dialogInterface, i) -> {})
                        .setPositiveButton("Contact us", (dialogInterface, i) ->
                                startActivity(new Intent(AccountActivity.this, ContactActivity.class)))
                        .show();
            }
        }
        else if(requestCode==NG_SUB_ACTIVITY && resultCode==RESULT_OK){
            Log.i(TAG, "onActivityResult from paystack: Success");
            method = "card";
            updateDatabase(true, "");
            sendNotification(true);
            popUp(true);
        }
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    //Display after successful sub
    private void popUp(boolean credit){
        String message = credit?
                "You have funded your wallet successfully.\nYou can now subscribe to whoever you like \uD83D\uDE09":
                "Your withdrawal is being processed. You will receive within 24 hours.\nHave a pleasant day \uD83D\uDE09";

        //Display success dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this,R.style.CustomMaterialAlertDialog);
        builder.setTitle("TRANSACTION SUCCESSFUL")
                .setCancelable(false)
                .setMessage(message)
                .setNegativeButton("Okay", (dialogInterface, i) -> {})
                .show();
    }

    private void updateDatabase(boolean credit, String acc) {
        String amnt = Html.fromHtml(currencySymbol[value] + amount).toString();
        String desc = credit? "Deposit to your wallet": "Withdrawal ";
        String type = credit? "deposit":"withdrawal";
        int status = credit? 1:2;
        Transaction transaction = new Transaction(amnt, desc, type, method, acc, userId, username, userId, username, credit,status);
        FirebaseUtil.getFirebaseFirestore().collection(TRANSACTIONS).add(transaction);
        if(credit) {
            FirebaseUtil.getFirebaseFirestore().collection(PROFILES).document(userId)
                    .update("d6_balance_wallet", (amount + getBal_wallet()));
        }
        else
            FirebaseUtil.getFirebaseFirestore().collection(PROFILES).document(userId)
                    .update("d7_balance_sub",(getBal_subs()-amount));
    }

    private void sendNotification(boolean credit){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference dbReference = db.getReference();
        //sends notification to subscriber
        String title = "Transaction Successful";
        String type = credit? "deposit":"withdrawal";
        String message = credit? Html.fromHtml("You deposited " + currencySymbol[value] + amount + " to your wallet").toString():
                Html.fromHtml("You withdrew " + currencySymbol[value] + amount + " from your wallet").toString();
        Notification notification = new Notification("transaction", title, message, type,
                Calculations.TIPSHUB, "", userId,Calculations.TIPSHUB);
        FirebaseUtil.getFirebaseFirestore().collection(NOTIFICATIONS).add(notification);
        dbReference.child(NOTIFICATIONS).push().setValue(notification);
    }

    //Returns currency matching with the selected country
    private String getCurrency(String country){
        //init all accepted payment methods to false
        acceptAccount = acceptCard = acceptMpesa = acceptGhMobile = acceptUgMobile = acceptZmMobile = acceptRwfMobile
                = acceptBankTransfer = acceptFracMobile =false;
        switch (country.toLowerCase()){
            case "nigeria":
                value=0;
                acceptAccount = true;
                acceptCard = true;
                return "NGN";
            case "ghana":
                value=4;
                acceptCard = true;
                acceptGhMobile = true;
                return "GHS";
            case "kenya":
                value = 5;
                acceptMpesa =true;
                acceptCard = true;
                return "KES";
            case "uganda":
                value = 6;
                acceptCard = true;
                acceptUgMobile = true;
                return "UGX";
            case "tanzania":
                value = 7;
                return "TZS";
            case "south africa":
                value=8;
                return "ZAR";
            case "zambia":
                value=9;
                acceptCard = true;
                acceptZmMobile = true;
                return "ZMW";
            case "rwanda":
                value=10;
                return "RWF";
            case "cameroon":
                value=11;
                acceptCard = true;
                acceptFracMobile = true;
                return "XAF";
            case "mali":
            case "ivory coast":
            case "senegal":
                value=12;
                acceptCard = true;
                acceptFracMobile = true;
                return "XOF";
            case "northern ireland":
            case "wales":
            case "england":
            case "scotland":
            case "united kingdom":
                value=3;
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
                return "EUR";
            default:
                value=1;
                return "USD";
        }
    }

    private void pay() {
        double amount = (double) this.amount;

        Log.i("SubAct", "pay: ");
        String txRef = userId.substring(0, 6) + "_" + String.valueOf(new Date().getTime()).substring(4, 9);

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("txt_ref", txRef);
        clipboard.setPrimaryClip(clip);
        //String pKeyTest = "FLWPUBK-dc9169da645ead8fb0ce93f743056301-X";
        //String eKeyTest = "8f301671a18ce0a49e858cb2";

        final String PUBLIC_KEY = "FLWPUBK-6135390cafde2808e6aaf6b9869bf5b9-X";
        final String ENCRYPTION_KEY = "9ed4313390654a2c61f4a2b3";

        new RaveUiManager(AccountActivity.this).setAmount(amount)
                .setCurrency(currency)
                .setEmail(email)
                .setfName(firstName)
                .setlName(lastName)
                .setNarration("Tipshub deposit")
                .setPublicKey(PUBLIC_KEY)
                .setEncryptionKey(ENCRYPTION_KEY)
                .setTxRef(txRef)
                .acceptAccountPayments(acceptAccount)
                .acceptCardPayments(acceptCard)
                .acceptMpesaPayments(acceptMpesa)
                .acceptGHMobileMoneyPayments(acceptGhMobile)
                .acceptUgMobileMoneyPayments(acceptUgMobile)
                .acceptZmMobileMoneyPayments(acceptZmMobile)
                .acceptRwfMobileMoneyPayments(acceptRwfMobile)
                .acceptBankTransferPayments(acceptBankTransfer)
                .acceptFrancMobileMoneyPayments(acceptFracMobile)
                .onStagingEnv(false)
                .withTheme(R.style.DefaultTheme)
                .initialize();
    }

    public void startWhatsapp(View view) {
        String mssg = "Hello Tipshub\nI want to fund my wallet. Send me payment details.";
        String toNumber = "2349041463249";
        Uri uri = Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+mssg);
        try {
            Intent whatsApp = new Intent(Intent.ACTION_VIEW);
            whatsApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            whatsApp.setData(uri);
            getPackageManager().getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            startActivity(whatsApp);
        }
        catch (PackageManager.NameNotFoundException e){
            Toast.makeText(this, "No WhatApp installed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriberAdapter.stopListening();
        subscriptionAdapter.stopListening();
        transactionAdapter.stopListening();
    }

    public long getBal_wallet() {
        return bal_wallet;
    }

    public long getBal_subs() {
        return bal_subs;
    }
}
