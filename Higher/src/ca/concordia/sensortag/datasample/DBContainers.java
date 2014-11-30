package ca.concordia.sensortag.datasample;

public class DBContainers {
	public static final int NO_CHANGE_FLAG = -1;
	
	public class User {
		String name = null;
		int age = NO_CHANGE_FLAG;
		Boolean gender= null; // true = male, false = female
		double height = NO_CHANGE_FLAG;
		double weight = NO_CHANGE_FLAG;
		double bmi = NO_CHANGE_FLAG;
		int list_pref = NO_CHANGE_FLAG;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public Boolean getGender() {
			return gender;
		}
		public void setGender(boolean gender) {
			this.gender = gender;
		}
		public double getHeight() {
			return height;
		}
		public void setHeight(double height) {
			this.height = height;
		}
		public double getWeight() {
			return weight;
		}
		public void setWeight(double weight) {
			this.weight = weight;
		}
		public void setBMI (double bmi) {
			this.bmi = bmi;
		}
		
		public double getBMI () {
			return this.bmi;
		}
		
		public int getList_pref() {
			return list_pref;
		}
		public void setList_pref(int list_pref) {
			this.list_pref = list_pref;
		}
	}
	
	public class WorkoutSession {
		int session_id;
		String start_time;
		String end_time;
		
		public int getSession_id() {
			return session_id;
		}
		public void setSession_id(int session_id) {
			this.session_id = session_id;
		}
		public String getStart_time() {
			return start_time;
		}
		public void setStart_time(String start_time) {
			this.start_time = start_time;
		}
		public String getEnd_time() {
			return end_time;
		}
		public void setEnd_time(String end_time) {
			this.end_time = end_time;
		}
	}
	
	public class StepInfo {
		int session_id;
		double time_stamp;
		double altitude;
		public int getSession_id() {
			return session_id;
		}
		public void setSession_id(int session_id) {
			this.session_id = session_id;
		}
		public double getTime_stamp() {
			return time_stamp;
		}
		public void setTime_stamp(double time_stamp) {
			this.time_stamp = time_stamp;
		}
		public double getAltitude() {
			return altitude;
		}
		public void setAltitude(double altitude) {
			this.altitude = altitude;
		}
		
	}
	
	public class SessionInfo {
		int session_id;
		String date;
		double average_speed;
		double total_energy;
		double total_altitude_magnitude;
		double total_altitude;
		int total_step;
		double total_duration;
		
		public int getSession_id() {
			return session_id;
		}
		public void setSession_id(int session_id) {
			this.session_id = session_id;
		}
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public double getAverage_speed() {
			return average_speed;
		}
		public void setAverage_speed(double average_speed) {
			this.average_speed = average_speed;
		}
		public double getTotal_energy() {
			return total_energy;
		}
		public void setTotal_energy(double total_energy) {
			this.total_energy = total_energy;
		}
		public double getTotal_altitude_magnitude() {
			return total_altitude_magnitude;
		}
		public void setTotal_altitude_magnitude(double total_altitude_magnitude) {
			this.total_altitude_magnitude = total_altitude_magnitude;
		}
		public double getTotal_altitude() {
			return total_altitude;
		}
		public void setTotal_altitude(double total_altitude) {
			this.total_altitude = total_altitude;
		}
		public int getTotal_step() {
			return total_step;
		}
		public void setTotal_step(int total_step) {
			this.total_step = total_step;
		}
		public double getTotal_duration() {
			return total_duration;
		}
		public void setTotal_duration(double total_duration) {
			this.total_duration = total_duration;
		}
		
	}
}
