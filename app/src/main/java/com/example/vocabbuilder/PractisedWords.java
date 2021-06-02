package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
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

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class PractisedWords extends AppCompatActivity {

    TextView calendarDate;
    ImageButton upButton;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference markRef, markWordRef;
    List<WordDetails> practisedWords = new ArrayList<>();
    FavouriteAdapter favAdapter;
    ArrayList<String> keys = new ArrayList<>();
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practised_words);

        calendarDate = findViewById(R.id.calendar_date);
        upButton = findViewById(R.id.practised_upbtn);
        recyclerView = findViewById(R.id.practised_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Intent incomingIntent = getIntent();
        String date = incomingIntent.getStringExtra("date");
        calendarDate.setText(date);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        markRef = firebaseDatabase.getReference("Marked");
        markWordRef = firebaseDatabase.getReference("Marked Words").child(currentUserId);

        Query query = markWordRef.orderByChild("displayDate").equalTo(date);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    WordDetails data = ds.getValue(WordDetails.class);
                    practisedWords.add(0,data);
                    String uid = ds.getKey();
                    keys.add(0,uid);
                }
                favAdapter = new FavouriteAdapter(practisedWords,keys);
                favAdapter.notifyItemInserted(0);
                recyclerView.setAdapter(favAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    WordDetails wordDeleted = null;


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            int position = viewHolder.getAbsoluteAdapterPosition();
            final String postKey = keys.get(position);

            switch (direction){

                case ItemTouchHelper.RIGHT:
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    String currentUserId = firebaseUser.getUid();
                    wordDeleted = practisedWords.get(position);
                    markRef.child(postKey).child(currentUserId).removeValue();
                    delete(wordDeleted.getDisplayDate());
                    practisedWords.remove(position);
                    favAdapter.notifyItemRemoved(position);


                    Snackbar.make(recyclerView,wordDeleted.getWord(), Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    practisedWords.add(position, wordDeleted);
                                    markRef.child(postKey).child(currentUserId).setValue(true);

                                    String id = markWordRef.push().getKey();
                                    markWordRef.child(id).setValue(wordDeleted);
                                    favAdapter.notifyItemInserted(position);
                                }
                            }).show();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.red))
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
    private void delete(String date) {
        Query query = markWordRef.orderByChild("displayDate").equalTo(date);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    dataSnapshot.getRef().removeValue();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}