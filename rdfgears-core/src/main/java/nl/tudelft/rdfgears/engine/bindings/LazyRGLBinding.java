package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.workflow.LazyRGLValue;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class LazyRGLBinding extends TupleBinding<RGLValue> {

	private RGLValue cachedResultValue;
	private RGLFunction function;
	private ValueRow inputRow;

	public LazyRGLBinding() {}

	public LazyRGLBinding(RGLValue cachedValue, RGLFunction function,
			ValueRow inputRow) {
		this.cachedResultValue = cachedValue;
		this.function = function;
		this.inputRow = inputRow;
	}

	@Override
	public RGLValue entryToObject(TupleInput in) {
		if (in.readBoolean()) { // there's cachedResultValue
			return BindingFactory.createBinding(in.readString()).entryToObject(
					in);
		} else { // the value is actually lazy
			return new LazyRGLValue(new RGLFunctionBinding().entryToObject(in),
					new ValueRowBinding().entryToObject(in));
		}
	}

	@Override
	public void objectToEntry(RGLValue v, TupleOutput out) {
		if (cachedResultValue != null) {
			out.writeBoolean(true);
			out.writeString(cachedResultValue.getClass().getName());
			cachedResultValue.getBinding()
					.objectToEntry(cachedResultValue, out);
		} else {
			out.writeBoolean(false);
			new RGLFunctionBinding().objectToEntry(function, out);
			new ValueRowBinding().objectToEntry(inputRow, out);
		}
	}

}
