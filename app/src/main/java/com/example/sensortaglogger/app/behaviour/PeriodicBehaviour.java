package com.example.sensortaglogger.app.behaviour;

/**
 * Created by Brendan on 10/06/2014.
 */
public class PeriodicBehaviour extends Behaviour {

    public int numSteps;

    /**
     * Used for running or walking. See Behaviour.class for more info.
     * @param numSteps Number of steps taking.
     */
    public PeriodicBehaviour(String name, long length, int numSteps) {
        super(name, length);
        this.numSteps = numSteps;
    }
}
