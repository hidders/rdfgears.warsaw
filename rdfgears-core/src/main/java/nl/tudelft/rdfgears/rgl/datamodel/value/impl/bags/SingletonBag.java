package nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import nl.tudelft.rdfgears.engine.bindings.SingletonBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.OrderedBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

import com.sleepycat.bind.tuple.TupleBinding;


public class SingletonBag extends OrderedBagValue {
	final RGLValue value;
	public SingletonBag(RGLValue element){
		value = element;
	}
	
	public SingletonBag(Long id, Map<Long, Integer> iteratorPosition, RGLValue element) {
		this(element);
		this.myId = id;
//		this.iteratorPosition = iteratorPosition;
	}
	
	@Override
	public Iterator<RGLValue> iterator() {
		return new Iterator<RGLValue>(){
			private boolean done = false;
			
			@Override
			public boolean hasNext() {
				return !done;
			}

			@Override
			public RGLValue next() {
				if(done) 
					throw new NoSuchElementException();
				done = true;
				return value;
			}

			@Override
			public void remove() {	throw new UnsupportedOperationException();	}
			
		};
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public void prepareForMultipleReadings() {
		// nothing to do 
	}

	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new SingletonBagBinding();
	}
	
	
	
}
