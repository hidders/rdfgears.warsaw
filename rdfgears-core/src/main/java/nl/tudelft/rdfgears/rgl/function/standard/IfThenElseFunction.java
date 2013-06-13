package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.BooleanType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * The if/then/else construct. 
 * 
 *  If the input for ifTrueInput is True, then the element in 'thenResult' is returned. 
 *  If the input if False, the element in 'elseResult' is returned. 
 *  If the input is Null, then Null is returned.  
 * @author Eric Feliksik
 *
 */
public class IfThenElseFunction extends AtomicRGLFunction  {
	public static String ifTrueInput = "if_true";
	public static String thenResult = "then";
	public static String elseResult = "else";
	
	public IfThenElseFunction(){
		requireInput(ifTrueInput);
		requireInput(thenResult);
		requireInput(elseResult);
	}
	
	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		RGLValue trueOrFalse = inputRow.get(ifTrueInput);
		if (trueOrFalse.isBoolean()){
			if (trueOrFalse.asBoolean().isTrue()){
				return inputRow.get(thenResult);
			} else {
				return inputRow.get(elseResult);
			}
		}
		
		/* not a boolean */
		assert (trueOrFalse.isNull());
		return trueOrFalse; // return the non-value
	}

	@Override
	public void initialize(Map<String, String> config) {
		// nothing to do 
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		if (! inputTypes.get(ifTrueInput).isSubtypeOf(BooleanType.getInstance())){
			throw new FunctionTypingException("Must input a Boolean in the field '"+ifTrueInput+"'."); 
		}
		
		RGLType thenType = inputTypes.get(thenResult);
		RGLType elseType = inputTypes.get(elseResult);
		
		/**
		 * The If and Else clause need not necessarily return the same type. 
		 * if we receive types A and B and A is supertype of B, we return A. 
		 * 
		 * 
		 * Note that: 
		 * - If NULL is input as a type (because it was fetched from a ConstantProcessor), we return the *other* type. 
		 * - We assume that constant NULL values in the workflow have the generic SuperType. This seems ok
		 *    but it is not formally proven and should be carefully rethought. 
		 */
		
		
		RGLType returnType; 
		if (thenType.isSupertypeOf(elseType)){
			/* return thenType, unless it is a NULL type */
			if (thenType instanceof SuperType)
				returnType = elseType;
			else 
				returnType = thenType;
		} else if (elseType.isSupertypeOf(thenType)){
			/* return elseType, unless it is a NULL type */
			if (elseType instanceof SuperType)
				returnType = thenType;
			else 
				returnType = elseType; 
		} else {
			throw new FunctionTypingException("The input types for '"+thenResult+"' and '"+elseResult+"' are not interchangable (should be subtypes).");
		}
		
		return returnType;
	}

}