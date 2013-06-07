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

import gme.cacore_cacore._3_2.edu_northwestern_radiology.AnatomicEntity;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Annotation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Calculation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.CalculationResult;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.CalculationResultIdentifier;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Coordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.DICOMImageReference;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Data;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Equipment;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.GeometricShape;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Image;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageReference;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImagingObservation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImagingObservationCharacteristic;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ObjectFactory;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Segmentation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.SegmentationIdentifier;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.SpatialCoordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.TwoDimensionSpatialCoordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Annotation.AnatomicEntityCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Annotation.CalculationCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Annotation.ImagingObservationCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Calculation.CalculationResultCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.GeometricShape.SpatialCoordinateCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation.GeometricShapeCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation.ImageReferenceCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation.SegmentationCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImagingObservation.ImagingObservationCharacteristicCollection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.SequenceAttribute;
import com.pixelmed.dicom.SequenceItem;
import com.pixelmed.dicom.UIDGenerator;

public class CreateAIMObject {
	
	private ObjectFactory factory = new ObjectFactory(); 
	
	private String aim_Version = "TCGA";
	private String aim_SchemeDesignator = "AVT2";
	private String aim_SchemaVersion = "v0_rv1";
	private String aim_CodeValue = "AVT2";
	private String aim_CodeMeaning = "CDRH baseline target lesion";
	
	private String annotation_Reader = "";
	private int annotation_Role = 0;
	
	private String algo_manufacturer = "";
	private String algo_model = "";
	private String algo_version = "";
	
	private String patient_Name = "";
	private String patient_ID = "";
	private String patient_Gender = "";
	
	public CreateAIMObject(){
		aim_Version = "TCGA";
		aim_SchemeDesignator = "AVT2";
		aim_SchemaVersion = "v0_rv1";
		aim_CodeValue = "AVT005";
		aim_CodeMeaning = "Tumor volme segmentation";
		
		annotation_Reader = "Reader1";
		annotation_Role = 0;
		
		algo_manufacturer = "Siemens";
		algo_model = "AVT2EXT";
		algo_version = "0.1.2";

		patient_Name = "";
		patient_ID = "";
		patient_Gender = "";		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		assert args.length > 0 : "Input DICOM segment object and Output AIM object should be provided!";
		
		String aimFile = args[0];
		File aim = new File(aimFile);
		
		String seed = args[1];
		String uid = args[2];
		
		String dcmFile = args[3];
		File dcm = new File(dcmFile);
		assert dcm != null : "Input DICOM segment object should exist!"; 
		
		CreateAIMObject aimCreator = new CreateAIMObject();
		aimCreator.marshallVolume(aim, seed, uid, dcm);
	}
	
	public void setAIMInformation(String version, String schemeDesignator, String schemaVersion, String codeValue, String codeMeaning){
		aim_Version = version;
		aim_SchemeDesignator = schemeDesignator;
		aim_SchemaVersion = schemaVersion;
		aim_CodeValue = codeValue;
		aim_CodeMeaning = codeMeaning;
	}
	
	public void setAnnotationInformation(String annotator, int role){
		annotation_Reader = annotator;
		annotation_Role = role;
	}
	
	public void setAlgorithmInformatation(String manufacturer, String model, String version){
		algo_manufacturer = manufacturer;
		algo_model = model;
		algo_version = version;
	}
	
	public void setPatientInformation(String pat_Name, String pat_ID, String pat_Gender){
		patient_Name = pat_Name;
		patient_ID = pat_ID;
		patient_Gender = pat_Gender;
	}
	
	public void marshallVolume(File aimFile, String seedPoint, String seedUID,
			File segDcm) {
		try {
			//create ImageAnnotation
			ImageAnnotation annot = factory.createImageAnnotation();
			annot.setId(BigInteger.ZERO);
			annot.setAimVersion(aim_Version);
			annot.setDateTime(getCurrentTime());
			annot.setName(annotation_Reader);
			annot.setCodingSchemeDesignator(aim_SchemeDesignator);
			annot.setCodingSchemeVersion(aim_SchemaVersion);
			annot.setCodeValue(aim_CodeValue);
			annot.setCodeMeaning(aim_CodeMeaning);

			UIDGenerator uid = new UIDGenerator();
			annot.setUniqueIdentifier(uid.getNewUID());
	
			//create user
			gme.cacore_cacore._3_2.edu_northwestern_radiology.User user = factory.createUser();
			user.setName(annotation_Reader);
			user.setId(new BigInteger("0"));
			user.setLoginName("wustl");
			switch (annotation_Role) {
			case 0://nominal ground truth
				user.setRoleInTrial("Norminal GroundTruth");
				break;
				
			case 1://algorithm
				user.setRoleInTrial("Algorithm");
				break;
			}
			Annotation.User user2 = factory.createAnnotationUser();
			user2.setUser(user);
			annot.setUser(user2);					
			
			//create Equipment
			annot.setEquipment(assembleEquipment(algo_manufacturer, algo_model, algo_version));
			
			//create AnatomicEntity
			annot.setAnatomicEntityCollection(assmbleAnatomicEntity());

			//create GeometricShape Collection to store the seed
			annot.setGeometricShapeCollection(assmblePolylineShapeCollection(seedPoint, seedUID));

			//create imagingObservation
//			annot.setImagingObservationCollection(assmbleImageObservations());			
			
			//create segmentation Collection
			List<File> segFiles = new ArrayList<File>();
			segFiles.add(segDcm);
			annot.setSegmentationCollection(assembleSegmentationCollection(segFiles));
			
			//create imageReference collection
			annot.setImageReferenceCollection(assembleImageReferenceCollection(segDcm));

			//Create patient
			gme.cacore_cacore._3_2.edu_northwestern_radiology.Patient pat = factory.createPatient();
			
			pat.setId(BigInteger.ZERO);
			
			//PatientName
			pat.setName(patient_Name);
			
			//PatientID
			pat.setPatientID(patient_ID);

			//PatientSex
			pat.setSex(patient_Gender);
			
			ImageAnnotation.Patient _pat = factory.createImageAnnotationPatient();
			_pat.setPatient(pat);
			annot.setPatient(_pat);

			try {
		   	   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
	    	   Marshaller marshaller = jaxbContext.createMarshaller();
	           marshaller.setProperty("jaxb.schemaLocation", "gme://caCORE.caCORE/3.2/edu.northwestern.radiology.AIM AIM_TCGA09302009_XML.xsd");
	           JAXBElement<ImageAnnotation> catalogElement = factory.createImageAnnotation(annot);
	           marshaller.marshal(catalogElement,new FileOutputStream(aimFile));
 			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public void marshallRECIST(File aimFile, File segDcm, String recist, String recistPoint,
			String recistUID) {
		try {
			//create ImageAnnotation
			ImageAnnotation annot = factory.createImageAnnotation();
			annot.setId(BigInteger.ZERO);
			annot.setAimVersion(aim_Version);
			annot.setDateTime(getCurrentTime());
			annot.setName(annotation_Reader);
			annot.setCodingSchemeDesignator(aim_SchemeDesignator);
			annot.setCodingSchemeVersion(aim_SchemaVersion);
			annot.setCodeValue(aim_CodeValue);
			annot.setCodeMeaning(aim_CodeMeaning);

			UIDGenerator uid = new UIDGenerator();
			annot.setUniqueIdentifier(uid.getNewUID());
	
			//create user
			gme.cacore_cacore._3_2.edu_northwestern_radiology.User user = factory.createUser();
			user.setName(annotation_Reader);
			user.setId(new BigInteger("0"));
			user.setLoginName("wustl");
			switch (annotation_Role) {
			case 0://nominal ground truth
				user.setRoleInTrial("Norminal GroundTruth");
				break;
				
			case 1://algorithm
				user.setRoleInTrial("Algorithm");
				break;
			}
			Annotation.User user2 = factory.createAnnotationUser();
			user2.setUser(user);
			annot.setUser(user2);					
			
			//create Equipment
			annot.setEquipment(assembleEquipment(algo_manufacturer, algo_model, algo_version));
			
			//create AnatomicEntity
			annot.setAnatomicEntityCollection(assmbleAnatomicEntity());

			//create Calculation collection
			annot.setCalculationCollection(assmbleCalculationCollection(recist, "mm"));

			//create GeometricShape Collection to store the seed
			annot.setGeometricShapeCollection(assmblePolylineShapeCollection(recistPoint, recistUID));

			//create imageReference collection
			annot.setImageReferenceCollection(assembleImageReferenceCollection(segDcm, recistUID));

			//Create patient
			gme.cacore_cacore._3_2.edu_northwestern_radiology.Patient pat = factory.createPatient();
			
			pat.setId(BigInteger.ZERO);
			
			//PatientName
			pat.setName(patient_Name);
			
			//PatientID
			pat.setPatientID(patient_ID);

			//PatientSex
			pat.setSex(patient_Gender);
			
			ImageAnnotation.Patient _pat = factory.createImageAnnotationPatient();
			_pat.setPatient(pat);
			annot.setPatient(_pat);

			try {
		   	   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
	    	   Marshaller marshaller = jaxbContext.createMarshaller();
	           marshaller.setProperty("jaxb.schemaLocation", "gme://caCORE.caCORE/3.2/edu.northwestern.radiology.AIM AIM_TCGA09302009_XML.xsd");
	           JAXBElement<ImageAnnotation> catalogElement = factory.createImageAnnotation(annot);
	           marshaller.marshal(catalogElement,new FileOutputStream(aimFile));
 			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void marshallWHO(File aimFile, File segDcm, String who, String whoPoint, String whoUID) {
		try {
			//create ImageAnnotation
			ImageAnnotation annot = factory.createImageAnnotation();
			annot.setId(BigInteger.ZERO);
			annot.setAimVersion(aim_Version);
			annot.setDateTime(getCurrentTime());
			annot.setName(annotation_Reader);
			annot.setCodingSchemeDesignator(aim_SchemeDesignator);
			annot.setCodingSchemeVersion(aim_SchemaVersion);
			annot.setCodeValue(aim_CodeValue);
			annot.setCodeMeaning(aim_CodeMeaning);

			UIDGenerator uid = new UIDGenerator();
			annot.setUniqueIdentifier(uid.getNewUID());
	
			//create user
			gme.cacore_cacore._3_2.edu_northwestern_radiology.User user = factory.createUser();
			user.setName(annotation_Reader);
			user.setId(new BigInteger("0"));
			user.setLoginName("wustl");
			switch (annotation_Role) {
			case 0://nominal ground truth
				user.setRoleInTrial("Norminal GroundTruth");
				break;
				
			case 1://algorithm
				user.setRoleInTrial("Algorithm");
				break;
			}
			Annotation.User user2 = factory.createAnnotationUser();
			user2.setUser(user);
			annot.setUser(user2);					
			
			//create Equipment
			annot.setEquipment(assembleEquipment(algo_manufacturer, algo_model, algo_version));
			
			//create AnatomicEntity
			annot.setAnatomicEntityCollection(assmbleAnatomicEntity());

			//create Calculation collection
			annot.setCalculationCollection(assmbleCalculationCollection(who, "mm2"));

			//create GeometricShape Collection to store the seed
			annot.setGeometricShapeCollection(assmblePolylineShapeCollection(whoPoint, whoUID));

			//create imageReference collection
			annot.setImageReferenceCollection(assembleImageReferenceCollection(segDcm, whoUID));

			//Create patient
			gme.cacore_cacore._3_2.edu_northwestern_radiology.Patient pat = factory.createPatient();
			
			pat.setId(BigInteger.ZERO);
			
			//PatientName
			pat.setName(patient_Name);
			
			//PatientID
			pat.setPatientID(patient_ID);

			//PatientSex
			pat.setSex(patient_Gender);
			
			ImageAnnotation.Patient _pat = factory.createImageAnnotationPatient();
			_pat.setPatient(pat);
			annot.setPatient(_pat);

			try {
		   	   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
	    	   Marshaller marshaller = jaxbContext.createMarshaller();
	           marshaller.setProperty("jaxb.schemaLocation", "gme://caCORE.caCORE/3.2/edu.northwestern.radiology.AIM AIM_TCGA09302009_XML.xsd");
	           JAXBElement<ImageAnnotation> catalogElement = factory.createImageAnnotation(annot);
	           marshaller.marshal(catalogElement,new FileOutputStream(aimFile));
 			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private CalculationCollection assmbleCalculationCollection(String value, String unit) {
		// set Calculation Collection
		CalculationCollection calculationColl = factory.createAnnotationCalculationCollection();
		List<Calculation> calculationList = calculationColl.getCalculation();
		
		Calculation cal = factory.createCalculation();
		
		if (unit.compareToIgnoreCase("mm") == 0){
			cal.setCodeMeaning("Long Axis of Tumor");
			cal.setCodeValue("AVT202");
			cal.setCodingSchemeDesignator("AVT2");
//			cal.setMathML("");
			cal.setId(BigInteger.ZERO);
			cal.setDescription("Linear Measurement");
			cal.setUid("0");
		}
		
		if (unit.compareToIgnoreCase("mm2") == 0){
			cal.setCodeMeaning("Cross Product of Tumor Diameters");
			cal.setCodeValue("AVT203");
			cal.setCodingSchemeDesignator("AVT2");
//			cal.setMathML("");
			cal.setId(BigInteger.ZERO);
			cal.setDescription("Area Measurement");
			cal.setUid("0");
		}
		
		CalculationResultCollection resultColl = factory.createCalculationCalculationResultCollection();
		List<CalculationResult> calculationResult = resultColl.getCalculationResult();
		calculationResult.add(getCalResult(value, unit));
		cal.setCalculationResultCollection(resultColl);

		calculationList.add(cal);
		
		return calculationColl;
	}
	
	private CalculationResult getCalResult(String value, String unit){
		CalculationResult calResult = factory.createCalculationResult();
		calResult.setNumberOfDimensions(BigInteger.valueOf(1));
		calResult.setId(BigInteger.ZERO);
		calResult.setUnitOfMeasure(unit);
		calResult.setType(CalculationResultIdentifier.SCALAR);
		
		// set dimensionCollection node
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Dimension dimension1 = factory.createDimension();
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Dimension dimension2 = factory.createDimension();
		if (unit.compareToIgnoreCase("mm") == 0){		
			dimension1.setId(BigInteger.ZERO);
			dimension1.setIndex(BigInteger.ZERO);
			dimension1.setLabel("Length");
			dimension1.setSize(BigInteger.ONE);
		}
		if (unit.compareToIgnoreCase("mm2") == 0){
			dimension1.setId(BigInteger.ZERO);
			dimension1.setIndex(BigInteger.ZERO);
			dimension1.setLabel("Length0");
			dimension1.setSize(BigInteger.ONE);
		}
			
		CalculationResult.DimensionCollection dimCollection = factory.createCalculationResultDimensionCollection();
		List<gme.cacore_cacore._3_2.edu_northwestern_radiology.Dimension> dimList = dimCollection.getDimension();
		dimList.add(dimension1);
		
		if (unit.compareToIgnoreCase("mm2") == 0){
			// dimension two
			dimension2.setId(BigInteger.ZERO);
			dimension2.setIndex(BigInteger.ONE);
			dimension2.setLabel("Length1");
			dimension2.setSize(BigInteger.ONE);
			
			dimList.add(dimension2);
		}
		
		calResult.setDimensionCollection(dimCollection);
		
		// set dataCollection node
		Coordinate coordinate = factory.createCoordinate();
		coordinate.setDimensionIndex(BigInteger.ZERO);
		coordinate.setId(BigInteger.ZERO);
		coordinate.setPosition(BigInteger.ZERO);
		
		Data.CoordinateCollection coordinateCollection = factory.createDataCoordinateCollection();
		List<Coordinate> cooList = coordinateCollection.getCoordinate();
		cooList.add(coordinate);
		
		Data data = factory.createData();
		data.setCoordinateCollection(coordinateCollection);
		data.setId(BigInteger.ZERO);
		data.setValue(Integer.parseInt(value));
		
		CalculationResult.DataCollection dataCollection = factory.createCalculationResultDataCollection();
		List<Data> dataList = dataCollection.getData();
		dataList.add(data);
		
		calResult.setDataCollection(dataCollection);
		
		return calResult;
	}
	
	private ImageReferenceCollection assembleImageReferenceCollection(
			File segDcm, String recistUID) {
		DICOMImageReference imageRef = factory.createDICOMImageReference();
		imageRef.setId(BigInteger.ZERO);
		
		//study
		DICOMImageReference.Study study = factory.createDICOMImageReferenceStudy();
		
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Study normalStudy = factory.createStudy();
		normalStudy.setId(BigInteger.ZERO);

		//study 

		//parse the DICOM segment object
		DicomInputStream segInput;
		AttributeList tags = new AttributeList();
		try {
			segInput = new DicomInputStream(segDcm);
			tags.read(segInput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		//study InstanceUID
		AttributeTag tag = new AttributeTag(0x20, 0x0d);
		Attribute attrib = tags.get(tag);
		if (attrib != null)
			normalStudy.setInstanceUID(attrib.getSingleStringValueOrEmptyString());
		
		//series
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Series seriesItem = factory.createSeries();
		
		//series Number
		tag = new AttributeTag(0x20, 0x11);
		attrib = tags.get(tag);
		if (attrib != null)
			seriesItem.setId(new BigInteger(attrib.getSingleStringValueOrEmptyString()));
		
		//series instance UID (as the FrameofReferenceUID)
		tag = new AttributeTag(0x20, 0x52);
		attrib = tags.get(tag);
		if (attrib != null)
			seriesItem.setInstanceUID(attrib.getSingleStringValueOrEmptyString());
		
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Series.ImageCollection imageColl = factory.createSeriesImageCollection();

		List<Image> imageList = imageColl.getImage();
		Image image = factory.createImage();
		image.setId(BigInteger.ZERO);
		
//		tag = new AttributeTag(0x08, 0x16);
//		attrib = tags.get(tag);
//		image.setSopClassUID(attrib.getSingleStringValueOrEmptyString());
		
		image.setSopInstanceUID(recistUID);
		
		imageList.add(image);
		
		seriesItem.setImageCollection(imageColl);
		
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Study.Series series = factory.createStudySeries();
		series.setSeries(seriesItem);
		
		normalStudy.setSeries(series);
		study.setStudy(normalStudy);
		
		imageRef.setStudy(study);
		
		ImageReferenceCollection imagingReferenceColl = factory.createImageAnnotationImageReferenceCollection();
		List<ImageReference> imageReferenceList = imagingReferenceColl.getImageReference();
		imageReferenceList.add(imageRef);
		
		return imagingReferenceColl;
	}
	
	private GeometricShapeCollection assmblePolylineShapeCollection( String seedPoint, String seedUID) {
			
		GeometricShapeCollection geoShapeColl = factory.createImageAnnotationGeometricShapeCollection();
		List<GeometricShape> geoShapeList = geoShapeColl.getGeometricShape();
		
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Polyline polyline = factory.createPolyline();
		polyline.setId(BigInteger.ZERO);
		polyline.setIncludeFlag(false);
//		polyline.setLineColor("");
//		polyline.setLineOpacity("");
//		polyline.setLineStyle("");
//		polyline.setLineThickness("");
		polyline.setShapeIdentifier(BigInteger.valueOf(0));
		
		SpatialCoordinateCollection  spatialCoordinateColl = factory.createGeometricShapeSpatialCoordinateCollection();
		List<SpatialCoordinate> spatialCoordinateList = spatialCoordinateColl.getSpatialCoordinate();
		
		String tmp = (seedPoint.replace('[', ' ')).replace(']', ' ');
		String[] buffer = tmp.trim().split(",");
		for (int i = 0; i < buffer.length/2; i++){
			//point
			TwoDimensionSpatialCoordinate twoDimSpaCoordinate = factory.createTwoDimensionSpatialCoordinate();
			twoDimSpaCoordinate.setId(BigInteger.valueOf(0));
			twoDimSpaCoordinate.setImageReferenceUID(seedUID);
			
			String val = (buffer[2*i]).trim();
			twoDimSpaCoordinate.setX(Integer.parseInt(val));
			val = (buffer[2*i+1]).trim();
			twoDimSpaCoordinate.setY(Integer.parseInt(val));
			twoDimSpaCoordinate.setCoordinateIndex(BigInteger.valueOf(0));
			twoDimSpaCoordinate.setReferencedFrameNumber(BigInteger.valueOf(i));
			
			spatialCoordinateList.add(twoDimSpaCoordinate);
		}
		
		polyline.setSpatialCoordinateCollection(spatialCoordinateColl);
		
		geoShapeList.add(polyline);
		
		return geoShapeColl;
	}
	  public AnatomicEntityCollection assmbleAnatomicEntity(){
	    	AnatomicEntityCollection anatomicentityCollection = factory.createAnnotationAnatomicEntityCollection();
	    	
	    	List<AnatomicEntity> anatomicentityList = anatomicentityCollection.getAnatomicEntity();
	    	AnatomicEntity anatomicentity = factory.createAnatomicEntity();
	    	
	    	anatomicentity.setId(BigInteger.ZERO);
	    	anatomicentity.setCodeValue("T-28000");
	    	anatomicentity.setCodeMeaning("Lung");
	    	anatomicentity.setCodingSchemeDesignator("SRT");
	    	anatomicentityList.add(anatomicentity);
	    	
	    	return anatomicentityCollection;
	   }

	   public ImagingObservationCollection assmbleImageObservations(){
			ImagingObservation observation = factory.createImagingObservation();
			observation.setId(BigInteger.ZERO);
			observation.setCodeMeaning("Nodule");
			observation.setCodeValue("M-03010");
			observation.setCodingSchemeDesignator("SRT");
						
			ImagingObservationCharacteristic observationCharacter = factory.createImagingObservationCharacteristic();
			observationCharacter.setCodeMeaning("irregularly shaped");
			observationCharacter.setCodeValue("RID5809");
			observationCharacter.setCodingSchemeDesignator("RadLex");
			observationCharacter.setId(BigInteger.ZERO);
			
			ImagingObservationCharacteristicCollection observationCharacterColl = factory.createImagingObservationImagingObservationCharacteristicCollection();
			List<ImagingObservationCharacteristic> observationCharacterList = observationCharacterColl.getImagingObservationCharacteristic();
			observationCharacterList.add(observationCharacter);
			
			observation.setImagingObservationCharacteristicCollection(observationCharacterColl);
			
			ImagingObservationCollection observationColl = factory.createAnnotationImagingObservationCollection();
			List<ImagingObservation> observationCollList = observationColl.getImagingObservation();
			observationCollList.add(observation);
			
			return observationColl;
	   }
	   
	   public SegmentationCollection assembleSegmentationCollection(List<File> segFiles){
		   SegmentationCollection segCollection = factory.createImageAnnotationSegmentationCollection();
		   List<Segmentation> segList = segCollection.getSegmentation();
		   
		   for (int i = 0; i < segFiles.size(); i++){
				Segmentation seg = factory.createSegmentation();
				seg.setId(new BigInteger(Integer.toString(i)));	
				seg.setType(SegmentationIdentifier.BINARY);
				   
				File segDcm = segFiles.get(i);
				if (!segDcm.exists())
					continue;
				//parse the DICOM segment object
				DicomInputStream segInput;
				AttributeList tags = new AttributeList();
				
				try {
					segInput = new DicomInputStream(segDcm);
					
					tags.read(segInput);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DicomException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   
				//get SOPInstanceUID
				AttributeTag tag = new AttributeTag(0x02, 0x03);
				Attribute attrib = tags.get(tag);
				if (attrib != null)
					seg.setReferencedSopInstanceUID(attrib.getSingleStringValueOrEmptyString());
				
				seg.setSegmentNumber(new BigInteger(Integer.toString(i)));
				
				//get SOPClassUID
				tag = new AttributeTag(0x02, 0x02);
				attrib = tags.get(tag);
				if (attrib != null)
					seg.setSopClassUID(attrib.getSingleStringValueOrEmptyString());
				
				UIDGenerator uid = new UIDGenerator();
				//sopInstanceUID
		        GregorianCalendar today = new GregorianCalendar();
				String strSOPInstanceUID;
				try {
					strSOPInstanceUID = uid.getNewSOPInstanceUID(
							Integer.toString(today.get(GregorianCalendar.HOUR_OF_DAY)),
							Integer.toString(today.get(GregorianCalendar.MINUTE)), 
							Integer.toString(today.get(GregorianCalendar.SECOND)));
					
					seg.setSopInstanceUID(strSOPInstanceUID);
				} catch (DicomException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Segmentation.ImagingObservation segObservation= factory.createSegmentationImagingObservation();
				
				ImagingObservation observation = factory.createImagingObservation();
				observation.setId(new BigInteger("0"));
				observation.setCodeMeaning("Neoplasm");
				observation.setCodeValue("M-8FFFF");
				observation.setCodingSchemeDesignator("SRT");
							
				ImagingObservationCharacteristic observationCharacter = factory.createImagingObservationCharacteristic();
				observationCharacter.setCodeMeaning("irregularly shaped");
				observationCharacter.setCodeValue("RID5809");
				observationCharacter.setCodingSchemeDesignator("RadLex");
				observationCharacter.setId(new BigInteger("0"));
				
				ImagingObservationCharacteristicCollection observationCharacterCollection = factory.createImagingObservationImagingObservationCharacteristicCollection();
				List<ImagingObservationCharacteristic> observationCharacterList = observationCharacterCollection.getImagingObservationCharacteristic();
				observationCharacterList.add(observationCharacter);
				
				observation.setImagingObservationCharacteristicCollection(observationCharacterCollection);
				
				segObservation.setImagingObservation(observation);
				seg.setImagingObservation(segObservation);

				segList.add(seg);
		   }
		   
		   return segCollection;
	   }
	   
	   public Annotation.Equipment assembleEquipment(String manufacturer, String model, String version){
		   Annotation.Equipment equipInfor = factory.createAnnotationEquipment();
		   
		   Equipment equip = factory.createEquipment();
		   equip.setId(BigInteger.ZERO);
		   equip.setManufacturer(manufacturer);
		   equip.setManufacturerModelName(model);
		   equip.setSoftwareVersion(version);	   
		   
		   equipInfor.setEquipment(equip);
		   return equipInfor;
	   }
	   
	   public ImageReferenceCollection assembleImageReferenceCollection(File segDcm){
			DICOMImageReference imageRef = factory.createDICOMImageReference();
			imageRef.setId(BigInteger.ZERO);
			
			//study
			DICOMImageReference.Study study = factory.createDICOMImageReferenceStudy();
			
			gme.cacore_cacore._3_2.edu_northwestern_radiology.Study normalStudy = factory.createStudy();
			normalStudy.setId(BigInteger.ZERO);

			//study 

			//parse the DICOM segment object
			DicomInputStream segInput;
			AttributeList tags = new AttributeList();
			try {
				segInput = new DicomInputStream(segDcm);
				tags.read(segInput);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DicomException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			//study InstanceUID
			AttributeTag tag = new AttributeTag(0x20, 0x0d);
			Attribute attrib = tags.get(tag);
			if (attrib != null)
				normalStudy.setInstanceUID(attrib.getSingleStringValueOrEmptyString());
			
			//series
			gme.cacore_cacore._3_2.edu_northwestern_radiology.Series seriesItem = factory.createSeries();
			
			//series Number
			tag = new AttributeTag(0x20, 0x11);
			attrib = tags.get(tag);
			if (attrib != null)
				seriesItem.setId(new BigInteger(attrib.getSingleStringValueOrEmptyString()));
			
			//series instance UID (as the FrameofReferenceUID)
			tag = new AttributeTag(0x20, 0x52);
			attrib = tags.get(tag);
			if (attrib != null)
				seriesItem.setInstanceUID(attrib.getSingleStringValueOrEmptyString());
			
			gme.cacore_cacore._3_2.edu_northwestern_radiology.Series.ImageCollection imageColl = factory.createSeriesImageCollection();

			List<Image> imageList = imageColl.getImage();
			
			//shared functional groups sequence
			tag = new AttributeTag(0x5200,0x9229); 
			SequenceAttribute attribSeq = (SequenceAttribute)tags.get(tag);

			if (attribSeq != null){
				int num = attribSeq.getNumberOfItems();
				for (int i = 0; i < num; i++){
					SequenceItem itemSeq = attribSeq.getItem(i);
					AttributeList itemListSeq = itemSeq.getAttributeList();
					
					//Derivation Image sequence
					tag = new AttributeTag(0x0008,0x9124);
					SequenceAttribute sq = (SequenceAttribute)itemListSeq.get(tag);
					
					if (sq != null){
						for (int k = 0; k < sq.getNumberOfItems(); k++){
							SequenceItem item = sq.getItem(k);
							
							Image image = factory.createImage();
							image.setId(new BigInteger(Integer.toString(k)));
							
							AttributeList itemList = item.getAttributeList();
							tag = new AttributeTag(0x08, 0x16);
							attrib = itemList.get(tag);
							image.setSopClassUID(attrib.getSingleStringValueOrEmptyString());
							
							tag = new AttributeTag(0x08, 0x18);
							attrib = itemList.get(tag);
							image.setSopInstanceUID(attrib.getSingleStringValueOrEmptyString());
							
							imageList.add(image);
						}
					}
				}
			}
			
			seriesItem.setImageCollection(imageColl);
			
			gme.cacore_cacore._3_2.edu_northwestern_radiology.Study.Series series = factory.createStudySeries();
			series.setSeries(seriesItem);
			
			normalStudy.setSeries(series);
			study.setStudy(normalStudy);
			
			imageRef.setStudy(study);
			
			ImageReferenceCollection imagingReferenceColl = factory.createImageAnnotationImageReferenceCollection();
			List<ImageReference> imageReferenceList = imagingReferenceColl.getImageReference();
			imageReferenceList.add(imageRef);
			
			return imagingReferenceColl;
	   }
	  private XMLGregorianCalendar getCurrentTime(){
	    	DatatypeFactory factory = null;
	    	try{
	    	    factory = DatatypeFactory.newInstance();
	    	    
	    	}catch (DatatypeConfigurationException e){
	    		e.printStackTrace();
	    	}
	    	XMLGregorianCalendar xmlcalender = factory.newXMLGregorianCalendar();
		    Calendar calendar = Calendar.getInstance();
		    xmlcalender.setYear(calendar.get(Calendar.YEAR));
		    xmlcalender.setMonth(calendar.get(Calendar.MONTH) +1);
		    xmlcalender.setDay(calendar.get(Calendar.DAY_OF_MONTH));
		    xmlcalender.setHour(calendar.get(Calendar.HOUR_OF_DAY));
		    xmlcalender.setMinute(calendar.get(Calendar.MINUTE));
		    xmlcalender.setSecond(calendar.get(Calendar.SECOND));
		    return xmlcalender;
	    }
		

}
