package com.example.punchme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

public class ResultScreen extends AppCompatActivity {

    //the values gained from the last activity
    private ArrayList collection = new ArrayList();

    //the strings values to be displayed/saved
    private String tot;
    private String highest;
    private String ave;
    private String rate;
    private String seconds;

    //creates an array to be used to save the data
    private String[] save = new String[6];

    //needed for passing the save list
    public static final String passSave = "rS2vW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_screen);

        //gets the array list collection from the last screen
        Intent intent = getIntent();
        collection = intent.getStringArrayListExtra(BlueToothControl.passColl);
        printToScreen();

        //if yes is pressed save it before leaving
        Button save = (Button) findViewById(R.id.YButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { saveWorkout();
            }
        });

        //if no is pressed just go back to the main page
        Button leave = (Button) findViewById(R.id.NButton);
        leave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMain();
            }
        });
    }

    private void printToScreen(){
        //sets the up the strings that will be displayed
        tot = "Number of hits on the bag: " + collection.get(0);
        highest = "The highest G-Force was: " + collection.get(1) + "G";
        ave = "The average G-Force was: " + collection.get(2) + "G";
        rate = "The hits per second was: " + collection.get(3);
        seconds = "Over a period of " + collection.get(4) + " seconds.";

        //declares the text view
        TextView totalText;
        TextView aveText;
        TextView maxText;
        TextView rateText;
        TextView secText;

        //links the length of each text view so they can be edited
        totalText = findViewById(R.id.sizeView);
        aveText = findViewById(R.id.maxView);
        maxText = findViewById(R.id.aveView);
        rateText = findViewById(R.id.rateView);
        secText = findViewById(R.id.secView);

        //sets the text on the screen to the result values
        totalText.setText(tot);
        maxText.setText(highest);
        aveText.setText(ave);
        rateText.setText(rate);
        secText.setText(seconds);
    }

    //saves the workout before going back to view workout
    private void saveWorkout(){
        storeResults();

        Intent nextPage = new Intent(this, ViewWorkout.class);
        nextPage.putExtra("from", "Result");
        nextPage.putExtra(passSave, save);
        startActivity(nextPage);
    }

    //sends the code back to the main page
    private void backToMain(){
        Intent nextPage = new Intent(this, MainActivity.class);
        startActivity(nextPage);
    }

    //gets the date and stores the results in the phone
    private void storeResults(){
        //gets the current date
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance().format(calendar.getTime());

        //gets the current time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String time = timeFormat.format(calendar.getTime());

        //shows the user that the data was saved at the current time
        String toast = "Results taken on " + currentDate + " at " + time + " are saved.";
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

        //edits the string to fit more durring final display
        toast = "Results taken on " + currentDate + " at " + time;

        //adds all the strings into an array to be saved
        save[0] = toast;
        save[1] = tot;
        save[2] = highest;
        save[3] = ave;
        save[4] = rate;
        save[5] = seconds;
    }
}