package nl.tudelft.rdfgears.rgl.function.imreal.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/** IMREAL user vocabulary class for namespace http://imreal-project.eu/rdf/user# */
public class IMREAL {

	protected static final String uri = "http://imreal-project.eu/rdf/user#";

	/**
	 * returns the URI for this schema
	 * 
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	private static Model m = ModelFactory.createDefaultModel();

}
