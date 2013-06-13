package nl.tudelft.rdfgears.rgl.datamodel.value;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.JenaRDFConstants;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class RGLNull extends DeterminedRGLValue {
	private String errorMessage = "NULL";
	private static Resource nullResource = Engine.getDefaultModel().createResource(JenaRDFConstants.valueBaseURI+"null"); 
		
	public RGLNull(String errorMessage){
		this.errorMessage = errorMessage;
	}
	
	public RGLNull(){
	}
	
	@Override
	public void accept(RGLValueVisitor visitor){
		visitor.visit(this);
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public boolean isNull(){
		return true;
	}
	
	@Override
	public RGLNull asNull(){
		return this;
	}

	@Override
	public RDFNode getRDFNode(){
		return nullResource;
	}
	
	/**
	 * For order rules, see http://www.w3.org/TR/sparql11-query/#modOrderBy
	 * NULL value is treated as 'not bound'. 
	 */
	public int compareTo(RGLValue v2) {
		if (v2.isNull())
			return 0; // all null values are equal
		else 
			return -1; // all null's are smaller than actual values
		
	}

	@Override
	public void prepareForMultipleReadings() {
		/* nothing to do */
	}

}
