package nl.tudelft.rdfgears.rgl.datamodel.value.ifaces;

import java.util.Iterator;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

public interface AbstractBagValue extends
		Iterable<RGLValue>, RGLValue {

	public abstract Iterator<RGLValue> iterator();

	/**
	 * Get the size of this bag. Note that this MAY (re)iterate the bag, which
	 * MAY be expensive if it is not Materialized.
	 * 
	 * @return
	 */
	public abstract int size();

}
