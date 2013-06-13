package nl.tudelft.rdfgears.engine.bindings;

import java.util.Iterator;
import java.util.List;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class NaiveBagBinding extends ComplexBinding {
	private class InnerBinding extends TupleBinding<RGLValue> {

		@Override
		public RGLValue entryToObject(TupleInput in) {
			int size = in.readInt();
			List<RGLValue> backingList = ValueFactory.createBagBackingList();
			for (int i = 0; i < size; ++i) {
				backingList.add(BindingFactory.createBinding(in.readString())
						.entryToObject(in));
			}
			return new ListBackedBagValue(id, backingList);
		}

		@Override
		public void objectToEntry(RGLValue value, TupleOutput out) {
			AbstractBagValue bag = value.asBag();
			out.writeInt(bag.size());
			Iterator<RGLValue> it = bag.iterator();
			for (RGLValue element : bag) {
				out.writeString(element.getClass().getName());
				element.getBinding().objectToEntry(element, out);
			}
		}

	}

	@Override
	protected TupleBinding<RGLValue> getInnerBinding() {
		return new InnerBinding();
	}
}
