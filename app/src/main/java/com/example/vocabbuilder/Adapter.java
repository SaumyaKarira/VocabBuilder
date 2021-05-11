package com.example.vocabbuilder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    LayoutInflater inflater;
    List<WordDetails> details;

    public Adapter(List<WordDetails> details) {
        this.inflater = inflater;
        this.details = details;
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

        holder.wordName.setText(details.get(position).getWord());
        holder.wordDefination.setText(details.get(position).getDefination());
        holder.wordExamples.setText(details.get(position).getExamples());
        holder.wordDate.setText(details.get(position).getDisplayDate());
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView wordName, wordDefination, wordExamples, wordDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            wordName = itemView.findViewById(R.id.word);
            wordDefination = itemView.findViewById(R.id.defination);
            wordExamples = itemView.findViewById(R.id.examples);
            wordDate = itemView.findViewById(R.id.dateShown);
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
