package nl.tudelft.rdfgears.rgl.function.custom;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;



/**
 * A stupid testing function that crashes on evaluation. Used to verify whether we are really lazy.  
 * @author Eric Feliksik
 *
 */
public class ErroringFunction extends SimplyTypedRGLFunction {
	
	public static String value1 = "value1";
	public static String value2 = "value2";
	private ErroringFunction(){
		requireInputType(value1, RDFType.getInstance());
		requireInputType(value2, RDFType.getInstance());
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		throw new EvaluationException("I am evaluated, but I should have been treated lazily");
	}

	@Override
	public RGLType getOutputType() {
		return RDFType.getInstance();
	}

	@Override
	public void initialize(Map<String, String> config) {
		// TODO Auto-generated method stub
		
	}
}

class EvaluationException extends RuntimeException {
	public EvaluationException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 1L;
	
}