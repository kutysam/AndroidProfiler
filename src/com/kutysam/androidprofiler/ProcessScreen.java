package com.kutysam.androidprofiler;

import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class ProcessScreen {
	private Context c;
	private PowerManager pm;
	private ActivityManager am;
	private String TAG = "ProcessScreen";
	
	public double screenIW;
	public double screenAW;
	public double screenJ;
	
	ProcessScreen(Context context){
		c = context;
		pm = (PowerManager) c.getSystemService(Activity.POWER_SERVICE);
		am = (ActivityManager) c.getSystemService(Activity.ACTIVITY_SERVICE);
	}
	
	public void updateScreenUsage(TreeMap<Integer,UIDProcess> uidtm, double counter){	//Screen is confirmed switched on.
		updateALLUid(uidtm, counter);
		
		int fgUID = getForeGroundUID();
		
		if(fgUID != -1){	//Update screen usage nowwww
			UIDProcess u = uidtm.get(fgUID);
			if(u==null)
				System.out.println("NULLLLLLLLL");
			try {
				double curBrightnessValue;
				int brightnessMode = Settings.System.getInt(c.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
				
				//Default autobrightness to 50%
				if (brightnessMode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
					curBrightnessValue = 255.0 / 2;
				}
				else{
					curBrightnessValue = Settings.System.getInt(c.getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
				}
				
				double currMA = (curBrightnessValue / 255.0) * PowerValues.screenfull + PowerValues.screenon;
				
				u.screenIW = PowerCalculator.calculateWIFIJoule(1000, currMA, PowerValues.battVoltage);
				u.screenJ += u.screenIW;	//Update total. Since polling interval is 1second, we are safe.
				u.screenTime += 1;
				
				u.screenAW = u.screenJ / counter;
				
				//System.out.println(u.screenJ + " " + u.name);
				
				//UPDATE Totals
				screenIW = u.screenIW;
				screenJ += u.screenIW;
				screenAW = screenJ / counter;
				u.totalJ += u.screenIW;
			} catch (SettingNotFoundException e) {
				screenIW = 0;
				screenAW = screenJ / counter;
				Log.e(TAG,"ERROR Setting now found");
				e.printStackTrace();
			} catch (NullPointerException e){
				Log.e(TAG,"Null pointer in screen");
				screenIW = 0;
				screenAW = screenJ / counter;
			}
		}else{
			screenIW = 0;
			screenAW = screenJ / counter;
		}
	}
	
	private int getForeGroundUID(){
    	if(pm.isScreenOn()){
    		try {
				return c.getPackageManager().getApplicationInfo(am.getRunningTasks(1).get(0).topActivity.getPackageName(),0).uid;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "Screen Usage UID Not Found!");
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "Screen Usage SECURITY EXCEPTION!");
			}
    	}
    	return -1;
	}
	
	private void updateALLUid(TreeMap<Integer,UIDProcess> uidtm, double counter){
		for(Map.Entry<Integer,UIDProcess> entry : uidtm.entrySet()) {
			UIDProcess currProc = entry.getValue();
			currProc.screenIW = 0;
			currProc.screenAW = currProc.screenJ / counter;
		}
	}
}
