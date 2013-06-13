package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

/**
 * @author Tomasz Traczyk
 * 
 */
public class LRUValueManager extends AbstractBDBValueManager {
	private Map<Long, RGLValueWrapper> valuesCache = new HashMap<Long, RGLValueWrapper>();
	private TreeMap<Long, Long> lastUse = new TreeMap<Long, Long>();

	private int valuesCacheSize = 1000;

	private void putIntoCache(RGLValueWrapper v) {
		if (valuesCache.size() == valuesCacheSize) {
			long lruId = leastRecentlyUsed(lastUse);
			lastUse.remove(lruId);
			RGLValueWrapper lruV = valuesCache.remove(lruId);
			dumpValue(lruV);
		}
		valuesCache.put(v.getRglValue().getId(), v);
		lastUse.put(v.getRglValue().getId(), System.currentTimeMillis());
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
		}
		lastUse.put(id, System.currentTimeMillis());
		return fetchedValue.getRglValue();
	}

	private long leastRecentlyUsed(Map<Long, Long> lastUse) {
		long min = Long.MAX_VALUE;
		long id = -1;

		for (Entry<Long, Long> e : lastUse.entrySet()) {
			if (e.getValue() < min) {
				min = e.getValue();
				id = e.getKey();
			}
		}

		return id;
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
