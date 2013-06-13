package nl.tudelft.rdfgears.util.row;

import java.util.HashMap;
import java.util.Set;



public class HashRow<E> extends AbstractRow<E> {
	/* hashmap based implementation of a row */
	private HashMap<String, E> map = new HashMap<String, E>(); 
	
	@Override
	public E get(String k) {
		return map.get(k);
	} 

	public void put(String key, E element) {
		map.put(key, element);
	}

	@Override
	public Set<String> getRange() {
		return map.keySet();
	}

}
