package nl.tudelft.rdfgears.rgl.datamodel.type;
public class RDFType extends GraphOrRDFType {
	private static RDFType instance = new RDFType();
	private RDFType() {}

	/**
	 * singleton constructor
	 * @return
	 */
	public static synchronized RDFType getInstance(){
		if (instance == null){
			instance = new RDFType();
			assert(instance!=null);
		}

		assert(instance!=null);
		return instance;
	}

	@Override
	public boolean isType(RGLType type) {
		return this.equals(type);
	}

	@Override
	public boolean isSupertypeOf(RGLType otherType) {
		return (otherType instanceof RDFType);
	}

	@Override
	public boolean isRDFValueType(){
		return true;
	}
	
	public String toString(){
		return "RDFValue";
	}

}
