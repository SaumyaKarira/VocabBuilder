package com.example.vocabbuilder;

import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

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

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.ViewHolder>{

    LayoutInflater inflater;
    List<WordDetails> favDetails;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference favRef, favWordsRef;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    ArrayList<String> keys;
    WordDetails wordDeleted = new WordDetails();
//    private OnItemClickListener onItemClickListener;



    public FavouriteAdapter(List<WordDetails> favDetails, ArrayList<String> keys) {
        this.inflater = inflater;
        this.favDetails = favDetails;
        this.keys = keys;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //View view = inflater.inflate(R.layout.word_list, parent, false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourite_words, parent,false);
        //view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        final String postKey = keys.get(holder.getAbsoluteAdapterPosition());

        favRef = firebaseDatabase.getReference("Favourites");
        favWordsRef = firebaseDatabase.getReference("Favourites List").child(currentUserId);

        String wrd = favDetails.get(holder.getAbsoluteAdapterPosition()).getWord();
        String def = favDetails.get(holder.getAbsoluteAdapterPosition()).getDefination();
        String eg = favDetails.get(holder.getAbsoluteAdapterPosition()).getExamples();
        String date = favDetails.get(holder.getAbsoluteAdapterPosition()).getDisplayDate();


        holder.wordName.setText(wrd);
        holder.wordDefination.setText(def);
        holder.wordExamples.setText(eg);
        holder.wordDate.setText(date);

        WordDetails favWord = favDetails.get(position);
        holder.itemView.setTag(favWord);

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                favRef.child(postKey).child(currentUserId).removeValue();
//                delete(date);
//                wordDeleted = favDetails.get(holder.getAbsoluteAdapterPosition());
//                Snackbar.make(view,wordDeleted.getWord() + " Deleted", Snackbar.LENGTH_LONG)
//                        .setAction("UNDO", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                favRef.child(postKey).child(currentUserId).setValue(true);
//                                wordDetails.setWord(wrd);
//                                wordDetails.setDefination(def);
//                                wordDetails.setExamples(eg);
//                                wordDetails.setDisplayDate(date);
//                                favDetails.add(position, wordDeleted);
//                                String id = favWordsRef.push().getKey();
//                                favWordsRef.child(id).setValue(favDetails);
//                                //favAdapter.notifyItemInserted(position);
//                            }
//                        }).show();
//            }
//        });

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
        return favDetails.size();
    }

//    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
//        this.onItemClickListener = onItemClickListener;
//    }

//    @Override
//    public void onClick(View view) {
//
//    }

//    public void onItemRemove(RecyclerView.ViewHolder viewHolder, RecyclerView mRecyclerView) {
//    }
//
//    public interface OnItemClickListener {
//    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView wordName, wordDefination, wordExamples, wordDate;

//        private OnItemClickListener mListener;
//        public interface OnItemClickListener {
//            void onItemClick(int position);
//        }
//
//        public void setOnItemClickListener(OnItemClickListener listener){
//            mListener = listener;
//        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            wordName = itemView.findViewById(R.id.fav_word);
            wordDefination = itemView.findViewById(R.id.fav_defination);
            wordExamples = itemView.findViewById(R.id.fav_examples);
            wordDate = itemView.findViewById(R.id.fav_dateShown);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if(mListener != null){
//                        int position = getAbsoluteAdapterPosition();
//                        if(position != RecyclerView.NO_POSITION){
//                            mListener.onItemClick(position);
//                        }
//                    }
//                }
//            });

        }


    }

}
