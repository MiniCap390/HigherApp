package ca.concordia.sensortag.datasample;

import java.util.List;

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
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GraphActivity extends Activity {
	static public final String TAG = "GraphAct"; // Tag for Android's logcat
	private int mSessionIndex;
	private GraphViewData[] stepData;
	/* Service */
	private RecordService.Binder mRecSvc = null;
	BluetoothDevice mBtDevice = null;
	private TextView mDate;
	
	private void setupGui(){
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mSessionIndex = extras.getInt("SESSION_ID");
		}
		
		List<DBContainers.StepInfo> liStepInfo = mRecSvc.getAllStepInfo(mSessionIndex);
		
		//Add steps: altitude & timestamp as series data
		if (liStepInfo.size() != 0){
			stepData = new GraphViewData[liStepInfo.size()];
	
			for (int i = (liStepInfo.size() - 1); i > -1 ; i--){
				double time = liStepInfo.get(i).getTime_stamp()/1000;
				double altitude = liStepInfo.get(i).getAltitude();
				stepData[i] = new GraphViewData(time, altitude);
				//JAN: this is weird
			}
		}else{
			stepData = new GraphViewData[]{new GraphViewData(0.0, 0.0)};
		}
		GraphViewSeries stepSeries = new GraphViewSeries( stepData );
		GraphView posGraph = new LineGraphView(this, "Position");
		Log.i(TAG, "Just created posGraph");
		posGraph.addSeries( stepSeries );
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.graph);
		layout.addView(posGraph);
	
		mDate = (TextView) findViewById(R.id.workout_date);
		mDate.setText(mRecSvc.getSessionInfo(mSessionIndex).getDate());
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_graph);
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