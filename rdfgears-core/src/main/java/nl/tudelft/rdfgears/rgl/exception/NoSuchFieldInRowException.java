package nl.tudelft.rdfgears.rgl.exception;


/**
 * Thrown when a anknown record-field is indexed
 * 
 * @author af09017
 *
 */
public class NoSuchFieldInRowException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public NoSuchFieldInRowException(String fieldName) {
		super("The fieldName "+ ( fieldName!=null ? "'"+fieldName+"'" : "null" ) +" is not defined for this ValueRow or Record.");
	}
	
}
