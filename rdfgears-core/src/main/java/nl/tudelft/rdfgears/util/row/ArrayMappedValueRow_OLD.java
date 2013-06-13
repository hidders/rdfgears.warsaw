package nl.tudelft.rdfgears.util.row;

import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.NoSuchFieldInRowException;


/**
 * A row over RGLValue objects. Inspired by FieldMappedValueRow, but uses an array with fieldNames instead of 
 * a fieldIndexMap.
 * 
 * It is thus optimized to use as little memory as possible, as the lazy evaluation makes that it can be 
 * instantiated many times, and kept in memory until the function is executed. 
 * 
 * The same array of field names can be used for many rows, and the rows themselves only need an array to store
 * their values.  
 * 
 * @author Eric Feliksik
 *
 */
public class ArrayMappedValueRow_OLD extends AbstractRow<RGLValue> implements ValueRow {
	
	/* the array storing our RGLValue objects */
	private RGLValue[] valueArray;	
	
	/* An array pointing to the fields of this array. The indices correspond with the valueArray's indices to map names to values 
	 * same array is used in many ArrayMappedValueRow instances */
	private String[] nameArray; 

	private boolean recyclable; 
	
	/**
	 * Create a ValueRow that can contain elements for the domain specified by the domain of 
	 * fieldIndexMap.   
	 */
	public ArrayMappedValueRow_OLD(String[] fieldNameArray){
		assert(nameArray!=null);
		nameArray = fieldNameArray;
		valueArray = new RGLValue[nameArray.length];
	}
//	
//	/**
//	 * Create a ValueRow with given function, and copy the given array.
//	 */
//	private ArrayMappedValueRow(String[] nameArray, RGLValue[] values){
//		this(nameArray);
//		assert(values!=null);
//		assert(nameArray.length==values.length);
//		
//		/* System.arrayCopy is only faster, I read, for large arrays */
//        for (int i=0; i<valueArray.length; i++)
//        	valueArray[i] = values[i];
//	}
	
//	/**
//	 * Clone this ValueRow. That is, make a new instance, with a shallow copy of the interal 
//	 * array of RGLValue objects, and reference the same RGLFunction.
//	 */
//	public ArrayMappedValueRow clone(){
//		return new ArrayMappedValueRow(fiMap, valueArray);		
//	}

	/**
	 * find index of given fieldName for nameArray and valueArray 
	 */
	private int findIndex(String fieldName){
		if (fieldName==null)
			throw new NoSuchFieldInRowException(null);
		
		for (int i = 0; i<nameArray.length; i++){
			if (fieldName.equals(nameArray[i]))
				return i;
		}
		throw new NoSuchFieldInRowException(fieldName);
	}
	
	@Override
	public RGLValue get(String fieldName) {
		return valueArray[findIndex(fieldName)]; 
	} 
	
	public void put(String fieldName, RGLValue element) {
		valueArray[findIndex(fieldName)] = element;
	}
	
	@Override
	public Set<String> getRange() {
		return null;
		//return fiMap.getFieldNameSet();
	}
	
	/**
	 * Return true iff the row can be recycled, that is, there is not LazyValues waiting to use it for it's value generation.
	 * Recycling means that the valueArray can be filled with other values, so that the valuerow can be used
	 * for a new LazyValue. 
	 * 
	 * IT IS NOT YET CLEAR WHETHER THIS ACTUALLY HELPS! It seems not. 
	 * @return
	 */
	public boolean isRecyclable() {
		return recyclable;
	}

	public void setRecyclable(boolean recyclable) {
		this.recyclable = recyclable;
	}
	
//	@Override
//	public Set<String> getRange() {
//		return function.getRequiredInputNames();
//	}
	
	

}
