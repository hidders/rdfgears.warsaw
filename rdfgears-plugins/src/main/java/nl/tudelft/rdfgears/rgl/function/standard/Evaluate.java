package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.ValueEvaluator;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * This is the identity function, but it does evaluate the input recursively. 
 * 
 * Thus, it will generate possible side-effects, if they are specified in the Function. 
 * 	
 */
public class Evaluate extends AtomicRGLFunction  {
	public static String valueField = "value";
	
	public Evaluate(){
		requireInput(valueField); // any value is ok 
	}
	
	@Override
	public void initialize(Map<String, String> config) {
		// TODO Auto-generated method stub
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes)
			throws FunctionTypingException {
		return inputTypes.get(valueField);
	}

	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		ValueEvaluator visitor = new ValueEvaluator();
		RGLValue val = inputRow.get(valueField);
		val.accept(visitor);
		return val;
	}

}
