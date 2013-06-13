package nl.tudelft.rdfgears.tests;

import static org.junit.Assert.assertTrue;
import nl.tudelft.rdfgears.engine.WorkflowLoader;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.util.row.SingleElementValueRow;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Test created when a lazy evaluation bug was detected. Somewhere, laziness affected records.
 * @author Eric Feliksik
 *
 */
public class TestLazy {
	
	@Test
	public void testFoo() throws WorkflowLoadingException {
		
		nl.tudelft.rdfgears.Test.enableTestConfig();
		
		RGLValue res = WorkflowLoader.loadWorkflow("tests/various/tests10dir").execute(
			new SingleElementValueRow("graph", Data.getGraphFromFile("./data/lmdb-10directors.xml"))
		);
		assertTrue("should be a graph", res.isGraph());

		Resource s = ResourceFactory.createResource("http://data.linkedmdb.org/resource/director/9");
		Property p = ResourceFactory.createProperty("http://data.linkedmdb.org/resource/movie/director_name");
		RDFNode o = ResourceFactory.createPlainLiteral("Max Reinhardt");
		
		assertTrue("did not correctly preserve records", res.asGraph().getModel().contains(s,p,o));
		
		
	}

}