package nl.tudelft.rdfgears.util;

/**
 * Implementing this interface requires an object to be able to provide
 * {@link RenewableIterator}.
 * 
 * @author Tomasz Traczyk
 * 
 * @param <T>
 */
public interface RenewablyIterable<T> extends Iterable<T> {

	/**
	 * <p>
	 * Gives a <code>RenewableIterator</code> of the object.
	 * </p>
	 * 
	 * <p>
	 * Subsequent calls of the method with the same id should give iterators
	 * representing the same iteration process.
	 * </p>
	 * 
	 * <p>
	 * I.e. for certain <code>i</code>, if someone calls
	 * <code>renewableIterator(i)</code>, than iterates over <code>m</code>
	 * elements out of the total number of <code>n</code> elements in of the
	 * <code>RenewablyIterable</code> then the next call of
	 * <code>iterator(i)</code>, should return an iterator, that would only
	 * iterate over the other <code>(n - m + 1)</code> elements, but starting
	 * with the last element we've reached before.
	 * </p>
	 * 
	 * @param id
	 *            the id of iterator to be (re)created
	 * @return the iterator
	 */
	public RenewableIterator<T> renewableIterator(long id);
	
//	public RenewableIterator<T> previousRenewableIterator(long id);
}
