package nl.tudelft.rdfgears.rgl.datamodel.value.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.bindings.MemoryURIBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.URIValue;
import nl.tudelft.rdfgears.rgl.exception.ComparisonNotDefinedException;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.sleepycat.bind.tuple.TupleBinding;

public class MemoryURIValue extends URIValue {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7693375149701159596L;

	public MemoryURIValue(String uri) {
		super(Engine.getDefaultModel().createResource(uri));
	}

	public MemoryURIValue(RDFNode uriNode) {
		super(uriNode);

		if (!uriNode.isURIResource()) {
			throw new RuntimeException(
					"Need a Node_URI; cannot instantiate URIValue with Node of type "
							+ uriNode.getClass());
		}
	}

	/**
	 * Override getRDFNode, do not create some Jena-resource with our value-id,
	 * but instead just return our Jena equivalent URI
	 */
	@Override
	public RDFNode getRDFNode() {
		assert (node != null);
		return node;
	}

	public int compareTo(RGLValue v2) {
		if (v2.isURI()) { // most likely
			return uriString().compareTo(v2.asURI().uriString());
		} else if (v2.isNull()) {
			return 1; // larger than all nulls
		} else if (v2.isLiteral()) {
			return -1; // we are bigger than null values / booleans
		} else {
			throw new ComparisonNotDefinedException(this, v2);
		}
	}

	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new MemoryURIBinding();
	}
	
}
