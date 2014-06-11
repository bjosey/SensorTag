package com.example.sensortaglogger.app;

import com.example.sensortaglogger.app.behaviour.Behaviour;

/**
 * Created by Brendan on 11/06/2014.
 */
public class BehaviourSummary {

    public String name;
    public long length;
    public int numSteps;

    /**
     * Used for ActivitySummaryAdapter. Represents *multiple* Behaviours over time.
     * @param name
     * @param length
     * @param numSteps
     */
    public BehaviourSummary(String name, long length, int numSteps) {
        this.name = name;
        this.length = length;
        this.numSteps = numSteps;
    }

    public String getName() {
        return name;
    }

    public String getLength() {
        return getFriendlyTime(length / 1000);
    }

    public String getNumSteps() {
        if (name.equals("Walking") || name.equals("Running")) {
            return Integer.toString(numSteps) + " steps";
        }
        return "";
    }

    private String getFriendlyTime(long seconds) {
        long diffInSeconds = seconds;
        StringBuffer sb = new StringBuffer();

        long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
        long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
        long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
        long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
        long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
        long years = (diffInSeconds = (diffInSeconds / 12));

        if (years > 0) {
            if (years == 1) {
                sb.append("1 year");
            } else {
                sb.append(years + " years");
            }
            if (years <= 6 && months > 0) {
                if (months == 1) {
                    sb.append(", 1 month");
                } else {
                    sb.append(", " + months + " months");
                }
            }
        } else if (months > 0) {
            if (months == 1) {
                sb.append("1 month");
            } else {
                sb.append(months + " months");
            }
            if (months <= 6 && days > 0) {
                if (days == 1) {
                    sb.append(", 1 day");
                } else {
                    sb.append(", " + days + " days");
                }
            }
        } else if (days > 0) {
            if (days == 1) {
                sb.append("1 day");
            } else {
                sb.append(days + " days");
            }
            if (days <= 3 && hrs > 0) {
                if (hrs == 1) {
                    sb.append(", 1 hour");
                } else {
                    sb.append(", " + hrs + " hours");
                }
            }
        } else if (hrs > 0) {
            if (hrs == 1) {
                sb.append("1 hour");
            } else {
                sb.append(hrs + " hours");
            }
            if (min > 1) {
                sb.append(", " + min + " minutes");
            }
        } else if (min > 0) {
            if (min == 1) {
                sb.append("1 minute");
            } else {
                sb.append(min + " minutes");
            }
            if (sec > 1) {
                sb.append(", " + sec + " seconds");
            }
        } else {
            if (sec == 1) {
                sb.append("1 second");
            } else {
                sb.append(sec + " seconds");
            }
        }

        return sb.toString();
    }


}
