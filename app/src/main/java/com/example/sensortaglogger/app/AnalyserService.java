package com.example.sensortaglogger.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

import sample.ble.sensortag.BleSensorsRecordService;

/**
 * This class controls the classifier and saves the behaviour to file.
 * It DOESN'T do the analysis itself!
 */
public class AnalyserService extends Service {
    public AnalyserService() {
        Log.d(TAG, "Analyser instantiated!");
    }
    private static final String TAG = AnalyserService.class.getSimpleName();

    private ArrayList<SensorReading> accelReadings = new ArrayList<>();
    private ArrayList<SensorReading> gyroReadings = new ArrayList<>();

    private static final int ANALYSIS_INTERVAL = 10000; //10 secs
    private long lastAnalysis = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Analyser created!");
        registerReceiver(myReceiver, new IntentFilter(BleSensorsRecordService.NOTIFICATION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Analyser Service started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Analyser Destroyed");
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Milliseconds since last analysis
     */
    private long timeSinceAnalysis() {
        return System.currentTimeMillis() - lastAnalysis;
    }

    /**
     * Receives broadcasts of the raw data from BleSensorRecordService.
     */
    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getBundleExtra("bundle");
            float[] data = bundle.getFloatArray("data_float");
            //instantiate a SensorReading and  add it the the array of things to analyze.
            SensorReading sr = new SensorReading(bundle.getString("sensor_name"),
                    bundle.getLong("time"), data[0], data[1], data[2]);
            if (sr.sensorName.equals("Accelerometer")) {
                accelReadings.add(sr);
            } else if (sr.sensorName.equals("Gyroscope")) {
                gyroReadings.add(sr);
            }

            if (timeSinceAnalysis() > ANALYSIS_INTERVAL) {
                //TODO call an analysis
                lastAnalysis = System.currentTimeMillis();
                Log.d(TAG, "Doing an analysis.");
                accelReadings.clear();
                gyroReadings.clear();
            }
        }
    };
}
