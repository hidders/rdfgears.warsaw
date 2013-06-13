package nl.feliksik.rdfgears;

import java.util.List;
import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.WorkflowLoader;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.util.row.SingleElementValueRow;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * A function that takes the elements from a bag that score highest, given a testingFunction. 
 * If multiple elements score equally high, their ordering is undefined, and it is undefined 
 * which elements will be in the result bag. 
 * If the input bag contains less than n elements, the result bag will be equal to the input bag. 
 * 
 * 
 * Implementation of the function is identical to Filter, but the difference is in the 
 * result-Bag implementation used. 
 * 
 * @author Eric Feliksik
 *
 */
public class BagTopFunction extends AtomicRGLFunction  {
	public static String inputName = "bag";
	private RGLFunction testingFunction;
	private String testingFunctionInputName;
	
	public BagTopFunction() {
		requireInput(inputName);
		// caller must still initialize()
	}
	
	/**
	 * @param testingFunction
	 */
	public BagTopFunction(RGLFunction testingFunction){
		this();
		setTestingFunction(testingFunction);
	}

	private void setTestingFunction(RGLFunction testingFunction){
		this.testingFunction = testingFunction;
	}

	private RGLFunction  getTestingFunction(){
		return testingFunction;
	}
	
	@Override
	public void initialize(Map<String, String> config) throws WorkflowLoadingException {
		setTestingFunction(WorkflowLoader.instantiateFunction(config.get("scoringFunction")));
		
		/** find the input name of the function; functin will be called with the bag elements as only (named) argument */
		List<String> requiredInputNames = getTestingFunction().getRequiredInputNames();
		if (requiredInputNames.size()!=1){
			throw new IllegalArgumentException("The filterFunction must have exactly one inputname, not "+requiredInputNames.size());
		}
		testingFunctionInputName = requiredInputNames.iterator().next();
	}
	
	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		RGLValue bag = inputRow.get(inputName);
		assert(bag!=null): "Something went wrong with typechecking";
		
		double highestScore = Double.MIN_VALUE;
		RGLValue bestScoringValue = null;
		for (RGLValue val : bag.asBag()){
			if (val.isNull())
				continue; // skip this one 
			
			RGLValue scoreVal = testingFunction.execute(new SingleElementValueRow(testingFunctionInputName, val));
			
			if (scoreVal.isNull())
				continue; // skip it 
			
			double score = scoreVal.asLiteral().getValueDouble();
			if (score > highestScore){ 
				highestScore = score;
				bestScoringValue = val;
			}
		}
		
		return bestScoringValue!=null ? bestScoringValue : ValueFactory.createNull("no value in input bag of BagTopFunction");
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		RGLType actualType = inputTypes.get(inputName); 

		if (!(actualType.isBagType() )){
			throw new FunctionTypingException("I require a bag on input with name '"+inputName+"'");
		}
		
		BagType bagType = (BagType) actualType;
		
		TypeRow inputTypeRow = new TypeRow();
		inputTypeRow.put(testingFunctionInputName, bagType.getElemType());
		RGLType testingOutputType;
		try {
			testingOutputType = testingFunction.getOutputType(inputTypeRow);
		} catch (WorkflowCheckingException e) {
			/* the filter function is not well typed. We will throw an error with a trace to this filter function, which *includes* an error 
			 * with a trace to the problem IN the filter function. 
			 */
			e.setProcessorAndFunction(null, testingFunction);
			String filterProblemMsg = e.getProblemDescription();
			throw new FunctionTypingException(filterProblemMsg+"\n\nWhich is used as a score function for BagTopFunction: ");
		} 
		
		if (!(testingOutputType.isRDFValueType())){
			throw new FunctionTypingException("The "+testingFunction.getRole()+" '"+testingFunction.getFullName()+"' is used as score-function and must therefore return a RDFValue (number) type.");
		}
		
		return  bagType.getElemType(); // inputTypes.get(inputName); // type is just the type of the input bag (but value may have less elements)
	}
	
}	
