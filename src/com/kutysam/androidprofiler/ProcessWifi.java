package com.kutysam.androidprofiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import android.util.Log;

public class ProcessWifi {
	//Only for Wifi
	double wStandByIW;
	double wStandByAW;
	double wStandByTOTALW;
	double wStandByJ;
	
	long currentPackets;	//Current Packets
	long refPackets;	//Number of packets since ref set. (total - orig)
	long totalPackets;	//Total number of packets
	long origPackets;	//Packets at the reference point
	double wPacketIW;
	double wPacketAW;
	double wPacketJ;
	
	double wTotalIW;
	double wTotalAW;
	double wTotalJ;

	long changedBytes;	//From latest update within processes
	private ArrayList<UIDProcess> changedProcess;
	
	long oneWifiPacketInterval = 210;	//210milisecond
	
	long rxPackets;
	long txPackets;
	BufferedReader bufferedReader;
	Process ps;
	private String TAG = "AndroidProfiler";

	
	ProcessWifi(){
		origPackets = 0;
		totalPackets = 0;
		refPackets = 0;
		currentPackets = 0;
		changedProcess = new ArrayList<UIDProcess> ();
		
	}
	
	public void initWifi(TreeMap<Integer,UIDProcess> uidtm){
		try {
			BufferedReader in = new BufferedReader(new FileReader("/sys/class/net/wlan0/statistics/rx_packets"));
			rxPackets = Long.parseLong(in.readLine()); 
		    in.close();
		    
			in = new BufferedReader(new FileReader("/sys/class/net/wlan0/statistics/tx_packets"));
			txPackets += Long.parseLong(in.readLine()); 
		    in.close();
		    
		    origPackets = txPackets + rxPackets;
		    
			for(Map.Entry<Integer,UIDProcess> entry : uidtm.entrySet()) {
				UIDProcess up = entry.getValue();
				up.initWifi();
			}
		    
		    
		    
		} catch (IOException e) {	//No File!
			origPackets = 0;
		}
	}
	
	public void updateWifiUsage(TreeMap<Integer,UIDProcess> uidtm, double counter){
		try{
			long standbyTime = 0;
			long activeTime = 0;
			BufferedReader in = new BufferedReader(new FileReader("/sys/class/net/wlan0/statistics/rx_packets"));
			totalPackets = Long.parseLong(in.readLine()); 
		    in.close();
		    
			in = new BufferedReader(new FileReader("/sys/class/net/wlan0/statistics/tx_packets"));
			totalPackets += Long.parseLong(in.readLine()); 
		    in.close();
			
			currentPackets = totalPackets - origPackets - refPackets;
			refPackets = totalPackets - origPackets;
			
			wStandByIW = 0;
			wTotalIW = 0;
			
			//1st step: Set all IW and AW to proper values for ALL UID
			//2nd step: Check if packets 0 then update standby mode
			//3rd step: If packets not 0, scan ALL UID for change of bytes. Which ever has byte change, add to ChangedProcess
			//4th step: if changedbytes != 0 , we check ratio and then set each CHANGEPROCESS IW / AW.
			//5th step: Set overall standby and set overall active.
			//6th step: if changedbytes == 0, we set active and standby accordingly.
			
			//1st step: Set all IW and AW to proper values for ALL UID
			for(Map.Entry<Integer,UIDProcess> entry : uidtm.entrySet()) {
				UIDProcess up = entry.getValue();
				up.wCurrAW = up.wCurrJ / counter;
				up.wCurrIW = 0;
				up.wCurrBytes = 0;
				up.wCurrPackets = 0;
			}
			
			//2nd step: Check if packets 0 then update standby mode			
			if(currentPackets == 0){
				wStandByIW = PowerCalculator.calculateWIFIJoule(1000, PowerValues.wifion , PowerValues.battVoltage);
				wStandByJ += wStandByIW;
			}
			
			
			//3rd step: If packets not 0, scan ALL UID for change of bytes. Which ever has byte change, add to ChangedProcess
			else{
				changedProcess.clear();
				changedBytes = 0;
				//Now we set the reference of all bytes within each process
				for(Map.Entry<Integer,UIDProcess> entry : uidtm.entrySet()) {
					UIDProcess up = entry.getValue();
					
					try{
						in = new BufferedReader(new FileReader("/proc/uid_stat/" + up.uid + "/tcp_rcv"));
						rxPackets = Long.parseLong(in.readLine()); 
					    in.close();
					    
						in = new BufferedReader(new FileReader("/proc/uid_stat/" + up.uid + "/tcp_snd"));
						txPackets += Long.parseLong(in.readLine()); 
					    in.close();
					    
					    up.wTotalBytes = rxPackets + txPackets;
						up.wCurrBytes = up.wTotalBytes - up.wOrigBytes - up.wRefBytes;
						up.wRefBytes = up.wTotalBytes - up.wOrigBytes;
						
					}catch(IOException e){
						up.wTotalBytes = 0;
						up.wCurrBytes = 0;
						up.wRefBytes = 0;
					}
						
					if(up.wCurrBytes != 0){	//There are changes to this process.
						changedBytes += up.wCurrBytes;
						changedProcess.add(up);
					}
				}
				
				//4th step: if changedbytes != 0 , we check ratio and then set each CHANGEPROCESS IW / AW.
				if(changedBytes != 0){
					standbyTime = standbyTimeCalculator(currentPackets);
					if(standbyTime != 0){
						wStandByIW = PowerCalculator.calculateWIFIJoule(standbyTime, PowerValues.wifion , PowerValues.battVoltage);
						wStandByJ += wStandByIW;
						wStandByAW = wStandByJ / counter;
					}
					double bytesPerPacket = changedBytes / currentPackets;
					for(int i = 0; i < changedProcess.size(); i++){
						changedProcess.get(i).wCurrPackets = (int) (changedProcess.get(i).wCurrBytes / bytesPerPacket);
						//Log.e(TAG, (int) (processList[changedProcess.get(i)].wCurrBytes / bytesPerPacket) + " " + currentPackets);
						packetPowerCalculator(changedProcess.get(i).wCurrPackets, currentPackets, changedProcess.get(i));
						changedProcess.get(i).wCurrJ += changedProcess.get(i).wCurrIW;
						changedProcess.get(i).wCurrAW = changedProcess.get(i).wCurrJ / counter;
						
						//wTotalIW += changedProcess.get(i).wCurrIW;
						//wTotalJ += changedProcess.get(i).wCurrIW;
						changedProcess.get(i).totalJ += changedProcess.get(i).wCurrIW;
					}
					
					if(wTotalIW == 0){	//Packets are too small in size till its 0.1 etc. We will ignore the packets that a specific app sent.
						activeTime = 1000 - standbyTime;
						//wTotalIW = PowerCalculator.calculateWIFIJoule(activeTime, PowerValues.wifiactive , PowerValues.battVoltage);
						//wTotalJ += wTotalIW;
						wStandByAW = wStandByJ / counter;
					}
					
					
					wTotalAW = wTotalJ / counter;
				}else{//6th step: if changedbytes == 0, we assume that its udp packets, we can't update any process but we can update overall instead.
					standbyTime = standbyTimeCalculator(currentPackets);
					if(standbyTime != 0){
						wStandByIW = PowerCalculator.calculateWIFIJoule(standbyTime, PowerValues.wifion , PowerValues.battVoltage);
						wStandByJ += wStandByIW;
						wStandByAW = wStandByJ / counter;
					}
					
					activeTime = 1000 - standbyTime;
					
					//wTotalIW = PowerCalculator.calculateWIFIJoule(activeTime, PowerValues.wifiactive , PowerValues.battVoltage);
					//wTotalJ += wTotalIW;
					//wTotalAW = wTotalJ / counter;
				}
				activeTime = 1000 - standbyTime;
				wTotalIW = PowerCalculator.calculateWIFIJoule(activeTime, PowerValues.wifiactive , PowerValues.battVoltage);
				wTotalJ += wTotalIW;
				wTotalAW = wTotalJ / counter;
			}
		}catch(IOException e){
			Log.e(TAG,"IOException");
		}
	}
	
	//Returns the standby time
	private long standbyTimeCalculator(long noPacket){
		if(noPacket <= 4){
			return 1000 - oneWifiPacketInterval;
		}else if(noPacket <= 8){
			return 1000 - oneWifiPacketInterval * 2;
		}else if(noPacket <= 12){
			return 1000 - oneWifiPacketInterval * 3;
		}else if(noPacket <= 18){
			return 1000 - oneWifiPacketInterval * 4;
		}else{
			return 0;
		}
	}
	
	//Sets the power consumption of that specific process's last second.
	private void packetPowerCalculator(long noPacket, long totalPackets, UIDProcess p){
		float ratio = (float) noPacket / (float) totalPackets;
		//210MS
		if((float)totalPackets <= 4){
			p.wCurrIW = ratio * PowerCalculator.calculateWIFIJoule(oneWifiPacketInterval, PowerValues.wifiactive , PowerValues.battVoltage);
		}else if((float)totalPackets <= 8){	//420MS
			p.wCurrIW = ratio * PowerCalculator.calculateWIFIJoule(oneWifiPacketInterval * 2, PowerValues.wifiactive , PowerValues.battVoltage);
		}else if((float)totalPackets <= 12){	//630MS
			p.wCurrIW = ratio * PowerCalculator.calculateWIFIJoule(oneWifiPacketInterval * 3, PowerValues.wifiactive , PowerValues.battVoltage);
		}else if((float)totalPackets <= 18){	//840MS
			p.wCurrIW = ratio * PowerCalculator.calculateWIFIJoule(oneWifiPacketInterval * 4, PowerValues.wifiactive , PowerValues.battVoltage);
		}else{
			p.wCurrIW = ratio * PowerCalculator.calculateWIFIJoule(1000, PowerValues.wifiactive , PowerValues.battVoltage);
		}
		//Log.e(TAG, "CURRENT IW " + p.wCurrIW + "\nx3 " + oneWifiPacketInterval * 3 + "\nWIFIACTIVVE: " +  PowerValues.wifiactive + "\nBATT VOLT " + PowerValues.battVoltage + "\nCalculated: " + PowerCalculator.calculateWIFIJoule(oneWifiPacketInterval * 3, PowerValues.wifiactive , PowerValues.battVoltage));
	}
}