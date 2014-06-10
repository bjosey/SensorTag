package com.example.sensortaglogger.app;

/**
 * Represents a single sensor reading.
 */
public class SensorReading implements Comparable {

    //who cares about getters and setters. Just make them all public!
    public String sensorName;
    public long time;
    public float x;
    public float y;
    public float z;

    public SensorReading(String sensorName, long time, float x, float y, float z) {
        this.sensorName = sensorName;
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getMagnitude() {
        return (float)Math.sqrt(x*x + y*y + z*z);
    }

    public int compareTo(Object o) {
        //comparator to sort by time
        SensorReading s = (SensorReading)o;
        return (int)(this.time - s.time);
    }

    /**
     * Only useful for accelerometer.
     * Calculates the inclination. Eg the device in standing up = 90 degrees.
     * Lying down (either way) = 0 degrees. Upside down = -90 degrees.
     */
    public float calcInclination() {
        if (getMagnitude() == 0.0) {
            return 0;
        }
       double theta = Math.toDegrees(Math.acos(y / getMagnitude()));
        return (float)(90.0 - theta);
    }
}
