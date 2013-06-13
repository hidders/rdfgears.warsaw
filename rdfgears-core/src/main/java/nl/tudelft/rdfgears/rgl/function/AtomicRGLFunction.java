package nl.tudelft.rdfgears.rgl.function;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.util.row.ValueRow;

public abstract class AtomicRGLFunction extends RGLFunction {

	@Override
	public final RGLValue execute(ValueRow inputRow) {
		RGLValue v = executeImpl(inputRow);
		return ValueFactory.registerValue(v);
	}

	protected abstract RGLValue executeImpl(ValueRow inputRow);

}
