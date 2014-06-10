package com.example.sensortaglogger.app;

import com.example.sensortaglogger.app.behaviour.Behaviour;
import com.example.sensortaglogger.app.behaviour.PeriodicBehaviour;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Brendan on 10/06/2014.
 */
public class AnalyserEngine {

    private ArrayList<SensorReading> gyroReadings;
    private ArrayList<SensorReading> accelReadings;

    //minimum readings to classify behaviour.
    private static final int MIN_READINGS = 15;

    //Enum of the possible activities
    private static String ACTIVITY_NONE = "None";
    private static String ACTIVITY_SITTING =  "Sitting";
    private static String ACTIVITY_STANDING =  "Standing";
    private static String ACTIVITY_WALKING =  "Walking";
    private static String ACTIVITY_RUNNING =  "Running";

    public AnalyserEngine(ArrayList<SensorReading> accelReadings, ArrayList<SensorReading> gyroReadings) {
        this.gyroReadings = gyroReadings;
        this.accelReadings = accelReadings;
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
        return numUpright > numHoriz;
    }


    /**
     * Determines if there's some periodic components involved. Is indicative
     * of walking or running.
     */
    private boolean isPeriodic() {
        //TODO: Not yet implemented
        return false;
    }

    /**
     * Calculates the number of steps (or running paces) taken during this sample.
     * Should only be called if behaviour is thought to be walking or running
     * @return Number of steps/paces
     */
    private int calcSteps() {
        //Will likely use zero-rossing or something simple.
        //TODO: Not yet implemented
        return 0;
    }

    /**
     * Returns their determined behaviour based upon the data given during the constructor
     */
    public Behaviour analyse()
    {

        if (accelReadings.size() < MIN_READINGS) { //not sufficient readings.
            return new Behaviour(ACTIVITY_NONE, AnalyserService.ANALYSIS_INTERVAL);
        }


        if (!isUpright()) { //They're sitting. No further analysis needed.
            return new Behaviour(ACTIVITY_SITTING, AnalyserService.ANALYSIS_INTERVAL);
        }

        //Okay, then we're Standing, Walking or Running


        if (!isPeriodic()) { //Nothing periodic. Probably standing.
            return new Behaviour(ACTIVITY_STANDING, AnalyserService.ANALYSIS_INTERVAL);
        }

        //Okay, Walking or Running
        //TODO: Differentiate between Walking and Running
        return new PeriodicBehaviour(ACTIVITY_WALKING, AnalyserService.ANALYSIS_INTERVAL,
                calcSteps());

    }
}
