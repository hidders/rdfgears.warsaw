package nl.tudelft.rdfgears.rgl.datamodel.type;


public class BooleanType extends RGLType {
	private static BooleanType instance = new BooleanType();
	private BooleanType(){
	}
	
	public static synchronized BooleanType getInstance(){
		return instance;
	}

	@Override
	public boolean isType(RGLType type) {
		return (type instanceof BooleanType);
	}

	@Override
	public boolean isSupertypeOf(RGLType otherType) {
		return isType(otherType);
	}
	
	public String toString(){
		return "Boolean";
	}
	
	@Override
	public boolean isBooleanType(){
		return true;
	}
	
}
