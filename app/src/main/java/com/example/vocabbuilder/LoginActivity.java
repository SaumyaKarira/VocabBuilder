package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button signin;
    TextView signup, forgotPassword;
    FirebaseAuth fAuth;
    ProgressBar progressBar;
    ImageButton loginPhone;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    AllUsers allUsers;
    //String currentUserId;
    FirebaseUser firebaseUser;

    GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        fAuth = FirebaseAuth.getInstance();
        firebaseUser = fAuth.getCurrentUser();

        if(firebaseUser != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        email = findViewById(R.id.email_address);
        password = findViewById(R.id.password);
        signin = findViewById(R.id.signin_btn);
        signup = findViewById(R.id.signup);
        forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class));
            }
        });

        loginPhone = findViewById(R.id.login_phone);
        loginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), LoginPhoneActivity.class));
                finish();
            }
        });

        allUsers = new AllUsers();
        progressBar = findViewById(R.id.progress_bar);



        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String Email = email.getText().toString().trim();
                String Password = password.getText().toString();

                if(TextUtils.isEmpty(Email)) {
                    email.setError("Email is required");
                    email.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(Email).matches()){
                    email.setError("Please provide valid email address");
                    email.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(Password)) {
                    password.setError("Password is required");
                    password.requestFocus();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate the user
                fAuth.signInWithEmailAndPassword(Email, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Logged In Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }else {
                            Toast.makeText(LoginActivity.this, "Error!!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SigninActivity.class));
                finish();
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("339124003537-280m9ol664odbbloqa3k7p77404rf9eq.apps.googleusercontent.com")
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }

    private void signIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                // account is null, initialize auth credentials
                AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                fAuth.signInWithCredential(authCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }

                    }
                });
            }

            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

                String currentUserId = firebaseUser.getUid();
                documentReference = firebaseFirestore.collection("user").document(currentUserId);
                storageReference = FirebaseStorage.getInstance().getReference("Profile Images");
                databaseReference = firebaseDatabase.getReference("All User");

                Map<String, String> profile =  new HashMap<>();
                profile.put("FullName", personName);
                profile.put("Email", personEmail);
                profile.put("uid", currentUserId);
                //profile.put("phone", Phone);
                allUsers.setFullName(personName);
                allUsers.setEmail(personEmail);
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
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            //Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Log.d("SignInFail" , e.toString());
        }
    }
}