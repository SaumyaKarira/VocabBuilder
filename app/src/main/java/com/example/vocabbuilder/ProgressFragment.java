package com.example.vocabbuilder;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ProgressFragment extends Fragment {

    private static final String TAG = "CalendarFragment";
    private CalendarView calendarView;
    TextView totalWords, avg;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference markRef, markWordRef;
    String firstDay, currentDay;
    long difference;
    int count;
    Calendar calendar;
    SimpleDateFormat dateFormat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        currentDay = dateFormat.format(calendar.getTime());

        final String PREFS_NAME = "MyPrefsFile";

        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            Log.i("Comments", "First time");
            // first time task
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            firstDay = dateFormat.format(calendar.getTime());
            settings.edit().putString("first_day", firstDay).apply();

            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).apply();
        }
        if(savedInstanceState != null){

            SimpleDateFormat myFormat = new SimpleDateFormat("dd/MM/yyyy");
            String first = settings.getString("first_day", currentDay);
            try {
                Date date1 = myFormat.parse(first);
                Date date2 = myFormat.parse(currentDay);
                long diff = date2.getTime() - date1.getTime();
                difference = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//                if (difference == 0){
//                    difference = 1;
//                }
                Log.i("Days: ", String.valueOf(difference));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        totalWords = view.findViewById(R.id.total);
        avg = view.findViewById(R.id.avg);
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
        //assert firebaseUser != null;
        String currentUserId = firebaseUser.getUid();

        markWordRef = firebaseDatabase.getReference("Marked Words").child(currentUserId);
        markWordRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    count = (int) snapshot.getChildrenCount();
                    totalWords.setText(Integer.toString(count));
                    if ((int)difference != 0){
                        avg.setText(Integer.toString(count/(int)difference));
                    }
                    avg.setText(Integer.toString(count));
                }
                else{
                    totalWords.setText("0");
                    avg.setText("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}