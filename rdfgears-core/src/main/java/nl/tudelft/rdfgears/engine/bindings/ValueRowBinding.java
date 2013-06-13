package nl.tudelft.rdfgears.engine.bindings;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.util.row.FieldIndexHashMap;
import nl.tudelft.rdfgears.util.row.FieldMappedValueRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ValueRowBinding extends TupleBinding<ValueRow> {

	@Override
	public ValueRow entryToObject(TupleInput in) {
		int size = in.readInt();
		FieldIndexHashMap fiMap = new FieldIndexHashMap();
		List<String> fieldNames = new ArrayList<String>();

		for (int i = 0; i < size; ++i) {
			String fieldName = in.readString();
			fieldNames.add(fieldName);
			fiMap.addFieldName(fieldName);
		}

		FieldMappedValueRow row = new FieldMappedValueRow(fiMap);

		for (String fieldName : fieldNames) {
			if (in.readBoolean()) { // fieldValue == null
				row.put(fieldName, null);
			} else { // fieldValue != null
				if (in.readBoolean()) { // reading simple value
					String className = in.readString();
					row.put(fieldName, BindingFactory.createBinding(className)
							.entryToObject(in));
				} else { // creating DiskRGLValue representation of complex
							// value
					row.put(fieldName, new IdRGLValue(in
							.readLong()));
				}
			}
		}

		return row;
	}

	@Override
	public void objectToEntry(ValueRow inputRow, TupleOutput out) {
		out.writeInt(inputRow.getRange().size());
		for (String fieldName : inputRow.getRange()) {
			out.writeString(fieldName);
		}
		for (String fieldName : inputRow.getRange()) {
			RGLValue fieldValue = inputRow.get(fieldName);
			out.writeBoolean(fieldValue == null);
			if (fieldValue != null) {
				out.writeBoolean(fieldValue.isSimple());
				out.writeString(fieldValue.getClass().getName());
				fieldValue.getBinding().objectToEntry(fieldValue, out);
			}
		}
	}
}
