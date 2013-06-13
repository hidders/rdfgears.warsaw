package nl.tudelft.rdfgears.rgl.datamodel.value;

import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;

import com.hp.hpl.jena.datatypes.RDFDatatype;

/**
 * Abstract class for LiteralValue. Can be MemoryLiteralValue, although a RDB-based version 
 * may exists. 
 * 
 * @author Eric Feliksik
 *
 */
public abstract class LiteralValue extends RDFValue {
	
	public void accept(RGLValueVisitor visitor){
		visitor.visit(this);
	}
	
	@Override
	public boolean isLiteral(){
		return true;
	}
	
	public LiteralValue asLiteral(){
		return this;
	}
	
	public String toString(){
		return "<toString() not implemented for LiteralValue, please override in subclass>"; 
	}
	

	/**
	 * Get the type object for this literal. Return null if it is a plain (i.e. untyped) literal 
	 * @return
	 */
	public abstract RDFDatatype getLiteralType();

	/**
	 * Get the language tag for this literal. Returns null if the literal has no such tag, or
	 * if it is a typed literal  
	 * @return
	 */
	public abstract String getLanguageTag();
	
	/*****************************************************************************************
	 * 
	 * Special Literal functions below
	 * 
	 * We can add getValueDate(), getValueInt(), etc.
	 * 
	 ****************************************************************************************/
	
	/**
	 * Get a double representation of this value, if possible
	 * @return
	 */
	public abstract double getValueDouble();
	
	/**
	 * Get the String representation of this value (without language/type) 
	 * @return
	 */
	public abstract String getValueString();
	

	@Override
	public void prepareForMultipleReadings() {
		/* nothing to do */
	}

	
	
}
