package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.HashMap;
import java.util.Map;

import nl.tudelft.rdfgears.engine.valuefactory.ValueFactoryIface;
import nl.tudelft.rdfgears.rgl.datamodel.type.BooleanType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

public class ComparatorFunction extends SimplyTypedRGLFunction {
	private static String input1 = "a";
	private static String input2 = "b";
	
	private static Map<String, ComparatorIface> comparators = null;
	
	private ComparatorIface thisComparator;
	
	public ComparatorFunction(){
		// this could eventually be any type, as long as they are the same and they are comparable with less than/ greater than 
		// (e.g. bags can be compared on size, etc)
		// for now, only support RDFValueTypes 
		requireInputType(input1, RDFType.getInstance()); 
		requireInputType(input2, RDFType.getInstance());
	}
	
	private static void initMap(){
		if (comparators==null){
			comparators = new HashMap<String, ComparatorIface>();
			comparators.put("OP_LESS", new Comparator_less());
			comparators.put("OP_LESS_EQUAL", new Comparator_less_equal());
			comparators.put("OP_GREATER", new Comparator_greater());
			comparators.put("OP_GREATER_EQUAL", new Comparator_greater_equal());
			comparators.put("OP_EQUAL", new Comparator_equal());
			comparators.put("OP_NOT_EQUAL", new Comparator_not_equal());
		}
	}
	
	@Override
	public RGLType getOutputType() {
		return BooleanType.getInstance();
	}

	@Override
	public void initialize(Map<String, String> config) {
		initMap();
		String opName = config.get("operator").trim().toUpperCase();
		thisComparator = comparators.get(opName);
		
		if (thisComparator==null){
			throw new RuntimeException("Not a valid compare operation: "+opName);
		}
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		RGLValue v1 = inputRow.get(input1);
		RGLValue v2 = inputRow.get(input2);
		return thisComparator.compare(v1, v2) ? ValueFactoryIface.trueValue : ValueFactoryIface.falseValue;
	}

}


interface ComparatorIface {
	/**
	 * Return true iff the comparison matches, return false otherwise. 
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 */
	public boolean compare(RGLValue v1, RGLValue v2);
}

class Comparator_less implements ComparatorIface {
	@Override
	public boolean compare(RGLValue v1, RGLValue v2) {
		return v1.compareTo(v2) < 0;
	}
}

class Comparator_less_equal implements ComparatorIface {
	@Override
	public boolean compare(RGLValue v1, RGLValue v2) {
		return v1.compareTo(v2) <= 0;
	}
}

class Comparator_greater implements ComparatorIface {
	@Override
	public boolean compare(RGLValue v1, RGLValue v2) {
		return v1.compareTo(v2) > 0;
	}
}

class Comparator_greater_equal implements ComparatorIface {
	@Override
	public boolean compare(RGLValue v1, RGLValue v2) {
		return v1.compareTo(v2) >= 0;
	}
}

/**
 * implement separate equal operator. 
 * Read http://jena.sourceforge.net/javadoc/com/hp/hpl/jena/rdf/model/Literal.html#sameValueAs%28com.hp.hpl.jena.rdf.model.Literal%29
 * 
 * Sometimes there is reason to not use equals(), this can be changed here or in compareTo implementation. 
 * 
 * @author Eric Feliksik
 *
 */
class Comparator_equal implements ComparatorIface {
	@Override
	public boolean compare(RGLValue v1, RGLValue v2) {
		return v1.compareTo(v2) == 0;
	}
}

/**
 * 
 * @author Eric Feliksik
 *
 */
class Comparator_not_equal implements ComparatorIface {
	@Override
	public boolean compare(RGLValue v1, RGLValue v2) {
		return v1.compareTo(v2) != 0;
	}
}



