package nl.tudelft.rdfgears.rgl.workflow;

import java.util.HashMap;
import java.util.Iterator;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.util.row.ValueRow;

public class DiskValueRowIterator extends AbstractValueRowIterator {

	/*
	 * A map with iterators over the bags. Each iterator has a certain state.
	 * Altogether these states determine how far the iteration is.
	 */
	private HashMap<String, Long> inputIterIdMap;

	public DiskValueRowIterator(ValueRow originalInputs,
			FunctionProcessor processor, boolean recycleReturnRow) {
		super(originalInputs, processor, recycleReturnRow);
	}

	@Override
	protected Iterator<RGLValue> resetBagIterator(String name) {
		if (inputIterIdMap == null) {
			inputIterIdMap = new HashMap<String, Long>();
		}
		long newIterationId = ValueFactory.getNewId();
		Iterator<RGLValue> bagIter = ((RenewablyIterableBag) originalInputs
				.get(name).asBag()).renewableIterator(newIterationId);
		inputIterIdMap.put(name, newIterationId);
		return bagIter;
	}

	@Override
	protected Iterator<RGLValue> getBagIterator(String name) {
		Iterator<RGLValue> it = ((RenewablyIterableBag) originalInputs
				.get(name).asBag()).renewableIterator(inputIterIdMap.get(name));
		return it;
	}

}
