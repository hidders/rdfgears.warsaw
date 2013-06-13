package nl.tudelft.rdfgears.rgl.datamodel.value.visitors;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.rgl.datamodel.value.BooleanValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLNull;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.URIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize.CategoryBag;
import nl.tudelft.rdfgears.rgl.workflow.LazyRGLValue;

public class IdWrappingVisitor implements RGLValueVisitor {

	private RGLValue result;
	
	public void visit(CategoryBag bag) {
		Engine.getLogger().info(bag);
		result = BagCategorize.createIdCategoryBag(bag);
	}
	
	@Override
	public void visit(AbstractBagValue bag) {
		result = new IdRenewablyIterableBag(bag);
	}

	@Override
	public void visit(GraphValue graph) {
		result = graph;
	}

	@Override
	public void visit(BooleanValue bool) {
		result = bool;
	}

	@Override
	public void visit(LiteralValue literal) {
		result = literal;
	}

	@Override
	public void visit(AbstractRecordValue record) {
		result = new IdRecordValue(record);
	}

	@Override
	public void visit(URIValue uri) {
		result = uri;
	}

	@Override
	public void visit(LazyRGLValue lazyValue) {
		result = new IdRGLValue(lazyValue);
	}

	@Override
	public void visit(RGLNull rglError) {
		result = rglError;
	}
	
	public RGLValue getValue() {
		return result;
	}

}
