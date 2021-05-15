package com.example.vocabbuilder;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;


public class ProgressFragment extends Fragment {

    private static final String TAG = "CalendarFragment";
    private CalendarView calendarView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        calendarView = getActivity().findViewById(R.id.calendar);
        // Inflate the layout for this fragment

        try {
            return inflater.inflate(R.layout.fragment_progress, container, false);
            // ... rest of body of onCreateView() ...
        } catch (Exception e) {
            Log.e("error prog", "onCreateView", e);
            throw e;
        }
    }
}