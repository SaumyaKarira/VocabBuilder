package com.example.vocabbuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
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
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChosenWordActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ImageButton upButton;
    RecyclerView recyclerView;
    String word;
    List<WordDetails> details;
    Adapter adapter;
    String api = "r23z9iddrkwv17ayqm4l905rw9xb2so4aujqw17fawmh2dgoi";
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference allWords, userDatabase;
    List<String> keys;
    List<Words> searchWords;
    Calendar calendar;
    SimpleDateFormat dateFormat;
    String date, fetchedWord;

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
        }, 5000);

    }

    private void showData() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                details = new ArrayList<>();
                adapter = new Adapter(details, keys);
                recyclerView.setAdapter(adapter);
                Query query = userDatabase.orderByChild("word").equalTo(fetchedWord);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            details.clear();
                            for (DataSnapshot ds : snapshot.getChildren()) {
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                progressBar.setVisibility(View.GONE);
            }
        });
        }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chosen_word);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();
        allWords = firebaseDatabase.getReference("Words");
        userDatabase = firebaseDatabase.getReference("User Words").child(currentUserId);

        details = new ArrayList<>();
        keys = new ArrayList<>();
        searchWords = new ArrayList<>();

        progressBar = findViewById(R.id.chosen_wrd_progress_bar);
        upButton = findViewById(R.id.chosen_word_upbtn);
        recyclerView = findViewById(R.id.chosen_word_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        date = dateFormat.format(calendar.getTime());

        Intent incomingIntent = getIntent();
        fetchedWord = incomingIntent.getStringExtra("word");

        Query query = userDatabase.orderByChild("word").equalTo(fetchedWord);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    details.clear();
                    for (DataSnapshot ds : snapshot.getChildren()) {
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
                fetchDefination(fetchedWord);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fetchDefination(String wrd) {

        final String[] def = new String[1];

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String URL = "https://api.wordnik.com/v4/word.json/" + wrd +"/definitions?limit=5&includeRelated=false&useCanonical=false&includeTags=false&api_key=" + api;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
//                Log.i("response", response.toString());
//                Toast.makeText(getContext(), "def response:"+response.toString(), Toast.LENGTH_SHORT).show();
                try {
                    int i = 0;
                    while (i < response.length() && def[0] == null) {
                        JSONObject wordDefination = response.getJSONObject(0);
                        def[0] = wordDefination.getString("text");
                        if(def[0] != null){break;}
                        //Log.i("wordDef", def[0]);
                        //Toast.makeText(getContext(), "wrd def:" + wrd +"- " + def[0], Toast.LENGTH_SHORT).show();
                        i++;
                    }
//                    new Timer().schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            // this code will be executed after 500 mseconds
//                            fetchExample(wrd, Jsoup.parse(def[0]).text());
//                        }
//                    }, 500);
                    fetchExample(wrd, Jsoup.parse(def[0]).text());
                    //setDetails(wrd,def[0],"eg");
                } catch(JSONException e){
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
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(120000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonArrayRequest);
    }

    private void fetchExample(String wrd, String def) {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String URL = "https://api.wordnik.com/v4/word.json/" + wrd + "/topExample?useCanonical=false&api_key=" + api;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

//                Log.i("eg response", response.toString());
//                Toast.makeText(getContext(), "eg response:"+response.toString(), Toast.LENGTH_SHORT).show();

                try {
                    String ex = response.getString("text");
                    //Log.i("worEg", ex);
                    //Toast.makeText(getContext(), "wrd eg:" + wrd +"- " + ex, Toast.LENGTH_SHORT).show();
                    setDetails(wrd,def,Jsoup.parse(ex).text());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(120000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(jsonObjectRequest);
    }

    private void setDetails(String wrd, String def, String ex) {

        WordDetails wordDetails = new WordDetails();
        wordDetails.setWord(wrd);
        wordDetails.setDefination(def);
        wordDetails.setExamples(ex);
        wordDetails.setDisplayDate(date);
        details.add(wordDetails);
        String id = userDatabase.push().getKey();
        userDatabase.child(id).setValue(wordDetails);

        Words words = new Words();
        words.setWord(wrd);
        searchWords.add(words);
        String searchId = allWords.push().getKey();
        allWords.child(searchId).setValue(words);

        //progressBar.setVisibility(View.VISIBLE);



    }
}