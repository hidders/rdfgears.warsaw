package nl.tudelft.rdfgears.engine.bindings;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.SingletonBag;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class SingletonBagBinding extends OrderedBagBinding {
	private class InnerBinding extends TupleBinding<RGLValue> {

		@Override
		public RGLValue entryToObject(TupleInput in) {
			Map<Long, Integer> iteratorsMap = readMap(in);
			String className = in.readString();
			RGLValue element = BindingFactory.createBinding(
					className).entryToObject(in);
			return new SingletonBag(id, iteratorsMap, element);
		}

		@Override
		public void objectToEntry(RGLValue value, TupleOutput out) {
			SingletonBag sBag = (SingletonBag) value.asBag();
			writeMap(sBag.getIteratorMap(), out);
			RGLValue element = sBag.iterator().next();
			out.writeString(element.getClass().getName());
			element.getBinding().objectToEntry(element, out);
		}

	}

	@Override
	protected TupleBinding<RGLValue> getInnerBinding() {
		return new InnerBinding();
	}
}
