package nl.tudelft.rdfgears.rgl.datamodel.value;

import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;
import nl.tudelft.rdfgears.rgl.exception.ComparisonNotDefinedException;

public abstract class BooleanValue extends DeterminedRGLValue {
	// extends BagValue  { // no this may be theoretically interesting in NRC but not in implementation

	@Override
	public String toString() {
		return isTrue() ? "<RGLBoolean:True>" : "<RGLBoolean:False>" ;
	}
	
	public BooleanValue asBoolean(){
		return this;
	}
	
	public boolean isBoolean(){
		return true;
	}
	
	public abstract boolean isTrue();

	public void accept(RGLValueVisitor visitor){
		visitor.visit(this);
	}
	

	public int compareTo(RGLValue v2) {
		if (v2.isBoolean()){
			int  myValue = isTrue() ? 1 : 0;
			int  hisValue = v2.asBoolean().isTrue() ? 1 : 0;
			return myValue - hisValue; 
		} else if (v2.isNull()){
			return 1; // boolean is bigger than null
		}
		
		throw new ComparisonNotDefinedException(this, v2);
	}
}
