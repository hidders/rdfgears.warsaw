package nl.tudelft.rdfgears.rgl.datamodel.value.impl;

import java.util.Collections;
import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;


public class EmptyRecordValue extends RecordValue {
	private static EmptyRecordValue singleton = new EmptyRecordValue();
	
	private EmptyRecordValue(){ /* do nothing */ }
	
	@Override
	public RGLValue get(String fieldName) {
		return null; // no such element
	}

	@Override
	public Set<String> getRange() {
		return Collections.emptySet();
	}
	
	public static AbstractRecordValue getInstance(){
		return singleton;
	}

}
