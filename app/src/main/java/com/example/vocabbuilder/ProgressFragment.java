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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProgressFragment extends Fragment {

    private static final String TAG = "CalendarFragment";
    private CalendarView calendarView;
    TextView totalWords, weeklyAvg, monthlyAvg, yearlyAvg;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference markRef, markWordRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        totalWords = view.findViewById(R.id.total);
        weeklyAvg = view.findViewById(R.id.weekly_avg);
        monthlyAvg = view.findViewById(R.id.monthly_avg);
        yearlyAvg = view.findViewById(R.id.yearly_avg);
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
        calendarView.setMaxDate(System.currentTimeMillis()-1000);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        markWordRef = firebaseDatabase.getReference("Marked Words").child(currentUserId);
        markWordRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    totalWords.setText(Integer.toString((int) snapshot.getChildrenCount()));
                }
                else{
                    totalWords.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}