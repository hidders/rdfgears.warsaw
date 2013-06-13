package nl.tudelft.rdfgears.engine.bindings;

import java.util.Map;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class OrderedBagBinding extends ComplexBinding {
	protected void writeMap(Map<Long, Integer> map, TupleOutput out) {
		new IteratorsMapBinding().objectToEntry(map, out);
	}
	
	protected Map<Long, Integer> readMap(TupleInput in) {
		return new IteratorsMapBinding().entryToObject(in);
	}

}
