package com.sqube.tipshub;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hbb20.CountryCodePicker;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {
    Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        btnSignup = findViewById(R.id.btnSignup); btnSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSignup:
                completeProfile();
                break;
        }
    }

    public void completeProfile(){
        /*
        Build a dialogView for user to set profile image
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_signup2, null);
        final boolean[] numberValid = new boolean[1];
        builder.setView(dialogView);

        final AlertDialog dialog= builder.create();
        dialog.setCancelable(false);
        dialog.show();

        final CountryCodePicker ccp = dialog.findViewById(R.id.ccp);
        final EditText edtCarrierNumber= dialog.findViewById(R.id.editText_carrierNumber);
        ccp.registerCarrierNumberEditText(edtCarrierNumber);
        Button btnSave = dialog.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("CountryCodePicker", "onClick: "+ ccp.getSelectedCountryName()
                        + ", " + ccp.getFullNumber() + ", " + numberValid[0]);
                dialog.cancel();
            }
        });
        ccp.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {
            @Override
            public void onValidityChanged(boolean isValidNumber) {
                numberValid[0] =isValidNumber;
            }
        });
    }

}
