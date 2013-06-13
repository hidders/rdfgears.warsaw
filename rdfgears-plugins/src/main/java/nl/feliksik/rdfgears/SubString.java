package nl.feliksik.rdfgears;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * Get a substring from a given string.  
 * @author Eric Feliksik
 *
 */
public class SubString extends SimplyTypedRGLFunction {
	public static String stringName = "string";
	public static String beginIndexName = "beginIndex";
	public static String endIndexName = "endIndex";
	
	public SubString(){
		requireInputType(stringName, RDFType.getInstance());
		requireInputType(beginIndexName, RDFType.getInstance());
		requireInputType(endIndexName, RDFType.getInstance());
		
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		String stringVal = inputRow.get(stringName).asLiteral().getValueString();
		int start_index = (int) inputRow.get(beginIndexName).asLiteral().getValueDouble();
		int end_index = (int) inputRow.get(endIndexName).asLiteral().getValueDouble();
		
		// return untyped string 
		return ValueFactory.createLiteralTyped(stringVal.substring(start_index, end_index), null); 
	}

	@Override
	public RGLType getOutputType() {
		return BagType.getInstance(RDFType.getInstance());
	}

}
