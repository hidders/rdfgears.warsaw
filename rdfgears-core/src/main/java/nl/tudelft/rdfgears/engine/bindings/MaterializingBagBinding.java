package nl.tudelft.rdfgears.engine.bindings;

import java.util.List;
import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class MaterializingBagBinding extends OrderedBagBinding {

	private List<RGLValue> storedList;
	private long id;
	private Map<Long, Integer> iteratorPosition;
	private boolean createListBackedBag;

	private class InnerBinding extends TupleBinding<RGLValue> {

		@Override
		public RGLValue entryToObject(TupleInput in) {
			List<RGLValue> backingList = BindingFactory.createListBinding()
					.entryToObject(in);
			if (!createListBackedBag) {
				return new MaterializingBag(backingList, null);
			} else {
				assert (iteratorPosition != null);
				return new ListBackedBagValue(id, iteratorPosition, backingList);
			}
		}

		@Override
		public void objectToEntry(RGLValue bag, TupleOutput out) {
			assert (storedList != null);
			BindingFactory.createListBinding().objectToEntry(storedList, out);
		}

	}

	@Override
	protected TupleBinding<RGLValue> getInnerBinding() {
		return new InnerBinding();
	}

	public MaterializingBagBinding(List<RGLValue> storedList) {
		this.storedList = storedList;
	}

	public MaterializingBagBinding(long id, Map<Long, Integer> iteratorPosition) {
		this.id = id;
		this.iteratorPosition = iteratorPosition;
		this.createListBackedBag = true;
	}

	public MaterializingBagBinding() {
		createListBackedBag = false;
	}
}
