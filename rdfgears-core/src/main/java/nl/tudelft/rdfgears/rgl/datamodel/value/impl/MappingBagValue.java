package nl.tudelft.rdfgears.rgl.datamodel.value.impl;

import java.util.Iterator;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.value.BagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.workflow.AbstractValueRowIterator;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.LazyRGLValue;
import nl.tudelft.rdfgears.util.RenewableIterator;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * 
 * A MappingBagValue is a bag that takes an InputRow and a processor. The
 * processors has 0 or more iterating ports. For every iterating port 'p', the
 * InputRow's element 'p' is assumed to contain a Bag; For the other elements,
 * assume they are conceptually put in a singleton bag.
 * 
 * Now take the Cartesian bag product 'C' of all these bags. This is a Bag of
 * mappings (inputName=>element).
 * 
 * This bags contents are defined as the Mapping of C with the processor's
 * function. It is thus used to implement the iteration mechanism or RDF gears.
 * So if story is unclear, read the formal RGL definition.
 * 
 * @author Eric Feliksik
 * 
 */
public class MappingBagValue extends StreamingBagValue {

	class MappingIterator implements RenewableIterator<RGLValue> {
		Iterator<ValueRow> inputRowIter;

		public MappingIterator() {
			inputRowIter = ValueFactory.createValueRowIterator(inputRow, processor, false);
		}
		
		public MappingIterator(AbstractValueRowIterator inputRowIter) {
			this.inputRowIter = inputRowIter;
		}

		@Override
		public synchronized RGLValue next() {
			assert (hasNext());
			return transformValueRow(inputRowIter.next());
		}

		@Override
		public boolean hasNext() {
			return inputRowIter.hasNext();
		}

		@Override
		public void remove() {
			assert (false) : "not implemented";
		}

		private RGLValue transformValueRow(ValueRow input) {
			if (transformationFunction.isLazy()) {
				return new LazyRGLValue(transformationFunction, input);
			} else {
				return transformationFunction.execute(input);
			}
		}
	}

	private RGLFunction transformationFunction;
	private ValueRow inputRow;
	private FunctionProcessor processor;

	/**
	 * Instantiate a BagValue that ...
	 */
	public MappingBagValue(ValueRow inputRow, FunctionProcessor processor) {
		this.inputRow = inputRow;
		this.processor = processor;
		this.transformationFunction = processor.getFunction();
	}

	public MappingBagValue(long id, ValueRow inputRow,
			FunctionProcessor processor) {
		this(inputRow, processor);
		this.myId = id;
	}

	public MappingBagValue(long id, MaterializingBag materializingBag,
			ValueRow valueRow, FunctionProcessor processor) {
		this(id, valueRow, processor);
		this.materializingBag = new MaterializingBag(materializingBag,
				getFirstStreamingBagIterator());

	}
	
	@Override
	protected Iterator<RGLValue> getStreamingBagIterator() {
		return new MappingIterator();
	}

	@Override
	public int size() {
		return BagValue.getNaiveSize(this);
	}

}