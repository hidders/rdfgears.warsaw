package nl.tudelft.rdfgears.rgl.workflow;

/**
 * A WorkflowNode that isn't a workflow input port port 
 * @author Eric Feliksik
 *
 */
public abstract class ProcessorNode extends WorkflowNode {
	String path = null;
	
	protected ProcessorNode(String id) {
		super(id);
	}
	
	protected ProcessorNode() {
		
	}
	
	public void setPath(String path){
		this.path = path;
	}
	public String getPath(){
		return path; 
	}

}
