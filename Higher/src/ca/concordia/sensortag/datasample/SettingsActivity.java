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
	
	//Height predetermined values
	String mHeight[]= {	"1.35" , "1.4" , "1.45",
						"1.5"  , "1.55", "1.6" ,
						"1.65" , "1.7" , "1.75",
						"1.8"  , "1.85", "1.9" ,
						"1.95" , "2.0" , "2.05",
						"2.1"  , "2.15", "2.2" ,
						"2.25" , "2.3" , "2.35", "2.4" };
	
	//Weight predetermined values
	String[] mWeight = new String[191];
	
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_setting);
		setupGui();
		populateWeightArray();

	}
	
	private void setupGui() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setTitle("Higher Application Settings");
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
	
	private void populateWeightArray(){
		double value = 40;
		for (int i = 0 ; i <191 ; i++) {
		     mWeight[i] =  String.valueOf(value);
		     value += 0.5;
		}
	}
	
	/**
	 * Set User Height dialog called by the XML button
	 * @param view
	 */
	public void setHeight(View view){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.settings_height_prompt);

        final NumberPicker np = new NumberPicker(this);

        np.setMinValue(0);
        np.setMaxValue(mHeight.length-1);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(mHeight);
        np.setValue(10);
        

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        	//Create temp user, assign it to the user height in DB, display toast to user
        	DBContainers.User user = DBContainers.containers.new User();
        	double height = Double.valueOf(mHeight[np.getValue()]);
        	user.setWeight(height);
        	mRecSvc.setUser(user);
        	Log.d(TAG, "User Height set as " + mHeight[np.getValue()]);
        	Toast.makeText(getApplicationContext(), "User Height set as " + mHeight[np.getValue()] + "m",
        		    Toast.LENGTH_SHORT).show();
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
	
	/**
	 * Set User Weight dialog called by the XML button
	 * @param view
	 */
	public void setWeight(View view){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.settings_weight_prompt);

        final NumberPicker np = new NumberPicker(this);

        np.setMinValue(0);
        np.setMaxValue(mWeight.length-1);
        np.setWrapSelectorWheel(false);
        np.setDisplayedValues(mWeight);
        np.setValue(60);
        

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        	//Create temp user, assign it to the user weight in DB, display toast to user
        	DBContainers.User user = DBContainers.containers.new User();
        	double weight = Double.valueOf(mWeight[np.getValue()]);
        	user.setWeight(weight);
        	mRecSvc.setUser(user);
        	Log.d(TAG, "User Weight set as " + mWeight[np.getValue()]);
        	Toast.makeText(getApplicationContext(), "User Weight set as " + mWeight[np.getValue()] + "kg",
        		    Toast.LENGTH_SHORT).show();
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
	
	/**
	 * Set User Age dialog called by the XML button
	 * Uses number picker directly, no pre configuration
	 * @param view
	 */
	public void setAge(View view){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.settings_age_prompt);

        final NumberPicker np = new NumberPicker(this);

        np.setMinValue(10);
        np.setMaxValue(80);
        np.setWrapSelectorWheel(false);
        np.setValue(25);
        

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {

        	DBContainers.User user = DBContainers.containers.new User();
        	user.setAge(np.getValue());
        	mRecSvc.setUser(user);
        	Log.d(TAG, "User Age set as " + String.valueOf(np.getValue()));
        	Toast.makeText(getApplicationContext(), "User Age set as " + String.valueOf(np.getValue()),
        		    Toast.LENGTH_SHORT).show();
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
	
	/**
	 * Set User Gender dialog called by the XML button
	 * @param view
	 */
	public void setGender(View view){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.settings_gender);
        

        alert.setPositiveButton("Male", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {

        	DBContainers.User user = DBContainers.containers.new User();
        	user.setGender(true);
        	mRecSvc.setUser(user);
        	Log.d(TAG, "User Gender set as Male");
        	Toast.makeText(getApplicationContext(), "User Gender set as Male",
        		    Toast.LENGTH_SHORT).show();
          }
        });

        alert.setNegativeButton("Female", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
        	DBContainers.User user = DBContainers.containers.new User();
          	user.setGender(false);
          	mRecSvc.setUser(user);
          	Log.d(TAG, "User Gender set as Female");
        	Toast.makeText(getApplicationContext(), "User Gender set as Female",
        		    Toast.LENGTH_SHORT).show();
          }
        });

        alert.show();
	}
	
	/**
	 * Set User Age dialog called by the XML button
	 * Uses number picker directly, no pre configuration
	 * @param view
	 */
	public void setCompareRange(View view){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.settings_compare_range_prompt);

        final NumberPicker np = new NumberPicker(this);

        np.setMinValue(5);
        np.setMaxValue(15);
        np.setWrapSelectorWheel(false);
        np.setValue(5);
        

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        	// Do something with value!
        	DBContainers.User user = DBContainers.containers.new User();
        	user.setList_pref(np.getValue());
        	mRecSvc.setUser(user);
        	Log.d(TAG, "Workout comparison range set to " + String.valueOf(np.getValue()));
        	Toast.makeText(getApplicationContext(), "Workout comparison range set to " + String.valueOf(np.getValue()),
        		    Toast.LENGTH_SHORT).show();
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
}

