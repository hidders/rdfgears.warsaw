package nl.tudelft.rdfgears.engine.diskvalues.valuemanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize.AbstractCategoryBag;

import com.hp.hpl.jena.query.Query;

public abstract class AbstractValueManager implements ValueManagerIface {

	private Map<Long, RGLValue> memoryValuesMap = new HashMap<Long, RGLValue>();
	private List<RGLFunction> functionsList = new ArrayList<RGLFunction>();
	private Map<RGLFunction, Integer> functionsMap = new HashMap<RGLFunction, Integer>();
	private List<Query> queryList = new ArrayList<Query>();
	private Map<Query, Integer> queryMap = new HashMap<Query, Integer>();
	private Map<Long, Map<String, AbstractCategoryBag>> categoryBagMaps = new HashMap<Long, Map<String, AbstractCategoryBag>>();
	private Map<Long, Integer> iteratorPositionsMap = new HashMap<Long, Integer>();
	
	public AbstractValueManager() {
		super();
	}

	public int registerFunction(RGLFunction function) {
		Integer id = functionsMap.get(function);
		if (id == null) {
			functionsList.add(function);
			id = functionsList.size() - 1;
		}
		return id;
	}

	public RGLFunction getFunction(int id) {
		return functionsList.get(id);
	}

	public void registerMap(long id, Map<String, AbstractCategoryBag> map) {
		categoryBagMaps.put(id, map);
	}

	public Map<String, AbstractCategoryBag> getMap(long id) {
		return categoryBagMaps.get(id);
	}

	public int registerQuery(Query query) {
		Integer id = queryMap.get(query);
		if (id == null) {
			queryList.add(query);
			id = functionsList.size() - 1;
		}
		return id;
	}

	public Query getQuery(int id) {
		return queryList.get(id);
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
	public boolean registerComplex(Long id) {
		/*
		 * if (complexSet.contains(id)) return false; else { complexSet.add(id);
		 * return true; }
		 */
		return true;

	}

	public void registerMemoryValue(RGLValue value) {
		memoryValuesMap.put(value.getId(), value);
	}

	public RGLValue getMemoryValue(long id) {
		return memoryValuesMap.get(id);
	}

	@Override
	public Map<Long, Integer> getIteratorPositionsMap() {
		return iteratorPositionsMap;
	}
	
}