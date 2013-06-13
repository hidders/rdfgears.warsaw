package nl.tudelft.rdfgears.engine.bindings;

import java.util.List;

import nl.tudelft.rdfgears.engine.diskvalues.DiskList;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class DiskListBinding extends TupleBinding<List<RGLValue>> {

	@Override
	public List<RGLValue> entryToObject(TupleInput in) {
		int size = in.readInt();
		int cacheSize = in.readInt();
		int addOffset = in.readInt();
		String databaseName = in.readString();
		return new DiskList(size, cacheSize, databaseName);
	}

	@Override
	public void objectToEntry(List<RGLValue> list, TupleOutput out) {
			DiskList diskList = (DiskList) list;
			diskList.prepareForStoring();
			out.writeInt(diskList.size());
			out.writeInt(diskList.getCacheSize());
			out.writeInt(diskList.getAddOffset());
			out.writeString(diskList.getDatabaseName());
	}
}