package nl.tudelft.rdfgears.engine.bindings;

import java.util.List;
import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ListBackedBagBinding extends OrderedBagBinding {

	private class InnerBinding extends TupleBinding<RGLValue> {

		@Override
		public RGLValue entryToObject(TupleInput in) {
			Map<Long, Integer> iteratorPosition = readMap(in);
			List<RGLValue> backingList = BindingFactory.createListBinding()
			.entryToObject(in);
			return new ListBackedBagValue(id, iteratorPosition, backingList);
		}

		@Override
		public void objectToEntry(RGLValue bag, TupleOutput out) {
			ListBackedBagValue listBag = (ListBackedBagValue) bag;
			writeMap(listBag.getIteratorMap(), out);
			BindingFactory.createListBinding().objectToEntry(
					listBag.getBackingList(), out);
		}

	}

	@Override
	protected TupleBinding<RGLValue> getInnerBinding() {
		return new InnerBinding();
	}

}
