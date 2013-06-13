package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

/**
 * Similar to the MemoryBinding. But the thing is, that we assume, that there
 * won't ever be many functions in workflow - number of functions is determined
 * by the size of workflow, not by the size of data. Therefore, there's no need
 * to provide a real binding for functions - we can just always store them in
 * memory.
 * 
 * @author Tomasz Traczyk
 * 
 */
public class RGLFunctionBinding extends TupleBinding<RGLFunction> {

	@Override
	public RGLFunction entryToObject(TupleInput in) {
		return ValueManager.getFunction(in.readInt());
	}

	@Override
	public void objectToEntry(RGLFunction function, TupleOutput out) {
		int fId = ValueManager.registerFunction(function);
		out.writeInt(fId);
	}

}
