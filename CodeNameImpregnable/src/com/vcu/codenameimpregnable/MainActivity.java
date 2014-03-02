package com.vcu.codenameimpregnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends Activity {
	
	public static final String bump_prefix = "PBump-",trigger = "TRIGGERRR!!!!";
	UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	BluetoothAdapter bt_adapter;
	BufferedWriter writer;
	BufferedReader reader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//pbump_service_intent = new Intent(this, PBumpService.class);
		setContentView(R.layout.activity_main);

		bt_adapter = BluetoothAdapter.getDefaultAdapter();
		
		//Log.d("TEST","starting service");
		//startService(pbump_service_intent);
		final Button button_client = (Button)findViewById(R.id.buttonClient);
		final Button button_server = (Button)findViewById(R.id.buttonServer);
		final Button button_trigger = (Button)findViewById(R.id.buttonTrigger);
		
		button_client.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				startClient();
				((ViewGroup)button_client.getParent()).removeView(button_client);
				((ViewGroup)button_server.getParent()).removeView(button_server);
			}
		});
		button_server.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				startServer();
				((ViewGroup)button_client.getParent()).removeView(button_client);
				((ViewGroup)button_server.getParent()).removeView(button_server);
			}
		});
		button_trigger.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				sendData("TEST!");
			}
		});
	}
	
	public void startClient(){
		BroadcastReceiver bt_listener = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action) && writer==null) {
		            // Get the BluetoothDevice object from the Intent
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            Log.i("Log", "Viewing device "+device.getName() + " : " + device.getBondState());
					// Add the name and address to an array adapter to show in a ListView
		            if(device.getName().indexOf(bump_prefix) == 0){
		                Log.i("Log", "Accepted!");
		                try {
		                    Method method = BluetoothDevice.class.getMethod("createBond", new Class[0]);
		                    method.invoke(device);
		                    try{
	                        	//BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
	                        	Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
	                        	BluetoothSocket socket = (BluetoothSocket) m.invoke(device, 1);
	                        	
	                        	bt_adapter.cancelDiscovery();
	                        	
	                        	socket.connect();
	                        	
	                        	writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	        					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        					
	        					connectionEstablished();
	        		            Log.i("Log", "Connected succesfully!"+(writer!=null));
                        	}catch(Exception e){
                        		Log.e("ERROR","COULD NOT BE A CLIENT NOOOOOOO!!!!!!");
                        		e.printStackTrace();
                        		Log.e("ERROR",""+e.getMessage());
                        	}
		                } catch (Exception e) {
		                    Log.i("Log", "Inside catch of serviceFromDevice Method");
		                    e.printStackTrace();
		                }
			        }
		        }
		        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && writer == null){
		            Log.i("Log", "Listening again...");
		        	bt_adapter.startDiscovery();
		        }
		    }
		};
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(bt_listener, filter);

        Log.i("Log", "Beggining to listen...");
		bt_adapter.startDiscovery();
	}
	
	public void makeDeviceDiscoverable(){
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1800);
		discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(discoverableIntent);
	}

	public void startServer(){
		new Thread(){
			public void run(){
				try{
					if(bt_adapter.getName().indexOf(bump_prefix)!=0)
						bt_adapter.setName(bump_prefix + bt_adapter.getName());
					
					makeDeviceDiscoverable();
					
					BluetoothServerSocket server_socket = bt_adapter.listenUsingRfcommWithServiceRecord(bt_adapter.getName(), uuid);
					BluetoothSocket socket = server_socket.accept();
					socket.connect();
					writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
					connectionEstablished();
			        Log.i("Log", "Connection established");
					
				}catch(Exception e){
					Log.e("ERROR","COULD NOT BE A SERVER NOOOOOO!!!!");
				}
			}
		}.start();
	}
	
	public void connectionEstablished(){
		receiveData();
	}
	
	public void receiveData(){
		new Thread(){
			public void run(){
				while(true){
					String line;
					try {
			            Log.i("Log", "Waiting for data...");
						line = reader.readLine();
						handleData(line);
					} catch (IOException e) {
						Log.e("ERROR","Could not receive data!");
					}
				}
			}
		}.start();
	}
	
	public void handleData(String data){
		Log.d("Data","Received: " + data);
	}
	
	public void sendData(String data){
		try{
			writer.write(data);
			writer.newLine();
			writer.flush();
            Log.i("Log", "Sent data!");
		}catch(Exception e){
			e.printStackTrace();
			Log.e("ERROR","COULD NOT SEND DAYA NOOONONNONO!!!!");
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onStop(){
		super.onStop();
	}
}
