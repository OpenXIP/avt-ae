package com.siemens.cmiv.avt.ae.core;

import java.util.EventObject;

import com.siemens.cmiv.avt.mvt.datatype.AnnotationEntry;

@SuppressWarnings("serial")
public class AlgorithmExecuteEvent extends EventObject{

	public AlgorithmExecuteEvent(AnnotationEntry arg0) {
		super(arg0);
	}

}
