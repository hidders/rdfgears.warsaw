package nl.tudelft.rdfgears.rgl.function.core;

import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.NNRCFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * Create a singleton bag from a value 
 * 
 * @author Eric Feliksik
 *
 */
public class BagSingleton extends NNRCFunction {
	public static final String value = "value";
	public BagSingleton(){
		this.requireInput(value);
	}

	@Override
	public void initialize(Map<String, String> config) {
		/* nothing to be done */
	}
	
	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		return ValueFactory.createBagSingleton(inputRow.get(value));
	}
	
	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		RGLType inputType = inputTypes.get(value);
		if (inputType==null){
			throw new FunctionTypingException("Expect an input with name '"+value+"'");
		}
		
		return BagType.getInstance(inputType);
	}

}
