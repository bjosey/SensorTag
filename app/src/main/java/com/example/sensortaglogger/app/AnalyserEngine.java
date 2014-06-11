package com.example.sensortaglogger.app;

import android.util.Log;

import com.example.sensortaglogger.app.behaviour.Behaviour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Brendan on 10/06/2014.
 */
public class AnalyserEngine {

    private ArrayList<SensorReading> gyroReadings;
    private ArrayList<SensorReading> accelReadings;
    private static final String TAG = AnalyserService.class.getSimpleName();

    //minimum readings to classify behaviour.
    private static final int MIN_READINGS = 15;
    //thresshold for classifying as movement.
    private static final float MOVEMENT_THRESHOLD = 38.0F;
    // threshold for classifying as running.
    private static final double THRESHOLD_RUNNING = 1.3;

    //Enum of the possible activities
    private static final String ACTIVITY_NONE = "None";
    private static final String ACTIVITY_SITTING =  "Sitting";
    private static final String ACTIVITY_STANDING =  "Standing";
    private static final String ACTIVITY_WALKING =  "Walking";
    private static final String ACTIVITY_RUNNING =  "Running";

    // Exponentially Weighted Moving Average, to calculate steps
    private static final double ALPHA = 0.1;
    private static String PREVIOUS_ACTIVITY = ACTIVITY_NONE;
    private static boolean INITIALISED_MOVING_AVERAGE = false;
    private static double PREVIOUS_MOVING_AVERAGE = 0;
    private static double PREVIOUS_ACCELERATION = 0;
    private List<Double> average;

    public AnalyserEngine(ArrayList<SensorReading> accelReadings, ArrayList<SensorReading> gyroReadings) {
        this.gyroReadings = gyroReadings;
        this.accelReadings = accelReadings;
        this.average = new ArrayList<>();
        Collections.sort(accelReadings);
        Collections.sort(gyroReadings);
    }

    /**
     * Determines whether the person has been upright the majority of the time,
     * or horizontal.
     * @return True if upright, false otherwise
     */
    private boolean isUpright() {
        //How's it work? If the accel indicates the device is >45degrees more than it's not, it'll say it is upright. Pretty simple
        int numUpright = 0;
        int numHoriz = 0;

        for (SensorReading sr : accelReadings) {
            if (Math.abs(sr.calcInclination()) > 45.0) {
                numUpright++;
            } else {
                numHoriz++;
            }
        }
        Log.v(TAG, "Num upright: " + Integer.toString(numUpright) + ", Num horiz:" + Integer.toString(numHoriz));
        return numUpright > numHoriz;
    }

    /**
     * Uses the gyroscope to determine if movement is occuring.
     */
    private boolean isMovement() {
        double sum = 0.0;
        for (SensorReading sr : gyroReadings) {
            sum += sr.getMagnitude();
        }
        float avg = (float)(sum / (float)gyroReadings.size());
        Log.v(TAG, "Average gyro is: " + Float.toString(avg));
        return avg > MOVEMENT_THRESHOLD;
    }

    private void calculateAverage() {
        if (!PREVIOUS_ACTIVITY.equals(ACTIVITY_WALKING) && !PREVIOUS_ACTIVITY.equals(ACTIVITY_RUNNING)) {
            // Initialise the moving average to the first accelerometer magnitude.
            PREVIOUS_MOVING_AVERAGE = accelReadings.get(0).getMagnitude();
        }

        double prevMovAvg = PREVIOUS_MOVING_AVERAGE;

        for (SensorReading sr : accelReadings) {
            double movAvg = ALPHA * sr.getMagnitude() + (1 - ALPHA) * prevMovAvg;
            average.add(movAvg);
            prevMovAvg = movAvg;
        }

        // Store the previous value of the moving average, for future calculations.
        PREVIOUS_MOVING_AVERAGE = prevMovAvg;
    }

    /**
     * Calculates the number of steps (or running paces) taken during this sample.
     * Should only be called if behaviour is thought to be walking or running
     * @return Number of steps/paces
     */
    private int calcSteps() {
        //Will likely use zero-crossing or something simple.
        //TODO: Not yet implemented

        int steps = 0;

        // Determine starting point for the data.
        int i;
        double prevAcc;
        if (PREVIOUS_ACTIVITY.equals(ACTIVITY_WALKING) || PREVIOUS_ACTIVITY.equals(ACTIVITY_RUNNING)) {
            // Continue the step counting from previous readings.
            i = 0;
            prevAcc = PREVIOUS_ACCELERATION;
        } else {
            // Start from current data
            i = 1;
            prevAcc = accelReadings.get(0).getMagnitude();
        }

        for (; i < accelReadings.size(); ++i) {
            double acc = accelReadings.get(i).getMagnitude();
            double movAvg = average.get(i);

            // Check if a step occurred
            if (prevAcc < movAvg && acc >= movAvg) {
                ++steps;
            }

            prevAcc = acc;
        }

        PREVIOUS_ACCELERATION = prevAcc;

        return steps;
    }

    private String determineMovement() {
        // Calculate average of moving average, then classify activity
        double sum = 0;
        for (double acc : average) {
            sum += acc;
        }

        double avg = sum / average.size();

        Log.v(TAG, "Average Movement: " + Double.toString(avg));

        if (avg >= THRESHOLD_RUNNING) {
            return ACTIVITY_RUNNING;
        }

        return ACTIVITY_WALKING;
    }

    /**
     * Returns their determined behaviour based upon the data given during the constructor
     */
    public Behaviour analyse()
    {

        if (accelReadings.size() < MIN_READINGS) { //not sufficient readings.
            PREVIOUS_ACTIVITY = ACTIVITY_NONE;
            return new Behaviour(ACTIVITY_NONE, AnalyserService.ANALYSIS_INTERVAL, 0);
        }


        if (!isUpright()) { //They're sitting. No further analysis needed.
            PREVIOUS_ACTIVITY = ACTIVITY_SITTING;
            return new Behaviour(ACTIVITY_SITTING, AnalyserService.ANALYSIS_INTERVAL, 0);
        }

        //Okay, then we're Standing, Walking or Running


        if (!isMovement()) { //No significant movement. Probably standing.
            PREVIOUS_ACTIVITY = ACTIVITY_STANDING;
            return new Behaviour(ACTIVITY_STANDING, AnalyserService.ANALYSIS_INTERVAL, 0);
        }

        //Okay, Walking or Running
        calculateAverage();
        //TODO: Differentiate between Walking and Running
        String activity = determineMovement();
        PREVIOUS_ACTIVITY = activity;
        return new Behaviour(activity, AnalyserService.ANALYSIS_INTERVAL, calcSteps());
    }
}
