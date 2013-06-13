package nl.tudelft.rdfgears.rgl.datamodel.value.idvalues;

import nl.tudelft.rdfgears.engine.bindings.idvalues.IdRGLBinding;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.value.BooleanValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RDFValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.URIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.sleepycat.bind.tuple.TupleBinding;

public class IdRGLValue implements RGLValue {

	protected long id;
	
	public IdRGLValue(long id) {
		this.id = id;
	}
	
	public IdRGLValue(RGLValue value) {
		this.id = value.getId();
	}

	public RGLValue fetch() {
		return ValueManager.fetchValue(id);
	}

	@Override
	public void accept(RGLValueVisitor visitor) {
		fetch().accept(visitor);
	}

	@Override
	public AbstractBagValue asBag() {
		return fetch().asBag();
	}

	@Override
	public BooleanValue asBoolean() {
		return fetch().asBoolean();
	}

	@Override
	public GraphValue asGraph() {
		return fetch().asGraph();
	}

	@Override
	public LiteralValue asLiteral() {
		return fetch().asLiteral();
	}

	@Override
	public RDFValue asRDFValue() {
		return fetch().asRDFValue();
	}

	@Override
	public AbstractRecordValue asRecord() {
		return fetch().asRecord();
	}

	@Override
	public URIValue asURI() {
		return fetch().asURI();
	}

	@Override
	public int compareTo(RGLValue value) {
		return fetch().compareTo(value);
	}

	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new IdRGLBinding();
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public RDFNode getRDFNode() {
		return fetch().getRDFNode();
	}

	@Override
	public boolean isBag() {
		return fetch().isBag();
	}

	@Override
	public boolean isBoolean() {
		return fetch().isBoolean();
	}

	@Override
	public boolean isGraph() {
		return fetch().isGraph();
	}

	@Override
	public boolean isLiteral() {
		return fetch().isLiteral();
	}

	@Override
	public boolean isNull() {			
		return fetch().isNull();
	}

	@Override
	public boolean isRDFValue() {
		return fetch().isRDFValue();
	}

	@Override
	public boolean isRecord() {
		return fetch().isRecord();
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public boolean isURI() {
		return fetch().isURI();
	}

	@Override
	public void prepareForMultipleReadings() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public String toString() {
		return fetch().toString();
	}
	
}
