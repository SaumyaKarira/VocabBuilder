package com.example.vocabbuilder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.Transaction;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileFragment extends Fragment implements View.OnClickListener {

    CircleImageView profileImage;
    TextView profileName;
    Button editProfile;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUserId = firebaseUser.getUid();
    DocumentReference documentReference;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final static int PICK_IMAGE = 1;

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        String currentUserId = firebaseUser.getUid();
//
//        DocumentReference documentReference;
//        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
//        documentReference = firebaseFirestore.collection("user").document(currentUserId);
//        documentReference.get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                        if(task.getResult().exists()){
//                            String nameResult = task.getResult().getString("FullName");
//                            //String emailResult = task.getResult().getString("Email");
//                            String url = task.getResult().getString("url");
//                            Picasso.get().load(url).into(profileImage);
//
//                            profileName.setText(nameResult);
//                            //profileEmail.setText(emailResult);
//
//                        }else {
////                            Intent intent = new Intent(getActivity(),SigninActivity.class);
////                            startActivity(intent);
//                            profileName.setText("Full Name");
//                            //profileEmail.setText("Email");
//                        }
//
//                    }
//                });
//    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        profileImage = getActivity().findViewById(R.id.profile_image);
        profileName = getActivity().findViewById(R.id.profile_name);
        //profileEmail = getActivity().findViewById(R.id.profile_email);
        editProfile = getActivity().findViewById(R.id.edit_profile);
        editProfile.setOnClickListener(this);
        profileImage.setOnClickListener(this);
//
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        String currentUserId = firebaseUser.getUid();
//
//        DocumentReference documentReference;
//        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
//        documentReference = firebaseFirestore.collection("user").document(currentUserId);
//        documentReference.get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                        if(task.getResult().exists()){
//                            String nameResult = task.getResult().getString("FullName");
//                            //String emailResult = task.getResult().getString("Email");
//                            String url = task.getResult().getString("url");
//                            Picasso.get().load(url).into(profileImage);
//
//                            profileName.setText(nameResult);
//                            //profileEmail.setText(emailResult);
//
//                        }else {
////                            Intent intent = new Intent(getActivity(),SigninActivity.class);
////                            startActivity(intent);
//                            profileName.setText("Full Name");
//                            //profileEmail.setText("Email");
//                        }
//
//                    }
//                });
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getUserInfo();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        // Inflate the layout for this fragment

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserInfo();
    }

    private void getUserInfo() {

        documentReference = firebaseFirestore.collection("user").document(currentUserId);
        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.getResult().exists()){
                            String nameResult = task.getResult().getString("FullName");
                            profileName.setText(nameResult);
                            String url = task.getResult().getString("url");
                            Picasso.get().load(url).into(profileImage);

                        }else {
                            Toast.makeText(getContext(), "No Profile",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.edit_profile:
                Intent intent = new Intent(getActivity(), UpdateProfile.class);
                startActivity(intent);
                break;
            case R.id.profile_image:
                Intent intent1 = new Intent(getActivity(), ImageActivity.class);
                startActivity(intent1);
                break;
        }
    }
}