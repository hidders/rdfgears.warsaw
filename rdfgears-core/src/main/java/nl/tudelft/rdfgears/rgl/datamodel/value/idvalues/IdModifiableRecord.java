package nl.tudelft.rdfgears.rgl.datamodel.value.idvalues;

import com.sleepycat.bind.tuple.TupleBinding;

import nl.tudelft.rdfgears.engine.bindings.idvalues.IdModifiableRecordBinding;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.ModifiableRecord;

public class IdModifiableRecord extends IdRecordValue implements
		AbstractModifiableRecord {

	public IdModifiableRecord(AbstractModifiableRecord value) {
		super(value);
	}


	public IdModifiableRecord(long id) {
		super(id);
	}

	@Override
	public void put(String fieldName, RGLValue element) {
		//Engine.getLogger().info(fieldName + " " + element.getClass());
		ModifiableRecord realValue = (ModifiableRecord) fetch(); 
		realValue.put(fieldName, element);
		ValueManager.registerValue(realValue); //we need to update the record, because it got dirty
	}


	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new IdModifiableRecordBinding();
	}

}
