package com.nl.tudelft.rdfgearsUI.client.Dia;

import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.nl.tudelft.rdfgearsUI.client.RGServiceAsync;
import com.nl.tudelft.rdfgearsUI.client.RGType;
import com.nl.tudelft.rdfgearsUI.client.RGTypeUtils;


public class RGNode extends Node{
	RGServiceAsync RService = null;
	String nodeType = null;
	String functionType = "";
	String nodeFullName = "";
	boolean iterated = false;
	com.google.gwt.xml.client.Element xmlDef = null;
	public RGNode(String nType, String id, RGWorkflow owner, boolean withHelper){
		super(id, owner, withHelper);
		nodeType = nType;
		nodeFullName = nodeType;
	}
	public RGNode(String nType, 
				  String id, 
				  com.google.gwt.xml.client.Element _xmlDef,
				  RGWorkflow owner,
				  boolean withHelper){
		
		super(id, owner, withHelper);
		nodeType = nType;
		xmlDef = _xmlDef;
	}
	@Override
	void draw(final RGCanvas canvas) {
		super.setCanvas(canvas);
		
		Element c = canvas.getElement();
		c.appendChild(root);
		showLoadingAnimation(); 
		
		RService = canvas.getRemoteService();
		nodeFullName = nodeType;
		if(nodeType.equals("custom-java") && xmlDef != null){
			 NodeList cfgs= xmlDef.getElementsByTagName("config");
			 for(int i = 0; i < cfgs.getLength(); i++){
				 com.google.gwt.xml.client.Element cfg = (com.google.gwt.xml.client.Element) cfgs.item(i);
				 if(cfg.hasAttribute("param")){
					 if(cfg.getAttribute("param").equals("implementation")){
						 XMLParser.removeWhitespace(cfg);
						 nodeFullName = cfg.getChildNodes().item(0).toString();
						 if(!nodeFullName.startsWith("workflow:")){
							 nodeFullName = "function:" + nodeFullName;
						 }
						 i = cfgs.getLength();
					 }
				 }
			 }
		}
			
		RService.getNode(nodeFullName, new AsyncCallback <String>(){

			public void onFailure(Throwable arg0) {
				canvas.displayErrorMessage("Cannot connect to server");
				canvas.updateNodeDrawingState(getId(), NodeDrawingState.ERROR);
			}

			public void onSuccess(String arg0) {
//				Log.debug("I have to parse this value: " + arg0);
				
				parseAndDrawNode(arg0);
				
				//create new node on an active workflow graph on workspace
				if(xmlDef == null) 
					canvas.setActiveNode(getInstance());
				//else is when we open a workflow graph
				
				//Log.debug("toXml :" + toXml(XMLParser.createDocument()).toString());
				canvas.updateNodeDrawingState(getId(), NodeDrawingState.DONE);
			}
			  
		  });
	
		
		setupRootEventHandler();
		
	}
	
	private void parseAndDrawNode(String nodeInXml) {
		removeLoadingAnimation();
		NodeList childs;
		com.google.gwt.xml.client.Element node;
		try{
			Document nodeDom = XMLParser.parse(nodeInXml);
			//test for error
			if(nodeDom.getFirstChild().getNodeName().equalsIgnoreCase("error")){
				canvas.displayErrorMessage("Node's source file cannot be found: " + nodeFullName);
				owner.removeNodeById(getId());
				if(root != null){
					root.removeFromParent();
				}
				return;
			}
			
			String header = ((com.google.gwt.xml.client.Element) nodeDom.getElementsByTagName("processor").item(0)).getAttribute("label"); 
			setHeaderText(header);
			//nodeDom.normalize(); //raise an error on IE
			childs = ((com.google.gwt.xml.client.Element) nodeDom.getElementsByTagName("processor").item(0)).getChildNodes();
			
			for(int i = 0; i < childs.getLength(); i++){
				try{
					if(childs.item(i).getNodeType() == 1){
						node = (com.google.gwt.xml.client.Element)childs.item(i);
						if(node.getTagName().equalsIgnoreCase("inputs")){
							parseInputNode(node);
						}else if(node.getTagName().equalsIgnoreCase("output")){
							parseOutputNode(node);
						}else if(node.getTagName().equalsIgnoreCase("description")){
							parseDescriptionNode(node);
						}else if(node.getTagName().equalsIgnoreCase("help")){
							parseHelpNode(node);
						}else{
							//wrong tag;
						}
					}
				} catch (Exception e){};//"dirty" way to skip blank child node
			}
		}catch (Exception e){
			canvas.displayErrorMessage("DOM Exception: node's xml file is not well-formed");
			Log.debug(e.toString());
			e.printStackTrace();
			if(root != null){
				root.removeFromParent();
			}
			owner.removeNodeById(getId());
			
		}
	}
	
	//****************************input parser***************************//
	private boolean parseInputNode(com.google.gwt.xml.client.Element input){
		NodeList childs;
		com.google.gwt.xml.client.Element node, type;
		Map <String, Boolean> savedNodeIterateValue = new HashMap <String, Boolean>();
		//get iterated value of input port if the operation is opening a saved workflow
		try{
			if(xmlDef != null){
				NodeList inputPorts = xmlDef.getElementsByTagName("inputPort");
				for(int j = 0; j < inputPorts.getLength(); j++){
					com.google.gwt.xml.client.Element ip = (com.google.gwt.xml.client.Element) inputPorts.item(j);
					String name = ip.getAttribute("name");
					Boolean it = false;
					if(ip.hasAttribute("iterate")){
						it = (ip.getAttribute("iterate").equalsIgnoreCase("true"))? true : false;
					}
					savedNodeIterateValue.put(name, it);
				}
			}
		}catch (Exception e){
			canvas.displayErrorMessage("DOM Exception: node's xml file is not well-formed");
			Log.debug(e.toString());
			e.printStackTrace();
			if(root != null){
				root.removeFromParent();
			}
			owner.removeNodeById(getId());
			
			e.printStackTrace();
		}
		
		childs = input.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++){
			try{
				if(childs.item(i).getNodeType() == 1) {
					node = (com.google.gwt.xml.client.Element)childs.item(i);
					if(node.getTagName().equalsIgnoreCase("data")){
						boolean iterate = false;
						if(node.hasAttribute("iterate")){
							iterate = (node.getAttribute("iterate").equalsIgnoreCase("true"))? true : false;
							if(iterate)
								iterated = true;
							
							if(savedNodeIterateValue.containsKey(node.getAttribute("name"))){
								iterated = savedNodeIterateValue.get(node.getAttribute("name"));
								iterate = iterated;
							}
						}
						
						if(node.hasChildNodes()){
							type = (com.google.gwt.xml.client.Element) node.getElementsByTagName("type").item(0);
							//Log.debug("raw type:" + type.toString());
							if(type != null){
								RGType t = new RGType(canvas.getTypeChecker().rename(RGTypeUtils.unwrap(type), getId()));
								t.setIterate(iterate);
								t.revert();
								addInputEntry(canvas.createUniqueId(),
									  node.getAttribute("name"),
									  t,
									  node.getAttribute("label"), iterate);
								//Log.debug("input port added");
							}else{
								RGType t = new RGType("<var name=\""+canvas.getTypeChecker().createUniqueTypeName()+"\"/>");
								t.setIterate(iterate);
								t.revert();
								addInputEntry(canvas.createUniqueId(),
										  node.getAttribute("name"),
										  t,
										  node.getAttribute("label"), iterate);
							}
						}else{
							RGType t = new RGType("<var name=\""+canvas.getTypeChecker().createUniqueTypeName()+"\"/>");
							t.setIterate(iterate);
							t.revert();
							addInputEntry(canvas.createUniqueId(),
									  node.getAttribute("name"),
									  t,
									  node.getAttribute("label"), iterate);
						}
					}else if(node.getTagName().equalsIgnoreCase("function")){
						functionType = node.getAttribute("type");
						if(node.hasChildNodes())
							parseFunctionParams(node);
					}
				}
			} catch (Exception e){
				canvas.displayErrorMessage("DOM Exception: node's xml file is not well-formed");
				Log.debug(e.toString());
				e.printStackTrace();
				if(root != null){
					root.removeFromParent();
				}
				owner.removeNodeById(getId());
				
				e.printStackTrace();
				
			};
		}
		return true;
	}
	
	private boolean parseFunctionParams(com.google.gwt.xml.client.Element fNode){
		NodeList childs, configParams;
		com.google.gwt.xml.client.Element node, descNode, source, function, cParam;
		String pValue = "";
		
		childs = fNode.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++){
			try{
				if(childs.item(i).getNodeType() != 1)
					continue;
				
				node = (com.google.gwt.xml.client.Element)childs.item(i);
				if(node.getTagName().equalsIgnoreCase("param")){
					if(node.hasAttribute("type")){
						String pType = node.getAttribute("type");
						
						if(node.hasAttribute("value"))
							pValue = node.getAttribute("value");
							
						if(pType.equalsIgnoreCase("List")){
							if(node.hasAttribute("source")){
								//addListParam(node.getAttribute("name"), "def", node.getAttribute("label"), node.getAttribute("source"), null);
							}else if(node.hasChildNodes()){ //the options are embedded
								String selectedValue = "";
								if(xmlDef != null){ // processing workflow graph
									function = (com.google.gwt.xml.client.Element) xmlDef.getElementsByTagName("function").item(0);
									configParams = function.getElementsByTagName("config");
									for(int k = 0; k< configParams.getLength(); k++){
										cParam = (com.google.gwt.xml.client.Element) configParams.item(k);
										if(cParam.hasAttribute("param")){
											if(cParam.getAttribute("param").equalsIgnoreCase(node.getAttribute("name")) && cParam.hasChildNodes()){
												selectedValue = cParam.getFirstChild().getNodeValue();
											}
										}
									}
								}
								
								source = (com.google.gwt.xml.client.Element) node.getElementsByTagName("source").item(0);
								addListParam(node.getAttribute("name"), selectedValue, node.getAttribute("label"),source);
								
							}
							
							if(node.hasChildNodes()){
								descNode = (com.google.gwt.xml.client.Element) node.getElementsByTagName("description").item(0);
								if(descNode != null)
									if(descNode.hasChildNodes()){
										getParam(node.getAttribute("name")).setDescriptionText(descNode.getFirstChild().getNodeValue());
										//Log.debug("set desc text with: " + descNode.getFirstChild().getNodeValue());
									}
							}
						}else{
							if(xmlDef != null){ // processing workflow graph
								function = (com.google.gwt.xml.client.Element) xmlDef.getElementsByTagName("function").item(0);
								configParams = function.getElementsByTagName("config");
								for(int k = 0; k< configParams.getLength(); k++){
									cParam = (com.google.gwt.xml.client.Element) configParams.item(k);
									if(cParam.hasAttribute("param")){
										if(cParam.getAttribute("param").equalsIgnoreCase(node.getAttribute("name")) && cParam.hasChildNodes()){
											String v = cParam.getFirstChild().getNodeValue();
											addParam(node.getAttribute("name"), pType, v, node.getAttribute("label"));
											k = configParams.getLength();
										}
									}
								}
							}else{
								addParam(node.getAttribute("name"), pType, pValue, node.getAttribute("label"));
							}
							
							if(node.hasChildNodes()){
								if(node.getElementsByTagName("description").getLength() > 0){
									descNode = (com.google.gwt.xml.client.Element) node.getElementsByTagName("description").item(0);
									if(descNode.hasChildNodes()){
										if(descNode.getFirstChild() != null)
											if(getParam(node.getAttribute("name")) != null)
												getParam(node.getAttribute("name")).setDescriptionText(descNode.getFirstChild().getNodeValue());
									}
								}
							}
						}
					}else if(node.hasAttribute("value")){
						pValue = node.getAttribute("value");
						addParam(node.getAttribute("name"), "CONSTANT", pValue, "");
						if(pValue.startsWith("workflow:")){
								isWorkflow = true;
								workflowId = pValue.substring(9);
								addParam(pValue.substring(9), "wfTools", null, null);
						}
						if(node.hasChildNodes()){
							if(node.getElementsByTagName("description").getLength() > 0){
								descNode = (com.google.gwt.xml.client.Element) node.getElementsByTagName("description").item(0);
								//Log.debug(descNode.getFirstChild().getNodeValue());
								if(descNode.hasChildNodes()){
									if(descNode.getFirstChild() != null)
										getParam(node.getAttribute("name")).setDescriptionText(descNode.getFirstChild().getNodeValue());
								}
							}
						}
					}
				}else{
					//other function child
				}
			}catch (Exception e){
				canvas.displayErrorMessage("DOM Exception: node's xml file is not well-formed");
				Log.debug(e.toString());
				e.printStackTrace();
				if(root != null){
					root.removeFromParent();
				}
				owner.removeNodeById(getId());
			};
		}
		
		return true;
	}

	//*****************************ouput parser***************************//
	private boolean parseOutputNode(com.google.gwt.xml.client.Element output){
		com.google.gwt.xml.client.Element type;// = output.getAttribute("type");
		
		if(output.hasChildNodes()){
			type = (com.google.gwt.xml.client.Element) output.getElementsByTagName("type").item(0);
			if(type != null){
				RGType t = new RGType(canvas.getTypeChecker().rename(RGTypeUtils.unwrap(type), getId()));
				if(iterated){
					t.setIterate(true);
					t.revert();
				}
				addNodeOutputPort(t);
			}else{
				RGType t = new RGType("<var name=\""+canvas.getTypeChecker().createUniqueTypeName()+"\"/>");
				if(iterated){
					t.setIterate(true);
					t.revert();
				}
				addNodeOutputPort(t);
			}
		}else{
			RGType t = new RGType("<var name=\""+canvas.getTypeChecker().createUniqueTypeName()+"\"/>");
			if(iterated){
				t.setIterate(true);
				t.revert();
			}
			addNodeOutputPort(t);
		}
	
		return true;
	}
	
	private boolean parseDescriptionNode(com.google.gwt.xml.client.Element desc){
		if(desc.hasChildNodes()){
			String descString = desc.getFirstChild().getNodeValue();
			descString = descString.trim().replaceAll("&", "&amp;");
			descString = descString.trim().replaceAll(">", "&gt;");
			descString = descString.trim().replaceAll("<", "&lt;");
			
			descString = descString.trim().replaceAll("\n", "<br/>");
			
			addParam(canvas.createUniqueId(), "Description", descString, "Description");
		}
		
		return true;
	}
	private boolean parseHelpNode(com.google.gwt.xml.client.Element help){
	
		return true;
	}
	
	@Override
	void displayProperties(Element container) {
		//show param's input form
		displayParamForm(container);
	}
	
	
	
	@Override
	public com.google.gwt.xml.client.Element toXml(com.google.gwt.xml.client.Document doc) {
		com.google.gwt.xml.client.Element node =  doc.createElement("processor");
		com.google.gwt.xml.client.Element function =  doc.createElement("function");
		node.setAttribute("id", getId());
		node.setAttribute("x", "" + getX());
		node.setAttribute("y", "" + getY());
		
		if(functionType.length() > 0)
			function.setAttribute("type", functionType);
		else
			function.setAttribute("type", nodeType); //safe mode
		
		for(String paramId: paramIds){
			RGFunctionParam p = functionParamBuffer.get(paramId);
			if(p != null){
				com.google.gwt.xml.client.Element paramElement = p.toXml(doc);
				if(paramElement != null)
					function.appendChild(paramElement);
			}
		}
		node.appendChild(function);
		for(String portId: inputPorts){
			String entryId = portId2EntryId.get(portId);
			String name = portId2Name.get(portId);
			boolean iterate = entryId2iterateState.get(entryId);
			com.google.gwt.xml.client.Element inputPort = doc.createElement("inputPort");
			inputPort.setAttribute("name", name);
			if(iterate)
				inputPort.setAttribute("iterate", "true");
			else
				inputPort.setAttribute("iterate", "false");
			
			if(entryId2Paths.containsKey(entryId)){
				com.google.gwt.xml.client.Element source = doc.createElement("source");
				Path p = entryId2Paths.get(entryId);
				String sourceId = p.getSourceId();
				if(sourceId.equalsIgnoreCase(RGCanvas.WORKFLOW_INPUT_NODE_ID)){
					Node workflowInput = p.getStartNode();
					source.setAttribute("workflowInputPort", workflowInput.getPortNameByPortId(p.getStartPortId()));
				}else{
					source.setAttribute("processor", sourceId);
				}
				
				inputPort.appendChild(source);
			}
			node.appendChild(inputPort);
		}
		return node;
	}

}
