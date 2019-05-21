package com.example.punchme;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class startWorkout extends AppCompatActivity{
    public static final String passNum = "SW2TS";

    public BlueToothControl bCon;
    private boolean connected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_workout);

        Intent intent = new Intent(this, BlueToothControl.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

        Button button = (Button) findViewById(R.id.GetStart);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTimerScreen();
            }
        });
    }

    public void openTimerScreen() {
        EditText editNum = (EditText) findViewById(R.id.WorkLength);
        int workTime = Integer.parseInt(editNum.getText().toString());

        //sends the start protocal
        bCon.sendMes("1");
        bCon.setWorkTime(workTime);

        Intent intent = new Intent(this, TimerScreen.class);
        intent.putExtra(passNum, workTime);
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

}

