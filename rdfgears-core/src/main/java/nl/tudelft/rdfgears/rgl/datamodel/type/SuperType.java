package nl.tudelft.rdfgears.rgl.datamodel.type;

import nl.tudelft.rdfgears.engine.Engine;

/**
 * A type that could be any type
 * 
 * @author Eric Feliksik
 *
 */
public class SuperType extends RGLType {
	public boolean isType(RGLType type){
		return this.equals(type);
	}
	
	/**
	 * The SuperType class is supertype of every other type that is not a SuperType, i.e. 
	 * of every concrete type
	 */
	@Override
	public boolean isSupertypeOf(RGLType otherType) {
		if (otherType instanceof SuperType){
			/* we cannot assume that thatType is a subtype of this type */
			Engine.getLogger().warn("We do NOT assume that one SuperType is the supertype of the other. ");
			return false;
		}
		
		if (otherType==null)
			return false;
		
		return true;
	}
	
	public String toString(){
		// types are not SO deeply nested, so no need to write to pass a stringbuilder. 
		return "AnyType";
	}
}
