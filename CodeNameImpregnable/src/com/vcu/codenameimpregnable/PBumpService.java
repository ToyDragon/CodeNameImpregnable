package com.vcu.codenameimpregnable;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;

public class PBumpService extends Service{

	boolean started = false;
	BluetoothAdapter bt_adapter;
	String data_to_send;
	String data_recieved;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
	   // handleCommand(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
		
		if(!started){
			started = true;
			
			//start listening
			startListening();
			
			//look at closest device
			lookAtDevices();
			//continued in lookAtDevices
				// check for trigger
			
					//send trigger data
					
					//listen for trigger data
			
						//send all data
			
						//receive data
				
						//email/sms
			
		}
	    return START_STICKY;
	}

	private void lookAtDevices() {
		
		//do this in another thread

		// check for trigger
		if(isTriggerConditionMet()){
			//send trigger data
			sendTriggerData();

			//listen for trigger data
			listenForTriggerData();
			//continued in listenForTriggerData

				//send all data

				//receive data
		
				//email/sms
		}
	}

	private void listenForTriggerData() {
		sendAllData();
		//send all data

		receiveData();
		//receive data

		sendEmailSms();
		//email/sms
	}

	private void sendEmailSms() {
		// TODO Auto-generated method stub
		
	}

	private void receiveData() {
		// TODO Auto-generated method stub
		
	}

	private void sendAllData() {
		// TODO Auto-generated method stub
		
	}

	private void sendTriggerData() {
		// TODO Auto-generated method stub
		
	}

	private boolean isTriggerConditionMet() {
		// TODO Auto-generated method stub
		return false;
	}

	private void startListening() {
		// TODO Auto-generated method stub
		
	}
	
}
