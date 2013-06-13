package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * The identity function.
 * It doesn't do anything to it's input: It just returns the only input, named "value". 
 * @author Eric Feliksik
 *
 */
public class Identity extends AtomicRGLFunction  {
	public static String value1 = "value";
	
	public Identity(){
		requireInput(value1);
	}
	
	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		return inputRow.get(value1);
	}

	@Override
	public void initialize(Map<String, String> config) {
		// nothing to do 
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		return inputTypes.get(value1);
	}

}