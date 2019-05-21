package com.example.punchme;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

public class tempInput extends AppCompatActivity {

    private int workTime; //the workout length
    private ArrayList collection = new ArrayList(); //final list of strings
    private ArrayList inputs = new ArrayList(); //list of integers for input


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_input);

        //gets worktime for the last function
        Intent intent = getIntent();
        workTime = intent.getIntExtra(startWorkout.passNum, 0);

        //triggers addInput everytime add button is pressed
        Button addIN = (Button) findViewById(R.id.addBut);
        addIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addInput();
            }
        });

        //opens openResult once done is pressed
        Button button = (Button) findViewById(R.id.DoneBut);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openResult();
            }
        });
    }

    //this gets the values ready to be passed then moves to the
    //final screen
    private void openResult(){
        getValues();
        goNext();
    }

    //passes collection and moves to the result screen
    private void goNext(){
        //Intent nextPage = new Intent(this, ResultScreen.class);
        //nextPage.putExtra(passColl, collection);
        //startActivity(nextPage);
    }


    //gets the values from the array and turns them into strings for passing
    private void getValues(){
        double hitPsec = 0;
        double total = 0;
        double max = 0;
        int size = inputs.size(); //the size of the array, or number of hits
        double average = 0;

        for (int i = 0; i < size; i++){
            //adds up all the values to a total
            total = (double) inputs.get(i) + total;

            //finds the maximum value in the array
            if (max < (double) inputs.get(i))
                max = (double) inputs.get(i);
        }

        //calculates the average energy
        average =  total / size;

        //calculates the hits per second
        hitPsec = (double) size / workTime;

        //puts max, average, and hits per second into two decimal format
        String maxStr = new DecimalFormat("#.##").format(max);
        String ave = new DecimalFormat("#.##").format(average);
        String HPS = new DecimalFormat("#.##").format(hitPsec);

        //puts all the final values into the collection to be shown off
        collection.add(0, size);
        collection.add(1, maxStr);
        collection.add(2, ave);
        collection.add(3, HPS);
        collection.add(4, workTime);
    }

    //puts the double into inputs and clears the edit text field when add is pressed
    private void addInput(){
        EditText editNum = (EditText) findViewById(R.id.InField);
        double newNum = Double.parseDouble(editNum.getText().toString());

        inputs.add(newNum);

        editNum.setText("");

        String toast = "Value of " + newNum + " added";
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }
}
