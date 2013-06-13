package nl.tudelft.rdfgears.rgl.datamodel.type;

public class BagType extends RGLType {

	private RGLType elemType = null;
	private BagType(RGLType elemType){
		assert(elemType!=null);
		this.elemType = elemType;
	}
	/**
	 * 
	 * Singleton to allow returning the same RecordType for the same row type, if we may want this later
	 * Now it's not an effective singleton... 
	 */
	public static synchronized BagType getInstance(RGLType elemType){
		return new BagType(elemType);
	}
	
	public RGLType getElemType(){
		return this.elemType;
	}

	public boolean equals(Object that){
		if (that instanceof BagType){
			BagType thatBag = ((BagType)that);
			return this.getElemType().equals(thatBag.getElemType());
		}
		return false;
	}
	@Override
	public boolean isType(RGLType otherType) {
		if (otherType instanceof BagType){
			BagType otherBagType = (BagType) otherType;
			return this.getElemType().isType(otherBagType.getElemType());
		}
		return false;
	}
	@Override
	public boolean isSupertypeOf(RGLType otherType) {
			if (otherType instanceof BagType){
				BagType otherBagType = (BagType) otherType;
				return this.getElemType().isSupertypeOf(otherBagType.getElemType());
			}
			return false;
	}
	
	@Override
	public boolean isBagType(){
		return true;
	}
	

	public String toString(){
		// types are not SO deeply nested, so no need to write to pass a strinbuilder. 
		return "Bag( "+elemType + " )";
	}
}
