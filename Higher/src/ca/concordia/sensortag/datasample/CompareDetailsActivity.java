package ca.concordia.sensortag.datasample;

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
import android.widget.TextView;


public class CompareDetailsActivity extends Activity {
	static public final String TAG = "CompareDetAct"; // Tag for Android's logcat

	/* Service */
	private RecordService.Binder mRecSvc = null;
	BluetoothDevice mBtDevice = null;
	private TextView txtTotalTime1;
	private TextView txtSteps1;
	private TextView txtSpeed1;
	private TextView txtTotalEnergy1;
	private TextView txtTotalDistance1;
	private TextView txtAbsoluteDistance1;
	private TextView txtTotalTime2;
	private TextView txtSteps2;
	private TextView txtSpeed2;
	private TextView txtTotalEnergy2;
	private TextView txtTotalDistance2;
	private TextView txtAbsoluteDistance2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_details);
		setupGui();
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
		// Show the "back"/"up" button on the Action Bar (top left corner)
		getActionBar().setDisplayHomeAsUpEnabled(true);
		txtTotalTime1 = (TextView) findViewById(R.id.textTotalTime1);
		txtSteps1 = (TextView) findViewById(R.id.textSteps1);
		txtSpeed1 = (TextView) findViewById(R.id.textSpeed1);
		txtTotalEnergy1 = (TextView) findViewById(R.id.textEnergy1);
		txtTotalDistance1 = (TextView) findViewById(R.id.textTotalDistance1);
		txtAbsoluteDistance1 = (TextView) findViewById(R.id.textAbsoluteDistance1);
		txtTotalTime2 = (TextView) findViewById(R.id.textTotalTime2);
		txtSteps2 = (TextView) findViewById(R.id.textSteps2);
		txtSpeed2 = (TextView) findViewById(R.id.textSpeed2);
		txtTotalEnergy2 = (TextView) findViewById(R.id.textEnergy2);
		txtTotalDistance2 = (TextView) findViewById(R.id.textTotalDistance2);
		txtAbsoluteDistance2 = (TextView) findViewById(R.id.textAbsoluteDistance2);

	}
	private ServiceConnection mSvcConnection = new ServiceConnection() {
		/**
		 * Called when the service is connected (is bound). Read and analyze data from the service.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "RecordService connected.");
			mRecSvc = (RecordService.Binder) service;
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