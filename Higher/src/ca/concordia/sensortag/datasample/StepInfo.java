package ca.concordia.sensortag.datasample;


public class StepInfo {
	private double elapsed_time;
	private double x;
	private double y;
	private double z;
	
	public double getElapsed_time() {
		return elapsed_time;
	}
	public void setElapsed_time(double elapsed_time) {
		this.elapsed_time = elapsed_time;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	public double getZ() {
		return z;
	}
	
	public void setXYZ(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}
