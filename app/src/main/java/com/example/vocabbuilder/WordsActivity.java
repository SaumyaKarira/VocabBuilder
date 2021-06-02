package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    ImageButton upButton;
    Calendar calendar;
    SimpleDateFormat dateFormat;
    String date,buffer;
    String api = "r23z9iddrkwv17ayqm4l905rw9xb2so4aujqw17fawmh2dgoi";
    List<WordDetails> details = new ArrayList<>();
    Adapter adapter;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference wordsDatabase;
    List<String> keys = new ArrayList<>();
    ProgressBar progressBar;

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
        }, 3000);

    }

    private void showData() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Query query = wordsDatabase.orderByChild("displayDate").equalTo(date);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
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

//                wordsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        for(DataSnapshot ds:snapshot.getChildren()){
//                            WordDetails data = ds.getValue(WordDetails.class);
//                            details.add(0,data);
//                            String uid = ds.getKey();
//                            keys.add(0,uid);
////                    Log.i("uid", uid);
////                    Toast.makeText(getContext(), "uid:"+uid, Toast.LENGTH_SHORT).show();
//                        }
//                        adapter = new Adapter(details, keys);
//                        adapter.notifyItemInserted(0);
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

        progressBar = findViewById(R.id.words_progress_bar);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();
        wordsDatabase = firebaseDatabase.getReference("Word Of The Day").child(currentUserId);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        date = dateFormat.format(calendar.getTime());

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
                    fetchWordOfDay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void fetchWordOfDay() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        buffer = dateFormat.format(calendar.getTime());
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


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tag","onErrorMessage:" + error.getMessage());
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        queue.add(jsonObjectRequest);
    }
}