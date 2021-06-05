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
    DatabaseReference userDatabase, favRef, favWordsRef, markRef, markWordRef;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    Boolean favouriteChecker = false, practisedChecker = false;
    WordDetails wordDetails = new WordDetails();
    List<String> keys;
    String wrd,def,eg, date;


    public Adapter(List<WordDetails> details, List<String> keys) {
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

        markRef = firebaseDatabase.getReference("Marked");
        markWordRef = firebaseDatabase.getReference("Marked Words").child(currentUserId);


        //if(position != RecyclerView.NO_POSITION && position < details.size()){
            final String postKey = keys.get(holder.getAbsoluteAdapterPosition());
            wrd = details.get(holder.getAbsoluteAdapterPosition()).getWord();
            def = details.get(holder.getAbsoluteAdapterPosition()).getDefination();
            eg = details.get(holder.getAbsoluteAdapterPosition()).getExamples();
            date = details.get(holder.getAbsoluteAdapterPosition()).getDisplayDate();

//            postKey = keys.get(position);
//            wrd = details.get(position).getWord();
//            def = details.get(position).getDefination();
//            eg = details.get(position).getExamples();
//            date = details.get(position).getDisplayDate();
        //}



        holder.wordName.setText(wrd);
        holder.wordDefination.setText(def);
        holder.wordExamples.setText(eg);
        holder.wordDate.setText(date);

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

        holder.practisedChecker(postKey);
        holder.practisedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                practisedChecker = true;
                markRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(practisedChecker.equals(true)){
                            if(snapshot.child(postKey).hasChild(currentUserId)){
                                markRef.child(postKey).child(currentUserId).removeValue();
                                deleteMarked(date);
                            }else{
                                markRef.child(postKey).child(currentUserId).setValue(true);
                                wordDetails.setWord(wrd);
                                wordDetails.setDefination(def);
                                wordDetails.setExamples(eg);
                                wordDetails.setDisplayDate(date);

                                String id = markWordRef.push().getKey();
                                markWordRef.child(id).setValue(wordDetails);
                            }
                            practisedChecker = false;

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

    private void deleteMarked(String date) {
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

    @Override
    public int getItemCount() {
        return details.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView wordName, wordDefination, wordExamples, wordDate;
        ImageButton favouriteButton, practisedButton;
        DatabaseReference favouriteItemRef, markAsPractisedReference;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            wordName = itemView.findViewById(R.id.word);
            wordDefination = itemView.findViewById(R.id.defination);
            wordExamples = itemView.findViewById(R.id.examples);
            wordDate = itemView.findViewById(R.id.dateShown);
            favouriteButton = itemView.findViewById(R.id.favbtn);
            practisedButton = itemView.findViewById(R.id.mark_as_practised);

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

        public void practisedChecker(String postKey) {
            practisedButton = itemView.findViewById(R.id.mark_as_practised);
            markAsPractisedReference = firebaseDatabase.getReference("Marked");

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            String currentUserId = firebaseUser.getUid();

            markAsPractisedReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.child(postKey).hasChild(currentUserId)){
                        practisedButton.setImageResource(R.drawable.ic_baseline_done_coloured_24);
                    }else {
                        practisedButton.setImageResource(R.drawable.ic_baseline_done_shadow_24);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

//    public void updateWordList(List<WordDetails> words) {
//        final MyDiffUtilClass diffCallback = new MyDiffUtilClass(details, words);
//        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
//
////        this.details.clear();
////        this.details.addAll(words);
//        details = words;
//        diffResult.dispatchUpdatesTo(this);
//    }
}
