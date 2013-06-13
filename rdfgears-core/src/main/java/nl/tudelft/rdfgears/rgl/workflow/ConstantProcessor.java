package nl.tudelft.rdfgears.rgl.workflow;

import nl.tudelft.rdfgears.rgl.datamodel.type.BooleanType;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

/**
 * ConstantProcessor, a Workflow node producing constants.
 * 
 * No iteration and other things, as it has no inputs. 
 * 
 * It is supposed to only contain RDFValues, Graphs, Booleans or Null.
 *  
 * Not bags/records. 
 *  
 * @author Eric Feliksik
 *
 */
public class ConstantProcessor extends ProcessorNode {
	
	RGLValue value;

	/**
	 * Constructor. A ConstantProcessor takes some Constant value on instantiation. 
	 * @param function
	 */
	public ConstantProcessor(RGLValue value, String id){
		super(id);
		this.value = value;
	}
	
	public ConstantProcessor(RGLValue value){
		this(value, null);
	}
	
	
	/*****************************************************************************
	 * 
	 * AbstractProcessor implementation
	 * 
	 *****************************************************************************/
	
	/** 
	 * Execute the processor's function on the given input row over values, 
	 * and cache the result.   
	 * @param inputs
	 * @return the result value of the execution.
	 */
	@Override
	public RGLValue getResultValue(){
		return value;
	}
	@Override
	public RGLType getOutputType() {
		assert(value!=null);
		if (value.isRDFValue())
			return RDFType.getInstance();
		else if (value.isGraph())
			return GraphType.getInstance();
		else if (value.isBoolean())
			return BooleanType.getInstance();
		else if (value.isNull())
			return new SuperType();
		
		assert(false) : "shouldn't have this value in this class "+value.getClass().getCanonicalName();
		return null;
	}

	@Override
	public void resetProcessorCache() {
		/* nothing to do, we are not caching as we're constant. And we have no inputs to reset. */
	}


	

}
