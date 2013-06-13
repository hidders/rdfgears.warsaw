package nl.tudelft.rdfgears.rgl.datamodel.type;

import java.io.Serializable;

/**
 * TODO: It is probably nicer to *NOT* check the types by doing 'someType instanceof BagType' 
 * but rather by using some functions .isBagType(), .isRecordType(), etc.
 * 
 * @author Eric Feliksik
 *
 */
public abstract class RGLType implements Serializable {
	public abstract boolean isType(RGLType type);
	public abstract boolean isSupertypeOf(RGLType otherType);
	
	public boolean isSubtypeOf(RGLType otherType) {
		if (otherType==null)
			return false;
		return otherType.isSupertypeOf(this);
	}
	
	
	public boolean isBagType(){
		return false;
	}
	public boolean isRecordType(){
		return false;
	}

	public boolean isGraphType(){
		return false;
	}

	public boolean isRDFValueType(){
		return false;
	}

	public boolean isBooleanType(){
		return false;
	}

	
}
