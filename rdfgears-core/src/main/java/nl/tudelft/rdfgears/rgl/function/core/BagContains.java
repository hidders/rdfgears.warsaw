package nl.tudelft.rdfgears.rgl.function.core;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.bindings.UnionBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.BooleanType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.BagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.BooleanValueImpl;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.NNRCFunction;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.RenewableIterator;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.rdf.model.Model;
import com.sleepycat.bind.tuple.TupleBinding;

public class BagContains extends SimplyTypedRGLFunction {
	public static final String bag = "bag";
	public static final String element = "element";

	private boolean mergesGraphs = false; // if true, we merge graphs.

	public BagContains() {
		requireInputType(bag, BagType.getInstance(new SuperType())); // any bag will do
		this.requireInput(element);
	}

	@Override
	public void initialize(Map<String, String> config) {
		/* nothing to be done */
	}

	@Override
	public RGLType getOutputType() {
		return BooleanType.getInstance();
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		AbstractBagValue bagVal = inputRow.get(bag).asBag();
		RGLValue elementVal = inputRow.get(element);
		for (RGLValue v : bagVal) {
			if (elementVal.equals(v))
				return BooleanValueImpl.getTrueInstance();
		}

		return BooleanValueImpl.getFalseInstance();
	}
}
