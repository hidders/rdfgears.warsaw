package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryLiteralValue;

import com.hp.hpl.jena.datatypes.xsd.impl.XSDDouble;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class MemoryLiteralBinding extends TupleBinding<RGLValue> {
	final int DOUBLE = 0;
	final int STRING = 1;

	@Override
	public RGLValue entryToObject(TupleInput in) {
		RGLValue ret = null;
		switch (in.readInt()) {
		case DOUBLE:
			ret = MemoryLiteralValue.createLiteralTyped(in.readDouble(),
					new XSDDouble("double"));
			break;
		case STRING:
			ret = MemoryLiteralValue.createPlainLiteral(in.readString(), null);
			break;
		}
		return ret;
	}

	@Override
	public void objectToEntry(RGLValue value, TupleOutput out) {
		// TODO Auto-generated method stub
		MemoryLiteralValue literal = (MemoryLiteralValue) value;
		if (literal.getRDFNode().getDatatypeURI() == null) {
			out.writeInt(STRING);
			out.writeString(literal.getValueString());
		} else if (literal.getRDFNode().getDatatypeURI().equals(
				"http://www.w3.org/2001/XMLSchema#double")) {
			out.writeInt(DOUBLE);
			out.writeDouble(literal.getValueDouble());
		} else {
			out.writeInt(STRING);
			out.writeString(literal.getValueString());
		}
	}

}