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
