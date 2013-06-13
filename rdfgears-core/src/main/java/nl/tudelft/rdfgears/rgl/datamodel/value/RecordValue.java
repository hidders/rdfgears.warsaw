package nl.tudelft.rdfgears.rgl.datamodel.value;

import java.util.Set;

import nl.tudelft.rdfgears.engine.bindings.RecordBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;
import nl.tudelft.rdfgears.rgl.exception.ComparisonNotDefinedException;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.sleepycat.bind.tuple.TupleBinding;

/**
 * An abstract implementation of RecordValue. Implementing classes must behave like a ValueRow
 * @author Eric Feliksik
 *
 */
public abstract class RecordValue extends DeterminedRGLValue implements ValueRow, AbstractRecordValue {
	public abstract RGLValue get(String fieldName);

	//public abstract Iterator<String> fieldNames();
	public abstract Set<String> getRange();
	
	public void accept(RGLValueVisitor visitor){
		visitor.visit(this);
	}
	
	@Override
	public AbstractRecordValue asRecord(){
		return this;
	}
	
	@Override
	public boolean isRecord(){
		return true;
	}

	public int compareTo(RGLValue v2) {
		// but may be implemented by subclass. It must be determined what is comparable, i think it'd be elegant to make as much as possible comparable.
		throw new ComparisonNotDefinedException(this, v2);
	}
	

	@Override
	public void prepareForMultipleReadings() {
		/* nothing to do */
	}

	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new RecordBinding();
	}
	
	

}
