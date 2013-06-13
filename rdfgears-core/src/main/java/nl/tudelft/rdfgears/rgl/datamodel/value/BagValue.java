package nl.tudelft.rdfgears.rgl.datamodel.value;

import java.util.Iterator;

import nl.tudelft.rdfgears.engine.bindings.NaiveBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;
import nl.tudelft.rdfgears.rgl.exception.ComparisonNotDefinedException;

import com.sleepycat.bind.tuple.TupleBinding;

public abstract class BagValue extends DeterminedRGLValue implements
		AbstractBagValue {

	public abstract Iterator<RGLValue> iterator();

	/**
	 * Get the size of this bag. Note that this MAY (re)iterate the bag, which
	 * MAY be expensive if it is not Materialized.
	 * 
	 * @return
	 */
	public abstract int size();

	@Override
	public AbstractBagValue asBag() {
		return this;
	}

	@Override
	public boolean isBag() {
		return true;
	}

	@Override
	public boolean isGraph() {
		return false; // may become true iff bag contains (s,p,o) records -
		// correction: now, we need explicit casting for now
	}

	public void accept(RGLValueVisitor visitor) {
		visitor.visit(this);
	}

	/**
	 * A bag may decide itself how it likes to perform multiple iterations. A
	 * MaterializingBag may not need to do anything, a streaming bag may decide
	 * to apply a MaterializingBag to itself
	 * 
	 */
	@Override
	public abstract void prepareForMultipleReadings();

	/**
	 * A very naive function that gets bagsize by iterating over all elements.
	 * Bag implementations that are smarter should override this
	 * 
	 * @param bag
	 * @return
	 */
	public static int getNaiveSize(AbstractBagValue bag) {
		int size = 0;
		for (RGLValue val : bag) {
			System.out.println(val);
			size++;
			System.out.println(size);
		}
		return size;
	}

	public int compareTo(RGLValue v2) {
		// but may be implemented by subclass. It must be determined what is
		// comparable, i think it'd be elegant to make as much as possible
		// comparable.
		throw new ComparisonNotDefinedException(this, v2);
	}

	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new NaiveBagBinding();
	}

}
