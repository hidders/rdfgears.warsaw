package nl.tudelft.rdfgears.util.row;

import java.util.Collections;
import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;


/**
 * A row over RGLValues, where the range contains only a single element. 
 * 
 * @author Eric Feliksik
 *
 */
public class SingleElementValueRow implements ValueRow {	
	private RGLValue value;
	private String key; 
	
	/**
	 * Create a ValueRow that can contain elements for the domain specified by the domain of 
	 * fieldIndexMap.   
	 */
	public SingleElementValueRow(String key, RGLValue value){
		assert(key!=null);
		assert(value!=null);
		
		this.key = key;
		this.value = value;
	}
	
	@Override
	public RGLValue get(String fieldName) {
		if (key.equals(fieldName)){
			return value;
		} else {
			return null;
		}		
	}
	
	@Override
	public Set<String> getRange() {
		return Collections.singleton(key);
	}

}
