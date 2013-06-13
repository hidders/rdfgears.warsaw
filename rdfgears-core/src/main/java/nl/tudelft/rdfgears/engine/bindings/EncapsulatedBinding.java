package nl.tudelft.rdfgears.engine.bindings;

import nl.tudelft.rdfgears.engine.diskvalues.DatabaseManager;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.RGLValueWrapper;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;

public class EncapsulatedBinding {

	long id;

	public EncapsulatedBinding(long id) {
		this.id = id;
	}

	public static RGLValueWrapper entryToObject(DatabaseEntry entry) {
//		String className = in.readString();
//		ComplexBinding binding = (ComplexBinding) BindingFactory
//				.createBinding(className);
//		return binding.complexInputToObject(in, id);
		StoredClassCatalog classCatalog = DatabaseManager.getClassCatalog();

	    // Create the binding
	    EntryBinding<RGLValue> dataBinding = new SerialBinding<RGLValue>(classCatalog, 
	                                                 RGLValue.class);

	    // Create DatabaseEntry objects for the key and data

	    // Do the get as normal

	    // Recreate the MyData object from the retrieved DatabaseEntry using
	    // the EntryBinding created above
	    return (RGLValueWrapper) dataBinding.entryToObject(entry);
	}

	public void objectToEntry(RGLValue value, TupleOutput out) {
		out.writeString(value.getClass().getName());
		((ComplexBinding) value.getBinding()).writeComplexToOutput(value, out);
	}

	public static void writeComplex(RGLValueWrapper value) {
		Database complexStore = DatabaseManager.getComplexStore();
		DatabaseEntry valueEntry = new DatabaseEntry();
		DatabaseEntry idEntry = DatabaseManager.long2entry(value.getRglValue().getId());
		
		StoredClassCatalog classCatalog = DatabaseManager.getClassCatalog();
		EntryBinding<RGLValueWrapper> dataBinding = new SerialBinding<RGLValueWrapper>(classCatalog,
				RGLValueWrapper.class);
		dataBinding.objectToEntry(value, valueEntry);
		
		complexStore.put(null, idEntry, valueEntry);
	}
}
