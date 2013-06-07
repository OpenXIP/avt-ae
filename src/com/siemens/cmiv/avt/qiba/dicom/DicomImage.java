/*
Copyright (c) 2010, Siemens Corporate Research a Division of Siemens Corporation 
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
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
