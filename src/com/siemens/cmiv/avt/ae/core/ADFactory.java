package com.siemens.cmiv.avt.ae.core;

import com.siemens.scr.avt.ad.api.ADFacade;
import com.siemens.scr.avt.ad.api.FacadeManager;

public class ADFactory {	
		private static ADFacade avtMgr = FacadeManager.getFacade();
		
		private ADFactory(){};
		
		public static ADFacade getADServiceInstance(){				
			return avtMgr;
		}
}
