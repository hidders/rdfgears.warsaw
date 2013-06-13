package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.WorkflowLoader;
import nl.tudelft.rdfgears.engine.bindings.FilteringBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.BooleanType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.BagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.util.RenewableIterator;
import nl.tudelft.rdfgears.util.row.SingleElementValueRow;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.sleepycat.bind.tuple.TupleBinding;

/**
 * A function that filters a bag with by testing all elements with the given
 * testingFunction.
 * 
 * The testingFunction must output a boolean value that states whether a value
 * from the inputbag qualifies for the output bag. It must have exactly one
 * input.
 * 
 * Not that is does filter out NULL values, as well as values for which the
 * testing function yields NULL.
 * 
 * @author Eric Feliksik
 * 
 */
public class FilterFunction extends AtomicRGLFunction {
	public static String inputName = "bag";
	private RGLFunction testingFunction;
	private String testingFunctionInputName;

	public FilterFunction() {
		requireInput(inputName);
		// caller must still initialize()
	}

	/**
	 * @param testingFunction
	 */
	public FilterFunction(RGLFunction testingFunction) {
		this();
		setTestingFunction(testingFunction);
	}

	private void setTestingFunction(RGLFunction testingFunction) {
		this.testingFunction = testingFunction;
	}

	public RGLFunction getTestingFunction() {
		return testingFunction;
	}

	@Override
	public void initialize(Map<String, String> config)
			throws WorkflowLoadingException {
		setTestingFunction(WorkflowLoader.instantiateFunction(config
				.get("implementation")));

		/**
		 * find the input name of the function; function will be called with the
		 * bag elements as only (named) argument
		 */
		List<String> requiredInputNames = getTestingFunction()
				.getRequiredInputNames();
		if (requiredInputNames.size() != 1) {
			throw new IllegalArgumentException(
					"The filterFunction must have exactly one inputname, not "
							+ requiredInputNames.size());
		}
		testingFunctionInputName = requiredInputNames.iterator().next();

	}

	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		RGLValue bag = inputRow.get(inputName);
		Engine.getLogger().info(bag);
		assert (bag != null) : "Something went wrong with typechecking";
		RenewablyIterableBag inputBag = (RenewablyIterableBag) bag.asBag();

		return new FilteringBagValue(inputBag, testingFunction);
		// return new FilteringBagValue(inputBag, testingFunction);
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes)
			throws FunctionTypingException {
		RGLType elemType = inputTypes.get(inputName);

		if (!(elemType instanceof BagType)) {
			throw new FunctionTypingException(
					"I require a bag on input with name '" + inputName + "'");
		}

		BagType bagType = (BagType) elemType;

		TypeRow inputTypeRow = new TypeRow();
		inputTypeRow.put(testingFunctionInputName, bagType.getElemType());
		RGLType testingOutputType;
		try {
			testingOutputType = testingFunction.getOutputType(inputTypeRow);
		} catch (WorkflowCheckingException e) {
			/*
			 * the filter function is not well typed. We will throw an error
			 * with a trace to this filter function, which *includes* an error
			 * with a trace to the problem IN the filter function.
			 */
			e.setProcessorAndFunction(null, testingFunction);
			String filterProblemMsg = e.getProblemDescription();
			throw new FunctionTypingException(filterProblemMsg
					+ "\n\nWhich is used as a filter-function: ");
		}

		if (!(testingOutputType instanceof BooleanType)) {
			throw new FunctionTypingException(
					"The "
							+ testingFunction.getRole()
							+ " '"
							+ testingFunction.getFullName()
							+ "' is used as filter-function and must therefore return a boolean type.");
		}

		return inputTypes.get(inputName); // type is just the type of the input
		// bag (but value may have less
		// elements)
	}

	public static FilteringBagValue createFilteringBag(long id,
			RenewablyIterableBag inputBag,
			MaterializingBag materializingBag, RGLFunction testingFunction, RGLValue nextOutput) {
		return new FilteringBagValue(id, inputBag, testingFunction,
				materializingBag, nextOutput);
	}

	public static FilteringBagValue createFilteringBag(long id,
			RenewablyIterableBag inputBag, RGLFunction testingFunction) {
		return new FilteringBagValue(id, inputBag, testingFunction);
	}

	/**
	 * A BagValue whose contents are defined by an inputBag and a
	 * filterfunction; Every element of the InputBag for which the
	 * filterFunction returns True is also in this Bag, with the same occurrence
	 * frequency.
	 * 
	 * The filterFunction must receive exactly one named argument; the name does
	 * not matter.
	 * 
	 * @author Eric Feliksik
	 * 
	 */
	public static class FilteringBagValue extends StreamingBagValue {
		private RGLValue nextOutput;
		private RGLFunction testingFunction;
		private RenewablyIterableBag inputBag; // the inputbag
		private String testingFunctionInputName;

		private FilteringBagValue(RenewablyIterableBag inputBag,
				RGLFunction testingFunction) {
			this.inputBag = inputBag;
			this.testingFunction = testingFunction;
			this.testingFunctionInputName = testingFunction
					.getRequiredInputNames().iterator().next();
		}

		private  FilteringBagValue(long id, RenewablyIterableBag inputBag,
				RGLFunction testingFunction) {
			this(inputBag, testingFunction);
			this.myId = id;
		}

		private FilteringBagValue(long id, RenewablyIterableBag inputBag,
				RGLFunction testingFunction, MaterializingBag materializingBag, RGLValue nextOutput) {
			this(id, inputBag, testingFunction);
			this.nextOutput = nextOutput;
			this.materializingBag = new MaterializingBag(materializingBag,
					getFirstStreamingBagIterator());
		}

		@Override
		protected Iterator<RGLValue> getStreamingBagIterator() {
			return new FilterIterator();
		}

		@Override
		protected Iterator<RGLValue> getFirstStreamingBagIterator() {
			return new FilterIterator(myId);
		}

		@Override
		public int size() {
			return BagValue.getNaiveSize(this);
		}

		class FilterIterator implements RenewableIterator<RGLValue> {
			private Iterator<RGLValue> inputIter;
			private boolean haveNext;

			public FilterIterator(long id) {
				inputIter = inputBag.renewableIterator(id);
				if (nextOutput == null) {
					findNext();
				} else {
					haveNext = true;
				}
			}

			public FilterIterator() {
				inputIter = inputBag.iterator();
				findNext();
			}

			@Override
			public RGLValue next() {
				if (nextOutput == null)
					throw new RuntimeException(
							"there is no next() value, you should first call hasNext() on iterator");

				RGLValue retVal = nextOutput;
				findNext();
				return retVal;
			}

			@Override
			public void remove() {
				assert (false);
			}

			/**
			 * find and set the nextOutput (possibly computed by another
			 * iterator over this bag), or set it to null if there is no
			 * qualifying next output in bag/iterator. Also set haveNext
			 */
			@Override
			public boolean hasNext() {
				return haveNext;
			}

			/**
			 * Find the next value, configuring 'haveNext' and nextOutput
			 */
			private void findNext() {
				while (inputIter.hasNext()) {
					nextOutput = inputIter.next();
					
					if (testValue(nextOutput)) {
						/* ok, this one qualifies */
						haveNext = true;
						return;
					}

				}
				haveNext = false;
			}

			/* test the given RGLValue on the configured selectionFunction */
			private boolean testValue(RGLValue value) {
				RGLValue booleanValue = testingFunction
						.execute(new SingleElementValueRow(
								testingFunctionInputName, value));
				return (!booleanValue.isNull())
						&& (booleanValue.asBoolean().isTrue());
			}
		}

		public TupleBinding<RGLValue> getBinding() {
			return new FilteringBagBinding(materializingBag, inputBag,
					testingFunction, nextOutput);
		}

	}
}
