package com.example.sendtext;
import com.github.sendgrid.SendGrid;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
//import android.view.M
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Button buttonSend;
	EditText textPhoneNo;
	EditText textSMS;
	SendGrid sendgrid;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		buttonSend = (Button) findViewById(R.id.buttonSend);
		textPhoneNo = (EditText) findViewById(R.id.editTextPhoneNo);
		textSMS = (EditText) findViewById(R.id.editTextSMS);
		
		sendgrid = new SendGrid("mirabile", "xavier131");
		sendgrid.addTo("mirabilesp@vcu.edu");
		sendgrid.setFrom("mirabilesp@vcu.edu");
		sendgrid.setSubject("Hello World");
		sendgrid.setText("My first email through SendGrid");

		new SendEmailTask().execute(sendgrid);
		
		buttonSend.setOnClickListener(new OnClickListener(){
			
			@Override
			public void onClick(View v){
				
				String phoneNo = textPhoneNo.getText().toString();
				String sms = textSMS.getText().toString();
				
				try{
					
					SmsManager smsManager = SmsManager.getDefault();
					
					smsManager.sendTextMessage(phoneNo, null, sms, null, null);
					
					Toast.makeText(getApplication(), "SMS sent!",
							Toast.LENGTH_LONG).show();
					
				}catch (Exception e){
					
					Toast.makeText(getApplicationContext(),
							"SMS Failed, please try again!",
							Toast.LENGTH_LONG).show();
					
					e.printStackTrace();
					
				}
				
				
			}
			
		});
		
		
		
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
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
