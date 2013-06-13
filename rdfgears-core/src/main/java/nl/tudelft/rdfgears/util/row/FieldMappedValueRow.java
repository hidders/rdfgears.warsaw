package nl.tudelft.rdfgears.util.row;

import java.io.Serializable;
import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;


/**
 * A row over RGLValue objects 
 * It is used as input for the RGLFunction execute() call, and for RecordValue implementation.
 *   
 * This class is optimized to use as little memory as possible, as the lazy evaluation makes that it can be 
 * instantiated many times, and kept in memory until the function is executed. 
 * 
 * This is done by using a FieldIndexMap for instantiation. This object maps fieldNames to integer indices. 
 * The same FieldIndexMap can be used for many rows, and the rows themselves only need an array to store
 * their values.  
 * 
 * @author Eric Feliksik
 *
 */
public class FieldMappedValueRow extends AbstractRow<RGLValue> implements ValueRowWithPut, Serializable {
	
	/* the array storing our RGLValue objects */
	RGLValue[] valueArray;	
	
	/* A map that tells us in what index we should store what key value. 
	 * We could have used a HashMap<String, RGLValue> for every ValueRow, 
	 * but the small keySets make that the overhead is relatively large. So we still use a Map
	 * to find the index (it isn't that slow, as Strings cache their hashCode), but we save memory
	 * because the map is reused for many ValueRows, each ValueRow only having an expr-array. */
	private FieldIndexMap fiMap;

//	private boolean recyclable; 
	
	/**
	 * Create a ValueRow that can contain elements for the domain specified by the domain of 
	 * fieldIndexMap.   
	 */
	public FieldMappedValueRow(FieldIndexMap fieldIndexMap){
		this.fiMap = fieldIndexMap;
		valueArray = new RGLValue[fieldIndexMap.size()];
	}
	
	/**
	 * Create a ValueRow with given function, and copy the given array. 
	 */
	private FieldMappedValueRow(FieldIndexMap fieldIndexMap, RGLValue[] values){
		this(fieldIndexMap);
		assert(values!=null);
		assert(valueArray.length==values.length);
		
		/* System.arrayCopy is only faster, I read, for large arrays */
        for (int i=0; i<valueArray.length; i++)
        	valueArray[i] = values[i];
	}
	
	/**
	 * Clone this ValueRow. That is, make a new instance, with a shallow copy of the interal 
	 * array of RGLValue objects, and reference the same RGLFunction.
	 */
	public FieldMappedValueRow clone(){
		return new FieldMappedValueRow(fiMap, valueArray);		
	}
	
	@Override
	public RGLValue get(String fieldName) {
		return valueArray[fiMap.getIndex(fieldName)];
	} 

	@Override
	public void put(String fieldName, RGLValue element) {
		valueArray[fiMap.getIndex(fieldName)] = element;
	}

	@Override
	public Set<String> getRange() {
		return fiMap.getFieldNameSet();
	}
	
//	/**
//	 * Return true iff the row can be recycled, that is, there is not LazyValues waiting to use it for it's value generation.
//	 * Recycling means that the valueArray can be filled with other values, so that the valuerow can be used
//	 * for a new LazyValue. 
//	 * 
//	 * IT IS NOT YET CLEAR WHETHER THIS ACTUALLY HELPS! It seems not. 
//	 * @return
//	 */
//	public boolean isRecyclable() {
//		return recyclable;
//	}
//
//	public void setRecyclable(boolean recyclable) {
//		this.recyclable = recyclable;
//	}
	
//	@Override
//	public Set<String> getRange() {
//		return function.getRequiredInputNames();
//	}
	


}



//
//
//
//
//package nl.tudelft.rdfgears.util.row;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
//import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
//
//
///**
// * A row over RGLValue objects 
// * It is used as input for the RGLFunction execute() call, and for RecordValue implementation.
// *   
// * This class is optimized to use as little memory as possible, as the lazy evaluation makes that it can be 
// * instantiated many times, and kept in memory until the function is executed. 
// * 
// * This is done by using a FieldIndexMap for instantiation. This object maps fieldNames to integer indices. 
// * The same FieldIndexMap can be used for many rows, and the rows themselves only need an array to store
// * their values.  
// * 
// * @author Eric Feliksik
// *
// */
//public class FieldMappedValueRow extends AbstractRow<RGLValue> implements ValueRowWithPut {
//	
//	/* the array storing our RGLValue objects */
//	private Map<String, RGLValue> map;
//	boolean recyclable = false;
//
//	
//	/* A map that tells us in what index we should store what key value. 
//	 * We could have used a HashMap<String, RGLValue> for every ValueRow, 
//	 * but the small keySets make that the overhead is relatively large. So we still use a Map
//	 * to find the index (it isn't that slow, as Strings cache their hashCode), but we save memory
//	 * because the map is reused for many ValueRows, each ValueRow only having an expr-array. */
//
//	/**
//	 * Create a ValueRow that can contain elements for the domain specified by the domain of 
//	 * fieldIndexMap.   
//	 */
//	public FieldMappedValueRow(Map<String, RGLValue> newMap){
//		map = newMap;
//	}
//
//	/**
//	 * Create a ValueRow that can contain elements for the domain specified by the domain of 
//	 * fieldIndexMap.   
//	 */
//	public FieldMappedValueRow(FieldIndexMap fieldIndexMap){
//		map = new HashMap<String, RGLValue>();
//	}
//	
//	
//	/**
//	 * Clone this ValueRow. That is, make a new instance, with a shallow copy of the interal 
//	 * array of RGLValue objects, and reference the same RGLFunction.
//	 */
//	public FieldMappedValueRow clone(){
//		return new FieldMappedValueRow(new HashMap<String, RGLValue>(map));		
//	}
//	
//	@Override
//	public RGLValue get(String fieldName) {
//		return  map.get(fieldName);
//	} 
//
//	@Override
//	public void put(String fieldName, RGLValue element) {
//		map.put(fieldName, element);
//	}
//
//	@Override
//	public Set<String> getRange() {
//		return map.keySet();
//	}
//	
////	
////	/**
////	 * Return true iff the row can be recycled, that is, there is not LazyValues waiting to use it for it's value generation.
////	 * Recycling means that the valueArray can be filled with other values, so that the valuerow can be used
////	 * for a new LazyValue. 
////	 * 
////	 * IT IS NOT YET CLEAR WHETHER THIS ACTUALLY HELPS! It seems not. 
////	 * @return
////	 */
////	public boolean isRecyclable() {
////		return recyclable;
////	}
////
////	public void setRecyclable(boolean recyclable) {
////		this.recyclable = recyclable;
////	}
//	
////	@Override
////	public Set<String> getRange() {
////		return function.getRequiredInputNames();
////	}
//	
//
//
//}
