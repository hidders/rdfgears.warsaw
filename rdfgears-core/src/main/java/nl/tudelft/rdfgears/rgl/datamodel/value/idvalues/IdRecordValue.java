package nl.tudelft.rdfgears.rgl.datamodel.value.idvalues;

import java.util.Set;

import nl.tudelft.rdfgears.engine.bindings.idvalues.IdRecordBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;

import com.sleepycat.bind.tuple.TupleBinding;

public class IdRecordValue extends IdRGLValue implements AbstractRecordValue {

	public IdRecordValue(AbstractRecordValue value) {
		super(value);
	}

	public IdRecordValue(long id) {
		super(id);
	}

	@Override
	public RGLValue get(String fieldName) {
		return ((AbstractRecordValue) fetch()).get(fieldName);
	}

	@Override
	public Set<String> getRange() {
		return ((AbstractRecordValue) fetch()).getRange();
	}
	
	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new IdRecordBinding();
	}
}
