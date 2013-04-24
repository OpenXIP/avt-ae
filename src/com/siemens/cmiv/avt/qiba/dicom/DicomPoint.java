package com.siemens.cmiv.avt.qiba.dicom;

public class DicomPoint {
	
	private float x;
    private float y;
    private float z;
    private double cos;
	public DicomPoint(float x,float y,float z){
		this.x = x;
		this.y = y;
		this.z = z;
		this.cos = 0;
	}
	public DicomPoint(){
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}
	public float getX() {
		return x;
	}
	public void setX(float x) {
		this.x = x;
	}
	public float getY() {
		return y;
	}
	public void setY(float y) {
		this.y = y;
	}
	public float getZ() {
		return z;
	}
	public void setZ(float z) {
		this.z = z;
	}
	public void set(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public double getCos() {
		return cos;
	}
	public void setCos(double cos) {
		this.cos = cos;
	}
}
