package nl.tudelft.rdfgears.rgl.workflow;

import java.util.HashMap;
import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowConnectionException;
import nl.tudelft.rdfgears.rgl.exception.WrappedWorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;


/**
 * A Workflow contains a network of Processors. 
 * As the processors reference eachother, we only need to keep track of the 
 * inputs and outputs. 
 * 
 * Note: A new Workflow object with a new processor network needs to be instantiated for 
 * every processor that uses the workflow as it's function definition.  
 * 
 * @author Eric Feliksik
 *
 */
public class Workflow extends RGLFunction {
	private Map<String,WorkflowInputPort> namesToWFIPorts = new HashMap<String,WorkflowInputPort>();
	public static final long INIT_CACHE_VERSION = 0; 
	long cacheVersion = INIT_CACHE_VERSION; // a counter to refer to our cache version, so that internal processors can check whether they can use their cache
	
	/* the output producer */
	private WorkflowNode outputProcessor; 
	
	/* the row of input RGLValue objects. WorkflowInputPorts select their value and pass it to the reading processors */
	private ValueRow currentInputRow; 
	private TypeRow inputTypeRow;
	private String workflowName;
	
	public Workflow(){
//		this.isLazy = true;
	}
	
	
	/* allows my WorkflowInputPorts to read my inputRow */
	protected ValueRow getCurrentInputRow(){
		return this.currentInputRow;
	}
	
	private void setCurrentInputRow(ValueRow currentInputRow){
		this.currentInputRow = currentInputRow;
	}
	
	/**
	 * Create an input port for this workflow, and attach a processor input port that reads from it. 
	 * 
	 * @param workflowInputName
	 * @param processorInputPort
	 */
	public void addInputReader(String workflowInputName, InputPort processorInputPort){
		if (outputProcessor!=null){
			throw new RuntimeException("You must not do workflow.addInputReader() after you have called setOutputProcessor()");
		}
		
		/* register portname and create workflow input port */
		WorkflowInputPort wfiport = namesToWFIPorts.get(workflowInputName);
		if (wfiport == null){
			wfiport = new WorkflowInputPort(this, workflowInputName);
			namesToWFIPorts.put(workflowInputName, wfiport);
			requireInput(workflowInputName);
		}
		
		
		/* create a connection from WorkflowInputPot to processorInputPort */
		//this.inputConnectionMap.put(processorInputPort, wfiport);
		
		processorInputPort.setInputProcessor(wfiport);
	}

	public void setOutputProcessor(WorkflowNode outputProcessor){
		this.outputProcessor = outputProcessor;
	}
	
	public WorkflowNode getOutputProcessor(){
		return this.outputProcessor;
	}
	
	/**
	 * Execute the workflow (i.e. execute the outputProcessor). The processor that is wrapping this 
	 * workflow is responsible for iteration, and for caching. 
	 */
	@Override
	public RGLValue execute(ValueRow inputRow) {
		
		setCurrentInputRow(inputRow); /* allow the WorkflowInputPorts to getCurrentInputRow */
		
		/* FIXME: this is suboptimal as we will be recursively resetting everything. 
		 * We could determine before workflow execution which ProcessorInputs will become dirty, 
		 * and which won't. But one can wonder how big this overhead is. 
		 */
		
		RGLValue value = outputProcessor.getResultValue();
		
		invalidateInternalProcessorCaches(); 
		
		currentInputRow = null;
		return value;
	}
	
	/**
	 * Method recursively invalidates processor caches - also of unused processors.  
	 * 
	 * It may be more efficient to do it differently: 
	 * - maintain a long cacheVersion in a workflow. 
	 * - increment the cacheVersion after every workflow run. 
	 * - Make the workflow known as the 'environment' of all internal processors, on instantiation
	 * - let a functionProcessor only reuse it's cache if it's cacheVersion matches that of the environment workflow. 
	 */
	private void invalidateInternalProcessorCaches(){
		outputProcessor.resetProcessorCache(); /* reset it to allow garbage collection, and allow another execute() invocation */
	}
	
	public long getCacheVersion(){
		return cacheVersion;
	}
	
	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws WorkflowCheckingException {
		
		/**
		 * check whether the inputTypes TypeRow will suffice 
		 */
		for (String inputName : getRequiredInputNames()){
			if (inputTypes.get(inputName)==null){
				throw new WorkflowConnectionException("Input not connected; sorry, we can currently only check workflows with configured inputs", inputName);
			}
		}
		
		setInputTypeRow(inputTypes);
		try {
			/* any processor in this workflow may throw an exception */
			return outputProcessor.getOutputType();
		} catch (WorkflowCheckingException e){
			/*
			 * Processor detecting the exception has already registered itself in e. 
			 * Create a new exception with the caught one as the cause; this administers the cause trace. 
			 */
			throw new WrappedWorkflowCheckingException(e); // processor must register itself in thrown Exception
		}
	}
	
	/* type checking methods */
	protected TypeRow getInputTypeRow() {
		return this.inputTypeRow;
	}
	
	private  void setInputTypeRow(TypeRow typeRow) {
		this.inputTypeRow = typeRow;
	}


	@Override
	public void initialize(Map<String, String> config) {
		// nothing to initialize in a workflow 		
	}
	
	public void setName(String workflowName){
		this.workflowName = workflowName;
	}
	public String getShortName() {
		return workflowName;
	}
	
	public String getFullName() {
		return workflowName;
	}
	
	@Override
	public String getRole() {
		return "workflow";
	}

	
}
