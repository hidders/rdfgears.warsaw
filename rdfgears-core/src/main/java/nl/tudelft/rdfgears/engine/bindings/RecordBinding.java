package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.ModifiableRecord;
import nl.tudelft.rdfgears.util.row.FieldIndexHashMap;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * @author Tomasz Traczyk
 * 
 *         This class relays only on fulfilling by Record implementation of the
 *         ValueRow interface and thus will work (not necessarily optimal) for
 *         any further implementations of Record that would fulfill the
 *         mentioned inteface.
 */
public class RecordBinding extends ComplexBinding {

	private class InnerBinding extends TupleBinding<RGLValue> {

		@Override
		public RGLValue entryToObject(TupleInput in) {
			ValueRow valueRow =new ValueRowBinding()
					.entryToObject(in);
			FieldIndexHashMap map = new FieldIndexHashMap();
			for (String s : valueRow.getRange()) {
				map.addFieldName(s);
			}
			ModifiableRecord modifiableRecord = new ModifiableRecord(id, map);
			
			for (String s : valueRow.getRange()) {
				modifiableRecord.put(s, valueRow.get(s));
			}
			return modifiableRecord;
		}

		@Override
		public void objectToEntry(RGLValue record, TupleOutput out) {
			new ValueRowBinding().objectToEntry(record.asRecord(), out);
		}

	}

	@Override
	protected TupleBinding<RGLValue> getInnerBinding() {
		return new InnerBinding();
	}
}
