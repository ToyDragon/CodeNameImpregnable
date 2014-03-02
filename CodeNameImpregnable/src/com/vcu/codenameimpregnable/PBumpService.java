package com.vcu.codenameimpregnable;

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
import android.util.Log;
import android.widget.TextView;

public class PBumpService extends Service{
	
	public static final String bump_prefix = "PBump-";
	public BroadcastReceiver bt_listener;
	
	
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
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
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
		new Thread(new Runnable() 
		{

			@Override
			public void run() 
			{
				
			}
			
		});
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
		final BluetoothAdapter bt_adapter = BluetoothAdapter.getDefaultAdapter();
		bt_listener = new BroadcastReceiver() {
		    public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            // Get the BluetoothDevice object from the Intent
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// Add the name and address to an array adapter to show in a ListView
		            if(device.getName().indexOf(bump_prefix)==0&&device.getBondState() == BluetoothDevice.BOND_NONE){
			            //device.bond or whatever
		            	Boolean bool = false;
		                try {
		                    Log.i("Log", "service method is called ");
		                    Class cl = Class.forName("android.bluetooth.BluetoothDevice");
		                    Class[] par = {};
		                    Method method = cl.getMethod("createBond", par);
		                    Object[] args = {};
		                    bool = (Boolean) method.invoke(device);
		                    
		                    for(BluetoothDevice d : bt_adapter.getBondedDevices()){
			                	//d.
			                	BluetoothSocket paired_socket = d.createInsecureRfcommSocketToServiceRecord(d.getUuids()[0].getUuid());
			                	
			                	paired_socket.connect();
			                	
			                	BufferedWriter output = new BufferedWriter(new OutputStreamWriter(paired_socket.getOutputStream()));
			                	output.write("THIS IS A TEST LOL PENIS");
			                	output.flush();
			                	
			                	BufferedReader input = new BufferedReader(new InputStreamReader(paired_socket.getInputStream()));
			                	//TextView btLabel = (TextView)findViewById(R.id.bluetoothLabel);
			        			//btLabel.setText(btLabel.getText() + " : " + input.readLine());
		                    }
		                } catch (Exception e) {
		                    Log.i("Log", "Inside catch of serviceFromDevice Method");
		                    e.printStackTrace();
		                }		            	
		            	
						//TextView bt_devices = (TextView)findViewById(R.id.bluetoothDevices);
						/*bt_devices.setText(bt_devices.getText() + "\n" + device.getName()
								+ "\n  " + device.getAddress()
								+ "\n  " + device.getBondState()
								+ "\n  " + BluetoothDevice.BOND_NONE);*/
			        }
		        }
		    }
		};
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(bt_listener, filter);
		
		bt_adapter.startDiscovery();
		
	}
	
}
