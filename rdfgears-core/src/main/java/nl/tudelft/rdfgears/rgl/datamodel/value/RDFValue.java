package nl.tudelft.rdfgears.rgl.datamodel.value;

import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;

public abstract class RDFValue extends DeterminedRGLValue  {
	
	
	public static RGLType type = RDFType.getInstance();
	
	

	@Override
	public boolean isRDFValue(){
		return true;
	}
	
	@Override
	public RDFValue asRDFValue(){
		return this;
	}
	

	@Override
	public void prepareForMultipleReadings() {
		/* nothing to do */
	}

	
}
