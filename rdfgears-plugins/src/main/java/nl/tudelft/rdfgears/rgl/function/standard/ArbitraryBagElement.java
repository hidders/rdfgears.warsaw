package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Iterator;
import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * Get some element (first we can find) from a bag. 
 * Especially useful if you know you are dealing with a singleton bag, but you want to treat the element in there. 
 *  
 *  Returns NULL if input bag is empty! 
 * 
 * @author Eric Feliksik
 *
 */
public class ArbitraryBagElement extends AtomicRGLFunction {
	public static final String bag1 = "bag";
	public static final BagType requiredType = BagType.getInstance(new SuperType());
	public ArbitraryBagElement(){
		this.requireInput(bag1);
		
	}
	
	@Override
	public void initialize(Map<String, String> config) {
		/* nothing to be done */
	}
	
	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		Iterator<RGLValue> iterator = inputRow.get(bag1).asBag().iterator();
		if (iterator.hasNext()){
			return iterator.next();
		} else {
			return ValueFactory.createNull("BagFirstElement can get no elements from empty bag");
		}
	}
	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		RGLType actualType = inputTypes.get(bag1);
		
		if (! (actualType.isSubtypeOf(requiredType))){
			throw new FunctionTypingException(bag1, requiredType, actualType);
		}
		
		return ((BagType)actualType).getElemType();
	}
}
