package nl.tudelft.rdfgears.rgl.function.sparql;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * Interface for implementation of SPARQL function. 
 * 
 * It is assumed to be used *within* a SPARQLFunction class. 
 * The reason it can't be a subclass is that the correct Sparql Behavior depends on the 
 * runtime configuration of the SPARQLFunction (i.e., the configured query). 
 * 
 * @author Eric Feliksik
 *
 */
public interface SparqlBehavior {

	public RGLType getOutputType();

	public RGLValue simpleExecute(ValueRow inputRow);
	
}
