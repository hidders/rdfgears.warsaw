package nl.tudelft.rdfgears.rgl.function.custom;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * A stupid testing function that adds 5 values
 *  
 * @author Eric Feliksik
 *
 */
public class Sum5Function extends SimplyTypedRGLFunction {
	public static String value1 = "value1";
	public static String value2 = "value2";
	public static String value3 = "value3";
	public static String value4 = "value4";
	public static String value5 = "value5";
	
	public Sum5Function(){
		requireInputType(value1, RDFType.getInstance());
		requireInputType(value2, RDFType.getInstance());
		requireInputType(value3, RDFType.getInstance());
		requireInputType(value4, RDFType.getInstance());
		requireInputType(value5, RDFType.getInstance());
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		
		double d1 = inputRow.get(value1).asLiteral().getValueDouble();
		double d2 = inputRow.get(value2).asLiteral().getValueDouble();
		double d3 = inputRow.get(value3).asLiteral().getValueDouble();
		double d4 = inputRow.get(value4).asLiteral().getValueDouble();
		double d5 = inputRow.get(value5).asLiteral().getValueDouble();
		
		//System.out.println(String.format("%.1f + %.1f + %.1f + %.1f + %.1f = %.1f", d1,d2,d3,d4,d5, result));
		
		return ValueFactory.createLiteralDouble(d1+d2+d3+d4+d5);
	}

	@Override
	public RGLType getOutputType() {
		return RDFType.getInstance();
	}

}
