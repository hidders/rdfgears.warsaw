package nl.tudelft.rdfgears.rgl.workflow;

import java.util.HashMap;
import java.util.Iterator;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * ValueRowIterator is an iterator over ValueRow objects. Construction takes an
 * ValueRow and a processor that iterates. It then offers a way to iterate over
 * ValueRows that are constructed from the given ValueRow according to the RGL
 * specification. That is, the types of the non-iterating ports are preserved
 * and the values simply passed on; But for the iterating ports, the value in
 * the returned ValueRow is some value taken from the bag in the original
 * ValueRow.
 * 
 * @author Eric Feliksik
 * 
 */
public class MemoryValueRowIterator extends AbstractValueRowIterator {

	/*
	 * A map with iterators over the bags. Each iterator has a certain state.
	 * Altogether these states determine how far the iteration is.
	 */
	private HashMap<String, Iterator<RGLValue>> inputIterMap;

	/* administration */
	boolean rowReadyToBeReturned; /*
								 * only true if we just initialized and next()
								 * was never called
								 */

	public MemoryValueRowIterator(ValueRow originalInputs, FunctionProcessor processor, boolean recycleReturnRow) {
		super(originalInputs, processor, recycleReturnRow);
	}

	@Override
	protected Iterator<RGLValue> resetBagIterator(String name) {
		if (inputIterMap == null)
			inputIterMap = new HashMap<String, Iterator<RGLValue>>();
		Iterator<RGLValue> bagIter = originalInputs.get(name).asBag().iterator();
		inputIterMap.put(name, bagIter);
		return bagIter;
	}

	@Override
	protected Iterator<RGLValue> getBagIterator(String name) {
		return inputIterMap.get(name);
	}

}
