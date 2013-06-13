package nl.feliksik.rdfgears;

import java.util.List;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * Split a string into a bag of substrings, given a delimiter string. 
 * 
 * @author Eric Feliksik
 *
 */
public class SplitString extends SimplyTypedRGLFunction {
	public static String str_name = "string";
	public static String delimiter_name = "delimiter";
	
	public SplitString(){
		requireInputType(str_name, RDFType.getInstance());
		requireInputType(delimiter_name, RDFType.getInstance());
		
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		String str = inputRow.get(str_name).asLiteral().getValueString();
		String delim = inputRow.get(delimiter_name).asLiteral().getValueString();
		
		String[] split = str.split(delim);
		
		List<RGLValue> list = ValueFactory.createBagBackingList();
		for (int i=0; i<split.length; i++){
			LiteralValue literalElem = ValueFactory.createLiteralTyped(split[i], null);
			list.add(literalElem);
		}
		return new ListBackedBagValue(list);
	}

	@Override
	public RGLType getOutputType() {
		return BagType.getInstance(RDFType.getInstance());
	}

}
