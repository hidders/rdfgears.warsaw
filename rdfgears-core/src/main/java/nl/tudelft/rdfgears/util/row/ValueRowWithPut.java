package nl.tudelft.rdfgears.util.row;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;


/**
 * editable valueRow 
 * 
 * @author Eric Feliksik
 *
 */
public interface ValueRowWithPut extends ValueRow {
	
	public void put(String fieldName, RGLValue value);
	
}
