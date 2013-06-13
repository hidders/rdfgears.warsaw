package nl.tudelft.rdfgears.rgl.datamodel.value.visitors;

import nl.tudelft.rdfgears.rgl.datamodel.value.BooleanValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLNull;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.URIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.workflow.LazyRGLValue;


/**
 * A visitor that just visites all values, recursively, thus evaluating them.  
 * 
 * @author Eric Feliksik
 *
 */
public class ValueEvaluator implements RGLValueVisitor {
	
	public ValueEvaluator(){
	}
	
	@Override
	public void visit(AbstractBagValue bag) {
		for (RGLValue val : bag){
			val.accept(this);
		}
	}
	
	@Override
	public void visit(GraphValue graph) {
		// nothing to evaluate
	}
	
	@Override
	public void visit(BooleanValue bool) {
		// nothing to evaluate
	}
	
	@Override
	public void visit(LiteralValue literal) {
		// nothing to evaluate

	}

	@Override
	public void visit(AbstractRecordValue record) {
		
		for (String fieldName : record.getRange()){
			record.get(fieldName).accept(this);
		}
	}

	@Override
	public void visit(URIValue uri) {
		// nothing to evaluate
	}


	@Override
	public void visit(RGLNull rglError) {
		// nothing to evaluate
	}
	
	@Override
	public void visit(LazyRGLValue lazyValue) {
		// we cannot deal with this value, let the value evaluate itself and call this visitor 
		// again with right method signature for OO-dispatching
		lazyValue.accept(this);
	}



}
