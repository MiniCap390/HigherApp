/*
 * ELEC390 and COEN390: TI SensorTag Library for Android
 * Higher Application
 * Author: Janice Agustin
 * With code from example application DataSampler written by
 * Marc-Alexandre Chan <marcalexc@arenthil.net>
 * Institution: Concordia University
 */
package ca.concordia.sensortag.datasample;

import java.text.DecimalFormat;
import java.util.ArrayList;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Activity that reads data from the RecordService and shows some statistics about the recorded
 * data so far on screen.
 */
public class MainActivity extends Activity implements RecordServiceListener{
	static public final String TAG = "AnalyzeAct"; // Tag for Android's logcat
	static public final Double FREQ_DISP_SCALING = 60.0; // per second --> per minute
	static public final Double SEC_TO_MSEC = 1000.0;
	static protected final DecimalFormat FORMAT_FREQ = new DecimalFormat("###0.00");
	
	/* Service */
	private RecordService.Binder mRecSvc = null;
	BluetoothDevice mBtDevice = null;
	
	/* GUI */
	private TextView mPastStepsMin;
	private TextView mPresentStepsMin;
	private TextView mPastTotalSteps;
	private TextView mPresentTotalSteps;
	private TextView mPastAvgSpeed;
	private TextView mPresentAvgSpeed;
	private TextView mPastEnergy;
	private TextView mPresentEnergy;
	
	/* Data analysis */
	private List<Long> mEventTimestamps = null;
	private List<Double> mEventDifference = null; // time between each event (e.g. each footstep)
	private List<Double> mEventFrequency = null;
	private List<Double> mEventFrequencyTime = null;
	
	/* Calculated data */
	private long mTotalTime = 0;
	private int mNumSteps = 0;
	private Double mFreqAvg = null;
	private Double mFreqRmsVar = null;
	private Double mFreqMax = null;
	private Double mFreqMin = null;
	
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
		connectService();
		setupGui();
	}

	/**
	 * Binds to the service. Unlike in RecordActivity, in this case we will assume it is already
	 * started; even if it isn't, binding to it will start it, and we don't care about the service
	 * running even if the activity closes (since we just want to get data from it, not start a
	 * recording).
	 */
	private void connectService() {
		// Bind to the RecordService to get data (don't need to start it like in
		// RecordService - assume it's already started)
		Log.i(TAG, "Binding to RecordService...");
		Intent recordIntent = new Intent(this, RecordService.class);
		bindService(recordIntent, mSvcConnection, Context.BIND_ABOVE_CLIENT);
	}

	/**
	 * Service connector for RecordService. Handles messages about the service being connected or
	 * disconnected.
	 */
	private ServiceConnection mSvcConnection = new ServiceConnection() {
		/**
		 * Called when the service is connected (is bound). Read and analyze data from the service.
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(TAG, "RecordService connected.");
			mRecSvc = (RecordService.Binder) service;
			mRecSvc.addStepListener(MainActivity.this);
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
	
	/**
	 * Called in onCreate(). Sets up the GUI before the data is analysed, and gets references to all
	 * the GUI elements.
	 */
	private void setupGui() {
		// Show the "back"/"up" button on the Action Bar (top left corner)
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
		

//		mValueTime = (TextView) findViewById(R.id.textValueTime);
//		mValueEvents = (TextView) findViewById(R.id.textValueEvents);
//		mValueAverage = (TextView) findViewById(R.id.textValueAverage);
//		mValueRmsvar = (TextView) findViewById(R.id.textValueRmsVar);
//		mValueMin = (TextView) findViewById(R.id.textValueMin);
//		mValueMax = (TextView) findViewById(R.id.textValueMax);

		//JAN: change ids
		//mButtonRecordPause = (Button) findViewById(R.id.buttonRecordPause);

		
		mPastStepsMin = (TextView) findViewById(R.id.textValuePastTime) ;
		mPresentStepsMin = (TextView) findViewById(R.id.textValuePresentTime) ;
		mPastTotalSteps = (TextView) findViewById(R.id.textValuePastEvents);
		mPresentTotalSteps = (TextView) findViewById(R.id.textValuePresentEvents);
		mPastAvgSpeed = (TextView) findViewById(R.id.textValuePastSpeed);
		mPresentAvgSpeed = (TextView) findViewById(R.id.textValuePresentSpeed);
		mPastEnergy = (TextView) findViewById(R.id.textValuePastEnergy);
		mPresentEnergy = (TextView) findViewById(R.id.textValuePresentEnergy);
		
	}
	
	public boolean onCreateOptionsMenu (Menu menu){
		getMenuInflater().inflate(R.menu.topmenu, menu);
		return super.onCreateOptionsMenu(menu);
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
	
	/**
	 * Called upon successful connection to the Service. Retrieve the recording data from the
	 * service and analyse it.
	 * 
	 * One issue with the statistics to collect is that the frequency of events can change over
	 * time: events can be coming in quickly at one moment, and then slow down the next. We want to
	 * be able to represent this as an approximate function f(t).
	 * 
	 * The data collected is represented as a discrete function t[n], representing the time `t` of
	 * the `n`th event (steps taken in this example). t[n] is monotonic increasing, since every
	 * subsequent event must occur after the last one, and therefore t[n] is inversible.
	 * 
	 * If we take the inverse function we get n(t), which represents the `n`th recorded sample at a
	 * given time `t` (the function is undefined at `t` for which a sample was not recorded, so n(t)
	 * is only isolated points). If we assume that n(t) contains samples of a smooth continuous
	 * function x(t), then taking the derivative dx/dt would give us the frequency of events as a
	 * smooth continuous function (units of 1/seconds, or events/second if x is in generalised
	 * real-valued events).
	 * 
	 * Let's use the Newton different quotient on consecutive events to get a first-order
	 * approximation of dn/dt:
	 * 
	 * dn/dt = [n(t_2) - n(t_1)] / (t_2 - t_1)
	 * 
	 * This is dn/dt at (t_2 - t_1)/2. Note that t_2 and t_1 are always for CONSECUTIVE events, so
	 * n(t_2) - n(t_1) is always 1.
	 * 
	 * Therefore, we can simplify this to
	 * 
	 * dn/dt = 1.0/(t_2 - t_1)
	 * 
	 * for any two consecutive event times t_2, t_1. This is an approximation of the frequency of
	 * events between event n(t_1) and n(t_2).
	 */
	private void analyzeData() {
		if(mRecSvc == null) {
			Toast.makeText(this, "Service not bound", Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Cannot analyze data: service not bound");
			return;
		}
		
		mEventTimestamps = mRecSvc.getData();
		mNumSteps = mEventTimestamps.size();
		mTotalTime = mRecSvc.getElapsedTime();
		
		mFreqAvg = mNumSteps / ((double) mTotalTime / SEC_TO_MSEC);
		
		mEventDifference = new ArrayList<Double>(mNumSteps);
		mEventFrequency = new ArrayList<Double>(mNumSteps);
		mEventFrequencyTime = new ArrayList<Double>(mNumSteps);
		double freqSquareIntegral = 0;
		
		// Initial value for min/max calculation
		mFreqMin = mFreqMax = null;

		// As a first point: Consider the time between the start of recording and the first event
		if (mEventTimestamps.size() > 0) {
			double deltaTimeMs = (double) mEventTimestamps.get(0);
//			double sampleTimeMs = mEventTimestamps.get(0) / 2.0;
			double freqHz = 1.0 / (deltaTimeMs / SEC_TO_MSEC);

			// only consider this for the minimum frequency
			// (event on ending record isn't a high freq event nor desired for RMS calculation)
			mFreqMin = freqHz;
		}
		
		// Calculate the time interval between each event
		for(int i = 0; i < mNumSteps - 1; ++i) {
			double deltaTimeMs = (double) mEventTimestamps.get(i+1) - mEventTimestamps.get(i);
			double sampleTimeMs = (mEventTimestamps.get(i+1) + mEventTimestamps.get(i)) / 2.0;
			double freqHz = 1.0 / (deltaTimeMs/SEC_TO_MSEC);
			mEventDifference.add(deltaTimeMs);
			mEventFrequency.add(freqHz); // f(t) = dn/dt above
			mEventFrequencyTime.add(sampleTimeMs); // t for to each value f(t) in mEventFrequency
			
			if(mFreqMax == null || freqHz > mFreqMax) mFreqMax = freqHz;
			if(mFreqMin == null || freqHz < mFreqMin) mFreqMin = freqHz;
		}
		
		// As a last point: consider the time between the last event and end of recording
		{
			long mLastEvent = mEventTimestamps.get(mEventTimestamps.size() - 1);
			double deltaTimeMs = (double)(mTotalTime - mLastEvent);
//			double sampleTimeMs = (double)(mTotalTime + mLastEvent) / 2.0;
			double freqHz = 1.0 / (deltaTimeMs / SEC_TO_MSEC);

			// only consider this for the minimum frequency
			// (event on ending record isn't a high freq event nor desired for RMS calculation)
			if(mFreqMin == null || freqHz < mFreqMin) mFreqMin = freqHz;
		}

		/*
		 * Next, calculate the root mean squared of mEventFrequency over time: we will call this
		 * function freq(t). However, we want this to be "AC coupled", i.e. we only want the root
		 * mean squared of the CHANGES of freq(t), not of a "DC" or constant component.
		 * Therefore, we take the root mean squared of (freq(t) - mean of freq(t)).
		 * 
		 * Definition of RMS:
		 * 
		 * f_rms = sqrt( integral(freq(t) - mean)^2 dt / totalTime );
		 */
		// 
		
		// first we need that integration - use the trapezoid rule
		for(int i = 0; i < mEventFrequency.size() - 1; ++i) {
			// trapezoid area = (a1 + a2)*t/2.0
			// t is time interval, a1/a2 are endpoint values of function
			// (in this case, the function is the SQUARE of frequency for RMS)
			double f1_ac = mEventFrequency.get(i) - mFreqAvg;
			double f2_ac = mEventFrequency.get(i+1) - mFreqAvg;
			
			double f1_squared = f1_ac * f1_ac;
			double f2_squared = f2_ac * f2_ac;
			// want this in milliseconds --> mTotalTime is in milliseconds too
			double deltaTimeMs = (mEventFrequencyTime.get(i+1) - mEventFrequencyTime.get(i));
			freqSquareIntegral += (f1_squared + f2_squared) * deltaTimeMs / 2.0;
		}
		
		// Now calculate the RMS about the variable
		mFreqRmsVar = Math.sqrt(freqSquareIntegral / mTotalTime);
	}
	
	/**
	 * Takes the analysed data, as calculated by analyzeData(), and displays it on the GUI.
	 */
//	private void displayData() {
//		// Reset everything to blank values - in case some of the values analysed aren't valid
//		mValueTime.setText("-.-- s");
//		mValueEvents.setText("-");
//		mValueAverage.setText("-.--");
//		mValueRmsvar.setText("-.--");
//		mValueMin.setText("-.--");
//		mValueMax.setText("-.--");
//		
//		// Format the time into hh:mm:ss.xx format. We don't use a date formatter since this is a
//		// time interval, not a time-of-day - a date formatter will wrap above 12 or 24 hours.
//		double secondsDecimal = (double)(mTotalTime % 60000)/1000;
//		long minutes = (long) ((mTotalTime / (1000*60)) % 60);
//		long hours   = (long) (mTotalTime / (1000*60*60));
//		
//		mValueTime.setText(String.format("%02d:%02d:%03.1f", hours, minutes, secondsDecimal));
//		mValueEvents.setText(Integer.toString(mNumSteps, 10));
//		if(mFreqAvg != null)  mValueAverage.setText(FORMAT_FREQ.format(mFreqAvg*FREQ_DISP_SCALING));
//		if(mFreqRmsVar != null)mValueRmsvar.setText(FORMAT_FREQ.format(mFreqRmsVar*FREQ_DISP_SCALING));
//		if(mFreqMin != null)  mValueMin.setText(FORMAT_FREQ.format(mFreqMin*FREQ_DISP_SCALING));
//		if(mFreqMax != null)  mValueMax.setText(FORMAT_FREQ.format(mFreqMax*FREQ_DISP_SCALING));
//	}

	/**
	 * Called by Android when the Activity comes back into the foreground (i.e. on-screen). When
	 * called, reconnect to the service in order to update the analysis.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onResume() {
		super.onResume();
		connectService();
	}

	/**
	 * Called by Android when the Activity goes out of focus (for example, if another Application
	 * pops up on top of it and partially obscures it). When called, this method unbinds from the
	 * service.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onPause() {
		super.onPause();
		unbindService(mSvcConnection);
	}

	/**
	 * Called when the Activity is destroyed by Android. Cleans up the Bluetooth connection to the
	 * SensorTag.
	 * 
	 * @see https://developer.android.com/reference/android/app/Activity.html#ActivityLifecycle
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	/**
	 * Update text view with average steps/minute
	 */
	private void updateAvgSteps(){
		int totalSteps  = mRecSvc.getAllSteps().size();
		double elapsedTimeSeconds = (mRecSvc.getElapsedTime() * 1000);
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
		
		mPresentAvgSpeed.setText(String.valueOf(avgSpeed) + "m/s");
		
	}
	
	private void calculateEnergy() {
		//formula reverse-engineered from http://www.dietcombat.com/best-exercise-to-lose-weight
		//rate = calories/min
		double weight = 70;
		double rate = (weight*2.2-100)*0.93/15.0 + 6.4;
		double energy = (mRecSvc.getElapsedTime() * 1000) * 1000 * rate / 60;
		mPresentEnergy.setText(String.valueOf(energy) + " Calories");
	}

//
//	/**
//	 * Update the state of all buttons, based on the current RecordService state. For example,
//	 * while stopped, the main button is Record, but while running it is Pause.
//	 * 
//	 * Requires service connection. If the RecordService is not connected, disables all buttons.
//	 */
//	
//	protected void updateButtons() {
//		if (mRecSvc == null) {
//			Log.w(TAG, "updateButtons(): No RecordService connection; disabling buttons");
//			mButtonRecordPause.setEnabled(false);
//			mButtonRecordPause.setClickable(false);		
//			return;
//		}
//
//		RecordService.Status svcStatus = mRecSvc.getStatus();
//		String logPre = "updateButtons(): Service state is " + svcStatus.name();
//
//		switch (svcStatus) {
//		case ERROR: // fallthrough
//		case ERROR_SENSORTAG: // fallthrough
//		case ERROR_SENSORTAG_NULL:
//			Log.w(TAG, logPre + "(\"" + mRecSvc.getLastError() + "\"); disabling buttons.");
//			mButtonRecordPause.setEnabled(false);
//			mButtonRecordPause.setClickable(false);		
//			break;
//
//		case FINISHED:
//			Log.i(TAG, logPre + "; RecordPauseButton=Record; AnalyzeButton=enabled; ResetButton=enabled");
//			mButtonRecordPause.setEnabled(false);
//			mButtonRecordPause.setClickable(false);
//			mButtonRecordPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0,
//					0, 0);
//			mButtonRecordPause.setText(R.string.act_samp_button_record_complete);
//			mButtonRecordPause.setOnClickListener(mOnClickRecordListener);
//			break;
//			
//		case STANDBY:
//			Log.i(TAG, logPre + "; RecordPauseButton=Record; AnalyzeButton=disabled; ResetButton=disabled");
//			mButtonRecordPause.setEnabled(true);
//			mButtonRecordPause.setClickable(true);
//			mButtonRecordPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0,
//					0, 0);
//			mButtonRecordPause.setText(R.string.act_samp_button_record);
//			mButtonRecordPause.setOnClickListener(mOnClickRecordListener);
//			break;
//			
//		case PAUSE:
//			Log.i(TAG, logPre + "; RecordPauseButton=Resume; AnalyzeButton=enabled; ResetButton=enabled");
//			mButtonRecordPause.setEnabled(true);
//			mButtonRecordPause.setClickable(true);
//			mButtonRecordPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0,
//					0, 0);
//			mButtonRecordPause.setText(R.string.act_samp_button_resume);
//			mButtonRecordPause.setOnClickListener(mOnClickRecordListener);
//			break;
//			
//		case RECORD:
//			Log.i(TAG, logPre + "; RecordPauseButton=Pause; AnalyzeButton=disabled; ResetButton=disabled");
//			mButtonRecordPause.setEnabled(true);
//			mButtonRecordPause.setClickable(true);
//			mButtonRecordPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0,
//					0, 0);
//			mButtonRecordPause.setText(R.string.act_samp_button_pause);
//			mButtonRecordPause.setOnClickListener(mOnClickPauseListener);
//			break;
//
//		case NOT_STARTED:
//		default:
//			Log.i(TAG, "updateButtons(): unknown Service state; disabling buttons.");
//			mButtonRecordPause.setEnabled(false);
//			mButtonRecordPause.setClickable(false);
//		break;
//		}
//	}
	
	public void onToggleClicked (View v){
		boolean on = ((ToggleButton) v).isChecked();
	    if (on) {
	    	mRecSvc.record();
	    } else {
	    	//mRecSvc.stop();
	    }
	}

	@Override
	public void onStatusChanged(Status s) {
	//MAYBE CHANGE THE BUTTON HERE
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
}
