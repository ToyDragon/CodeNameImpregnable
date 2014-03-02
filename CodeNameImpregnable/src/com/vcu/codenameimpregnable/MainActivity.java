package com.vcu.codenameimpregnable;

import java.lang.reflect.InvocationTargetException;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	Intent pbump_service_intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		pbump_service_intent = new Intent(this, PBumpService.class);
		setContentView(R.layout.activity_main);

		Log.d("TEST","starting service");
		startService(pbump_service_intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onStop(){
		stopService(pbump_service_intent);
		
		BluetoothAdapter bt_adapter = BluetoothAdapter.getDefaultAdapter();
		if(bt_adapter!=null){
			for(BluetoothDevice device : bt_adapter.getBondedDevices()){
				try {
					if(device.getName().indexOf("PBump-")==0){
						//device.getClass().getMethod("removeBond", (Class[]) null).invoke(device, (Object[]) null);
						//Log.e("BTDevices","Unpaired!");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		super.onStop();
	}
}
