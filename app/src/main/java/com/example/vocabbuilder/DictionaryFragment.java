package com.example.vocabbuilder;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class DictionaryFragment extends Fragment{

    Calendar calendar;
    SimpleDateFormat dateFormat;
    String date;
    RecyclerView recyclerView;
    List<WordDetails> details;
    Adapter adapter;
    String api = "r23z9iddrkwv17ayqm4l905rw9xb2so4aujqw17fawmh2dgoi";
    ImageButton favButton;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    DocumentReference documentReference;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference userDatabase, favRef, favWordsRef, markRef, markWordRef, allWords;
    List<String> keys;
    ProgressBar progressBar;
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
                        }, 5000);

    }

    private void showData() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                details = new ArrayList<>();
                adapter = new Adapter(details, keys);
                recyclerView.setAdapter(adapter);
                userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds:snapshot.getChildren()){
                            WordDetails data = ds.getValue(WordDetails.class);
                            details.add(data);
                            Collections.reverse(details);
                            String uid = ds.getKey();
                            keys.add(uid);
                            Collections.reverse(keys);
//                    Log.i("uid", uid);
//                    Toast.makeText(getContext(), "uid:"+uid, Toast.LENGTH_SHORT).show();
                        }
                        //adapter = new Adapter(details, keys);
                        //adapter.notifyItemInserted(0);
                        adapter.notifyDataSetChanged();

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        String currentUserId = firebaseUser.getUid();
//
//        documentReference = firebaseFirestore.collection("user").document(currentUserId);
//        userDatabase = firebaseDatabase.getReference("User Words").child(currentUserId);
//        // To check if favourite word is saved or not
//        favRef = firebaseDatabase.getReference("Favourites");
//        //Reference for favourite words
//        favWordsRef = firebaseDatabase.getReference("Favourites List").child(currentUserId);

    }

    //called before onCreateView
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        extractRandomWords();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_dictionary, container, false);
        keys = new ArrayList<>();
        details = new ArrayList<>();
        searchWords = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerView);
        favButton = view.findViewById(R.id.favbtn);
        progressBar = view.findViewById(R.id.dictionary_progress_bar);
        calendar = Calendar.getInstance();
//        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
//        SharedPreferences settings = getActivity().getSharedPreferences("PREFS",0);
//        int lastDay = settings.getInt("day", 0);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        date = dateFormat.format(calendar.getTime());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        documentReference = firebaseFirestore.collection("user").document(currentUserId);
        userDatabase = firebaseDatabase.getReference("User Words").child(currentUserId);

        favRef = firebaseDatabase.getReference("Favourites");
        favWordsRef = firebaseDatabase.getReference("Favourites List").child(currentUserId);

        markRef = firebaseDatabase.getReference("Marked");
        markWordRef = firebaseDatabase.getReference("Marked Words").child(currentUserId);

        allWords = firebaseDatabase.getReference("Words");


        //details = new ArrayList<>();

//        if(lastDay != currentDay){
//            SharedPreferences.Editor editor = settings.edit();
//            editor.putInt("day", currentDay);
//            editor.commit();
//            Toast.makeText(getContext(),   " currentdate: " + date, Toast.LENGTH_SHORT).show();
//            //extractRandomWords();
//        }

//        extractRandomWords();
        return view;
    }


    private void extractRandomWords() {

        RequestQueue queue = Volley.newRequestQueue(getContext());
        String URL ="https://api.wordnik.com/v4/words.json/randomWords?hasDictionaryDef=true&maxCorpusCount=-1&minDictionaryCount=1&maxDictionaryCount=-1&minLength=4&maxLength=-1&limit=1&api_key=" + api;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
//                Log.i("response", response.toString());
//                Toast.makeText(getContext(), response.toString(), Toast.LENGTH_SHORT).show();
                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject randomWords = response.getJSONObject(i);
                        String val = randomWords.getString("word");
                        //Log.i("RandomWord", val);
                        //Toast.makeText(getContext(), val, Toast.LENGTH_SHORT).show();
                        //randomWordList.add(val);
//                        new Timer().schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                // this code will be executed after 500 mseconds
//                                fetchDefination(val);
//                            }
//                        }, 500);
                        fetchDefination(val);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tag","onErrorMessage:" + error.getMessage());
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        queue.add(jsonArrayRequest);
    }



    private void fetchDefination(String wrd) {

        final String[] def = new String[1];

        RequestQueue queue = Volley.newRequestQueue(getContext());
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

        RequestQueue queue = Volley.newRequestQueue(getContext());
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

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = firebaseUser.getUid();

        documentReference = firebaseFirestore.collection("user").document(currentUserId);
        userDatabase = firebaseDatabase.getReference("User Words").child(currentUserId);

        favRef = firebaseDatabase.getReference("Favourites");
        favWordsRef = firebaseDatabase.getReference("Favourites List").child(currentUserId);

        WordDetails wordDetails = new WordDetails();
        wordDetails.setWord(wrd);
        wordDetails.setDefination(def);
        wordDetails.setExamples(ex);
        wordDetails.setDisplayDate(date);
        details.add(0,wordDetails);
        String id = userDatabase.push().getKey();
        userDatabase.child(id).setValue(wordDetails);

        Words words = new Words();
        words.setWord(wrd);
        searchWords.add(words);
        String searchId = allWords.push().getKey();
        allWords.child(searchId).setValue(words);

        //Toast.makeText(getContext(), "word added in firebase", Toast.LENGTH_SHORT).show();



        //details.add(0,wordDetails);
        //adapter = new Adapter(details);
        //adapter.notifyItemInserted(0);
        //adapter.notifyDataSetChanged();
        //adapter.updateWordList(details);



        //recyclerView.setAdapter(adapter);


    }

}