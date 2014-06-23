package com.kutysam.androidprofiler;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.Map.Entry;
 

import android.content.Context;
import android.graphics.Color;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {
	@SuppressWarnings("unused")
	private String TAG = "ExpandableListAdapter";
    private Context _context;
    private ArrayList<UIDProcess> _listDataHeader; // header titles
 
    public ExpandableListAdapter(Context context, ArrayList<UIDProcess> listDataHeader,
    		HashMap<UIDProcess, ArrayList<UIDProcess>> listChildData) {
        this._context = context;
        this._listDataHeader = listDataHeader;
    }
 
    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataHeader.get(groupPosition);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }
 
    @Override
    public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

    	
    	final UIDProcess p = (UIDProcess) getChild(groupPosition,childPosition);
 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
        
        
        TextView lblProcFullName = (TextView) convertView.findViewById(R.id.lblProcFullName);
        TextView lblProcUID = (TextView) convertView.findViewById(R.id.lblProcUID);
        TextView lblProcCPUTime = (TextView) convertView.findViewById(R.id.lblProcCPUTime);
        TextView lblProcCPUJ = (TextView) convertView.findViewById(R.id.lblProcCPUJ);
        TextView lblProcCPUAW = (TextView) convertView.findViewById(R.id.lblProcCPUAW);
        TextView lblProcCPUIW = (TextView) convertView.findViewById(R.id.lblProcCPUIW);
        TextView lblProcScreenTime = (TextView) convertView.findViewById(R.id.lblProcScreenTime);
        TextView lblProcScreenAW = (TextView) convertView.findViewById(R.id.lblProcScreenAW);
        TextView lblProcScreenIW = (TextView) convertView.findViewById(R.id.lblProcScreenIW);
        TextView lblProcScreenJ = (TextView) convertView.findViewById(R.id.lblProcScreenJ);
        
        TextView lblProcWActive = (TextView) convertView.findViewById(R.id.lblProcWActive);
        TextView lblProcWActiveIW = (TextView) convertView.findViewById(R.id.lblProcWActiveIW);
        TextView lblProcWActiveAW = (TextView) convertView.findViewById(R.id.lblProcWActiveAW);
        TextView lblProcWActiveJ = (TextView) convertView.findViewById(R.id.lblProcWActiveJ);

        TextView lblProcMActive = (TextView) convertView.findViewById(R.id.lblProcMActive);
        TextView lblProcMActiveIW = (TextView) convertView.findViewById(R.id.lblProcMActiveIW);
        TextView lblProcMActiveAW = (TextView) convertView.findViewById(R.id.lblProcMActiveAW);
        TextView lblProcMActiveJ = (TextView) convertView.findViewById(R.id.lblProcMActiveJ);
        
        TextView lblNoOfProcess = (TextView) convertView.findViewById(R.id.lblNoOfProcess);
        	
        lblProcFullName.setText(p.fullName);
        lblProcUID.setText(""+p.uid);
        lblProcCPUTime.setText(""+PowerCalculator.timeCalculator(p.sinceLaunchCPUTime * 10));
        lblProcCPUIW.setText(PowerValues.getWatt(p.cpuIW));
        lblProcCPUAW.setText(PowerValues.getWatt(p.cpuAW));
        lblProcCPUJ.setText(PowerValues.getJoule(p.cpuJ));
        
        lblProcScreenTime.setText(""+PowerCalculator.timeCalculator(p.screenTime*1000));
        lblProcScreenIW.setText(PowerValues.getWatt(p.screenIW));
        lblProcScreenAW.setText(PowerValues.getWatt(p.screenAW));
        lblProcScreenJ.setText(PowerValues.getJoule(p.screenJ));
        
        lblProcWActive.setText(""+p.wCurrPackets + " pkt");
        lblProcWActiveIW.setText(PowerValues.getWatt(p.wCurrIW));
        lblProcWActiveAW.setText(PowerValues.getWatt(p.wCurrAW));
        lblProcWActiveJ.setText(PowerValues.getJoule(p.wCurrJ));

        lblProcMActive.setText(""+p.mCurrPackets + " pkt");
        lblProcMActiveIW.setText(PowerValues.getWatt(p.mCurrIW));
        lblProcMActiveAW.setText(PowerValues.getWatt(p.mCurrAW));
        lblProcMActiveJ.setText(PowerValues.getJoule(p.mCurrJ));
        lblNoOfProcess.setText(""+p.utm.size());
        
        
        //SUBPROCESS TABLE
        TableLayout tblSubProcess =(TableLayout) convertView.findViewById(R.id.tblSubProcess);
        tblSubProcess.removeAllViews();
        
        TableRow row= new TableRow(this._context);
        row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        TextView subProcNo = new TextView(this._context);
        subProcNo.setTextColor(Color.BLACK);
        subProcNo.setText("No");
        
        TextView subProcName = new TextView(this._context);
        subProcName.setTextColor(Color.BLACK);
        subProcName.setText("Name");
        
        TextView subProcTime = new TextView(this._context);
        subProcTime.setTextColor(Color.BLACK);
        subProcTime.setText("CPU Time");
        
        TextView subProcPower =  new TextView(this._context);
        subProcPower.setTextColor(Color.BLACK);
        subProcPower.setText("Total J");
        
        row.addView(subProcNo);
        row.addView(subProcName);
        row.addView(subProcTime);
        row.addView(subProcPower);
        tblSubProcess.addView(row);
        
        
        
        int i = 0;
        
        
        for(Entry<Integer, PIDProcess> entry : p.utm.entrySet()) {
            row= new TableRow(this._context);
            row.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            subProcNo = new TextView(this._context);
            subProcNo.setTextColor(Color.BLACK);
            subProcName = new TextView(this._context);
            subProcName.setTextColor(Color.BLACK);
            subProcTime = new TextView(this._context);
            subProcTime.setTextColor(Color.BLACK);
            subProcPower =  new TextView(this._context);
            subProcPower.setTextColor(Color.BLACK);
            
            
            subProcNo.setPadding(0, 0, 20, 0);
            subProcNo.setText("" + (i + 1));
            subProcName.setText(entry.getValue().name);
            //Play Around subProcName.setWidth(0);
            subProcTime.setText(PowerCalculator.timeCalculator(entry.getValue().lastCPUUpdateTime * 10));
            subProcPower.setText(PowerValues.getJoule(entry.getValue().totalCPUPower));
            
            row.addView(subProcNo);
            row.addView(subProcName);
            row.addView(subProcTime);
            row.addView(subProcPower);
            tblSubProcess.addView(row);
            i++;
        }
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }
 
    @Override
    public UIDProcess getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }
 
    @Override
    public int getGroupCount() {
        return this._listDataHeader.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
	@Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
    	
    	UIDProcess header = getGroup(groupPosition);

        
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }
 
        TextView lblProcName = (TextView) convertView.findViewById(R.id.lblProcName);
        TextView lblProcTotalPower = (TextView) convertView.findViewById(R.id.lblProcTotalPower);
        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
        
        lblProcName.setText(header.name);
        lblProcTotalPower.setText(PowerValues.getJoule(header.totalJ));
        imgIcon.setImageDrawable(header.icon);
        
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    
    public boolean refreshList(){
        this.notifyDataSetChanged();
        return true;
    }
}