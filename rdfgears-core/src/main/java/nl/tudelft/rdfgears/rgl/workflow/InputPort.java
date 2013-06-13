package nl.tudelft.rdfgears.rgl.workflow;

import java.io.Serializable;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;

/**  
 * A processor input port, that can be connected to another processor to read its input from. 
 * @author Eric Feliksik
 *
 */
public class InputPort implements Comparable, Serializable {
	private String portName;
	private FunctionProcessor proc;
	private WorkflowNode inputProcessor;
	private boolean iterate;

	public InputPort(String portName, FunctionProcessor owner){
		this.portName = portName;
		this.proc = owner;
	}
	
	public String getName(){
		return this.portName;
	}
	
	public FunctionProcessor getOwnerProcessor(){
		return this.proc;
	}

	protected boolean isConnected(){
		return this.inputProcessor!=null;
	}
	
	protected RGLValue readInput(){
		assert(this.isConnected());
		return this.inputProcessor.getResultValue();
	}

	public void resetInput(){
		this.inputProcessor.resetProcessorCache();
	}
	
	protected RGLType getInputType() throws WorkflowCheckingException{
		assert(isConnected());
		return inputProcessor.getOutputType();
	}

	public void setInputProcessor(WorkflowNode node){
		assert(node!=null): "cannot set a 'null' input";
		
		if (inputProcessor!=null){
			boolean found = inputProcessor.removeOutputReader(this); // deliberately not inside the assertion
			assert(found); 
		}
		
		inputProcessor = node;
		inputProcessor.addOutputReader(this);
	}
	
	public WorkflowNode getInputProcessor(){
		return this.inputProcessor;
	}

	public void markIteration(){
		this.iterate = true;
		this.proc.flagIteration(this); /* notify processor */
	}
	
	/** 
	 * return true iff this port is marked for iteration
	 * @return
	 */
	public boolean iterates(){
		return this.iterate;
	}

	/**
	 * Comparable only needed to be able to map ports in a TreeMap. 
	 * Maybe later we can invesigate whether this is really faster than HashMap, and whether this is significant.  
	 */
	@Override
	public int compareTo(Object o) {
		if (! (o instanceof InputPort)){
			return 1; /* not equal, other than that it doesn't matter */
		}
		return this.getName().compareTo(((InputPort) o).getName());
	}
	
	public String toString(){
		return super.toString() + "_portname="+getName();
	}
}
