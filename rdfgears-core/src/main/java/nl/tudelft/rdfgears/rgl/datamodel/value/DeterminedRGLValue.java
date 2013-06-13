package nl.tudelft.rdfgears.rgl.datamodel.value;

import java.io.Serializable;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.JenaRDFConstants;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.bindings.MemoryBinding;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.sleepycat.bind.tuple.TupleBinding;
/**
 * A DeterminedRGLValue is a calculated value. 
 * It is abstract and is designed to provide an error-throwing implementation
 * for the compulsory interface functions. A subimplementation can then implement only the required functions.  
 * 
 * @author Eric Feliksik
 *
 */
public abstract class DeterminedRGLValue implements RGLValue, Serializable {
	protected long myId; 
	public long getId(){ return myId; }
	
	/**
	 * get the RDF Node associated with this primitive value. 
	 *   
	 * For RDFValue this is the underlying RDFNode implementation. 
	 * For GraphValue, it is a unique random RDFNode URI that identifies the graph. 
	 * 
	 * @return
	 * 
	 * @deprecated Using this method is discouraged, we're not sure it is a good idea to expose this so please
	 * don't use this in 3rd party functions.
	 */
	@Deprecated
	public RDFNode getRDFNode(){
		// should be overridden by graph/literal/uri subclass
		return Engine.getDefaultModel().createResource(JenaRDFConstants.valueBaseURI + getId());
	}
	
	public TupleBinding<RGLValue> getBinding() {
		return new MemoryBinding();
	}

	protected DeterminedRGLValue(){
		this.myId = ValueFactory.getNewId();
	}	
	
	/*
	 * methods for easy typecasting
	 */
	
	/**
	 * If this RGL Value is a bag, cast it; otherwise, throw an Exception.  
	 */	
	public AbstractBagValue asBag(){
		throw new RuntimeException("Cannot cast element of class "+this.getClass().getCanonicalName()+") to BagValue");
	}

	/**
	 * If this RGL Value is a Record, cast it; otherwise, throw an Exception.  
	 */	
	public AbstractRecordValue asRecord(){
		throw new RuntimeException("Cannot cast element of class "+this.getClass().getCanonicalName()+") to RecordValue");
	}

	/**
	 * If this RGL Value is a Graph, cast it; otherwise, throw an Exception.  
	 */	
	public GraphValue asGraph(){
		throw new RuntimeException("Cannot cast element of class "+this.getClass().getCanonicalName()+") to GraphValue");
	}

	/**
	 * If this RGL Value is a Literal, cast it; otherwise, throw an Exception.  
	 */	
	public LiteralValue asLiteral(){
		throw new RuntimeException("Cannot cast element of class "+this.getClass().getCanonicalName()+") to LiteralValue");
	}

	/**
	 * If this RGL Value is a URI, cast it; otherwise, throw an Exception.  
	 */	
	public URIValue asURI(){
		throw new RuntimeException("Cannot cast element of class "+this.getClass().getCanonicalName()+") to URIValue");
	}

	/**
	 * If this RGL Value is a URI, cast it; otherwise, throw an Exception.  
	 */	
	public BooleanValue asBoolean(){
		throw new RuntimeException("Cannot cast element of class "+this.getClass().getCanonicalName()+") to Boolean");
	}

	/**
	 * If this RGL Value is a RDFValue, cast it; otherwise, throw an Exception.  
	 */	
	public RDFValue asRDFValue(){
		throw new RuntimeException("Cannot cast element of class "+this.getClass().getCanonicalName()+") to RDFValue");
	}
	
	/**
	 * If this RGL Value is a RDFValue, cast it; otherwise, throw an Exception.  
	 */	
	public RGLNull asNull(){
		throw new RuntimeException("Cannot cast element of class "+this.getClass().getCanonicalName()+") to an RDFGears Error value");
	}

	public abstract void accept(RGLValueVisitor visitor);
	
	/**
	 * Do not serialize.  
	 */
	public String toString(){
		return "RGL_VALUE_"+getId() + " " + this.getClass();  
	}

	/**
	 * Ordering is currently UNDEFINED between different types, except for the comparison of RDFValues with RDFValues and NULL. 
	 * 
	 * - NULL is equal to every other NULL value
	 * - NULL is smaller than every non-NULL value
	 * - sorting RDFValues is done as per http://www.w3.org/TR/sparql11-query/#modOrderBy
	 *  
	 */
	public abstract int compareTo(RGLValue v2);
	
	/**
	 * Test whether this can be converted to a bag  
	 */	
	public boolean  isBag(){ return false ; }

	/**
	 * Test whether this can be converted to a record  
	 */	
	public boolean  isRecord(){ return false ; }

	/**
	 * Test whether this can be converted to a Graph  
	 */	
	public boolean  isGraph(){ return false ; }

	/**
	 * Test whether this can be converted to a Literal  
	 */	
	public boolean  isLiteral(){ return false ; }

	/**
	 * Test whether this can be converted to a URI  
	 */	
	public boolean  isURI(){ return false ; }
	
	/**
	 * Test whether this can be converted to a Boolean   
	 */	
	public boolean  isBoolean(){ return false ; }

	/**
	 * Test whether this can be converted to an RDFValue
	 */
	public boolean isRDFValue(){ return false ; }
	
	public boolean isSimple(){ return isBoolean() || isURI() || isLiteral() || isNull(); }

	/**
	 * Test whether this is an Error value. 
	 */
	public boolean isNull(){ return false ; }

}
