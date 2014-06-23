package com.kutysam.androidprofiler;

import java.io.BufferedReader;

import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.util.Log;

import com.android.internal.os.PowerProfile;

public final class PowerValues {	
	private static String TAG = "AndroidProfiler";
	public static double none, cpuidle, cpuawake, wifiscan, wifion, wifiactive, gpson, bluetoothon, bluetoothactive, bluetoothat, screenon, screenfull, radioscanning, radioactive, radioon, dspaudio, dspvideo, batterycapacity, cpuSpeedSteps;
	public static Double[] cpuactives;
	public static Integer[] cpuspeeds;
	public static ArrayList<Integer> allcpuspeeds;
	public static double[] cpupower; 
	public static long battVoltage;
	public static double currCpuPower = 0; //Watts
	public static int currCpuSpeed;
	public boolean descending = false;
	public static int maxCpuSpeed = 0;
	PowerValues(PowerProfile pp){
		allcpuspeeds = new ArrayList<Integer>();
		none = pp.getAveragePower(PowerProfile.POWER_NONE);
		cpuidle = pp.getAveragePower(PowerProfile.POWER_CPU_IDLE);
		cpuawake = pp.getAveragePower(PowerProfile.POWER_CPU_AWAKE);
		wifiscan = pp.getAveragePower(PowerProfile.POWER_WIFI_SCAN);
		wifion = pp.getAveragePower(PowerProfile.POWER_WIFI_ON);
		wifiactive = pp.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE);
		gpson = pp.getAveragePower(PowerProfile.POWER_GPS_ON);
		bluetoothon = pp.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON);
		bluetoothactive = pp.getAveragePower(PowerProfile.POWER_BLUETOOTH_ACTIVE);
		bluetoothat = pp.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD);
		screenon = pp.getAveragePower(PowerProfile.POWER_SCREEN_ON);
		screenfull = pp.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
		radioscanning = pp.getAveragePower(PowerProfile.POWER_RADIO_SCANNING);
		radioactive = pp.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE);
		radioon = pp.getAveragePower(PowerProfile.POWER_RADIO_ON);
		dspaudio = pp.getAveragePower(PowerProfile.POWER_AUDIO);
		dspvideo = pp.getAveragePower(PowerProfile.POWER_VIDEO);
		batterycapacity = pp.getBatteryCapacity();
		cpuSpeedSteps = pp.getNumSpeedSteps(); 	//REMEMBER TO DO MAPPING!
        cpuspeeds = new Integer[(int) cpuSpeedSteps];
        cpuactives = new Double[(int) cpuSpeedSteps];
		for (int i = 0; i < cpuSpeedSteps; i++) {
            cpuspeeds[i] = (int) pp.getAveragePower(PowerProfile.POWER_CPU_SPEEDS, i);
            cpuactives[i] = pp.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, i);
        }
		//cpuspeeds[0] = 1200000;
		//cpuspeeds[1] = 1000000;
		Arrays.sort(cpuspeeds);
		Arrays.sort(cpuactives);
		setAllCpuSpeeds();
		setCpuPower();
		setVoltage();
		printCPUValues();
	}
	
	private void setAllCpuSpeeds(){
		try{
			BufferedReader br = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state"));
			String line;
			while ((line = br.readLine()) != null) {
				 String[] nums = line.split(" ");
				 int currReadSpeed =Integer.parseInt(nums[0]); 
				 allcpuspeeds.add(currReadSpeed);
				 if(maxCpuSpeed < currReadSpeed)
					 maxCpuSpeed = currReadSpeed;
			}
			if(allcpuspeeds.get(0) < allcpuspeeds.get(allcpuspeeds.size()-1))	//0 , 1 , 2
				descending = true;
			else
				descending = false;
			Collections.sort(allcpuspeeds);
			
			if(descending == true){
				Collections.reverse(allcpuspeeds);
				Arrays.sort(cpuspeeds, Collections.reverseOrder());
				Arrays.sort(cpuactives, Collections.reverseOrder());
			}
			
			br.close();
		}catch(Exception e){
			Log.e("PowerValues", "Error in retrieving cpuspeeds");
		}
	}
	
	private static void setCpuPower(){
		//Error in computation. We need to set all again.
		cpupower = new double[maxCpuSpeed+1];	//Max 5.0Ghz
		if(cpuspeeds.length != allcpuspeeds.size() || checkIfValuesAreSame() == false){
			Log.e("PowerValues", "CPU Speeds not set properly. Will need to recalculate");
			long minSpeed = cpuspeeds[0];
			long maxSpeed = cpuspeeds[cpuspeeds.length-1];
			double minPower = cpuactives[0];
			double maxPower = cpuactives[cpuactives.length-1];
			double oneHzPower = (minPower+maxPower) / (minSpeed+maxSpeed);
			for(int i =0; i < allcpuspeeds.size(); i++){
				cpupower[allcpuspeeds.get(i)] = allcpuspeeds.get(i) * oneHzPower;
				
				//System.out.println("" + allcpuspeeds.get(i) + " " + cpupower[allcpuspeeds.get(i)] + " ");
			}
		}
		else{	//All are the same, we just set our array for hashing.
			Log.e("PowerValues", "CPU Speeds are correctly set already!");
			for(int i = 0; i < allcpuspeeds.size(); i++){
				cpupower[allcpuspeeds.get(i)] = cpuactives[i];
				System.out.println("Power: " + allcpuspeeds.get(i) + " " + cpupower[allcpuspeeds.get(i)] + " ");
			}
		}
		setCurrCpuPower();
	}
	
	private static boolean checkIfValuesAreSame(){
		for(int i = 0; i < cpuspeeds.length ; i ++){
			if(cpuspeeds[i] - allcpuspeeds.get(i) !=0){
				System.out.println("WRONG: " + cpuspeeds[i] + " " + allcpuspeeds.get(i) + " " +(allcpuspeeds.get(i) - cpuspeeds[i]));
				return false;
			}
		}
		
		return true;
	}
	
	public static double getCpuPower(int x){
		return cpupower[x];
	}
	
	
	public static void setVoltage(){
		try {
			BufferedReader br = new BufferedReader(new FileReader("/sys/class/power_supply/battery/voltage_now"));
			battVoltage = Long.parseLong(br.readLine())/1000;
			br.close();
		} catch (IOException e) {
			Log.e(TAG,"ERROR IN RETRIEVING VOLTAGE");
			battVoltage = 3700;	//Default
		}
	}
	
    public static String getWatt(double watt){
    	if(watt < 1)
    		return (new DecimalFormat("#").format(watt*1000) + "mW");
    	else
    		return (new DecimalFormat("#.##").format(watt) + "W");
    			
    }
    
    public static String getJoule(double joule){
    	if(joule < 1)
    		return (new DecimalFormat("#").format(joule*1000) + "mJ");
    	else
    		return (new DecimalFormat("#.##").format(joule) + "J");
    }
    
    //Sets the current power in mW based on the current cpu speed.
	public static void setCurrCpuPower(){
		String cpuInfo = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
		double powerval;
		try{
			BufferedReader br = new BufferedReader(new FileReader(cpuInfo));
			currCpuSpeed = Integer.parseInt(br.readLine());
			powerval = PowerValues.getCpuPower(currCpuSpeed);
			br.close();
		}catch(Exception e){
			Log.e("PowerValues","Error in retreiving cpu stats info");
			powerval = 0;
		}
		currCpuPower = powerval;
	}
	
	//Returns in mW
	public static double getCurrCPUWatt(){
		return currCpuPower * (double)battVoltage/1000.0;	//ma * v
	}
	
	//Helper func
	public static void printCPUValues(){
		for(int i = 0; i < cpuspeeds.length; i++){
			System.out.println(""+cpuspeeds[i] + " " +cpuactives[i]);
		}
	}
}