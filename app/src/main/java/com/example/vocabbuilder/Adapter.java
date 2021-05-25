package com.example.vocabbuilder;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Collections;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    LayoutInflater inflater;
    List<WordDetails> details;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference userDatabase, favRef, favWordsRef;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    Boolean favouriteChecker = false;
    WordDetails wordDetails = new WordDetails();
    ArrayList<String> keys;


    public Adapter(List<WordDetails> details, ArrayList<String> keys) {
        this.inflater = inflater;
        this.details = details;
        this.keys = keys;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = inflater.inflate(R.layout.word_list, parent, false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_list, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        userDatabase = firebaseDatabase.getReference("User Words").child(currentUserId);

        favRef = firebaseDatabase.getReference("Favourites");
        favWordsRef = firebaseDatabase.getReference("Favourites List").child(currentUserId);

        final String postKey = keys.get(holder.getAbsoluteAdapterPosition());

        holder.wordName.setText(details.get(holder.getAbsoluteAdapterPosition()).getWord());
        holder.wordDefination.setText(details.get(holder.getAbsoluteAdapterPosition()).getDefination());
        holder.wordExamples.setText(details.get(holder.getAbsoluteAdapterPosition()).getExamples());
        holder.wordDate.setText(details.get(holder.getAbsoluteAdapterPosition()).getDisplayDate());

        String wrd = details.get(holder.getAbsoluteAdapterPosition()).getWord();
        String def = details.get(holder.getAbsoluteAdapterPosition()).getDefination();
        String eg = details.get(holder.getAbsoluteAdapterPosition()).getExamples();
        String date = details.get(holder.getAbsoluteAdapterPosition()).getDisplayDate();

        holder.favChecker(postKey);
        holder.favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                favouriteChecker = true;
                favRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(favouriteChecker.equals(true)){
                            if(snapshot.child(postKey).hasChild(currentUserId)){
                                favRef.child(postKey).child(currentUserId).removeValue();
                                delete(date);
                            }else{
                                favRef.child(postKey).child(currentUserId).setValue(true);
                                wordDetails.setWord(wrd);
                                wordDetails.setDefination(def);
                                wordDetails.setExamples(eg);
                                wordDetails.setDisplayDate(date);

                                String id = favWordsRef.push().getKey();
                                favWordsRef.child(id).setValue(wordDetails);
                            }
                            favouriteChecker = false;

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    private void delete(String date) {
        Query query = favWordsRef.orderByChild("displayDate").equalTo(date);
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

    @Override
    public int getItemCount() {
        return details.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView wordName, wordDefination, wordExamples, wordDate;
        ImageButton favouriteButton;
        DatabaseReference favouriteItemRef;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            wordName = itemView.findViewById(R.id.word);
            wordDefination = itemView.findViewById(R.id.defination);
            wordExamples = itemView.findViewById(R.id.examples);
            wordDate = itemView.findViewById(R.id.dateShown);
            favouriteButton = itemView.findViewById(R.id.favbtn);
//            favouriteButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    int position = getAbsoluteAdapterPosition();
//                }
//            });
        }

        public void favChecker(String postKey) {
            favouriteButton = itemView.findViewById(R.id.favbtn);
            favouriteItemRef = firebaseDatabase.getReference("Favourites");

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserId = firebaseUser.getUid();

            favouriteItemRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(postKey).hasChild(currentUserId)){
                        favouriteButton.setImageResource(R.drawable.ic_fav_coloured_24);
                    }else {
                        favouriteButton.setImageResource(R.drawable.ic_fav_shadow_24);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    public void updateWordList(List<WordDetails> words) {
        final MyDiffUtilClass diffCallback = new MyDiffUtilClass(details, words);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

//        this.details.clear();
//        this.details.addAll(words);
        details = words;
        diffResult.dispatchUpdatesTo(this);
    }
}
