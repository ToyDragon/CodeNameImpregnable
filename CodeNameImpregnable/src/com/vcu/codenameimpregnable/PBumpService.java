package com.vcu.codenameimpregnable;
import com.github.sendgrid.SendGrid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;

public class PBumpService extends Service{
	

	public static final String bump_prefix = "PBump-";
	public BroadcastReceiver bt_listener;
	
	BluetoothDevice closestDevice;

	boolean started;
	BluetoothAdapter bt_adapter;
	String data_to_send;
	String data_recieved;
	String addTo,setFrom,setSubject,setText,phoneNumber;
	boolean is_stopped;
	int rssi = 0;
	
	static BluetoothSocket bt_socket;
	static BufferedWriter bt_writer;
	static BufferedReader bt_reader;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {

		Log.d("TEST","start");
		Log.d("TEST","starccccc "+started);
	    
	    if(!started){
			started = true;
			
			if(bt_adapter!=null && bt_adapter.getName().indexOf(bump_prefix)!=0)
				bt_adapter.setName(bump_prefix + bt_adapter.getName());
			
			//
			makeDeviceDiscoverable();
			
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
	}
	
	public void makeDeviceDiscoverable(){
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3500);
		discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(discoverableIntent);
		Log.d("TEST","DISCOVERABLE");
	}
	
	public void onDestroy(){
		unregisterReceiver(bt_listener);
		is_stopped = true;
		super.onDestroy();
		Log.d("TAG","DESTROYED");
	}
	
	public boolean isStopped(){
		return is_stopped;
	}

	private void lookAtDevices() {
		new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				// check for trigger
				while(!isStopped()){
					
					if(bt_adapter!=null){
						if (bt_adapter.isDiscovering())
		                {
		                	bt_adapter.cancelDiscovery();
		                }
						
		                bt_adapter.startDiscovery();
					}

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
			}
		}).start();
	}
	 
	private void listenForTriggerData() {
		sendAllData();
		//send all data

		receiveData();
		//receive data
		setTemplate("mirabilesp@vcu.edu","mirabilesp@vcu.edu",
				     "Testing Application", "If you got this, then we will make trillions","7037953696");
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
		try{
			data_recieved = bt_reader.readLine();
		}catch(Exception e){
			
		}
	}

	private void sendAllData() {
		try{
			bt_writer.write(data_to_send);
			bt_writer.newLine();
			bt_writer.flush();
		}catch(Exception e){
			Log.d("PBump Error","Could not write to socket!\n"+e.toString());
		}
	}

	private void sendTriggerData() {
		// TODO Auto-generated method stub
		
	}

	private boolean isTriggerConditionMet() {
		
		return false;
	}

	private void startListening() {
		final BluetoothAdapter bt_adapter = BluetoothAdapter.getDefaultAdapter();

        Log.i("Log", "loadin ");
		bt_listener = new BroadcastReceiver() {
		    
			public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        Log.i("Log", "test1 ");
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            // Get the BluetoothDevice object from the Intent
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            Log.i("Log", "test2 "+device.getName() + " : " + device.getBondState());
					// Add the name and address to an array adapter to show in a ListView
		            if(device.getName().indexOf(bump_prefix) == 0){// && device.getBondState() == BluetoothDevice.BOND_NONE){
		                Log.i("Log", "test3 ");
			            //device.bond or whatever
		                try {
		                    Log.i("Log", "service method is called ");
		                    Class cl = Class.forName("android.bluetooth.BluetoothDevice");
		                    Class[] par = {};
		                    Method method = cl.getMethod("createBond", par);
		                    method.invoke(device);
		                } catch (Exception e) {
		                    Log.i("Log", "Inside catch of serviceFromDevice Method");
		                    e.printStackTrace();
		                }
			        }
		        }
		        
	            if(BluetoothDevice.ACTION_FOUND.equals(action) && (intent.getStringExtra(BluetoothDevice.EXTRA_NAME) != null) && (intent.getStringExtra(BluetoothDevice.EXTRA_NAME).indexOf(bump_prefix)==0))
	            {
	                int temp;
	            	temp = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
	            	String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
	            	if (temp > rssi)
	            	{
	            		rssi = temp;
	            		for(BluetoothDevice d : bt_adapter.getBondedDevices())
	            		{
	            			if (d.getName().equals(name))
	            			{
	            				closestDevice = d;
	            				try{
		                        	bt_socket = d.createInsecureRfcommSocketToServiceRecord(d.getUuids()[0].getUuid());
		                        	bt_reader = new BufferedReader(new InputStreamReader(bt_socket.getInputStream()));
		                        	bt_writer = new BufferedWriter(new OutputStreamWriter(bt_socket.getOutputStream()));
	                        	}catch(Exception e){
	                        		Log.e("ERROR",""+e);
	                        	}
	            			}
	            		}
	            	}
	                Log.d("RSSI", name + " -> " + rssi);
	            }
		    }
		};
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(bt_listener, filter);
		
		bt_adapter.startDiscovery();
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
		
		protected void onProgressUpdate(Integer... progress){}
		
		protected void onPostExecute(Long result){}
		
	}// end Class SendEmailTask
}// end Class PBumbServices
