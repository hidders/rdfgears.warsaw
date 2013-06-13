package nl.tudelft.rdfgears.rgl.function.core;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.NNRCFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

public class RecordUnion extends NNRCFunction {
	public static String r1 = "r1"; 
	public static String r2 = "r2"; 
	public RecordUnion(){
		this.requireInput(r1);
		this.requireInput(r2);
	}

	@Override
	public void initialize(Map<String, String> config) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		// TODO Auto-generated method stub
		return null;
	}


}
