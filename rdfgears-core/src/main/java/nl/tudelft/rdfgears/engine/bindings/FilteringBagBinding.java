package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.standard.FilterFunction;
import nl.tudelft.rdfgears.rgl.function.standard.FilterFunction.FilteringBagValue;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class FilteringBagBinding extends StreamingBagBinding<FilteringBagValue> {

	private RGLFunction testingFunction;
	private RenewablyIterableBag inputBag;
	private RGLValue nextOutput;

	public FilteringBagBinding(MaterializingBag materializingBag,
			RenewablyIterableBag inputBag, RGLFunction testingFunction, RGLValue nextOutput) {
		this.materializingBag = materializingBag;
		this.inputBag = inputBag;
		this.testingFunction = testingFunction;
		this.nextOutput = nextOutput;
	}

	public FilteringBagBinding() {
		super();
	}

	@Override
	protected FilteringBagValue createMaterializingValue() {
		return FilterFunction.createFilteringBag(id, inputBag,
				materializingBag, testingFunction, nextOutput);
		
	}

	@Override
	protected FilteringBagValue createPureValue() {
		return FilterFunction.createFilteringBag(id, inputBag, testingFunction);
	}

	@Override
	protected void readMembers(TupleInput in) {
		testingFunction = new RGLFunctionBinding().entryToObject(in);
		
		inputBag = (RenewablyIterableBag) BindingFactory.createBinding(
				in.readString()).entryToObject(in).asBag();
		if (in.readBoolean()) {
			nextOutput = BindingFactory.createBinding(in.readString()).entryToObject(in);
		}
	}

	@Override
	protected void writeMembers(TupleOutput out) {
		new RGLFunctionBinding().objectToEntry(testingFunction, out);
		out.writeString(inputBag.getClass().getName());
		inputBag.getBinding().objectToEntry(inputBag, out);
		out.writeBoolean(nextOutput != null);
		if (nextOutput != null) {
			out.writeString(nextOutput.getClass().getName());
			nextOutput.getBinding().objectToEntry(nextOutput, out);
		}
	}

}
