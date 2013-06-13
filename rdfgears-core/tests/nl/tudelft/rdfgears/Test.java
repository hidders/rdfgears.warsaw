package nl.tudelft.rdfgears;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.bindings.BindingsTest;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.tests.TestLazy;
import nl.tudelft.rdfgears.tests.TestProcessorNetwork;
import nl.tudelft.rdfgears.tests.TestRGLFunctions;
import nl.tudelft.rdfgears.tests.TestTypes;
import nl.tudelft.rdfgears.tests.TestWorkflow;
import nl.tudelft.rdfgears.tests.TestWorkflowManuallyBuilt;
import nl.tudelft.rdfgears.tests.core.TestCoreFunctions;
import nl.tudelft.rdfgears.tests.sparql.TestLocalQueries;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses ({
	  TestTypes.class,
	  TestRGLFunctions.class,
	  TestProcessorNetwork.class,
	  TestWorkflowManuallyBuilt.class,
	  TestWorkflow.class,
	  TestLocalQueries.class,
	  TestCoreFunctions.class,
	  TestLazy.class,
	  BindingsTest.class
})

public class Test {
	public static boolean isWellTyped(FunctionProcessor proc){
		try {
			proc.getOutputType();
			return true;
		} catch (WorkflowCheckingException e) {
			return false;
		}
	}
	
	public static void enableTestConfig(){
		Engine.init(null);
		Engine.getConfig().configurePath("./tests/workflows/"); // i actually want to do this on initialize() but it isn't run :-s
	}
}



