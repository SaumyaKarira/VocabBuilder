package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfile extends AppCompatActivity {

    EditText editName;
    ImageView updatedImage;
    String nameResult;
    Button saveChanges;
    FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    String currentUserId;
    private final static int PICK_IMAGE = 1;
    Uri imageUri, url;
    UploadTask uploadTask;
    StorageReference storageReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;
    DocumentReference documentReference;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = firebaseUser.getUid();
        documentReference = firebaseFirestore.collection("user").document(currentUserId);

        storageReference = firebaseStorage.getReference("Profile");

        editName = findViewById(R.id.update_name);
        updatedImage = findViewById(R.id.update_image);
        saveChanges = findViewById(R.id.save_changes);


        updatedImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
                UpdateImage();
            }
        });
    }

    private String getFileExt(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try{
            if (requestCode == PICK_IMAGE || requestCode == RESULT_OK || data != null || data.getData() != null){
                imageUri = data.getData();
                Picasso.get().load(imageUri).into(updatedImage);
            }
        }catch (Exception e){
            Toast.makeText(this, "Error"+e, Toast.LENGTH_SHORT).show();
        }

    }

//    public void chooseImage(View view) {
//
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(intent, PICK_IMAGE);
//
//    }

    //To show previous data on start of activity
    @Override
    protected void onStart() {
        super.onStart();

        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){
                            nameResult = task.getResult().getString("FullName");
                            editName.setText(nameResult);
                            String url = task.getResult().getString("url");
                            Picasso.get().load(url).into(updatedImage);

                        }else {
                            Toast.makeText(UpdateProfile.this, "No Profile",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void updateProfile() {

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
                Toast.makeText(UpdateProfile.this, "Profile Updated",Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UpdateProfile.this, "Updation Failed",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UpdateImage() {
        if(imageUri != null){

            final StorageReference reference = storageReference.child(System.currentTimeMillis() + "." + getFileExt(imageUri));
            uploadTask = reference.putFile(imageUri);

            Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw  task.getException();
                    }
                    return reference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        Map<String, Object> profile = new HashMap<>();
                        profile.put("url", downloadUri.toString());

                        firebaseFirestore.collection("user").document(currentUserId).set(profile, SetOptions.merge());

//                        final DocumentReference sDoc = firebaseFirestore.collection("user").document(currentUserId);
//
//                        firebaseFirestore.runTransaction(new Transaction.Function<Void>() {
//                            @Nullable
//                            @Override
//                            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
//                                transaction.update(sDoc, "url", downloadUri);
//                                return null;
//                            }
//                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Toast.makeText(UpdateProfile.this, "Photo Updated",Toast.LENGTH_SHORT).show();
//                                finish();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Toast.makeText(UpdateProfile.this, "Updation Failed"+e,Toast.LENGTH_SHORT).show();
//                            }
//                        });
                    }
                }
            });
        }

    }
}