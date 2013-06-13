package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize.AbstractCategoryBag;

import com.hp.hpl.jena.query.Query;

/**
 * Static facade to configured {@link ValueManagerIface}.
 * 
 * @author Tomasz Traczyk
 */
public class ValueManager {

	/**
	 * The real valueManager
	 */
	private static ValueManagerIface valueManager = getValueManager();

	private static ValueManagerIface getValueManager() {
		return new MemoryValueManager();
//		return new JCSValueManager();
//		return new LRUValueManager();
//		return new SoftValueManager();
//		return new ChunksValueManager();
//		return new MixedValueManager();
	}

	/**
	 * Registers an {@link RGLFunction} in the manager for later use.
	 * 
	 * @param function
	 *            the function to be registered
	 * @return the id on which the function will be available
	 */
	public static int registerFunction(RGLFunction function) {
		return valueManager.registerFunction(function);
	}

	/**
	 * Gets the function with given id from the memory cache.
	 * 
	 * @param id
	 *            the id of the function
	 * @return function
	 */
	public static RGLFunction getFunction(int id) {
		return valueManager.getFunction(id);
	}

	// TODO przyjrzeć się, co to robi, dlaczego to robi i dodać dokumentację.
	public static void registerMap(long id, Map<String, AbstractCategoryBag> map) {
		valueManager.registerMap(id, map);
	}

	public static Map<String, AbstractCategoryBag> getMap(long id) {
		return valueManager.getMap(id);
	}

	public static int registerQuery(Query query) {
		return valueManager.registerQuery(query);
	}

	public static Query getQuery(int id) {
		return valueManager.getQuery(id);
	}

	/**
	 * This is deprecated, because it's no longer needed, since Bag's don't any
	 * more manage their actual element's caches themselves.
	 * 
	 * @param id
	 *            the id of the complex object to be registered
	 * @return true if the object have not been registered earlier (thus it must
	 *         be serialized), false - otherwise
	 */
	@Deprecated
	public static boolean registerComplex(Long id) {
		return valueManager.registerComplex(id);
	}

	public static void registerMemoryValue(RGLValue value) {
		valueManager.registerMemoryValue(value);
	}

	public static RGLValue getMemoryValue(long id) {
		return valueManager.getMemoryValue(id);
	}

	static double maxMemoryUsed = 0;
	
	/**
	 * Registers the given {@link RGLValue} in the manager - it will be ever
	 * since available in the {@link #fetchValue(long)} method.
	 * 
	 * @param value
	 *            the value to be registered
	 */
	public static void registerValue(RGLValue value) {
		valueManager.registerValue(value);
	}
	
	public static void updateValue(RGLValue value) { //FIXME #1 
//		valueManager.registerValue(value);
	}

	/**
	 * Fetches the value with given id. This may, or may not perform some disk
	 * operations depending on managers implementation and state.
	 * 
	 * @param id
	 *            the id of value to be fetched
	 * @return the value connected with the given id
	 */
	public static RGLValue fetchValue(long id) {
		return valueManager.fetchValue(id);
	}
	
	public static void shutDown() {
		valueManager.shutDown();
	}

	public static Map<Long, Integer> getIteratorPositionsMap() {
		return valueManager.getIteratorPositionsMap();
	}
}