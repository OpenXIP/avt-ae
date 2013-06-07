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

public class StoreAIMObject {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		assert args.length > 0 : "Input AIM object and Attached DICOM segment object should be provided!";
		
		String aimFile = args[0];
		File aim = new File(aimFile);
		
		assert aim != null : "Input AIM object should exist!"; 

		String attachmentFile = args[1];
		File attachment = new File(attachmentFile);
		
		assert attachment != null : "Input DICOM segment object should exist!"; 

		storeObject(aim, attachment);

	}
	public static void storeObject(File aimFile, File attachmentFile){
//		ADFacade facade = new DefaultADFacadeImpl();
//		ImageAnnotation annotation;
//		
//		try {
//			annotation = AnnotationIO.loadAnnotationWithAttachment(aimFile, attachmentFile);
//			
//			AnnotationIO.saveOrUpdateAnnotation(annotation);
//			
////			User user = new User();
////			user.setUserName("Algorithm");
////			user.setUserId(0);
////			user.setRoleInt(1);
//			
////			facade.saveAnnotation(annotation, user, "Algorithm");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JDOMException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}
