/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Example application: Data/Event Sampler
 * Author: Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */
package ca.concordia.sensortag.datasample;

import java.util.List;

import ca.concordia.sensortag.datasample.RecordService.RecordServiceListener;
import ca.concordia.sensortag.datasample.RecordService.Status;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This is the main activity that allows setting up a recording session and recording data. This
 * activity lets you count "steps" or "shakes", usually from a SensorTag in your pocket (this lets
 * you use your phone without worrying about falsifying data from having it in your hand or moving
 * it). Once the recording process has started, this activity does not need to be in the foreground.
 * 
 * You can use this app as a template for any kind of data sampling or tracking application: for
 * example, for measuring temperature over time, or for tracking events like steps, or full
 * rotations of a wheel (using the magnetometer and a magnet), or over/under-temperature events and
 * the time that these events occur. From there you can analyse the data and present visualisations
 * and statistics to the user.
 */
public class MainActivity extends Activity implements RecordServiceListener {
	static public final String TAG = "MainAct"; // Tag for Android's logcat
	static public final Long SEC_TO_MSEC = 1000L;

	/* Service */
	RecordService.Binder mRecSvc = null;
	BluetoothDevice mBtDevice = null;

	/* GUI objects */
	private TextView mPastStepsMin;
	private TextView mPresentStepsMin;
	private TextView mPastTotalSteps;
	private TextView mPresentTotalSteps;
	private TextView mPastAvgSpeed;
	private TextView mPresentAvgSpeed;
	private TextView mPastEnergy;
	private TextView mPresentEnergy;

	/**
	 * Called by Android when the Activity is first created. This sets up the GUI for the Activity,
	 * sets up the variables to control the GUI elements in the program, and prepares the Bluetooth
	 * communication to the SensorTag.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
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

		// Usually we'd detect mBtDevice == null and error out...
		// But the service might already be running with a bluetooth device (if we're returning to
		// the RecordActivity)... so don't check that, let the Service handle it.
		
		connectService();
		setupGui();
	}

	/**
	 * Start the RecordService and then bind (connect) to it.
	 */
	private void connectService() {
		if (mBtDevice == null) Log.e(TAG, "connectService(): No Bluetooth device");

		// Bind this Activity to the Service described in the recordIntent, starting it if necessary
		// This creates a bound service, which will be destroyed if all bound Activities unbind or
		// close. However, in onServiceConnected() we take measures to make the service persistent
		// even if the user moves away from this Activity.
		Log.i(TAG, "Binding to RecordService...");
		Intent recordIntent = new Intent(this, RecordService.class);
		bindService(recordIntent, mSvcConnection, Context.BIND_AUTO_CREATE | Context.BIND_ABOVE_CLIENT);
	}

	/**
	 * Get references to the GUI elements and set up listeners.
	 */
	private void setupGui() {
		// Show the "back"/"up" button on the Action Bar (top left corner)
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);

		// Get a reference to the Java object corresponding to the GUI elements on the layout. This
		// allows us to use the variables later on to change what's shown on the GUI and respond to
		// clicks, etc.
		mPastStepsMin = (TextView) findViewById(R.id.textValuePastTime) ;
		mPresentStepsMin = (TextView) findViewById(R.id.textValuePresentTime) ;
		mPastTotalSteps = (TextView) findViewById(R.id.textValuePastEvents);
		mPresentTotalSteps = (TextView) findViewById(R.id.textValuePresentEvents);
		mPastAvgSpeed = (TextView) findViewById(R.id.textValuePastSpeed);
		mPresentAvgSpeed = (TextView) findViewById(R.id.textValuePresentSpeed);
		mPastEnergy = (TextView) findViewById(R.id.textValuePastEnergy);
		mPresentEnergy = (TextView) findViewById(R.id.textValuePresentEnergy);;

	}
	
	public boolean onCreateOptionsMenu (Menu menu){
		getMenuInflater().inflate(R.menu.topmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	

	/**
	 * Service connector for RecordService. Handles messages about the service being connected or
	 * disconnected.
	 */
	private ServiceConnection mSvcConnection = new ServiceConnection() {
		/**
		 * Called when the service is connected (is bound). Initialises the service using
		 * startService to set up the SensorTag and storage backend, and sets up the initial GUI
		 * state using previously stored information retrieved from the service.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "RecordService connected.");
			mRecSvc = (RecordService.Binder) service;
			mRecSvc.addListener(MainActivity.this);
			mRecSvc.addStepListener(MainActivity.this);
			mRecSvc.startService(mBtDevice);
			
		}

		/**
		 * Called when the service is disconnected (unbound). Update the GUI to disable service-
		 * dependent functionality.
		 */
		@Override
		public void onServiceDisconnected(android.content.ComponentName name) {
			Log.i(TAG, "RecordService disconnected.");
			mRecSvc = null;
		}
	};

	/**
	 * Called by Android when the Activity comes back into the foreground (i.e. on-screen). When
	 * called, enables processing sensor measurements (which are received by {@link ManagerListener}
	 * ).
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}

	/**
	 * Called by Android when the Activity goes out of focus (for example, if another Application
	 * pops up on top of it and partially obscures it). When called, this method disables processing
	 * sensor measurements but does not close the Bluetooth connection or disable the sensors,
	 * allowing the application to save power/CPU by not processing sensor measurement info but
	 * restore quickly when it comes into the foreground again.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	/**
	 * Called when the Activity is destroyed by Android. Stops the RecordService and unbinds from
	 * it.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		
		Log.d(TAG, "Unbinding from service...");
		if(mRecSvc != null) {
			// Only unbind here: if the Activity is killed but the service is recording, we don't
			// want the recording to be interrupted in the background, so we do not call
			// mRecSvc.stopService()
			unbindService(mSvcConnection);
			mRecSvc = null;
		}
	}
	
	/**
	 * Menu buttons functions
	 */
	public void menuClickView(){
		Log.i(TAG, "Starting View Steps activity.");
		Intent intent = new Intent(MainActivity.this, ViewActivity.class);
		intent .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}
	
	public void menuClickSettings(){
		Log.i(TAG, "Starting View Steps activity.");
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void menuClickCompare(){
		Log.i(TAG, "Starting View Steps activity.");
		Intent intent = new Intent(MainActivity.this, CompareActivity.class);
		intent .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}
	
	
	
	/**
	 * Called when a menu item is pressed. In this case we don't have an explicit menu, but we do
	 * have the "back" button in the Action Bar (top bar). We want it to act like the regular Back
	 * button, that is to say, pressing either Back buttons closes the current Activity and returns
	 * to the previous activity in the stack.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId())
	    {
	        case android.R.id.home:
	            this.finish();
	            return true;
            case R.id.main_screen:
	        	//do nothing
	            return true;
	        case R.id.view_activity:
	        	menuClickView();
	            return true;
	        case R.id.Compare_activity:
	        	menuClickCompare();
	            return true;
	        case R.id.Settings:
	        	menuClickSettings();
	        	return true;
            default:
            	return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onToggleClicked (View v){
		boolean on = ((ToggleButton) v).isChecked();
	    if (on) {
	    	mRecSvc.record();
	    	((Chronometer) findViewById(R.id.chronometer1)).setBase(SystemClock.elapsedRealtime());
	    	((Chronometer) findViewById(R.id.chronometer1)).start();
	    } else {
	    	mRecSvc.stop();
	    	((Chronometer) findViewById(R.id.chronometer1)).stop();
	    }
	}

	/**
	 * Called when RecordService's status changes.
	 * @param s The new status value.
	 */
	@Override
	public void onStatusChanged(Status s) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
			}
			
		});
	}

	@Override
	public void onStepEvent() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				updateAvgSteps();
				updateStepCount();
				updateAvgSpeed();
				calculateEnergy();
			}
			
		});
	}
	
	/**
	 * Update text view with average steps/minute
	 */
	private void updateAvgSteps(){
		int totalSteps  = mRecSvc.getAllSteps().size();
		double elapsedTimeSeconds = (mRecSvc.getElapsedTime() / 1000);
		double stepsPerMinute = (double)totalSteps/elapsedTimeSeconds;
		stepsPerMinute *= 60;
		
		mPresentStepsMin.setText(String.format("%02.2f", stepsPerMinute) + " Steps/Min");
	}
	
	private void updateStepCount(){
		int totalSteps = mRecSvc.getAllSteps().size();
		mPresentTotalSteps.setText(String.valueOf(totalSteps) + " Steps");
	}
	
	private void updateAvgSpeed(){
		List<DBContainers.StepInfo> current_step_infos = mRecSvc.getAllCurrentStepInfo();
		int numberOfSteps = current_step_infos.size();
		double lastStepAltitude = current_step_infos.get(numberOfSteps-1).getAltitude();
		
		double elapsedTimeSeconds = (mRecSvc.getElapsedTime() * 1000);
		
		double avgSpeed = lastStepAltitude/elapsedTimeSeconds;
		avgSpeed *= 60;
		
		mPresentAvgSpeed.setText(String.format("%02.2f", avgSpeed) + " m/s");
		
	}
	
	private void calculateEnergy() {
		//formula reverse-engineered from http://www.dietcombat.com/best-exercise-to-lose-weight
		//rate = calories/min
		double weight = 70;
		double rate = (weight*2.2-100)*0.93/15.0 + 6.4;
		double energy = (mRecSvc.getElapsedTime()) /1000 * rate / 60;
		mPresentEnergy.setText(String.format("%02.2f", energy) + " Cal");
	}
	
	
	
}
