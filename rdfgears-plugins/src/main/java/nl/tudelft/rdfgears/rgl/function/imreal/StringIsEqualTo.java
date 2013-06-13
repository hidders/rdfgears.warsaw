package nl.tudelft.rdfgears.rgl.function.imreal;

import java.util.Map;



import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.BooleanType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * Determines if two strings are equal 
 * @author Claudia
 *
 */
public class StringIsEqualTo extends SimplyTypedRGLFunction  {
	public static String value1 = "value1";
	public static String value2 = "value2";
	
	public StringIsEqualTo(){
		requireInputType(value1, RDFType.getInstance());
		requireInputType(value2, RDFType.getInstance());
	}
	
	@Override
	public void initialize(Map<String, String> config) {
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		
		
		RGLValue rdfValue1 = inputRow.get(value1);
		if (!rdfValue1.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());
		
		String s1 = rdfValue1.asLiteral().toString();
		
		RGLValue rdfValue2 = inputRow.get(value2);
		if (!rdfValue2.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());
		
		String s2 = rdfValue2.asLiteral().toString();

		if(s1.equals(s2))
			return ValueFactory.createTrue();
		
		return ValueFactory.createFalse(); 
	}
	

	@Override
	public BooleanType getOutputType() {
		return BooleanType.getInstance();
	}

}
