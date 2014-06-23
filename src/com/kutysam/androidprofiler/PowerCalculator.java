package com.kutysam.androidprofiler;

import java.util.concurrent.TimeUnit;

public final class PowerCalculator {
	//private static String TAG = "PowerCalculator";
	//JouleCalculator (timeused = 1000 means 10 second.) 
	//Returns Joule
	
	//FOR CPU ONLY!
	
	//CPU. timeused = in miliseconds.
	//Returns Joules.
	public static double calculateCPUJoule(long miliseconds, double miliWatt){
		return (double)miliseconds/1000 * miliWatt/1000;	//Seconds x Watts
	}
	
	//WIFI
	public static double calculateWIFIJoule(long timeUsed, double miliAmps, float milivolt){
		return (float)timeUsed/1000 * (float)miliAmps/1000 * (float)milivolt/1000;
	}
	
	public static String timeCalculator(long milisecond){
		long seconds = milisecond / 1000;        
        //long hours = TimeUnit.SECONDS.toHours(seconds);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);
		//return hours + "h " + minute + "m "+ second + "s";
        return minute + "m "+ second + "s";
	}
}