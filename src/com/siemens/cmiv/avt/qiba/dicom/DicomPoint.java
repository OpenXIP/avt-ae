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
