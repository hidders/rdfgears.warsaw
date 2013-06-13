package nl.tudelft.rdfgears.engine;

import com.hp.hpl.jena.rdf.model.Property;

public class JenaRDFConstants {

	public static final String valueBaseURI = "http://rgl_temp/"; // unfortunately we string-concatenate this... 
	public static final String rglOntologyNS = "http://wis.ewi.tudelft.nl/rgl/";
	public static final String recordFieldBaseURI = "http://wis.ewi.tudelft.nl/rgl/field#";
	public static final Property bagElemProp = Engine.getDefaultModel().createProperty(rglOntologyNS, "bagElement"); 
	
	
}
