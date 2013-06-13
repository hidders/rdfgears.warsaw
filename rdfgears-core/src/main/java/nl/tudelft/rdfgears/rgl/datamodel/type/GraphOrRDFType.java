package nl.tudelft.rdfgears.rgl.datamodel.type;

public class GraphOrRDFType extends RGLType {

	@Override
	public boolean isType(RGLType type) {
		return (type.getClass()==this.getClass());
	}

	@Override
	public boolean isSupertypeOf(RGLType otherType) {
		return (otherType instanceof GraphOrRDFType);
	}

}
