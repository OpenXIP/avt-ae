package com.siemens.cmiv.avt.ae.core;

import java.util.EventListener;

public interface AEListener extends EventListener{
	public void executionResultsAvailable(AlgorithmExecuteEvent e);

}
