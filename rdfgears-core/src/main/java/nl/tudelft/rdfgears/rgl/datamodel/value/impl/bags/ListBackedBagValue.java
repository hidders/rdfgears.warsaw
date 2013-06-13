package nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.bindings.ListBackedBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.OrderedBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

import com.sleepycat.bind.tuple.TupleBinding;

/**
 * A MemoryBag value supports adding elements, for during the construction.
 * 
 * @author Eric Feliksik
 * 
 */
public class ListBackedBagValue extends OrderedBagValue {
	protected List<RGLValue> backingList;

	public ListBackedBagValue(long id, Map<Long, Integer> iteratorPositions,
			List<RGLValue> list) {
		this(id, list);
//		this.iteratorPosition = iteratorPositions;
	}
	
	public ListBackedBagValue(long id, List<RGLValue> list) {
		this(list);
		this.myId = id;
	}

	public ListBackedBagValue(List<RGLValue> list) {
		backingList = list;
	}

	public ListBackedBagValue() {
		this(ValueFactory.createBagBackingList());
	}

	/**
	 * Return the List<RGLValue> Object that is backing this bag implementation.
	 * Note that it must NOT be changed after an iterator() has been
	 * instantiated
	 * 
	 * 
	 * @return The backing list.
	 */
	public List<RGLValue> getBackingList() {
		return backingList;
	}

	/**
	 * Get an iterator over the bag; that is, an iterator over the backingList,
	 * as it is assumed to be completely filled.
	 */
	@Override
	public Iterator<RGLValue> iterator() {
		return backingList.iterator();
	}

	@Override
	protected Iterator<RGLValue> iteratorAt(int position) {
		return backingList.listIterator(position);
	}

	/**
	 * Get the size of this bag. That is, the size of the backingList, as it is
	 * assumed to be completely filled.
	 */
	@Override
	public int size() {
		return backingList.size();
	}

	@Override
	public void prepareForMultipleReadings() {
		// nothing to do
	}

	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new ListBackedBagBinding();
	}
	
	
}
