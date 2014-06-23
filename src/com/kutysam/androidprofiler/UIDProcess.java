package com.kutysam.androidprofiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class UIDProcess {
	int uid;
	String TAG = "UIDProcess";
	String name, fullName;
	Drawable icon;
	
	TreeMap<Integer,PIDProcess> utm;

	
	//POWER 
	//Since Launch CPU Time
	//Total CPU Power = since launch cpu power
	//Interval CPUPower = last second update
	//Average CPUPower = average from time since started.
	long sinceLaunchCPUTime;
	double cpuJ, cpuIW, cpuAW;
	PackageManager pm;
	
	//Total Power
	double totalJ;
	
	//Screen [Foreground]
	long screenTime;
	double screenIW, screenAW, screenJ;
	
	//Wifi
	long wTotalBytes, wCurrBytes, wRefBytes, wOrigBytes, wCurrPackets;
	double wCurrIW, wCurrAW, wCurrJ, wCurrTotalW;
	
	//Mobile
	long mTotalBytes, mCurrBytes, mRefBytes, mOrigBytes, mCurrPackets;
	double mCurrIW, mCurrAW, mCurrJ, mCurrTotalW;
	
	
	UIDProcess(int uid, PackageManager pm){
		this.uid = uid;
		sinceLaunchCPUTime = 0;
		cpuJ = 0;
		cpuIW = 0;
		totalJ = 0;
		
		utm = new TreeMap<Integer,PIDProcess>();
		
		this.pm = pm;
		
		initNameIcon();
		initWifi();
	}
	
	public void addPID(int pid,PIDProcess p){
		utm.put(pid, p);
	}

	private void initNameIcon(){
		try {
			this.name = (String) pm.getApplicationLabel(pm.getApplicationInfo(pm.getNameForUid(this.uid),0));
			this.fullName = (String) pm.getNameForUid(this.uid);
			this.icon = pm.getApplicationIcon(pm.getNameForUid(this.uid));
		} catch (NameNotFoundException e) {
			Log.e(TAG,"Name not found, Using default icon");
			switch(this.uid){
			case 0:
				this.name = "ROOT";
				break;
			case 1000:
				this.name = "AID_SYSTEM";
				break;
			case 1001:
				this.name = "AID_RADIO";
				break;
			case 1002:
				this.name = "AID_BLUETOOTH";
				break;
			case 1003:
				this.name = "AID_GRAPHICS";
				break;
			case 1004:
				this.name = "AID_INPUT";
				break;
			case 1005:
				this.name = "AID_AUDIO";
				break;
			case 1006:
				this.name = "AID_CAMERA";
				break;
			case 1007:
				this.name = "AID_LOG";
				break;
			case 1008:
				this.name = "AID_COMPASS";
				break;
			case 1009:
				this.name = "AID_MOUNT";
				break;
			case 1010:
				this.name = "AID_WIFI";
				break;
			case 1011:
				this.name = "AID_ADB";
				break;
			case 1012:
				this.name = "AID_INSTALL";
				break;
			case 1013:
				this.name = "AID_MEDIA";
				break;
			case 1014:
				this.name = "AID_DHCP";
				break;
			case 2000:
				this.name = "AID_SHELL";
				break;
			case 2001:
				this.name = "AID_CACHE";
				break;
			case 2002:
				this.name = "AID_DIAG";
				break;
			default:
				if(pm.getNameForUid(this.uid) == null)
					this.name = "*" + this.uid;
				else 
					this.name = pm.getNameForUid(this.uid);
				break;
			}
			//this.icon = null;	//SET TO SYSTEM ICON?
		}
	}

	public void initWifi(){
		try {
			BufferedReader in = new BufferedReader(new FileReader("/proc/uid_stat/" + uid + "/stat/tcp_rcv"));
			wOrigBytes = Long.parseLong(in.readLine()); 
		    in.close();
		    
			in = new BufferedReader(new FileReader("/proc/uid_stat/" + uid + "/stat/tcp_snd"));
			wOrigBytes += Long.parseLong(in.readLine()); 
		    in.close();
		} catch (IOException e) {	//No File!
			wOrigBytes = 0;
		}
	}

}
 /*
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