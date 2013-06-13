package nl.tudelft.rdfgears.rgl.function.imreal.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/** USEM vocabulary class for namespace http://wis.ewi.tudelft.nl/rdf/usem.owl# */
public class USEM {

	protected static final String uri = "http://wis.ewi.tudelft.nl/rdf/usem.owl#";

	/**
	 * returns the URI for this schema
	 * 
	 * @return the URI for this schema
	 */
	public static String getURI() {
		return uri;
	}

	private static Model m = ModelFactory.createDefaultModel();

	public static final Resource WeightedKnowledge = m.createResource(uri
			+ "WeightedKnowledge");
	public static final Resource DefaultScale = m.createResource(uri
			+ "DefaultScale");
	public static final Property knowledge = m.createProperty(uri, "knowledge");
}
