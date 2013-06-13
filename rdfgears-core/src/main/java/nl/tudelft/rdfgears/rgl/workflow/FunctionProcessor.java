package nl.tudelft.rdfgears.rgl.workflow;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MappingBagValue;
import nl.tudelft.rdfgears.rgl.exception.IterationTypingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowConnectionException;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.util.row.FieldMappedValueRow;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * FunctionProcessor, a processor with inputs.    
 * A new processor must be instantiated for every occurrence in a workflow, 
 * it is thus unique.
 *  
 * Implements iteration marking, the iteration mechanism, result caching 
 * of the processor's execution, and linking processors to other 
 * processors, thus creating a network. 
 *  
 * @author Eric Feliksik
 *
 */
public class FunctionProcessor extends ProcessorNode implements Serializable {
	
	
	private int iterationCounter = 0; // remove this, diagnostic
	
	/**
	 * Values that do *not* change after initialization 
	 */
	private Map<String, InputPort> portMap = new HashMap<String, InputPort>();
	private RGLType outputType; 
	private RGLFunction function;	
	
	/* a row of RGLValue objects that is input for our function. Declared here because,
	 * if the function is *not* lazy, it is evaluated immediately, and we need not allocate a new 
	 * ValueRow every time but we can reuse it instead. 
	 */
//	private FieldMappedValueRow previousRow; // recycling disabled
	
	/**
	 * Values that *do* change after initialization 
	 */	
	private RGLValue cachedResultValue;
	
	/* whether or not we have a marked port */
	private boolean iterates;

	private boolean valueIsReadMultipleTimes = false; // disable materialization by default
	
	/**
	 * Constructor. A ReadingProcessor must take some RGLFunction to help it generate values.
	 * @param function
	 */
	public FunctionProcessor(RGLFunction function){
		this(function, null);
	}
	
	protected FunctionProcessor() {
		
	}
	
	public FunctionProcessor(RGLFunction function, String id){
		super(id);
		this.function = function;
		Iterator<String> nameIter = function.getRequiredInputNames().iterator();
		while (nameIter.hasNext()){
			String portName = nameIter.next();
			InputPort port = new InputPort(portName, this);
			portMap.put(portName, port);
		}
		
		
//		previousRow = new FieldMappedValueRow(function.getFieldIndexMap());
//		previousRow.setRecyclable(true);
		//System.out.println("FunctionProcessor: making strict "+function);
		
		//this.function.isLazy = false;
	}
	
	public InputPort getPort(String portName){
		return portMap.get(portName);
	}
	
	
	public RGLFunction getFunction(){
		return this.function;
	}
	
	/** 
	 * Execute the processor's function on the given input row over values, 
	 * and cache the result.   
	 * @param inputs
	 * @return the result value of the execution.
	 */
	@Override
	public RGLValue getResultValue(){
		
		//assert(initialized) : "you must initialize with finishInputInitialization()";
		if (cachedResultValue==null){
			/* our processor has been reset, either because our program just started or because 
			 * we are working within a workflow that is iterating.   
			 */
			
			assert(this.getFunction()!=null);
			
			
			
//			if (! previousRow.isRecyclable()){ // we cannot reuse the previous row 
//				previousRow = new FieldMappedValueRow(function.getFieldIndexMap());
//			} else {
//				// we are going to reuse it
//				previousRow.setRecyclable(false);
//			}
//			FieldMappedValueRow inputRow = previousRow; // no need to create new instance and create garbage
			
			FieldMappedValueRow inputRow = new FieldMappedValueRow(function.getFieldIndexMap()); // no recycling

			fillValueRow(inputRow);
			
			if (! iterates()){
				/* create a single return value */
				
				if (function.isLazy()){
					cachedResultValue = new LazyRGLValue(getFunction(), inputRow);
				}
				else {
					try { /* modify allocated valueRow and evaluate immediately */
						assert(hasNecessaryFields(inputRow, function));
						cachedResultValue = function.execute(inputRow);
//						inputRow.setRecyclable(true);
					} catch (RuntimeException e){
						System.out.println("Problem executing function "+getFunction().getShortName()+" in processor "+getId());
						throw(e);
					}	
				}
				
			} else { 
				/* iterate: perform a mapping over the input bag(s), using the given function.
				 * The MappingBagValue respects the functions laziness if applicable  */
				cachedResultValue = new MappingBagValue(inputRow, this);	
			}
			
			if (valueIsReadMultipleTimes) // beware of any evaluating log messages above (getId()!!!) , which may result in setting this flag too late
				cachedResultValue.prepareForMultipleReadings();

			iterationCounter++;

		}
		
		//System.out.println("Proc "+getId()+" executed function "+function.getFullName()+" with valueIsReadMultipleTimes=="+valueIsReadMultipleTimes+" yielding value "+cachedResultValue);
		
		return cachedResultValue;
	}
	
	
	/** check whether the row has the fields necessary for function */
	private boolean hasNecessaryFields(ValueRow row, RGLFunction func){
		for (String field : function.getRequiredInputNames()){
			if (row.get(field)==null)
				return false;
		}
		return true;
	}

	/**
	 * fill the valueRow with the up-to-date values. 
	 * @param eRow
	 */
	private void fillValueRow(FieldMappedValueRow eRow){
		
		/* make new row over Values */
		/* FIXME this can be optimized by only copying volatile inputs. But we will only do this if 
		 * it turns out that this actually happens often, otherwise it just looks complex  */
		
		for (InputPort inputPort : getPortSet()){
			assert(inputOk(inputPort));
			eRow.put(inputPort.getName(), inputPort.readInput());
		}
	}
	
	private boolean inputOk(InputPort port) {
		if (port.readInput()==null){
			try {
				getOutputType(); // may throw exception
				// no typing exception, but no input either. this is a problem.
				throw new RuntimeException("Something went wrong with typechecking, this is a bug.");
				
			} catch (WorkflowCheckingException e) {
				throw new RuntimeException("You cannot execute an ill-typed processor, typing exception is:  "+e.getStackTrace()+" .\n");
			}
		}
		
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public RGLType getOutputType() throws WorkflowCheckingException {
		if (outputType==null){
				
			/* build a row of input types, for which we can calculate an output type */
			TypeRow inputTypeRow = new TypeRow();
			for (InputPort port : getPortSet()){
				String inputName = port.getName();
				if (! port.isConnected()){
					/* we are missing a required input */
//						throw new WorkflowCheckingException(function, inputName, new SuperType(), null, "You must configure input '"+inputName+"'.");
					WorkflowConnectionException ex = new WorkflowConnectionException("Input not connected", inputName);
					ex.setProcessorAndFunction(this, null); /* Register ourselves */
					throw(ex);
				}
				
				RGLType inputType = port.getInputType();
				assert(inputType!=null) : "Processor with function "+this.getFunction()+" needs input "+port.getName()+", but its port receives a null type";
				
				if (port.iterates()){
					/* input should a bag, and returntype is element-type of that bag */
					if (inputType instanceof BagType){
						BagType bType = (BagType) inputType;
						inputType = bType.getElemType(); /* the effective input type is the type used in the bag */
					}
					else {
						/* throw an exception that we want a bag. We don't tell what elements we expect in the bag;
						 * it would be nicer to tell this, but we don't know as we have not implemented a way to 
						 * find this out from the function.  
						 */
						IterationTypingException ex = new IterationTypingException("Port "+port.getName()+" is marked for iteration, but the provided inputType "+inputType+" is not not a bag. ");
						ex.setProcessorAndFunction(this, null); /* Register ourselves */
						throw(ex);
					}
				}
				
				inputTypeRow.put(inputName, inputType);	
			}
			/* let the function implementation determine the result type */
			try {
				outputType = getFunction().getOutputType(inputTypeRow);
			} catch (WorkflowCheckingException e){
				e.setProcessorAndFunction(this, getFunction()); /* Register ourselves */
				throw(e);
			} 
			
			if (outputType==null){
				throw new RuntimeException("The function "+this.getFunction()+" returned null on getOutputType. However, it should either return a Type, or throw a FunctionTypingException.");
			}
			
			if (this.iterates()){
				/* this is an iterating processor */ 
				outputType = BagType.getInstance(outputType);
			}
			
			Engine.getLogger().debug("proc "+getId()+" outputs type "+outputType);
		}
		/* use cache */
		
		return outputType;
	}
	
	/**
	 * check whether there is a marked input port
	 * @return
	 */
	private boolean expensiveIterationCheck(){
		for (InputPort port : getPortSet()){
			if (port.iterates()) 
				return true;
		}
		return false;
	}
	
	public boolean iterates() {
		assert(this.iterates == expensiveIterationCheck()): "Violating an invariant!";
		return this.iterates;
	}

	/**
	 * only to be called by one of my ports!
	 */
	protected void flagIteration(InputPort p) {
		assert(p.iterates()): " function must only be called by iterating port";
		this.iterates = true;
	}
	
	/**
	 * Return a set of ports. The type is a collection, but it will not contain duplicates.  
	 * @return
	 */
	public Collection<InputPort> getPortSet() {
		return this.portMap.values();
	}
	
	/**
	 * Reset this processors' cache, and that of all it's inputs. 
	 * This is suboptimal, as not all inputs are necessarily dirty.
	 * 
	 * Resetting is only useful if we are working in some iterating workflow.
	 * Then a new row has to be built for generating a RGLValue, and cloning it is assumed to be 
	 * faster than building a new row. But we don't even have to set all the RGLValue-s, but only 
	 * those that are 'dirty'. But we don't track that, we are naive.  
	 */
	@Override
	public void resetProcessorCache() {
		//System.out.println("Resetting cached value generator for proc "+this);
		this.cachedResultValue = null; 
		Iterator<InputPort> portIter = getPortSet().iterator();
		while (portIter.hasNext()){
			InputPort port = portIter.next();
			port.resetInput();
		}
	}


	/**
	 * If true, caching is enabled if it is possible for the processor. 
	 * @param multipleTimes
	 */
	public void valueIsReadMultipleTimes(boolean multipleTimes) {
		valueIsReadMultipleTimes = multipleTimes;		
	}


}
