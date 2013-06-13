package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.LRUValueManager;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.function.sparql.RemoteSelectBehavior.BatchedRemoteQueryingBagValue;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.query.Query;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * This is not finished. One needs to implement serialization mechanisms for
 * BatchedRemoteQueryingBag's iterator to complete the implementation of this
 * binding.
 * 
 * @author Tomasz Traczyk
 * 
 */
public class BatchedRemoteQueryingBagBinding extends StreamingBagBinding<BatchedRemoteQueryingBagValue> {

	private Query originalQuery;
	private ValueRow inputRow;
	private String endpointURI;

	public BatchedRemoteQueryingBagBinding(Query originalQuery,
			ValueRow inputRow, String endpointURI) {
		this.originalQuery = originalQuery;
		this.inputRow = inputRow;
		this.endpointURI = endpointURI;
	}

	@Override
	protected BatchedRemoteQueryingBagValue createMaterializingValue() {
		return new BatchedRemoteQueryingBagValue(id, iteratorPosition,
				materializingBag, originalQuery, inputRow, endpointURI);
	}

	@Override
	protected BatchedRemoteQueryingBagValue createPureValue() {
		return new BatchedRemoteQueryingBagValue(id, iteratorPosition,
				originalQuery, inputRow, endpointURI);
	}

	@Override
	protected void readMembers(TupleInput in) {
		originalQuery = ValueManager.getQuery(in.readInt());
		inputRow = new ValueRowBinding().entryToObject(in);
		endpointURI = in.readString();
	}

	@Override
	protected void writeMembers(TupleOutput out) {
		out.writeInt(ValueManager.registerQuery(originalQuery));
		new ValueRowBinding().objectToEntry(inputRow, out);
		out.writeString(endpointURI);
	}

}
