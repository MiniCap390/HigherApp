package ca.concordia.sensortag.datasample;

import java.text.DecimalFormat;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewDetailsActivity extends Activity {
	static public final String TAG = "ViewDetAct"; // Tag for Android's logcat
	static public final Double FREQ_DISP_SCALING = 60.0; // per second --> per minute
	static public final Double SEC_TO_MSEC = 1000.0;
	static protected final DecimalFormat FORMAT_FREQ = new DecimalFormat("###0.00");
	public int SESSION_ID;
	
	/* GUI objects */
	private TextView mDate;
	private TextView mSteps;
	private TextView mSpeed;
	private TextView mEnergy;
	private TextView mDistance;
	private TextView mDisplacement;
	private TextView mDuration;

	/* Service */
	private RecordService.Binder mRecSvc = null;
	BluetoothDevice mBtDevice = null;
	
	private Button	mButtonGraph;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_details);
		
	}
	@Override
	protected void onResume() {
		super.onResume();
		connectService();
	}
	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mSvcConnection);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void connectService() {
		// Bind to the RecordService to get data (don't need to start it like in
		// RecordService - assume it's already started)
		Log.i(TAG, "Binding to RecordService...");
		Intent recordIntent = new Intent(this, RecordService.class);
		bindService(recordIntent, mSvcConnection, Context.BIND_ABOVE_CLIENT);
	}
	private void setupGui() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			SESSION_ID = extras.getInt("SESSION_ID");
		}
		
		DBContainers.SessionInfo session = mRecSvc.getSessionInfo(SESSION_ID);
		
		
		// Show the "back"/"up" button on the Action Bar (top left corner)
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//JAN: change ids
		mButtonGraph = (Button) findViewById(R.id.show_graph_button);
		mButtonGraph.setOnClickListener(mOnClickGraph);
		
		mDate = (TextView) findViewById(R.id.workout_date) ;
		mSteps = (TextView) findViewById(R.id.textValueEvents) ;
		mSpeed = (TextView) findViewById(R.id.textValueEventFrequency);
		mEnergy = (TextView) findViewById(R.id.textValueEnergy);
		mDistance = (TextView) findViewById(R.id.textValueTotalDistance);
		mDisplacement = (TextView) findViewById(R.id.textValueAbsoluteDistance);
		mDuration = (TextView) findViewById(R.id.textValueTime);
		
		mDate.setText(session.getDate());
		mSteps.setText(String.valueOf(session.getTotal_step()));
//		mSpeed.setText(String.valueOf(session.getAverage_speed()));
		mEnergy.setText(String.valueOf(session.getTotal_energy()));
//		mDistance.setText(String.valueOf(session.getTotal_altitude()));
		mDisplacement.setText(String.valueOf(session.getTotal_altitude()));
		mDuration.setText(formatTime(session.getTotal_duration()));
		
		mSpeed.setText(String.format("%02.2f", session.getAverage_speed()));
//		mEnergy.setText(String.format("%02.2f", session.getTotal_energy()));
		mDistance.setText(String.format("%02.2f", session.getTotal_altitude()));
//		mDisplacement.setText(String.format("%02.2f", session.getTotal_altitude()));
//		mDuration.setText(String.format("%02.2f", session.getTotal_duration()));
		
	}
	private ServiceConnection mSvcConnection = new ServiceConnection() {
		/**
		 * Called when the service is connected (is bound). Read and analyze data from the service.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "RecordService connected.");
			mRecSvc = (RecordService.Binder) service;
			setupGui();
		}

		/**
		 * Called when the service is disconnected (is unbound).
		 */
		@Override
		public void onServiceDisconnected(android.content.ComponentName name) {
			Log.i(TAG, "RecordService disconnected.");
			mRecSvc = null;
		}
	};
	private Button.OnClickListener mOnClickGraph = new Button.OnClickListener() {

	 
		@Override
		public void onClick(View v) {
			
			/**
			 * Called when the Graph button is clicked. Displays position graph
			 */
			Log.i(TAG, "Starting Graph activity.");
			Intent intent = new Intent(ViewDetailsActivity.this, GraphActivity.class);
			intent.putExtra("SESSION_ID", SESSION_ID);
			startActivity(intent);
			
		}
		
	};
	
	private String formatTime(double time_ms) {
		final long HRS_TO_SEC = 3600;
		final long HRS_TO_MIN = 60;
		final long MIN_TO_SEC = 60;
		
		double time_s = time_ms / 1000;
		int hours = (int)(time_s / HRS_TO_SEC);
		
		double time_s_mod_hour = time_s - (hours * HRS_TO_SEC);
		int minutes = (int)(time_s_mod_hour / MIN_TO_SEC);
		
		double time_s_mod_min = time_s_mod_hour - (minutes * MIN_TO_SEC);
		int seconds = (int)(time_s_mod_min);
		
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
	        case android.R.id.home:
	            this.finish();
	            return true;
            default:
            	return super.onOptionsItemSelected(item);
	    }
	}
}