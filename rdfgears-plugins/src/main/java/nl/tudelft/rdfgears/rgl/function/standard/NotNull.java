package nl.tudelft.rdfgears.rgl.function.standard;
        
import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BooleanType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A simple function to fetch Linked Data from the web.  
 * @author Eric Feliksik
 *
 */
public class NotNull extends AtomicRGLFunction  {
	public static String port = "value";
	
	public NotNull(){
		this.requireInput(port); 
	}

	public RGLType getOutputType() {
		return BooleanType.getInstance();
	}
	
	@Override
	public void initialize(Map<String, String> config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		if (inputRow.get(port).isNull())
			return ValueFactory.createFalse(); 
		else 
			return ValueFactory.createTrue(); 

	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws WorkflowCheckingException {
		// any type is ok. However, this class does not extend SimplyTypedRGLFunction because that would 
		// also mean that NULL value inputs would be automatically propagated, which is not what we want. 
		return BooleanType.getInstance(); 
	}

}
