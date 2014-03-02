package com.example.pbump;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;


public class MainActivity extends Activity 
{

	private BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        Button button = (Button) findViewById(R.id.button);
        Button button1 = (Button) findViewById(R.id.button1);
        
        button1.setOnClickListener(new OnClickListener()
        {
        	
            public void onClick(View v) 
            {
            	BTAdapter.cancelDiscovery();
                System.exit(0);
            }
        });
        button.setOnClickListener(new OnClickListener()
        {
        	
            public void onClick(View v)
            {
                if (BTAdapter.isDiscovering())
                {
                	BTAdapter.cancelDiscovery();
                }
                BTAdapter.startDiscovery();
            }
        });
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	 private final BroadcastReceiver receiver = new BroadcastReceiver()
	 {
	        @Override
	        public void onReceive(Context context, Intent intent) 
	        {
	            String action = intent.getAction();
	            if(BluetoothDevice.ACTION_FOUND.equals(action) && (intent.getStringExtra(BluetoothDevice.EXTRA_NAME) != null) && (intent.getStringExtra(BluetoothDevice.EXTRA_NAME).equals("MATT-PC")))
	            {
	                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
	                String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
	                TextView rssi_msg = (TextView) findViewById(R.id.textView1);
	                rssi_msg.setText( name + " => " + rssi + "dBm\n");
	                
	            }
	        }
	 };

}
