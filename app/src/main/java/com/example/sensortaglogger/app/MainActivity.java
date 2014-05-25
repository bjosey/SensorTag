package com.example.sensortaglogger.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import sample.ble.sensortag.BleSensorsRecordService;
import sample.ble.sensortag.sensor.TiAccelerometerSensor;


public class MainActivity extends Activity {

    private TextView txtAccel;
    MyResultReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultReceiver = new MyResultReceiver(null);
        Intent intent = new Intent(this, BleSensorsRecordService.class);
        intent.putExtra("receiver", resultReceiver);
        this.startService(intent);

        txtAccel = (TextView)findViewById(R.id.txtAccel);
        txtAccel.setText("No data!!");
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

    class UpdateUI implements Runnable {
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
        }
    }


    class MyResultReceiver extends ResultReceiver {
        public MyResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            //send the data through to the UI thread for processing
            //method taken from http://www.lalit3686.blogspot.in/2012/06/how-to-update-activity-from-service.html
            if (resultCode == 0) {
                runOnUiThread(new UpdateUI(resultData));
            }
        }
    }
}
