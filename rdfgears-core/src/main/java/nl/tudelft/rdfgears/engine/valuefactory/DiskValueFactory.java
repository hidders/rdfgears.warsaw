package nl.tudelft.rdfgears.engine.valuefactory;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.diskvalues.DatabaseManager;
import nl.tudelft.rdfgears.engine.diskvalues.DiskList;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.IdWrappingVisitor;
import nl.tudelft.rdfgears.rgl.workflow.AbstractValueRowIterator;
import nl.tudelft.rdfgears.rgl.workflow.DiskValueRowIterator;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * Overrides some memory valuefactory functions to create disk based values.
 * 
 * 
 * @author Eric Feliksik
 * 
 */
public class DiskValueFactory extends MemoryValueFactory {

	// /**
	// * Make disk based model
	// */
	// @Override
	// public Model createModel() {
	// return TDBFactory.createNamedModel("someName", ".");
	// }
	//
	// /**
	// * Hmmm we actually make a memorygraphvalue, it depends on the model
	// whether
	// * it's disk based or not...
	// */
	// @Override
	// public GraphValue createGraphValue(Model model) {
	// return new MemoryGraphValue(model);
	// }

	public DiskValueFactory() {
		Engine.getLogger().info("DiskValueFactory");
//		DatabaseManager.initialize(); // FIXME: only for testing, initialization
		// should in the main method.
	}

	@Override
	public List<RGLValue> createBagBackingList() {
//		return new DiskList();
		return new ArrayList<RGLValue>();
	}

	@Override
	public boolean isDiskBased() {
		return true;
	}

	@Override
	public AbstractModifiableRecord createModifiableRecordValue(
			FieldIndexMap map) {
		AbstractModifiableRecord modifiableRecord = super
				.createModifiableRecordValue(map);
		ValueManager.registerValue(modifiableRecord);
		return new IdModifiableRecord(modifiableRecord);
	}

	@Override
	public RGLValue registerValue(RGLValue value) {
		Engine.getLogger().debug(value.toString());
		IdWrappingVisitor visitor = new IdWrappingVisitor();

		if (value.isSimple() || value.isGraph()) {
			return value;
		} else {
			ValueManager.registerValue(value);
			value.accept(visitor);
			return visitor.getValue();
		}
	}
	
	@Override
	public AbstractValueRowIterator createValueRowIterator(
			ValueRow originalInputs, FunctionProcessor processor,
			boolean recycleReturnRow) {
		return new DiskValueRowIterator(originalInputs, processor, recycleReturnRow);
	}

}
