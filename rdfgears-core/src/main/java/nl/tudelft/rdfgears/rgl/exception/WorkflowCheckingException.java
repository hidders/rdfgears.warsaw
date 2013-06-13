package nl.tudelft.rdfgears.rgl.exception;

import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;

public abstract class WorkflowCheckingException extends Exception {

	private static final long serialVersionUID = 1L;


	private static final String WorkflowCheckingException = null;
	

	private FunctionProcessor complainingProcessor;
	private RGLFunction complainingFunction; 
	
	
	
	public WorkflowCheckingException(String message){
		super(message);
	}

	public WorkflowCheckingException(WorkflowCheckingException cause){
		super(cause);
	}

	public FunctionProcessor getProcessor() {
		return complainingProcessor;
	}
	

	public RGLFunction getFunction() {
		return complainingFunction;
	}
	
	
	/**
	 * Register processor and function as complainers that caused the thrown exception. 
	 * Function may be null if the processor was complaining (e.g. iterate-over-nonbag-value or unconnected input). 
	 * Processor may be null if function is not executed in processor-context (root of workflow).  
	 * @param p
	 * @param f
	 */
	public void setProcessorAndFunction(FunctionProcessor p, RGLFunction f){
		this.complainingFunction = f;
		this.complainingProcessor = p;
	}
	
	/**
	 * Get the original message that describes the problem of the complaining Function/Processor
	 * @return
	 */
	public String getOriginalMessage(){
		return getMessage();
	}
	
	

	@Override
	public WorkflowCheckingException getCause(){
		return (WorkflowCheckingException) super.getCause();
	}

	
	public WorkflowCheckingException getRootCause(){
		WorkflowCheckingException wce = (WorkflowCheckingException) super.getCause();
		if (wce==null)
			return this;
		else
			return wce.getRootCause();
	}
	
	public String getProblemLocation() {
		String msg = "";
		if (getCause()!=null){
			msg += getCause().getProblemLocation() ;
		}
		
		msg += "\n\t in ";
		
		if (getFunction()!=null){
			msg += getFunction().getRole()+" "+getFunction().getFullName();
			if (getProcessor()!=null){
				msg += " used by "; 
			}
		}
		
		if (getProcessor()!=null){
			msg += "processor "+getProcessor().getId();
		}
		
		return msg;
	}
	
	
	public String getProblemDescription() {
		return 
			getOriginalMessage() + 
			getProblemLocation();
	}
	
	
}
