package nl.tudelft.rdfgears.tests.sparql;

import junit.framework.TestCase;
import nl.tudelft.rdfgears.engine.WorkflowLoader;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.rgl.workflow.ConstantProcessor;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.Workflow;
import nl.tudelft.rdfgears.tests.Data;

import org.junit.Before;
import org.junit.Test;

public class TestRemoteQueries extends TestCase {

	@Before 
	public void initialize() {
		/* doesn't run */
		assertTrue("this isn't checked, oddly enough", false);
	}

	@Test 
    public void testConstruct() throws WorkflowLoadingException {
		nl.tudelft.rdfgears.Test.enableTestConfig();

		
    	ConstantProcessor graphProc = new ConstantProcessor(Data.getGraphFromFile("./data/dbpedia_incomplete.xml"));
    	Workflow workflow = WorkflowLoader.loadWorkflow("tests/localConstruct");    	
    	FunctionProcessor workflowProc = new FunctionProcessor(workflow);
    	workflowProc.getPort("graph").setInputProcessor(graphProc);
    	assertTrue("should have given number of values", workflowProc.getResultValue().asGraph().getModel().size()==965);
	}
	

	@Test 
    public void testSelect() throws WorkflowLoadingException {
		nl.tudelft.rdfgears.Test.enableTestConfig();
    	
    	Workflow workflow = WorkflowLoader.loadWorkflow("tests/remoteSelect");    	
    	FunctionProcessor workflowProc = new FunctionProcessor(workflow);
    	
    	//assertTrue("Should be director of 5 movies ", workflowProc.getResultValue().asBag().size()==5);
    	boolean gotIt = false; // find a pre-known movie
    	for (RGLValue rec : workflowProc.getResultValue().asBag()){
    		String uri = rec.asRecord().get("mov").asURI().uriString();
    		
    		if ("http://dbpedia.org/resource/Eraserhead".equals(uri)){
    			gotIt = true;
    		}
    	}
    	assertTrue("should have found Eraserhead", gotIt);
	}

	@Test 
    public void testSelectWithError() throws WorkflowLoadingException {
		nl.tudelft.rdfgears.Test.enableTestConfig();
    	
    	Workflow workflow = WorkflowLoader.loadWorkflow("tests/remoteSelectError");    	
    	FunctionProcessor workflowProc = new FunctionProcessor(workflow);
    	
    	//assertTrue("Should be director of 5 movies ", workflowProc.getResultValue().asBag().size()==5);
    	boolean gotIt = false; // find a pre-known movie
    	try { 
    		workflowProc.getResultValue().asBag();
    		assertTrue(false); // execution should fail as the BIND in the query is not supported at the time. 
    	}
    	catch (Exception e){
    		//System.out.println("caught exception; "+e);
    		assertTrue("Should give an error on the BIND keyword", e.getMessage().contains("BIND"));
    	}
    	
	}
}
