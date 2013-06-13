package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

public class SoftValueManager extends AbstractBDBValueManager {
	
	private Map<Long, SoftReference<RGLValueWrapper>> valuesCache = new HashMap<Long, SoftReference<RGLValueWrapper>>();
	private static Logger logger = LoggerFactory.getLogger(SoftValueManager.class);

	@Override
	public void registerValue(RGLValue value) {
		logger.debug("put");
		RGLValueWrapper wrapper = new RGLValueWrapper(value, false);
		putIntoCache(wrapper);
		dumpValue(wrapper);
	}

	@Override
	public RGLValue fetchValue(long id) {
		SoftReference<RGLValueWrapper> ref = valuesCache.get(id);
		if (ref == null) {
			System.err.println("readingValue");
			RGLValueWrapper fetchedValue = readValue(id);
			putIntoCache(fetchedValue);
			return fetchedValue.getRglValue();
		} else {
			return ref.get().getRglValue();
		}
	}
	
	private void putIntoCache(RGLValueWrapper value) {
		valuesCache.put(value.getRglValue().getId(), new SoftReference<RGLValueWrapper>(value));
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
