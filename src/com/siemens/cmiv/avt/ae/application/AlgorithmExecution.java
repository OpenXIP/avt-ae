package com.siemens.cmiv.avt.ae.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.nema.dicom.wg23.ArrayOfObjectLocator;
import org.nema.dicom.wg23.ArrayOfUUID;
import org.nema.dicom.wg23.AvailableData;
import org.nema.dicom.wg23.ObjectDescriptor;
import org.nema.dicom.wg23.ObjectLocator;
import org.nema.dicom.wg23.Rectangle;
import org.nema.dicom.wg23.State;
import org.nema.dicom.wg23.Uuid;

import com.siemens.cmiv.avt.ae.ui.AlgorithmExecutionPanel;

import edu.wustl.xipApplication.aim.AimParser;
import edu.wustl.xipApplication.application.ApplicationDataManager;
import edu.wustl.xipApplication.application.ApplicationDataManagerFactory;
import edu.wustl.xipApplication.application.ApplicationTerminator;
import edu.wustl.xipApplication.application.WG23Application;
import edu.wustl.xipApplication.applicationGUI.ExceptionDialog;
import edu.wustl.xipApplication.samples.ApplicationFrameTempl;
import edu.wustl.xipApplication.wg23.ApplicationImpl;
import edu.wustl.xipApplication.wg23.OutputAvailableEvent;
import edu.wustl.xipApplication.wg23.OutputAvailableListener;
import edu.wustl.xipApplication.wg23.WG23DataModel;
import edu.wustl.xipApplication.wg23.WG23DataModelImpl;
import edu.wustl.xipApplication.wg23.WG23Listener;

/**
 * @author Jie Zheng
 *
 */
public class AlgorithmExecution extends WG23Application implements WG23Listener, OutputAvailableListener{
	ApplicationFrameTempl frame = new ApplicationFrameTempl();		
	AlgorithmExecutionPanel appPanel = new AlgorithmExecutionPanel();

	private String aimFolder = "C:/temp/AE";
	
	State appCurrentState;
	ApplicationDataManager dataMgr;

	public AlgorithmExecution(URL hostURL, URL appURL) {
		super(hostURL, appURL);
		
		appPanel.setHostFlag(true);
		appPanel.setVisible(true);
		appPanel.addOutputAvailableListener(this);
		
		frame.getDisplayPanel().add(appPanel);
		frame.setVisible(true);	
		/*Set application dimensions */
		Rectangle rect = getClientToHost().getAvailableScreen(null);			
		frame.setBounds(rect.getRefPointX(), rect.getRefPointY(), rect.getWidth(), rect.getHeight());
		
		/*Notify Host application was launched*/							
		dataMgr = ApplicationDataManagerFactory.getInstance();
		ApplicationImpl appImpl = new ApplicationImpl();
		appImpl.addWG23Listener(this);
		setAndDeployApplicationService(appImpl);		
		getClientToHost().notifyStateChanged(State.IDLE);	
		
		File tempPath = new File(aimFolder);
		
		if (!tempPath.exists())
			tempPath.mkdir();
	}
	
	public static void main(String[] args) {
		try {
			/*args = new String[4];
			args[0] = "--hostURL";
			args[1] = "http://localhost:8090/HostInterface";
			args[2] = "--applicationURL";
			args[3] = "http://localhost:8060/ApplicationInterface";*/	
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			System.out.println("Number of parameters: " + args.length);
			for (int i = 0; i < args.length; i++){
				System.out.println(i + ". " + args[i]);
			}
			URL hostURL = null;
			URL applicationURL = null;
			for (int i = 0; i < args.length; i++){
				if (args[i].equalsIgnoreCase("--hostURL")){
					hostURL = new URL(args[i + 1]);
				}else if(args[i].equalsIgnoreCase("--applicationURL")){
					applicationURL = new URL(args[i + 1]);
				}					
			}									
			new AlgorithmExecution(hostURL, applicationURL);										
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MalformedURLException e) {			
			e.printStackTrace();
		} catch (NullPointerException e){
			new ExceptionDialog("List of parameters is not valid!", 
					"Ensure: --hostURL url1 --applicationURL url2",
					"Launch Application Dialog");
			System.exit(0);
		}
	}

	@Override
	public boolean bringToFront() {
		frame.setAlwaysOnTop(true);
		frame.setAlwaysOnTop(false);
		return false;
	}

	@Override
	public void notifyDataAvailable(AvailableData availableData, boolean lastData) {
		// TODO Auto-generated method stub
		clearDir(new File(aimFolder));
		
		List<ObjectDescriptor> aimObjects = availableData.getObjectDescriptors().getObjectDescriptor();		
		ArrayOfUUID arrayUUIDsAim = new ArrayOfUUID();
		List<Uuid> listUUIDsAim = arrayUUIDsAim.getUuid();
		for(int i = 0; i < aimObjects.size(); i++){
			listUUIDsAim.add(aimObjects.get(i).getUuid());
		}	
		ArrayOfObjectLocator arrayOfObjectLocator = getClientToHost().getDataAsFile(arrayUUIDsAim, false);
		List<ObjectLocator> objectLocatorsAim = arrayOfObjectLocator.getObjectLocator();
		
		for (int i = 0; i < objectLocatorsAim.size(); i++) {
			try {
				File inputFile = new File(new URI(objectLocatorsAim.get(i).getUri()));
				File outputFile = new File(String.format("%s/SEED_AIM_%d.xml", aimFolder, i));
				
//				System.out.println(inputFile.getPath());
	
				FileReader in;
				FileWriter out;
				int c;
	
				in = new FileReader(inputFile);
				out = new FileWriter(outputFile);
				while ((c = in.read()) != -1)
					out.write(c);
	
				in.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		

		appPanel.updateAimList(aimFolder);
		
	}

	@Override
	public boolean setState(State newState) {
		if(State.valueOf(newState.toString()).equals(State.CANCELED)){
			getClientToHost().notifyStateChanged(State.CANCELED);
			getClientToHost().notifyStateChanged(State.IDLE);
		}else if(State.valueOf(newState.toString()).equals(State.EXIT)){
			getClientToHost().notifyStateChanged(State.EXIT);						
			//terminating endpoint and existing system is accomplished through ApplicationTerminator
			//and ApplicationScheduler. ApplicationSechduler is present to alow termination delay if needed (posible future use)
			ApplicationTerminator terminator = new ApplicationTerminator(getEndPoint());
			Thread t = new Thread(terminator);
			t.start();	
		}else{
			getClientToHost().notifyStateChanged(newState);
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void outputAvailable(OutputAvailableEvent e) {
		List<File> output = (List<File>)e.getSource();
		WG23DataModel wg23DM = new WG23DataModelImpl(output);		
		dataMgr.setOutputData(wg23DM);
		AvailableData availableData = wg23DM.getAvailableData();		
		getClientToHost().notifyDataAvailable(availableData, true);	
	}

	public static boolean clearDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) { 
				boolean success = new File(String.format("%s/%s", dir.getPath(),children[i])).delete(); 
				if (!success) {
					return false; 
					} 
				} 
			}
		
		return true; 
	} 
	
}
