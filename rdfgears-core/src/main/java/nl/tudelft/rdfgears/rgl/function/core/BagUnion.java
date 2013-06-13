package nl.tudelft.rdfgears.rgl.function.core;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.bindings.UnionBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.NNRCFunction;
import nl.tudelft.rdfgears.util.RenewableIterator;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.rdf.model.Model;
import com.sleepycat.bind.tuple.TupleBinding;

/**
 * NNRC Union operation for bags
 * 
 * Creates the union of two input bags 'bag1' and 'bag2'
 * 
 * @author Eric Feliksik
 * 
 */
public class BagUnion extends NNRCFunction {
	public static final String bag1 = "bag1";
	public static final String bag2 = "bag2";

	private boolean mergesGraphs = false; // if true, we merge graphs.

	public BagUnion() {
		this.requireInput(bag1);
		this.requireInput(bag2);
	}

	@Override
	public void initialize(Map<String, String> config) {
		/* nothing to be done */
	}

	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		RGLValue bag1val = inputRow.get(bag1);
		RGLValue bag2val = inputRow.get(bag2);

		if (bag1val.isNull() || bag2val.isNull()) {
			return bag1val; // return the absent value
		}

		if (mergesGraphs) {
			Model m = ValueFactory.createModel();
			m.add(bag1val.asGraph().getModel());
			m.add(bag2val.asGraph().getModel());
			return ValueFactory.createGraphValue(m);
		}

		return new UnionBagValue((RenewablyIterableBag) bag1val.asBag(),
				(RenewablyIterableBag) bag2val.asBag());
		// return new UnionBagValue((RenewablyIterableBagValue) bag1val.asBag(),
		// (RenewablyIterableBagValue) bag2val.asBag());
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes)
			throws FunctionTypingException {
		RGLType type1 = inputTypes.get(bag1);
		RGLType type2 = inputTypes.get(bag2);

		boolean bothBags = type1.isBagType() && type2.isBagType();
		boolean bothGraphs = type1.isGraphType() && type2.isGraphType();

		if (bothBags) {
			RGLType elem1 = ((BagType) type1).getElemType();
			RGLType elem2 = ((BagType) type2).getElemType();

			if (!elem1.equals(elem2)) {
				throw new FunctionTypingException("bag2", type1, type2);
			}
			return type1; /* inputs ok, thus type1==type2==outputtype */
		} else if (bothGraphs) {
			mergesGraphs = true;
			return type1;
		} else {
			if (!(type1.isBagType())) {
				throw new FunctionTypingException(bag1, BagType
						.getInstance(new SuperType()), type1);
			}
			throw new FunctionTypingException(bag2, BagType
					.getInstance(new SuperType()), type1);

		}

	}

	public static UnionBagValue createUnionBag(long id,
			Map<Long, Integer> iteratorPosition, RenewablyIterableBag bag1,
			RenewablyIterableBag bag2) {
		return new UnionBagValue(id, iteratorPosition, bag1, bag2);
	}

	public static UnionBagValue createUnionBag(long id,
			Map<Long, Integer> iteratorPosition,
			MaterializingBag materializingBag, RenewablyIterableBag bag1,
			RenewablyIterableBag bag2) {
		return new UnionBagValue(id, iteratorPosition, materializingBag, bag1,
				bag2);
	}

	/**
	 * A FlattenedBagValue is defined by a Bag of Bags. When iterating, it
	 * iterates over all the internal bags. So it does not materialize anything,
	 * and iterates the united bags every time this bag is iterated.
	 * 
	 * @author Eric Feliksik
	 * 
	 */
	public static class UnionBagValue extends StreamingBagValue {
		RenewablyIterableBag bag1, bag2;

		private UnionBagValue(RenewablyIterableBag bag1,
				RenewablyIterableBag bag2) {
			this.bag1 = bag1;
			this.bag2 = bag2;
		}

		private UnionBagValue(long id, Map<Long, Integer> iteratorPositions,
				RenewablyIterableBag bag1, RenewablyIterableBag bag2) {
			this(bag1, bag2);
			this.myId = id;
//			this.iteratorPosition = iteratorPositions;
		}

		private UnionBagValue(long id, Map<Long, Integer> iteratorPositions,
				MaterializingBag materializingBag, RenewablyIterableBag bag1,
				RenewablyIterableBag bag2) {
			this(id, iteratorPositions, bag1, bag2);
			Iterator<RGLValue> it = getFirstStreamingBagIterator();
//			it.next();// we need to skip the last visited element, since
//			// UnionBagValue doesn't read anything in advance
			this.materializingBag = new MaterializingBag(materializingBag, it);
		}

		@Override
		protected Iterator<RGLValue> getStreamingBagIterator() {
			return new UnionBagIterator();
		}

		@Override
		protected Iterator<RGLValue> getFirstStreamingBagIterator() {
			return new UnionBagIterator(myId);
		}

		/**
		 * Dont really flatten the bags, just calculate what the result would
		 * look like.
		 */
		@Override
		public int size() {
			return bag1.size() + bag2.size(); // may be costly evaluation, do we
			// want to cache this?
		}

		class UnionBagIterator implements RenewableIterator<RGLValue> {
			Iterator<RGLValue> currentIter;// = bag1.iterator();
			boolean iteratingBag2 = false; // if true, currentIter is iterator
			// over
			// bag2
			boolean haveNext = true;

			public UnionBagIterator() {
				currentIter = bag1.iterator();
				setHaveNext();
			}

			public UnionBagIterator(long id) {
				currentIter = bag1.renewableIterator(id);
				if (!currentIter.hasNext())
					currentIter = bag2.renewableIterator(id);
				setHaveNext();
			}

			@Override
			public RGLValue next() {
				if (!haveNext)
					throw new NoSuchElementException();
				RGLValue res = currentIter.next();
				setHaveNext();
				return res;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean hasNext() {
				return haveNext;
			}

			private void setHaveNext() {
				haveNext = currentIter.hasNext();
				if (!haveNext && !iteratingBag2) {
					iteratingBag2 = true;
					currentIter = bag2.iterator();
					haveNext = currentIter.hasNext();
				}
			}
		}

		@Override
		public TupleBinding<RGLValue> getBinding() {
			return new UnionBagBinding(materializingBag, bag1, bag2);
		}

	}
}
