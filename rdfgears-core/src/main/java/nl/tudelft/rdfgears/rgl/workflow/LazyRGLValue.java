package nl.tudelft.rdfgears.rgl.workflow;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.bindings.LazyRGLBinding;
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
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.sleepycat.bind.tuple.TupleBinding;

/**
 * A LazyRGLValue uses some FunctionProcessors' function to generate a value, 
 * but postpones doing so until evaluate() is called. 
 * 
 * For that reason, the LazyRGLValue must store a ValueRow to read it's input from, 
 * at the moment it is called. 
 * 
 * This class caches the evaluation result.
 * @author Eric Feliksik
 *
 */
public class LazyRGLValue implements RGLValue  {
	
	private long myId = ValueFactory.getNewId();
	
	/**
	 * Return id of evaluated result. Thus, it evaluates the expression. 
	 * 
	 * To what extent does this affect laziness?
	 *  If we don't do it like this but instead ask an id from the ValueFactory, we must (?) also 
	 *  use this id in the evaluated value, but this is not always possible (e.g. in case of lazily evaluated project)
	 *  because it might already be assigned. So we'd then have 2 id's for the same value (lazy-version and evaluated version).  
	 */
	@Override
	public long getId(){
//		return evaluate().getId();
		return myId;
	}

	@Override
	public RDFNode getRDFNode() {
		return evaluate().getRDFNode();
	}
	
	private RGLFunction function;
	private ValueRow inputRow = null;
	private RGLValue cachedResultValue = null;
	
	private boolean valueIsReadMultipleTimes;

	/**
	 * Create a simple LazyRGLValue.  
	 * 
	 * @param function
	 * @param inputRow
	 * @param iterateIfPossible
	 */
	public LazyRGLValue(RGLFunction function, ValueRow inputRow){
		//assert(false);
		if (false) 
			throw new RuntimeException("Laziness is disabled!!! ");
		this.function = function;
		this.inputRow = inputRow;
		
		assert(this.function!=null);
		assert(this.inputRow!=null);
		
	}

	
	/** 
	 * All good things come to an end, and so does laziness and procrastination.
	 * We now really have to call the implementation function to produce the value. 
	 * 
	 * But because our inputRow is also lazy, not even all inputs need to be generated, 
	 * if our implementation function does not converts them to a concrete value (e.g. with .asRecord()) 
	 */
	private RGLValue evaluate() {
		if (cachedResultValue==null){
			assert(inputRow!=null);
			cachedResultValue  = function.execute(inputRow);
			
//				if (inputRow instanceof FieldMappedValueRow){
//					/* it may be nicer to move flagRecyclable up in the class hierarchy */
//					((FieldMappedValueRow) inputRow).setRecyclable(true); 
//				}
			
			this.inputRow = null; /* collect garbage */
			
			if(valueIsReadMultipleTimes) // beware of any evaluating log messages above, which may result in setting this flag too late
				cachedResultValue.prepareForMultipleReadings();	
		}
		
		//assert(this.inputRow==null) : "don't remember inputRow, we already know cachedResultValue";
		assert(cachedResultValue!=null);
//		Engine.getLogger().fatal(cachedResultValue.getClass());
		return cachedResultValue;
	}
	
	

	@Override
	public void accept(RGLValueVisitor visitor) {
		//Engine.getLogger().info(evaluate());
		evaluate().accept(visitor);
	}

	@Override
	public int compareTo(RGLValue v2) {
		return evaluate().compareTo(v2);
	}

	
	@Override
	public AbstractBagValue asBag() {
		return evaluate().asBag();
	}

	@Override
	public AbstractRecordValue asRecord() {
		return evaluate().asRecord();
	}

	@Override
	public GraphValue asGraph() {
		return evaluate().asGraph();
	}

	@Override
	public LiteralValue asLiteral() {
		return evaluate().asLiteral();
	}

	@Override
	public URIValue asURI() {
		return evaluate().asURI();
	}

	@Override
	public BooleanValue asBoolean() {
		return evaluate().asBoolean();
	}
	
	@Override
	public RDFValue asRDFValue() {
		return evaluate().asRDFValue();
	}
	
	@Override
	public boolean isBag() {
		return evaluate().isBag();
	}

	@Override
	public boolean isRecord() {
		return evaluate().isRecord();
	}

	@Override
	public boolean isGraph() {
		return evaluate().isGraph();
	}

	@Override
	public boolean isLiteral() {
		return evaluate().isLiteral();
	}

	@Override
	public boolean isURI() {
		return evaluate().isURI();
	}

	@Override
	public boolean isBoolean() {
		return evaluate().isBoolean();
	}

	@Override
	public boolean isRDFValue() {
		return evaluate().isRDFValue();
	}
	
	public boolean isSimple() {
		return true;
	}
	
	@Override
	public boolean isNull() {
		return evaluate().isNull();
	}
	
	/**
	 * evaluate and delegate.  
	 */
	@Override
	public String toString(){
		return evaluate().toString();
	}

	@Override
	public void prepareForMultipleReadings() {
		this.valueIsReadMultipleTimes = true;
		
	}

	@Override
	public TupleBinding<RGLValue> getBinding() {
		return new LazyRGLBinding(cachedResultValue, function, inputRow);
	}
	
//	
//	equals not implemented for now, comparing bags is kind of tricky (occurence frequency, etc)
//
//	public boolean equals(Object o){
//		if (this==o)
//			return true;
//		
//		if (o instanceof RGLValue){
//			return ((RGLValue)o).equals(this.evaluate());
//		}
//		
//		return false;
//	}


}
