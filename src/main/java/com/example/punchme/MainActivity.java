package com.example.punchme;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button WStart; //start workout button
    private Button WView; //view old workout button
    private Button WBlue;
    private ListView list;

    public BlueToothControl bCon;
    private boolean connected;

    //auto generated do not mess with
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //starts bluetooth
        Intent intent = new Intent(this, BlueToothControl.class);
        startService(intent);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        //sets an on click functionality for start workout
        WStart = (Button) findViewById(R.id.buttonStart);
        WStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity_start_Workout();
            }
        });

        //sets up on click for view workout
        WView = (Button) findViewById(R.id.buttonView);
        WView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity_View_Workout();
            }
        });

        //opens the bluettooth screen
        WBlue = (Button) findViewById(R.id.bButton);
        WBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChoices();
            }
        });

        list = findViewById(R.id.devs);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                bCon.pairUp(info, address);
            }
        });
    }

    //moves the code to the start workout page
    public void openActivity_start_Workout(){

        if(bCon.btConnected()) {
            Intent intent = new Intent(this, startWorkout.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(this,"Please Connect first",Toast.LENGTH_SHORT).show();
        }
    }

    //moves the code to the view workout page
    public void openActivity_View_Workout(){
        Intent intent = new Intent(this, ViewWorkout.class);
        intent.putExtra("from", "Main");
        startActivity(intent);
    }


    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            connected = true;
            BlueToothControl.LocalBinder mLocalbinder = (BlueToothControl.LocalBinder) service;
            bCon = mLocalbinder.getServerIntance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bCon = null;
            connected = false;
        }
    };

    //sets up the listview
    public void getChoices(){
        list.setAdapter(bCon.getPair());
    }
}

