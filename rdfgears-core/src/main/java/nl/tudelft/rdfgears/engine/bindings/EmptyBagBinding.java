package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.EmptyBag;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class EmptyBagBinding extends TupleBinding<RGLValue> {

	@Override
	public RGLValue entryToObject(TupleInput in) {
		return new EmptyBag(in.readLong());
	}

	@Override
	public void objectToEntry(RGLValue bag, TupleOutput out) {
		out.writeLong(bag.getId());
	}

}
