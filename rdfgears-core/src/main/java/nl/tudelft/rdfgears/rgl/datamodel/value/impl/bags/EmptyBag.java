package nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags;

import java.util.Iterator;

import nl.tudelft.rdfgears.engine.bindings.EmptyBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.OrderedBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

import com.sleepycat.bind.tuple.TupleBinding;



/**
 * @author Tomasz Traczyk
 *
 */
public class EmptyBag extends OrderedBagValue {
	
	public EmptyBag() {}
	
	public EmptyBag(long id) {
		//myId = id;
	}

	@Override
	public Iterator<RGLValue> iterator() {
		/* return empty iterator */
		return new Iterator<RGLValue>(){
			@Override
			public boolean hasNext() {	return false; }

			@Override
			public RGLValue next() { throw new java.util.NoSuchElementException();	}

			@Override
			public void remove() {	throw new UnsupportedOperationException();	}
			
		};
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public void prepareForMultipleReadings() {
		// nothing to do
	}

	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new EmptyBagBinding();
	}

	@Override
	public boolean isSimple() {
		return true;
	}

}


