package nl.tudelft.rdfgears.util.row;

import java.util.Set;

public interface FieldIndexMapIface {
	
	/** 
	 * get the array-index we should use for the given fieldName. 
	 * Returns -1 if the fieldName is not stored in the array we are describing. 
	 * @param fieldName
	 * @return
	 */
	public int getIndex(String fieldName); 
	
	public Set<String> getFieldNameSet(); 
	
	public int size(); 
	

}
