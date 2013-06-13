package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * Return the bag size 
 * @author Eric Feliksik
 *
 */
public class BagSize extends SimplyTypedRGLFunction  {
	public static String bag = "bag";
	
	public BagSize(){
		requireInputType(bag, BagType.getInstance(new SuperType())); // any bag will do
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		int size = inputRow.get(bag).asBag().size();
		return ValueFactory.createLiteralDouble(size);
		
		//Engine.getValueFactory().createTypedLiteral(new Double(size), XSDDatatype.XSDdouble);	
	}
	
	@Override
	public void initialize(Map<String, String> config) {
		// TODO Auto-generated method stub
	}

	@Override
	public RGLType getOutputType() {
		return RDFType.getInstance();
	}

}
