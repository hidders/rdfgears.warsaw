package nl.tudelft.rdfgears.rgl.function.imreal;

import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 *  Determines if a particular string literal occurs in a bag
 *  @author Claudia
 *
 */
public class StringInBag extends SimplyTypedRGLFunction  {
	public static String bag = "bag";
	public static String searchString = "searchString";
	
	public StringInBag(){
		requireInputType(bag, BagType.getInstance(new SuperType())); // any bag will do
		requireInputType(searchString, RDFType.getInstance());
	}
	
	@Override
	public void initialize(Map<String, String> config) {
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		
		RGLValue rdfValue = inputRow.get(searchString);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());
		
		String toSearchFor = rdfValue.asLiteral().toString();
		
		AbstractBagValue bv = inputRow.get(bag).asBag();
		for(RGLValue val : bv)
		{
			if(val.toString().equals(toSearchFor))
				return ValueFactory.createTrue();			
		}
		return ValueFactory.createFalse(); 
	}
	

	@Override
	public RGLType getOutputType() {
		return RDFType.getInstance();
	}

}
