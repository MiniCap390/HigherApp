package ca.concordia.sensortag.datasample;

import java.text.DecimalFormat;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewDetailsActivity extends Activity {
	static public final String TAG = "AnalyzeAct"; // Tag for Android's logcat
	static public final Double FREQ_DISP_SCALING = 60.0; // per second --> per minute
	static public final Double SEC_TO_MSEC = 1000.0;
	static protected final DecimalFormat FORMAT_FREQ = new DecimalFormat("###0.00");

	/* Service */
	private RecordService.Binder mRecSvc = null;
	BluetoothDevice mBtDevice = null;
	
	private Button	mButtonGraph;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.activity_main);
		// Get the Bluetooth device selected by the user - should be set by DeviceSelectActivity
		// upon launching this application
		Intent receivedIntent = getIntent();
		if(receivedIntent != null) {
			BluetoothDevice dev = (BluetoothDevice) receivedIntent
					.getParcelableExtra(DeviceSelectActivity.EXTRA_DEVICE);
			if(dev != null) {
				// Only update mBtDevice if a new device was passed
				// This avoids an error if we are returning to the RecordActivity while the service
				// had already started and/or was recording in the background...
				mBtDevice = dev;
			}
		}
		connectService();
		setupGui();
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
		//JAN: change ids
		mContext = this.getApplicationContext();
		mButtonGraph = (Button) findViewById(R.id.buttonGraph);
		mButtonGraph.setOnClickListener(mOnClickGraph);
	}
	private ServiceConnection mSvcConnection = new ServiceConnection() {
		/**
		 * Called when the service is connected (is bound). Read and analyze data from the service.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "RecordService connected.");
			mRecSvc = (RecordService.Binder) service;
			mRecSvc.startService(mBtDevice);

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
			Log.i(TAG, "Starting View Steps activity.");
			Intent intent = new Intent(ViewDetailsActivity.this, GraphActivity.class);
			startActivity(intent);
			
		}
		
	};
}