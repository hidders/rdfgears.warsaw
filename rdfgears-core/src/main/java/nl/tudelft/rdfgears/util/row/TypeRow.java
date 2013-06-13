package nl.tudelft.rdfgears.util.row;

import java.io.Serializable;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;




public class TypeRow extends HashRow<RGLType> implements Serializable {
	
	/**
	 * this TypeRow is a supertype of otherTypeRow if every field in this TypeRow exists 
	 * in otherTypeRow, and contains a supertype of the equivalent field in the otherTypeRow. 
	 * 
	 * If the otherTypeRow contains more fields than required, we don't mind.  
	 */
	public boolean isSupertypeOf(TypeRow otherTypeRow) {
		for (String thisField: getRange()){
			if (! get(thisField).isSupertypeOf(otherTypeRow.get(thisField))){
				return false;
			}
		}
		/* all rows are supertype of the equivalent row in otherType. */
		return true;
	}
	

	public boolean isSubtypeOf(TypeRow otherType) {
		if (otherType==null)
			return false;
		return otherType.isSupertypeOf(this);
	}

}
