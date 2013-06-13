package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.function.core.BagFlatten;
import nl.tudelft.rdfgears.rgl.function.core.BagFlatten.FlattenedBagValue;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class FlattenedBagBinding extends StreamingBagBinding<FlattenedBagValue> {

	private RenewablyIterableBag outerBag;

	@Override
	protected FlattenedBagValue createMaterializingValue() {
		return BagFlatten.createFlattenedBagValue(id, iteratorPosition,
				materializingBag, outerBag);
	}

	@Override
	protected FlattenedBagValue createPureValue() {
		return BagFlatten.createFlattenedBagValue(id, iteratorPosition,
				outerBag);
	}

	@Override
	protected void readMembers(TupleInput in) {
		String className = in.readString();
		outerBag = (RenewablyIterableBag) BindingFactory.createBinding(
				className).entryToObject(in);
	}

	@Override
	protected void writeMembers(TupleOutput out) {
		out.writeString(outerBag.getClass().getName());
		outerBag.getBinding().objectToEntry(outerBag, out);
	}

	public FlattenedBagBinding() {
		super();
	}

	public FlattenedBagBinding(RenewablyIterableBag outerBag) {
		this.outerBag = outerBag;
	}

}
