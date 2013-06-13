package nl.tudelft.rdfgears.rgl.datamodel.value.ifaces;

import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * An abstract implementation of RecordValue. Implementing classes must behave like a ValueRow
 * @author Eric Feliksik
 *
 */
public interface AbstractRecordValue extends ValueRow, RGLValue {
	public abstract RGLValue get(String fieldName);

	//public abstract Iterator<String> fieldNames();
	public abstract Set<String> getRange();
	
}
