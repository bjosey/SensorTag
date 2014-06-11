package com.example.sensortaglogger.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
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


public class MainActivity extends Activity {

    private TextView txtAccel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtAccel = (TextView)findViewById(R.id.txtAccel);
        txtAccel.setText("No data!!");

        final ListView lv = (ListView) findViewById(R.id.ListView01);
        constructSummaries();
        lv.setAdapter(new ActivitySummaryAdapter(this, summaries));

        getActionBar().setTitle("Activity Monitor");


        startService(new Intent(MainActivity.this, AnalyserService.class));
    }

    private ArrayList<BehaviourSummary> summaries;

    private void constructSummaries() {
        summaries = new ArrayList<BehaviourSummary>();
        //TODO grab fresh data from SQLite database. For now just give zeroes.
        BehaviourSummary bs1 = new BehaviourSummary("Sitting", 0, 0);
        BehaviourSummary bs2 = new BehaviourSummary("Standing", 0, 0);
        BehaviourSummary bs3 = new BehaviourSummary("Walking", 0, 0);
        BehaviourSummary bs4 = new BehaviourSummary("Running", 0, 0);
        summaries.add(bs1);
        summaries.add(bs2);
        summaries.add(bs3);
        summaries.add(bs4);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(myReceiver, new IntentFilter(BleSensorsRecordService.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(myReceiver);
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


    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //send the data through to the UI thread for processing
            Bundle bundle = intent.getBundleExtra("bundle");
            runOnUiThread(new UpdateUI(bundle));
        }
    };
}
