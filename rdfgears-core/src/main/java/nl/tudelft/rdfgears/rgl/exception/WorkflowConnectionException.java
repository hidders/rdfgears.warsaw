package nl.tudelft.rdfgears.rgl.exception;

import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;

public class WorkflowConnectionException extends WorkflowCheckingException {

	private static final long serialVersionUID = 1L;
	private FunctionProcessor complainingProcessor;
	private String portName;

	public WorkflowConnectionException(String message, String portName){
		super(message);
		this.portName = portName;
	}
	
	public String getPortName(){
		return portName;
	}
	
	
	public String getOriginalMessage() {
		return "Connection problem with port '"+getPortName()+"': "+getMessage(); 
	}

	
	
}
