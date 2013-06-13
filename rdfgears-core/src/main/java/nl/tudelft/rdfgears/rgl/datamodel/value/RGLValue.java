package nl.tudelft.rdfgears.rgl.datamodel.value;

import java.io.Serializable;

import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.sleepycat.bind.tuple.TupleBinding;

/**
 * RGLValue is the interface of the values we deal with.
 * 
 * @author Eric Feliksik
 * 
 */
public interface RGLValue extends Comparable<RGLValue>, Serializable {

	public long getId();

	/**
	 * Get a Jena RDFNode describing this value. It is either a URI or a literal
	 * String.
	 */
	public RDFNode getRDFNode();

	/**
	 * Get a binding for a specific class
	 */
	public TupleBinding<RGLValue> getBinding();

	/*
	 * methods for easy typecasting
	 */

	/**
	 * Convert this value to a Bag. If this is not possible, throw an Exception.
	 */
	public AbstractBagValue asBag();

	/**
	 * Convert this value to a Record. If this is not possible, throw an
	 * Exception.
	 */
	public AbstractRecordValue asRecord();

	/**
	 * Convert this value to a Graph. If this is not possible, throw an
	 * Exception.
	 */
	public GraphValue asGraph();

	/**
	 * Convert this value to a Literal. If this is not possible, throw an
	 * Exception.
	 */
	public LiteralValue asLiteral();

	/**
	 * Convert this value to a URI. If this is not possible, throw an Exception.
	 */
	public URIValue asURI();

	/**
	 * Convert this value to a Boolean. If this is not possible, throw an
	 * Exception.
	 */
	public BooleanValue asBoolean();

	/**
	 * is this needed, or can we require the user to cast to literal/URI
	 * immediately? I think it is needed - Eric
	 * 
	 * @return
	 */
	public RDFValue asRDFValue();

	/**
	 * Test whether this can be converted to a Bag
	 */
	public boolean isBag();

	/**
	 * Test whether this can be converted to a Record
	 */
	public boolean isRecord();

	/**
	 * Test whether this can be converted to a Graph
	 */
	public boolean isGraph();

	/**
	 * Test whether this can be converted to a Literal
	 */
	public boolean isLiteral();

	/**
	 * Test whether this can be converted to a URI
	 */
	public boolean isURI();

	/**
	 * Test whether this can be converted to a Boolean
	 */
	public boolean isBoolean();

	/**
	 * Test whether this can be converted to an RDFValue
	 */
	public boolean isRDFValue();

	/**
	 * Test whether this is literal, URI, or Boolean.
	*/
	public boolean isSimple();

	/**
	 * Receive an RGLValueVisitor
	 * 
	 * @param visitor
	 */
	public abstract void accept(RGLValueVisitor visitor);

	/**
	 * Serialize this RGL Value. Note that this can return VERY long strings, if
	 * the value contains many sub-values.
	 */
	public String toString();

	public int compareTo(RGLValue value);

	public boolean isNull();

	/**
	 * Signal this value that it should prepare for multiple readings.
	 * 
	 * Only relevant for bags, as they may do materialization in order to
	 * generate results only once. So maybe it is not pretty to have this for
	 * all values, or maybe it will turn out useful in the future.
	 * 
	 */
	public void prepareForMultipleReadings();

}
