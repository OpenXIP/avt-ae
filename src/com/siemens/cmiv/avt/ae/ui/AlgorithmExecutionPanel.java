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
package com.siemens.cmiv.avt.ae.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SpringLayout;
import javax.swing.border.SoftBevelBorder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import com.siemens.cmiv.avt.ae.core.AEListener;
import com.siemens.cmiv.avt.ae.core.AlgorithmExecuteEvent;
import com.siemens.cmiv.avt.ae.core.ExecuteAlgorithm;
import com.siemens.cmiv.avt.aim.CreateAIMObject;
import com.siemens.cmiv.avt.mvt.datatype.AnnotationEntry;
import com.siemens.cmiv.avt.mvt.ui.AIMPanel;
import com.siemens.cmiv.avt.mvt.ui.PlotWindow;

import edu.wustl.xipApplication.wg23.OutputAvailableEvent;
import edu.wustl.xipApplication.wg23.OutputAvailableListener;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Calculation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.DICOMImageReference;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.GeometricShape;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageAnnotation;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.ImageReference;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Patient;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Series;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.SpatialCoordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Study;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.TwoDimensionSpatialCoordinate;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.User;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.Annotation.CalculationCollection;
import gme.cacore_cacore._3_2.edu_northwestern_radiology.GeometricShape.SpatialCoordinateCollection;

@SuppressWarnings("serial")
public class AlgorithmExecutionPanel extends JPanel implements ActionListener, AEListener{

	//get screen resolution
	private int screenHeight = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
	private int screenWidth = (int)java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
	private float heightProportion = 1;
	private float widthProportion = 1;
	private	Color xipColor = new Color(51, 51, 102);
	private Color labelColor = new Color(0, 153, 153);
	private Color bgColor = new Color(156, 162, 189);
	
	private int offset_left = screenWidth/10;
	private int width = screenWidth - 2 * offset_left;
	private int offset_top = 20;
	private int height = (int) ((int)screenHeight * 0.5);
	
	private JList textArea = null;
	private DefaultListModel listModel = new DefaultListModel(); 
	
	private AIMPanel AimPanel = new AIMPanel();
	
	private JTextField jLabelField = null;
	private JTextField jVersionField = null;
	private JTextField jAuthorField = null;
	
	private JButton jStartButton = null;
	private JButton jCancelButton = null;
	
	private JProgressBar jProgressBar = null;
	
	private ExecuteAlgorithm batchProcessor = null;
	
	private String tempFolder = "C:\\temp";
	private List<String> algoResult = new ArrayList<String>();
	
	private boolean bHosted = false;
	
	public AlgorithmExecutionPanel(){
		if (screenWidth < 1680)
			widthProportion = (float)(screenWidth / (float)1680);
		if (screenHeight < 1050)
			heightProportion = (float)(screenHeight / (float)1050);
		
    	setBackground(xipColor);
		setBorder(BorderFactory.createLineBorder(Color.WHITE));
		setLayout(null);
    	
		addTitlePanel();
		addAuditTrailPanel();
		addAimListPanel();
		addResultPanel();
		
		jStartButton.addActionListener(this);
		jCancelButton.addActionListener(this);
		
		updateAimList("C:/temp/AE/");		
	}
	
	private void addAuditTrailPanel() {
		JTabbedPane jAlgoTabbedPane = new JTabbedPane();
		
		JPanel algorithmPanel = new JPanel();
		algorithmPanel.setBackground(xipColor);
		
		SpringLayout layout = new SpringLayout();

		algorithmPanel.setLayout(layout);		
		
		JLabel label0 = new JLabel("Label:");
		label0.setBackground(xipColor);
		label0.setForeground(Color.WHITE);

		jLabelField = new JTextField();
		jLabelField.setText("ITK SegmentAlgo");
		jLabelField.setFont(new Font("",Font.BOLD,getHeightValue(12)));
		jLabelField.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
		
		JLabel label1 = new JLabel("Version:");
		label1.setBackground(xipColor);
		label1.setForeground(Color.WHITE);

		jVersionField = new JTextField();
		jVersionField.setText("v1.1.0");
		jVersionField.setFont(new Font("",Font.BOLD,getHeightValue(12)));
		jVersionField.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

		JLabel label2 = new JLabel("Author:");
		label2.setBackground(xipColor);
		label2.setForeground(Color.WHITE);

		jAuthorField = new JTextField();
		jAuthorField.setText("Siemens");
		jAuthorField.setFont(new Font("",Font.BOLD,getHeightValue(12)));
		jAuthorField.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

		algorithmPanel.add(label0);
		algorithmPanel.add(jLabelField);
		algorithmPanel.add(label1);
		algorithmPanel.add(jVersionField);
		algorithmPanel.add(label2);
		algorithmPanel.add(jAuthorField);

	    SpringUtilities.makeCompactGrid(algorithmPanel, 1, algorithmPanel
				.getComponentCount(), getWidthValue(10), getHeightValue(10), getWidthValue(10), getHeightValue(10));

		jAlgoTabbedPane.addTab("Algorithm", algorithmPanel);
		jAlgoTabbedPane.setBounds(getWidthValue(offset_left), getHeightValue(120), getWidthValue(width), getHeightValue((int) (height*0.15)));
		
		this.add(jAlgoTabbedPane);
	}

	private void addResultPanel() {
		JPanel result = new JPanel();
		result.setBackground(xipColor);
		result.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		result.setBounds(getWidthValue(offset_left), getHeightValue((int) (screenHeight*0.65)), getWidthValue(width), getHeightValue((int) (height*0.5)));
		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

		textArea = new JList(listModel);
	    textArea.setForeground(Color.WHITE);
	    textArea.setBackground(bgColor);
	    textArea.addMouseListener(new ActionJList(this));
	    
	    JScrollPane jScrollPanel = new JScrollPane(textArea);
	    jScrollPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
	    result.add(jScrollPanel);
	    
	    result.add(Box.createRigidArea(new Dimension(0,getHeightValue(offset_top))));
	    
		JPanel tool = new JPanel();
		tool.setBackground(xipColor);
		tool.setLayout(new BoxLayout(tool, BoxLayout.LINE_AXIS));
		
		jStartButton = new JButton();
		jStartButton.setText("               Start               ");
		jStartButton.setFont(new Font("", Font.BOLD, getHeightValue(12)));
		tool.add(Box.createRigidArea(new Dimension(getWidthValue((int) (width*0.2)),0)));
		tool.add(jStartButton);
		
		jCancelButton = new JButton();
		jCancelButton.setText("               Cancel               ");
		jCancelButton.setFont(new Font("", Font.BOLD, getHeightValue(12)));
		jCancelButton.setEnabled(false);

		tool.add(Box.createHorizontalGlue());
		tool.add(jCancelButton);
		tool.add(Box.createRigidArea(new Dimension(getWidthValue((int) (width*0.2)),0)));

		tool.setAlignmentX(Component.CENTER_ALIGNMENT);
		result.add(tool);

		jProgressBar = new JProgressBar();
		jProgressBar.setFont(new Font("", Font.BOLD, getHeightValue(12)));
		jProgressBar.setIndeterminate(false);
		jProgressBar.setStringPainted(true);	    
		jProgressBar.setBackground(new Color(156, 162, 189));
		jProgressBar.setForeground(xipColor);
		
	    result.add(Box.createRigidArea(new Dimension(0,getHeightValue(offset_top))));
	    
	    jProgressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
		result.add(jProgressBar);
		
		this.add(result);
	}

	private void addAimListPanel() {
		AimPanel.setBounds(getWidthValue(offset_left), getHeightValue(210), getWidthValue(width), getHeightValue((int) (height*0.85)));
		this.add(AimPanel);		
	}

	private int getHeightValue(int height){
		return (int)(height * heightProportion);
	}
	private int getWidthValue(int width){
		return (int)(width * widthProportion);
	}

	private void addTitlePanel() {
		JPanel title = new JPanel();
		
		title.setBackground(xipColor);
		title.setBounds(getWidthValue(offset_left), getHeightValue(offset_top), getWidthValue(width), getHeightValue(100));
		title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS));
		
		JLabel jLabel0 = new JLabel();
		jLabel0.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, getHeightValue(50)));
		jLabel0.setText("AVT");
		jLabel0.setBackground(xipColor);
		jLabel0.setForeground(labelColor);
		jLabel0.setAlignmentX(Component.CENTER_ALIGNMENT);
		title.add(jLabel0);
		
		JLabel jLabel01 = new JLabel();
		jLabel01.setFont(new Font("Arial", Font.BOLD, getHeightValue(22)));
		jLabel01.setText("Algorithm Execution Application");
		jLabel01.setBackground(xipColor);
		jLabel01.setForeground(labelColor);
		jLabel01.setAlignmentX(Component.CENTER_ALIGNMENT);
		title.add(jLabel01);
		
		this.add(title);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == jStartButton){	
			algoResult.clear();
			listModel.clear();
			
			List<String> aims = AimPanel.getAimFileList();
			
			if (aims.size() > 0){
				jProgressBar.setString("Processing execution request ...");
				jProgressBar.setIndeterminate(true);			
				jProgressBar.updateUI();
				
				batchProcessor = new ExecuteAlgorithm(tempFolder, aims);
				batchProcessor.addAEListener(this);
				batchProcessor.setRunFlag(true);
				
				Thread t = new Thread(batchProcessor);
				t.start();	
				
				jCancelButton.setEnabled(true);
			}
		}

		if(e.getSource() == jCancelButton){	
			batchProcessor.setRunFlag(false);
			
			jProgressBar.setString("Interrupt execution");
			jProgressBar.setIndeterminate(false);	
		}		
	}

	public String getAlgorithmLable(){
		return jLabelField.getText();
	}
	
	public String getAlgorithmVersion(){
		return jVersionField.getText();
	}

	public String getAlgorithmAuthor(){
		return jAuthorField.getText();
	}

	public JList getUIList(){
		return textArea;
	}
	public List<String> getResultList(){
		return algoResult;
	}

	@Override
	public void executionResultsAvailable(AlgorithmExecuteEvent e) {
		AnnotationEntry result = (AnnotationEntry) e.getSource();
		if (result.getNoduleType().compareTo("LAST") == 0){
			jProgressBar.setString("Finish execution");
			jProgressBar.setIndeterminate(false);	
		}
		else {
			String message = result.getComment();
			if (result.getComment().compareTo("SUCCESS") == 0){
				CreateAIMObject aimCreator = new CreateAIMObject();
				aimCreator.setAnnotationInformation(jLabelField.getText(), 1);
				aimCreator.setPatientInformation(result.getPatientName(), result.getPatientID(), result.getPatientGender());
				aimCreator.setAlgorithmInformatation(jAuthorField.getText(), jLabelField.getText(), jVersionField.getText());
				aimCreator.setAIMInformation("TCGA", "AVT2", "v0_rv1", "AVT005", "Tumor volume segmentation");
		
		        String dcmFile = String.format("%s\\tumor_%s.dcm", tempFolder, result.getAnnotaionUID());
		        String aimFile = String.format("%s\\algo_aim_tumor_%s.xml", tempFolder, result.getAnnotaionUID());
		        
		        File segAim = new File(aimFile);
		        File segDcm = new File(dcmFile);
		        aimCreator.marshallVolume(segAim, result.getAnnotationSeed(), result.getAnnotaionUID(), segDcm);
		        
				List<File> serializedAIMs = new ArrayList<File>();
				serializedAIMs.add(segAim);
				serializedAIMs.add(segDcm);
				
				if (bHosted)
					notifyDataAvailable(serializedAIMs);
				
			    message = String.format("%s is finished, please refer to the result: %s", result.getAnnotaionUID(), result.getAnnotaionScreenshot());
			}
			
			jProgressBar.setString("executing");
			listModel.addElement(message);
			algoResult.add(result.getAnnotaionScreenshot());
			
			System.out.println(message);			
		}		
	}
	
	OutputAvailableListener listener; 
	public void addOutputAvailableListener(OutputAvailableListener l){
		 this.listener = l;
	}
		
	void notifyDataAvailable(List<File> serializedAIMs){
		OutputAvailableEvent event = new OutputAvailableEvent(serializedAIMs);
		listener.outputAvailable(event);
	}
	
	public void updateAimList(String folder){
		AimPanel.removeAimEntries();
		
		File dir = new File(folder);
		String[] aims = dir.list();
		if (aims != null){
			for (int i = 0; i < aims.length; i++){
				String aim = aims[i];
				
				File aimFile = new File(String.format("%s/%s", folder, aim));
		    	if (!aimFile.exists())
		    		continue;
		    	
				AnnotationEntry entry = new AnnotationEntry();
				
		        try{
				   JAXBContext jaxbContext = JAXBContext.newInstance("gme.cacore_cacore._3_2.edu_northwestern_radiology");
				   Unmarshaller u = jaxbContext.createUnmarshaller();
				   JAXBElement obj = (JAXBElement)u.unmarshal(aimFile);			
				   ImageAnnotation imageAnnotation = ((ImageAnnotation)obj.getValue());
				   
				   //get annotation UID
				   entry.setAnnotationUID(imageAnnotation.getUniqueIdentifier());
				   entry.setAnnotationFile(aim);
				   
				   XMLGregorianCalendar timeStamp = imageAnnotation.getDateTime();
				   entry.setAnnotationStamp(timeStamp.toString());
				   
				   String annotationType = imageAnnotation.getCodeValue();
				   
				   if (annotationType.compareToIgnoreCase("AVT001") == 0)
					   entry.setAnnotationType("SEED");
					   
				   CalculationCollection cal = imageAnnotation.getCalculationCollection();
				   if (cal != null){
					   List<Calculation> cals = cal.getCalculation();
					   String str = cals.get(0).getCodeMeaning();
					   if (str.compareToIgnoreCase("Long_Axis") == 0 || str.indexOf("Long Axis") != -1)
						   entry.setAnnotationType("RECIST");
				   }
					
				   //get user information
				   gme.cacore_cacore._3_2.edu_northwestern_radiology.Annotation.User reader = imageAnnotation.getUser();
				   User _reader = reader.getUser();
				   String username = _reader.getName();
				   
					String [] buffer;
					buffer = username.split("_");
					if (buffer.length > 1){
						entry.setAnnotationReader(buffer[0]);
 					    entry.setAnnotationStamp(buffer[buffer.length-1]);
					}
					else
						entry.setAnnotationReader(username);
				   
				   String type = entry.getAnnotationType();
				   if (!type.isEmpty())
					   AimPanel.addAimEntry(entry);
				   
		        } catch (JAXBException e){
		            e.printStackTrace();
		 	    }    	
				
			}
		}
		AimPanel.updateAimEntries();		
	}
	
	class ActionJList extends MouseAdapter{
		  protected JList list;
		  protected List<String> algoResult;
		    
		  public ActionJList(AlgorithmExecutionPanel l){
		   list = l.getUIList();
		   algoResult = l.getResultList();
		   }
		    
		  public void mouseClicked(MouseEvent e){
		   if(e.getClickCount() == 2){
		     int index = list.locationToIndex(e.getPoint());
		     if (index == -1)
		    	 return;
		     
		     String png = algoResult.get(index);
			 try {
					BufferedImage img = ImageIO.read(new File(png));
					PlotWindow imageWin = new PlotWindow();
					imageWin.setWindowTile("Result Screenshot");
					imageWin.setPlotImage(img);
					imageWin.updateWindowSize();
					
					imageWin.setVisible(true);
					
			       } catch (IOException arg) {
			    	   System.out.println("load algorithm failed");
			    	   
			       }

		     ListModel dlm = list.getModel();
		     Object item = dlm.getElementAt(index);;
		     list.ensureIndexIsVisible(index);
		     System.out.println("Double clicked on " + item);
		     }
		   }
	}
	public void setHostFlag(boolean b) {
		bHosted = b;
	}
}
