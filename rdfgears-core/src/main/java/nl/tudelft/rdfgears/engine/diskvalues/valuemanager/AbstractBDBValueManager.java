package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import nl.tudelft.rdfgears.engine.bindings.EncapsulatedBinding;
import nl.tudelft.rdfgears.engine.diskvalues.DatabaseManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBDBValueManager extends AbstractValueManager {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractBDBValueManager.class);

	protected void dumpValue(RGLValueWrapper value) {
		if (value == null) {
			throw new RuntimeException();
		}
		try {
			if (!value.isEverDumped()) {
				value.setEverDumped(true);
				EncapsulatedBinding.writeComplex(value);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected RGLValueWrapper readValue(long id) {
		return EncapsulatedBinding.entryToObject(DatabaseManager.getComplexEntry(id));
	}
	
	public void finalize(RGLValueWrapper value) {
		dumpValue(value);
	}
}
