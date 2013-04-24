package com.siemens.cmiv.avt.qiba;

import gme.cacore_cacore._3_2.edu_northwestern_radiology.Annotation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Calculation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.CalculationResult;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.CalculationResultIdentifier;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Coordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.DICOMImageReference;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Data;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Dimension;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.GeometricShape;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Image;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageReference;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ObjectFactory;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Series;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.SpatialCoordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Study;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.TwoDimensionSpatialCoordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.GeometricShape.SpatialCoordinateCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation.GeometricShapeCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation.ImageReferenceCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Series.ImageCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Annotation.CalculationCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Calculation.CalculationResultCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.CalculationResult.DataCollection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.UIDGenerator;

import com.siemens.cmiv.avt.qiba.dicom.DicomImage;
import com.siemens.cmiv.avt.qiba.dicom.DicomPoint;
import com.siemens.cmiv.avt.qiba.dicom.DicomSlice;

public class QIBAImageAnnotation {

	private DicomImage dicomImage = null;
	private DocumentBuilder builder = null;
	private XPath path = null;
	private String OutPutXMLTpye = "";
	private String UserName = "";
	private ObjectFactory factory = null;
 	public String getUserName() {
		return UserName;
	}
	public String getOutPutXMLTpye() {
		return OutPutXMLTpye;
	}
	public void setOutPutXMLTpye(String outPutXMLTpye) {
		OutPutXMLTpye = outPutXMLTpye;
	}
	public void setUserName(String userName) {
		UserName = userName;
	}
	public DocumentBuilder getBuilder() {
		return builder;
	}
	public void setBuilder(DocumentBuilder builder) {
		this.builder = builder;
	}
	public XPath getPath() {
		return path;
	}
	public void setPath(XPath path) {
		this.path = path;
	}
	public DicomImage getDicomImage() {
		return dicomImage;
	}
	public void setDicomImage(DicomImage dicomImage) {
		this.dicomImage = dicomImage;
	}
	public QIBAImageAnnotation(){
		factory = new ObjectFactory();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XPathFactory xPathFactory = XPathFactory.newInstance();
		path = xPathFactory.newXPath();
	}
	// construct the dicomImage object.
	public boolean ConstructDicomImage(File dicomFolder, File dicomPointsFile ){
		if(false == dicomFolder.exists() || false == dicomPointsFile.exists()){
			return false;
		}
		if(false == dicomFolder.isDirectory() || false == dicomPointsFile.isFile()){
			return false;
		}
		File[] dicomFileList = dicomFolder.listFiles();
		
		// set Image Attributes to dicomImage object.
		setImageAttributes(dicomFileList[0]);
		
		ArrayList<SliceValue> sliceValueList = new ArrayList<SliceValue>();
		try {
		    Document doc = builder.parse(dicomPointsFile); 
		    NodeList seqNodeList = (NodeList)path.evaluate("/file-format/data-set/sequence", doc, XPathConstants.NODESET); 
		    int length = seqNodeList.getLength();
		    for(int i = 0; i < length; ++i){
		    	Element element = (Element)seqNodeList.item(i);
		    	if(!"ROIContourSequence".equals(element.getAttribute("name"))){
		    		continue;
		    	}
		    	NodeList pointsNodeList = (NodeList)path.evaluate("item/sequence[1]/item/element", element, XPathConstants.NODESET);
		    	int nums = pointsNodeList.getLength();
		    	for(int j = 0; j < nums; ++j){
		    		String pointValue = path.evaluate(".", pointsNodeList.item(j));
		    		StringBuffer pointStringBuffer = new StringBuffer(pointValue);
			    	ArrayList<DicomPoint> listPoint = new ArrayList<DicomPoint>();
			    	float zValue = getSliceZvalueandListPoint(pointStringBuffer,listPoint);
			    	
			    	// set points to Clockwise order
			    	listPoint  = setCounterClockWiseOrder(listPoint);
			    	
			    	SliceValue sliceValue = new SliceValue(zValue, listPoint);
			    	sliceValueList.add(sliceValue);
		    	}
		    }
		    // construct the dicomSlice objects.
		    ConstructDicomSlices(sliceValueList, dicomFileList);
		}
		catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		return true;
	}
	private ArrayList<DicomPoint> setCounterClockWiseOrder(ArrayList<DicomPoint> listPoint){
		float[] OriginPoint = calOriginCoordinate(listPoint);
		// the points of greater than 180
		ArrayList<DicomPoint> listPoint2 = new ArrayList<DicomPoint>();
		int num = listPoint.size();
		// calculating cosine
		for(int i = 0; i < num; ++i){
			DicomPoint point = listPoint.get(i);
			float vectorX = point.getX() - OriginPoint[0];
			float vectorY = point.getY() - OriginPoint[1];
			float vectorsquare = vectorX * vectorX + vectorY * vectorY;
			point.setCos(vectorX / Math.sqrt(vectorsquare));
			if(vectorY < 0){
				listPoint.remove(point);
				listPoint2.add(point);
				i--;
				num--;
			}
		}
		num = listPoint.size();
		// Bubble sort
		for(int j = 0; j < num; ++j){
			DicomPoint point = listPoint.get(j);
			for(int k = j + 1; k < num; ++k){
				if(listPoint.get(k).getCos() < point.getCos()){
					point = listPoint.get(k);
				}
			}
			int index = listPoint.indexOf(point);
			point = listPoint.remove(index);
			listPoint.add(0, point);
		}
		int num2 = listPoint2.size();
		for(int j = 0; j < num2; ++j){
			DicomPoint point = listPoint2.get(j);
			for(int k = j + 1; k < num2; ++k){
				if(listPoint2.get(k).getCos() > point.getCos()){
					point = listPoint2.get(k);
				}
			}
			int index = listPoint2.indexOf(point);
			point = listPoint2.remove(index);
			listPoint2.add(0, point);
		}
		listPoint.addAll(listPoint2);
		return listPoint;
	}
	// the origin value is the average value of the x and y.
	private float[] calOriginCoordinate(ArrayList<DicomPoint> listPoint){
		float[] originPoint = new float[2];
		originPoint[0] = 0;
		originPoint[1] = 0;
		int num = listPoint.size();
		for(int i = 0; i < num; ++i){
			originPoint[0] += listPoint.get(i).getX();
			originPoint[1] += listPoint.get(i).getY();
		}
		originPoint[0] = originPoint[0] / num;
		originPoint[1] = originPoint[1] / num;
		return originPoint;
	}
	// set Image Attributes, include Study Instance UID, Series Instance UID
	private void setImageAttributes(File dicomFile){
		DicomInputStream segInput = null;
		AttributeList tags = new AttributeList();
		try {
			segInput = new DicomInputStream(dicomFile);
			tags.read(segInput);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Study Instance UID
		AttributeTag tag = new AttributeTag(0x20, 0x0d);
		Attribute attrib = tags.get(tag);
		if (attrib != null){
			dicomImage.setStudyUID(attrib.getDelimitedStringValuesOrEmptyString());
		}
		// Series Instance UID
		tag = new AttributeTag(0x20, 0x0e);
		attrib = tags.get(tag);
		if (attrib != null){
			dicomImage.setSeriesUID(attrib.getDelimitedStringValuesOrEmptyString());
		}
		// Patient's Sex
		tag = new AttributeTag(0x10, 0x40);
		attrib = tags.get(tag);
		if (attrib != null){
			dicomImage.setPatientSex(attrib.getDelimitedStringValuesOrEmptyString());
		}
		// Patient ID
		tag = new AttributeTag(0x10, 0x20);
		attrib = tags.get(tag);
		if (attrib != null){
			dicomImage.setPatientID(attrib.getDelimitedStringValuesOrEmptyString());
		}
		// Patient's Name
		tag = new AttributeTag(0x10, 0x10);
		attrib = tags.get(tag);
		if (attrib != null){
			dicomImage.setPatientName(attrib.getDelimitedStringValuesOrEmptyString());
		}
	}
	// construct the dicomSlice objects.
	private void ConstructDicomSlices(ArrayList<SliceValue> sliceList, File[] dicomFileList){
		// For each slice, to obtain the z value and compared with the z value of each SliceValue object.
		// if equaling, construct DicomSlice object.
		int dicomFileNum = dicomFileList.length;
		int sliceListlength = sliceList.size();
		
		DicomInputStream segInput = null;
		AttributeList tags = new AttributeList();
		// Image Position 
		AttributeTag tag = new AttributeTag(0x20, 0x32);
		Attribute attrib = null;;
		float[] positions = null;
		int sliceNum = 0;
		for(int i = 0; i < dicomFileNum; ++i){
			File dicomFile = dicomFileList[i];
			try {
				segInput = new DicomInputStream(dicomFile);
				tags.read(segInput);
			    attrib = tags.get(tag);
				if (null != attrib){
				    positions = attrib.getFloatValues();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DicomException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if( null != positions){
				// get the z value
				float z = positions[2];
				for(int j = 0; j < sliceListlength; ++j){
					SliceValue sliceValue = sliceList.get(j);
					if(sliceValue.getzValueKey() == z){
						ConstructDicomSlicetoImage(tags, sliceValue);
						sliceNum++;
						break;
					}
				}
			}
			if(sliceNum == sliceListlength){
				break;
			}
		}
	}
	// Construct DicomSlice object and add the object to DicomImage.
	private void ConstructDicomSlicetoImage(AttributeList tags, SliceValue sliceValue){
		DicomSlice dicomSlice = new DicomSlice();
		DicomPoint originPoint = new DicomPoint();
		dicomSlice.setSlicePoints(sliceValue.getPointList());
		dicomSlice.setOriginPoint(originPoint);
		// SOP Instance UID
		AttributeTag tag = new AttributeTag(0x08, 0x18);
		Attribute attrib = tags.get(tag);
		if (attrib != null){
			dicomSlice.setSopInstanceUID(attrib.getDelimitedStringValuesOrEmptyString());
		}
		// SOP Class UID
		tag = new AttributeTag(0x08, 0x16);
		attrib = tags.get(tag);
		if (attrib != null){
			dicomSlice.setSopClassUID(attrib.getDelimitedStringValuesOrEmptyString());
		}
		// Pixel Spacing
		tag = new AttributeTag(0x28, 0x30);
		attrib = tags.get(tag);
		float[] pixelSpacing = null;
		try{
		    if (null != attrib){
		    	pixelSpacing = attrib.getFloatValues();
		    }
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null != pixelSpacing){
			dicomSlice.setPixelSpacingX(pixelSpacing[0]);
			dicomSlice.setPixelSpacingY(pixelSpacing[1]);
		}
		// Origin point
		tag = new AttributeTag(0x20, 0x32);
		attrib = tags.get(tag);
		float[] positions = null;
		try{
		    if (null != attrib){
		        positions = attrib.getFloatValues();
		    }
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null != positions){
			originPoint.set(positions[0], positions[1], positions[2]);
		}
		// referencedFrameNumber
		tag = new AttributeTag(0x20, 0x13);
		attrib = tags.get(tag);
		int[] referencedFrameNumber = null;
		try{
		    if (null != attrib){
		    	referencedFrameNumber = attrib.getIntegerValues();
		    }
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(null != referencedFrameNumber){
			dicomSlice.setReferencedFrameNumber(referencedFrameNumber[0]);
		}
		// add DicomSlice object to DicomImage object.
		dicomImage.getDicomSlices().add(dicomSlice);
	}
	// construct the point list and return the z value.
	private float getSliceZvalueandListPoint(StringBuffer pointBuffer, ArrayList<DicomPoint> ListPoint){
		
		String xStr = null;
		String yStr = null;
		String zStr = null;
		
		Float xFloat = null;
		Float yFloat = null;
		Float zFloat = null;
		
		int dataSeparator = pointBuffer.indexOf("\\");
		// construct a dicom point in a loop.
		while(-1 != dataSeparator){
			
			xStr = pointBuffer.substring(0, dataSeparator);
			pointBuffer.delete(0, dataSeparator + 1);
			dataSeparator = pointBuffer.indexOf("\\");
			
			yStr = pointBuffer.substring(0, dataSeparator);
			pointBuffer.delete(0, dataSeparator + 1);
			dataSeparator = pointBuffer.indexOf("\\");
			
			if(-1 != dataSeparator){
				zStr = pointBuffer.substring(0, dataSeparator);
				pointBuffer.delete(0,dataSeparator + 1);
				dataSeparator = pointBuffer.indexOf("\\");
			}else {
				zStr = pointBuffer.toString();
			}
			
			xFloat = Float.valueOf(xStr);
			yFloat = Float.valueOf(yStr);
			zFloat = Float.valueOf(zStr);
			
			DicomPoint point = new DicomPoint(xFloat.floatValue(), yFloat.floatValue(), zFloat.floatValue());
			if(false == ComparePointValue(ListPoint, point)){
				ListPoint.add(point);
			}
		}
		return zFloat.floatValue();
	}
	private boolean ComparePointValue(ArrayList<DicomPoint> ListPoint,DicomPoint point){
		int size = ListPoint.size();
		for(int i = 0; i < size; ++i){
			DicomPoint dicomPoint = ListPoint.get(i);
			if((dicomPoint.getX() == point.getX()) && 
			   (dicomPoint.getY() == point.getY()) && 
			   (dicomPoint.getZ() == point.getZ())){
				return true;
			}
		}
		return false;
	}
	private DICOMImageReference getDicomImageRef(){
		//create imageReference collection
		DICOMImageReference imageRef = factory.createDICOMImageReference();
		imageRef.setId(BigInteger.ZERO);
		
		//study
		DICOMImageReference.Study study = factory.createDICOMImageReferenceStudy();
		
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Study normalStudy = factory.createStudy();
		normalStudy.setId(BigInteger.ZERO);

		//study 
		normalStudy.setInstanceUID(dicomImage.getStudyUID());
		
		//series
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Series seriesItem = factory.createSeries();
		
		//series Number
		seriesItem.setId(BigInteger.ZERO);
		
		//series instance UID (as the FrameofReferenceUID)
		seriesItem.setInstanceUID(dicomImage.getSeriesUID());
		
		// Sop instance UID and Sop Class UID
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Series.ImageCollection imageColl = factory.createSeriesImageCollection();
		List<Image> imageList = imageColl.getImage();
		ArrayList<DicomSlice> dicomSliceList = dicomImage.getDicomSlices();
		int sliceNum = dicomSliceList.size();
		for(int i = 0; i < sliceNum; ++i){
			DicomSlice slice = dicomSliceList.get(i);
			Image image = factory.createImage();
			image.setId(BigInteger.valueOf(i));
			image.setSopClassUID(slice.getSopClassUID());
			image.setSopInstanceUID(slice.getSopInstanceUID());
			imageList.add(image);
		}
		
		seriesItem.setImageCollection(imageColl);
		
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Study.Series series = factory.createStudySeries();
		series.setSeries(seriesItem);
		
		normalStudy.setSeries(series);
		study.setStudy(normalStudy);
		
		imageRef.setStudy(study);
		return imageRef;
	}
	private gme.cacore_cacore._3_2.edu_northwestern_radiology.Patient getPatient(){
		//Create patient
		gme.cacore_cacore._3_2.edu_northwestern_radiology.Patient pat = factory.createPatient();
		
		pat.setId(BigInteger.ZERO);
		
		//PatientName
		pat.setName(dicomImage.getPatientName());
		
		//PatientID
		pat.setPatientID(dicomImage.getPatientID());

		//PatientSex
		pat.setSex(dicomImage.getPatientSex());
		return pat;
	}
	private gme.cacore_cacore._3_2.edu_northwestern_radiology.User getUser(){
		//Create user
		gme.cacore_cacore._3_2.edu_northwestern_radiology.User user = factory.createUser();
		
		user.setId(BigInteger.ZERO);
		
		// user name
		user.setName(UserName);
		
		// loginName
		user.setLoginName("NWU");
		
		// numberWithinRoleOfClinicalTrial
		user.setNumberWithinRoleOfClinicalTrial(BigInteger.valueOf(3));
		
		// roleInTrial
		user.setRoleInTrial("Referring");
		
		return user;
	}
	
	private CalculationResult getRulerCalResult(){
		CalculationResult calResult = factory.createCalculationResult();
		calResult.setNumberOfDimensions(BigInteger.valueOf(1));
		calResult.setId(BigInteger.ZERO);
		calResult.setUnitOfMeasure("mm");
		calResult.setType(CalculationResultIdentifier.SCALAR);
		
		// set dimensionCollection node
		Dimension dimension = factory.createDimension();
		dimension.setId(BigInteger.ZERO);
		dimension.setIndex(BigInteger.ZERO);
		dimension.setLabel("Length");
		dimension.setSize(BigInteger.ONE);
		
		CalculationResult.DimensionCollection dimCollection = factory.createCalculationResultDimensionCollection();
		List<Dimension> dimList = dimCollection.getDimension();
		dimList.add(dimension);
		
		calResult.setDimensionCollection(dimCollection);
		
		// set dataCollection node
		Coordinate coordinate = factory.createCoordinate();
		coordinate.setDimensionIndex(BigInteger.ZERO);
		coordinate.setId(BigInteger.ZERO);
		coordinate.setPosition(BigInteger.ZERO);
		
		Data.CoordinateCollection coordinateCollection = factory.createDataCoordinateCollection();
		List<Coordinate> cooList = coordinateCollection.getCoordinate();
		cooList.add(coordinate);
		
		// there is one slice.
		DicomSlice slice = dicomImage.getDicomSlices().get(0);
		ArrayList<DicomPoint> pointList = slice.getSlicePoints();
		
		// calculate the distance between two points.
		DicomPoint point1 = pointList.get(0);
		DicomPoint point2 = pointList.get(1);
		float xdistance = point1.getX() - point2.getX();
		float ydistance = point1.getY() - point2.getY();
		double distance = Math.sqrt(xdistance * xdistance + ydistance * ydistance);
		// Take the whole
		int distanceint = (int)(distance + 0.5);
		
		Data data = factory.createData();
		data.setCoordinateCollection(coordinateCollection);
		data.setId(BigInteger.ZERO);
		data.setValue(distanceint);
		
		CalculationResult.DataCollection dataCollection = factory.createCalculationResultDataCollection();
		List<Data> dataList = dataCollection.getData();
		dataList.add(data);
		
		calResult.setDataCollection(dataCollection);
		
		return calResult;
	}
	private CalculationResult getCrossProductCalResult(){
		CalculationResult calResult = factory.createCalculationResult();
		calResult.setNumberOfDimensions(BigInteger.valueOf(2));
		calResult.setId(BigInteger.ZERO);
		calResult.setUnitOfMeasure("mm");
		calResult.setType(CalculationResultIdentifier.SCALAR);
		
		// set dimensionCollection node
		// dimension one
		Dimension dimension1 = factory.createDimension();
		dimension1.setId(BigInteger.ZERO);
		dimension1.setIndex(BigInteger.ZERO);
		dimension1.setLabel("Length0");
		dimension1.setSize(BigInteger.ONE);
		
		// dimension two
		Dimension dimension2 = factory.createDimension();
		dimension2.setId(BigInteger.ZERO);
		dimension2.setIndex(BigInteger.ONE);
		dimension2.setLabel("Length1");
		dimension2.setSize(BigInteger.ONE);
		
		CalculationResult.DimensionCollection dimCollection = factory.createCalculationResultDimensionCollection();
		List<Dimension> dimList = dimCollection.getDimension();
		dimList.add(dimension1);
		dimList.add(dimension2);
		
		calResult.setDimensionCollection(dimCollection);
		
		// set dataCollection node
		// data one
		Coordinate coordinate1 = factory.createCoordinate();
		coordinate1.setDimensionIndex(BigInteger.ZERO);
		coordinate1.setId(BigInteger.ZERO);
		coordinate1.setPosition(BigInteger.ZERO);
		
		Data.CoordinateCollection coordinateCollection1 = factory.createDataCoordinateCollection();
		List<Coordinate> cooList1 = coordinateCollection1.getCoordinate();
		cooList1.add(coordinate1);
		
		// there is one slice.
		DicomSlice slice = dicomImage.getDicomSlices().get(0);
		ArrayList<DicomPoint> pointList = slice.getSlicePoints();
		
		// calculate the distance between two points.
		DicomPoint point1 = pointList.get(0);
		DicomPoint point2 = pointList.get(1);
		float xdistance1 = point1.getX() - point2.getX();
		float ydistance1 = point1.getY() - point2.getY();
		double distance1 = Math.sqrt(xdistance1 * xdistance1 + ydistance1 * ydistance1);
		// Take the whole
		int distanceint1 = (int)(distance1 + 0.5);
		
		Data data1 = factory.createData();
		data1.setCoordinateCollection(coordinateCollection1);
		data1.setId(BigInteger.ZERO);
		data1.setValue(distanceint1);
		
		// data two
		Coordinate coordinate2 = factory.createCoordinate();
		coordinate2.setDimensionIndex(BigInteger.ONE);
		coordinate2.setId(BigInteger.ZERO);
		coordinate2.setPosition(BigInteger.ONE);
		
		Data.CoordinateCollection coordinateCollection2 = factory.createDataCoordinateCollection();
		List<Coordinate> cooList2 = coordinateCollection2.getCoordinate();
		cooList2.add(coordinate2);
		
		// calculate the distance between two points.
		DicomPoint point3 = pointList.get(2);
		DicomPoint point4 = pointList.get(3);
		float xdistance2 = point3.getX() - point4.getX();
		float ydistance2 = point3.getY() - point4.getY();
		double distance2 = Math.sqrt(xdistance2 * xdistance2 + ydistance2 * ydistance2);
		// Take the whole
		int distanceint2 = (int)(distance2 + 0.5);
		
		Data data2 = factory.createData();
		data2.setCoordinateCollection(coordinateCollection2);
		data2.setId(BigInteger.ZERO);
		data2.setValue(distanceint2);
		
		CalculationResult.DataCollection dataCollection = factory.createCalculationResultDataCollection();
		List<Data> dataList = dataCollection.getData();
		dataList.add(data1);
		dataList.add(data2);
		
		calResult.setDataCollection(dataCollection);
		
		return calResult;
	}
	private Calculation getRulerCalculation(){
		Calculation cal = factory.createCalculation();
		cal.setCodeMeaning("Long_Axis");
		cal.setCodeValue("AVT2");
		cal.setCodingSchemeDesignator("AVT2");
		cal.setMathML("");
		cal.setId(BigInteger.ZERO);
		cal.setDescription("Linear Measurement");
		cal.setUid("0");
		
		CalculationResultCollection resultColl = factory.createCalculationCalculationResultCollection();
		List<CalculationResult> calculationResult = resultColl.getCalculationResult();
		calculationResult.add(getRulerCalResult());
		cal.setCalculationResultCollection(resultColl);
		return cal;
		
	}
	private Calculation getCrossProductCalculation(){
		Calculation cal = factory.createCalculation();
		cal.setCodeMeaning("CrossProduct");
		cal.setCodeValue("AVT2");
		cal.setCodingSchemeDesignator("AVT2");
		cal.setMathML("");
		cal.setId(BigInteger.ZERO);
		cal.setDescription("Area Measurement");
		cal.setUid("0");
		
		CalculationResultCollection resultColl = factory.createCalculationCalculationResultCollection();
		List<CalculationResult> calculationResult = resultColl.getCalculationResult();
		calculationResult.add(getCrossProductCalResult());
		cal.setCalculationResultCollection(resultColl);
		return cal;
	}
	private gme.cacore_cacore._3_2.edu_northwestern_radiology.MultiPoint getMultiPointGeometricShape(){
		//create MultiPoint
		gme.cacore_cacore._3_2.edu_northwestern_radiology.MultiPoint geoShape = factory.createMultiPoint();
		geoShape.setId(BigInteger.ZERO);
		geoShape.setIncludeFlag(true);
		geoShape.setLineColor("YELLOW");
		geoShape.setLineOpacity("OPACITY");
		geoShape.setLineStyle("SOLID");
		geoShape.setShapeIdentifier(BigInteger.valueOf(1));
		
		SpatialCoordinateCollection  spatialCoordinateColl = factory.createGeometricShapeSpatialCoordinateCollection();
		
		geoShape.setSpatialCoordinateCollection(spatialCoordinateColl);
		
		List<SpatialCoordinate> spatialCoordinateList = spatialCoordinateColl.getSpatialCoordinate();
		ArrayList<DicomSlice> dicomSliceList = dicomImage.getDicomSlices();
		int dicomPointNum = 0;
		int sliceNum = dicomSliceList.size();
		for(int i = 0; i < sliceNum; ++i){
			DicomSlice slice = dicomSliceList.get(i);
			String sopInstanceUID = slice.getSopInstanceUID();
			float originPointX = slice.getOriginPoint().getX();
			float originPointY = slice.getOriginPoint().getY();
			float pixelSpacingX = slice.getPixelSpacingX();
			float pixelSpacingY = slice.getPixelSpacingY();
			ArrayList<DicomPoint> dicomPointList = slice.getSlicePoints();
			int slicePointNum = dicomPointList.size();
			for(int j = 0; j < slicePointNum; ++j){
				DicomPoint point = dicomPointList.get(j);
				int x = (int)((point.getX() - originPointX) / pixelSpacingX + 0.5);
				int y = (int)((point.getY() - originPointY) / pixelSpacingY + 0.5);
				TwoDimensionSpatialCoordinate twoDimSpaCoordinate = factory.createTwoDimensionSpatialCoordinate();
				twoDimSpaCoordinate.setId(BigInteger.valueOf(dicomPointNum));
				twoDimSpaCoordinate.setImageReferenceUID(sopInstanceUID);
				twoDimSpaCoordinate.setX(x);
				twoDimSpaCoordinate.setY(y);
				twoDimSpaCoordinate.setCoordinateIndex(BigInteger.valueOf(j));
				twoDimSpaCoordinate.setReferencedFrameNumber(BigInteger.valueOf(slice.getReferencedFrameNumber()));
				spatialCoordinateList.add(twoDimSpaCoordinate);
				dicomPointNum++;
			}
		}
		return geoShape;
	}
	private GeometricShapeCollection getPolylineShapeCollection(){
		
		GeometricShapeCollection geoShapeColl = factory.createImageAnnotationGeometricShapeCollection();
		List<GeometricShape> geoShapeList = geoShapeColl.getGeometricShape();
		
		// There is only one slice.
		DicomSlice slice = dicomImage.getDicomSlices().get(0);
		String sopInstanceUID = slice.getSopInstanceUID();
		float originPointX = slice.getOriginPoint().getX();
		float originPointY = slice.getOriginPoint().getY();
		float pixelSpacingX = slice.getPixelSpacingX();
		float pixelSpacingY = slice.getPixelSpacingY();
		
		ArrayList<DicomPoint> pointList = slice.getSlicePoints();
		int pointnum = pointList.size();
		// the number of lines
		int lineNum = pointnum / 2;
		for(int i = 0; i < lineNum; ++i){
			gme.cacore_cacore._3_2.edu_northwestern_radiology.Polyline polyline = factory.createPolyline();
			polyline.setId(BigInteger.ZERO);
			polyline.setIncludeFlag(false);
			polyline.setLineColor("");
			polyline.setLineOpacity("");
			polyline.setLineStyle("");
			polyline.setLineThickness("");
			polyline.setShapeIdentifier(BigInteger.valueOf(i));
			
			SpatialCoordinateCollection  spatialCoordinateColl = factory.createGeometricShapeSpatialCoordinateCollection();
			List<SpatialCoordinate> spatialCoordinateList = spatialCoordinateColl.getSpatialCoordinate();
			
			// one point
			TwoDimensionSpatialCoordinate twoDimSpaCoordinate1 = factory.createTwoDimensionSpatialCoordinate();
			DicomPoint point1 = pointList.get(i * 2);
			int x1 = (int)((point1.getX() - originPointX) / pixelSpacingX + 0.5);
			int y1 = (int)((point1.getY() - originPointY) / pixelSpacingY + 0.5);
			twoDimSpaCoordinate1.setId(BigInteger.valueOf(0));
			twoDimSpaCoordinate1.setImageReferenceUID(sopInstanceUID);
			twoDimSpaCoordinate1.setX(x1);
			twoDimSpaCoordinate1.setY(y1);
			twoDimSpaCoordinate1.setCoordinateIndex(BigInteger.valueOf(0));
			twoDimSpaCoordinate1.setReferencedFrameNumber(BigInteger.valueOf(slice.getReferencedFrameNumber()));
			
			// two point
			TwoDimensionSpatialCoordinate twoDimSpaCoordinate2 = factory.createTwoDimensionSpatialCoordinate();
			DicomPoint point2 = pointList.get(i * 2 + 1);
			int x2 = (int)((point2.getX() - originPointX) / pixelSpacingX + 0.5);
			int y2 = (int)((point2.getY() - originPointY) / pixelSpacingY + 0.5);
			twoDimSpaCoordinate2.setId(BigInteger.valueOf(1));
			twoDimSpaCoordinate2.setImageReferenceUID(sopInstanceUID);
			twoDimSpaCoordinate2.setX(x2);
			twoDimSpaCoordinate2.setY(y2);
			twoDimSpaCoordinate2.setCoordinateIndex(BigInteger.valueOf(1));
			twoDimSpaCoordinate2.setReferencedFrameNumber(BigInteger.valueOf(slice.getReferencedFrameNumber()));
			
			spatialCoordinateList.add(twoDimSpaCoordinate1);
			spatialCoordinateList.add(twoDimSpaCoordinate2);
			
			polyline.setSpatialCoordinateCollection(spatialCoordinateColl);
			
			geoShapeList.add(polyline);
		}
		
		return geoShapeColl;
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
	public void outputDicomImagetoXml(File filename){
		//create ImageAnnotation
		// Do not know the value of some properties
		ImageAnnotation annot = factory.createImageAnnotation();
		annot.setId(BigInteger.ZERO);
		annot.setAimVersion("TCGA");
		annot.setDateTime(getCurrentTime());
		annot.setName("SIEMENS");
		annot.setCodingSchemeDesignator("AVT2");
		annot.setCodingSchemeVersion("v0_rv1");
		annot.setCodeValue("AVT2");
		annot.setCodeMeaning("CDRH baseline target lesion");
		
		UIDGenerator uid = new UIDGenerator();
		try{
			annot.setUniqueIdentifier(uid.getNewUID());
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ImageReferenceCollection imagingReferenceColl = factory.createImageAnnotationImageReferenceCollection();
		List<ImageReference> imageReferenceList = imagingReferenceColl.getImageReference();
		imageReferenceList.add(getDicomImageRef());
		annot.setImageReferenceCollection(imagingReferenceColl);
		
		ImageAnnotation.Patient _pat = factory.createImageAnnotationPatient();
		_pat.setPatient(getPatient());
		annot.setPatient(_pat);
		
		// set user node
		Annotation.User _user = factory.createAnnotationUser();
		_user.setUser(getUser());
		annot.setUser(_user);
		
		if("Ruler".equals(OutPutXMLTpye) || "CrossProduct".equals(OutPutXMLTpye)){
			
			// set Calculation Collection
			CalculationCollection calculationColl = factory.createAnnotationCalculationCollection();
			List<Calculation> calculationList = calculationColl.getCalculation();
			
			if("Ruler".equals(OutPutXMLTpye)){
				calculationList.add(getRulerCalculation());
			}
			else{
				calculationList.add(getCrossProductCalculation());
			}
			
			annot.setCalculationCollection(calculationColl);
			
			annot.setGeometricShapeCollection(getPolylineShapeCollection());
			
		}
		else{
			// set GeometricShapeCollection node
			GeometricShapeCollection geoShapeColl = factory.createImageAnnotationGeometricShapeCollection();
			List<GeometricShape> geoShapeList = geoShapeColl.getGeometricShape();
			geoShapeList.add(getMultiPointGeometricShape());
			
			annot.setGeometricShapeCollection(geoShapeColl);
		}
		try{
	    	JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
	        Marshaller marshaller = jaxbContext.createMarshaller();
	        marshaller.setProperty("jaxb.schemaLocation", "gme://caCORE.caCORE/3.2/edu.northwestern.radiology.AIM AIM_TCGA09302009_XML.xsd");
	        JAXBElement<ImageAnnotation> catalogElement = factory.createImageAnnotation(annot);
	        marshaller.marshal(catalogElement,new FileOutputStream(filename));
	     } catch (FileNotFoundException e){
	        e.printStackTrace();
	     } catch (JAXBException e){
	        e.printStackTrace();
		 }
	}
	
	// construct the dicomImage object.
	public boolean ConstructDicomImage(File dicomFolder, File dicomPointsFile, String zvalueString) {
		if (false == dicomFolder.exists() || false == dicomPointsFile.exists()) {
			return false;
		}
		if (false == dicomFolder.isDirectory()
				|| false == dicomPointsFile.isFile()) {
			return false;
		}
		File[] dicomFileList = dicomFolder.listFiles();

		// set Image Attributes to dicomImage object.
		setImageAttributes(dicomFileList[0]);

		// construct dicom slice object and put the object into dicom image object 
		return ConstructSlice(dicomPointsFile, dicomFileList, zvalueString);
	}
	private void getPointfrom39(NodeList nodeList, ArrayList<DicomPoint> pointList){
		int num = nodeList.getLength();
		for(int i = 0; i < num; ++i){
			Element element = (Element)nodeList.item(i);
			String xStr = element.getAttribute("x");
			String yStr = element.getAttribute("y");
			DicomPoint point = new DicomPoint();
			point.setX(Float.valueOf(xStr));
			point.setY(Float.valueOf(yStr));
			pointList.add(point);
		}
	}
	private void getPointfrom1114(NodeList nodeList, ArrayList<DicomPoint> pointList){
		int num = nodeList.getLength();
		try{
			for(int i = 0; i < num; ++i){
				Element element = (Element)nodeList.item(i);
				String type = element.getAttribute("Type");
				if("ANNOBJECT_RULER".equals(type)){
					OutPutXMLTpye = "Ruler";
					Element pointnode = (Element)path.evaluate("Rect", element, XPathConstants.NODE);
					String xrightString = pointnode.getAttribute("Right");
					String ybottomString = pointnode.getAttribute("Bottom");
					String xleftString = pointnode.getAttribute("Left");
					String ytopString = pointnode.getAttribute("Top");
					
					DicomPoint point1 = new DicomPoint();
					point1.setX(Float.valueOf(xrightString));
					point1.setY(Float.valueOf(ybottomString));
					pointList.add(point1);
					
					DicomPoint point2 = new DicomPoint();
					point2.setX(Float.valueOf(xleftString));
					point2.setY(Float.valueOf(ytopString));
					pointList.add(point2);
				}else if("ANNOBJECT_CROSSPRODUCT".equals(type)){
					OutPutXMLTpye = "CrossProduct";
					Element pointnode = (Element)path.evaluate("Points", element, XPathConstants.NODE);
					NodeList childrenlist = pointnode.getChildNodes();
					int length = childrenlist.getLength();
					for(int j = 0; j < length; ++j){
						Node child = childrenlist.item(j);
						if(child instanceof Element){
							Element childElement = (Element) child;
							Text text = (Text)childElement.getFirstChild();
							String valueStr = text.getData().trim();
							
							int index = valueStr.indexOf(",");
							String xStr = valueStr.substring(0, index).trim();
							String yStr = valueStr.substring(index + 1).trim();
							
							DicomPoint point = new DicomPoint();
							point.setX(Float.valueOf(xStr));
							point.setY(Float.valueOf(yStr));
							
							pointList.add(point);
							
						}
					}
				}
			}
		}catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	// get dicom points value
	private void getDicomPointList(File dicomPointsFile, ArrayList<DicomPoint> pointList){
		try {
			Document doc = builder.parse(dicomPointsFile);
			NodeList Node1114List = (NodeList)path.evaluate("/Measurements/AnnObject", doc, XPathConstants.NODESET);
			NodeList Ruler39NodeList = (NodeList)path.evaluate("/Container/Ruler/Points/Coord", doc, XPathConstants.NODESET);
			NodeList CrossProduct39NodeList = (NodeList)path.evaluate("/Container/CrossProduct/Points/Coord", doc, XPathConstants.NODESET);
			
			if(Ruler39NodeList.getLength() > 0){
				OutPutXMLTpye = "Ruler";
			}
			if(CrossProduct39NodeList.getLength() > 0){
				OutPutXMLTpye = "CrossProduct";
			}
			getPointfrom1114(Node1114List, pointList);
			getPointfrom39(Ruler39NodeList, pointList);
			getPointfrom39(CrossProduct39NodeList, pointList);
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	// Construct DicomSlice object and add the object to DicomImage.
	private void ConstructDicomSlicetoImage(AttributeList tags, ArrayList<DicomPoint> pointList){
		DicomSlice dicomSlice = new DicomSlice();
		DicomPoint originPoint = new DicomPoint();
		dicomSlice.setSlicePoints(pointList);
		dicomSlice.setOriginPoint(originPoint);
		// SOP Instance UID
		AttributeTag tag = new AttributeTag(0x08, 0x18);
		Attribute attrib = tags.get(tag);
		if (attrib != null){
			dicomSlice.setSopInstanceUID(attrib.getDelimitedStringValuesOrEmptyString());
		}
		// SOP Class UID
		tag = new AttributeTag(0x08, 0x16);
		attrib = tags.get(tag);
		if (attrib != null){
			dicomSlice.setSopClassUID(attrib.getDelimitedStringValuesOrEmptyString());
		}
		// Pixel Spacing
		tag = new AttributeTag(0x28, 0x30);
		attrib = tags.get(tag);
		float[] pixelSpacing = null;
		try{
		    if (null != attrib){
		    	pixelSpacing = attrib.getFloatValues();
		    }
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null != pixelSpacing){
			dicomSlice.setPixelSpacingX(pixelSpacing[0]);
			dicomSlice.setPixelSpacingY(pixelSpacing[1]);
		}
		// Origin point
		tag = new AttributeTag(0x20, 0x32);
		attrib = tags.get(tag);
		float[] positions = null;
		try{
		    if (null != attrib){
		        positions = attrib.getFloatValues();
		    }
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null != positions){
			originPoint.set(positions[0], positions[1], positions[2]);
		}
		// referencedFrameNumber
		tag = new AttributeTag(0x20, 0x13);
		attrib = tags.get(tag);
		int[] referencedFrameNumber = null;
		try{
		    if (null != attrib){
		    	referencedFrameNumber = attrib.getIntegerValues();
		    }
		}catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(null != referencedFrameNumber){
			dicomSlice.setReferencedFrameNumber(referencedFrameNumber[0]);
		}
		// add DicomSlice object to DicomImage object.
		dicomImage.getDicomSlices().add(dicomSlice);
	}
	// construct dicom slice object and put the object into dicom image object
	private boolean ConstructSlice(File dicomPointsFile, File[] dicomFileList,String zvalueString) {
		// get sop instance uid from file name
		float zvalue = Float.valueOf(zvalueString);

		int dicomFileNum = dicomFileList.length;
		DicomInputStream segInput = null;
		AttributeList tags = new AttributeList();

		// Origin point
		AttributeTag tag = new AttributeTag(0x20, 0x32);
		Attribute attrib = null;
		float[] positions = null;
		try {
			for (int i = 0; i < dicomFileNum; ++i) {
				File dicomFile = dicomFileList[i];
				segInput = new DicomInputStream(dicomFile);
				tags.read(segInput);
				attrib = tags.get(tag);
				if(null != attrib){
					positions = attrib.getFloatValues();
				}
				if(null != positions && positions[2] == zvalue){
					// get dicom points value 
					ArrayList<DicomPoint> pointList = new ArrayList<DicomPoint>();
					getDicomPointList(dicomPointsFile, pointList);
					// Construct DicomSlice object and add the object to DicomImage.
					ConstructDicomSlicetoImage(tags,pointList);
					break;
				}
				// no slice
				if(i == dicomFileNum){
					return false;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DicomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	// get points from xml file and output points to list.
	@SuppressWarnings("unchecked")
	public void getVdicomPointFromXml(File filename,
			ArrayList<Point2D> pointList, ArrayList<Integer> pointIndexList) {
		if ((filename == null) || (!filename.exists()) || (pointList == null)
				|| (pointIndexList == null)) {
			return;
		}
		
		try {
			   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
			   Unmarshaller u = jaxbContext.createUnmarshaller();
			   JAXBElement obj = (JAXBElement)u.unmarshal(filename);
			   
			   //get annotation UID
			   ImageAnnotation imageAnnotation = ((ImageAnnotation)obj.getValue());			   
			   List<GeometricShape> geoShapes = imageAnnotation.getGeometricShapeCollection().getGeometricShape();
			   if (geoShapes.size() > 0){
				   List<SpatialCoordinate> coord_results = geoShapes.get(0).getSpatialCoordinateCollection().getSpatialCoordinate();
				   int pointnum = coord_results.size();
				   String pointsopUID = "";
				   int pointIndex = 0;
				   for (int i = 0; i < pointnum; ++i) {
					   TwoDimensionSpatialCoordinate coord = (TwoDimensionSpatialCoordinate) coord_results.get(i);
					   String sopUID = coord.getImageReferenceUID();
					   float x = (float) coord.getX();
					   float y = (float) coord.getY();
					   Point2D point2d = new Point2D(x, y);
					   // first point
					   if (i == 0) {
						   pointsopUID = sopUID;
						   pointIndex = 0;
						}
						// other points
						else {
							if (!pointsopUID.equals(sopUID)) {
								pointsopUID = sopUID;
								pointIndex++;
							}
						}
						pointList.add(point2d);
						pointIndexList.add(Integer.valueOf(pointIndex));
					}
				   
			   }
			   
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// get RECIST from xml file
	@SuppressWarnings("unchecked")
	public float getRECISTFromXml(File filename) {
		if ((filename == null) || (!filename.exists())) {
			return 0;
		}
		try {
		   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
		   Unmarshaller u = jaxbContext.createUnmarshaller();
		   JAXBElement obj = (JAXBElement)u.unmarshal(filename);
		   
		   //get annotation UID
		   ImageAnnotation imageAnnotation = ((ImageAnnotation)obj.getValue());
		   List<Calculation> calculations = imageAnnotation.getCalculationCollection().getCalculation();
		   if (calculations.size() > 0){
			   List<CalculationResult> cal_results = calculations.get(0).getCalculationResultCollection().getCalculationResult();
			   if (cal_results.size() > 0){
				   List<Data> data = cal_results.get(0).getDataCollection().getData();
				   if (data.size() > 0){
					  return (float) data.get(0).getValue();
				   }
			   }
			   
		   }			   
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return 0;
	}

	// get WHO from xml file
	@SuppressWarnings("unchecked")
	public float getWHOFromXml(File filename) {
		if ((filename == null) || (!filename.exists())) {
			return 0;
		}
		try {
		   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
		   Unmarshaller u = jaxbContext.createUnmarshaller();
		   JAXBElement obj = (JAXBElement)u.unmarshal(filename);
		   
		   //get annotation UID
		   ImageAnnotation imageAnnotation = ((ImageAnnotation)obj.getValue());
		   List<Calculation> calculations = imageAnnotation.getCalculationCollection().getCalculation();
		   if (calculations.size() > 0){
			   List<CalculationResult> cal_results = calculations.get(0).getCalculationResultCollection().getCalculationResult();
			   if (cal_results.size() > 0){
				   List<Data> data = cal_results.get(0).getDataCollection().getData();
				   if (data.size() > 1){
					   float x = (float) data.get(0).getValue();
					   float y = (float) data.get(1).getValue();
					   return x*y;
				   }
			   }
			   
		   }
			   
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return 0;
	}
	
	// get Annotation UID from xml file
	@SuppressWarnings("unchecked")
	public String getAnnotationUIDFromXml(File filename) {
		if ((filename == null) || (!filename.exists())) {
			return null;
		}
		
		try {
		   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
		   Unmarshaller u = jaxbContext.createUnmarshaller();
		   JAXBElement obj = (JAXBElement)u.unmarshal(filename);
		   
		   //get annotation UID
		   ImageAnnotation imageAnnotation = ((ImageAnnotation)obj.getValue());
		   return imageAnnotation.getUniqueIdentifier();
		   
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	// get Series UID from xml file
	@SuppressWarnings("unchecked")
	public String getReferredSeriesUIDFromXml(File filename) {
		if ((filename == null) || (!filename.exists())) {
			return null;
		}
		
		try {
		   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
		   Unmarshaller u = jaxbContext.createUnmarshaller();
		   JAXBElement obj = (JAXBElement)u.unmarshal(filename);
		   
		   //get annotation UID
		   ImageAnnotation imageAnnotation = ((ImageAnnotation)obj.getValue());
		   
		   //get series instance UID
		   ImageReference imageReference = imageAnnotation.getImageReferenceCollection().getImageReference().get(0);
		   DICOMImageReference ref = (DICOMImageReference) imageReference;
		   Study study = ref.getStudy().getStudy();	 
		   Series series = study.getSeries().getSeries();

		   return series.getInstanceUID();
		   
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	// get Series UID from xml file
	@SuppressWarnings("unchecked")
	public boolean getReferredImagesUIDFromXml(File filename, List<String> imageUIDs) {
		if ((filename == null) || (!filename.exists())) {
			return false;
		}		
		
		try {
			   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
			   Unmarshaller u = jaxbContext.createUnmarshaller();
			   JAXBElement obj = (JAXBElement)u.unmarshal(filename);
			   
			   //get annotation UID
			   ImageAnnotation imageAnnotation = ((ImageAnnotation)obj.getValue());
			   
			   //get series instance UID
			   ImageReference imageReference = imageAnnotation.getImageReferenceCollection().getImageReference().get(0);
			   DICOMImageReference ref = (DICOMImageReference) imageReference;
			   Study study = ref.getStudy().getStudy();	 
			   List<Image> images = study.getSeries().getSeries().getImageCollection().getImage();
				int num = images.size();
				for (int i = 0; i < num; i++){
					imageUIDs.add("");
				}
				for (int i = 0; i < num; i++){
					Image element = images.get(i);
					String id = element.getId().toString();
					if (!id.isEmpty()){
						String str = element.getSopInstanceUID();
						int index =Integer.parseInt(id);
						imageUIDs.set(index, str);
					}
				}
					
				return true;
			   
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		return false;
	}

	// get points from list and output points to file
    public void OutPutPointsToTXT(File filename, ArrayList<Point2D> pointList, ArrayList<Integer> pointIndexList){
		if ((filename == null) || (pointList == null) || (pointIndexList == null)) {
			return;
		}
		StringBuffer strbuf = new StringBuffer();
		strbuf.append("[");
		int pointnum = pointList.size();
		for(int i = 0; i < pointnum; ++i){
			Point2D point2d = pointList.get(i);
			strbuf.append(String.valueOf(point2d.getX()));
			strbuf.append(" ");
			strbuf.append(String.valueOf(point2d.getY()));
			// not the last one.
			if(i != pointnum - 1){
				strbuf.append(", ");
			}
		}
		strbuf.append("]\n[");
        int pointIndexnum = pointIndexList.size();
        for(int i = 0; i < pointIndexnum; ++i){
			Integer pointIndex = pointIndexList.get(i);
			strbuf.append(String.valueOf(pointIndex.toString()));
			// not the last one.
			if(i != pointIndexnum - 1){
				strbuf.append(", ");
			}
		}
		strbuf.append("]");
		try {
			FileOutputStream out = new FileOutputStream(filename); 
			out.write(strbuf.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
	// get points from list and output points to file
    public boolean OutPutPointsToStrings(File filename, StringBuffer pointBuf, StringBuffer pointIndexBuf){
		if ((filename == null) || (pointBuf == null) || (pointIndexBuf == null)) {
			return false;
		}
		
		ArrayList<Point2D> pointList = new ArrayList<Point2D>();
		ArrayList<Integer> pointIndexList = new ArrayList<Integer>();
		
		getVdicomPointFromXml(filename, pointList, pointIndexList);
		
		pointBuf.append("[");
		int pointnum = pointList.size();
		for(int i = 0; i < pointnum; ++i){
			Point2D point2d = pointList.get(i);
			pointBuf.append(String.valueOf(point2d.getX()));
			pointBuf.append(" ");
			pointBuf.append(String.valueOf(point2d.getY()));
			// not the last one.
			if(i != pointnum - 1){
				pointBuf.append(", ");
			}
		}
		pointBuf.append("]");
		
		pointIndexBuf.append("[");
        int pointIndexnum = pointIndexList.size();
        for(int i = 0; i < pointIndexnum; ++i){
			Integer pointIndex = pointIndexList.get(i);
			pointIndexBuf.append(String.valueOf(pointIndex.toString()));
			// not the last one.
			if(i != pointIndexnum - 1){
				pointIndexBuf.append(", ");
			}
		}
        pointIndexBuf.append("]");
        
        return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        QIBAImageAnnotation aCollection = new QIBAImageAnnotation();
        if(args.length == 0){
        	return;
        }
        try {
			Document doc = aCollection.getBuilder().parse(new File(args[0]));
			NodeList Node1114List = (NodeList)aCollection.getPath().evaluate("/Root/Parameters", doc, XPathConstants.NODESET);
			int num = Node1114List.getLength();
			for(int i = 0; i < num; ++i){
				boolean issuccess = false;
				Element element = (Element)Node1114List.item(i);
				String xmlfileString = aCollection.getPath().evaluate("XmlFile", element).trim();
				String DicomFolder = aCollection.getPath().evaluate("DicomFolder", element).trim();
				String ZValue = aCollection.getPath().evaluate("ZValue", element).trim();
				String UserName = aCollection.getPath().evaluate("UserName", element).trim();
				String OutPutName = aCollection.getPath().evaluate("OutPutName", element).trim();
				aCollection.setUserName(UserName);
				DicomImage image = new DicomImage();
				aCollection.setDicomImage(image);
				if(!"".equals(ZValue)){
					issuccess = aCollection.ConstructDicomImage(new File(DicomFolder), new File(xmlfileString), ZValue);
				}else{
					aCollection.setOutPutXMLTpye("");
					issuccess = aCollection.ConstructDicomImage(new File(DicomFolder), new File(xmlfileString));
				}
				if(true == issuccess){
					aCollection.outputDicomImagetoXml(new File(OutPutName));
				}
			}
			NodeList Vnodelist = (NodeList)aCollection.getPath().evaluate("/Root/VPoint", doc, XPathConstants.NODESET);
			int Vpointnum = Vnodelist.getLength();
			for(int i = 0; i < Vpointnum; ++i){
				Element element = (Element)Vnodelist.item(i);
				String vfileString = aCollection.getPath().evaluate("InputXml", element).trim();
				String outputfiletxt = aCollection.getPath().evaluate("OutputTXT", element).trim();
				ArrayList<Point2D> point2ds = new ArrayList<Point2D>();
				ArrayList<Integer> pointIndexs = new ArrayList<Integer>();
				aCollection.getVdicomPointFromXml(new File(vfileString), point2ds, pointIndexs);
				aCollection.OutPutPointsToTXT(new File(outputfiletxt), point2ds, pointIndexs);
			}
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
// use in saving the z value and point value list of a slice
class SliceValue{
	private float zValueKey;
	private ArrayList<DicomPoint> pointList;
	public SliceValue(){
		zValueKey = 0;
		pointList = null;
	}
	public SliceValue(float zValueKey, ArrayList<DicomPoint> pointList){
		this.zValueKey = zValueKey;
		this.pointList = pointList;
	}
	public float getzValueKey() {
		return zValueKey;
	}
	public void setzValueKey(float zValueKey) {
		this.zValueKey = zValueKey;
	}
	public ArrayList<DicomPoint> getPointList() {
		return pointList;
	}
	public void setPointList(ArrayList<DicomPoint> pointList) {
		this.pointList = pointList;
	}
}

class Point2D{
	private float x;
	private float y;
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
	public Point2D(float x, float y){
		this.x = x;
		this.y = y;
	}
}
