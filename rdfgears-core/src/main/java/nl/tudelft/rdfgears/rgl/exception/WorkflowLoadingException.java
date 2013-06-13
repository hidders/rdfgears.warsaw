package nl.tudelft.rdfgears.rgl.exception;


public class WorkflowLoadingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String workflowName ;

	public WorkflowLoadingException(String message){
		super(message);
	}
	
	public WorkflowLoadingException(String message, String workflowName){
		super(message);
		this.workflowName = workflowName;
	}
	
	public void setWorkflowName(String name){
		this.workflowName = name;
	}
	
	public String getMessage(){
		return "Cannot load workflow '"+workflowName+"': "+super.getMessage();
	}
	
	
}
