package nl.tudelft.rdfgears.tests.core;

import junit.framework.TestCase;
import nl.tudelft.rdfgears.engine.WorkflowLoader;
import nl.tudelft.rdfgears.engine.bindings.BindingsTest;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.LRUValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.ValueSerializerInformal;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.Workflow;
import nl.tudelft.rdfgears.util.row.TypeRow;

import org.junit.Test;

public class TestCoreFunctions extends TestCase {

	/**
	 * Test a workflow combining all bag operations
	 * @throws WorkflowLoadingException 
	 */
	@Test 
    public void testBagAll() throws WorkflowLoadingException {
		nl.tudelft.rdfgears.Test.enableTestConfig();

    	Workflow workflow = WorkflowLoader.loadWorkflow("tests/operators/bagAll");
    	
    	/**
    	 * Check type directly on workflow
    	 */
    	try {
			RGLType outputType = workflow.getOutputType(new TypeRow());
			assertTrue("output should be a bag", outputType instanceof RecordType);
			
		} catch (WorkflowCheckingException e) {
			
			assertTrue("Exception thrown: "+e.getMessage()+" ", false);
		}
		
		/**
		 * wrap workflow in outputprocessor and check type
		 */
		ValueSerializerInformal serializer = new ValueSerializerInformal();
		
		
		RGLValue workflowResultValue = workflow.getOutputProcessor().getResultValue();
		System.out.println("");
		System.out.println("================ workflow result: ");
		System.out.flush();
		serializer.serialize(workflowResultValue);
		assertTrue("should have size 4", workflowResultValue.asRecord().get("flattened").asBag().size()==2);
		assertTrue("should have size 4", workflowResultValue.asRecord().get("union").asBag().size()==2);
		
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
		assertTrue("result can never be null", procResultValue!=null);
		serializer.serialize(procResultValue);
		
		
		
	}

	/**
	 * Test a workflow combining all bag operations
	 * @throws WorkflowLoadingException 
	 */
	@Test 
    public void testBagCategorizer() throws WorkflowLoadingException {
		nl.tudelft.rdfgears.Test.enableTestConfig();
		
		/***** test workflow: 
		  

<?xml version="1.0" encoding="UTF-8"?>
<rdfgears>
   <metadata>
      <id>tests/operators/bagCategorizer</id>
      <name/>
      <password/>
   </metadata>
   <workflow>
      <workflowInputList x="33" y="27">
         <workflowInputPort name="input1"/>
      </workflowInputList>
      <network output="node_4" x="394" y="119">
         <processor id="node_4" x="474" y="200">
            <function type="bag-categorize">
               <config param="categorizeFunction">rdfgears.rgl.workflow.function.standard.Identity</config>
               <config param="categories">duplicates;strings;a;doesnt_occur;</config>
            </function>
            <inputPort iterate="false" name="bag">
               <source processor="node_5"/>
            </inputPort>
         </processor>
         <processor id="node_5" x="286" y="256">
            <function type="custom-java">
               <config param="implementation">nl.feliksik.rdfgears.SplitString</config>
            </function>
            <inputPort iterate="false" name="string">
               <source processor="node_3"/>
            </inputPort>
            <inputPort iterate="false" name="delimiter">
               <source processor="node_8"/>
            </inputPort>
         </processor>
         <processor id="node_3" x="84" y="279">
            <function type="constant">
               <config param="value">"this;is;a;colon;separated;list;of;strings;with;duplicates;it;is;composed;of;strings;with;duplicates;many;duplicates"</config>
            </function>
         </processor>
         <processor id="node_8" x="84" y="348">
            <function type="constant">
               <config param="value">";"</config>
            </function>
         </processor>
      </network>
   </workflow>
</rdfgears>

 */
		
    	Workflow workflow = WorkflowLoader.loadWorkflow("tests/operators/bagCategorizer");
		
    	/**
    	 * Check type directly on workflow
    	 */
    	try {
			RGLType outputType = workflow.getOutputType(new TypeRow());
			assertTrue("output should be a bag", outputType instanceof RecordType);
		} catch (WorkflowCheckingException e) {
			
			assertTrue("Exception thrown: "+e.getMessage()+" ", false);
		}
		
		/**
		 * wrap workflow in outputprocessor and check type
		 */
		ValueSerializerInformal serializer = new ValueSerializerInformal();
		
		
		/**
		 * words are categorized by themselves ;-) that is, for every word a category (and thus a record field) is 
		 * created containing those words. 
		 */

		RGLValue workflowResultValue = workflow.getOutputProcessor().getResultValue();
		
		serializer.serialize(workflowResultValue);
		
		
//		RGLValue foo = BindingsTest.thereAndBack(workflowResultValue.asRecord().get("duplicates").asBag());
//		ValueInflator.registerValue(foo);
//		AbstractBagValue bag = foo.asBag();
		
		assertTrue("should be a record with bags ", workflowResultValue.isRecord());
		assertTrue("should have 3 times the word 'duplicate'", workflowResultValue.asRecord().get("duplicates").asBag().size()==3);
//		assertTrue("should have 3 times the word 'duplicate', but have it  ", bag.size()==3);
		assertTrue("should have 2 times the word 'strings'", workflowResultValue.asRecord().get("strings").asBag().size()==2);
		assertTrue("should have 1 times the word 'a'", workflowResultValue.asRecord().get("a").asBag().size()==1);
		assertTrue("should have 0 times the word 'doesnt_occur'", workflowResultValue.asRecord().get("doesnt_occur").asBag().size()==0);

		
	}

}
