package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

public class MemoryValueManager extends AbstractValueManager {

	@Override
	public void registerValue(RGLValue value) {
		registerMemoryValue(value);
	}

	@Override
	public RGLValue fetchValue(long id) {
		return getMemoryValue(id);
	}

	@Override
	public void updateValue(RGLValue value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		
	}

}
