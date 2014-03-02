package com.vcu.codenameimpregnable;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class MainActivity extends Activity {

	Intent pbump_service_intent = new Intent(this, PBumpService.class);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startService(pbump_service_intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onPause(){
		super.onPause();

		stopService(pbump_service_intent);
	}
}
