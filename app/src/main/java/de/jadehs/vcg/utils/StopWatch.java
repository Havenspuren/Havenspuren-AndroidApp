package de.jadehs.vcg.utils;

import android.util.Log;

public class StopWatch {
    private static final String TAG = "StopWatch";

    private static long startTime;
    public static void start(){
        startTime = System.nanoTime();
    }

    public static double duration(){
        return (System.nanoTime()-startTime)/1000000D;
    }

    public static void logDuration(String prefix){
        Log.d(TAG, "logDuration: " + prefix + " " + duration());
    }
}
