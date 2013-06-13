package com.nl.tudelft.rdfgearsUI.client.Dia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.nl.tudelft.rdfgearsUI.client.RGType;

public class RGFunctionParamListItem {
	int idx;
	String value;
	String label;
	int inputDataNum;
	
	private ArrayList <String> inputIds = new ArrayList <String>();
	private Map <String, RGFunctionParamListInputData> inputIdMap = new HashMap<String, RGFunctionParamListInputData>();
	
	public RGFunctionParamListItem(int _idx, String v, String l){
		idx = _idx;
		value = v;
		label = l;
	}
	
	public void addInputData(String _name, String _label, RGType _type, boolean _iterate){
		inputIds.add(_name);
		inputIdMap.put(_name, new RGFunctionParamListInputData(_name, _label, _type, _iterate));
	}
	
	public int getInputNum(){
		return inputIds.size();
	}
	
	public RGFunctionParamListInputData getInputDataByIdx(int idx){
		return (inputIdMap.get(inputIds.get(idx)));
	}
}
