package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * <p>
 * This is a binding, that does only register the value in central memory store
 * represented by ValueInflator and than just write the value's Id to disk. Thus
 * the value doesn't get really stored to disk.
 * </p>
 * 
 * <p>
 * This Binding should be used as rarely as possible - it's just an emergency
 * binding for values that won't match any existing binding (f.e. because there
 * were introduced after the development of bindings.
 * </p>
 * @author Tomasz Traczyk
 *
 */
public class MemoryBinding extends TupleBinding<RGLValue> {

	@Override
	public RGLValue entryToObject(TupleInput in) {
		long id = in.readLong();
		return ValueManager.getMemoryValue(id);
	}

	@Override
	public void objectToEntry(RGLValue v, TupleOutput out) {
		ValueManager.registerMemoryValue(v);
		out.writeLong(v.getId());
	}
}
