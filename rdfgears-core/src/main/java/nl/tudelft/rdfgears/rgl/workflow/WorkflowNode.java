package nl.tudelft.rdfgears.rgl.workflow;

import java.util.HashSet;
import java.util.Set;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;


public abstract class WorkflowNode {
	
	//protected abstract RGLFunction getFunction();
	
	public static int idCounter = 1;
	private String id;
	
	/**
	 * outputReaders can be null if the is last in a workflow (it is outputNode). 
	 * This is not pretty, we would rather make outputReaders a set of workflowPorts, that could also be
	 * OutputPort; and then we can do more clever diagnostics on which nodes are reading from us *outside* the
	 * workflow (useful for optimization) 
	 */
	private Set<InputPort> outputReaders = null;
	
	protected WorkflowNode() {
		//nothing to do here - for the sake of serialization
	}
	
	/**
	 * Instantiate a workflow node with given id. If id==null, generate one. 
	 * @param id
	 */
	protected WorkflowNode(String id){
		
		if (id==null){
			int count = idCounter++;
			/* generate an id */
			if (this instanceof WorkflowInputPort){
				id = "workflowInput_"+count;
			} else if (this instanceof ConstantProcessor || this instanceof FunctionProcessor){
				id = "node_"+count;
			}
		}
		
		setId(id);
	}
	
	/**
	 * Get the set of InputPorts that read the output of this node. 
	 * Client, do not modify! 
	 */
	public Set<InputPort> getOutputReaders(){
		return outputReaders;
	}
	
	
	/**
	 * Get a RGLValue. May do caching. 
	 * @return
	 */
	public abstract RGLValue getResultValue();
	

	/**
	 * Get the output type for the processor.
	 * For some processor functions, the output type will depend on the input types. Therefor the result
	 * may depend on the processor inputs. If a processor is not well-typed, this method returns null.  
	 * 
	 * @return the output type. 
	 * @throws WorkflowCheckingException 
	 */
	public abstract RGLType getOutputType() throws WorkflowCheckingException;

	public abstract void resetProcessorCache();

	public String getId() {
		return id;
	}
	
	protected void setId(String id){
		this.id = id;
	}

	/**
	 * InputPorts can use this to (de)register themselves. 
	 * @param inputPort
	 */
	public void addOutputReader(InputPort inputPort) {
		if (outputReaders==null){
			outputReaders = new HashSet<InputPort>();
		}
		getOutputReaders().add(inputPort);
	}

	/**
	 * InputPorts can use this to (de)register themselves. 
	 * @param inputPort
	 * @return true iff the ports was actually an output reader of this node
	 */
	public boolean removeOutputReader(InputPort inputPort) {
		if (outputReaders==null){
			outputReaders = new HashSet<InputPort>();
		}
		return getOutputReaders().remove(inputPort);
	}

}
