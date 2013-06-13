package nl.tudelft.rdfgears.tests;

import junit.framework.TestCase;
import nl.tudelft.rdfgears.engine.WorkflowLoader;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.ValueSerializerInformal;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.Workflow;
import nl.tudelft.rdfgears.util.row.TypeRow;

import org.junit.Before;
import org.junit.Test;

public class TestWorkflow extends TestCase {
	
	@Before public void initialize() {
		/* doesn't run */
		assertTrue("this isn't checked, oddly enough", false);
	}

	@Test 
    public void testWorkflowLoader() throws WorkflowLoadingException {
		nl.tudelft.rdfgears.Test.enableTestConfig();
    	
    	Workflow workflow = WorkflowLoader.loadWorkflow("tests/silkExOk/queries");
		//Workflow workflow = (new WorkflowLoader("silkEx/queries2")).getWorkflow();
    	/**
    	 * Check type directly on workflow
    	 */
    	try {
			RGLType outputType = workflow.getOutputType(new TypeRow());
			assertTrue("output should be a bag", outputType instanceof BagType);
		} catch (WorkflowCheckingException e) {
			
			assertTrue("Exception thrown: "+e.getMessage()+" ", false);
		}
		
		/**
		 * wrap workflow in outputprocessor and check type
		 */
		ValueSerializerInformal serializer = new ValueSerializerInformal();
		
		
		RGLValue workflowResultValue = workflow.getOutputProcessor().getResultValue();
		System.out.println("");
		System.out.println("================ workflow result silkExOk/queries: ");
		System.out.flush();
		serializer.serialize(workflowResultValue);
		
		int size = workflowResultValue.asBag().size();
		assertTrue("should have size 4, but size is "+size, size==4);
		
		FunctionProcessor fproc = new FunctionProcessor(workflow, "root");
		
		try {
			fproc.getOutputType();
		} catch (WorkflowCheckingException e) {
			assertTrue("ERROR: The workflow is not executable, as it is not well-typed: "+e.getMessage(), false);
		}
		
		RGLValue procResultValue = fproc.getResultValue();
		
		//assertTrue("values should be the same, because we are not iterating", procResultValue==workflowResultValue);

		
		System.out.println("");
		System.out.println("================ proc result: ");
		System.out.flush();
		serializer.serialize(procResultValue);
		
		size = procResultValue.asBag().size();
		assertTrue("should have size 4, but is "+size, size==4);
		assertTrue("Should be (Record[lmdb:Record[dir_name:String]])", procResultValue.asBag().iterator().next().asRecord().get("lmdb").asRecord().get("dir_name").isLiteral());		
	} 
	
	@Test 
    public void testMistypedIteration() throws WorkflowLoadingException {
		Workflow workflow = WorkflowLoader.loadWorkflow("tests/silkTypingError/queries");
		
    	/**
    	 * Check type directly on workflow
    	 */
    	try {
			RGLType outputType = workflow.getOutputType(new TypeRow());
			assertTrue("should have thrown typing error", false);
		} catch (WorkflowCheckingException e) {
			/* ok */
		}
	}
    
}
