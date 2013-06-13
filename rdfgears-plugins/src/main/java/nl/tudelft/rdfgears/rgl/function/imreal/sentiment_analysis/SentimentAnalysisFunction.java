package nl.tudelft.rdfgears.rgl.function.imreal.sentiment_analysis;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

import org.persweb.sentiment.eval.USemSentimentAnalysis;

/**
 * A function that performs sentiment analysis
 * 
 */
public class SentimentAnalysisFunction extends SimplyTypedRGLFunction {

	/**
	 * The name of the input field providing the statement that will be analysed
	 */
	public static final String INPUT_STATEMENT = "statement";


	public SentimentAnalysisFunction() {
		this.requireInputType(INPUT_STATEMENT, RDFType.getInstance());
	}

	@Override
	public RGLType getOutputType() {
		return RDFType.getInstance();
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		// typechecking the input
		RGLValue rdfValue = inputRow.get(INPUT_STATEMENT);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		// extracting the twitter username from the input
		String statement = rdfValue.asLiteral().getValueString();

		double result = USemSentimentAnalysis.analyzeTweetSentiment(statement);
		
		if(result > 0){
			return ValueFactory.createLiteralPlain("positive", null);
		} else if(result < 0){
			return ValueFactory.createLiteralPlain("negative", null);
		}

		return ValueFactory.createLiteralPlain("neutral", null);
	}

}
