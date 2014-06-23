package com.kutysam.androidprofiler;

import java.util.Map;
import java.util.TreeMap;

public class ProcessCpu {
	//Output temporary
	String birdIW;
	
	String ocpuIW;
	String ocpuAW;
	
	double cpuJ;
	double cpuIW;
	double cpuAW;
	
	long cpuOverallTime;	//in ms
	long currCpuStdByTime;
	long cpuStdbyTime;
	double cpuStdbyPower;
	ProcessCpu(){
		cpuJ = 0;
		cpuIW = 0;
		cpuAW = 0;
	}
	
	public void updateCPUUsage(TreeMap<Integer,UIDProcess> uidtm, double currMW, double counter){
		long time = 0;
		double power = 0;
		
		for(Map.Entry<Integer,UIDProcess> entry : uidtm.entrySet()) {
			UIDProcess up = entry.getValue();
			up.cpuIW = 0;
			TreeMap<Integer,PIDProcess> utm = entry.getValue().utm;
			for(Map.Entry<Integer,PIDProcess> entry1 : utm.entrySet()) {
				PIDProcess p = entry1.getValue(); 
				p.updateCPUTime(currMW);
				time += p.intervalCPUUpdateTime;
				power += p.intervalCPUUpdatePower;
				up.sinceLaunchCPUTime += p.intervalCPUUpdateTime;
				up.cpuIW += p.intervalCPUUpdatePower;
			}
//			if(up.name.equals("Angry Birds") == true){
	//			birdIW += ","+Math.round(up.cpuIW * 1000);
		//	}
			up.cpuJ += up.cpuIW;
			up.cpuAW = up.cpuJ / counter;
			up.totalJ += up.cpuIW;
		}		
		
		cpuStdbyTime = 1000 - time*10;
		if(cpuStdbyTime < 0){	//cases where the device is run on a super slow phone this may happen
			cpuStdbyTime = 0;
		}
		//System.out.println("ABC: " + cpuStdbyTime + " " + time);
		cpuOverallTime += (time*10);
		
		//seconds * a * v = J
		cpuStdbyPower = cpuStdbyTime / 1000.0 * PowerValues.cpuawake / 1000.0 * PowerValues.battVoltage/ 1000.0;
		
		cpuIW = power + cpuStdbyPower;
		//ocpuIW += ","+ Math.round(cpuIW*1000);
		
		cpuJ += cpuIW;
		
		cpuAW = cpuJ / counter;
		//ocpuAW += ","+ Math.round(cpuAW*1000);
	}
}