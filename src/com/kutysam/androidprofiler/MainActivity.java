package com.kutysam.androidprofiler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;

import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

 
public class MainActivity extends Activity {
	private Button btnSetRef;
	private ProcessController pctrl;
	private String TAG = "AndroidProfiler";
	
	private boolean pluggedIn = false;
	private boolean refStarted = false;

	private PowerManager pm;
	private PowerManager.WakeLock wl;

	private TextView txtRefTime,txtRefCpuTime,txtTotalJ,txtAvgW,txtCurrentW,txtCPUIW,txtCPUAW,txtCPUJ,
	txtScreenIW,txtScreenAW,txtScreenJ,txtWIFISIW,txtWIFISAW,txtWIFISJ,txtWIFIAIW,txtWIFIAAW,txtWIFIAJ,
	txtMOBILESIW,txtMOBILESAW,txtMOBILESJ,txtMOBILEAIW,txtMOBILEAAW,txtMOBILEAJ;
    
	private ExpandableListAdapter listAdapter;
	private ExpandableListView expListView;
	
	private static Handler myHandler;
	
	private ArrayList<UIDProcess> GUIuid;
	private HashMap<UIDProcess, ArrayList<UIDProcess>> subGUIuid;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        myHandler = new Handler(new Handler.Callback(){
            public boolean handleMessage(Message msg) {
            	updateGUI();
            	return true;
            }
        });
        
        btnSetRef = (Button)findViewById(R.id.btnSetRef);
        
    	txtRefTime = (TextView)findViewById(R.id.txtRefTime);
    	txtRefCpuTime = (TextView)findViewById(R.id.txtRefCpuTime);
    	txtTotalJ = (TextView)findViewById(R.id.txtTotalJ);
    	txtAvgW = (TextView)findViewById(R.id.txtAvgW);
    	txtCurrentW = (TextView)findViewById(R.id.txtCurrentW);
    	txtCPUIW = (TextView)findViewById(R.id.txtCPUIW);
    	txtCPUAW = (TextView)findViewById(R.id.txtCPUAW);
    	txtCPUJ = (TextView)findViewById(R.id.txtCPUJ);
    	txtScreenIW = (TextView)findViewById(R.id.txtScreenIW);
    	txtScreenAW = (TextView)findViewById(R.id.txtScreenAW);
    	txtScreenJ = (TextView)findViewById(R.id.txtScreenJ);
    	txtWIFISIW = (TextView)findViewById(R.id.txtWIFISIW);
    	txtWIFISAW = (TextView)findViewById(R.id.txtWIFISAW);
    	txtWIFISJ = (TextView)findViewById(R.id.txtWIFISJ);
    	txtWIFIAIW = (TextView)findViewById(R.id.txtWIFIAIW);
    	txtWIFIAAW = (TextView)findViewById(R.id.txtWIFIAAW);
    	txtWIFIAJ = (TextView)findViewById(R.id.txtWIFIAJ);
    	txtMOBILESIW = (TextView)findViewById(R.id.txtMOBILESIW);
    	txtMOBILESAW = (TextView)findViewById(R.id.txtMOBILESAW);
    	txtMOBILESJ = (TextView)findViewById(R.id.txtMOBILESJ);
    	txtMOBILEAIW = (TextView)findViewById(R.id.txtMOBILEAIW);
    	txtMOBILEAAW = (TextView)findViewById(R.id.txtMOBILEAAW);
    	txtMOBILEAJ = (TextView)findViewById(R.id.txtMOBILEAJ); 
        
    	expListView = (ExpandableListView)findViewById(R.id.lvExp);
        
        //Initialize GUI        
        
        //Wakelock Manager!
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
        //End of wakelock
		
		
    	//Initialize Receivers
        //this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //End of Receiver Initialization
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "RESUMED");
        wl.acquire();
        //this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }
    
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "PAUSED");
        wl.acquire();
        try{
        	if(refStarted==true){
        	//this.unregisterReceiver(batteryInfoReceiver);
        	}
        }catch(Exception e){
        	Log.e("MAIN","Receiver not registered?");
        }
    }
    
    protected void onStop(){
    	super.onStop();
    	Log.e(TAG, "STOPPED");
    	wl.acquire();
        try{
        	if(refStarted==true){
        	//this.unregisterReceiver(batteryInfoReceiver);
        	}
        }catch(Exception e){
        	Log.e("MAIN","Receiver not registered?");
        }
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menuExit:
	        	//this.unregisterReceiver(batteryInfoReceiver);
	        	finish();
	            return true;
	        case R.id.menuRefresh:
	        	//refresh();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
    private void refStart() {
    	//Initialize Receivers & Wakelock
    	wl.acquire();
        //this.registerReceiver(batteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        //End of Receiver Initialization
        
        //Create new process controller and initialize some TextViews in GUI
        GUIuid = new ArrayList<UIDProcess>();
        pctrl = new ProcessController(getPackageManager(),this,myHandler, GUIuid);
        pctrl.start();

        //SET GUI
        listAdapter = new ExpandableListAdapter(this, GUIuid, subGUIuid);
		expListView.setAdapter(listAdapter);
    }
    
    private void refStop(){
    	/*String one = ("Bird: " + pctrl.pCPU.birdIW);
    	String two = ("Current: " + pctrl.pCPU.ocpuIW);
    	String three = ("Average: " + pctrl.pCPU.ocpuAW);
    	String full = one+ "\n" + two + "\n" + three;
    	
    	String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/sampling");
        File file = new File (myDir, "sampling.csv");
        myDir.mkdirs();
    	
        try {
        	FileOutputStream fos = new FileOutputStream(file);
        	fos.write(full.getBytes());
        	fos.close();
        	System.out.println("WRRITEN");
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        } 
        */
    	wl.release();
		Toast.makeText(this, "Program Stopped!", Toast.LENGTH_SHORT).show();
		btnSetRef.setText("Start");
		refStarted = false;
		if(pctrl != null)
			pctrl.halt();
    }
    
    //Onclick Listener for Set Reference Button
    public void setBtnRefOnClick(View v) throws SettingNotFoundException{
    	if(refStarted == false){	//Program hasnt' been started and we want to START it
        	//if(pluggedIn==true){	//Unable to start cause phone plugged in.
        	//	Toast.makeText(this, "Unable to start as phone is plugged in.\n", Toast.LENGTH_SHORT).show();
        	//}
        	//else{
        		Toast.makeText(this, "Program Started!", Toast.LENGTH_SHORT).show();
        		btnSetRef.setText("Stop");
        		refStarted = true;
                refStart();
        	//}
    	}
    	
    	else if(refStarted == true){	//Program has been started and we want to stop it.
    		refStop();
    	}
    }
 
   /* private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);	//Check if the device is plugged in or not. used later
            //plugged = 0 means on battery only.

            if(plugged!=0){	//Make sure program stops!
            	if(pluggedIn == false){
            		pluggedIn = true;
                	Toast.makeText(context, "Phone is plugged in! Program will halt until you stop charging.", Toast.LENGTH_SHORT).show();
            		refStop();
            	}
            }
            else {
            	if(pluggedIn == true){	//Phone has been unplugged
	            	Toast.makeText(context, "Phone is UN-Plugged! Please start the program again.", Toast.LENGTH_SHORT).show();
	            	pluggedIn = false;
	            	refStarted = false;
            	}
            }
            //PowerValues.battVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE,0);
        }
    };*/
    
    
    
    
    
    
    private void updateGUI(){
    	//Top Values Updated
    	txtRefTime.setText(PowerCalculator.timeCalculator(pctrl.counter*1000));
    	txtRefCpuTime.setText("" + PowerValues.currCpuSpeed/1000 + "MHz");
    	txtTotalJ.setText(PowerValues.getJoule(pctrl.allJ));
    	txtAvgW.setText(PowerValues.getWatt(pctrl.allAW));
    	txtCurrentW.setText(PowerValues.getWatt(pctrl.allIW));
    	
    	txtCPUIW.setText(PowerValues.getWatt(pctrl.pCPU.cpuIW));
    	txtCPUAW.setText(PowerValues.getWatt(pctrl.pCPU.cpuAW));
    	txtCPUJ.setText(PowerValues.getJoule(pctrl.pCPU.cpuJ));
    	
    	txtScreenIW.setText(PowerValues.getWatt(pctrl.pScreen.screenIW));
    	txtScreenAW.setText(PowerValues.getWatt(pctrl.pScreen.screenAW));
    	txtScreenJ.setText(PowerValues.getJoule(pctrl.pScreen.screenJ));
    	
    	txtWIFISIW.setText(PowerValues.getWatt(pctrl.pcw.wStandByIW));
    	txtWIFISAW.setText(PowerValues.getWatt(pctrl.pcw.wStandByAW));
    	txtWIFISJ.setText(PowerValues.getJoule(pctrl.pcw.wStandByJ));
    	
    	txtWIFIAIW.setText(PowerValues.getWatt(pctrl.pcw.wTotalIW));
    	txtWIFIAAW.setText(PowerValues.getWatt(pctrl.pcw.wTotalAW));
    	txtWIFIAJ.setText(PowerValues.getJoule(pctrl.pcw.wTotalJ));
    	
    	//Update ListAdapter
    	listAdapter.refreshList();
    }
}