package nl.tudelft.rdfgears.rgl.function.imreal.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * Weighted Interests vocabulary class for namespace
 * http://purl.org/ontology/wi/core#
 */
public class WI {

	protected static final String uri = "http://purl.org/ontology/wi/core#";

	/**
	 * returns the URI for this schema
	 * 
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	private static Model m = ModelFactory.createDefaultModel();

	public static final Property topic = m.createProperty(uri, "topic");
}
