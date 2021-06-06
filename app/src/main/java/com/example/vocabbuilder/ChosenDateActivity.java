package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChosenDateActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageButton upButton;
    TextView chosenDate;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference wordsDatabase;
    ProgressBar progressBar;
    List<WordDetails> details = new ArrayList<>();
    Adapter adapter;
    List<String> keys = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_date);

        //progressBar = findViewById(R.id.chosen_progress_bar);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();
        wordsDatabase = firebaseDatabase.getReference("Word Of The Day").child(currentUserId);
        chosenDate = findViewById(R.id.chosen_date);
        upButton = findViewById(R.id.chosen_upbtn);
        recyclerView = findViewById(R.id.chosen_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent incomingIntent = getIntent();
        String date = incomingIntent.getStringExtra("date");
        chosenDate.setText(date);

        //progressBar.setVisibility(View.VISIBLE);

//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
                Query query = wordsDatabase.orderByChild("displayDate").equalTo(date);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            WordDetails data = ds.getValue(WordDetails.class);
                            details.add(data);
                            String uid = ds.getKey();
                            keys.add(uid);
                        }
                        adapter = new Adapter(details, keys);
                        //adapter.notifyItemInserted(0);
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//            }
//        }, 1000);


        //progressBar.setVisibility(View.GONE);
    }
}