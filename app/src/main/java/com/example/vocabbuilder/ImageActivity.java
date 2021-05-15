package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity {

    ImageView profileImage;
    Button editButton, deleteButton;
    DocumentReference documentReference;
    String url;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //deleteButton = findViewById(R.id.delete_button);
        //editButton = findViewById(R.id.edit_button);
        profileImage = findViewById(R.id.user_image);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        documentReference = firebaseFirestore.collection("user").document(currentUserId);

//        editButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(ImageActivity.this, UpdatePhoto.class);
//                startActivity(intent);
//            }
//        });
//
//        deleteButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
//                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(ImageActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(ImageActivity.this, "Failed To Deleted"+e, Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()){
                    url = task.getResult().getString("url");
                    Picasso.get().load(url).into(profileImage);
                }else{
                    Toast.makeText(ImageActivity.this, "No Profile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}