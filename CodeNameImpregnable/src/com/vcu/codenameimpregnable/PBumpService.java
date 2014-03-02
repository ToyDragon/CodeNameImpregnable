package com.vcu.codenameimpregnable;
import com.github.sendgrid.SendGrid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
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
	UUID uui = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
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
			bt_adapter = BluetoothAdapter.getDefaultAdapter();
			if(bt_adapter.getName().indexOf(bump_prefix)!=0)
				bt_adapter.setName(bump_prefix + bt_adapter.getName());
			
			//
			makeDeviceDiscoverable();
			
			//start listening
			startListening();
			
			//set up server
			setUpServer();
			
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
	
	public void setUpServer(){
		try {
			new Thread(){
				public void run(){
					try {
						BluetoothServerSocket server = bt_adapter.listenUsingRfcommWithServiceRecord(bt_adapter.getName(), uui);
						Log.d("LogServerSocket","Setting up server socket!");
						bt_socket = server.accept();
						bt_reader = new BufferedReader(new InputStreamReader(bt_socket.getInputStream()));
	                    bt_writer = new BufferedWriter(new OutputStreamWriter(bt_socket.getOutputStream()));
					} catch (IOException e) {
						Log.e("LogServerSocket","Could not set up server socket!");
					}
				}
			}.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
	}
	
	public void makeDeviceDiscoverable(){
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1800);
		discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(discoverableIntent);
		Log.d("TEST","DISCOVERABLE");
	}
	
	public void onDestroy(){
		unregisterReceiver(bt_listener);
		is_stopped = true;
		super.onDestroy();
		Log.d("t","DESTROYED");
	}
	
	public boolean isStopped(){
		return is_stopped;
	}

	private void lookAtDevices() {
		new Thread(new Runnable() 
		{
			int passes = 0;

			@Override
			public void run() 
			{
				// check for trigger
                bt_adapter.startDiscovery();
				while(!isStopped()){

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
					try {
						Thread.sleep(2000);
						Log.d("TAG", "sleeping");
						passes++;
						if(passes >= 50) {
							is_stopped = true;
							Log.d("t","LAD() has stopped looking");
						}
					} catch (Exception e) {
						Log.d("TAG", "ouch");
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
		Log.d("TAG",phoneNumber);
		if (phoneNumber == null){
			
			sendgrid.addTo(addTo);
			sendgrid.setFrom(setFrom);
			sendgrid.setSubject(setSubject);
			sendgrid.setText(setText);

			new SendEmailTask().execute(sendgrid);
			
		}else{
			
			phoneNumber = "+1"+phoneNumber;
			Log.d("TAG",phoneNumber);
			SmsManager smsManager = SmsManager.getDefault();
		
			smsManager.sendTextMessage(phoneNumber, null, setText, null, null);
			
		}
	}

	private void receiveData() {
		try{
			data_recieved = bt_reader.readLine();
			Log.d("DATA","Received: " + data_recieved);
		}catch(Exception e){
			
		}
	}

	private void sendAllData() {
		try{
			bt_writer.write(data_to_send);
			bt_writer.newLine();
			bt_writer.flush();
			Log.d("DATA","Send: " + data_to_send);
		}catch(Exception e){
			Log.d("PBump Error","Could not write to socket!\n"+e.toString());
		}
	}

	private void sendTriggerData() {
		// TODO Auto-generated method stub
		
	}

	private boolean isTriggerConditionMet() {
		
		return bt_writer != null;
	}

	private void startListening() {
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
		                    
		                    try{
	                        	BluetoothSocket s = device.createInsecureRfcommSocketToServiceRecord(uui);
	                        	s.connect();
	        		            Log.i("Log", "socket set with "+device.getName());
                        	}catch(Exception e){
                        		Log.e("ERROR",""+e);
                        	}
		                } catch (Exception e) {
		                    Log.i("Log", "Inside catch of serviceFromDevice Method");
		                    e.printStackTrace();
		                }
			        }
		        }
		        /*
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
	            				
	            			}
	            		}
	            	}
	                Log.d("RSSI", name + " -> " + rssi);
	            }
	            */
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
