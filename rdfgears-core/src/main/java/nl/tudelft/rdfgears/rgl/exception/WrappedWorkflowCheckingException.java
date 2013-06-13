package nl.tudelft.rdfgears.rgl.exception;




/**
 * A wrapper around another WorkflowCheckingException. It is needed to maintain the problem trace in nested workflows,
 * and administers the processor and function of the workflows where a problem was detected.  
 * @author Eric Feliksik
 *
 */
public class WrappedWorkflowCheckingException extends WorkflowCheckingException {
	private static final long serialVersionUID = 1L;
	
	public WrappedWorkflowCheckingException(WorkflowCheckingException cause) {
		super(cause);
//		this.complainingFunction = f;
//		this.complainingProcessor = p;
	}
	
	
	public String getOriginalMessage(){
		return getCause().getOriginalMessage();
	}
//	public String getProblemDescription() {
//		return 
//			getOriginalMessage() + 
//			getProblemLocation();
//	}
}
