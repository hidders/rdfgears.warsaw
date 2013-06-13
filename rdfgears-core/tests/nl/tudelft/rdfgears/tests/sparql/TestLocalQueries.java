package nl.tudelft.rdfgears.tests.sparql;

import junit.framework.TestCase;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.WorkflowLoader;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.rgl.workflow.ConstantProcessor;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.Workflow;
import nl.tudelft.rdfgears.tests.Data;

import org.junit.Before;
import org.junit.Test;

public class TestLocalQueries extends TestCase {

	@Before 
	public void initialize() {
		/* doesn't run */
		assertTrue("this isn't checked, oddly enough", false);
	}

	@Test 
    public void testLocalConstruct() throws WorkflowLoadingException {
		nl.tudelft.rdfgears.Test.enableTestConfig();

		
    	ConstantProcessor graphProc = new ConstantProcessor(Data.getGraphFromFile("./data/dbpedia_incomplete.xml"));
    	Workflow workflow = WorkflowLoader.loadWorkflow("tests/localConstruct");    	
    	FunctionProcessor workflowProc = new FunctionProcessor(workflow);
    	workflowProc.getPort("graph").setInputProcessor(graphProc);
    	assertTrue("should have given number of values", workflowProc.getResultValue().asGraph().getModel().size()==965);
	}

	@Test 
    public void testLocalSelect() throws WorkflowLoadingException {
		nl.tudelft.rdfgears.Test.enableTestConfig();
		
    	ConstantProcessor graphProc = new ConstantProcessor(Data.getGraphFromFile("./data/dbpedia_incomplete.xml"));
    	ConstantProcessor dirProc = new ConstantProcessor(ValueFactory.createURI("http://dbpedia.org/resource/David_Lynch"));
    	
    	Workflow workflow = WorkflowLoader.loadWorkflow("tests/localSelect");    	
    	FunctionProcessor workflowProc = new FunctionProcessor(workflow);
    	workflowProc.getPort("graph").setInputProcessor(graphProc);
    	workflowProc.getPort("diruri").setInputProcessor(dirProc);
    	assertTrue("Should be director of 5 movies ", workflowProc.getResultValue().asBag().size()==5);
	}
}
