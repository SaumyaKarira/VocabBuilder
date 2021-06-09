package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfile extends AppCompatActivity {

    EditText editName;
    CircleImageView profileImage;
    String nameResult;
    Button saveChanges;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    String currentUserId;
    private final static int PICK_IMAGE = 1;
    Uri imageUri, url;
    private String myUri = "";
    //UploadTask uploadTask;
    StorageTask uploadTask;
    ImageButton upButton;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    DocumentReference documentReference;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);


        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = firebaseUser.getUid();
        documentReference = firebaseFirestore.collection("user").document(currentUserId);
        storageReference = firebaseStorage.getReference("Profile");
        upButton = findViewById(R.id.update_profile_upbtn);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        editName = findViewById(R.id.update_name);
        profileImage = findViewById(R.id.update_image);
        saveChanges = findViewById(R.id.save_changes);

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadProfileImage();
                updateName();
                //restartApp();

                progressDialog.setTitle("Set Your Profile");
                progressDialog.setMessage("Please Wait, while we are setting up your data your app will restart");
                progressDialog.show();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // this code will be executed after 3 seconds
                        restartApp();
                    }
                }, 1000);
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity().setAspectRatio(1,1).start(UpdateProfile.this);
            }
        });
        
       getUserinfo();
        
    }

    private void restartApp() {
//        progressDialog.setTitle("Profile Updation");
//        progressDialog.setMessage("Please Wait, your app will restart after few seconds");
//        progressDialog.show();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after 3 seconds
                Intent mStartActivity = new Intent(UpdateProfile.this, MainActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(UpdateProfile.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager)UpdateProfile.this.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);
            }
        }, 3000);

        progressDialog.dismiss();
    }


    private void getUserinfo() {
        //Toast.makeText(UpdateProfile.this, "IN getUserinfo",Toast.LENGTH_SHORT).show();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        DocumentReference documentReference;
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        documentReference = firebaseFirestore.collection("user").document(currentUserId);
        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){
                            nameResult = task.getResult().getString("FullName");
                            editName.setText(nameResult);
                            String url = task.getResult().getString("url");
                            Picasso.get().load(url).into(profileImage);

                        }else {
                            Toast.makeText(UpdateProfile.this, "No Profile",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private String getFileExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadProfileImage() {
        progressDialog.setTitle("Set Your Profile");
        progressDialog.setMessage("Please Wait, while we are setting up your data your app will restart");
        progressDialog.show();

        if(imageUri != null){
            final StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
//            final StorageReference fileRef = storageReference
//                    .child(firebaseAuth.getCurrentUser().getUid() + "." + getFileExt(imageUri));
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadurl = (Uri) task.getResult();
                        myUri = downloadurl.toString();

                        Map<String, Object> profile = new HashMap<>();
                        profile.put("url", myUri);

                        final DocumentReference sDoc = firebaseFirestore.collection("user").document(currentUserId);
                        firebaseFirestore.runTransaction(new Transaction.Function<Void>() {
                            @Nullable
                            @Override
                            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                transaction.update(sDoc, "url", myUri);
                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //Toast.makeText(UpdateProfile.this, "Image Updated",Toast.LENGTH_SHORT).show();
                                //finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UpdateProfile.this, "Image Updation Failed " + e,Toast.LENGTH_SHORT).show();
                            }
                        });

                        //firebaseFirestore.collection("user").document(currentUserId).set(profile, SetOptions.merge());
                        progressDialog.dismiss();

                    }

                }
            });
        }
        else{
            progressDialog.dismiss();
            Toast.makeText(UpdateProfile.this, "No Image Selected",Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profileImage.setImageURI(imageUri);


        }
        else{
            Toast.makeText(UpdateProfile.this, "Error! Try Again", Toast.LENGTH_SHORT).show();
        }

    }


//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        progressDialog.dismiss();
//    }

    private void updateName() {

        String fullName = editName.getText().toString();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();
        final DocumentReference sDoc = firebaseFirestore.collection("user").document(currentUserId);

        firebaseFirestore.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                transaction.update(sDoc, "FullName", fullName);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //Toast.makeText(UpdateProfile.this, "Name Updated",Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateProfile.this, "Name Updation Failed " + e,Toast.LENGTH_SHORT).show();
            }
        });
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                finish();
//                Intent mStartActivity = new Intent(UpdateProfile.this, MainActivity.class);
//                int mPendingIntentId = 123456;
//                PendingIntent mPendingIntent = PendingIntent.getActivity(UpdateProfile.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
//                AlarmManager mgr = (AlarmManager)UpdateProfile.this.getSystemService(Context.ALARM_SERVICE);
//                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//                System.exit(0);
//            }
//        });
    }

}