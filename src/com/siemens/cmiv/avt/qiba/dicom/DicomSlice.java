package com.siemens.cmiv.avt.qiba.dicom;

import java.util.ArrayList;

public class DicomSlice {
    private DicomPoint originPoint;
    private float pixelSpacingX;
    private float pixelSpacingY;
    private String sopInstanceUID;
    private String sopClassUID;
    private int referencedFrameNumber;
    private ArrayList<DicomPoint> slicePoints;
    public DicomSlice(){
    	sopInstanceUID = "";
    	sopClassUID = "";
    	originPoint = null;
    	slicePoints = null;
    	pixelSpacingX = 1;
    	pixelSpacingY = 1;
    	referencedFrameNumber = 0;
    }
	public String getSopClassUID() {
		return sopClassUID;
	}
	public void setSopClassUID(String sopClassUID) {
		this.sopClassUID = sopClassUID;
	}
	public DicomPoint getOriginPoint() {
		return originPoint;
	}
	public void setOriginPoint(DicomPoint originPoint) {
		this.originPoint = originPoint;
	}
	public String getSopInstanceUID() {
		return sopInstanceUID;
	}
	public void setSopInstanceUID(String sopInstanceUID) {
		this.sopInstanceUID = sopInstanceUID;
	}
	public ArrayList<DicomPoint> getSlicePoints() {
		return slicePoints;
	}
	public void setSlicePoints(ArrayList<DicomPoint> slicePoints) {
		this.slicePoints = slicePoints;
	}
	public float getPixelSpacingX() {
		return pixelSpacingX;
	}
	public void setPixelSpacingX(float pixelSpacingX) {
		this.pixelSpacingX = pixelSpacingX;
	}
	public float getPixelSpacingY() {
		return pixelSpacingY;
	}
	public void setPixelSpacingY(float pixelSpacingY) {
		this.pixelSpacingY = pixelSpacingY;
	}
	public int getReferencedFrameNumber() {
		return referencedFrameNumber;
	}
	public void setReferencedFrameNumber(int referencedFrameNumber) {
		this.referencedFrameNumber = referencedFrameNumber;
	}
	
}
