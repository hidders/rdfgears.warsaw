package nl.tudelft.rdfgears.util.row;

import java.util.Set;

import nl.tudelft.rdfgears.engine.bindings.ValueRowBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

import com.sleepycat.bind.tuple.TupleBinding;


/**
 * An interface for a row over RGLValue objects. That is, a mapping of fieldNames to RGLValues.
 * 
 * So it is actually a record; but we formally distinguish RGLValue-records and ValueRows. 
 * A ValueRow can be input to a function, but it is not an RGLValue. 
 * 
 * @author Eric Feliksik
 *
 */
public interface ValueRow {
	
	/**
	 * Get the LazyRGLValue to which 'fieldName' is mapped 
	 * @param fieldName
	 * @return
	 */
	public RGLValue get(String fieldName);

	public Set<String> getRange();
	
}
