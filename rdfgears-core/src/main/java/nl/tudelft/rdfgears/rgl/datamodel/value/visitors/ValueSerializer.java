package nl.tudelft.rdfgears.rgl.datamodel.value.visitors;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;


/**
 * A visitor that serializes an RGL Value, writing it to an Output Stream. 
 * 
 * @author Eric Feliksik
 *
 */
public abstract class ValueSerializer implements RGLValueVisitor {
	
	public abstract void serialize(RGLValue value);
	


}
