package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;
import static gwtquery.plugins.draggable.client.Draggable.Draggable;
import com.allen_sauer.gwt.log.client.Log;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.css.CSS;
import com.google.gwt.query.client.css.Length;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;
import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.DragFunction;
import gwtquery.plugins.draggable.client.events.DragContext;

import com.nl.tudelft.rdfgearsUI.client.RGType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public abstract class Node {
	protected Element root, header, content, border, closeButton, warningButton, loadingAnimation;
	protected boolean isPermanentNode = false;
	private String nodeId = "";
	private String name = "";
	private String elementId;
	private String NODE_CLASS = "node unselectable";
	private int headerHeight = 25; //node bar height in pixel
	private int entryHeight = 22;
	private int nodeWidth = 100;
	private int entryNum = 0;
	private OutputPort nodeOutputPort = null;
	private boolean isRemoved = false;
	private boolean isVisible = true;
	protected boolean isWorkflow = false;
	protected String workflowId = "";
	protected RGWorkflow owner;
	
	public RGCanvas canvas = null;
	protected ArrayList <Path> inputPaths = new ArrayList<Path>();
	protected ArrayList <Path> outputPaths = new ArrayList <Path>();
	private DraggableOptions draggOptions = new DraggableOptions();
	private int newX, newY;
	
	protected ArrayList <String> inputPorts = new ArrayList <String>();
	
	protected Map <String, Path>	entryId2Paths = new HashMap <String, Path>();
	protected Map <String, Element> entryId2Element = new HashMap <String, Element>();
	protected Map <String, Element> entryId2TextContainer = new HashMap <String, Element>();
	
	protected Map <String, String> portId2EntryId = new HashMap<String, String>();
	protected Map <String, String> entryId2PortId = new HashMap <String, String>();
	protected Map <String, String> portId2Name = new HashMap <String, String>();
	protected Map <String, NodePort> portName2Port = new HashMap <String, NodePort>();
	//protected Map <String, RGType> portId2Type = new HashMap <String, RGType>();
	protected Map <String, Element> inPortId2Element = new HashMap <String, Element>();
	protected Map <String, Element> iterateFlagId2Element = new HashMap <String, Element>();
	protected Map <String, String> iterateFlagId2EntryId = new HashMap <String, String>();
	protected Map <String, Boolean> entryId2iterateState = new HashMap <String, Boolean>();
	protected ArrayList <String> inputEntryId = new ArrayList <String>();
	//protected Map <String, RGType> outPortId2Type = new HashMap <String, RGType>();
	
	private Map <String, String> grouperId2HeaderId = new HashMap<String, String>();
	private Map <String, Element> grouperId2Element = new HashMap<String, Element>();
	
	protected ArrayList <String> paramIds = new ArrayList <String>();
	protected Map <String, RGFunctionParam> functionParamBuffer = new HashMap <String, RGFunctionParam>();
	private RGFunctionParam descParam = null;
	private RGPortTypeViewer typeViewer = null;
	
	public Node (String id, RGWorkflow _owner, final boolean withHelper){
		root = DOM.createDiv();
		nodeId = id;
		owner = _owner;
		elementId = HTMLPanel.createUniqueId() + "-" + id;
		root.setAttribute("class", NODE_CLASS);
		root.setAttribute("id", elementId);
		root.setAttribute("style", "position: absolute; left: 0; top: 0; height:"+headerHeight+"px; width:"+nodeWidth+"px;");
		
		border = DOM.createDiv();
		border.setAttribute("class", "nodeBorder");
		border.setAttribute("style", "position: absolute; left: -2px; top: -2px; height:"+headerHeight+"px; width:"+nodeWidth+"px;");
		root.appendChild(border);
		
		header =  DOM.createDiv();
		header.setAttribute("id", "header-" + elementId);
		header.setAttribute("class", "nodeHeader");
		header.setAttribute("style", "position:absolute; height: " + (headerHeight - 3) + "px; width:100%;");
		root.appendChild(header);
		
		if(!isPermanentNode){
			closeButton = DOM.createDiv();
			closeButton.setAttribute("id", "close-" + elementId);
			closeButton.setAttribute("class", "closeButton");
			closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
											  "height: 13px; width:13px;display: none;");
		
			root.appendChild(closeButton);
			
			warningButton = DOM.createDiv();
			warningButton.setAttribute("id", "warning-" + elementId);
			warningButton.setAttribute("class", "warningButton");
			warningButton.setAttribute("style", "position:absolute; right:10px; top:-6px; " +
											  "height: 13px; width:13px;display: none;");
		
			root.appendChild(warningButton);
			
		}
		
		content = DOM.createDiv();
		content.setAttribute("id", "content-"+elementId);
		content.setAttribute("class", "nodeContent");
		content.setAttribute("style", "position:absolute; height:100%; width:100%; top:"+ headerHeight +"px;");
		root.appendChild(content);
		
		if(withHelper){
			GQuery helper = $("<div id=\"dragHelper\" class=\"dragHelper\" style=\"width: 10px;height: 10px;\"></div>");
	
			draggOptions.setHelper(helper);
			draggOptions.setOnDragStart(new DragFunction(){
				public void f(DragContext context){
					$("#dragHelper").css(CSS.WIDTH.with(Length.px(nodeWidth)),
										 CSS.HEIGHT.with(Length.px(entryNum * entryHeight + headerHeight)));
				}
			});
			
			draggOptions.setOnDrag(new DragFunction() {
				public void f(DragContext context) {
					onDragOperationsWithHelper();
				}
			});
		}else{
			draggOptions.setOnDrag(new DragFunction() {
				public void f(DragContext context) {
					redrawPaths();
				}
			});
		}
		
		draggOptions.setOnDragStop(new DragFunction() {
			public void f(DragContext context){
				onDragStopOperations(withHelper);
			}
		});
	}
	
	private void onDragOperationsWithHelper(){
		newX = $("#dragHelper").left();
		newY = $("#dragHelper").top();
	}
	
	public void redrawPaths(){
		for(Path p: inputPaths){
			p.updateEndByPortPosition();
		}
		
		for(Path p: outputPaths){
			p.updateStartByPortPosition();
		}
	}
	
	private void onDragStopOperations(boolean withHelper){
		if(withHelper) setPosition(newX, newY);
		
		redrawPaths();
		owner.setIsModified(true);
	}
	
	public Node getInstance(){
		return this;
	}
	public void addInputPath(Path p){
		String eId = portId2EntryId.get(p.getEndPortId());
		entryId2Paths.put(eId, p);
		inputPaths.add(p);
	}
	public void addOutputPath(Path p){
		String eId = portId2EntryId.get(p.getStartPortId());
		entryId2Paths.put(eId, p);
		outputPaths.add(p);
	}
	
	/************ param buffer functions ***************/
	public void addParam(String id, String type, String value, String label){
		//param type desugaring
		if(type.equalsIgnoreCase("String")){
			functionParamBuffer.put(id, new RGFunctionParamString(id, value, label, this.getInstance()));
		}else if(type.equalsIgnoreCase("Fields")){
			functionParamBuffer.put(id, new RGFunctionParamFields(id, value, label, this.getInstance()));
			addStaticEntry(label ,label);
			resetSize();
		}else if(type.equalsIgnoreCase("Text")){
			functionParamBuffer.put(id, new RGFunctionParamText(id, value, label, this.getInstance()));
			addStaticEntry(canvas.createUniqueId(), label);
			resetSize();
		}else if(type.equalsIgnoreCase("Query")){
			functionParamBuffer.put(id, new RGFunctionParamQuery(id, value, label, this.getInstance()));
			addStaticEntry(canvas.createUniqueId(), label);
			resetSize();
		}else if(type.equalsIgnoreCase("InputFields")){
			String groupId = canvas.createUniqueId();
			addEntryGroupHolder(groupId, "Inputs");
			functionParamBuffer.put(id, new RGFunctionParamInputFields(id, value, label, groupId, this.getInstance()));
		}else if(type.equalsIgnoreCase("Option")){
			Log.error("Use addOptionParam() to add parameter with type option"); 
		}else if(type.equalsIgnoreCase("CONSTANT")){
			functionParamBuffer.put(id, new RGFunctionParamConstant(id, value, this.getInstance()));
		}else if(type.equalsIgnoreCase("wfTools")){
			functionParamBuffer.put(id, new RGFunctionParamWorkflowTools(id, this.getInstance()));
		}else if(type.equalsIgnoreCase("Description")){
			descParam = new RGFunctionParamDescription(id, label, value);
			return;
		}else{
			Log.error("Unknown param type: " + type);
			return;
		}
		
		paramIds.add(id);
	}
	//special case for parameter type option
	public void addListParam(String id, String value, String label, com.google.gwt.xml.client.Element embeddedXmlSource){
		paramIds.add(id);
		String groupId = canvas.createUniqueId();
		addEntryGroupHolder(groupId, label);
		functionParamBuffer.put(id, new RGFunctionParamList(id, value, label, embeddedXmlSource, groupId, getInstance()));

		resetSize();
	}
	
	public void deleteParam(String id){
		functionParamBuffer.get(id).removeElement();
		functionParamBuffer.remove(id);
		paramIds.remove(id);
	}
	
	public RGFunctionParam getParam(String id){
		return functionParamBuffer.get(id);
	}
	
	public void setParamValue(String id, String value){
		RGFunctionParam p = functionParamBuffer.get(id);
		p.setValueFromString(value);
	}
	
	public void displayParamForm(Element container){
		for(String pId: paramIds){
			RGFunctionParam p = functionParamBuffer.get(pId);
			p.display(container);
		}
		
		if(typeViewer == null){
			typeViewer = new RGPortTypeViewer(getInstance());
		}
		
		typeViewer.display(container);
		
		if(descParam != null){
			descParam.display(container);
		}
	}
	/*********** param buffer functions end ***********/
	
	public void setHeaderText(String s){
		if(s.length() > 11){
			nodeWidth = (s.length() * 8);
		}
		name = s;
		header.setInnerText(name);
	}
	
	public void setNodeDescription(String descText){
		if(descParam == null){
			descParam = new RGFunctionParamDescription(canvas.createUniqueId(), "Description", descText);
		}else{
			descParam.setValueFromString(descText);
		}
	}
	
	public void showHideNodeId(){
		if(header.getInnerText().equals(name)){
			header.setInnerText(getId());
		}else{
			header.setInnerText(name);
		}
	}
	public String getName(){
		return name;
	}
	public void setId(String id){
		nodeId = id;
		root.setAttribute(id, nodeId);
	}
	
	public void addContent(Element el){
		content.appendChild(el);
	}
	
	public void setContent(String str){
		content.setInnerHTML(str);
	}
	public void setCanvas(RGCanvas c){
		canvas = c;
	}
	public String getId(){
		return nodeId;
	}
	
	public void setIsModified(){
		owner.setIsModified(true);
	}
	
	public ArrayList <String> getParentsId(){
		ArrayList <String> parents = new ArrayList <String>();
		for(Path p: inputPaths){
			String parentId = p.getStartNode().getId();
			if(!parents.contains(parentId)){
				parents.add(parentId);
				ArrayList <String> gParents = p.getStartNode().getParentsId();
				for(String gpId: gParents){
					if(!parents.contains(gpId)){
						parents.add(gpId);
					}
				}
			}
		}
		
		return parents;
	}
	/**
	 * when connecting A --> B
	 * B will ask A (A.isCyclicTo(B)) if those connection will create cyclic
	 * @param destNodeId
	 * @return
	 */
	public boolean isCyclicTo(String nodeId){
		return(getParentsId().contains(nodeId));
	}
	
	public ArrayList <String> getPortNames(){
		ArrayList <String> keys = new ArrayList <String>();
		Set <String> keySet = portName2Port.keySet();
		Iterator <String> it = keySet.iterator();
		while(it.hasNext()){
			keys.add((String) it.next());
		}
		
		return keys;
	}
	public NodePort getPortByPortName(String portName){
		if(portName2Port.containsKey(portName)){
			return portName2Port.get(portName);
		}
		
		return null;
	}
	
	public String getPortNameByPortId(String portId){
		if(portId2Name.containsKey(portId)){
			return portId2Name.get(portId);
		}
		
		return "";
	}
	
	public void setPosition(int x, int y){
		root.getStyle().setLeft(x, Style.Unit.PX);
		root.getStyle().setTop(y, Style.Unit.PX);
		getX();
	}
	
	public int getX(){
		String x = root.getStyle().getLeft();
		x = x.replace("px", "");
		return Integer.parseInt(x);
	}
	
	public int getY(){
		String y = root.getStyle().getTop();
		y = y.replace("px", "");
		return Integer.parseInt(y);
	}
	public void setSelected(boolean s){
		if(s){
			root.addClassName("nodeActive");
			border.addClassName("nodeBorderActive");
		}else{
			root.removeClassName("nodeActive");
			border.removeClassName("nodeBorderActive");
		}
	}
	
	public void resetSize(){
		root.getStyle().setWidth(nodeWidth, Style.Unit.PX);
		border.getStyle().setWidth(nodeWidth, Style.Unit.PX);
		root.getStyle().setHeight(entryNum * entryHeight + headerHeight + (entryNum - 1), Style.Unit.PX);
		border.getStyle().setHeight(entryNum * entryHeight + headerHeight + (entryNum - 1), Style.Unit.PX);
		if(nodeOutputPort != null){
			nodeOutputPort.setAsNodeOuputPort(entryNum, entryHeight); //reset the position
		}
		
		redrawPaths();
	}

	public void setDraggable(boolean d){
		if(d){
			$("#" + elementId).as(Draggable).draggable(draggOptions);
		}else
			$("#" + elementId).as(Draggable).destroy();
	}
	
	/**
	 * input without input/output port
	 * @param text: display text
	 */
	private Element createEntry(String id, String text, boolean withIterate){
		int marg = 5;
		if(withIterate){
			marg = 18;
		}
		
		Element entry = DOM.createDiv();
		Element textContainer = DOM.createDiv();
		
		textContainer.setInnerText(text);
		textContainer.setAttribute("style", "position : absolute; " +
											"top:10%;" +
											"margin-left:" + marg + "px;" +
											"width:" + (nodeWidth - marg) + "px;" +
											"white-space: nowrap;" +
											"overflow: hidden;" +
											"text-overflow: ellipsis;");
		textContainer.setId(canvas.createUniqueId());
		entryId2TextContainer.put(id, textContainer);
		
		entry.appendChild(textContainer);
		entry.setAttribute("class", "entry");
		entry.setAttribute("id", id);
		
		String entryBorder = (entryNum == 0)? "" : "border-top: 1px solid #E3E4E9;";
		entry.setAttribute("style", "position: relative; width:100%; height:" + entryHeight + "px;" + entryBorder);
		
		return entry;
	}
	
	protected Element createIterateFlag(boolean val){
		Element iterateFlag = DOM.createDiv();
		
		iterateFlag.setId(canvas.createUniqueId());
		
		iterateFlag.setAttribute("style", "position: absolute; " +
											"width:8px; " +
											"height:8px; " +
											"left:5px;; " +
											"top:50%; " +
											"margin-top: -5px; " +
											"border: 1px solid #CFCFCF;");
		
		if(val){
			iterateFlag.setAttribute("class", "iterateFlagTrue");
		}else
			iterateFlag.setAttribute("class", "iterateFlagFalse");
		iterateFlag.setTitle("Iterate");
		return iterateFlag;
	}
	
	protected void setIterateFlagEventHandler(final String id){
		$("#" + id).click(new Function(){
			@Override
			public void f(){
				Element f = iterateFlagId2Element.get(id);
				String entryId = iterateFlagId2EntryId.get(id); 
				Boolean currentState = false;
				if(entryId2iterateState.containsKey(entryId)){
					currentState = entryId2iterateState.get(entryId);
				}
				
				if(!currentState){
					f.addClassName("iterateFlagTrue");
					entryId2iterateState.put(entryId, true);
					getPortByPortName(getPortNameByEntryId(entryId)).getType().setIterate(true);
					
					NodePort nodeOutputPort = getPortByPortName(getId());
					if(nodeOutputPort != null){
						nodeOutputPort.getType().setIterate(true);
					}
					
					canvas.doTypeCheck();
				}else{
					f.removeClassName("iterateFlagTrue");
					entryId2iterateState.put(entryId, false);
					getPortByPortName(getPortNameByEntryId(entryId)).getType().setIterate(false);
					NodePort nodeOutputPort = getPortByPortName(getId());
					int iterated = 0;
					for(String eId: inputEntryId){
						if(entryId2iterateState.containsKey(eId)){
							if(entryId2iterateState.get(eId)){
								iterated += 1;
							}
						}
					}
					
					if(nodeOutputPort != null && iterated == 0){
						nodeOutputPort.getType().setIterate(false);
					}
					
					canvas.doTypeCheck();
				}
				
			}
		});
		$("#" + id).mouseover(new Function(){
			@Override
			public void f(){
				if(canvas.getState() == RGCanvasState.NONE){
					canvas.setState(RGCanvasState.SETTING_ITERATE);
				}
			}
		});
		
		$("#" + id).mouseout(new Function(){
			@Override
			public void f(){
				if(canvas.getState() == RGCanvasState.SETTING_ITERATE){
					canvas.setState(RGCanvasState.NONE);
				}
			}
		});
		
	}
	
	public void updateInputPortIndicator(boolean v, RGType sourceType){		
		for(String pId: inputPorts){
			NodePort p = getPortByPortName(getPortNameByPortId(pId));
			if(v){
				if(getConnectedPath(pId) == null){
				//	if(canvas.getTypeChecker().isTypeMatch(sourceType, p.getType())){
						p.setActive(v);
				//	}
				}
			}else{
				p.setActive(v);
			}
		}
		
	}
	
	protected void addStaticEntry(String id, String text){
		Element entry = createEntry(id, text, false);
		entry.addClassName("staticEntry");
		this.addContent(entry);
		
		entryNum += 1;
		resetSize();
	}
	
	/**
	 * Entry with input port
	 * @param text: display text
	 * @throws Exception 
	 */
	protected void addInputEntry(String id, String name, RGType type, String text, boolean iterate){
		addInputEntry(id, name, type, text, iterate, null);
	}
	
	protected void addInputEntry(String id, String name, RGType type, String text, boolean iterate, String groupId){
		Element grouperElement = null;
		
		if(groupId != null){
			if(!grouperId2Element.containsKey(groupId))
				addEntryGroupHolder(groupId, "Inputs");
			
			grouperElement = grouperId2Element.get(groupId);
		}
		
		Element entry = createEntry(id, text, true);
		Element iterateFlag = createIterateFlag(iterate);
		NodePort port = new InputPort(canvas.createUniqueId(), getInstance(), type);
		entry.appendChild(iterateFlag);
		entry.appendChild(port.getElement());
		inputPorts.add(port.getId());
		portId2Name.put(port.getId(), name);
		portName2Port.put(name, port);
		//portId2Type.put(port.getId(), type);
		inPortId2Element.put(port.getId(), port.getElement());
		iterateFlagId2Element.put(iterateFlag.getAttribute("id"), iterateFlag);
		iterateFlagId2EntryId.put(iterateFlag.getAttribute("id"), id);
		portId2EntryId.put(port.getId(), id);
//		Log.debug("added portId2EntryId: " + port.getId() + "->" + id);
		
		entryId2Element.put(id, entry);
		entryId2PortId.put(id, port.getId());
		entryId2iterateState.put(id, iterate);
		inputEntryId.add(id);
		
		if(groupId != null){
			grouperElement.appendChild(entry);
		}else{
			this.addContent(entry);
		}
		entryNum += 1;
		resetSize();
		
		//event handler have to be set after node's HTML element added to the workspace (addContent)
		port.enableEventHandler();
		setIterateFlagEventHandler(iterateFlag.getAttribute("id"));
	}
	
	protected void addEntryGroupHolder(String id, String initLabel){
		Element groupHolder = DOM.createDiv();
		groupHolder.setId(id);
		grouperId2Element.put(id, groupHolder);
		
		String groupHeaderId = canvas.createUniqueId();
		Element entry = createEntry(groupHeaderId, initLabel, false);
		grouperId2HeaderId.put(id, groupHeaderId);
		entry.addClassName("staticEntry");
		groupHolder.appendChild(entry);
		this.addContent(groupHolder);
		
		entryNum += 1;
		resetSize();
	}
	protected void changeGroupHeaderText(String groupId, String text){
		String headerId = grouperId2HeaderId.get(groupId);
		this.updateEntryText(headerId, text);
	}
	
	public void removeEntry(String id){
		if(entryId2Paths.containsKey(id)){
			Path p = entryId2Paths.get(id);
			if(inputPaths.contains(p)){
				p.removeAsInputPath();
				inputPaths.remove(p);
			}else{
				p.removeAsOutputPath();
				outputPaths.remove(p);
			}
			
			entryId2Paths.remove(id);
		}
		if(entryId2PortId.containsKey(id)){
			String pId = entryId2PortId.get(id);
			if(portId2Name.containsKey(pId))
				portId2Name.remove(pId);
		}
		
		Element entry = entryId2Element.get(id);
		entry.removeFromParent();
		entryNum -= 1;
		resetSize();
		inputEntryId.remove(id);
		entryId2iterateState.remove(id);
		
		entryId2Element.remove(id);
		entryId2PortId.remove(id);
		
	}
	
	public void updateEntryText(String id, String text){
		Element entry = entryId2TextContainer.get(id);
		entry.setInnerText(text);
	}
	public void updatePortNameByEntryId(String id, String name){
		String oldName = getPortNameByEntryId(id);
		NodePort port = getPortByPortName(oldName);
		portName2Port.remove(oldName);
		portName2Port.put(name, port);
		String pId = entryId2PortId.get(id);
		portId2Name.put(pId, name);
		
	}
	public String getPortNameByEntryId(String id){
		String pId = entryId2PortId.get(id);
//		Log.debug("the port id: " + pId);
		return portId2Name.get(pId);
	}
	public Path getConnectedPath(String portId){
			String eId = portId2EntryId.get(portId);
			if(entryId2Paths.containsKey(eId)){
				return entryId2Paths.get(eId);
			}
		return null;
	}
	/**
	 * entry with output port
	 * @param text: display text
	 * @throws Exception 
	 */
	protected void addOutputEntry(String id, String name, RGType type, String text){
		Element entry = createEntry(id, text, false);
		NodePort port = new OutputPort(canvas.createUniqueId(), getInstance(), type);
		entry.appendChild(port.getElement());
		entryId2Element.put(id, entry);
		portId2EntryId.put(port.getId(), id);
		//portId2Type.put(port.getId(), type);
		portId2Name.put(port.getId(), name);
		portName2Port.put(name, port);
		entryId2PortId.put(id, port.getId());
		this.addContent(entry);
		
		port.enableEventHandler();
		
		entryNum += 1;
		resetSize();
	}
	
	protected void addNodeOutputPort(RGType type){
		OutputPort port = new OutputPort(canvas.createUniqueId(), getInstance(), type);
		nodeOutputPort = port;
		port.setAsNodeOuputPort(entryNum, entryHeight);
		
		//portId2Type.put(port.getId(), type);
		portId2EntryId.put(port.getId(), getId());
		portId2Name.put(port.getId(), getId());
		portName2Port.put(getId(), port);
		
		this.addContent(port.getElement());
		port.enableEventHandler();
	}
	
	protected void addNodeInputPort(RGType type){
		InputPort port = new InputPort(canvas.createUniqueId(), getInstance(), type);
		port.setAsNodeInputPort(entryNum, entryHeight, headerHeight);
		
		inputPorts.add(port.getId());
		portId2EntryId.put(port.getId(), getId());
		portName2Port.put(getId(), port);
		inputPorts.add(port.getId());
		portId2Name.put(port.getId(), getId());
		//portId2Type.put(port.getId(), type);
		inPortId2Element.put(port.getId(), port.getElement());
		
		this.addContent(port.getElement());
		
		port.enableEventHandler();
	}
	
	protected void showLoadingAnimation(){
		loadingAnimation = DOM.createDiv();
		loadingAnimation.setAttribute("style", "width:100%; text-align:center;padding-top:4px;");
		loadingAnimation.setInnerHTML("<img src=\"images/loader.gif\">");
		content.appendChild(loadingAnimation);
		entryNum += 1;
		resetSize();
	}
	
	protected void removeLoadingAnimation(){
		loadingAnimation.removeFromParent();
		entryNum -= 1;
	}
	
	public void remove(){
		isRemoved = true;
		for(Path p: inputPaths){
			p.removeAsInputPath();
		}
		
		for(Path p: outputPaths){
			p.removeAsOutputPath();
		}
//		canvas.removeNode(getId());
		root.removeFromParent();
	}
	
	public void removePathReference(Path p){
		String portId = null;
		if(inputPaths.contains(p)){
			inputPaths.remove(p);
			portId = p.getEndPortId();
		}
		
		if(outputPaths.contains(p)){
			outputPaths.remove(p);
			portId = p.getStartPortId();
		}
		
		if(portId2EntryId.containsKey(portId)){
			String eId = portId2EntryId.get(portId);
			entryId2Paths.remove(eId);
		}
	}
	public void setVisible(boolean v){
		if(v){
			root.removeClassName("hidden");
			isVisible = false;
		}else{
			root.addClassName("hidden");
			isVisible = true;
		}
		
		for(Path p: inputPaths){
			p.setVisible(v);
		}
	}
	public boolean isVisible(){
		return isVisible;
	}
	
	public boolean isAWorkflowNode(){
		return isWorkflow;
	}
	public String getWorkflowId(){
		return workflowId;
	}
	
	public void setWorkflowId(String wfId){
		workflowId = wfId;
	}
	
	public void showWarningButton(boolean v){
		Log.debug("setting warning button");
		if(v){
			warningButton.getStyle().setDisplay(Style.Display.BLOCK);
		}else{
			warningButton.getStyle().setDisplay(Style.Display.NONE);
		}
	}
	
	protected void setupRootEventHandler(){
		
		setDraggable(true);
		
		$("#" + elementId).click(new Function(){
			@Override
			public void f(){
				if(canvas.getState() == RGCanvasState.NONE && !isRemoved)
					canvas.setActiveNode(getInstance());
				
				if(canvas.getState() == RGCanvasState.REMOVING_PATH){
					canvas.setState(RGCanvasState.NONE);
				}
			}
		});
		
		
			//show hide close button
			$("#" + elementId).mouseover(new Function(){
				@Override
				public void f(){
					canvas.setMouseState(RGMouseState.ON_NODE);
					if(!isPermanentNode){
						closeButton.getStyle().setDisplay(Style.Display.BLOCK);
					}
					//$("#close-" + getId()).fadeIn(1000);
				}
			});
			
			$("#" + elementId).mouseout(new Function(){
				@Override
				public void f(){
					//$("#close-" + getId()).fadeOut(1000);
					if(canvas.getState() == RGCanvasState.NONE)
						canvas.setMouseState(RGMouseState.FREE);
					
					if(!isPermanentNode){
						closeButton.getStyle().setDisplay(Style.Display.NONE);
					}
				}
			});
			
			//button handler
		if(!isPermanentNode){
			String closeButtonId = closeButton.getId();
			
			$("#" + closeButtonId).mouseover(new Function(){
				@Override
				public void f(){
					if(canvas.getState() == RGCanvasState.NONE){
						canvas.setState(RGCanvasState.REMOVING);
						closeButton.addClassName("closeButtonActive");
					}
				}
			});
			
			$("#" + closeButtonId).mouseout(new Function(){
				@Override
				public void f(){
					//$("#close-" + getId()).fadeOut(1000);
					if(canvas.getState() == RGCanvasState.REMOVING){
						canvas.setState(RGCanvasState.NONE);
						closeButton.removeClassName("closeButtonActive");
					}
				}
			});
			
			$("#" + closeButtonId).click(new Function(){
				@Override
				public void f(){
//					isRemoved = true;
					remove();
					owner.removeNodeById(getId());
					canvas.removeFromActiveNode(getId());
					canvas.setState(RGCanvasState.NONE);
					canvas.doTypeCheck();
				}
			});
			
			
			String warningButtonId = warningButton.getId();
			
			$("#" + warningButtonId).mouseover(new Function(){
				@Override
				public void f(){
					if(canvas.getState() == RGCanvasState.NONE){
						canvas.setState(RGCanvasState.REMOVING);
						warningButton.addClassName("warningButtonActive");
					}
				}
			});
			
			$("#" + warningButtonId).mouseout(new Function(){
				@Override
				public void f(){
					//$("#close-" + getId()).fadeOut(1000);
					if(canvas.getState() == RGCanvasState.REMOVING){
						canvas.setState(RGCanvasState.NONE);
						warningButton.removeClassName("warningButtonActive");
					}
				}
			});
			
			$("#" + warningButtonId).click(new Function(){
				@Override
				public void f(){
					canvas.refreshWorkflowNode(workflowId, getId());
				}
			});
		}
	}
	
	/**
	 * Abstract methods, have to be implemented by the child class
	 */
	abstract void draw(RGCanvas canvas);
	abstract void displayProperties(Element container);
	abstract com.google.gwt.xml.client.Element toXml(com.google.gwt.xml.client.Document doc);
}
