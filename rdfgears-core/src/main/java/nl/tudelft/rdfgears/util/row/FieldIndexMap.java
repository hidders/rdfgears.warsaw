package nl.tudelft.rdfgears.util.row;

import java.io.Serializable;
import java.util.Set;

public interface FieldIndexMap extends Serializable {
	
	/** 
	 * get the array-index we should use for the given fieldName. 
	 * throws a NoSuchFieldInRowExceptionif the fieldName is not stored in the array we are describing. 
	 * @param fieldName
	 * @return
	 */
	public int getIndex(String fieldName); 
	
	public Set<String> getFieldNameSet(); 
	
	public int size(); 
	
}
