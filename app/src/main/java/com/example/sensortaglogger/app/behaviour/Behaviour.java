package com.example.sensortaglogger.app.behaviour;

/**
 * Created by Brendan on 10/06/2014.
 */
public class Behaviour {

    public String name;
    public long interval;

    /**
     *
     * @param name Description of the behaviour. For possible values see AnalyserEngine
     * @param interval The analysis interval. See ANALYSIS_INTERVAL in AnalyserEngine.class
     */
    public Behaviour(String name, long interval) {
        this.name = name;
        this.interval = interval;
    }

    @Override
    public String toString() {
        return String.format("%s for %d seconds.", this.name, this.interval / 1000);
    }
}
