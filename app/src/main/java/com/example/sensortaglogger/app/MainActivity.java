package com.example.sensortaglogger.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.content.BroadcastReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import sample.ble.sensortag.BleSensorsRecordService;
import sample.ble.sensortag.sensor.TiAccelerometerSensor;
import sample.ble.sensortag.sensor.TiGyroscopeSensor;


public class MainActivity extends Activity {

    private TextView txtAccel;
    private TextView txtGyro;
    private TextView txtStatus;
    private TextView txtStatusLed;
    private TextView txtDebug;
    private ListView listview;
    private ActivitySummaryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtAccel = (TextView)findViewById(R.id.txtAccel);
        txtGyro = (TextView)findViewById(R.id.txtGyro);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        txtStatusLed = (TextView)findViewById(R.id.txtLed);
        txtDebug = (TextView)findViewById(R.id.txtDebug);
        txtAccel.setText("x=+0.000000\ny=+0.000000\nz=+0.000000");
        txtGyro.setText("x=+0.000000\ny=+0.000000\nz=+0.000000");

        listview = (ListView) findViewById(R.id.ListView01);

        DatabaseHelper dh = new DatabaseHelper(this);
        ArrayList<BehaviourSummary> summaries = dh.getAllSummaries();

        adapter = new ActivitySummaryAdapter(this, summaries);
        listview.setAdapter(adapter);

        getActionBar().setTitle("Activity Monitor");


        startService(new Intent(MainActivity.this, AnalyserService.class));
    }

   /* private ArrayList<BehaviourSummary> summaries;

    private void constructSummaries() {
        summaries = new ArrayList<BehaviourSummary>();
        BehaviourSummary bs1 = new BehaviourSummary("Sitting", 0, 0);
        BehaviourSummary bs2 = new BehaviourSummary("Standing", 0, 0);
        BehaviourSummary bs3 = new BehaviourSummary("Walking", 0, 0);
        BehaviourSummary bs4 = new BehaviourSummary("Running", 0, 0);
        summaries.add(bs1);
        summaries.add(bs2);
        summaries.add(bs3);
        summaries.add(bs4);
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myReceiver, new IntentFilter(BleSensorsRecordService.NOTIFICATION));
        registerReceiver(myStatusReceiver, new IntentFilter(BleSensorsRecordService.STATUS_NOTIFICATION));
        registerReceiver(myAnalysisReceiver, new IntentFilter(AnalyserService.NEW_ANALYSIS));
        //ask BSRS what the scanning status is
        Intent intent = new Intent(BleSensorsRecordService.STATUS_REQUEST);
        sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
        unregisterReceiver(myStatusReceiver);
        unregisterReceiver(myAnalysisReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ArrayList<String> sdBuffer = new ArrayList<>();

    public class UpdateUI implements Runnable {
        Bundle bundle;

        public UpdateUI(Bundle bundle) {
            this.bundle = bundle;
        }

        public void run() {
            String uuid = bundle.getString("sensor_uuid");
            String text = bundle.getString("text");
            if (uuid.equals(TiAccelerometerSensor.UUID_SERVICE)) {
                txtAccel.setText(text);
            } else if (uuid.equals(TiGyroscopeSensor.UUID_SERVICE)) {
                txtGyro.setText(text);
            }
            float[] data = bundle.getFloatArray("data_float");
            String jsonString = String.format("{\"time\":\"%d\", \"sensor\":\"%s\", \"x\":\"%f\", \"y\":\"%f\", \"z\":\"%f\"}",
                    System.currentTimeMillis(), bundle.getString("sensor_name"), data[0], data[1], data[2]);

            //only save to file if there's 100 entries waiting.
            sdBuffer.add(jsonString);
            if (sdBuffer.size() > 100) {
                //create the file & dir
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/ActivityMon");
                dir.mkdirs();
                File file = new File(dir, "log.txt");
                try {
                    FileOutputStream f = new FileOutputStream(file, true);
                    //now write each line to file
                    for (String line : sdBuffer) {
                        f.write(line.getBytes());
                        f.write("\r\n".getBytes());
                    }
                    f.close();
                } catch (IOException ex){
                    ex.printStackTrace();
                }
                //clear the buffer arraylist
                sdBuffer.clear();
            }
        }
    }

    private BroadcastReceiver myAnalysisReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String behav = intent.getStringExtra("description");
            runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    DatabaseHelper dh = new DatabaseHelper(MainActivity.this);
                    ArrayList<BehaviourSummary> summaries = dh.getAllSummaries();
                    adapter.setSummaries(summaries);
                }
            });
        }
    };


    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //send the data through to the UI thread for processing
            Bundle bundle = intent.getBundleExtra("bundle");
            runOnUiThread(new UpdateUI(bundle));
        }
    };

    private BroadcastReceiver myStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //send the data through to the UI thread for processing
            final String status = intent.getStringExtra("status");
            final int color = intent.getIntExtra("color", 0);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText(status);
                    txtStatusLed.setTextColor(color);
                    if (!txtStatus.getText().equals("Connected")) {
                        txtAccel.setText("x=+0.000000\ny=+0.000000\nz=+0.000000");
                        txtGyro.setText("x=+0.000000\ny=+0.000000\nz=+0.000000");
                    }
                }
            });
        }
    };
}
