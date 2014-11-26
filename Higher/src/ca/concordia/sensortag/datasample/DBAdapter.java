package ca.concordia.sensortag.datasample;

//------------------------------------ DBADapter.java ---------------------------------------------
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

	public static final String TAG = "DBAdapter";
	private final Context context;
	private final DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	//
	// ///////////////////////////////////////////////////////////////////
	// Private methods:
	// ///////////////////////////////////////////////////////////////////
	private final List<StepInfo> step_container = new ArrayList<StepInfo>();
	private final int BUFFER_SIZE = 1;

	private void insertBufferRowsStepInfo() {
		if (step_container != null) {
			for (StepInfo current_step : step_container) {
				ContentValues initialValues = new ContentValues();
				initialValues.put(DBConstants.STEP_INFO_ELAPSED_TIME,
						current_step.getElapsed_time());
				initialValues.put(DBConstants.STEP_INFO_X, current_step.getX());
				initialValues.put(DBConstants.STEP_INFO_Y, current_step.getY());
				initialValues.put(DBConstants.STEP_INFO_Z, current_step.getZ());

				// Insert it into the database.
				db.insert(DBConstants.TABLE_STEP_INFO, null, initialValues);
			}
			step_container.clear();
		}
	}

	private List<StepInfo> getAllCurrentRowStepInfo() {
		insertBufferRowsStepInfo();
		Cursor c = db.query(true, DBConstants.TABLE_STEP_INFO,
				DBConstants.STEP_INFO_ALL_KEYS, null, null, null, null, null,
				null);
		if (c != null) {
			c.moveToFirst();
		}

		List<StepInfo> current_step_container = new ArrayList<StepInfo>();

		if (c.moveToFirst()) {
			do {
				// Transfer Data
				StepInfo current_step = new StepInfo();

				current_step.setElapsed_time(c
						.getDouble(DBConstants.COL_STEP_INFO_ELAPSED_TIME));
				current_step.setXYZ(c.getDouble(DBConstants.COL_STEP_INFO_X),
						c.getDouble(DBConstants.COL_STEP_INFO_Y),
						c.getDouble(DBConstants.COL_STEP_INFO_Z));

				current_step_container.add(current_step);
			} while (c.moveToNext());
		}
		c.close();
		return current_step_container;
	}

	private String formatTime(double time_ms) {
		final long HRS_TO_SEC = 3600;
		final long MIN_TO_SEC = 60;

		double time_s = time_ms / 1000;
		int hours = (int) (time_s / HRS_TO_SEC);

		double time_s_mod_hour = time_s - (hours * HRS_TO_SEC);
		int minutes = (int) (time_s_mod_hour / MIN_TO_SEC);

		double time_s_mod_min = time_s_mod_hour - (minutes * MIN_TO_SEC);
		double seconds = (time_s_mod_min); // Modified to get better precision
											// to the 1/100th of second
		double totaltime = (hours * HRS_TO_SEC + minutes * MIN_TO_SEC + seconds) * 1000.0;
		double remaindertime_ms = (time_ms - totaltime) / 1;
		seconds = seconds + remaindertime_ms;
		return String.format("%02d:%02d:%02.1f", hours, minutes, seconds); // Added
																			// 1
																			// floating
																			// point
																			// value
	}

	//
	// ///////////////////////////////////////////////////////////////////
	// Public methods:
	// ///////////////////////////////////////////////////////////////////

	public DBAdapter(Context ctx) {
		this.context = ctx;
		myDBHelper = new DatabaseHelper(context);

		setNewRecording(0, 0); // to reset all variables
	}

	// Open the database connection.
	public DBAdapter open() {
		db = myDBHelper.getWritableDatabase();
		
		ContentValues initialValues = new ContentValues();	
		initialValues.put(DBConstants.PREFERENCES_DATA_SAMPLES, "");	
		db.insert(DBConstants.TABLE_PREFERENCES, null, initialValues);
		
		return this;
	}

	// Close the database connection.
	public void close() {
		myDBHelper.close();
	}

	public void pause_workout() {
		insertBufferRowsStepInfo();
	}

	public void deleteAllTableValues() {
		db.execSQL("DELETE FROM " + DBConstants.TABLE_STEP_INFO);
	}

	public void bufferStepInfo(StepInfo newStep) {
		step_container.add(newStep);
		if (step_container.size() > BUFFER_SIZE) {
			insertBufferRowsStepInfo();
		}
	}

	public String getLastStepInfoString() {
		List<StepInfo> current_step_container = getAllCurrentRowStepInfo();
		StepInfo current_step = current_step_container
				.get(current_step_container.size() - 1);
		String lastStepString = "Time : "
				+ formatTime(current_step.getElapsed_time()) + "\n"
				+ "Acceleration Vector in XYZ components: " + "\n" + " X: "
				+ current_step.getX() + "\n" + " Y: " + current_step.getY()
				+ "\n" + " Z: " + current_step.getZ() + "\n";

		return lastStepString;
	}

	public List<String> getCurrentAllStepInfoString() {
		insertBufferRowsStepInfo();
		List<StepInfo> current_step_container = getAllCurrentRowStepInfo();
		List<String> displayList = new ArrayList<String>();
		String display;

		for (StepInfo current_step : current_step_container) {

			display = "";
			display = display + "Time : "
					+ formatTime(current_step.getElapsed_time()) + "\n"
					+ "Acceleration Vector in XYZ components: " + "\n" + " X: "
					+ current_step.getX() + "\n" + " Y: " + current_step.getY()
					+ "\n" + " Z: " + current_step.getZ() + "\n";

			displayList.add(display);
		}

		return displayList;
	}

	// ///////////////////////////////////////////////////////////////////
	// For Preference table
	// ///////////////////////////////////////////////////////////////////
	
	public enum Status {
		NOT_STARTED, RECORDING, PAUSED, FINISHED
	};

	public static int DEFAULT_SAMPLES_SIZE = 512;

	// Recording settings
	private long mRecDuration_ms = 0; // 0 = infinite
	private int mRecSamples = 0; // 0 = infinite

	// save/load
	private Status mStatus;
	private List<Long> mEventTimestamps;
	private long mElapsed_ms;

	// used to determine whether to read/write the data on loadPreferences() or
	// savePreferences(),
	// since this operation is complex and not necessary if no changes have
	// occurred.
	private boolean mEventsChanged;
	// counts number of changes since the last savePreferences() call - can be
	// used by an external
	// client (via getDelta()) to determine when it is appropriate to save to
	// SharedPreferences
	private int mChanges;
	
	
	
	/**
	 * Start a new recording. Discards all data and sets up the RecordingData object for a new
	 * recording session.
	 * @param duration The maximum duration to record for.
	 * @param samples The maximum number of samples to record.
	 */
	synchronized public void setNewRecording(long duration, int samples) {
		Log.i(TAG, "setNewRecording(duration=" + duration + "ms, samples=" + samples + ")");
		mStatus = Status.NOT_STARTED;
		mRecDuration_ms = duration;
		mElapsed_ms = 0;
		mRecSamples = samples;
		mEventsChanged = true;
		mEventTimestamps = new ArrayList<Long>(mRecSamples != 0 ? mRecSamples
				: DEFAULT_SAMPLES_SIZE);
	}
	
	/**
	 * Add an event (data point) to the current list of data.
	 * @param timestamp The timestamp (in milliseconds) at which an event was recorded.
	 */
	synchronized public void addEvent(long timestamp) {
		
		mEventTimestamps.add(timestamp);
		mEventsChanged = true;
		Log.v(TAG, "addEvent(" + timestamp + ") [total:" + mEventTimestamps.size() + "]");
	}
	
	/**
	 * Store data in SharedPreferences, overwriting any current data.
	 */
	synchronized public boolean savePreferences() {
		Log.v(TAG, "savePreferences()");
		
		Cursor c = db.query(true, DBConstants.TABLE_PREFERENCES,
				DBConstants.PREFERENCES_ALL_KEYS, null, null, null, null, null,
				null);
		boolean res = false;
		if (c != null) {
			c.moveToFirst();
		}
		
		if (c.moveToFirst()) {
			ContentValues newValues = new ContentValues();
			
			newValues.put(DBConstants.PREFERENCES_STATUS, mStatus.toString());
			newValues.put(DBConstants.PREFERENCES_REC_DURATION, mRecDuration_ms);
			newValues.put(DBConstants.PREFERENCES_REC_SAMPLES, mRecSamples);
			newValues.put(DBConstants.PREFERENCES_ELAPSED, mElapsed_ms);
			
			if(mEventsChanged) {
				Log.d(TAG, "Events changed; saving new events list");
				String serializedData = "";
				for (Long timestamp : mEventTimestamps) {
					serializedData += Long.toHexString(timestamp) + " ";
				}
				newValues.put(DBConstants.PREFERENCES_DATA_DATA, serializedData);
				mEventsChanged = false;
			}
			
			db.update(DBConstants.TABLE_PREFERENCES, newValues, null, null);
		
			res = true;
		}
		
		if(!res) { 
			Log.e(TAG, "Failed to save data");
		}
		
		c.close();
		mChanges = 0;
		return res;
	}
	
	
	/**
	 * Load data from SharedPreferences, discarding anything currently stored. Note that this
	 * loads a flat string containing all data and deserialises it, so for large amounts of data
	 * it may be slow to run.
	 */
	synchronized public void loadPreferences() {
		Log.v(TAG, "loadPreferences()");
		
		Cursor c = db.query(true, DBConstants.TABLE_PREFERENCES,
				DBConstants.PREFERENCES_ALL_KEYS, null, null, null, null, null,
				null);
		if (c != null) {
			c.moveToFirst();
		}
		
		if (c.moveToFirst()) {
			mStatus = Status.valueOf(c.getString(DBConstants.COL_PREFERENCES_STATUS));
			mRecDuration_ms = c.getLong(DBConstants.COL_PREFERENCES_REC_DURATION);
			mRecSamples = c.getInt(DBConstants.COL_PREFERENCES_REC_SAMPLES);
			mElapsed_ms = c.getLong(DBConstants.COL_PREFERENCES_ELAPSED);
			String serializedData = c.getString(DBConstants.COL_PREFERENCES_DATA_DATA);

			if(mEventsChanged) {
				Log.d(TAG, "Events changed; reloading events list from preferences");
				
				String[] splitData = serializedData.split(" ");
				mEventTimestamps = new ArrayList<Long>(mRecSamples != 0 ? mRecSamples
						: DEFAULT_SAMPLES_SIZE);

				for (int i = 0; i < splitData.length; ++i) {
					if(splitData[i].length() == 0) continue; // extra spaces can be ignored
					try {
						addEvent(Long.parseLong(splitData[i], 16));
					}
					catch (NumberFormatException e) {
						Log.e(TAG, "loadPreferences: Value \"" + splitData[i] + "\" (index " + i
								+ ") has invalid format; expected hexadecimal long");
					}
				}
				mEventsChanged = false;
			}
		}
		c.close();
		mChanges = 0;
	}
	
	/**
	 * Return the stored recording status (pause, recording, etc.).
	 * @return Current recording status.
	 */
	public Status getStatus() {
		return mStatus;
	}

	/**
	 * @param status
	 *            Current recording status.
	 */
	synchronized public void setStatus(Status status) {
		mStatus = status;
		++mChanges;
	}

	/**
	 * Return the recording duration setting.
	 * 
	 * @return Current recording duration setting, in milliseconds. Zero for infinite duration.
	 */
	public long getRecordDuration() {
		return mRecDuration_ms;
	}

	/**
	 * Set the recording duration setting.
	 * 
	 * @param duration
	 *            Recording duration value, in milliseconds. Zero for infinite duration.
	 */
	synchronized public void setRecordDuration(long duration) {
		mRecDuration_ms = (duration > 0) ? duration : 0;
		++mChanges;
	}
	
	/**
	 * Return the amount of time that has elapsed for the current recording session.
	 * 
	 * @return Amount of time elapsed during recording so far, in milliseconds.
	 */
	public long getElapsedTime() {
		return mElapsed_ms;
	}

	/**
	 * Set the total amount of time that has elapsed for the current recording session.
	 * 
	 * @param duration
	 *            Amount of time elapsed during recording so far, in milliseconds.
	 */
	synchronized public void setElapsedTime(long time) {
		mElapsed_ms = time;
		++mChanges;
	}

	/**
	 * Increase the amount of time that has elapsed for teh current recording session by a value.
	 * This adds the passed value to the elapsed time stored.
	 * 
	 * @param duration
	 *            Amount of time to add to the total elapsed time, in milliseconds.
	 */
	synchronized public void addElapsedTime(long timeDelta) {
		mElapsed_ms += timeDelta;
		++mChanges;
	}

	/**
	 * Return the maximum event samples setting.
	 * 
	 * @return Maximum number of samples to record. Zero for infinite.
	 */
	public int getRecordMaxSamples() {
		return mRecSamples;
	}

	/**
	 * Set the maximum event samples setting.
	 * 
	 * @param samples
	 *            Maximum number of samples to record. Zero for infinite.
	 */
	synchronized public void setRecordMaxSamples(int samples) {
		mRecSamples = (samples > 0) ? samples : 0;
		++mChanges;
	}

	/**
	 * @return Number of samples stored so far.
	 */
	public int getSamplesStored() {
		return mEventTimestamps.size();
	}

	/**
	 * @return The list of event data so far, returned as an unmodifiable list.
	 */
	public List<Long> getData() {
		return Collections.unmodifiableList(mEventTimestamps);
	}
	
	/**
	 * @return The number of uncommitted/unsaved changes to the recording data (events as well as
	 * elapsed time, recordMaxSamples, etc.). Every set*() call will increment this value;
	 * loading or saving preferences will reset it.
	 */
	public int getDelta() {
		return mChanges;
	}
	
	/**
	 * Convert the data to a string. Shows data as key-value pairs; this may be useful for internal
	 * usage, e.g. debugging logcats, but is probably not useful to a user.
	 * 
	 * Example return value:
	 * 
	 * "Recording Settings: Delta[3] ElapsedTimep31400] RecordDuration[60000] RecordMaxSamples[1000]
	 * SamplesStored[7] Status[PAUSE] Data[123 456 789 1023 1553 1694 1754]"
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Recording Settings: ").append("Delta[").append(getDelta()).append("] ")
		.append("ElapsedTime[").append(getElapsedTime()).append("] ")
		.append("RecordDuration[ ").append(getRecordDuration()).append("] ")
		.append("RecordMaxSamples[").append(getRecordMaxSamples()).append("] ")
		.append("SamplesStored[").append(getSamplesStored()).append("] ")
		.append("Status[").append(getStatus()).append("] ")
		.append("Data").append(getData().toString());
		return b.toString();
	}
	
	// ///////////////////////////////////////////////////////////////////
	// Private Helper Classes:
	// ///////////////////////////////////////////////////////////////////

	/**
	 * Private class which handles database creation and upgrading. Used to
	 * handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DBConstants.DATABASE_NAME, null,
					DBConstants.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(DBConstants.CREATE_STEP_INFO_SQL);
			_db.execSQL(DBConstants.CREATE_PREFERENCES_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data!");

			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_STEP_INFO);
			_db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_PREFERENCES);

			// Recreate new database:
			onCreate(_db);
		}
	}
}
