package nl.tudelft.rdfgears.rgl.workflow;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

/**
 * A WorkflowInput port offers values to the consuming processors in a workflow. 
 *  
 * @author Eric Feliksik
 *
 */
public class WorkflowInputPort extends WorkflowNode {
	
	private Workflow workflow;
	private String portname;

	/** 
	 * Create a workflow input port. The nodeId is used for diagnostic purposes and may be null
	 * @param workflow
	 * @param portname
	 * @param nodeId
	 */
	protected WorkflowInputPort(Workflow workflow, String portname, String nodeId){
		super(null);
		this.portname = portname;
		this.workflow = workflow;
	}

	protected WorkflowInputPort(Workflow workflow, String portname){
		this(workflow, portname, null);
	}
	
	@Override
	public RGLValue getResultValue() {
		return workflow.getCurrentInputRow().get(portname);
	}
	
	@Override
	public RGLType getOutputType() {
		assert(workflow.getInputTypeRow()!=null) : "Cannot getValueType() for workflowInputPort '"+portname+"'. It looks like it is not (yet) connected";
		RGLType type = workflow.getInputTypeRow().get(portname);
		
		if (type==null){ // FIXME ? 
			throw new RuntimeException("Typing Error: Workflow input port '"+portname+"' of workflow "+workflow+"is not configured");
		}
		return type;
	}

	@Override
	public void resetProcessorCache() {
		/* nothing to be done, the workflow has to reset it's own currentInputRow. */
	}

	
}
