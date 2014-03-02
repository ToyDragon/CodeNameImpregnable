package com.vcu.codenameimpregnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.UUID;

import com.github.sendgrid.SendGrid;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.PebbleDataReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MainActivity extends Activity {
	
	public static final String bump_prefix = "PBump-",trigger = "TRIGGERRR!!!!";
	UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	UUID pebble_uuid = UUID.fromString("b014bf50-a22b-11e3-a5e2-0800200c9a66");
	String data_to_send = "Email:batesmatthewj@gmail.com,PhoneNumber:5409076417,Github:https://github.com/ToyDragon";
	long last_time_sent, last_time_received;
	BluetoothAdapter bt_adapter;
	BufferedWriter writer;
	BufferedReader reader;
	private String addTo;
	private String setFrom;
	private String setSubject;
	private String setText;
	private String phoneNumber;
	
	LinkedList<Double> x_list = new LinkedList<Double>();
	int records = 15;
	
	PebbleDataReceiver pebble_receiver = new PebbleDataReceiver(pebble_uuid){
		@Override 
		public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {         
			
			boolean meets_condition = false;
			
			double x = Double.parseDouble(data.getString(43));
			
			//TODO READ DATA HERE
			
			double this_x = x;
			x_list.add(this_x);
			if(x_list.size() >= records)
				x_list.remove(0);
			
			Log.d("X DIRECTION",""+x);
			double av_tot = 0;
			for(int i = 0; i < x_list.size()-1; i++){
				av_tot += x_list.get(i);
			}

			double recent = x_list.get(x_list.size()-1);
			av_tot /= x_list.get(x_list.size()-1);
			
			meets_condition = (Math.abs(av_tot) > 10) && (Math.abs(recent) < 1);
			
			
			if(meets_condition && last_time_sent <= System.currentTimeMillis() - 500){
				trigger();
			}
			
			try{
				Thread.sleep(100);
			}catch(Exception e){
				
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//pbump_service_intent = new Intent(this, PBumpService.class);
		setContentView(R.layout.activity_main);

        PebbleKit.registerReceivedDataHandler(this, pebble_receiver);

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
				trigger();
			}
		});
		
	}
	
	public void sendTrigger(){
		Log.d("DENASDASD","TRIGGER YES<! GOOD PUNCH!");
		sendData(trigger);
	}
	
	public void trigger(){
		sendTrigger();
		last_time_sent = System.currentTimeMillis();
		if(last_time_received >= last_time_sent - 250){
			sendMessageData();
		}
	}
	
	public void receiveTrigger(){
		last_time_received = System.currentTimeMillis();
		if(last_time_received <= last_time_sent + 250){
			sendMessageData();
		}
	}
	
	public void sendMessageData(){
		sendData(data_to_send);
	}
	
	public void startClient(){
		BroadcastReceiver bt_listener = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
		        String action = intent.getAction();
		        if (BluetoothDevice.ACTION_FOUND.equals(action) && writer==null) {
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            Log.i("Log", "Viewing device "+device.getName() + " : " + device.getBondState());
		            if(device.getName().indexOf(bump_prefix) == 0){
		                Log.i("Log", "Accepted!");
	                    try{
                        	BluetoothSocket socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                        	
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
					
					writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
					connectionEstablished();
			        Log.i("Log", "Connection established");
					
				}catch(Exception e){
					e.printStackTrace();
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
						e.printStackTrace();
						Log.e("ERROR","Could not receive data!");
						try{
							Thread.sleep(100);
						}catch(Exception ee){}
					}
				}
			}
		}.start();
	}
	
	public void handleData(String data){
		Log.d("Data","Received: " + data);
		if(data.equals(trigger)){
			receiveTrigger();
		}else{
			String email = data.substring("Email:".length(),data.indexOf(","));
			data = data.substring(data.indexOf(",")+1);
			String phone = data.substring("PhoneNumber:".length(),data.indexOf(","));
			setTemplate(email,email,"Hello","Test",phone);
			sendEmailSms();
		}
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
	
	private void sendEmailSms() {
		SendGrid sendgrid = new SendGrid("mirabile", "xavier131");
		
		sendgrid.addTo(addTo);
		sendgrid.setFrom(setFrom);
		sendgrid.setSubject(setSubject);
		sendgrid.setText(setText);

		new SendEmailTask().execute(sendgrid);
		
		phoneNumber = "+1"+phoneNumber;
		Log.d("TAG",phoneNumber);
		SmsManager smsManager = SmsManager.getDefault();
	
		smsManager.sendTextMessage(phoneNumber, null, setText, null, null);
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
			try{
				grids[0].send();
				Log.d("Log: ","We sent the email!");
			}catch(Exception e){
				e.printStackTrace();
				
			}
			return (long) 0;
		}
		
		protected void onProgressUpdate(Integer... progress){}
		
		protected void onPostExecute(Long result){}
		
	}
}
