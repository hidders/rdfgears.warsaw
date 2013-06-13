package nl.tudelft.rdfgears.rgl.function.imreal.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/** Weighting Ontology vocabulary class for namespace http://purl.org/ontology/wo/core# */
public class WO {

	protected static final String uri = "http://purl.org/ontology/wo/core#";

	/**
	 * returns the URI for this schema
	 * 
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	private static Model m = ModelFactory.createDefaultModel();

	public static final Resource Weight = m.createResource(uri
			+ "Weight");
	public static final Property weight = m.createProperty(uri, "weight");
	public static final Property weight_value = m.createProperty(uri, "weight_value");
	public static final Property scale = m.createProperty(uri, "scale");
}
