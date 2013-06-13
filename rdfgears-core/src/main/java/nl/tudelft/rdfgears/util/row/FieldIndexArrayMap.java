package nl.tudelft.rdfgears.util.row;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.rgl.exception.NoSuchFieldInRowException;
import nl.tudelft.rdfgears.util.ArrayIterator;

public class FieldIndexArrayMap implements FieldIndexMap {
	private String[] fields; 
	private Set<String> fieldNameSet; 
	
	private void checkNotNull(){
		for (int i=0; i<fields.length; i++){
			if (fields[i]==null)
				throw new IllegalArgumentException("Cannot construct a FieldIndexArrayMap with null-field. ");
		}
	}
	
	/**
	 * instantiate fieldMap with the fields given in the array 
	 * @param fieldList
	 */
	public FieldIndexArrayMap(String[] fieldList){
		assert(fieldList!=null);
		fields = fieldList; 
		checkNotNull();
	}
	
	/**
	 * Create fieldIndexMap from a collection. The collection should not contain duplicates or null. 
	 * @param requiredInputList
	 */
	public FieldIndexArrayMap(Collection<String> fieldList) {
		assert(fieldList!=null);
		fields = new String[fieldList.size()];
		int i=0;
		for (String s : fieldList ){
			fields[i++] = s;
		}
	}
	
	/** 
	 * get the array-index we should use for the given fieldName. 
	 * Returns -1 if the fieldName is not stored in the array we are describing. 
	 * @param fieldName
	 * @return
	 */
	public int getIndex(String fieldName){
		for (int i = 0; i<fields.length; i++){
			if (fields[i].equals(fieldName))
				return i;
		}
		throw new NoSuchFieldInRowException(fieldName); // also fires if fieldName == null
	}
	
	public Set<String> getFieldNameSet(){
		if (fieldNameSet==null){
			fieldNameSet = new ArrayBasedSet(); 
			
				/*
			new HashSet<String>();
			for (int i=0 ; i<fields.length; i++){
				fieldNameSet.add(fields[i]); // fixme: should be able to use an array-based set, faster? 
				Engine.getLogger().debug("creating set in a naive way... ");
			}
			*/
		}
		return fieldNameSet; 
	}
	
	public int size(){
		return fields.length;
	}
	
	
	class ArrayBasedSet implements Set<String>{
		private HashSet<String> hashSet;
		@Override
		public int size() {
			return fields.length;
		}

		@Override
		public boolean isEmpty() {
			return size()==0;
		}

		@Override
		public boolean contains(Object o) {
			return getHashSet().contains(o);
		}

		@Override
	    @SuppressWarnings("unchecked")
		public Iterator<String> iterator() {
			return new ArrayIterator(fields);
		}

		@Override
		public Object[] toArray() {
			return getHashSet().toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return (T[]) getHashSet().toArray(a);
		}

		@Override
		public boolean add(String e) {
			throw new UnsupportedOperationException("you should not modify the fields in a FieldIndexMap");
		}

		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException("you should not modify the fields in a FieldIndexMap");
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return getHashSet().containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends String> c) {
			throw new UnsupportedOperationException("you should not modify the fields in a FieldIndexMap");
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("you should not modify the fields in a FieldIndexMap");
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException("you should not modify the fields in a FieldIndexMap");
		}

		@Override
		public void clear() {
			throw new UnsupportedOperationException("you should not modify the fields in a FieldIndexMap");
		}
		
		private HashSet<String> getHashSet(){
			if (hashSet==null){
				hashSet = new HashSet<String>();
				for (String s : fields){
					hashSet.add(s);
				}
				Engine.getLogger().debug("creating set in a naive way... ");
			}
			return hashSet;
		}
	}
}
