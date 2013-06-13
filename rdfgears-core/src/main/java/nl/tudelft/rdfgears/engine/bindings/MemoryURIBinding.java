/**
 * 
 */
package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryURIValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author Tomasz Traczyk
 * 
 */
public class MemoryURIBinding extends TupleBinding<RGLValue> {

	@Override
	public RGLValue entryToObject(TupleInput in) {
		return new MemoryURIValue(in.readString());
	}

	@Override
	public void objectToEntry(RGLValue v, TupleOutput out) {
		out.writeString(v.getRDFNode().asResource().getURI());
	}

}
