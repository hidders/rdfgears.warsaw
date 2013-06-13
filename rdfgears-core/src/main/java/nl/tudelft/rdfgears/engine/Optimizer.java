package nl.tudelft.rdfgears.engine;

import java.util.HashSet;
import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.core.BagFlatten;
import nl.tudelft.rdfgears.rgl.function.core.BagGroup;
import nl.tudelft.rdfgears.rgl.function.core.BagUnion;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize;
import nl.tudelft.rdfgears.rgl.function.standard.FilterFunction;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.InputPort;
import nl.tudelft.rdfgears.rgl.workflow.Workflow;
import nl.tudelft.rdfgears.rgl.workflow.WorkflowNode;


/**
 * For an explanation of what this optimizer does, see Eric's MSc Thesis.
 *  
 * @author Eric Feliksik
 *
 */
public class Optimizer {
	
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
	public Workflow optimize(Workflow workflow, boolean wrappingWorkflowIterates) throws WorkflowCheckingException {
		
		optimizeFlow(workflow.getOutputProcessor(), wrappingWorkflowIterates);
		
		return workflow; 
	}
	
	
	
				
		/**
		 * 
		 * @throws WorkflowCheckingException 
		 */
	private void optimizeFlow(WorkflowNode nodeA, boolean wrappingWorkflowIterates) throws WorkflowCheckingException{

		if(! optimizedNodes.contains(nodeA)){
			//optimizedNodes.add(nodeA);
			if (nodeA instanceof FunctionProcessor){
				FunctionProcessor procA = (FunctionProcessor) nodeA;
				RGLType outputType = procA.getOutputType();
				
				if (outputType instanceof BagType){
					/* may need caching */
					boolean guaranteedToStream = false;
					if (isOutputProcessor(procA)){ /* this is an output processor */
						// in thesis I say:   x OR w
						guaranteedToStream = !wrappingWorkflowIterates; 
					} else {
						/* this is a processor in the middle of the workflow */
						guaranteedToStream = middleProcGuaranteedToStream(procA);
					}
					
					/* determine whether we are guaranteed to iterate/stream at most once over the output of procA ) */ 
					if (guaranteedToStream) {
						Engine.getLogger().debug("bags output by proc "+procA.getId()+" will be streamed");
						procA.valueIsReadMultipleTimes(false); // do NOT materialize
					} else {
						Engine.getLogger().debug("bags output by proc "+procA.getId()+" will be materialized");
						procA.valueIsReadMultipleTimes(true); // materialize
					}
					
				} else {
					Engine.getLogger().debug("proc "+procA.getId()+" does not produce a bag, nothing to optimize");

				}
				
				/* recursively do optimizing for source processors */
				for (InputPort readingPort : procA.getPortSet()){
					optimizeFlow(readingPort.getInputProcessor(), false); // false as this is not output proc
				}
				
				/* recursively do optimizing for workflow nested by this processor */
				recursivelyOptimizeFunction(procA.getFunction(), procA.iterates());
			}
			
		}
	}	
	
	private boolean isOutputProcessor(FunctionProcessor p) {
		return p.getOutputReaders()==null;
	}




	/**
	 * Recursively optimize a function. This is possible if it is a Workflow, or if it uses a workflow to perform
	 * a Filter/Categorize operation.   
	 * If the given processor contains an atomic function instead of a workflow, the function just returns.  
	 * @param proc
	 * @throws WorkflowCheckingException
	 */
	private void recursivelyOptimizeFunction(RGLFunction function, boolean wrappingWorkflowIterates) throws WorkflowCheckingException {
		
		if (function instanceof Workflow){
			/* if we let the workflow iterate it will output a bag of bags. So we need caching for 
			   the outputNode within the workflow (they create the inner bags, which are not iterated 
			   by the processor reading from 'proc'.
		    */ 
			(new Optimizer()).optimize((Workflow) function, wrappingWorkflowIterates);
		} else if (function instanceof BagCategorize){
			recursivelyOptimizeFunction(((BagCategorize)function).getCategorizerFunction(), false); // flag doesn't matter as categorizer func outputs Literal, not bag
		} else if (function instanceof FilterFunction){
			recursivelyOptimizeFunction(((FilterFunction)function).getTestingFunction(), false); // flag doesn't matter as testing function returns Boolean, not bag
		} else {
			// nothing to recursively optimize
		}
		
	}




	/**
	 * in thesis I say r AND ((i AND NOT m) OR (f AND NOT i))
	 * @param procA
	 * @return
	 */
	private boolean middleProcGuaranteedToStream(FunctionProcessor procA) {
		
		if (procA.getOutputReaders().size()==1){ // only one processor port is reading from procA (r in logic)
			InputPort readingPort = procA.getOutputReaders().iterator().next();
			if (readingPort.iterates()){ // the reader iterates (i in logic formula )
				boolean m = false; // true if there are More processor ports iterating, other than readingPort
				for (InputPort otherPort : readingPort.getOwnerProcessor().getPortSet()){
					if (otherPort != readingPort && otherPort.iterates()){
						m = true ; 
					}
				}	
				return !m; // if there are multiple iterating ports, cartesian product is taken and materialization possibly good
			} else {
				
				boolean f = functionIteratesMaxOnce(readingPort.getOwnerProcessor().getFunction());
				//System.out.println("     proc "+procA.getId()+" is read by a streaming function ");
				return f;
			}
			
		} else {
			/* if zero processors read, nobody cares. If multiple processors read, they may all iterate and we materialize */
			//System.out.println("     proc "+procA.getId()+" has multiple readers");
		}
		
		return false; 
	}


	/**
	 * It may be nice to actually delegate this determination to the function itself, by means of a 
	 * function.functionWontIterateTwice(String portName). It can return false by default so implementation
	 * is not compulsory, and some methods like FilterFunction can return true. 
	 * @param function
	 * @return
	 */
	private boolean functionIteratesMaxOnce(RGLFunction function) {
		return (
				function instanceof FilterFunction || 
				function instanceof BagGroup || 
				function instanceof BagCategorize ||
				function instanceof BagFlatten ||
				function instanceof BagUnion 
				); 
	}
}
