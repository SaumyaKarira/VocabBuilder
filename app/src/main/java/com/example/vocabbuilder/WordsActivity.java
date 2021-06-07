package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WordsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageButton upButton, pickDate;
    Calendar calendar;
    SimpleDateFormat dateFormat;
    String date,buffer;
    String api = "r23z9iddrkwv17ayqm4l905rw9xb2so4aujqw17fawmh2dgoi";
    List<WordDetails> details;
    Adapter adapter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference wordsDatabase, allWords;
    List<String> keys;
    ProgressBar progressBar;
    int  day, month,year;
    List<Words> searchWords;



    @Override
    public void onStart() {
        super.onStart();

        progressBar.setVisibility(View.VISIBLE);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // this code will be executed after 500 mseconds
                showData();
            }
        }, 2000);

    }

    private void showData() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Query query = wordsDatabase.orderByChild("displayDate").equalTo(date);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        details.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            WordDetails data = ds.getValue(WordDetails.class);
                            details.add(data);
                            String uid = ds.getKey();
                            keys.add(uid);
                        }
                        adapter = new Adapter(details, keys);
                        //adapter.notifyItemInserted(0);
                        adapter.notifyDataSetChanged();
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//                String currentUserId = firebaseUser.getUid();
//                wordsDatabase = firebaseDatabase.getReference("Word Of The Day").child(currentUserId);
//                wordsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for(DataSnapshot ds : snapshot.getChildren()){
//                            String fetchedDate = ds.child("displayDate").getValue().toString();
//                            if(fetchedDate.equals(date)) {
//                                WordDetails data = ds.getValue(WordDetails.class);
//                                details.add(data);
//                                String uid = ds.getKey();
//                                keys.add(uid);
//                            }
//                        }
//                        adapter = new Adapter(details, keys);
//                        //adapter.notifyItemInserted(0);
//                        adapter.notifyDataSetChanged();
//                        recyclerView.setAdapter(adapter);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });

                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        details = new ArrayList<>();
        keys = new ArrayList<>();

        calendar = Calendar.getInstance();
//        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//        date = dateFormat.format(calendar.getTime());

        progressBar = findViewById(R.id.words_progress_bar);
        pickDate = findViewById(R.id.pick_date);
        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                day = calendar.get(Calendar.DATE);
                month = calendar.get(Calendar.MONTH);
                year = calendar.get(Calendar.YEAR);

                DatePickerDialog datePickerDialog = new DatePickerDialog(WordsActivity.this, android.R.style.Theme_DeviceDefault_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        i1 = i1+1;
                        String chosenday = "", chosenmonth = "";
                        if(i2<9){
                            chosenday = "0" + i2;
                        }
                        else{
                            chosenday = Integer.toString(i2);
                        }
                        if(i1 < 9){
                            chosenmonth = "0" + i1;
                        }
                        else{
                            chosenmonth = Integer.toString(i1);
                        }
                        String choosenDate = chosenday + "/" + chosenmonth + "/" + i;
                        String buffer = i + "-" + chosenmonth + "-" + choosenDate;
                        Query query = wordsDatabase.orderByChild("displayDate").equalTo(choosenDate);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists()){
                                    fetchWordOfDay(buffer,choosenDate);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
//                        fetchWordOfDay(buffer,choosenDate);
                        Intent intent = new Intent(getApplicationContext(), ChosenDateActivity.class);
                        intent.putExtra("date", choosenDate);
                        startActivity(intent);

                    }
                }, year, month, day);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis()-1000);
                datePickerDialog.show();

            }
        });
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();
        wordsDatabase = firebaseDatabase.getReference("Word Of The Day").child(currentUserId);
        allWords = firebaseDatabase.getReference("Words");


        upButton = findViewById(R.id.words_upbtn);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.words_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Query query = wordsDatabase.orderByChild("displayDate").equalTo(date);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    buffer = dateFormat.format(calendar.getTime());
                    dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    date = dateFormat.format(calendar.getTime());
                    fetchWordOfDay(buffer,date);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void fetchWordOfDay(String buffer, String date) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
//        StringBuffer buffer = new StringBuffer(date);
//        buffer.reverse();
        String URL = "https://api.wordnik.com/v4/words.json/wordOfTheDay?date=" + buffer + "&api_key=" + api;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    String wrd = response.getString("word");

                    JSONArray defArray = response.getJSONArray("definitions");
                    JSONObject defObject = defArray.getJSONObject(0);
                    String def = defObject.getString("text");

                    JSONArray egArray = response.getJSONArray("examples");
                    JSONObject egObject = egArray.getJSONObject(0);
                    String eg = egObject.getString("text");

                    WordDetails wordDetails = new WordDetails();
                    wordDetails.setWord(wrd);
                    wordDetails.setDefination(Jsoup.parse(def).text());
                    wordDetails.setExamples(Jsoup.parse(eg).text());
                    wordDetails.setDisplayDate(date);

                    details.add(0,wordDetails);
                    String id = wordsDatabase.push().getKey();
                    wordsDatabase.child(id).setValue(wordDetails);

                    Words words = new Words();
                    words.setWord(wrd);
                    searchWords.add(words);
                    String searchId = allWords.push().getKey();
                    allWords.child(searchId).setValue(words);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tag","onErrorMessage:" + error.getMessage());
                VolleyLog.e("Error: ", error.getMessage());
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(WordsActivity.this);
                builder.setTitle(buffer)
                        .setMessage("Word of the day for date " + buffer +" will be shown after sometime.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.create().show();
            }
        });

        queue.add(jsonObjectRequest);
    }
}