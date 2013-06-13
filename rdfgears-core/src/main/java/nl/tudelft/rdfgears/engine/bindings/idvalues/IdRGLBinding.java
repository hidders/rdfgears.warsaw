package nl.tudelft.rdfgears.engine.bindings.idvalues;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IdRGLBinding extends TupleBinding<RGLValue> {

	@Override
	public final RGLValue entryToObject(TupleInput in) {
		long id = in.readLong();
		return createIdValue(id);
	}

	@Override
	public final void objectToEntry(RGLValue value, TupleOutput out) {
		out.writeLong(value.getId());
	}

	protected IdRGLValue createIdValue(long id) {
		return new IdRGLValue(id);
	}

}
