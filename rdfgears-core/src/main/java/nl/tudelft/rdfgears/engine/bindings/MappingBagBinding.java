package nl.tudelft.rdfgears.engine.bindings;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MappingBagValue;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class MappingBagBinding extends StreamingBagBinding<MappingBagValue> {

	ValueRow inputRow;
	FunctionProcessor processor;

	public MappingBagBinding() {
		super();
	}

	public MappingBagBinding(MaterializingBag materializingBag,
			ValueRow inputRow, FunctionProcessor processor) {
		this.materializingBag = materializingBag;
		this.inputRow = inputRow;
		this.processor = processor;
	}

	@Override
	protected MappingBagValue createPureValue() {
		return new MappingBagValue(id, inputRow, processor);
	}

	@Override
	protected MappingBagValue createMaterializingValue() {
		return new MappingBagValue(id, materializingBag, inputRow, processor);
	}

	@Override
	protected void writeMembers(TupleOutput out) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void readMembers(TupleInput in) {
		// TODO Auto-generated method stub

	}

	TupleBinding<Map<String, Long>> idMappingsBinding = new TupleBinding<Map<String, Long>>() {

		@Override
		public void objectToEntry(Map<String, Long> map, TupleOutput out) {
			out.writeInt(map.size());
			for (Entry<String, Long> e : map.entrySet()) {
				out.writeString(e.getKey());
				out.writeLong(e.getValue());
			}
		}

		@Override
		public Map<String, Long> entryToObject(TupleInput in) {
			Map<String, Long> ret = new HashMap<String, Long>();
			int size = in.readInt();

			for (int i = 0; i < size; ++i) {
				ret.put(in.readString(), in.readLong());
			}

			return ret;
		}
	};

}
