package nl.tudelft.rdfgears.rgl.exception;


public class CircularWorkflowException extends WorkflowLoadingException {

	private static final long serialVersionUID = 1L;
	private String id;

	public CircularWorkflowException(String msg){
		super(msg);
	}
	
}
