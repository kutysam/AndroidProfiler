package com.kutysam.androidprofiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import android.content.pm.PackageManager;
import android.util.Log;

public class PIDProcess {
	int uid, pid;
	boolean dead;
	String name;
	String TAG = "PIDProcess";
	//Total time = sys + usr
	//Launch time = sys + usr of when timeAppStarted
	//IntervalUpdateTime = Total - Launch - LastUpdateTime	[Shows the last second power / time]
	//LastUpdateTime = last total time before going out of this function.
	
	//totalCPUPower = since launch, whats the power
	//intervalCPUUpdatePower = last interval update.
	
	long totalCPUTime, launchCPUTime, intervalCPUUpdateTime, lastCPUUpdateTime;
	double totalCPUPower, intervalCPUUpdatePower;	//In Joules!
	ArrayList<UIDProcess> GUIuid;
	TreeMap<Integer, UIDProcess> tm;
	
	//Constructor for intializing a NEW pid
	PIDProcess(int uid, int pid, boolean init, TreeMap<Integer,UIDProcess> tm, PackageManager pm, ArrayList<UIDProcess> GUIuid){
		this.uid = uid;
		this.pid = pid;
		this.name = getName();
		this.tm = tm;
		this.GUIuid = GUIuid;
		
		dead = false;
		if(init == true){
			setLaunchTime();
		}
		else if(init == false){
			launchCPUTime = 0;
		}
		attachToUID(pm);
	}
	
	private void setLaunchTime(){	//Only ran when PID is being initialized	
		launchCPUTime = getSysUsrTime();
	}
	
	private void attachToUID(PackageManager pm){
		UIDProcess u = tm.get(this.uid);
		if(u == null){
			u = new UIDProcess(this.uid, pm);
			tm.put(this.uid, u);
			GUIuid.add(u);
		}
		
		u.addPID(this.pid, this);	//Add this pid to the uid's treemap.
	}
	
	
	public void updateCPUTime(double CPUPowerMW){	//Includes updating CPUPower
		if(dead == true || lastCPUUpdateTime == -1){ //Process has alr been destroyed and will not come back under the same PID
			return ;
		}
		
		long sysUsrTime = getSysUsrTime();
		
		if(sysUsrTime == -1){	//Process is destroyed and will not come back under the same PID
			intervalCPUUpdateTime = 0;
			intervalCPUUpdatePower = 0;
		}else{
			totalCPUTime = sysUsrTime;
			
			intervalCPUUpdateTime = totalCPUTime - launchCPUTime - lastCPUUpdateTime;
			lastCPUUpdateTime += intervalCPUUpdateTime;
			
			if(intervalCPUUpdateTime == 0){
				intervalCPUUpdatePower = 0;
			}
			else{
				intervalCPUUpdatePower = PowerCalculator.calculateCPUJoule(intervalCPUUpdateTime*10, CPUPowerMW);
			}
			totalCPUPower += intervalCPUUpdatePower;
		}
	}
	
	private String getName(){
		String[] splited = null;
	    String read = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader("/proc/" + pid + "/stat"));
		    while ((read = in.readLine()) != null) {
		        splited = read.split(" ");
		    }
		    in.close();
		    return (splited[1].substring(1, splited[1].length()-1));
		} catch (IOException e) {	//No File!
			Log.e(TAG,"Error in retrieving name");
			return "Error in Name";
		}
	}
	
	private long getSysUsrTime(){
		String[] splited = null;
	    String read = null;
		try {
			BufferedReader in = new BufferedReader(new FileReader("/proc/" + pid + "/stat"));
		    while ((read = in.readLine()) != null) {
		        splited = read.split(" ");
		    }
		    in.close();
		    return Long.parseLong(splited[13]) + Long.parseLong(splited[14]);
		} catch (IOException e) {	//No File!
			dead = true;
			Log.e(TAG,"Error in systemUsrTime" + " " + name + " " + pid);
			return -1;
		}
	}
	
	
	
}

/*
PCT INIT()
Scan all PID, Check against a validPID array.
If validPID contains, leave it.

If validPID doesn't exist, create a new PID
	-Assign launchCPUTime.
	-Assign its UID.
		-Check if UID exist.
		-If not, create a new UID and a new Mapping to its pid within the UID Object.
	
PCT UpdateUIDPID();
Scan all PID. Check against a validPID array.
If validPID contains, leave it.
If validPID doesn't exist, create a new PID
	- Assign launchCPUTime to be 0.
	- Assign its UID.
		-Check if UID exist.
		-If not, create a new UID and a new Mapping to its pid within the UID Object.

 
 PCT PollCPU();
Scan through all the VALIDPID! ,NOT the pids got from the function.
Update Each PID CPUPower and CPUTime. At the same time, update the UIDProcess too.
- This means, we HAVE to send the VALIDUID Hash Array inside.


 PCT PollCPU();
 
 * Scan thru all UID
 * Everytime we scan 1 uid, reset the UID's intervalCPUUpdateTime and intervalCPUUpdatePower first
 * Then scan every PID within the UID and send in THIS UID Object
 * The PID Function will update its total, Last and Interval.	//If the pid file doesn't exist, set the interval to be 0
 * After updating, it will update the UID object such that
 * UIDTotalTime += PIDintervalCPUUpdate
 * UIDTotalCPUPOWER += PIDintervalCPUPower
 * UIDIntervalCPUTime += PIDintervalCPUTime
 * UIDIntervalCPUPower += PIDintervalCPUPower
 * -Scanned all PID Finished for 1 UID-
 * Within the UID, we have the UPDATED total CPU time, total cpu power, interval also.
 * Continue to next PID.
 */