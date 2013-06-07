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
package com.siemens.cmiv.avt.aim;

import java.io.File;
import java.io.IOException;

public class StoreDicomObject {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		assert args.length > 1 : "Input DICOM object should be provided!";
		
		String folder = args[0];
	
		File dir = new File(folder);
		
		String[] children = dir.list();
		if (children == null) {
		    return;
		} else {
		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        String filename = folder + "\\" + children[i];
		        
				try {
					storeObject(filename);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}
	}
	public static void storeObject(String dcmFile) throws IOException{
//		File dcm = new File(dcmFile);
//		DicomObject dob;
//		
//		dob = DicomParser.read(dcm);
//		String SOPInstanceUID = dob.getString(Tag.SOPInstanceUID);
//		DicomIO.saveOrUpdateDicom(dob);		
	}
	
 }
