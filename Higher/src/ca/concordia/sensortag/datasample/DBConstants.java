package ca.concordia.sensortag.datasample;

public class DBConstants {
	/////////////////////////////////////////////////////////////////////
	//	Constants & Data
	/////////////////////////////////////////////////////////////////////
	
	
	// ///////////////////////////////////////////////////////////////////
	// DB Stuff
	// ///////////////////////////////////////////////////////////////////
	public static final String DATABASE_NAME = "HigherDB";
	public static final int DATABASE_VERSION = 1;
	
	public static final String KEY_ROWID = "_id";
	public static final int COL_KEY_ROWID = 0;
	
	// 
	// ///////////////////////////////////////////////////////////////////
	// Tables name
	// ///////////////////////////////////////////////////////////////////
	public static final String TABLE_PREFERENCES = "Preferences";
	public static final String TABLE_USER = "User";
	public static final String TABLE_WORKOUT_SESSION = "Workout_Session";
	public static final String TABLE_STEP_INFO = "Step_Info";
	public static final String TABLE_SESSION_INFO = "Session_Info";

	//
	// ///////////////////////////////////////////////////////////////////
	// For Preferences table
	// ///////////////////////////////////////////////////////////////////
	
	public static final String PREFERENCES_STATUS = "status";
	public static final String PREFERENCES_REC_DURATION = "record_duration";
	public static final String PREFERENCES_REC_SAMPLES = "record_samples";
	public static final String PREFERENCES_DATA_DATA = "data";
	public static final String PREFERENCES_DATA_SAMPLES = "number_samples";
	public static final String PREFERENCES_ELAPSED = "elapsed_time";
	
	public static final String[] PREFERENCES_ALL_KEYS = new String[] {
		PREFERENCES_STATUS, PREFERENCES_REC_DURATION,
		PREFERENCES_REC_SAMPLES, PREFERENCES_DATA_DATA,
		PREFERENCES_DATA_SAMPLES, PREFERENCES_ELAPSED
		};
	
	public static final int COL_PREFERENCES_STATUS = 0;
	public static final int COL_PREFERENCES_REC_DURATION = 1;
	public static final int COL_PREFERENCES_REC_SAMPLES = 2;
	public static final int COL_PREFERENCES_DATA_DATA = 3;
	public static final int COL_PREFERENCES_DATA_SAMPLES = 4;
	public static final int COL_PREFERENCES_ELAPSED = 5;
	
	// 
	// ///////////////////////////////////////////////////////////////////
	// For User table 
	// ///////////////////////////////////////////////////////////////////
	public static final String USER_NAME = "name";
	public static final String USER_AGE = "age";
	public static final String USER_GENDER = "gender";
	public static final String USER_WEIGHT = "weight";
	public static final String USER_HEIGHT = "height";
	public static final String USER_BMI = "bmi";
	public static final String USER_LIST_PREF = "list_pref";
	
	public static final String[] USER_ALL_KEYS = new String[] {
		KEY_ROWID, USER_NAME, USER_AGE, USER_GENDER, USER_WEIGHT,
		USER_HEIGHT, USER_BMI, USER_LIST_PREF
		};
	
	public static final int COL_USER_NAME = 1;
	public static final int COL_USER_AGE = 2;
	public static final int COL_USER_GENDER = 3;
	public static final int COL_USER_WEIGHT = 4;
	public static final int COL_USER_HEIGHT = 5;
	public static final int COL_USER_BMI = 6;
	public static final int COL_USER_LIST_PREF = 7;
	
	//
	// ///////////////////////////////////////////////////////////////////
	// For Workout_Session table
	// ///////////////////////////////////////////////////////////////////
	public static final String WORKOUT_SESSION_START_TIME = "start_time";
	public static final String WORKOUT_SESSION_END_TIME = "end_time";
	
	public static final String[] WORKOUT_SESSION_ALL_KEYS = new String[] {
		KEY_ROWID, WORKOUT_SESSION_START_TIME, WORKOUT_SESSION_END_TIME
		};
	
	public static final int COL_WORKOUT_SESSION_START_TIME = 1;
	public static final int COL_WORKOUT_SESSION_END_TIME = 2;
	
	//
	// ///////////////////////////////////////////////////////////////////
	// For Step_Info table
	// ///////////////////////////////////////////////////////////////////
	public static final String STEP_INFO_SESSION_ID = "session_id";
	public static final String STEP_INFO_TIME_STAMP = "time_stamp";
	public static final String STEP_INFO_ALTITUDE = "altitude";
	
	public static final String[] STEP_INFO_ALL_KEYS = new String[] {
		KEY_ROWID, STEP_INFO_SESSION_ID,
		STEP_INFO_TIME_STAMP, STEP_INFO_ALTITUDE
		};
	
	public static final int COL_STEP_INFO_SESSION_ID = 1;
	public static final int COL_STEP_INFO_TIME_STAMP = 2;
	public static final int COL_STEP_INFO_ALTITUDE = 3;
	
	//
	// ///////////////////////////////////////////////////////////////////
	// For Session_Info table
	// ///////////////////////////////////////////////////////////////////
	public static final String SESSION_INFO_DATE = "date";
	public static final String SESSION_INFO_AVERAGE_SPEED = "average_speed";
	public static final String SESSION_INFO_TOTAL_ENERGY = "total_energy";
	public static final String SESSION_INFO_TOTAL_ALTITUDE_MAGNITUDE = "total_altitude_magnitude";
	public static final String SESSION_INFO_TOTAL_ALTITUDE = "total_altitude";
	public static final String SESSION_INFO_TOTAL_STEP = "total_step";
	public static final String SESSION_INFO_TOTAL_DURATION = "total_duration";
	
	public static final String[] SESSION_INFO_ALL_KEYS = new String[] {
		KEY_ROWID, SESSION_INFO_DATE, SESSION_INFO_AVERAGE_SPEED,
		SESSION_INFO_TOTAL_ENERGY, SESSION_INFO_TOTAL_ALTITUDE_MAGNITUDE, SESSION_INFO_TOTAL_ALTITUDE,
		SESSION_INFO_TOTAL_STEP, SESSION_INFO_TOTAL_DURATION
		};
	
	public static final int COL_SESSION_INFO_DATE = 1;
	public static final int COL_SESSION_INFO_AVERAGE_SPEED = 2;
	public static final int COL_SESSION_INFO_TOTAL_ENERGY = 3;
	public static final int COL_SESSION_INFO_TOTAL_ALTITUDE_MAGNITUDE = 4;
	public static final int COL_SESSION_INFO_TOTAL_ALTITUDE = 5;
	public static final int COL_SESSION_INFO_TOTAL_STEP = 6;
	public static final int COL_SESSION_INFO_TOTAL_DURATION = 7;
	//
	// ///////////////////////////////////////////////////////////////////
	// Create table SQLite statement
	// ///////////////////////////////////////////////////////////////////
	
	public static final String CREATE_PREFERENCES_SQL =
			"create table " + TABLE_PREFERENCES
			+ " (" +  PREFERENCES_STATUS + " text default 'NOT_STARTED', "
			+ PREFERENCES_REC_DURATION + " long default 0, "
			+ PREFERENCES_REC_SAMPLES + " int default 0, "
			+ PREFERENCES_DATA_DATA + " text default '', "
			+ PREFERENCES_DATA_SAMPLES + " text, "
			+ PREFERENCES_ELAPSED + " long default 0"
			+ ");";
	
	public static final String CREATE_USER_SQL =
			"create table " + TABLE_USER
			+ " (" + KEY_ROWID + " integer primary key autoincrement, "
			+ USER_NAME + " text not null, "
			+ USER_AGE + " integer default 25, "
			+ USER_GENDER + " boolean default 1, "
			+ USER_WEIGHT + " double default 70, "
			+ USER_HEIGHT + " double default 1.8, "
			+ USER_BMI + " double default 21.6,"
			+ USER_LIST_PREF + " int default 5"
			+ ");";
	
	public static final String CREATE_WORKOUT_SESSION_SQL =
			"create table " + TABLE_WORKOUT_SESSION
			+ " (" + KEY_ROWID + " integer primary key autoincrement, "
			+ WORKOUT_SESSION_START_TIME + " text default (time('now','localtime')), "
			+ WORKOUT_SESSION_END_TIME + " text"
			+ ");";
	
	public static final String CREATE_STEP_INFO_SQL =
			"create table " + TABLE_STEP_INFO
			+ " (" + KEY_ROWID + " integer primary key autoincrement, "
			+ STEP_INFO_SESSION_ID + " integer not null, "
			+ STEP_INFO_TIME_STAMP + " text default (time('now','localtime')), "
			+ STEP_INFO_ALTITUDE + " double not null"
			+ ");";
	
	public static final String CREATE_SESSION_INFO_SQL =
			"create table " + TABLE_SESSION_INFO
			+ " (" + KEY_ROWID + " integer primary key autoincrement, "
			+ SESSION_INFO_DATE + " text default (date('now','localtime')), "
			+ SESSION_INFO_AVERAGE_SPEED + " double not null, "
			+ SESSION_INFO_TOTAL_ENERGY + " double not null, "
			+ SESSION_INFO_TOTAL_ALTITUDE_MAGNITUDE + " double not null,"
			+ SESSION_INFO_TOTAL_ALTITUDE + " double not null,"
			+ SESSION_INFO_TOTAL_STEP + " integer not null,"
			+ SESSION_INFO_TOTAL_DURATION + " double not null"
			+ ");";
	
}
