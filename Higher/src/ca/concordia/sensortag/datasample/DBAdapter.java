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
import android.text.format.Time;
import android.util.Log;

public class DBAdapter {

	public static final String TAG = "DBAdapter";
	private final Context context;
	private final DatabaseHelper myDBHelper;
	private SQLiteDatabase db;
	private final List<DBContainers.StepInfo> stepContainer = new ArrayList<DBContainers.StepInfo>();
	private final int BUFFER_SIZE = 1;
	DBContainers containers = new DBContainers();

	//
	// ///////////////////////////////////////////////////////////////////
	// Private methods:
	// ///////////////////////////////////////////////////////////////////
	
	/**
	 * TO DO
	 */
	private double calculateBMI(){
		double weight= 0;
		double height= 0;
		
		return (weight/(height*height));
	}
	
			//
			// ///////////////////////////////////////////////////////////////////
			// Function that calculate the values for the SessionInfo Table
			// ///////////////////////////////////////////////////////////////////

	private double getTotalAverageSpeed(int total_step, double duration) {
		return ((total_step)/duration);
	}

	/**
	 * TO DO
	 */
	private double getTotalEnergy() {
		// TODO Auto-generated method stub
		return 1;
	}

	private double getTotalAltitudeMagnitude(Cursor allCurrentSteps) {

		if (allCurrentSteps != null) {
			allCurrentSteps.moveToFirst();
		}

		double totalAltitudeMagnitude = 0;
		double currentAltitude = 0;
		double lastAltitude = allCurrentSteps
				.getDouble(DBConstants.COL_STEP_INFO_ALTITUDE);
		double altitudeChange = 0;
		if (allCurrentSteps.moveToNext()) {
			do {
				currentAltitude = allCurrentSteps
						.getDouble(DBConstants.COL_STEP_INFO_ALTITUDE);
				altitudeChange = currentAltitude - lastAltitude;

				if (altitudeChange > 0) {
					totalAltitudeMagnitude += altitudeChange;
				} else {
					totalAltitudeMagnitude -= altitudeChange;
				}
				lastAltitude = currentAltitude;
			} while (allCurrentSteps.moveToNext());
		}
		return totalAltitudeMagnitude;
	}

	private double getTotalAltitude(Cursor allCurrentSteps) {

		double totalAltitude = 0;

		if (allCurrentSteps.moveToFirst()) {

			totalAltitude = allCurrentSteps
					.getDouble(DBConstants.COL_STEP_INFO_ALTITUDE);

			if (allCurrentSteps.moveToLast()) {
				totalAltitude -= allCurrentSteps
						.getDouble(DBConstants.COL_STEP_INFO_ALTITUDE);
			}
		}
		return totalAltitude;
	}
	
	private int getTotalStep() {
		String query = "select count(*) from " + DBConstants.TABLE_STEP_INFO
								+ " WHERE " + DBConstants.STEP_INFO_SESSION_ID + "=" + getCurrentWorkoutID();
		Cursor c = 	db.rawQuery(query, null);
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count;
	}
	
	private double getTotalDuration(Cursor allCurrentSteps) { // in seconds

		boolean flag = true;
		if (allCurrentSteps != null) {
			flag = allCurrentSteps.moveToLast();
		}
		double totalDuration = 0;
		if (flag) {
			totalDuration = allCurrentSteps
					.getDouble(DBConstants.COL_STEP_INFO_TIME_STAMP);
		}
		return totalDuration;
	}
	
			//
			// ///////////////////////////////////////////////////////////////////
			// General Utility methods 
			// ///////////////////////////////////////////////////////////////////
	
	/**
	 * @returns the _id of the highest value from Workout_Session table
	 * @author Phohawkenics
	 */
	private int getCurrentWorkoutID() {
		String query = "SELECT MAX(" + DBConstants.KEY_ROWID + ") FROM " 
								+ DBConstants.TABLE_WORKOUT_SESSION;
		Cursor c = 	db.rawQuery(query, null);	
		c.moveToFirst();	
		int id = c.getInt(0);	
		c.close();
		return id;
	}
	
	private String getCurrentTime() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		return today.format("%k:%M:%S");
	}
	
	/**
	 * Inserts all the steps in the buffer in to the StepInfo table
	 * 
	 * @author Phohawkenics
	 */
	private void insertBufferRowsStepInfo() {
		if (stepContainer != null) {
			for (DBContainers.StepInfo current_step : stepContainer) {
				ContentValues initialValues = new ContentValues();
				initialValues.put(DBConstants.STEP_INFO_SESSION_ID,
						1);		//Temp change me to workout session info ** 1 => getCurrentWorkoutID()
				initialValues.put(DBConstants.STEP_INFO_TIME_STAMP,
						current_step.getTime_stamp());
				initialValues.put(DBConstants.STEP_INFO_ALTITUDE, 
						current_step.getAltitude());
				// Insert it into the database.
				db.insert(DBConstants.TABLE_STEP_INFO, null, initialValues);
			}
			stepContainer.clear();
		}
	}
	
	private Cursor getAllCurrentRowStepInfoCursor() {
		insertBufferRowsStepInfo();
		Cursor c = db.query(true, DBConstants.TABLE_STEP_INFO,
				DBConstants.STEP_INFO_ALL_KEYS, null, null, null, null, null,
				null);

		return c;
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
		return String.format("%02d:%02d:%02.1f", hours, minutes, seconds); // Added 1 floating point value
	}

	/**
	 * Grabs all the values in the StepInfo table, converts the into a List<DBContainers.StepInfo>
	 * 
	 * @return a list of every step inside of the StepInfo Table
	 * @author Phohawkenics
	 */
	private List<DBContainers.StepInfo> getAllCurrentRowStepInfo() {
		insertBufferRowsStepInfo();
		Cursor c = db.query(true, DBConstants.TABLE_STEP_INFO,
				DBConstants.STEP_INFO_ALL_KEYS, null, null, null, null, null,
				null);
		if (c != null) {
			c.moveToFirst();
		}

		List<DBContainers.StepInfo> current_step_container = new ArrayList<DBContainers.StepInfo>();

		if (c.moveToFirst()) {
			do {
				// Transfer Data
				DBContainers.StepInfo current_step = containers.new StepInfo();

				current_step.setTime_stamp(c
						.getDouble(DBConstants.COL_STEP_INFO_TIME_STAMP));
				current_step.setAltitude(c.getDouble(DBConstants.COL_STEP_INFO_ALTITUDE));
				current_step_container.add(current_step);
			} while (c.moveToNext());
		}
		c.close();
		return current_step_container;
	}
	
			//
			// ///////////////////////////////////////////////////////////////////
			// The core insert functions for each table
			// ///////////////////////////////////////////////////////////////////
	
	
	private void insertRowIntoUser(String name) {
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBConstants.USER_NAME, name);
		
		// Insert it into the database.
		db.insert(DBConstants.TABLE_USER, null, initialValues);
	}
	
	private void insertRowIntoWorkoutSession () {
		
		String endTime = " ";
		
		ContentValues initialValues = new ContentValues();
		// No start time because it is automatically put in
		initialValues.put(DBConstants.WORKOUT_SESSION_END_TIME, endTime);
		
		// Insert it into the database.
		db.insert(DBConstants.TABLE_WORKOUT_SESSION, null, initialValues);
	}
	
	private void insertRowStepInfo(double altitude) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBConstants.STEP_INFO_SESSION_ID, getCurrentWorkoutID());
		// No start time because it is automatically put in
		initialValues.put(DBConstants.STEP_INFO_ALTITUDE, altitude);
		
		// Insert it into the database.
		db.insert(DBConstants.TABLE_STEP_INFO, null, initialValues);
	}
	
	private void insertRowSessionInfo() {
		String where = DBConstants.STEP_INFO_SESSION_ID + "=" + getCurrentWorkoutID();
		Cursor c = db.query(true, DBConstants.TABLE_STEP_INFO,
				DBConstants.STEP_INFO_ALL_KEYS, where, null, null, null, null,
				null);
		
		int session_id = getCurrentWorkoutID();
		int totalStep = getTotalStep();
		double duration = getTotalDuration(c);// In Seconds
		double average_speed = getTotalAverageSpeed(totalStep, duration); // In steps/sec
		double totalAltitudeMagnitude = getTotalAltitudeMagnitude(c); // In wtv it is in StepInfo
		double totalAltitude = getTotalAltitude(c); // In wtv it is in StepInfo
		double totalEnergy = getTotalEnergy(); // Not yet implemented
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBConstants.KEY_ROWID, session_id);
		// No date because it is automatically put in
		initialValues.put(DBConstants.SESSION_INFO_AVERAGE_SPEED, average_speed);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_ENERGY, totalEnergy);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_ALTITUDE_MAGNITUDE, totalAltitudeMagnitude);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_ALTITUDE, totalAltitude);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_STEP, totalStep);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_DURATION, duration);
		
		c.close();
		
		// Insert it into the database.
		db.insert(DBConstants.TABLE_SESSION_INFO, null, initialValues);
	}
	
	/**
	 * Updates the current workout session with an end time
	 * 
	 * @author Phohawkenics
	 */
	private void updateRowWorkoutSession() {
		String where = DBConstants.KEY_ROWID + "=" + getCurrentWorkoutID();
		
		ContentValues newValues = new ContentValues();
		newValues.put(DBConstants.WORKOUT_SESSION_END_TIME, getCurrentTime());
		
		db.update(DBConstants.TABLE_WORKOUT_SESSION, newValues, where, null);
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
	
	public void start_workout() {
		this.insertRowIntoWorkoutSession();
	}
	
	public void stop_workout() {
		this.insertBufferRowsStepInfo();
		this.updateRowWorkoutSession();
		this.insertRowSessionInfo();
	}

	public void pause_workout() {
		insertBufferRowsStepInfo();
	}

	public void deleteAllTableValues() {
		db.execSQL("DELETE FROM " + DBConstants.TABLE_STEP_INFO);
	}

	public void bufferStepInfo(DBContainers.StepInfo newStep) {
		stepContainer.add(newStep);
		if (stepContainer.size() > BUFFER_SIZE) {
			insertBufferRowsStepInfo();
		}
	}

	public String getLastStepInfoString() {
		List<DBContainers.StepInfo> current_step_container = getAllCurrentRowStepInfo();
		DBContainers.StepInfo current_step = current_step_container
				.get(current_step_container.size() - 1);
		String lastStepString = "Time : "
				+ formatTime(current_step.getTime_stamp()) + "\n"
				+ "Altitude: "+ current_step.getAltitude() + "\n" ;

		return lastStepString;
	}

	public List<String> getCurrentAllStepInfoString() {
		insertBufferRowsStepInfo();
		List<DBContainers.StepInfo> current_step_container = getAllCurrentRowStepInfo();
		List<String> displayList = new ArrayList<String>();
		String display;

		for (DBContainers.StepInfo current_step : current_step_container) {

			display = "";
			display = display + "Time : "
					+ formatTime(current_step.getTime_stamp()) + "\n"
					+ "Altitude: "+ current_step.getAltitude() + "\n" ;

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
