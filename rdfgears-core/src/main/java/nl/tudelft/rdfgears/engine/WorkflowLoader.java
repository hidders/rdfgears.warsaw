package nl.tudelft.rdfgears.engine;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.feliksik.rdfgears.BagTopFunction;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.CircularWorkflowException;
import nl.tudelft.rdfgears.rgl.exception.FunctionConfigurationException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.core.BagContains;
import nl.tudelft.rdfgears.rgl.function.core.BagFlatten;
import nl.tudelft.rdfgears.rgl.function.core.BagGroup;
import nl.tudelft.rdfgears.rgl.function.core.BagSingleton;
import nl.tudelft.rdfgears.rgl.function.core.BagUnion;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize;
import nl.tudelft.rdfgears.rgl.function.core.RecordCreate;
import nl.tudelft.rdfgears.rgl.function.core.RecordJoin;
import nl.tudelft.rdfgears.rgl.function.core.RecordProject;
import nl.tudelft.rdfgears.rgl.function.core.RecordUnion;
import nl.tudelft.rdfgears.rgl.function.sparql.SPARQLFunction;
import nl.tudelft.rdfgears.rgl.function.standard.ComparatorFunction;
import nl.tudelft.rdfgears.rgl.function.standard.FilterFunction;
import nl.tudelft.rdfgears.rgl.function.standard.IfThenElseFunction;
import nl.tudelft.rdfgears.rgl.function.standard.InsertSQLGears;
import nl.tudelft.rdfgears.rgl.function.standard.SQLGears;
import nl.tudelft.rdfgears.rgl.workflow.ConstantProcessor;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.rgl.workflow.InputPort;
import nl.tudelft.rdfgears.rgl.workflow.Workflow;
import nl.tudelft.rdfgears.rgl.workflow.WorkflowNode;
import nl.tudelft.rdfgears.util.ValueParser;
import nl.tudelft.rdfgears.util.XMLUtil;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

public class WorkflowLoader {
	
	
	private static final WorkflowNode FunctionProcessor = null;
	
	
	public static Workflow loadWorkflow(String workflowId) throws WorkflowLoadingException{
		Engine.init(null);
		WorkflowLoader wLoader = new WorkflowLoader(workflowId);
		return wLoader.getWorkflow();
	}
	
	/**
	 * CONTINUE HERE, RECONSIDER APPROACH 
	 */
	private String workflowId;  
	private String usedXMLFile = "<no file>";
	
	private Document xmlDoc;
	private Set<WorkflowNode> inputsConfigured = new HashSet<WorkflowNode>();
	
	/* map of node id's to nodes; these do not include WorkflowInputNodes */
	private Map<String, WorkflowNode> nodeMap = new HashMap<String, WorkflowNode>();
	
	/* map of workflow input id's to the Node input ports */
	private Map<String, InputPort> inputMap = new HashMap<String, InputPort>();
	private Workflow workflow; // the loaded, complete workflow element
	private Workflow buildingWorkflow; // the workflow that is being built
	
	/**
	 * workflowId should give sufficient info to find the file; filename is based on id. 
	 * @param workflowId
	 * @throws WorkflowLoadingException 
	 */
	public WorkflowLoader(String workflowId) throws WorkflowLoadingException{
		this.workflowId = workflowId;
		
		
		try {

			load();
		} catch (WorkflowLoadingException e){
			e.setWorkflowName(workflowId);
			throw(e);
//			
//			String origMsg = e.getClass().getName() + ( e.getMessage()!=null ? " "+e.getMessage() : ""); 
//			RuntimeException ex = new RuntimeException("Loading of the XML file "+usedXMLFile+" failed, does it comply to the DTD? Error is: \n"+origMsg);
////			
////			if (true){
////				Engine.getLogger().error(ex.getMessage());
////				Engine.getLogger().error("Exiting with error status.");
////				System.exit(-1);
////			}
//			ex.setStackTrace(e.getStackTrace());
//			throw ex;
		}
		
	}
	
	public Workflow getWorkflow(){
		return this.workflow;
	}
	public void load() throws CircularWorkflowException, WorkflowLoadingException {
		loadWorkflowXMLDocument();
		buildingWorkflow = new Workflow();
		String outputNodeId = getNetworkElement().getAttribute("output");
		
		try {
			recursivelyLinkInputs(outputNodeId);	
		} catch (java.lang.StackOverflowError e){
			throw new CircularWorkflowException("A circular reference is detected via node "+outputNodeId);
		}
		
		
		buildingWorkflow.setOutputProcessor(getNode(outputNodeId));
		//AbstractProcessor outputProducer = createProcessorFromElement(outputProcElem);
		//workflow.setOutputProducer(outputProducer);
		
		/*  we made it, no exceptions */
		workflow = buildingWorkflow;
		
		workflow.setName(this.workflowId);
	}
	
	
	/**
	 * Given the node id, set it's inputs 
	 * @param nodeId
	 * @throws WorkflowLoadingException 
	 */
	private void recursivelyLinkInputs(String nodeId) throws WorkflowLoadingException {
		WorkflowNode node = getNode(nodeId);
		if (inputsConfigured.contains(node)){
			return; // already linked 
		}
		
		Element nodeElem = getProcessorElement(nodeId);
		
		if (node instanceof FunctionProcessor){
			FunctionProcessor fnode = (FunctionProcessor) node;
			for (Element inputElem : XMLUtil.getSubElementByName(nodeElem, "inputPort")){
				Element sourceElem = XMLUtil.getFirstSubElementByName(inputElem, "source");
				String inputName = inputElem.getAttribute("name");
				if (sourceElem==null){
					throw new RuntimeException("There is no <source> section for input '"+inputName+"' of processor "+nodeId);
				}
				
				
				String sourceNodeId = sourceElem.getAttribute("processor");
				String workflowInputName = sourceElem.getAttribute("workflowInputPort");
				InputPort destinationPort = fnode.getPort(inputName);
				
				if (destinationPort==null){
					for (String nnn : fnode.getFunction().getRequiredInputNames() ){
						System.out.println("Requires name "+nnn);
					}
					
					throw new WorkflowLoadingException("The processor '"+nodeId+"' specifies an <inputPort name=\""+inputName+"\"> , but there is no such input required for the function "+fnode.getFunction().getFullName());
				}
				if (! sourceNodeId.equals("")){ 
					WorkflowNode sourceNode = getNode(sourceNodeId);
					destinationPort.setInputProcessor(sourceNode);
					
					/* recursively link inputs of this node */
					recursivelyLinkInputs(sourceNodeId); 
					
				} else if (! workflowInputName.equals("")){
					/* must read from workflowInputPort */
					buildingWorkflow.addInputReader(workflowInputName, destinationPort);
				} else {
					throw new RuntimeException("There is no complete <source> tag for port '"+inputName+"' of processor '"+nodeId+"'.");
				}
			}
			
		} 
		else {
			/* nothing to be done */
		}
		
		
		inputsConfigured.add(node);
	}
	
	/**
	 * get the node object with given id; id is valid in this workflow XML
	 * @param id
	 * @return
	 * @throws WorkflowLoadingException 
	 */
	private WorkflowNode getNode(String id) throws WorkflowLoadingException{
		assert(id!=null);
		WorkflowNode node = nodeMap.get(id);
		if (node==null){
			try {
				node = loadNode(id);
			} catch (Exception e){
				
				String origMsg = e.getClass().getName() + ( e.getMessage()!=null ? " "+e.getMessage() : ""); 
				RuntimeException ex = new RuntimeException("The section for node '"+id+"' is not correct: "+origMsg);
				
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
			nodeMap.put(id, node);
		}
		
		if (node==null)
			throw new WorkflowLoadingException("The processor with id '"+id+"' is referenced, but not defined. "); 
		return node;
	}

	
	private Element getNetworkElement(){
		Element workflowElem = XMLUtil.getFirstSubElementByName(xmlDoc.getDocumentElement(), "workflow");
		return XMLUtil.getFirstSubElementByName(workflowElem , "network");
	}
	


	public Element getProcessorElement(String nodeId){
		List<Element> procElemList = XMLUtil.getSubElementByName(getNetworkElement(), "processor");
		for (Element procElem : procElemList ){
			if (nodeId.equals(procElem.getAttribute("id")))
				return procElem;
		}
		return null;
	}
	
	
	/**
	 * create a processor element from the XML. Mark iteration ports and set Function;
	 * it doesn't connect input/output ports to other processors. 
	 * @param nodeId
	 * @return
	 * @throws WorkflowLoadingException 
	 */
	private WorkflowNode loadNode(String nodeId) throws WorkflowLoadingException {
		assert(nodeId!=null);
		
		Element procElem = getProcessorElement(nodeId);
		if (procElem==null) 
			throw new RuntimeException("Processor with id '"+nodeId+"' is referenced, but it is not defined in the workflow");
		
		
		Element funcElem = XMLUtil.getFirstSubElementByName(procElem, "function");
		String type = funcElem.getAttribute("type");
		
		Map<String,String> configMap = new HashMap<String,String>(); 
		
		
		
		List<Element> configList = XMLUtil.getSubElementByName(funcElem, "config");
		if (configList.size()>0){
			for (Element configElement : configList){
				String configKey = configElement.getAttribute("param");
				String configValue = configElement.getTextContent();
				configMap.put(configKey, configValue);
			}
		}
		

		/**
		 * TODO: 
		 * Reconsider this if/then/else/else/else/else statement. It's ugly. 
		 */
		
		/* either processor or function will be set by if/then/else switch */
		WorkflowNode processor = null;
		RGLFunction function = null;
		
		if (type.equals("workflow")){
			String workflowId = getXMLFunctionParameter(funcElem, "workflow-id");
			function = WorkflowLoader.loadWorkflow(workflowId);
		} else if (type.equals("constant")){
			RGLValue value;
			try {
				value = ValueParser.parseNTripleValue(configMap.get("value"));
			}
			catch (ParseException e){
				throw new RuntimeException("Cannot parse your value: "+configMap.get("value"));
			}
				
			processor = new ConstantProcessor(value, nodeId);
		} else if (type.equals("comparator")){
			function = new ComparatorFunction();
		} else if (type.equals("filter")){
			function = new FilterFunction();
		} else {
			String functionName;
			if (type.equals("custom-java")){
				functionName = getXMLFunctionParameter(funcElem, "implementation");
			} else {
				functionName = type;
			}
			function = instantiateFunction(functionName);
		}
		
		if (processor==null){
			/* make a FunctionProcessor */
			
			assert(function!=null);
			try {
				function.initialize(configMap);	
			} catch (FunctionConfigurationException e){
				e.setProcessor(processor);
				throw(e);
			} catch (NullPointerException e){
				System.out.println("The implementation of the initialize() function in "+function.getClass().getCanonicalName()+
						" threw a NullPointerException, which probably means it is not resilient to missing "+
						"config parameters. Improve the function implementation and/or repair the XML file to specify the required parameter.");
				throw(e);
			}
			
			FunctionProcessor funcProc = new FunctionProcessor(function, nodeId);
			configureIteration(funcProc, procElem);
			processor = funcProc;
		} else {
			/* it is a ConstantProcessor */
		}
		
		return processor;
	}
	

	/**
	 * mark the input ports for iteration, as specified in the processor XML Element
	 * @param fproc
	 * @param processorElement
	 */
	private void configureIteration(FunctionProcessor fproc, Element processorElement){
		List<Element> inputElemList = XMLUtil.getSubElementByName(processorElement, "inputPort");
		for (Element inputElem : inputElemList){
			if ("true".equals(inputElem.getAttribute("iterate"))){
				String portName = inputElem.getAttribute("name");
				fproc.getPort(portName).markIteration();
			}
		}
	}
	
	/**
	 * Initialize the XML DOM tree by reading the XML file from disk.
	 * @throws SAXException 
	 * @throws WorkflowLoadingException 
	 */
	public void loadWorkflowXMLDocument() throws WorkflowLoadingException{
		ArrayList<String> checkedPaths = new ArrayList<String>();
		for (String path : Engine.getConfig().getWorkflowPathList()){
			String filePath = path+"/"+workflowId+".xml";
			try {
				usedXMLFile = filePath; // well, used... at least we TRY to use it
				
				FileReader fileReader = new FileReader(filePath);
				InputSource input=new InputSource(fileReader);
				DOMParser parser = new DOMParser();
				parser.parse(input);
				xmlDoc = parser.getDocument();
			} catch (FileNotFoundException e) {
				checkedPaths.add(filePath);
			} catch (SAXException e) {
				throw new WorkflowLoadingException("Cannot parse XML file '"+usedXMLFile+"': "+ e.getMessage());
//				e.printStackTrace();
//				System.exit(0);
			} catch (IOException e) {
				throw new WorkflowLoadingException("Cannot load XML file '"+usedXMLFile+"': "+ e.getMessage());
			} 
			
			if (xmlDoc!=null){
				return;
			}
		}
		
		if (xmlDoc==null){
			String msg = "Workflow XML file for '"+workflowId+"' not found: \n";
			
			for (String path : checkedPaths){
				msg += "\tno file '"+path+"'\n";
			}
			throw new WorkflowLoadingException(msg);
		}
		
	}
	
	
	
	/**
	 * classmap to contain classnames as keys, but also the pre-known keys (e.g. "nrc-record-project") 
	 */
	private static Map<String, Class<?>> functionClassMap = loadFunctionClassMap();
	
	private static Map<String, Class<?>> loadFunctionClassMap(){
		Map<String, Class<?>> map = new HashMap<String, Class<?>>();
		
		
		/* record functions */
		map.put("record-project", RecordProject.class);
		map.put("record-create", RecordCreate.class);
		map.put("record-union", RecordUnion.class);
		map.put("record-join", RecordJoin.class);
		
		/* bag functions */
		map.put("bag-flatten", BagFlatten.class);
		map.put("bag-singleton", BagSingleton.class);
		map.put("bag-union", BagUnion.class);
		map.put("bag-categorize", BagCategorize.class);
		map.put("bag-groupby", BagGroup.class);
		
		/* should this really be here? Currently function initialize()'ability requires a custom GUI node, 
		 * which requires it to be pre-known here. That not nice!  
		 */
		map.put("select-top-scorer", BagTopFunction.class); 
		
		/* Two different versions are not really needed, as they are both implemented in SPARQLFunction.
		 * But in the UI they are different.  
		 * 
		 * SPARQLFunction is automatically querying an endpoint if the "endpointURI" parameter is configured. 
		 * Only restriction is that in that case it doesn't accept any prebind variables yet... 
		 */
		map.put("sparql", SPARQLFunction.class);
		map.put("sparql-endpoint", SPARQLFunction.class); 
		map.put("if", IfThenElseFunction.class);
		map.put("bag-contains", BagContains.class);
		
		map.put("insert-sql", InsertSQLGears.class);
		map.put("query-sql", SQLGears.class);
		
		return map;
	}
	
	public static Class<?> loadFunctionClass(String funcName){
		Class<?> theClass = functionClassMap.get(funcName);
		if(theClass==null){
			/* not known as a default function, then it must be a classname */
			
			String className = funcName;
			try {
				theClass = WorkflowLoader.class.getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				String errorMsg = "Cannot load function '"+className+"'. It is not a predefined function, and I cannot find such a class. You may be missing the necessary jar file. ";
				Engine.getLogger().error(errorMsg);
				throw new RuntimeException(errorMsg);
			}
		}
		
		return theClass;
	}
	
	public static RGLFunction instantiateFunction(String funcName) throws WorkflowLoadingException{
		if (funcName.startsWith("workflow:")){
			String workflowId = funcName.substring("workflow:".length());
			return WorkflowLoader.loadWorkflow(workflowId);
		} else {
			// must be a predefined nrc function, or a class 
			try {
				Class<?> rglClass = loadFunctionClass(funcName);
				RGLFunction instance = (RGLFunction) rglClass.newInstance();
				return instance;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				System.out.println("Cannot instantiate class  "+funcName+", did you define a no-argument constructor? ");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				System.out.println("Cannot load class  "+funcName);
				e.printStackTrace();
				
			}
		}
		
		return null;
	}
	
	

	public static String getXMLFunctionParameter(Element functionElem, String parameterName){
		NodeList childNodes = functionElem.getChildNodes();		
		for (int i=0; i<childNodes.getLength(); i++){
			Node item = childNodes.item(i);
			if (item instanceof org.w3c.dom.Element){
				Element cfgElem = (Element) item; 
				if (parameterName.equals(cfgElem.getAttribute("param"))){
					return cfgElem.getTextContent(); 
				}	
			}
		}
		return null; // no parameter with that name! 
	}
	
	
	public static List<String> getInputList(Element procElem){
		List<String> inputNameList = new ArrayList<String>(); 
		List<Element> inputPortList = XMLUtil.getSubElementByName(procElem, "inputPort");
		
		for (Element inputPortElem : inputPortList ){
			inputNameList.add(inputPortElem.getAttribute("name"));
		}
		return inputNameList;
	}

	
	
}
