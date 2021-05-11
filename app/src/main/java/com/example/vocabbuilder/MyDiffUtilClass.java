package com.example.vocabbuilder;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class MyDiffUtilClass extends DiffUtil.Callback {

    private List<WordDetails> mOldWordList;
    private List<WordDetails> mNewWordList;

    public MyDiffUtilClass(List<WordDetails> oldWordList, List<WordDetails> newWordList) {
        this.mOldWordList = oldWordList;
        this.mNewWordList = newWordList;
    }

    @Override
    public int getOldListSize() {
        return mOldWordList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewWordList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return true;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final WordDetails oldList = mOldWordList.get(oldItemPosition);
        final WordDetails newList = mNewWordList.get(newItemPosition);

        return oldList.getWord().equals(newList.getWord()) || oldList.getDisplayDate().equals(newList.getDisplayDate());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
