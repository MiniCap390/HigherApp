package ca.concordia.sensortag.datasample;

//------------------------------------ DBADapter.java ---------------------------------------------
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.concordia.sensortag.datasample.DBContainers.SessionInfo;
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

	//
	// ///////////////////////////////////////////////////////////////////
	// Private methods:
	// ///////////////////////////////////////////////////////////////////
	
	/**
	 * @param height(m)
	 * @param weight(kg)
	 * @return
	 */
	private double calculateBMI(double height, double weight) {
		double bmi = weight/(height*height);
		
		return bmi;
	}
	
	/**
	 * @param weight(kg)
	 * @param time(ms)
	 * @return
	 */
	private double calculateEnergy(double weight, double time) {
		//formula reverse-engineered from http://www.dietcombat.com/best-exercise-to-lose-weight
		//rate = calories/min
		double rate = (weight*2.2-100)*0.93/15.0 + 6.4;
		return (double) time * 1000 * rate / 60;
	}
	
			//
			// ///////////////////////////////////////////////////////////////////
			// Function that calculate the values for the SessionInfo Table
			// ///////////////////////////////////////////////////////////////////

	private double getTotalAverageSpeed(int total_step, double duration) {
		return ((total_step)/(duration/1000))*60;
	}

	private double getTotalEnergy() {		
		Cursor currentUserSettings = getUserSetting();
		
		double weight = currentUserSettings.getDouble(DBConstants.COL_USER_WEIGHT);
		long time = getRecordDuration();
		
		currentUserSettings.close();
		
		return calculateEnergy(weight, time);
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
					.getDouble(DBConstants.COL_STEP_INFO_ELAPSED_TIME);
		}
		return getElapsedTime();//totalDuration;
	}
	
			//
			// ///////////////////////////////////////////////////////////////////
			// General Utility methods 
			// ///////////////////////////////////////////////////////////////////
	
	private Cursor getUserSetting() {
		// Hard coded to 1 since only 1 user
		String where = DBConstants.KEY_ROWID + "=1";
		
		// Get current values from the DB
		Cursor currentUserSettings = db.query(true, DBConstants.TABLE_USER,
				DBConstants.USER_ALL_KEYS, where, null, null, null, null,
				null);
		if (currentUserSettings != null) {
			currentUserSettings.moveToFirst();
		}
		
		return currentUserSettings;
	}
	
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
	
	/**
	 * @returns the time in standard 11:33:10 format
	 */
	private String getCurrentTime() {
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		return today.format("%k:%M:%S");
	}
	
	/**
	 * @returns a cursor containing all the rows of the StepInfo Table
	 *  (might be dead code)
	 */
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
		String where = DBConstants.STEP_INFO_SESSION_ID + "=" + getCurrentWorkoutID();
		Cursor c = db.query(true, DBConstants.TABLE_STEP_INFO,
				DBConstants.STEP_INFO_ALL_KEYS, where, null, null, null, null,
				null);
		if (c != null) {
			c.moveToFirst();
		}

		List<DBContainers.StepInfo> current_step_container = new ArrayList<DBContainers.StepInfo>();

		if (c.moveToFirst()) {
			do {
				// Transfer Data
				DBContainers.StepInfo current_step = DBContainers.containers.new StepInfo();

				current_step.setTime_stamp(c.getDouble(DBConstants.COL_STEP_INFO_ELAPSED_TIME));
				current_step.setAltitude(c.getDouble(DBConstants.COL_STEP_INFO_ALTITUDE));
				current_step_container.add(current_step);
			} while (c.moveToNext());
		}
		c.close();
		return current_step_container;
	}
	/**
	 * Return a SessionInfo list of all workout sessions
	 * @author: jaygustin
	 * @return
	 */
	private List<SessionInfo> getAllCurrentWorkoutSessionInfo() {
		Cursor c = getAllWorkoutSessions();
		if (c != null) {
			c.moveToFirst();
		}
		List<DBContainers.SessionInfo> current_session_container = new ArrayList<DBContainers.SessionInfo>();
		if (c.moveToFirst()) {
			do {
				// Transfer Data
				DBContainers.SessionInfo current_session = DBContainers.containers.new SessionInfo();

				current_session.setDate(c.getString(DBConstants.COL_SESSION_INFO_DATE));
				current_session.setSession_id(c.getInt(DBConstants.COL_STEP_INFO_SESSION_ID));
				current_session.setTotal_step(c.getInt(DBConstants.COL_SESSION_INFO_TOTAL_STEP));
				current_session.setAverage_speed(c.getDouble(DBConstants.COL_SESSION_INFO_AVERAGE_SPEED));
				current_session_container.add(current_session);
			} while (c.moveToNext());
		}
		c.close();
		return current_session_container;
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
						getCurrentWorkoutID());		//Temp change me to workout session info ** 1 => getCurrentWorkoutID()
				initialValues.put(DBConstants.STEP_INFO_ELAPSED_TIME,
						current_step.getTime_stamp());
				initialValues.put(DBConstants.STEP_INFO_ALTITUDE, 
						current_step.getAltitude());
				// Insert it into the database.
				db.insert(DBConstants.TABLE_STEP_INFO, null, initialValues);
			}
			stepContainer.clear();
		}
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
		Cursor allCurrentSteps = db.query(true, DBConstants.TABLE_STEP_INFO,
				DBConstants.STEP_INFO_ALL_KEYS, where, null, null, null, null,
				null);
		
		int session_id = getCurrentWorkoutID();
		int totalStep = getTotalStep();
		
		//NEW UPDATEs
		double duration = mElapsed_ms;// In ms
		//UPDATED getTotalAverageSpeed METHOD
		double average_speed = getTotalAverageSpeed(totalStep, duration); // In steps/MIN
		double totalAltitudeMagnitude = getTotalAltitudeMagnitude(allCurrentSteps); // In wtv it is in StepInfo
		double totalAltitude = getTotalAltitude(allCurrentSteps); // In wtv it is in StepInfo
		double totalEnergy = getTotalEnergy(); 
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(DBConstants.KEY_ROWID, session_id);
		// No date because it is automatically put in
		initialValues.put(DBConstants.SESSION_INFO_AVERAGE_SPEED, average_speed);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_ENERGY, totalEnergy);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_ALTITUDE_MAGNITUDE, totalAltitudeMagnitude);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_ALTITUDE, totalAltitude);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_STEP, totalStep);
		initialValues.put(DBConstants.SESSION_INFO_TOTAL_DURATION, duration);
		
		allCurrentSteps.close();
		
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
	
	/**
	 *  The flow of the DB fucntion calls to start a workout
	 */
	public void start_workout() {
		this.insertRowIntoWorkoutSession();
	}
	
	/**
	 * The flow of the DB fucntion calls to stop a workout
	 */
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
		
		/*Might be need for deleting table values during testing
		 * 
		 * sqlite_sequence also needs to be cleared to reset
		 * the autoincrementors of the tables eg. "_id" colomn
		 * 
		db.execSQL("DELETE FROM " + DBConstants.TABLE_USER);
		db.execSQL("DELETE FROM " + DBConstants.TABLE_WORKOUT_SESSION);
		db.execSQL("DELETE FROM " + DBConstants.TABLE_STEP_INFO);
		db.execSQL("DELETE FROM " + DBConstants.TABLE_SESSION_INFO);
		
		db.execSQL("DELETE FROM sqlite_sequence WHERE name = '"+DBConstants.TABLE_USER+"'");
		db.execSQL("DELETE FROM sqlite_sequence WHERE name = '"+DBConstants.TABLE_WORKOUT_SESSION+"'");
		db.execSQL("DELETE FROM sqlite_sequence WHERE name = '"+DBConstants.TABLE_STEP_INFO+"'");
		db.execSQL("DELETE FROM sqlite_sequence WHERE name = '"+DBConstants.TABLE_SESSION_INFO+"'");
		*/
	}

	public void bufferStepInfo(DBContainers.StepInfo newStep) {
		stepContainer.add(newStep);
		if (stepContainer.size() > BUFFER_SIZE) {
			insertBufferRowsStepInfo();
		}
	}
	
	/**
	 * Returns a list of StepInfo Containers which contains all the steps
	 * of a workout selected by the index
	 * 
	 */
	 public List<DBContainers.StepInfo> getAllWorkoutSteps(int _id) {
		 String where = DBConstants.STEP_INFO_SESSION_ID + "=" + _id;

		 Cursor c = db.query(true, DBConstants.TABLE_STEP_INFO,
		 DBConstants.STEP_INFO_ALL_KEYS, where, null, null, null, null, null);

		 List<DBContainers.StepInfo> allWorkoutSteps = new ArrayList<DBContainers.StepInfo>();

		 if (c.moveToFirst()) {
			 do {
				 DBContainers.StepInfo aStep = DBContainers.containers.new StepInfo();
				 aStep.setSession_id(_id);
				 aStep.setTime_stamp(c.getDouble(DBConstants.COL_STEP_INFO_ELAPSED_TIME));
				 aStep.setAltitude(c.getDouble(DBConstants.COL_STEP_INFO_ALTITUDE));
	
				 allWorkoutSteps.add(aStep);
			 } while (c.moveToNext());
		 } else {
		 Log.d(TAG,"stepInfo Cursor is Empty!");
		 }

		 return allWorkoutSteps;
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
	
	/**
	 * @returns A cursor containing all the rows the in WorkoutSession Table
	 */
	public Cursor getAllWorkoutSessions() {
		Cursor c = db.query(true, DBConstants.TABLE_WORKOUT_SESSION,
				DBConstants.WORKOUT_SESSION_ALL_KEYS, null, null, null, null, null,
				null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	/**
	 * @returns A cursor containing all the rows the in WorkoutSession Table
	 */
	public Cursor getAllWorkoutSessionInfo() {
		Cursor c = db.query(true, DBConstants.TABLE_SESSION_INFO,
				DBConstants.SESSION_INFO_ALL_KEYS, null, null, null, null, null,
				null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	public List<DBContainers.StepInfo> getAllCurrentStepInfo(){
		insertBufferRowsStepInfo();
		List<DBContainers.StepInfo> current_step_container = getAllCurrentRowStepInfo();
		return current_step_container;
	}
	
	public boolean checkIfWorkoutSessionsExist () {
		Cursor c = db.query(true, DBConstants.TABLE_WORKOUT_SESSION,
				DBConstants.WORKOUT_SESSION_ALL_KEYS, null, null, null, null, null,
				null);
		
		return c != null;
	}
	
	/**
	 * @Returns SessionInfo with a the calculated values from SessionInfo
	 * table of a single workout session
	 */
	 public SessionInfo getSessionInfo(int _id) {
		 String where = DBConstants.KEY_ROWID + "=" + _id;

		 Cursor c = db.query(true, DBConstants.TABLE_SESSION_INFO,
		 DBConstants.SESSION_INFO_ALL_KEYS, where, null, null, null, null, null);
		 if(c != null) {
		 c.moveToFirst();
		 } else {
		 Log.d(TAG,"sessionInfo Cursor is Empty!");
		 }

		 DBContainers.SessionInfo sessionInfo = DBContainers.containers.new SessionInfo();
		 sessionInfo.setSession_id(c.getInt(DBConstants.COL_KEY_ROWID));
		 sessionInfo.setDate(c.getString(DBConstants.COL_SESSION_INFO_DATE));
		 sessionInfo.setAverage_speed(c.getDouble(DBConstants.COL_SESSION_INFO_AVERAGE_SPEED));
		 sessionInfo.setTotal_duration(c.getDouble(DBConstants.COL_SESSION_INFO_TOTAL_DURATION));
		 sessionInfo.setTotal_energy(c.getDouble(DBConstants.COL_SESSION_INFO_TOTAL_ENERGY));
		 sessionInfo.setTotal_step(c.getInt(DBConstants.COL_SESSION_INFO_TOTAL_STEP));
		 sessionInfo.setTotal_altitude_magnitude(c.getInt(DBConstants.COL_SESSION_INFO_TOTAL_ALTITUDE_MAGNITUDE));
		 sessionInfo.setTotal_altitude(c.getInt(DBConstants.COL_SESSION_INFO_TOTAL_ALTITUDE));

		 return sessionInfo;
	}
	public Cursor getSessionInfoCursor(int _id) {
		String where = DBConstants.KEY_ROWID + "=" + _id;
		
		Cursor c = db.query(true, DBConstants.TABLE_SESSION_INFO,
				DBConstants.SESSION_INFO_ALL_KEYS, where, null, null, null, null,
				null);
		if(c != null) {
			c.moveToFirst();
		} else {
			Log.d(TAG,"sessionInfo Cursor is Empty!");
		}
		
		
		return c;
	}
	/**
	 * @returns a SessionInfo container which contains avg of last x SessionsInfo
	 *  where x is specified in the UserSettings a.k.a. the User table
	 *  @author Phohawkenics
	 */
	public DBContainers.SessionInfo getLastSessionsInfo(){
		Cursor userSettings = getUserSetting();
		double listPref = userSettings.getInt(DBConstants.COL_USER_LIST_PREF);
		
		userSettings.close();
		
		Cursor currentWorkoutSession = getAllWorkoutSessions();
		
		List<Cursor> lastSessionInfos = new ArrayList<Cursor>();
		
		if (currentWorkoutSession.moveToLast()) {
			for (int i = 0; i < listPref; i++) {
				lastSessionInfos.add(
						getSessionInfoCursor(currentWorkoutSession.getInt(DBConstants.COL_KEY_ROWID))
						);
				if(currentWorkoutSession.moveToPrevious()) {
					// move to previous-- this line just has to be here
				} else {
					listPref = i + 1;
					break;
				}
			}
		}
		
		currentWorkoutSession.close();
		
		double avgSpeed = 0;
		double avgEnergy = 0;
		double avgAltitudeMagnitude = 0;
		double avgAltitude = 0;
		int avgStep = 0;
		double avgDuration = 0;
		
		//Get data
		for (Cursor currentSessionInfo: lastSessionInfos) {
			avgSpeed += currentSessionInfo.getDouble(DBConstants.COL_SESSION_INFO_AVERAGE_SPEED);
			avgEnergy += currentSessionInfo.getDouble(DBConstants.COL_SESSION_INFO_TOTAL_ENERGY);
			avgAltitudeMagnitude += currentSessionInfo.getDouble(DBConstants.COL_SESSION_INFO_TOTAL_ALTITUDE_MAGNITUDE);
			avgAltitude += currentSessionInfo.getDouble(DBConstants.COL_SESSION_INFO_TOTAL_ALTITUDE);
			avgStep += currentSessionInfo.getInt(DBConstants.COL_SESSION_INFO_TOTAL_STEP);
			avgDuration += currentSessionInfo.getDouble(DBConstants.COL_SESSION_INFO_TOTAL_DURATION);
		}
		
		for (Cursor currentSessionInfo: lastSessionInfos) {
			currentSessionInfo.close();
		}
		
		avgSpeed = avgSpeed/listPref;
		avgEnergy = avgEnergy/listPref;
		avgAltitudeMagnitude = avgAltitudeMagnitude/listPref;
		avgAltitude = avgAltitude/listPref;
		avgStep = (int) (avgStep/listPref);
		avgDuration = avgDuration/listPref;
		
		DBContainers.SessionInfo avgSessions = DBContainers.containers.new SessionInfo();
		
		avgSessions.setAverage_speed(avgSpeed);
		avgSessions.setTotal_energy(avgEnergy);
		avgSessions.setTotal_altitude_magnitude(avgAltitudeMagnitude);
		avgSessions.setTotal_altitude(avgAltitude);
		avgSessions.setTotal_step(avgStep);
		avgSessions.setTotal_duration(avgDuration);
		
		return avgSessions;
	}
	
	/**
	 * -@return a SessionInfo container in current calculated Statistics
	 */
	public DBContainers.SessionInfo getRealTimeStats() {
		String where = DBConstants.STEP_INFO_SESSION_ID + "=" + getCurrentWorkoutID();
		Cursor allCurrentSteps = db.query(true, DBConstants.TABLE_STEP_INFO,
				DBConstants.STEP_INFO_ALL_KEYS, where, null, null, null, null,
				null);
		
		int session_id = getCurrentWorkoutID();
		int totalStep = getTotalStep();
		double duration = getTotalDuration(allCurrentSteps);// In Seconds--> wtf not use elaspsed time in ms?
		double average_speed = getTotalAverageSpeed(totalStep, duration); // In steps/sec
		double totalAltitudeMagnitude = getTotalAltitudeMagnitude(allCurrentSteps); // In wtv it is in StepInfo
		double totalAltitude = getTotalAltitude(allCurrentSteps); // In wtv it is in StepInfo
		double totalEnergy = getTotalEnergy();
		
		allCurrentSteps.close();
		
		DBContainers.SessionInfo realTimeInfo = DBContainers.containers.new SessionInfo();
		realTimeInfo.setSession_id(session_id);
		realTimeInfo.setAverage_speed(average_speed);
		realTimeInfo.setDate(""); // date doesn't exist yet
		realTimeInfo.setTotal_altitude(totalAltitude);
		realTimeInfo.setTotal_altitude_magnitude(totalAltitudeMagnitude);
		realTimeInfo.setTotal_energy(totalEnergy);
		realTimeInfo.setTotal_duration(duration);
		realTimeInfo.setTotal_step(totalStep);
		
		return realTimeInfo;

	}
	
	/**
	 * Simply creates the row for
	 * the one and only user, should only be called once
	 * 
	 * @author Phohawkenics
	 */
	public void userInit () {
		insertRowIntoUser("Phohawkenics");
	}
	
	/**
	 * Takes a user container and updates the db in the fields where the
	 * user has been set
	 * 
	 * 		eg. passing a user where setHeight has been called will only
	 * push the newHeight that has been added and will not affect the others
	 * 
	 * 		eg. passing a user where setHeight and set age has been called
	 * will only push the height and age
	 * 
	 * To modify all settings, obviously setHeight,setWeight, setAge,
	 * setGender and setListPref will have to be called by the user container
	 * 
	 * THIS DOES NOT SUPPORT NAME CHANGE, it can easily be modified for it though
	 * 
	 * @author Phohawkenics
	 */
	public void setUser (DBContainers.User user) {
		// Get current values from the DB
		Cursor currentUserSettings = getUserSetting();
		
		double height = currentUserSettings.getDouble(DBConstants.COL_USER_HEIGHT);
		double weight = currentUserSettings.getDouble(DBConstants.COL_USER_WEIGHT);
		
		// Check which values have been set in the user container
		boolean heightChange = ( user.getHeight() != DBContainers.NO_CHANGE_FLAG );
		boolean weightChange = ( user.getWeight() != DBContainers.NO_CHANGE_FLAG );
		boolean ageChange = ( user.getAge() != DBContainers.NO_CHANGE_FLAG );
		boolean genderChange = ( user.getGender() != null );
		boolean listPrefChange = ( user.getList_pref() != DBContainers.NO_CHANGE_FLAG );
		
		ContentValues newValues = new ContentValues();

		if(heightChange) {
			double newHeight = user.getHeight();
			newValues.put(DBConstants.USER_HEIGHT, newHeight);
			height = newHeight;
		}
		
		if (weightChange) {
			double newWeight = user.getWeight();
			newValues.put(DBConstants.USER_WEIGHT, newWeight);
			weight = newWeight;
		}
		
		if(ageChange) {
			int newAge = user.getAge();
			newValues.put(DBConstants.USER_AGE, newAge);
		}
		
		if(genderChange) {
			boolean newGender = user.getGender();
			newValues.put(DBConstants.USER_GENDER, newGender);
		}
		
		if(listPrefChange) {
			newValues.put(DBConstants.USER_LIST_PREF, user.getList_pref());
		}
		
		if( heightChange || weightChange)
			newValues.put(DBConstants.USER_BMI, calculateBMI(height, weight));
		
		currentUserSettings.close();
		
		
		String where = DBConstants.KEY_ROWID + "=1";
		db.update(DBConstants.TABLE_USER, newValues, where, null);
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
			_db.execSQL(DBConstants.CREATE_PREFERENCES_SQL);
			_db.execSQL(DBConstants.CREATE_USER_SQL);
			_db.execSQL(DBConstants.CREATE_WORKOUT_SESSION_SQL);
			_db.execSQL(DBConstants.CREATE_STEP_INFO_SQL);
			_db.execSQL(DBConstants.CREATE_SESSION_INFO_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data!");

			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_PREFERENCES);
			_db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_USER);
			_db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_WORKOUT_SESSION);
			_db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_STEP_INFO);
			_db.execSQL("DROP TABLE IF EXISTS " + DBConstants.TABLE_SESSION_INFO);

			// Recreate new database:
			onCreate(_db);
		}
	}

	public List<String> getAllWorkoutSessionsInfo() {
		List<DBContainers.SessionInfo> current_session_container = getAllCurrentWorkoutSessionInfo();
		List<String> displayList = new ArrayList<String>();
		String display;

		for (DBContainers.SessionInfo current_session : current_session_container) {

			display = "";
			display = display + "Date : "
					+ current_session.getDate() + "\n"
					+ "total steps: "+ current_session.getTotal_step() + "\n" ;

			displayList.add(display);
		}

		return displayList;
	}

}
