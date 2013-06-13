package nl.tudelft.rdfgears.rgl.exception;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

/**
 * Thrown when comparison is not defined between two RGL objects
 * 
 * @author af09017
 *
 */
public class ComparisonNotDefinedException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ComparisonNotDefinedException(RGLValue v1, RGLValue v2) {
		super("Comparison between the classes "+v1.getClass().getSimpleName()+" and "+v2.getClass().getSimpleName()+" is not defined/implemented");
	}
	
}
