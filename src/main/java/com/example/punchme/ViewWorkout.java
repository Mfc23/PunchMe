package com.example.punchme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ViewWorkout extends AppCompatActivity {
    private ArrayList<String[]> savedData;
    private int current = 0;

    public static final String passInfo = "vW2gD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_workout);

        //loads the data for the rest of the app
        loadData();

        //gets the name of the last activity
        Intent intent = getIntent();
        String previous = intent.getStringExtra("from");



        //if previous activity is result screen, gets list of strings and saves
        if(previous.equals("Result")) {
            String[] save = intent.getStringArrayExtra(ResultScreen.passSave);
            saveData(save);
        }

        //updates the display, if there are no entries yet, displays an error
        if(savedData.isEmpty()){
            TextView tempText = findViewById(R.id.curView);
            String string = "No values found";
            tempText.setText(string);
        }
        else display();

        //if back to main is pushed, opens backToMain
        Button back = (Button) findViewById(R.id.mainBut);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { backToMain();
            }
        });

        //if next button is pushed, opens next
        Button after = (Button) findViewById(R.id.nextButton);
        after.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { next();
            }
        });

        //if previous button is pushed, opens previous
        Button before = (Button) findViewById(R.id.PreButton);
        before.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { previous();
            }
        });
    }


    //loads the array list or makes a new one
    private void loadData(){
        //loads the data from shared prefrences
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Data List", null);
        Type type = new TypeToken<ArrayList<String[]>>() {}.getType();
        savedData = gson.fromJson(json,type);

        //if no data is found, makes a new arraylist
        if (savedData == null){
            savedData = new ArrayList<>();
        }
    }

    //saves the new incoming data
    private void saveData(String[] save){
        //adds the new information to the array list
        savedData.add(save);

        //saves the data
        SharedPreferences sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(savedData);
        editor.putString("Data List", json);
        editor.apply();
    }



    //displays the current workout
    private void display(){
        //gets the current list of data
        String[] display = savedData.get(current);

        //shows the user which input they are on
        String title = "Looking at value number: " + (current + 1);

        //links the length of each text view so they can be edited
        TextView curText = findViewById(R.id.curView);
        TextView dateText = findViewById(R.id.dateView);
        TextView totalText = findViewById(R.id.totView);
        TextView aveText = findViewById(R.id.maximView);
        TextView maxText = findViewById(R.id.roundView);
        TextView rateText = findViewById(R.id.hitView);
        TextView secText = findViewById(R.id.lengthView);

        //sets the text on the screen to the string values
        curText.setText(title);
        dateText.setText(display[0]);
        totalText.setText(display[1]);
        maxText.setText(display[2]);
        aveText.setText(display[3]);
        rateText.setText(display[4]);
        secText.setText(display[5]);
    }


    //moves to the next workout
    private void next(){
        //finds the max number the savedData can be
        int max = (savedData.size() - 1);

        //checks to make sure it is not on the last
        if(current == max){
            String toast = "No further entries.";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
            return;
        }
        //if it is not on the last one increaes current by one and updates display
        else {
            current = current + 1;
            display();
        }
    }

    //moves to the previous workout
    private void previous(){
        //checks to make sure it is not on the last
        if(current == 0){
            String toast = "No previous entries.";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
            return;
        }
        //if it is not on the last one lowers current by one and updates display
        else {
            current = current - 1;
            display();
        }
    }

    //sends the code back to the main page
    private void backToMain(){
        Intent nextPage = new Intent(this, MainActivity.class);
        startActivity(nextPage);
    }
}
