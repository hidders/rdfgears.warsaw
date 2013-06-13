package nl.tudelft.rdfgears.util.row;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FieldIndexHashMap implements FieldIndexMap {
	private Map<String, Integer> map = new HashMap<String, Integer>();
	
	/** 
	 * get the array-index we should use for the given fieldName. 
	 * Returns -1 if the fieldName is not stored in the array we are describing. 
	 * @param fieldName
	 * @return
	 */
	public int getIndex(String fieldName){
		assert(map.get(fieldName)!=null) : "I expected the name "+fieldName+", but it's not there. Did you typecheck? If so, this is a bug.";
		Integer intObj = map.get(fieldName);
		return (intObj!=null ? intObj.intValue() : -1);
	}
	
	/**
	 * add a fieldName  
	 * @param fieldName
	 * @return The new index on which it is stored. 
	 */
	public int addFieldName(String fieldName){
		int newIndex = size();
		map.put(fieldName, new Integer(newIndex));
		return newIndex;
	}
	
	public Set<String> getFieldNameSet(){
		return map.keySet();
	}
	
	public int size(){
		return map.size();
	}
	

	/** 
	 * Give a fieldmap that contains the union of the two ranges.
	 * This could return a new FieldMap, or reuse an old one. So the called should not modify the result.  
	 * @param range1
	 * @param range2
	 * @return
	 */
	public static FieldIndexHashMap union(Set<String> range1, Set<String> range2) {
		FieldIndexHashMap fiMap = new FieldIndexHashMap();
		
		for(String name : range1){
			fiMap.addFieldName(name);	
		}
		for(String name : range2){
			if (! range1.contains(name)){
				// not yet added to fieldmap
				fiMap.addFieldName(name);	
			}
		}
		
		return fiMap;
	}
}
