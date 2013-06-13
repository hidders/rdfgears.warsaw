package nl.tudelft.rdfgears.engine.bindings.idvalues;

import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;

public class IdModifiableRecordBinding extends IdRGLBinding {

	@Override
	protected IdRGLValue createIdValue(long id) {
		return new IdModifiableRecord(id);
	}
	
}
