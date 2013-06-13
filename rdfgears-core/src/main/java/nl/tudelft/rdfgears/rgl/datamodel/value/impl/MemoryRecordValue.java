package nl.tudelft.rdfgears.rgl.datamodel.value.impl;

import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RecordValue;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * Field values are modifiable!
 * 
 * @author af09017
 * 
 */
public class MemoryRecordValue extends RecordValue {
	private ValueRow row;

	/**
	 * RGL values are readonly, so we cannot modify the row
	 * 
	 * @param row
	 */
	public MemoryRecordValue(ValueRow row) {
		this.row = row;
	}

	public MemoryRecordValue(long id, ValueRow row) {
		myId = id;
		this.row = row;
	}

	protected ValueRow getRow() {
		return this.row;
	}

	@Override
	public RGLValue get(String s) {
		RGLValue v = row.get(s);
		assert (v != null) : "You fetched field '"
				+ s
				+ "' from a record value where the field is not set. If you're working with SPARQL OPTIONALS: sorry, this isn't supported";
		return v;
	}

	public Set<String> getRange() {
		return row.getRange();
	}

}
