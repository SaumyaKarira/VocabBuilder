package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginPhoneActivity extends AppCompatActivity {

    LinearLayout phoneLayout, otpLayout;
    Button continueButton, submitButton;
    TextView resendOtp;
    EditText phoneNum, otp;

    //To Resend OTP
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationID;
    private static final String TAG = "LoginPhoneActivity";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_phone);

        phoneLayout = findViewById(R.id.phoneLL);
        otpLayout = findViewById(R.id.otpLL);
        continueButton = findViewById(R.id.phone_continue);
        submitButton = findViewById(R.id.phone_submit);
        resendOtp = findViewById(R.id.resend_otp);
        phoneNum = findViewById(R.id.phone_number);
        otp = findViewById(R.id.otp);

        phoneLayout.setVisibility(View.VISIBLE);
        otpLayout.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progressDialog.dismiss();
                Toast.makeText(LoginPhoneActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(s, token);
                verificationID = s;
                forceResendingToken = token;
                progressDialog.dismiss();

                phoneLayout.setVisibility(View.GONE);
                otpLayout.setVisibility(View.VISIBLE);

                Toast.makeText(LoginPhoneActivity.this, "Verification Code Sent", Toast.LENGTH_SHORT).show();

            }
        };

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Phone = phoneNum.getText().toString().trim();
                if(TextUtils.isEmpty(Phone)) {
                    phoneNum.setError("Phone Number is required");
                    return;
                }
                else{
                    startPhoneNumberVerification(Phone);
                }

            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = otp.getText().toString().trim();
                if(TextUtils.isEmpty(code)) {
                    otp.setError("Phone Number is required");
                    return;
                }
                else{
                    verifyPhoneNumberWithCode(verificationID, code);
                }

            }
        });

        resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Phone = phoneNum.getText().toString().trim();
                if(TextUtils.isEmpty(Phone)) {
                    phoneNum.setError("Phone Number is required");
                    return;
                }
                else{
                    resendVerificationCode(Phone, forceResendingToken);
                }

            }
        });

    }

    private void verifyPhoneNumberWithCode(String verificationID, String code) {
        progressDialog.setMessage("Verifying Code");
        progressDialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID,code);
        signInWithPhoneCredential(credential);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        progressDialog.setMessage("Loggin In");
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        progressDialog.dismiss();
                        //String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void resendVerificationCode(String phone, PhoneAuthProvider.ForceResendingToken token) {

        progressDialog.setMessage("Verifying Code");
        progressDialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .setForceResendingToken(token)
                .build();

    }

    private void startPhoneNumberVerification(String phone) {
        progressDialog.setMessage("Verifying Phone Number");
        progressDialog.show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(mCallbacks)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}