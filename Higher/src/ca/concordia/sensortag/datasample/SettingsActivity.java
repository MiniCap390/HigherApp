package ca.concordia.sensortag.datasample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SettingsActivity extends Activity {
	
	static public final String TAG = "SettingsAct"; // Tag for Android's logcat
	
	/* Service */
	private RecordService.Binder mRecSvc = null;
	
	String mheight[]= {	"1.35" , "1.4" , "1.45",
						"1.5"  , "1.55", "1.6" ,
						"1.65" , "1.7" , "1.75",
						"1.8"  , "1.85", "1.9" ,
						"1.95" , "2.0" , "2.05",
						"2.1"  , "2.15", "2.2" ,
						"2.25" , "2.3" , "2.35", "2.4" };
						
	
//	/* GUI */
//	private Button mHeight;
//	private Button mWeight;
//	private Button mAge;
//	private ToggleButton mGender;
//	private Button mCompRange;
	private TextView mheightfeedback;
	
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_setting);
		setupGui();

	}
	
	private void setupGui() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Higher Application Settings");
		//mheightfeedback = (TextView) findViewById(R.id.textView1);
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
	 * Binds to the service. Unlike in RecordActivity, in this case we will assume it is already
	 * started; even if it isn't, binding to it will start it, and we don't care about the service
	 * running even if the activity closes (since we just want to get data from it, not start a
	 * recording).
	 */
	private void connectService() {
		// Bind to the RecordService to get data (don't need to start it like in
		// RecordService - assume it's already started)
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
	
	public void setHeight(View view){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.settings_height_prompt);

        final NumberPicker np = new NumberPicker(this);

        np.setMinValue(0);
        np.setMaxValue(mheight.length-1);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(mheight);
        np.setValue(10);
        

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        	// Do something with value!
        	//mheightfeedback.setText(mheight[np.getValue()] + "m");
          }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            // Cancel.
        	  return;
          }
        });

        alert.setView(np);
        alert.show();
	}
	
	
	public void onToggleGender(View view) {
	    // Is the toggle on?
	    boolean on = ((ToggleButton) view).isChecked();
	    
	    if (on) {
	        // Set Gender to female
	    } else {
	        // Set Gender to male
	    }
	}
	
}

