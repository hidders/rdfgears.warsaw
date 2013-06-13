package nl.tudelft.rdfgears.engine.valuefactory;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.rdfgears.engine.diskvalues.DiskList;
import nl.tudelft.rdfgears.rgl.datamodel.value.BooleanValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RDFValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLNull;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.URIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryGraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryLiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryURIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.ModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.SingletonBag;
import nl.tudelft.rdfgears.rgl.workflow.AbstractValueRowIterator;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.MemoryValueRowIterator;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class MemoryValueFactory implements ValueFactoryIface {

	private RGLNull error = new RGLNull();

	@Override
	public Model createModel() {
		// FIXME: learn to better understand the model factory concept
		// does this give a memory model by definition?
		return ModelFactory.createDefaultModel();
	}

	@Override
	public GraphValue createGraphValue(Model model) {
		return new MemoryGraphValue(model);
	}

	@Override
	public List<RGLValue> createBagBackingList() {
		return new ArrayList<RGLValue>();
	}

	@Override
	public AbstractRecordValue createRecordValue(ValueRow row) {
		return new MemoryRecordValue(row);
	}

	@Override
	public AbstractModifiableRecord createModifiableRecordValue(
			FieldIndexMap map) {
		return new ModifiableRecord(map);
	}

	@Override
	public RDFValue createRDFValue(RDFNode node) {
		if (node.isLiteral()) {
			return MemoryLiteralValue.createLiteral(node.asLiteral());
		} else if (node.isURIResource()) {
			return new MemoryURIValue(node);
		}

		assert (false) : "Fixme, not implemented";
		return null;
	}

	@Override
	public LiteralValue createLiteralTyped(Object value, RDFDatatype dtype) {
		return MemoryLiteralValue.createLiteralTyped(value, dtype);
	}

	@Override
	public LiteralValue createLiteralDouble(double d) {
		return MemoryLiteralValue.createLiteralTyped(new Double(d),
				XSDDatatype.XSDdouble);
	}

	@Override
	public URIValue createURI(String uri) {
		return new MemoryURIValue(uri);
	}

	//
	// public static RGLValue createFalse(){
	// return BooleanValueImpl.getFalseInstance();
	// }
	// public static RGLValue createTrue(){
	// return BooleanValueImpl.getTrueInstance();
	// }
	//	

	@Override
	public LiteralValue createLiteralPlain(String str, String language) {
		return MemoryLiteralValue.createPlainLiteral(str, language);
	}

	@Override
	public AbstractBagValue createBagSingleton(RGLValue value) {
		return new SingletonBag(value);
	}

	@Override
	public RGLValue createNull(String string) {
		if (string == null)
			return error;

		return new RGLNull(string);
	}

	public BooleanValue createFalse() {
		return falseValue;
	}

	public BooleanValue createTrue() {
		return trueValue;
	}

	@Override
	public AbstractBagValue createBagEmpty() {
		return emptyBag;
	}

	@Override
	public boolean isDiskBased() {
		return false;
	}

	@Override
	public RGLValue registerValue(RGLValue value) {
		return value;
	}

	@Override
	public AbstractValueRowIterator createValueRowIterator(
			ValueRow originalInputs, FunctionProcessor processor,
			boolean recycleReturnRow) {
		return new MemoryValueRowIterator(originalInputs, processor, recycleReturnRow);
	}
}
