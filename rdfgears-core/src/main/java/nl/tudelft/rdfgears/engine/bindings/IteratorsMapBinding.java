package nl.tudelft.rdfgears.engine.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class IteratorsMapBinding extends TupleBinding<Map<Long, Integer>> {

	@Override
	public Map<Long, Integer> entryToObject(TupleInput in) {
		int size = in.readInt();
		Map<Long, Integer> map = new HashMap<Long, Integer>();

		for (int i = 0; i < size; ++i) {
			map.put(in.readLong(), in.readInt());
		}

		return map;
	}

	@Override
	public void objectToEntry(Map<Long, Integer> map, TupleOutput out) {
		out.writeInt(map.size());
		for (Entry<Long, Integer> entry : map.entrySet()) {
			out.writeLong(entry.getKey());
			out.writeInt(entry.getValue());
		}

	}

}
