package nl.tudelft.rdfgears.rgl.function.core;

import java.util.Iterator;
import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.bindings.FlattenedBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.NNRCFunction;
import nl.tudelft.rdfgears.util.RenewableIterator;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.rdf.model.Model;
import com.sleepycat.bind.tuple.TupleBinding;

/**
 * NNRC Flatten operation for bags.
 * 
 * Requires that a bag contains bags of the same type, and generates a single
 * bag containing the union of all inner bags.
 * 
 * If an inner bag IS an NON-value, it cannot be iterated and that that
 * NON-value occurs once in the result bag.
 * 
 * If an inner bag CONTAINS a non-value, those are iterated as normally and
 * included in the result bag.
 * 
 * @author Eric Feliksik
 * 
 */
public class BagFlatten extends NNRCFunction {
	public static final String bag = "bag";

	private boolean mergesGraphs = false; // if true, we merge graphs.

	public BagFlatten() {
		this.requireInput(bag);
	}

	@Override
	public void initialize(Map<String, String> config) {
		/* nothing to be done */
	}

	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		RGLValue bagVal = inputRow.get(bag);
		if (bagVal.isNull()) {
			return bagVal;
		}
		RenewablyIterableBag outerBag = (RenewablyIterableBag) inputRow
				.get(bag).asBag();
		if (!mergesGraphs) {
			return new FlattenedBagValue(outerBag);
		} else {
			Model newModel = ValueFactory.createModel();
			/* we must merge the graphs */
			for (RGLValue val : outerBag) {
				if (val.isNull()) {
					/*
					 * return null value, completely failing the flattinging.
					 * Otherwise the error will not be noted and RDF queries may
					 * return no results without any error, driving the user
					 * crazy
					 */
					return val;
				} else {
					/* ok, data */
					newModel.add(val.asGraph().getModel());
				}

			}

			return ValueFactory.createGraphValue(newModel);
		}
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes)
			throws FunctionTypingException {
		RGLType bagOfBags = BagType.getInstance(BagType
				.getInstance(new SuperType()));
		RGLType bagOfGraphs = BagType.getInstance(GraphType.getInstance());

		RGLType type = inputTypes.get(bag);
		if (type.isSubtypeOf(bagOfBags)) {
			mergesGraphs = false;
			return ((BagType) type).getElemType();
		} else if (type.isSubtypeOf(bagOfGraphs)) {
			/* we can merge graphs, too! */
			mergesGraphs = true;
			return GraphType.getInstance();
		}

		throw new FunctionTypingException(bag, bagOfBags, inputTypes.get(bag));
	}

	public static FlattenedBagValue createFlattenedBagValue(long id,
			Map<Long, Integer> iteratorPosition,
			MaterializingBag materializingBag,
			RenewablyIterableBag outerBag) {
		return new FlattenedBagValue(id, iteratorPosition, materializingBag,
				outerBag);
	}

	public static FlattenedBagValue createFlattenedBagValue(long id,
			Map<Long, Integer> iteratorPosition,
			RenewablyIterableBag outerBag) {
		return new FlattenedBagValue(id, iteratorPosition, outerBag);
	}

	/**
	 * A FlattenedBagValue is defined by a Bag of Bags. When iterating, it
	 * iterates over all the internal bags. So it does not materialize anything.
	 * 
	 * If it is iterated multiple times, it may benefit from caching.
	 * 
	 * @author Eric Feliksik
	 * 
	 */
	public static class FlattenedBagValue extends StreamingBagValue {
		RenewablyIterableBag outerBag;

		private FlattenedBagValue(RenewablyIterableBag bagOfBags) {
			outerBag = bagOfBags;
		}

		private FlattenedBagValue(long id, Map<Long, Integer> iteratorPosition,
				RenewablyIterableBag outerBag) {
			this(outerBag);
			this.myId = id;
//			this.iteratorPosition = iteratorPosition;
		}

		private FlattenedBagValue(long id, Map<Long, Integer> iteratorPosition,
				MaterializingBag materializingBag,
				RenewablyIterableBag outerBag) {
			this(id, iteratorPosition, outerBag);
			this.materializingBag = materializingBag;
		}

		@Override
		protected Iterator<RGLValue> getStreamingBagIterator() {
			return new FlattenedBagIterator(myId);
		}

		@Override
		public TupleBinding<RGLValue> getBinding() {
			return new FlattenedBagBinding(outerBag);
		}

		/**
		 * Dont really flatten the bags, just calculate what the result would
		 * look like.
		 */
		@Override
		public int size() {
			int totalSize = 0;
			for (RGLValue innerBag : outerBag) {
				totalSize += innerBag.asBag().size();
			}
			return totalSize;
		}

		class FlattenedBagIterator implements RenewableIterator<RGLValue> {
			private Iterator<RGLValue> outerBagIter = outerBag.iterator();
			private Iterator<RGLValue> innerBagIter;
			private boolean haveNext = false;

			FlattenedBagIterator(long id) {
				if (!outerBagIter.hasNext()) {
					haveNext = false;
				} else {
					setInnerBagIter(outerBagIter.next());
					setHaveNext();
				}
			}

			@Override
			public boolean hasNext() {
				return haveNext;
			}

			private void setHaveNext() {
				while (true) {
					if (innerBagIter.hasNext()) {
						haveNext = true;
						break;
					} else {
						if (outerBagIter.hasNext()) {
							setInnerBagIter(outerBagIter.next());
						} else {
							haveNext = false;
							break;
						}
					}
				}
			}

			@Override
			public RGLValue next() {
				if (!haveNext)
					throw new java.util.NoSuchElementException();

				assert (innerBagIter.hasNext());
				RGLValue res = innerBagIter.next();
				setHaveNext();
				return res;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			/**
			 * Set the inner bag iterator based on the given innerBag. applies a
			 * small hack : put in a singleton iterator with the error, so it
			 * seems that we are gracefully iterating over the inner bag.
			 * 
			 * @param innerBag
			 */
			private void setInnerBagIter(RGLValue bagOrError) {
				if (bagOrError.isNull()) {
					/* quick way to create single-error-Iterator */
					innerBagIter = ValueFactory.createBagSingleton(bagOrError)
							.iterator();
				} else {
					innerBagIter = bagOrError.asBag().iterator();
				}

			}

		}
	}

}
