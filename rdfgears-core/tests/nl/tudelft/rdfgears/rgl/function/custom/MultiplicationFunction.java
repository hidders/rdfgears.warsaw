package nl.tudelft.rdfgears.rgl.function.custom;

import java.util.Map;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;


/**
 * A stupid testing function that multiplies value1 and value2 . 
 * @author Eric Feliksik
 *
 */
public class MultiplicationFunction extends SimplyTypedRGLFunction {
	public static String value1 = "value1";
	public static String value2 = "value2";
	public MultiplicationFunction(){
		requireInputType(value1, RDFType.getInstance());
		requireInputType(value2, RDFType.getInstance());
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		
		double d1 = inputRow.get(value1).asLiteral().getValueDouble();
		double d2 = inputRow.get(value2).asLiteral().getValueDouble();
		
		return ValueFactory.createLiteralTyped(new Double(d1*d2), XSDDatatype.XSDdouble);
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
