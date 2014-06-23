package com.kutysam.androidprofiler;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import com.android.internal.os.PowerProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Handler;

public class ProcessController  extends Thread {
	//Android-related Classes
	private PackageManager pm;
	private ConnectivityManager cm;
	
	//Timer Codes
	Timer timer;	//Other Timer default pollRate 1sec
	TimerTask timerTask;
	long timerVal = 1000;
	int counter = 0;	//To keep track of time
	
	//Own Codes
	public ProcessScreen pScreen;
	public ProcessWifi pcw;
	public ProcessCpu pCPU;
	

	public TreeMap<Integer,PIDProcess> pidtm;
	public TreeMap<Integer,UIDProcess> uidtm;
	public ArrayList<UIDProcess> GUIuid;
	//GUI TOP DISPLAYS
	public double allIW, allAW, allJ;	

	private double currCPUMW;
	@SuppressWarnings("unused")
	private String TAG = "ProcessController";
	

	
	//Handler for update GUI purposes.
	private Handler h;
	
	ProcessController(PackageManager pm, Context c, Handler h, ArrayList<UIDProcess> GUIuid){
		this.pm = pm;
		this.pCPU = new ProcessCpu();
		this.pScreen = new ProcessScreen(c);
		this.pcw = new ProcessWifi();
		this.cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		new PowerValues(new PowerProfile(c));
		this.h = h;
		this.GUIuid = GUIuid;
	}
		
	//Function to initialize
	public void run(){
		
		//gc.resetGUI();
		allIW = 0;
		allAW = 0;
		allJ = 0;
		counter = 0;
		uidtm = new TreeMap<Integer, UIDProcess>();
		pidtm = new TreeMap<Integer, PIDProcess>();
		
		/*
		 * Scan all PID and initialize them [ send in treemap of valid UID ]
		 * It will also initiliaze any new UID if there is
		 * In intializing the UID, it will set the TCP Send / Receive Bytes
		 * We then initialize the WIFI Packets being sent / received
		*/
		int[] abc = new int[99999];
		abc = android.os.Process.getPids("/proc", abc);
		
		for(int i = 0; i<abc.length; i++){
			if(abc[i] == -1){
				break;	//End of processes
			}
			int uidID =  android.os.Process.getUidForPid(abc[i]);
			PIDProcess newPID = new PIDProcess(uidID, abc[i], true, uidtm, pm, GUIuid);
			pidtm.put(abc[i],newPID);
		}
		//----End Of PID INITIALIZATION----
		
    	timer = new Timer();
        timerTask = new TimerTask(){
        	public void run(){        		
        		PowerValues.setVoltage();
        		PowerValues.setCurrCpuPower();	//set the latest cpu speed + watts
        		counter++;	//increase by 1 second.
        		currCPUMW = PowerValues.getCurrCPUWatt();
        		
        		//1ststep: Update all PID / UID
        		int[] abc = new int[99999];
        		abc = android.os.Process.getPids("/proc", abc);
        		
        		for(int i = 0; i<abc.length; i++){
        			if(abc[i] == -1){
        				break;	//End of processes
        			}
        			if(pidtm.containsKey(abc[i]) == false){
	        			int uidID =  android.os.Process.getUidForPid(abc[i]);
	        			
	        			PIDProcess newPID = new PIDProcess(uidID, abc[i], false, uidtm, pm, GUIuid);
	        			pidtm.put(abc[i],newPID);
        			}
        		}

        		//2nd step: Scan all UID's PID to see and update CPUChanges.
        		pollCPUUsage();
        		
        		//3rd step: Update Screen Usage Power
        		pollScreenUsage();
        		
        		//4th step: Update Wifi Usage Power || 3g Usage Power
        		if(cm.getActiveNetworkInfo() != null){
	        		if(cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI){
	        			pollWifiUsage();
	        		}
	        		else if(cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE)
	        			pollMobileData();
        		}
        		//6th step: Calculate totals
        		calculateTotal();
        		
        		//7th step: Update GUI
        		h.sendEmptyMessage(0);
        	}
        };
     	timer.scheduleAtFixedRate(timerTask,timerVal,timerVal);
		return ;
		
		
	}
	
	//Function to stop when user has stopped reference
	public void halt(){
		timerTask.cancel();
		timer.cancel();
	}
	
	public void pollCPUUsage(){
		pCPU.updateCPUUsage(uidtm,currCPUMW,counter);
	}
	
	public void pollWifiUsage(){
		pcw.updateWifiUsage(uidtm, counter);
	}
	
	public void pollMobileData(){
	}
	
	public void pollScreenUsage(){
		pScreen.updateScreenUsage(uidtm,counter);
	}
	
	private void calculateTotal(){
		allIW = pCPU.cpuIW + pScreen.screenIW + pcw.wStandByIW + pcw.wTotalIW;
		allJ += allIW;
		allAW = allJ / counter;
	}
}