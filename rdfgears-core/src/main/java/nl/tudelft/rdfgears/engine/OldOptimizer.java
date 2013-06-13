package nl.tudelft.rdfgears.engine;

import java.util.HashSet;
import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.standard.FilterFunction;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.InputPort;
import nl.tudelft.rdfgears.rgl.workflow.Workflow;
import nl.tudelft.rdfgears.rgl.workflow.WorkflowInputPort;
import nl.tudelft.rdfgears.rgl.workflow.WorkflowNode;



public class OldOptimizer {
	
	private Set<WorkflowNode> optimizedNodes = new HashSet<WorkflowNode>();
	
	
	/**
	 * Optimize the workflow, disabling materialization where possible. 
	 * Disable cache in outputNode if cacheOutputNode==false. If user wants to only print the result value, we need not 
	 * keep the calculated intermediate values in some cache. 
	 * @param workflow
	 * @return
	 * @throws WorkflowCheckingException
	 * @throws TypeCheckingNotSupportedException 
	 */
	public Workflow optimize(Workflow workflow, boolean cacheOutputNode) throws WorkflowCheckingException {
		
		WorkflowNode endNode = workflow.getOutputProcessor();
		optimizeFlow(endNode);
		
		if (!cacheOutputNode){
			/* do NOT materialize results of output node */
			if(endNode instanceof FunctionProcessor){
				FunctionProcessor fproc = (FunctionProcessor) endNode;
				fproc.valueIsReadMultipleTimes(false);
			}
		}
		
		return workflow; 
	}
	
	
	
				
		/**
		 * 
		 * Recursively follow nodes, flagging them to NOT use cache if it isn't needed. 
		 *                                                                                          
		 *                        ---.                                                               
		 *                            \                                                              
		 *        +-------------+      \    +-------------+                                      
		 *        |    proc A   |       \   |   proc B    |                                       
		 *       (?)            |        --( )            |                                       
		 *        |             |           |            ( )----                                       
		 *       (?)            o----------(?)B_a         |                          
		 *        |             |           |             |                                       
		 *        +-------------+           +-------------+                                       
		 *                                                                                          
		 *                                                                                          
		 *  Assume A outputs a bag, which we can detect by typechecking. A may or may not iterate over its inputs, 
		 *  and B may or may not iterate over the output of A. 
		 *  
		 *  The output bag of A need not be materialize/cached if: 
		 *  - It is read by only one processor (in this case proc B), AND 
		 *  - The reading processor iterates at most once over the bag. That is, one or more of the following conditions hold:
		 *     I) the output of A is read in port B_a and port port B_A is marked for iteration, as only port of proc B. 
		 *         (If there is multiple ports marked in B, Cartesian product is taken and iterates many times -- unless we 
		 *         select one port in the outer loop, and we can still guarantee iterate it only once. But this is advanced 
		 *         optimization we won't do now. Notice that if we would implement 
		 *         this, it would be advantageous to put the bag we suspect to be biggest in the outer loop, and then the 
		 *         biggest after that directly on, and the smallest in the innermost loop. We would only prevent the outermost 
		 *         loop-bag from needing cache, but we want the smallest loop inside to make it fit in memory-cache 
		 *         (like with nested loop join optimization))
		 *     II) the port is not marked for iteration, but can otherwise guarantee that the function of the processor 
		 *         will iterate at most once, and it will not output the input bag in (an element of) the output of B. 
		 *         This is e.g. the case with the Filter function. 
		 *         
		 * This algorithm does not exceed the workflow boundary. That is, if a workflow has no iterating ports, 
		 * we may just unpack the workflow in the containing one. But we don't detect this. So we optimize only locally. 
		 * @throws WorkflowCheckingException 
		 */
	private void optimizeFlow(WorkflowNode nodeA) throws WorkflowCheckingException{

		if(! optimizedNodes.contains(nodeA)){
			if (nodeA instanceof FunctionProcessor){

				FunctionProcessor procA = (FunctionProcessor) nodeA;
				RGLType outputType = procA.getOutputType();
				
				/**
				 * This if if/else/else implementation looks funky because caching is currently opt-out, not opt-in.
				 * 
				 * But it works, and is very effective!
				 */
				if (! (outputType instanceof BagType)){
					/* Now caching is one by default; leave it on.  
					 * The processor still may not *need* caching, but if it isn't a bagType we don't 
					 * care much.
					 */
				} else // we are a bag 
					if (procA.getOutputReaders()==null){
					/* *** DO CACHE ***  
					 * This is the output port of workflow and we didn't implement a method 
					 * do diagnose how our output is used. So if we output a bag, we will cache it. */
					// System.out.println(">>>> USING cache for function "+procA.getFunction() + " because it is last in workflow");
						
				} else // we are not last processor in workflow   
					if (procA.getOutputReaders().size()!=1){
					/* *** DO CACHE ***  
					 * There are 0 readers (then it will not be generated) or multiple readers (which may all iterate, so we 
					 * need cache) 
					 */ 
						// System.out.println(">>>> USING cache for function "+procA.getFunction() + " because it has "+procA.getOutputReaders().size()+ " outputs. ");
				} else // there is 1 reader
					if (! readerIteratesMaximallyOnce(procA.getOutputReaders().iterator().next())){
					/* *** DO CACHE ***  
					 * it is iterated multiple times, so it needs cache. */
						// System.out.println(">>>> USING cache for function "+procA.getFunction() + " because its reader may iterate many times ");

				} else {
					// System.out.println("Disabling cache for function "+procA.getFunction());
					procA.valueIsReadMultipleTimes(false);
				}
				
				for (InputPort readingPort : procA.getPortSet()){
					optimizeFlow(readingPort.getInputProcessor());
				}
				
				RGLFunction function = procA.getFunction();
				if (function instanceof Workflow){
					(new OldOptimizer()).optimize((Workflow) function, true);
				}
				
				
			} else if (nodeA instanceof WorkflowInputPort){
				/**
				 * This thing itself has no cache. More advanced optimization may try to analyze the relation
				 * between processors in different nested workflows, but we don't . 
				 */
								
			}
			
		}
	}	
	
	/**
	 * Return true iff the port will iterate maximally once over the input bag.
	 * This is true if it is the only iterating port, or if we know the function won't iterate more than once AND will
	 * not return the bag so no-one else can iterate. 
	 * 
	 *  We indeed assume port receives an input bag. 
	 * @param nodeB_port
	 * @return
	 */
	private boolean readerIteratesMaximallyOnce(InputPort nodeB_port) {
		if (! nodeB_port.iterates()){
			return functionWontIterateTwice(nodeB_port.getOwnerProcessor().getFunction());
		} 
		else {
			for (InputPort bPort : nodeB_port.getOwnerProcessor().getPortSet()){
				if (bPort!=nodeB_port && bPort.iterates()){
					return false; // multiple iterating ports, so will take cartesian product
				}
			}
			return true;	
		}
		
	}

	/**
	 * It may be nice to actually delegate this determination to the function itself, by means of a 
	 * function.functionWontIterateTwice(String portName). It can return false by default so implementation
	 * is not compulsory, and some methods like FilterFunction can return true. 
	 * @param function
	 * @return
	 */
	private boolean functionWontIterateTwice(RGLFunction function) {
		return (function instanceof FilterFunction); 
	}
}
