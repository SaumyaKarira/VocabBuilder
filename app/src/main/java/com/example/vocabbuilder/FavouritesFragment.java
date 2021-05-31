package com.example.vocabbuilder;

import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class FavouritesFragment extends Fragment{

    RecyclerView recyclerView;
    public static final String TAG = "FavouriteFragment";
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference, favRef;
    List<WordDetails> favDetails = new ArrayList<>();
    FavouriteAdapter favAdapter;
    ArrayList<String> keys = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_favourites, container, false);
        recyclerView = view.findViewById(R.id.favourite_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();
        favRef = firebaseDatabase.getReference("Favourites");
        databaseReference = firebaseDatabase.getReference("Favourites List").child(currentUserId);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    WordDetails data = ds.getValue(WordDetails.class);
                    favDetails.add(0,data);
                    String uid = ds.getKey();
                    keys.add(0,uid);
                }
                favAdapter = new FavouriteAdapter(favDetails,keys);
                favAdapter.notifyItemInserted(0);
                recyclerView.setAdapter(favAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //favAdapter = new FavouriteAdapter(favDetails,keys);
//        favAdapter.setOnItemClickListener(this);
//        recyclerView.swapAdapter(favAdapter, false);
//        ItemTouchHelper.Callback callback = new WordTouchHelper(favAdapter, recyclerView);
//        ItemTouchHelper helper = new ItemTouchHelper(callback);
//        helper.attachToRecyclerView(recyclerView);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
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
                    wordDeleted = favDetails.get(position);
                    favRef.child(postKey).child(currentUserId).removeValue();
                    delete(wordDeleted.getDisplayDate());
                    favDetails.remove(position);
                    favAdapter.notifyItemRemoved(position);


                    Snackbar.make(recyclerView,wordDeleted.getWord(), Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    favDetails.add(position, wordDeleted);
                                    favRef.child(postKey).child(currentUserId).setValue(true);

                                    String id = databaseReference.push().getKey();
                                    databaseReference.child(id).setValue(wordDeleted);
                                    favAdapter.notifyItemInserted(position);
                                }
                            }).show();
                    break;
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeRightBackgroundColor(ContextCompat.getColor(getContext(), R.color.red))
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
    private void delete(String date) {
        Query query = databaseReference.orderByChild("displayDate").equalTo(date);
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