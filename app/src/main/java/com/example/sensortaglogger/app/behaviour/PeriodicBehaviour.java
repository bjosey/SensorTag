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
    public PeriodicBehaviour(String name, long interval, int numSteps) {
        super(name, interval);
        this.numSteps = numSteps;
    }

    @Override
    public String toString() {
        return String.format("%s (%d steps) for %d seconds.", this.name, this.numSteps, this.interval / 1000);
    }
}
