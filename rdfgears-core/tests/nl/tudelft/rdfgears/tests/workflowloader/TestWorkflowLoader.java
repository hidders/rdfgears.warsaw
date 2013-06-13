package nl.tudelft.rdfgears.tests.workflowloader;

	

import java.text.ParseException;

import junit.framework.TestCase;
import nl.tudelft.rdfgears.engine.WorkflowLoader;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.util.ValueParser;

import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;


public class TestWorkflowLoader extends TestCase {
	
	/**
	 * Test the literal parsing mechanism 
	 * @throws WorkflowLoadingException 
	 */
	@Test 
	public void testCreateSimpleValueByParsing() throws WorkflowLoadingException{
		nl.tudelft.rdfgears.Test.enableTestConfig();
		
		Literal plainLit;
		try {
			plainLit = ValueParser.parseNTripleValue("\"apple\"").getRDFNode().asLiteral();
			assertTrue(plainLit.getLanguage().equals(""));
			assertTrue(plainLit.getDatatypeURI()==null);
	
			Literal langLit = ValueParser.parseNTripleValue("\"apple\"@en").getRDFNode().asLiteral();
			assertTrue("language is '"+langLit.getLanguage()+"' but should have been 'en'", langLit.getLanguage().equals("en"));
			assertTrue(plainLit.getDatatypeURI()==null);
			
			Literal typedLit = ValueParser.parseNTripleValue("\"1.2\"^^<"+XSDDatatype.XSDdouble.getURI()+">").getRDFNode().asLiteral();
			assertTrue(typedLit.getLanguage().equals(""));
			assertTrue(typedLit.getDatatypeURI().equals(XSDDatatype.XSDdouble.getURI()));
		} catch (ParseException e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		
		
	}
}
