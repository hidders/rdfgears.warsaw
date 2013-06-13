package nl.tudelft.rdfgears.engine.diskvalues;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.bindings.RGLListBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;

/**
 * DiskList is an implementation of List that can store its contents on HDD
 * using BerkeleyDB Base API as a back-end.
 * 
 * @author Tomasz Traczyk
 * 
 * @param <RGLValue>
 */
public class DiskList implements List<RGLValue>, Serializable {
	
	/**
	 * DiskListIterator is an implementation of Iterator for DiskList. Each
	 * instance of this class have its own cache of size defined in its DiskList
	 * instance.
	 * 
	 * It can also use its DiskList caches, but won't modify them.
	 * 
	 * DiskListIterator will fetch any requested elements from DB if they can't
	 * be found in any of caches.
	 * 
	 * @author Tomasz Traczyk
	 * 
	 */
	private class DiskListIterator implements ListIterator<RGLValue> {
		private int nextIndexPointer;
		private List<RGLValue> iteratorCache = new ArrayList<RGLValue>();
		private int iteratorOffset;

		public DiskListIterator() {
			nextIndexPointer = 0;
			iteratorOffset = -cacheSize;
		}

		public DiskListIterator(int position) {
			nextIndexPointer = position;
			iteratorOffset = -cacheSize;
		}

		@Override
		public boolean hasNext() {
			if (nextIndexPointer >= size)
				return false;
			else
				return true;
		}

		@Override
		public synchronized RGLValue next() {
			rangeCheck(nextIndexPointer);
			RGLValue ret;
			if ((ret = tryToGet(nextIndexPointer, iteratorOffset, iteratorCache)) != null)
				;
			else if ((ret = tryToGet(nextIndexPointer, addOffset, addCache)) != null)
				;
			else if ((ret = tryToGet(nextIndexPointer, getOffset, getCache)) != null)
				;
			else {
				iteratorOffset = loadCache(nextIndexPointer, iteratorCache);
				ret = iteratorCache.get(nextIndexPointer - iteratorOffset);
			}

			nextIndexPointer++;
			return ret;
		}

		@Override
		public void remove() {
			assert (false) : "Not implemented";
		}

		@Override
		public void add(RGLValue arg0) {
			assert (false) : "Not implemented";
		}

		@Override
		public boolean hasPrevious() {
			return nextIndexPointer - 1 > 0;
		}

		@Override
		public int nextIndex() {
			return nextIndexPointer;
		}

		@Override
		public RGLValue previous() {
			assert (false) : "Not implemented";
			return null;
		}

		@Override
		public int previousIndex() {
			return nextIndexPointer - 1;
		}

		@Override
		public void set(RGLValue e) {
			assert (false) : "Not implemented";
		}

	}

	private int size = 0;

	/* the maximum size of each cache */
	private int cacheSize;
	private String databaseName;

	private List<RGLValue> addCache = new ArrayList<RGLValue>();
	private int addOffset = 0;

	private List<RGLValue> getCache = new ArrayList<RGLValue>();
	private int getOffset;

	private Database listDatabase;
	private TupleBinding<List<RGLValue>> dataBinding;

	/**
	 * Create a DiskList with default cache size of 1000 instances of type E.
	 * 
	 * @throws Exception
	 */
	public DiskList() {
		this(1000);
	}

	/**
	 * Create a DiskList with specified cache size The constructor establishes a
	 * database for storing lists contensts and opens a connection with it. This
	 * might be postponed until needed, in a lazy manner, i.e. moved to
	 * dumpCache method, but it's not sure which approach is better for usual
	 * workflows.
	 * 
	 * @param cacheSize
	 *            cache size
	 * @throws DatabaseException
	 */
	public DiskList(int cacheSize) throws DatabaseException {
		this(0, cacheSize, "list" + System.currentTimeMillis());
	}

	public DiskList(int size, int cacheSize, String databaseName)
			throws DatabaseException {
		this.size = size;
		this.cacheSize = cacheSize;
		this.databaseName = databaseName;
		getOffset = -cacheSize; // initially there's no getCache;
		addOffset = Math.max(0, size - (size % cacheSize) - cacheSize);
		listDatabase = DatabaseManager.openListDatabase(databaseName);
		dataBinding = new RGLListBinding();
		if (size != 0)
			loadCache(addOffset, addCache);
	}

	@Override
	public boolean add(RGLValue value) {
		if (addCache.size() < cacheSize)
			addCache.add(value);
		else {
			dumpCache();
			addCache.add(value);
			addOffset += cacheSize;
		}
		size++;
		return true;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* dump the current addCache to HDD */
	private void dumpCache() {
		Stoper.diskTime -= System.currentTimeMillis();
		DatabaseEntry key = DatabaseManager.int2entry(addOffset);
		DatabaseEntry data = new DatabaseEntry();
		dataBinding.objectToEntry(addCache, data);
		listDatabase.put(null, key, data);
		Engine.getLogger().info("Dumping cache for " + databaseName + " on key " + addOffset + ", cacheSize: " + addCache.size() + " database.count: " + listDatabase.count());
		addCache.clear();

		Stoper.diskTime += System.currentTimeMillis();
	}

	public void prepareForStoring() {
		if (addCache.size() != 0) {
			dumpCache();
		}
		addOffset += cacheSize;
	}

	@Override
	public RGLValue get(int index) {
		rangeCheck(index);
		RGLValue ret;
		if ((ret = tryToGet(index, getOffset, getCache)) != null) {
			return ret;
		} else if ((ret = tryToGet(index, addOffset, addCache)) != null) {
			return ret;
		} else {
			getOffset = loadCache(index, getCache);
			return getCache.get(index - getOffset);
		}
	}

	public int getCacheSize() {
		return cacheSize;
	}

	/* probably shouldn't be implemented - it would be very costly */
	@Override
	public int indexOf(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return (size == 0);
	}

	@Override
	public Iterator<RGLValue> iterator() {
		return new DiskListIterator();
	}

	@Override
	public int lastIndexOf(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<RGLValue> listIterator() {
		return new DiskListIterator();
	}

	@Override
	public ListIterator<RGLValue> listIterator(int position) {
		return new DiskListIterator(position);
	}

	private int loadCache(int index, List<RGLValue> cache) {
		Stoper.diskTime -= System.currentTimeMillis();
		int offset = index - index % cacheSize;
		DatabaseEntry key = DatabaseManager.int2entry(offset);
		Engine.getLogger().info("Loading cache for " + databaseName + " on key " + offset + " to get index " + index + "...");

		DatabaseEntry data = new DatabaseEntry();
		listDatabase.get(null, key, data, LockMode.DEFAULT);
		cache.clear();
		cache.addAll((ArrayList<RGLValue>) dataBinding.entryToObject(data));
		Engine.getLogger().info("\t...size: " + cache.size());
		Engine.getLogger().info("\t...dataBaseSize: " + listDatabase.count());
		Stoper.diskTime += System.currentTimeMillis();
		return offset;
	}

	private void rangeCheck(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException(String.format(
					"Index: %d, Size: %d", index, size));
		}
	}

	@Override
	public RGLValue remove(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public RGLValue set(int arg0, RGLValue arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return size;
	}

	@Override
	public List<RGLValue> subList(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* check if desired index is in a cache */
	private RGLValue tryToGet(int index, int offset, List<RGLValue> cache) {
		if (index >= offset && index < offset + cache.size()) {
			return cache.get(index - offset);
		} else
			return null;
	}

	@Override
	public void add(int arg0, RGLValue arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addAll(Collection<? extends RGLValue> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends RGLValue> arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public int getAddOffset() {
		return addOffset;
	}

}