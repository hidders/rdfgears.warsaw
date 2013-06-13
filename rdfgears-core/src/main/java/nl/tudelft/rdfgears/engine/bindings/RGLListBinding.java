package nl.tudelft.rdfgears.engine.bindings;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class RGLListBinding extends TupleBinding<List<RGLValue>> {

	@Override
	public List<RGLValue> entryToObject(TupleInput in) {
		ArrayList<RGLValue> result = new ArrayList<RGLValue>();
		int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			if (in.readBoolean()) { // reading simple value
				result.add(BindingFactory.createBinding(
						in.readString()).entryToObject(in));
			} else { // creating DiskRGLValue representation of complex value
				result.add(new IdRGLValue(in.readLong()));
			}
		}		
		return result;
	}

	@Override
	public void objectToEntry(List<RGLValue> list, TupleOutput out) {
		out.writeInt(list.size());
		for (RGLValue element : list) {
			out.writeBoolean(element.isSimple());
			out.writeString(element.getClass().getName());

			TupleBinding<RGLValue> elementBinding = element.getBinding();
			Engine.getLogger().debug(
					"Binding class " + element.getClass().getSimpleName()
							+ " with "
							+ elementBinding.getClass().getSimpleName() + ".");
			elementBinding.objectToEntry(element, out);

		}

	}
}