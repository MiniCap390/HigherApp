package ca.concordia.sensortag.datasample;

public class DBConstants {

	// ///////////////////////////////////////////////////////////////////
	// DB Stuff
	// ///////////////////////////////////////////////////////////////////
	public static final String DATABASE_NAME = "HigherDB";
	public static final int DATABASE_VERSION = 1;
	
	// 
	// ///////////////////////////////////////////////////////////////////
	// Tables name
	// ///////////////////////////////////////////////////////////////////

	public static final String TABLE_STEP_INFO = "Step_Info";
	public static final String TABLE_PREFERENCES = "Preferences";
	
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
	
	public static final String[] PREFERENCES_ALL_KEYS = new String[] {PREFERENCES_STATUS, PREFERENCES_REC_DURATION,
															PREFERENCES_REC_SAMPLES, PREFERENCES_DATA_DATA,
															PREFERENCES_DATA_SAMPLES, PREFERENCES_ELAPSED};
	
	public static final int COL_PREFERENCES_STATUS = 0;
	public static final int COL_PREFERENCES_REC_DURATION = 1;
	public static final int COL_PREFERENCES_REC_SAMPLES = 2;
	public static final int COL_PREFERENCES_DATA_DATA = 3;
	public static final int COL_PREFERENCES_DATA_SAMPLES = 4;
	public static final int COL_PREFERENCES_ELAPSED = 5;
	//
	// ///////////////////////////////////////////////////////////////////
	// For Step_Info table
	// ///////////////////////////////////////////////////////////////////
	public static final String STEP_INFO_ELAPSED_TIME = "elapsed_time";
	public static final String STEP_INFO_X = "X_accel";
	public static final String STEP_INFO_Y = "Y_accel";
	public static final String STEP_INFO_Z = "Z_accel";
	
	public static final String[] STEP_INFO_ALL_KEYS = new String[] {STEP_INFO_ELAPSED_TIME, STEP_INFO_X, STEP_INFO_Y, STEP_INFO_Z};
	
	public static final int COL_STEP_INFO_ELAPSED_TIME = 0;
	public static final int COL_STEP_INFO_X = 1;
	public static final int COL_STEP_INFO_Y = 2;
	public static final int COL_STEP_INFO_Z = 3;
	
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
	
	public static final String CREATE_STEP_INFO_SQL =
			"create table " + TABLE_STEP_INFO
			+ " (" +  STEP_INFO_ELAPSED_TIME + " double not null, "
			+ STEP_INFO_X + " double not null, "
			+ STEP_INFO_Y + " double not null, "
			+ STEP_INFO_Z + " double not null"
			+ ");";

}		
			
			
			