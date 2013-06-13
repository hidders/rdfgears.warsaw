package nl.tudelft.rdfgears.util.row;

import java.util.Set;


/**
 * A generic row over objects. 
 * 
 * @author Eric Feliksik
 *
 * @param <E>
 */
public interface Row<E> {
	public E get(String k);
	
	public Set<String> getRange();
	
	public boolean equals(Object object);
}
