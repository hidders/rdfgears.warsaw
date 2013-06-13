package nl.tudelft.rdfgears.engine.bindings.idvalues;

import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRenewablyIterableBag;

public class IdBagBinding extends IdRGLBinding {

	@Override
	protected IdRGLValue createIdValue(long id) {
		return new IdRenewablyIterableBag(id);
	}	
	
}
