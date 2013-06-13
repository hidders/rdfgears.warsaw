package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

/**
 * @author Tomasz Traczyk
 * 
 */
public class MixedValueManager extends AbstractBDBValueManager {
	private Map<Long, RGLValueWrapper> valuesCache = new HashMap<Long, RGLValueWrapper>();
	private Map<Long, SoftReference<RGLValueWrapper>> softValuesCache = new HashMap<Long, SoftReference<RGLValueWrapper>>();
	private TreeMap<Long, Long> lastUse = new TreeMap<Long, Long>();

	private int valuesCacheSize = 100;

	private void putIntoCache(RGLValueWrapper v) {
		if (valuesCache.size() == valuesCacheSize) {
			long lruId = leastRecentlyUsed(lastUse);
			lastUse.remove(lruId);
			RGLValueWrapper lruV = valuesCache.remove(lruId);
			dumpValue(lruV);
			softValuesCache.put(lruId, new SoftReference<RGLValueWrapper>(lruV));
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
			SoftReference<RGLValueWrapper> ref = softValuesCache.get(id);
			if (ref == null) {
				fetchedValue = readValue(id);
				softValuesCache.put(id, new SoftReference<RGLValueWrapper>(fetchedValue));
			} else {
				fetchedValue = ref.get();
			}
			
		}
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
