package com.example.sensortaglogger.app.behaviour;

/**
 * Created by Brendan on 10/06/2014.
 */
public class Behaviour {

    public String name;
    public long interval;
    public int numSteps;

    /**
     *
     * @param name Description of the behaviour. For possible values see AnalyserEngine
     * @param interval The analysis interval. See ANALYSIS_INTERVAL in AnalyserEngine.class
     */
    public Behaviour(String name, long interval, int numSteps) {
        this.name = name;
        this.interval = interval;
        this.numSteps = numSteps;
    }

    @Override
    public String toString() {
        if (name.equals("Walking") || name.equals("Running")) {
            return String.format("%s (%d steps) for %d seconds.", this.name, this.numSteps, this.interval / 1000);
        }
        return String.format("%s for %d seconds.", this.name, this.interval / 1000);
    }
}
