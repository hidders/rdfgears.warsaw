package nl.tudelft.rdfgears.rgl.datamodel.value.ifaces;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

public interface AbstractModifiableRecord extends AbstractRecordValue {
	public void put(String fieldName, RGLValue element);
}
