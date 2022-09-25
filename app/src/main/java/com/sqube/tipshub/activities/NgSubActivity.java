package com.sqube.tipshub.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.sqube.tipshub.R;

import org.json.JSONException;

import java.util.Date;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;

public class NgSubActivity extends AppCompatActivity {
    private String userId;
    private String email;

    enum Status {SUCCESSFUL, PENDING}

    private EditText mEditCardNum;
    private EditText mEditCVV;
    private EditText mEditExpiryMonth;
    private EditText mEditExpiryYear;
    private MaterialButton btnPay;

    private TextView mTextResponse;

    private ProgressBar prgPayment;
    private ProgressDialog dialog;
    private Charge charge;
    private Transaction transaction;

    private int amount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ng_sub);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Subscription");
        }

        TextView mAmount = findViewById(R.id.txt_amount);
        mEditCardNum = findViewById(R.id.card_number);
        mEditCVV = findViewById(R.id.cvv);
        mEditExpiryMonth = findViewById(R.id.month);
        mEditExpiryYear = findViewById(R.id.year);
        btnPay = findViewById(R.id.btnPay);

        prgPayment = findViewById(R.id.prgPayment);
        mTextResponse = findViewById(R.id.textview_response);
        mTextResponse.setOnClickListener(view -> openContact());
        dialog = new ProgressDialog(NgSubActivity.this);

        if(savedInstanceState!=null){
            amount = savedInstanceState.getInt("amount", 0);
            userId = savedInstanceState.getString("userId");
            email = savedInstanceState.getString("email");
        }
        else{
            amount = getIntent().getIntExtra("amount", 0);
            userId = getIntent().getStringExtra("userId");
            email = getIntent().getStringExtra("email");
        }

        mAmount.setText(Html.fromHtml("&#8358;" + amount));
        PaystackSdk.initialize(getApplicationContext()); //initializing paystack sdk

        mEditCardNum.addTextChangedListener(new TextWatcher() {
            private static final char space = ' ';

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Remove all spacing char
                int pos = 0;
                while (true) {
                    if (pos >= s.length()) break;
                    if (space == s.charAt(pos) && (((pos + 1) % 5) != 0 || pos + 1 == s.length())) {
                        s.delete(pos, pos + 1);
                    } else {
                        pos++;
                    }
                }

                // Insert char where needed.
                pos = 4;
                while (true) {
                    if (pos >= s.length()) break;
                    final char c = s.charAt(pos);
                    // Only if its a digit where there should be a space we insert a space
                    if ("0123456789".indexOf(c) >= 0) {
                        s.insert(pos, "" + space);
                    }
                    pos += 5;
                }
            }
        });

        btnPay.setOnClickListener(view -> {
            try {
                transact();
            } catch (Exception e) {
                btnPay.setEnabled(true);
                NgSubActivity.this.mTextResponse.setText(String.format("An error occurred while charging card: %s", e.getMessage()));
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        if ((dialog != null) && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void transact(){
        int expiryMonth = 0;
        int expiryYear = 0;

        String cardNumber, mm, yy, cvv;
        cardNumber = mEditCardNum.getText().toString().trim();
        mm = mEditExpiryMonth.getText().toString().trim();
        yy = mEditExpiryYear.getText().toString().trim();
        cvv = mEditCVV.getText().toString().trim();

        if(cardNumber.isEmpty())
            mEditCardNum.setError("Card number");
        if(mm.isEmpty())
            mEditExpiryMonth.setError("month");
        if (yy.isEmpty())
            mEditExpiryYear.setError("year");
        if(cvv.isEmpty())
            mEditCVV.setError("CVV");

        if(cardNumber.isEmpty() || mm.isEmpty() || yy.isEmpty() || cvv.isEmpty())
            return;

        cardNumber = cardNumber.replace(" ", "");
        expiryMonth = Integer.parseInt(mm);
        expiryYear = Integer.parseInt(yy);

        Card card = new Card(cardNumber, expiryMonth, expiryYear, cvv);
        if (card.isValid()) {
            btnPay.setEnabled(false);
            mTextResponse.setText(getResources().getString(R.string.txt_processing));
            prgPayment.setVisibility(View.VISIBLE);

            charge = new Charge();
            charge.setCard(card);

            dialog = new ProgressDialog(NgSubActivity.this);
            dialog.setMessage(getResources().getString(R.string.txt_processing));
            dialog.show();

            charge.setAmount((amount*100));
            charge.setEmail(email);
            charge.setReference(userId +"_"+ new Date().getTime());
            try {
                charge.putCustomField("Charged From", "Tipshub main app");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            chargeCard(); //Function to Charge user here
        }
        else {
            Toast.makeText(NgSubActivity.this, "Invalid card details", Toast.LENGTH_LONG).show();
            mTextResponse.setText("Invalid card details");
        }
    }

    private void chargeCard() {
        transaction = null;
        PaystackSdk.chargeCard(NgSubActivity.this, charge, new Paystack.TransactionCallback() {
            // This is called only after transaction is successful
            @Override
            public void onSuccess(Transaction transaction) {
                if ((dialog != null) && dialog.isShowing())
                    dialog.dismiss();

                setResult(RESULT_OK);
                NgSubActivity.this.transaction = transaction;
                updateTextViews(Status.SUCCESSFUL);
                finish();
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                NgSubActivity.this.transaction = transaction;
                updateTextViews(Status.PENDING);
            }


            @Override
            public void onError(Throwable error, Transaction transaction) {
                Log.i("paystack", "onError: "+ error.getMessage());
                if ((dialog != null) && dialog.isShowing())
                    dialog.dismiss();
                prgPayment.setVisibility(View.GONE);
                btnPay.setEnabled(true);
                mTextResponse.setText(String.format("Error: %s\n\n%s", error.getMessage(),
                        getResources().getString(R.string.txt_try_again)));
            }
        });
    }

    private void updateTextViews(Status status) {
        if (transaction.getReference() != null) {
            if (status==Status.SUCCESSFUL) {
                prgPayment.setVisibility(View.GONE);
                mTextResponse.setText("Successful!!");
            }
            if(status==Status.PENDING){
                prgPayment.setVisibility(View.VISIBLE);
                mTextResponse.setText(getResources().getString(R.string.txt_processing));
            }
        } else {
            mTextResponse.setText("No transaction");
            prgPayment.setVisibility(View.GONE);
            btnPay.setEnabled(true);
        }
    }

    public void openContact(){
        startActivity(new Intent(NgSubActivity.this, ContactActivity.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("amount", amount);
        outState.putString("userId", userId);
        outState.putString("email", email);
        super.onSaveInstanceState(outState);
    }

}
