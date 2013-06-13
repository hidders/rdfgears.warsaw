package nl.tudelft.rdfgears.rgl.datamodel.type;

public class GraphType extends GraphOrRDFType {
	private static GraphType instance = new GraphType();
	private GraphType() {}

	/**
	 * singleton constructor
	 * @return
	 */
	public static synchronized GraphType getInstance(){
		if (instance == null){
			instance = new GraphType();
			assert(instance!=null);
		}

		assert(instance!=null);
		return instance;
	}

	@Override
	public boolean equals(Object that) {
		return this==that; // we can only have a singleton instance
	}

	@Override
	public boolean isType(RGLType type) {
		return this.equals(type);
	}

	@Override
	public boolean isSupertypeOf(RGLType otherType) {
		return (otherType instanceof GraphType); 
	}
	
	@Override
	public boolean isGraphType(){
		return true;
	}


	public String toString(){
		// types are not SO deeply nested, so no need to write to pass a strinbuilder. 
		return "Graph";
	}
	
	
}
