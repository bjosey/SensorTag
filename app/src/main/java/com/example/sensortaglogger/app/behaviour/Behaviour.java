package com.example.sensortaglogger.app.behaviour;

/**
 * Created by Brendan on 10/06/2014.
 */
public class Behaviour {

    public String name;
    public long length;

    /**
     *
     * @param name Description of the behaviour. For possible values see AnalyserEngine
     * @param length The analysis interval. See ANALYSIS_INTERVAL in AnalyserEngine.class
     */
    public Behaviour(String name, long length) {
        this.name = name;
        this.length = length;
    }
}
