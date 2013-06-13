package nl.tudelft.rdfgears.engine.bindings.idvalues;

import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRecordValue;

public class IdRecordBinding extends IdRGLBinding {

	@Override
	protected IdRGLValue createIdValue(long id) {
		return new IdRecordValue(id);
	}

}
