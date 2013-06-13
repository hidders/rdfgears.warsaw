package nl.tudelft.rdfgears.engine.bindings;

import java.util.Stack;

import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue.MaterializingBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize.CategoryBag;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class CategoryBagBinding extends StreamingBagBinding<CategoryBag> {

	private String category; /* the category for which I am a bag. */
	// private CategoryBagIterator instantiatedIterator = new
	// CategoryBagIterator();
	private boolean iteratorHasBeenRequested;
	private RGLValue inputBag;
	private RGLFunction categorizer;
	private Stack<RGLValue> iteratorsStack;
	private long recordId;

	@Override
	protected CategoryBag createMaterializingValue() {
		return BagCategorize.createCategoryBag(id, materializingBag, category,
				iteratorHasBeenRequested, inputBag, categorizer,
				iteratorsStack, recordId);
	}

	@Override
	protected CategoryBag createPureValue() {
		return BagCategorize.createCategoryBag(id, category,
				iteratorHasBeenRequested, inputBag, categorizer,
				iteratorsStack, recordId);
	}

	@Override
	protected void readMembers(TupleInput in) {
		category = in.readString();
		iteratorHasBeenRequested = in.readBoolean();
		inputBag = (RenewablyIterableBag) BindingFactory.createBinding(
				in.readString()).entryToObject(in).asBag();		
		categorizer = new RGLFunctionBinding().entryToObject(in);
		if (iteratorHasBeenRequested) {
			iteratorsStack = new Stack<RGLValue>();
			iteratorsStack.addAll(new RGLListBinding().entryToObject(in));
		}
		recordId = in.readLong();
	}

	@Override
	protected void writeMembers(TupleOutput out) {
		out.writeString(category);
		out.writeBoolean(iteratorHasBeenRequested);
		out.writeString(inputBag.getClass().getName());
		inputBag.getBinding().objectToEntry(inputBag, out);
//		Engine.getLogger().info(inputBag.getClass());
		new RGLFunctionBinding().objectToEntry(categorizer, out);
		if (iteratorHasBeenRequested) {
			new RGLListBinding().objectToEntry(iteratorsStack, out);
		}
		out.writeLong(recordId);
	}

	public CategoryBagBinding(String category,
			MaterializingBag materializingBag,
			boolean iteratorHasBeenRequested, RGLValue inputBag,
			RGLFunction categorizer, Stack<RGLValue> iteratorsStack,
			long recordId) {
		this.category = category;
		this.materializingBag = materializingBag;
		this.iteratorHasBeenRequested = iteratorHasBeenRequested;
		this.inputBag = inputBag;
		this.categorizer = categorizer;
		this.iteratorsStack = iteratorsStack;
		this.recordId = recordId;
	}

	public CategoryBagBinding() {

	}
}
