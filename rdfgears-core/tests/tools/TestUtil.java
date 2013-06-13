package tools;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;

public class TestUtil {
	
	/**
	 * get output type, but return null instead of throwing an exception, when it is ill-typed
	 * @param proc
	 * @return
	 */
	public static RGLType getOutputType(FunctionProcessor proc){
		try {
			return proc.getOutputType();
			
		} catch (WorkflowCheckingException e) {
			return null;
		}
	}
}
