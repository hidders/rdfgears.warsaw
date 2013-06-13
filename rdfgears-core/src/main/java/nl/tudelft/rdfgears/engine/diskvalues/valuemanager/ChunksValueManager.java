package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import java.util.HashMap;
import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

/**
 * @author Tomasz Traczyk
 * 
 */
public class ChunksValueManager extends AbstractBDBValueManager {
	private Map<Long, RGLValueWrapper> valuesCache = new HashMap<Long, RGLValueWrapper>();
	// private Set<Long> everDumped = new HashSet<Long>();

	private int valuesCacheSize = 100000;

	private void putIntoCache(RGLValueWrapper v) {
		if (valuesCache.size() == valuesCacheSize) {
			for (RGLValueWrapper e : valuesCache.values()) {
				dumpValue(e);
			}
			valuesCache.clear();
		}

		valuesCache.put(v.getRglValue().getId(), v);
	}

	public void registerValue(RGLValue value) {
		putIntoCache(new RGLValueWrapper(value, false));
	}

	@Override
	public RGLValue fetchValue(long id) {
		RGLValueWrapper fetchedValue;
		fetchedValue = valuesCache.get(id);
		if (fetchedValue == null) {
			fetchedValue = readValue(id);
			putIntoCache(fetchedValue);
			// everDumped.add(id);
		}
		return fetchedValue.getRglValue();
	}

	@Override
	public void updateValue(RGLValue value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub

	}
}
