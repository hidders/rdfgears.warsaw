package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Map;

import nl.tudelft.rdfgears.engine.JenaRDFConstants;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.BooleanValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLNull;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.URIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.rgl.workflow.LazyRGLValue;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Converts a complex RGL value to an RDF Graph representation. Inputs can be bags/records/graphs.  
 *   
 */
public class RGLtoGraph extends SimplyTypedRGLFunction  {
	public static String value = "value";
	
	public RGLtoGraph(){
		requireInputType(value, new SuperType());
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		ValueToRDFVisitor converter = new ValueToRDFVisitor();
		RGLValue rglValue = inputRow.get(value);
		if (rglValue.isGraph())
			return rglValue; // it's already a graph
		
		rglValue.accept(converter);
		
		return ValueFactory.createGraphValue(converter.getModel());
	}
	
	@Override
	public void initialize(Map<String, String> config) {
		// nothing to do 
	}

	@Override
	public RGLType getOutputType() {
		return GraphType.getInstance();
	}

}




/**
 * A visitor that converts an RGL value to RDF and returns the resulting graph. 
 * 
 * Generates id's over and over. They could be cached IN the element if we'd implement a getResource() method. 
 * Or they may not be cached there, but it'd delegate the functionality there. Let's decide that later. 
 * @author Eric Feliksik
 *
 */
class ValueToRDFVisitor implements RGLValueVisitor {
	Model model = ValueFactory.createModel();
	
	public ValueToRDFVisitor(){
	}
	
	public Model getModel() {
		return model;
	}


	@Override
	public void visit(AbstractBagValue bag) {
		Resource node = (Resource) bag.getRDFNode();
		
		for (RGLValue element : bag ){
			model.add(node, JenaRDFConstants.bagElemProp, element.getRDFNode());
			element.accept(this);
		}
	}
	
	@Override
	public void visit(GraphValue graph) {
		model.add(graph.getModel());
	}
	
	@Override
	public void visit(BooleanValue bool) {
		/* nothing to do */
	}
	
	@Override
	public void visit(LiteralValue literal) {
		/* nothing to do */
	}

	@Override
	public void visit(AbstractRecordValue record) {
		Resource node = (Resource) record.getRDFNode();

		for (String fieldName : record.getRange()){
			Property fieldNameProp = model.createProperty(JenaRDFConstants.recordFieldBaseURI, fieldName);
			
			RGLValue element = record.get(fieldName);
			model.add(node, fieldNameProp, element.getRDFNode());
			element.accept(this);
		}
	}

	@Override
	public void visit(URIValue uri) {
		/* nothing to do */
	}


	@Override
	public void visit(RGLNull rglError) {
		/* nothing to do */
	}
	
	@Override
	public void visit(LazyRGLValue lazyValue) {
		// we cannot deal with this value, let the value evaluate itself and call this visitor 
		// again with right method signature for OO-dispatching
		lazyValue.accept(this);
	}

}
