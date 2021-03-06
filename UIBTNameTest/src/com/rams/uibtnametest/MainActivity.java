package com.rams.uibtnametest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public static final String bump_prefix = "PBump-";
	public BroadcastReceiver bt_listener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		BluetoothAdapter bt_adapter = BluetoothAdapter.getDefaultAdapter();
		if(bt_adapter != null){
			
			//rename adapter
			if(bt_adapter.getName().indexOf(bump_prefix)!=0)
				bt_adapter.setName(bump_prefix+bt_adapter.getName());
			
			makeDeviceDiscoverable();
			
			listenForDevices();
			
			//update adapter name label
			TextView btLabel = (TextView)findViewById(R.id.bluetoothLabel);
			btLabel.setText(btLabel.getText() + " : " + bt_adapter.getName());
			
			
		}else{
			TextView btLabel = (TextView)findViewById(R.id.bluetoothLabel);
			btLabel.setText("Can't find bluetooth adapter :(");
		}
	}
	
	public void makeDeviceDiscoverable(){
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3500);
		startActivity(discoverableIntent);
	}
	
	public void listenForDevices(){
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
		                try {
		                    Log.i("Log", "service method is called ");
		                    Class cl = Class.forName("android.bluetooth.BluetoothDevice");
		                    Class[] par = {};
		                    Method method = cl.getMethod("createBond", par);
		                    method.invoke(device);
		                    
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
		            	
						TextView bt_devices = (TextView)findViewById(R.id.bluetoothDevices);
						bt_devices.setText(bt_devices.getText() + "\n" + device.getName()
								+ "\n  " + device.getAddress()
								+ "\n  " + device.getBondState()
								+ "\n  " + BluetoothDevice.BOND_NONE);
			        }
		        }
		    }
		};
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(bt_listener, filter);
		
		bt_adapter.startDiscovery();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(bt_listener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
