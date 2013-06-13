package nl.tudelft.rdfgears.rgl.function.standard;
        
import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A simple function that takes the max value of value1 and value2 . 
 * @author Eric Feliksik
 *
 */
public class MaxVal2 extends AtomicRGLFunction  {
	public static String value1 = "value1";
	public static String value2 = "value2";
	
	
	public MaxVal2(){
		requireInput(value1);
		requireInput(value2);
	}
	

	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		RDFType rdfType = RDFType.getInstance();
		if(! inputTypes.get(value1).isSubtypeOf(rdfType) )
			throw new FunctionTypingException(value1, rdfType, inputTypes.get(value1));
		
		if(! inputTypes.get(value2).isSubtypeOf(rdfType) )
			throw new FunctionTypingException(value2, rdfType, inputTypes.get(value2));
		
		return rdfType;
	}
	
	
	
	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		
		RGLValue v1 = inputRow.get(value1);
		RGLValue v2 = inputRow.get(value2);
		
		double d1 = -Double.MAX_VALUE; 
		double d2 = -Double.MAX_VALUE; 
		boolean haveLiteral = false;
		
		if (v1.isLiteral()){
			d1 = v1.asLiteral().getValueDouble();
			haveLiteral = true;
		}
		if (v2.isLiteral()){
			d2 = v2.asLiteral().getValueDouble();
			haveLiteral = true;
		}
		if (haveLiteral){
			if (d1>d2){
				return v1;
			} else {
				return v2;
			}
		} else {
			return v1; // return error 
		}
		
	}


	@Override
	public void initialize(Map<String, String> config) {
		// TODO Auto-generated method stub
	}

}
