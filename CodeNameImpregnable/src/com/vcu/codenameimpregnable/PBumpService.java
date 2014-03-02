package com.vcu.codenameimpregnable;
import com.github.sendgrid.SendGrid;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PBumpService extends Service{

	boolean started = false;
	BluetoothAdapter bt_adapter;
	String data_to_send;
	String data_recieved;
	String addTo,setFrom,setSubject,setText,phoneNumber;
	
	// CREATING FIELDS FOR BATES I MEAN TO SEND SHIT
	
	
	
	
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
		setTemplate("mirabilesp@vcu.edu","mirabilesp@vcu.edu",
				     "Testing Application", "If you got this, then we will make trillions",null);
		sendEmailSms();
		//email/sms
	}

	private void sendEmailSms() {
		// TODO Auto-generated method stub
		SendGrid sendgrid = new SendGrid("mirabile", "xavier131");
		
		if (phoneNumber != null){
			
			sendgrid.addTo(addTo);
			sendgrid.setFrom(setFrom);
			sendgrid.setSubject(setSubject);
			sendgrid.setText(setText);

			new SendEmailTask().execute(sendgrid);
			
		}else{
			
			phoneNumber = "+1"+phoneNumber;
			
			SmsManager smsManager = SmsManager.getDefault();
		
			smsManager.sendTextMessage(phoneNumber, null, setText, null, null);
			
		}
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
	
	private void setTemplate(String addTo, String setFrom,
			String setSubject, String setText, String phoneNumber){
		
		this.addTo = addTo;
		this.setFrom = setFrom;
		this.setSubject = setSubject;
		this.setText = setText;
		this.phoneNumber = phoneNumber;
		
	}
	
	
	private class SendEmailTask extends AsyncTask<SendGrid, Integer, Long>{
		protected Long doInBackground(SendGrid... grids){
			grids[0].send();
			Log.d("ERROR: ","We sent the Emails!");
			
			return (long) 0;
		}
		
		protected void onProgressUpdate(Integer... progress){
			//setProgressPercent(progress[0]);
		}
		
		protected void onPostExecute(Long result){
			//
		}
		
	}// end Class SendEmailTask

	
}// end Class PBumbServices
