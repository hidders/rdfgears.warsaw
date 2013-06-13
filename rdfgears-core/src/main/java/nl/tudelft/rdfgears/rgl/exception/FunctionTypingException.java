package nl.tudelft.rdfgears.rgl.exception;

import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;


public class FunctionTypingException extends WorkflowCheckingException {

	private static final long serialVersionUID = 1L;

	private RGLType requiredType;
	private RGLType actualType;
	
	/**
	 * 
	 * Do not use this constructor, but use the one with required and actual type instead. 
	 * 
	 * It allows a more uniform error message system. 
	 *  
	 * @deprecated
	 * @param message
	 */
	@Deprecated
	public FunctionTypingException(String message) {
		super(message);
	}
	
	public FunctionTypingException(String portName, RGLType requiredType, RGLType actualType) {
		this("Port '"+portName+"' received input of type "+actualType +", but I require "+requiredType);
		this.requiredType = requiredType;
		this.actualType = actualType;
	}
	
	public String getProblemDescription(){
		return "Typing problem: "+getMessage();
	}

	public RGLType getRequiredType() {
		return requiredType;
	}

	public RGLType getActualType() {
		return actualType;
	}
	
	/**
	 *  If the actualType is a bag of the required type, this is an iteration problem. 
	 *  
	 *  Return true if this is an iteration problem 
	 */
	public boolean isIterationProblem(){
		if (actualType instanceof BagType){
			BagType bagType = (BagType) actualType;
			RGLType elemType = bagType.getElemType();
			return (elemType.isSubtypeOf(requiredType));
		}
		return false;
	}

}
