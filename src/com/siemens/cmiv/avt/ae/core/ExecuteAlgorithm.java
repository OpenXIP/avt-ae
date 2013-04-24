package com.siemens.cmiv.avt.ae.core;

import gme.cacore_cacore._3_2.edu_northwestern_radiology.DICOMImageReference;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.GeometricShape;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageReference;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Patient;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Series;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.SpatialCoordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Study;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.TwoDimensionSpatialCoordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.GeometricShape.SpatialCoordinateCollection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomOutputStream;

import com.siemens.cmiv.avt.mvt.datatype.AnnotationEntry;
import com.siemens.scr.avt.ad.api.ADFacade;
import com.siemens.scr.avt.ad.dicom.GeneralImage;

/**
 * @author Jie Zheng
 *
 */

public class ExecuteAlgorithm implements Runnable {
	List<String> aims;
	boolean runFlag = true;
	String tempFolder = "c:\\temp";
	
	String aimFolder = "C:/temp/AE";
	String dataFolder = "data";
	String resultFolder = "result";
	
	public ExecuteAlgorithm(String temp, List<String> aimFiles){
		this.tempFolder = temp;
		this.aims = aimFiles;
		
		File outputFolder = new File(tempFolder);
		outputFolder.mkdir();
	}

	@Override
	public void run() {
		for (int i = 0; i < aims.size(); i++){		
			if (!runFlag)
				break;
			
			AnnotationEntry item = new AnnotationEntry();
			
			String aim = String.format("%s/%s", aimFolder, aims.get(i));
			
			Map<String, String> aimResult = getSeedInformation(aim);
			if (aimResult.size() <= 0){
				item.setComment(String.format("%s - Fail to load aim", aim));
			}else{
				String annotaitonUID = aimResult.get("AnnotationUID");
				String seriesUID = aimResult.get("SeriesInstanceUID");
				
				String imageUID = aimResult.get("ImageReferenceUID");
				String points = aimResult.get("SeedPoints");
				if(points.isEmpty() || imageUID.isEmpty()){
					item.setComment(String.format("%s - Fail to load aim", aim));
				}else{
					String dataPath =  getDataset(seriesUID, tempFolder);
//					String dataPath =  String.format("%s\\%s", dataFolder, seriesUID);
					File data = new File(dataPath);
					if (!data.exists()){
						item.setComment(String.format("%s - Fail to load dicom", aim));
					}
					else{
						String outputImage = String.format("%s\\image_%s.png", resultFolder, annotaitonUID);
						String configFile = generateConfiguration(resultFolder, dataPath, points, imageUID, annotaitonUID);
						if (configFile.isEmpty())
							continue;
						
						String exeCommand = String.format("xipbuilder.exe AlgorithmExecution.xip /cmd=%s", configFile);
						try {
							Process p = Runtime.getRuntime().exec(exeCommand); //("\"c:/program files/windows/notepad.exe\"");
							p.waitFor();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						item.setAnnotationReader("AVT2");
						item.setAnnotationSeed(points);
						item.setAnnotationUID(annotaitonUID);
						item.setAnnotationScreenshot(outputImage);
						
						item.setPatientName(aimResult.get("PatientName"));
						item.setPatientID(aimResult.get("PatientID"));
						item.setPatientGender(aimResult.get("PatientGender"));
						
						item.setComment("SUCCESS");
						
						deleteDir(data);
					}
				}
			}
			
			fireResultsAvailable(item);
		}	
		
		AnnotationEntry item = new AnnotationEntry();
		item.setNoduleType("LAST");
		
		fireResultsAvailable(item);
	}
	
	AEListener listener;
    public void addAEListener(AEListener l) {        
        listener = l;          
    }
    
	void fireResultsAvailable(AnnotationEntry result){
		AlgorithmExecuteEvent event = new AlgorithmExecuteEvent(result);         		
        listener.executionResultsAvailable(event);
	}	
    
	@SuppressWarnings("unchecked")
	public Map<String, String> getSeedInformation(String aim){
		
		Map<String, String> aimResult = new HashMap<String, String>();

		File aimFile = new File(aim);
    	if (!aimFile.exists())
    		return aimResult;
    	
        try{
		   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
		   Unmarshaller u = jaxbContext.createUnmarshaller();
		   JAXBElement obj = (JAXBElement)u.unmarshal(aimFile);			
		   ImageAnnotation imageAnnotation = ((ImageAnnotation)obj.getValue());
		   
		   //get annotation UID
		   aimResult.put("AnnotationUID", imageAnnotation.getUniqueIdentifier());
		   
		   //get series instance UID
		   ImageReference imageReference = imageAnnotation.getImageReferenceCollection().getImageReference().get(0);
		   DICOMImageReference ref = (DICOMImageReference) imageReference;
		   Study study = ref.getStudy().getStudy();	 
		   Series series = study.getSeries().getSeries();
		   aimResult.put("SeriesInstanceUID", series.getInstanceUID());
		   
		   //get the seed point
		   List<GeometricShape> geometricShapes = imageAnnotation.getGeometricShapeCollection().getGeometricShape();
		   SpatialCoordinateCollection pointCollection = geometricShapes.get(0).getSpatialCoordinateCollection();
		   List<SpatialCoordinate> pointCoords = pointCollection.getSpatialCoordinate();
		   
		   if (pointCoords.size() < 2)
			   return aimResult;
		   
		   //start point
		   int []point0 = new int[2];
		   TwoDimensionSpatialCoordinate point = (TwoDimensionSpatialCoordinate) pointCoords.get(0);
		   point0[0] = (int)point.getX();
		   point0[1] = (int)point.getY();
		   aimResult.put("ImageReferenceUID", point.getImageReferenceUID());
		   
		   int []point1 = new int[2];
		   point = (TwoDimensionSpatialCoordinate) pointCoords.get(1);
		   point1[0] = (int)point.getX();
		   point1[1] = (int)point.getY();
		   
		   aimResult.put("SeedPoints", String.format("[%d, %d, %d, %d]", point0[0], point0[1], point1[0], point1[1]));
		   
		   //get patient information
		   gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation.Patient pat = imageAnnotation.getPatient();
		   Patient _pat = pat.getPatient();
		   aimResult.put("PatientName", _pat.getName());
		   aimResult.put("PatientID", _pat.getPatientID());
		   aimResult.put("PatientGender", _pat.getSex());
		   
        } catch (JAXBException e){
            e.printStackTrace();
 	    }    	
    		
		return aimResult;
	}
	
	@SuppressWarnings("unchecked")
	public String getDataset(String seriesInstanceUID, String strTempFolder){
		ADFacade facade = ADFactory.getADServiceInstance();
		if (facade == null){
			System.out.println("fail to create ADFacade");
			return "";
		}
		HashMap<Integer, Object> dicomCriteria = new HashMap<Integer, Object>();
		dicomCriteria.put(Tag.SeriesInstanceUID, seriesInstanceUID);
		List<GeneralImage> images = facade.findImagesByCriteria(dicomCriteria, null);
		if (images.size() <=0 )
			return "";
		
		String dumpFolder = String.format("%s\\%s", strTempFolder, seriesInstanceUID);
		File dump = new File(dumpFolder);
		dump.mkdir();
		
		for (GeneralImage image : images){
			String sopInstanceUID = image.getSOPInstanceUID();
			DicomObject segObject = facade.getDicomObject(sopInstanceUID);

	    	File fileName = new File(dumpFolder + "\\" + sopInstanceUID + ".dcm");
	    	
	    	try {
				DicomOutputStream dout = new DicomOutputStream(new FileOutputStream(fileName));
				dout.writeDicomFile(segObject);
				dout.close();
	    	} catch(IOException e){
				System.out.println("fail to store DICOM file:" + fileName.toString());
	    	}
			
		}
		return dumpFolder;
	}
	
	public String generateConfiguration(String outputFolder, String data, String points, String uid, String annotationUID){
		Writer writer = null;
		String configFile = "";

        try
        {
        	configFile = String.format("%s\\configuration.txt", outputFolder);

            File file = new File(configFile);
            writer = new BufferedWriter(new FileWriter(file, false));
            
            String text = "processDelayQueue(1);";
            writer.write(text);
            ((BufferedWriter) writer).newLine();
            
            text = String.format("LoadDicom.name = \"%s\";", data);
            writer.write(text);
            ((BufferedWriter) writer).newLine();
          
            text = String.format("Seg_Seeds.points = \"%s\";", points);
            writer.write(text);
            ((BufferedWriter) writer).newLine();

            text = String.format("Seg_Seeds.sopInstanceUID = \"%s\";", uid);
            writer.write(text);
            ((BufferedWriter) writer).newLine();
           
            text = String.format("Export.file = \"%s\\tumor_%s.dcm\";", tempFolder, annotationUID);
            writer.write(text);
            ((BufferedWriter) writer).newLine();

            text = String.format("render(512, 512, \"%s\\image_%s.png\");", outputFolder, annotationUID);
            writer.write(text);
            ((BufferedWriter) writer).newLine();
           
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (writer != null)
                {
                    writer.close();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
		
		return configFile;
	}

	public void setRunFlag(boolean b) {
		runFlag = b;		
	}
	
	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) { 
				boolean success = deleteDir(new File(dir, children[i])); 
				if (!success) {
					return false; 
					} 
				} 
			} // The directory is now empty so delete it 
		
		return dir.delete(); 
		} 
	}


