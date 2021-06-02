package com.example.vocabbuilder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        calendarView = view.findViewById(R.id.calendar);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                i1 = i1+1;
                String day = "", month = "";
                if(i2<9){
                    day = "0" + i2;
                }
                else{
                    day = Integer.toString(i2);
                }
                if(i1 < 9){
                    month = "0" + i1;
                }
                else{
                    month = Integer.toString(i1);
                }
                String date = day + "/" + month + "/" + i;
                Intent intent = new Intent(getContext(), PractisedWords.class);
                intent.putExtra("date", date);
                startActivity(intent);

            }
        });

        return view;
    }
}