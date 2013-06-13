package nl.tudelft.rdfgears.rgl.exception;

import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.WorkflowNode;


public class FunctionConfigurationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private WorkflowNode proc; 
	
	public FunctionConfigurationException(String message) {
		super(message);
	}
	
	public void setProcessor(WorkflowNode processor){
		this.proc = processor;
	}
	public String getMessage(){
		String msg = super.getMessage();
		if (proc!=null){
			msg = "processor "+proc.getId()+" is ill-configured: "+msg;  
		} 
		
		return msg;
	}
}
