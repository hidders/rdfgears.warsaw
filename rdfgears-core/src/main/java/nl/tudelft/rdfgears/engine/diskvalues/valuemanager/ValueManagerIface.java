package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize.AbstractCategoryBag;

import com.hp.hpl.jena.query.Query;

public interface ValueManagerIface {

	int registerFunction(RGLFunction function);

	RGLFunction getFunction(int id);

	void registerMap(long id, Map<String, AbstractCategoryBag> map);

	Map<String, AbstractCategoryBag> getMap(long id);

	int registerQuery(Query query);

	Query getQuery(int id);

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
	boolean registerComplex(Long id);

	/**
	 * Registers a value in a memory only cache. This value would stay in the
	 * memory, won't get dumped to disk and won't get garbage collected.
	 * 
	 * @param value
	 *            the value to be registered
	 */
	void registerMemoryValue(RGLValue value);

	/**
	 * Returns the value with the given id.
	 * 
	 * @param id
	 *            id of value to get
	 * @return the value with the given id
	 */
	RGLValue getMemoryValue(long id);

	/**
	 * Registers the given {@link RGLValue} in the manager - it will be ever
	 * since available in the {@link #fetchValue(long)} method.
	 * 
	 * @param value
	 *            the value to be registered
	 */
	void registerValue(RGLValue value);

	/**
	 * Fetches the value with given id. This may, or may not perform some disk
	 * operations depending on managers implementation and state.
	 * 
	 * @param id
	 *            the id of value to be fetched
	 * @return the value connected with the given id
	 */
	RGLValue fetchValue(long id);

	/**
	 * Updates the given value. 
	 * 
	 * It's necessary to implement this method in {@link JCSValueManager},
	 * the rest of managers might probably have just an empty method here,
	 * but probably implementing this would be safer and wouldn't affect efficency
	 * that much 
	 * 
	 * @param value
	 */
	void updateValue(RGLValue value);

	void shutDown();
	
	Map<Long, Integer> getIteratorPositionsMap();
	
}