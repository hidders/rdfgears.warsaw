package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.nl.tudelft.rdfgearsUI.client.RGType;

public class RGFunctionParamListInputData {
	String name, label;
	RGType type;
	boolean iterate;
	public RGFunctionParamListInputData(String _name, String _label, RGType _type, boolean _iterate){
		name = _name;
		label = _label;
		type = _type;
		iterate = _iterate;
	}
}
