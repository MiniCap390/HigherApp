package ca.concordia.sensortag.datasample;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;


public class ViewActivity extends Activity {
	static public final String TAG = "ViewAct"; // Tag for Android's logcat
	
	/* Service */
	private RecordService.Binder mRecSvc = null;
	
	
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
		setContentView(R.layout.activity_view);
		
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
	
	
	/**
	 * Called in onCreate(). Sets up the GUI before the data is shown, and gets references to all
	 * the GUI elements.
	 */
	private void setupGui() {
		// Show the "back"/"up" button on the Action Bar (top left corner)
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
		
		Cursor c = mRecSvc.getAllWorkoutSessionsCursor();
		
		String[] from = new String[] {"date"};
		int[] to = new int[] {R.id.list_item_step_textview};
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.list_item_step, c, from, to, 0);

		final ListView listView = (ListView) findViewById(R.id.listview_steps);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener( new OnItemClickListener(){


			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				/**
				 * Called when an item in the list is clicked on
				 */
				Log.i(TAG, "Starting ViewDetails Activity.");
				Intent intent = new Intent(ViewActivity.this, ViewDetailsActivity.class);
				intent.putExtra("SESSION_ID", (int) id);
				startActivity(intent);
			}
        	
        });
	}
	
	/**
	 * GUI AND MENU STUFF
	 */
	
	public boolean onCreateOptionsMenu (Menu menu){
		getMenuInflater().inflate(R.menu.topmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Called when a menu item is pressed. In this case we don't have an explicit menu, but we do
	 * have the "back" button in the Action Bar (top bar). We want it to act like the regular Back
	 * button, that is to say, pressing either Back buttons closes the current Activity and returns
	 * to the previous activity in the stack.
	 */
	
		/**
	 * Menu buttons functions
	 */
	public void menuClickMain(){
		Log.i(TAG, "Starting Main activity.");
		Intent intent = new Intent(ViewActivity.this, MainActivity.class);
		intent .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
	}
	
	public void menuClickSettings(){
		Log.i(TAG, "Starting Settings activity.");
		Intent intent = new Intent(ViewActivity.this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void menuClickCompare(){
		Log.i(TAG, "Starting Compare activity.");
		Intent intent = new Intent(ViewActivity.this, CompareActivity.class);
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
	        	menuClickMain();
	            return true;
	        case R.id.view_activity:
	        	//do nothing
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
	 * Called when RecordService's receives a Step event.
	 * @param s The new status value.
	 * JAN: remove this later
	 */
	/*
	@Override
	public void onStepEvent() {
		runOnUiThread(new Runnable() {
			@Override
			public void run(){
				
				String lastStep = mRecSvc.getLastStep();

				liSessions.add(0, lastStep);
				mListAdapter.notifyDataSetChanged();
				
				}
		});
		*/
	}

	