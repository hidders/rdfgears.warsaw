package nl.tudelft.rdfgears.engine.bindings;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.value.OrderedBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class StreamingBagBinding<T extends StreamingBagValue> extends
		OrderedBagBinding {
	private class InnerBinding extends TupleBinding<RGLValue> {
		@Override
		public RGLValue entryToObject(TupleInput in) {
			iteratorPosition = readMap(in);
			boolean isNotMaterializing = in.readBoolean();
			if (isNotMaterializing) { // materializingBag == null
				readMembers(in);
				return createPureValue();
			} else { // materializingBag != null
				Boolean contentListComplete = in.readBoolean();
				if (contentListComplete) {
					// bag was fully materialized, can just return the
					// ListBackedBag with its contents
					return new MaterializingBagBinding(id, iteratorPosition)
							.entryToObject(in);
				} else {
					materializingBag = (MaterializingBag) new MaterializingBagBinding()
							.entryToObject(in);
					readMembers(in);
					StreamingBagValue sfBag = (StreamingBagValue) createMaterializingValue();
					sfBag.prepareForMultipleReadings();
					return sfBag;
				}
			}
		}

		@Override
		public void objectToEntry(RGLValue bag, TupleOutput out) {
			OrderedBagValue oBag = (OrderedBagValue) bag.asBag();
			writeMap(oBag.getIteratorMap(), out);
			out.writeBoolean(materializingBag == null);
			if (materializingBag == null) {
				writeMembers(out);
			} else {
				out.writeBoolean(materializingBag.contentListComplete());
				materializingBag.getBinding().objectToEntry(materializingBag,
						out);
				if (!materializingBag.contentListComplete()) {
					writeMembers(out);
				}
			}
		}
	}

	@Override
	protected TupleBinding<RGLValue> getInnerBinding() {
		return new InnerBinding();
	}

	protected MaterializingBag materializingBag;
	protected Map<Long, Integer> iteratorPosition;

	protected abstract T createPureValue();

	protected abstract T createMaterializingValue();

	/**
	 * Writes only the members specific for given kind of bag - the
	 * administration common for all StreamingBag (like writing the
	 * MaterializingBag) is done in
	 * {@link StreamingBagBinding.InnerBinding#entryToObject(TupleInput)}
	 * 
	 * @param out
	 */
	protected abstract void writeMembers(TupleOutput out);

	protected abstract void readMembers(TupleInput in);

}
