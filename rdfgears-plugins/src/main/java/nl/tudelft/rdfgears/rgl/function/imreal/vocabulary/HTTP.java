package nl.tudelft.rdfgears.rgl.function.imreal.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/** HTTP vocabulary class for namespace http://www.w3.org/2011/http# */
public class HTTP {

	protected static final String uri = "http://www.w3.org/2011/http#";

	/**
	 * returns the URI for this schema
	 * 
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	private static Model m = ModelFactory.createDefaultModel();

	public static final Resource Connection = m.createResource(uri
			+ "Connection");
	
	public static final Property absolutePath = m.createProperty(uri, "absolutePath");
	public static final Property request = m.createProperty(uri, "Request");
	public static final Property requests = m.createProperty(uri, "Requests");
	
}
