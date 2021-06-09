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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class LoginPhoneActivity extends AppCompatActivity {

    LinearLayout phoneLayout, otpLayout;
    Button continueButton, submitButton;
    TextView resendOtp;
    EditText phoneNum, otp;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    AllUsers allUsers;
    String currentUserId;
    FirebaseUser firebaseUser;

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
        //firebaseUser = firebaseAuth.getCurrentUser();

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
                    otp.setError("Verification Code is required");
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
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            allUsers = new AllUsers();
                            firebaseAuth = FirebaseAuth.getInstance();
                            firebaseUser = firebaseAuth.getCurrentUser();
                            currentUserId = firebaseUser.getUid();
                            documentReference = firebaseFirestore.collection("user").document(currentUserId);
                            storageReference = FirebaseStorage.getInstance().getReference("Profile Images");
                            databaseReference = firebaseDatabase.getReference("All User");

                            Map<String, String> profile = new HashMap<>();
                            profile.put("FullName", "Full Name");
                            profile.put("Email", "Email");
                            profile.put("uid", currentUserId);
                            //profile.put("phone", Phone);
                            allUsers.setFullName("Full Name");
                            allUsers.setEmail("Email");
                            //allUsers.setPhone(Phone);
                            databaseReference.child(currentUserId).setValue(allUsers);
                            documentReference.set(profile)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
//                                progressBar.setVisibility(View.INVISIBLE);
//                                Toast.makeText(SigninActivity.this, "Profile Created", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            progressDialog.dismiss();
                            //String phone = firebaseAuth.getCurrentUser().getPhoneNumber();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(LoginPhoneActivity.this, "Error!!" + task.getException().getMessage(), LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
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