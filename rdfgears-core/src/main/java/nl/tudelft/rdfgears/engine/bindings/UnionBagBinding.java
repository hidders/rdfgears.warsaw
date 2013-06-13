package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.function.core.BagUnion;
import nl.tudelft.rdfgears.rgl.function.core.BagUnion.UnionBagValue;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class UnionBagBinding extends StreamingBagBinding<UnionBagValue> {
	private RenewablyIterableBag bag1;
	private RenewablyIterableBag bag2;

	public UnionBagBinding(MaterializingBag materializingBag,
			RenewablyIterableBag bag1, RenewablyIterableBag bag2) {
		this.materializingBag = materializingBag;
		this.bag1 = bag1;
		this.bag2 = bag2;
	}

	public UnionBagBinding() {
		super();
	}

	@Override
	protected UnionBagValue createMaterializingValue() {
		return BagUnion.createUnionBag(id, iteratorPosition, materializingBag, bag1, bag2);
	}

	@Override
	protected UnionBagValue createPureValue() {
		return BagUnion.createUnionBag(id, iteratorPosition, bag1, bag2);
	}

	@Override
	protected void readMembers(TupleInput in) {
		bag1 = (RenewablyIterableBag) BindingFactory.createBinding(
				in.readString()).entryToObject(in);
		bag2 = (RenewablyIterableBag) BindingFactory.createBinding(
				in.readString()).entryToObject(in);

	}

	@Override
	protected void writeMembers(TupleOutput out) {
		out.writeString(bag1.getClass().getName());
		bag1.getBinding().objectToEntry(bag1, out);
		out.writeString(bag2.getClass().getName());
		bag2.getBinding().objectToEntry(bag2, out);
	}

}
