package nl.tudelft.rdfgears.rgl.function.imreal;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A function that takes as input an JavaScript expression and two parameters
 * that can be used in the expression. The function is responsible to evaluate
 * the expression and return the result;
 * 
 */
public class ExpressionEvaluation extends SimplyTypedRGLFunction {

	public static final String INPUT_A = "a";
	public static final String INPUT_B = "b";
	public static final String INPUT_EXPRESSION = "expression";

	public ExpressionEvaluation() {
		this.requireInputType(INPUT_A, RDFType.getInstance());
		this.requireInputType(INPUT_B, RDFType.getInstance());
		this.requireInputType(INPUT_EXPRESSION, RDFType.getInstance());
	}

	public RGLType getOutputType() {
		return RDFType.getInstance();
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		// typechecking the input
		RGLValue rdfValue = inputRow.get(INPUT_A);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		double a = 0;
		try {
			a = rdfValue.asLiteral().getValueDouble();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return ValueFactory.createNull("Cannot handle input. "
					+ getFullName());
		}

		// /////////////////////////////////////////////////////////////////

		// typechecking the input
		rdfValue = inputRow.get(INPUT_B);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		double b = 0;
		try {
			b = rdfValue.asLiteral().getValueDouble();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return ValueFactory.createNull("Cannot handle input. "
					+ getFullName());
		}

		// ////////////////////////////////////////////////////////////////

		// typechecking the input
		rdfValue = inputRow.get(INPUT_EXPRESSION);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		String expression = rdfValue.asLiteral().getValueString();

		// ////////////////////////////////////////////////////////////////

		Double result = null;
		try {
			result = evaluateExpression(expression, a, b);
		} catch (ScriptException e) {
			e.printStackTrace();
			return ValueFactory.createNull("Cannot evaluate expression: "
					+ e.getMessage());
		}

		if (result != null)
			return ValueFactory.createLiteralDouble(result);
		else
			return ValueFactory.createNull("The result value is null.");
	}

	/**
	 * Evaluates the JavaScript expression
	 */
	private Double evaluateExpression(String expression, double a, double b)
			throws ScriptException {
		// create a script engine manager
		ScriptEngineManager factory = new ScriptEngineManager();
		// create a JavaScript engine
		ScriptEngine engine = factory.getEngineByName("JavaScript");

		engine.put(INPUT_A, a);
		engine.put(INPUT_B, b);
		// evaluate JavaScript code from String
		Object result = engine.eval(expression);

		if (result instanceof Double) {
			return (Double) result;
		}
		return null;
	}
}
