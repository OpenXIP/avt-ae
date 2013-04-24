package com.siemens.cmiv.avt.qiba.dicom;

import java.util.ArrayList;

public class DicomImage {
    private String studyUID;
    private String seriesUID;
    private String patientSex;
    private String patientID;
    private String patientName;
    private ArrayList<DicomSlice> dicomSlices;
    public DicomImage(){
    	seriesUID = "";
    	studyUID = "";
    	patientSex = "";
    	patientID = "";
    	patientName = "";
    	dicomSlices = new ArrayList<DicomSlice>();
    }
	public String getStudyUID() {
		return studyUID;
	}
	public void setStudyUID(String studyUID) {
		this.studyUID = studyUID;
	}
	public String getSeriesUID() {
		return seriesUID;
	}
	public void setSeriesUID(String seriesUID) {
		this.seriesUID = seriesUID;
	}
	public ArrayList<DicomSlice> getDicomSlices() {
		return dicomSlices;
	}
	public void setDicomSlices(ArrayList<DicomSlice> dicomSlices) {
		this.dicomSlices = dicomSlices;
	}
	public String getPatientSex() {
		return patientSex;
	}
	public void setPatientSex(String patientSex) {
		this.patientSex = patientSex;
	}
	public String getPatientID() {
		return patientID;
	}
	public void setPatientID(String patientID) {
		this.patientID = patientID;
	}
	public String getPatientName() {
		return patientName;
	}
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
}
