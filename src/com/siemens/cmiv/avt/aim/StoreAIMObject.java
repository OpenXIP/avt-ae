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
