package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;
import static gwtquery.plugins.draggable.client.Draggable.Draggable;

import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.DragFunction;
import gwtquery.plugins.draggable.client.events.DragContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery.Offset;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.nl.tudelft.rdfgearsUI.client.RGServiceAsync;
import com.nl.tudelft.rdfgearsUI.client.RGType;
import com.nl.tudelft.rdfgearsUI.client.RGTypeChecker;

enum RGCanvasState {
	CONNECTING, //when connecting ports in process
	CONNECTED,
	DRAGGING, //moving nodes
	REMOVING,
	REMOVING_PATH,
	SETTING_ITERATE,
	NONE //no active process
}

enum RGMouseState {
	ON_NODE,
	ON_PANEL,
	FREE
}

enum NodeDrawingState{
	DRAWING,
	DONE,
	ERROR
}

public class RGCanvas extends Widget{
	private RGServiceAsync RService = null;
	private RGPropertyPanel propertyPanel = null;
	private RGNavigationPanel navigationPanel = null;
	private RGTabBar tabBar = null;
	private RGTypeChecker typeChecker = null;
	private String RDFGearsRestBaseUrl = "http://localhost:8080"; //configure this on server
	private String elementId = "canvas";
	public int nodeIndex = 0;
	private Map <String, RGWorkflow> openedWorkflows = new HashMap <String, RGWorkflow>();
	private ArrayList <String> openedWorkflowIds = new ArrayList <String>();
	private RGWorkflow activeWorkflow = null;
	
	private boolean draggerWithHelper = true;
	
	private Timer timer = null;
	private RGCanvasState state;
	private RGMouseState mouseState;
	//atributes need in process of creating connection between ports
	private NodePort targetPortCandidate = null;
	private Path activePath; //current path being created
	private Node startNode;
	private Node targetNode;
	private Node activeNode = null;
	private RGType startPortType = null; //cache the start port type
	
	
	private String latestNId = null;
	
	public int TOP_MARGIN = 0;
	public int LEFT_MARGIN = 0;
	
	
	public int workspaceWidth = 0;
	public int workspaceHeight = 0;
	public int canvasHeight = 0;
	public int canvasWidth = 0;
	public int canvasPosX = 0;
	public int canvasPosY = 0;
	
	//constants
	public static final String WORKFLOW_INPUT_NODE_ID = "WORKFLOW_INPUT";
	public static final String WORKFLOW_OUTPUT_NODE_ID = "WORKFLOW_OUTPUT";
	
	private Element loadingAnimation = null;
	private Element errorPanel, messagePanel, globalOverlay;
	private boolean errorPanelDisplayed = false;
	private boolean messagePanelDisplayed = false;
	private boolean popupPanelDisplayed = false;
	private DraggableOptions draggOptions = new DraggableOptions();
	private boolean isDraggingCanvas = false;
	
	JavaScriptObject cmEditor = null;
	private boolean copyReplace = false;
	/**
	 * create canvas by using existing HTML DIV element
	 * @param elementId
	 */
	public RGCanvas (String _elementId){
		this.elementId = _elementId;

		canvasWidth = DOM.getElementById("workspace").getClientWidth();
		canvasHeight = DOM.getElementById("workspace").getClientHeight();
		workspaceWidth = canvasWidth;
		workspaceHeight = canvasHeight;
		
		Element c = DOM.getElementById("canvas");
		c.getStyle().setPosition(Position.ABSOLUTE);
		c.getStyle().setTop(0, Unit.PX);
		c.getStyle().setLeft(0, Unit.PX);
		c.getStyle().setWidth(canvasWidth, Unit.PX);
		c.getStyle().setHeight(canvasHeight, Unit.PX);
		
		initEventHandler();
		state = RGCanvasState.NONE;
		mouseState = RGMouseState.FREE;
		
		draggOptions.setOnDragStart(new DragFunction(){
			public void f(DragContext context) {
				canvasPosX = $("canvas").left();
				canvasPosY = $("canvas").top();
			}
		});
		draggOptions.setOnDrag(new DragFunction(){
			public void f(DragContext context) {
				isDraggingCanvas = true;
			}
		});
		
		draggOptions.setOnDragStop(new DragFunction(){
			public void f(DragContext context) {
				canvasPosX = $("#"+ elementId).left();
				canvasPosY = $("#"+ elementId).top();
				isDraggingCanvas = false;
				canvasPosX = (canvasPosX > 0)? 0 : canvasPosX;
				canvasPosY = (canvasPosY > 0)? 0 : canvasPosY;
				reconfigureCanvas();
				$("#"+ elementId).as(Draggable).destroy();
			}
		});
	}
	
	public void handleWindowResizeEvent(){
		workspaceWidth = DOM.getElementById("workspace").getClientWidth();
		workspaceHeight = DOM.getElementById("workspace").getClientHeight();
		canvasWidth = workspaceWidth + activeWorkflow.getDraggLeftMargin();
		canvasHeight = workspaceHeight + activeWorkflow.getDraggTopMargin();
		
		Element c = DOM.getElementById("canvas");
		c.getStyle().setTop(canvasPosY, Unit.PX);
		c.getStyle().setLeft(canvasPosX, Unit.PX);
		c.getStyle().setWidth(canvasWidth, Unit.PX);
		c.getStyle().setHeight(canvasHeight, Unit.PX);
		
		navigationPanel.handleWindowResizeEvent();
	
	}
	
	public void resetCanvas(){
		Element c = DOM.getElementById("canvas");
		c.getStyle().setTop(0, Unit.PX);
		c.getStyle().setLeft(0, Unit.PX);
		canvasPosX = 0;
		canvasPosY = 0;
	}
	public void reconfigureCanvas(){
		canvasWidth = workspaceWidth - canvasPosX;
		canvasHeight = workspaceHeight - canvasPosY;
		
		activeWorkflow.setDraggTopMargin(0 - canvasPosY);
		activeWorkflow.setDraggLeftMargin(0 - canvasPosX);
		
		Element c = DOM.getElementById("canvas");
		c.getStyle().setTop(canvasPosY, Unit.PX);
		c.getStyle().setLeft(canvasPosX, Unit.PX);
		c.getStyle().setWidth(canvasWidth, Unit.PX);
		c.getStyle().setHeight(canvasHeight, Unit.PX);
		
	}
	public void reconfCanvasPositionFor(RGWorkflow w){
		canvasPosX = 0 - w.getDraggLeftMargin();
		canvasPosY = 0 - w.getDraggTopMargin();
		
		Element c = DOM.getElementById("canvas");
		c.getStyle().setTop(canvasPosY, Unit.PX);
		c.getStyle().setLeft(canvasPosX, Unit.PX);
		
	}
	public void initEventHandler(){
		$("#"+ elementId).css("cursor","default");
		$("#"+ elementId).mouseup(new Function(){
			  public void f(){
				 if(!isDraggingCanvas) $("#"+ elementId).as(Draggable).destroy();
				  $("#"+ elementId).unbind(Event.ONMOUSEMOVE);
//				  $("#" + elementId).css("cursor","auto");
				  if(!isTargetPortExist() && isConnecting()){ 
					  setState(RGCanvasState.NONE);
					  activePath.remove();
					  activePath = null;

					  for(String nId: activeWorkflow.getNodeIds()){
						 if(!nId.equalsIgnoreCase(startNode.getId())){
//							Node n = nodes.get(nId);
							Node n = activeWorkflow.getNode(nId);
							n.updateInputPortIndicator(false, startPortType);
						 }
					  }
					  startPortType = null;
					 // outputPaths.remove(outputPaths.size() - 1); //remove the object
				  }else if(isTargetPortExist() && isConnected()){
					  activePath.setId(createUniqueId());
					  activePath.setEndPort(targetPortCandidate);
					  activePath.setEndPortId(targetPortCandidate.getId());
//					  activePath.updateEndWithMargin(4 - (LEFT_MARGIN + draggLeftMargin), 4 - (TOP_MARGIN + draggTopMargin));
					  activePath.updateEndByPortPosition();
					  startNode.addOutputPath(activePath);
					  targetNode.addInputPath(activePath);
					  activePath.setStartNode(startNode);
					  activePath.setEndNode(targetNode);
					  
					  resetTargetPortCandidate();
					  setState(RGCanvasState.NONE);
					  activeWorkflow.setIsModified(true);
					  
					  for(String nId: activeWorkflow.getNodeIds()){
						if(!nId.equalsIgnoreCase(startNode.getId())){
//							Node n = nodes.get(nId);
							Node n = activeWorkflow.getNode(nId);
							n.updateInputPortIndicator(false, startPortType);
						 }
					  }
					  startPortType = null;
					  doTypeCheck();
				  }
			  }
		  });
	
		$("#"+ elementId).mousedown(new Function (){
				public boolean f(Event e){
					if(isConnecting()){
//						$("#" + elementId).css("cursor","pointer");
						for(String nId: activeWorkflow.getNodeIds()){
							if(!nId.equalsIgnoreCase(startNode.getId())){
								Node n = activeWorkflow.getNode(nId);
								n.updateInputPortIndicator(true, startPortType);
							 }
						  }
						
						$("#"+ elementId).bind(Event.ONMOUSEMOVE, null, new Function(){
							@Override
							public boolean f(Event e){
									activePath.updateEndByClientPos(e.getClientX() + getLeftMargin(), e.getClientY() + getTopMargin());
								return true;
							}
						});
					}else if(mouseState == RGMouseState.FREE && state == RGCanvasState.NONE){
						$("#"+ elementId).as(Draggable).draggable(draggOptions);
					}
					return true;
				}
		});
		
		$("#" + elementId).click(new Function (){
			public void f(){
				if(mouseState == RGMouseState.FREE && state == RGCanvasState.NONE){
					propertyPanel.showWorkflowProperty(activeWorkflow);
				}
			}
		});
		
	}
	public void setTabBarPanel(RGTabBar tb){
		tabBar = tb;
	}
	
	public RGTabBar getTabBarPanel(){
		return tabBar;
	}
	
	public void setPropertyPanel(RGPropertyPanel p){
		propertyPanel = p;
	}
	
	public void setNavigationPanel(RGNavigationPanel p){
		navigationPanel = p;
	}
	
	public void setRemoteService (RGServiceAsync rs){
		RService = rs;
	}
	
	public void setRdfGearsRestBaseUrl(String url){
		RDFGearsRestBaseUrl = url;
		Log.debug("base rest url:" + url);
	}
	public void setTypeChecker (RGTypeChecker tc){
		typeChecker = tc;
	}
	
	public RGServiceAsync getRemoteService (){
		return RService;
	}
	
	public RGPropertyPanel getPropertyPanel(){
		return propertyPanel;
	}
	
	public RGNavigationPanel getNavigationPanel(){
		return navigationPanel;
	}
	
	public RGTypeChecker getTypeChecker(){
		if(typeChecker == null)
			displayErrorMessage("Type checker cannot be found, please report this error");
		
		return typeChecker;
	}
	public void setMargin(int top, int left){
		TOP_MARGIN = top;
		LEFT_MARGIN = left;
	}
	
	public int getLeftMargin(){
		return activeWorkflow.getDraggLeftMargin() - LEFT_MARGIN;
	}
	public int getTopMargin(){
		return activeWorkflow.getDraggTopMargin() - TOP_MARGIN;
	}
	public String getElementId(){
		return elementId;
	}
	public RGWorkflow getActiveWorkflow(){
		return activeWorkflow;
	}
	public Element getElement(){
		return DOM.getElementById(elementId);
	}
		
	public void drawPath(Path p){
		p.draw(getElement());
	}
	public void setOriginNode(Node n){
		startNode = n;
	}
	
	public void setTargetNode(Node n){
		targetNode = n;
	}
	
	public void setTargetPortCandidate(NodePort port){
		targetPortCandidate = port;
	}
	
	public void resetTargetPortCandidate(){
		targetPortCandidate = null;
	}
	
	public boolean isTargetPortExist(){
		if(targetPortCandidate != null)
			return true;
			
		return false;
	}
	public NodePort getTargetPort(){
		return targetPortCandidate;
	}
	
	public boolean isConnecting(){
		if(state == RGCanvasState.CONNECTING) return true;
		
		return false;
	}
	
	public boolean isConnected(){
		if(state == RGCanvasState.CONNECTED) return true;
		
		return false;
	}
	
	public RGCanvasState getState(){
		return state;
	}
	public void setState(RGCanvasState s){
		state = s;
//		Log.debug("state: " + state);
	}
	public void setMouseState(RGMouseState s){
		mouseState = s;
	}
	
	public Path createNewPath(int sX, int sY, int eX, int eY){
		activePath = new Path(sX, sY, eX, eY);
		activePath.setId(createUniqueId());
		return activePath;
	}
	
	public void setActivePath(Path p){
		activePath = p;
	}
	public Path getActivePath(){
		return activePath;
	}
	public void doTypeCheck(){
		if(typeChecker.rewriteNetworkType(activeWorkflow.getNodeIds(), activeWorkflow.getNodes())){
			activeWorkflow.setWellTypeness(true);
		}else{
			activeWorkflow.setWellTypeness(false);
		}
	}
	
	public void doTypeCheck(RGWorkflow wf){
		if(typeChecker.rewriteNetworkType(wf.getNodeIds(), wf.getNodes())){
			wf.setWellTypeness(true);
		}else{
			wf.setWellTypeness(false);
		}
	}
	
	/**
	 * Draw connector between nodes
	 * 
	 * @param node Existing node on the canvas
	 * @param portId Existing node's output port
	 */
	public void drawConnectionFrom(Node n, NodePort port){
//		Offset pos = port.getCenterPosition(TOP_MARGIN + draggTopMargin, LEFT_MARGIN + draggLeftMargin);
		Offset pos = port.getCenterCoordinate();
		this.drawPath(createNewPath(pos.left, pos.top, pos.left, pos.top));
		activePath.setStartPortId(port.getId());
		activePath.setStartPort(port);
		activePath.setStartNode(n);
		startNode = n;
		startPortType = port.getType();
	}
	
	public void drawConnection(Node from, NodePort sPort, Node to, NodePort ePort){
//		Log.debug("draw connection from node:" + from.getId() + " to:" + to.getId());
		drawConnectionFrom(from, sPort);
		
		activePath.setId(createUniqueId());
		activePath.setEndPortId(ePort.getId());
		activePath.setEndPort(ePort);
//		activePath.updateEndWithMargin(4 - LEFT_MARGIN, 4 - TOP_MARGIN);
		activePath.updateEndByPortPosition();
		from.addOutputPath(activePath);
		to.addInputPath(activePath);
		activePath.setStartNode(from);
		activePath.setEndNode(to);
	}
	
	public void setActiveNode(Node n){
		if(activeNode != null)
			activeNode.setSelected(false);
		
		activeNode = n;
		activeNode.setSelected(true);
		propertyPanel.setActiveNode(activeNode);
	}
	
	
	public String createUniqueId(){
		return HTMLPanel.createUniqueId();
	}
	
	public boolean replaceOpenedWorkflowId(String oldId, String newId){
		if(openedWorkflowIds.contains(oldId) && !openedWorkflowIds.contains(newId)){
			if(tabBar.replaceOpenedWorkflowId(oldId, newId)){
				openedWorkflowIds.remove(oldId);
				RGWorkflow ref = openedWorkflows.get(oldId);
				openedWorkflows.remove(oldId);
				openedWorkflows.put(newId, ref);
				return true;
			}
		}
		
		return false;
	}
	public void setContainerSize(int width, int height){
		DOM.getElementById(elementId).setPropertyDouble("width", width);
		DOM.getElementById(elementId).setPropertyDouble("height", height);
	}
	
	public void resize(int width, int height){
		this.setContainerSize(width, height);
	}
	
	/**
	 * caller must have value property to store text, example text area
	 * @param caller
	 */
	public void displayPopupSPARQLEditor(final RGFunctionParamQuery caller){
		if(messagePanelDisplayed || errorPanelDisplayed || popupPanelDisplayed)
			return;
		
		
		globalOverlay  = getGlobalOverlay();
		final String editorId = createUniqueId();
		final Element editorPanel = DOM.createDiv();
		Element closeButton = DOM.createDiv();
		int x = (getElement().getClientWidth() / 2) - 325;
		closeButton.setAttribute("id", "popupPanelCloseButton");
		closeButton.setAttribute("class", "closeButton");
		closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
										  "height: 13px; width:13px;display: block;");
		editorPanel.setAttribute("id", "popupMsgPanel");
		editorPanel.setClassName("popupPanel");
		editorPanel.setAttribute("style", "position:absolute; " +
									"padding:5px; " +
									"width:600px;" +
									"height:343px;" +
									"background:white; " +
									"left: "+x+"px; " +
									"top:55px;" +
									"border: 1px solid #5F5C5C;" +
									"border-radius:3px;" +
									"moz-border-radius:3px;");
		Element cap = DOM.createDiv();
		cap.setInnerText("SPARQL Editor");
		cap.setAttribute("style", "font-size:14px;font-weight:bold;padding:5px;");
		editorPanel.appendChild(cap);
		Element queryEditor = DOM.createTextArea();
		queryEditor.setId(editorId);
		queryEditor.setAttribute("name", editorId);
		queryEditor.setAttribute("style", "width:590px; height:500px;");
		queryEditor.setInnerText(caller.getValue());
		editorPanel.appendChild(queryEditor);
		
		editorPanel.appendChild(closeButton);
		
		globalOverlay.appendChild(editorPanel);
		DOM.getElementById("app-container").appendChild(globalOverlay);
		//$("#sparqleditor").text(caller.getValue());
		cmEditor = UtilJSWrapper.setAsCMSparqlEditor(editorId);
//		getElement().appendChild(error);
		popupPanelDisplayed = true;
		
		$("#popupPanelCloseButton").click(new Function(){
			public void f(){
				String query = UtilJSWrapper.getEditorValue(cmEditor);// $("#" + editorId).val();
				editorPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
				caller.setValue(query);
			}
		});
		
	}
	
	public void displayPopupXmlEditor(String xml, boolean readOnly){
		if(messagePanelDisplayed || errorPanelDisplayed || popupPanelDisplayed)
			return;
		
		
		globalOverlay  = getGlobalOverlay();
		final String editorId = createUniqueId();
		final Element editorPanel = DOM.createDiv();
		Element closeButton = DOM.createDiv();
		int x = (getElement().getClientWidth() / 2) - 325;
		closeButton.setAttribute("id", "popupPanelCloseButton");
		closeButton.setAttribute("class", "closeButton");
		closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
										  "height: 13px; width:13px;display: block;");
		editorPanel.setAttribute("id", "popupMsgPanel");
		editorPanel.setClassName("popupPanel");
		editorPanel.setAttribute("style", "position:absolute; " +
									"padding:5px; " +
									"width:600px;" +
									"height:600px;" +
									"background:white; " +
									"left: "+x+"px; " +
									"top:55px;" +
									"border: 1px solid #5F5C5C" +
									"border-radius:3px;" +
									"moz-border-radius:3px;");
		Element cap = DOM.createDiv();
		cap.setInnerText("Workflow Source");
		cap.setAttribute("style", "font-size:14px;font-weight:bold;padding:5px;");
		editorPanel.appendChild(cap);
		Element queryEditor = DOM.createDiv();
		queryEditor.setId(editorId);
		queryEditor.setAttribute("name", editorId);
//		queryEditor.setAttribute("style", "width:590px; height:500px;");
//		queryEditor.setInnerText(caller.getValue());
		editorPanel.appendChild(queryEditor);
		
		editorPanel.appendChild(closeButton);
		
		globalOverlay.appendChild(editorPanel);
		DOM.getElementById("app-container").appendChild(globalOverlay);
		//$("#sparqleditor").text(caller.getValue());
		if(readOnly){
			cmEditor = UtilJSWrapper.setAsCMXmlViewer(editorId);
			UtilJSWrapper.setEditorValue(cmEditor, xml);
		}else{
			cmEditor = UtilJSWrapper.setAsCMXmlEditor(editorId);
		}
		
		Element buttonFCont, parseButton;
		String parseButtonId = createUniqueId();
		
		if(readOnly){
			UtilJSWrapper.setEditorSize(cmEditor, 600, 570);
		}else{
			UtilJSWrapper.setEditorSize(cmEditor, 600, 520);
			
			buttonFCont = DOM.createDiv();
			buttonFCont.setAttribute("style", "padding-left: 250px;");
			parseButton = DOM.createDiv();
			parseButton.setInnerText("Parse Source File");
			parseButton.setId(parseButtonId);
			parseButton.setClassName("popupPanelFormButton");
			buttonFCont.appendChild(parseButton);
			editorPanel.appendChild(buttonFCont);
		}
//		getElement().appendChild(error);
		popupPanelDisplayed = true;
		
		$("#popupPanelCloseButton").click(new Function(){
			public void f(){
				editorPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
			}
		});
		
		if(!readOnly){
			$("#" + parseButtonId).click(new Function(){
				public void f(){
					String source = UtilJSWrapper.getEditorValue(cmEditor);
					editorPanel.removeFromParent();
					globalOverlay.removeFromParent();
					popupPanelDisplayed = false;
					parseAndDrawWorkflowGraph(source);
				}
			});
		}
		
	}
	
	
	private Element getGlobalOverlay(){
		Element overlay = DOM.createDiv();
		overlay.setAttribute("style", "position:absolute;" +
				"top:0px; " +
				"bottom:0px; " +
				"left:0px; " +
				"right:0px;" +
				"background: rgba(255,255,255, 0.2); z-index: 99999");
		
		return overlay;
	}
	
	public void displayErrorMessage(String msg){
		if(messagePanelDisplayed)
			return;
		
		if(!errorPanelDisplayed){
			globalOverlay = DOM.createDiv();
			globalOverlay.setAttribute("style", "position:absolute;" +
												"top:0px; " +
												"bottom:0px; " +
												"left:0px; " +
												"right:0px;" +
												"background: rgba(255,255,255, 0.2); z-index: 99999");
			errorPanel = DOM.createDiv();
			Element closeButton = DOM.createDiv();
			int x = (getElement().getClientWidth() / 2) - 150;
			closeButton.setAttribute("id", "errorMessageCloseButton");
			closeButton.setAttribute("class", "closeButton");
			closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
											  "height: 13px; width:13px;display: block;");
			errorPanel.setAttribute("id", "errorMsgPanel");
			errorPanel.setClassName("popupPanel");
			errorPanel.setAttribute("style", "position:absolute; " +
										"padding:5px; " +
										"width:300px;" +
										"background:#E03939; " +
										"left: "+x+"px; " +
										"top:55px;" +
										"color: white;" +
										"border: 1px solid #A72323;" +
										"border-radius:3px;" +
										"moz-border-radius:3px;" +
										"font-weight: bolder;" +
										"font-size: 85%");
			errorPanel.setInnerHTML(msg);
			
			errorPanel.appendChild(closeButton);
			
			globalOverlay.appendChild(errorPanel);
			DOM.getElementById("app-container").appendChild(globalOverlay);
	//		getElement().appendChild(error);
			errorPanelDisplayed = true;
			
			$("#errorMessageCloseButton").click(new Function(){
				public void f(){
					errorPanel.removeFromParent();
					globalOverlay.removeFromParent();
					errorPanelDisplayed = false;
				}
			});
		}else{
			errorPanel.removeFromParent();
			globalOverlay.removeFromParent();
			errorPanelDisplayed = false;
			displayErrorMessage(errorPanel.getInnerHTML() + "<br/>" + msg);
		}
	}
	
	public void displayMessage(String msg){
		if(errorPanelDisplayed)
			return;
		
		if(!messagePanelDisplayed){
			globalOverlay = DOM.createDiv();
			globalOverlay.setAttribute("style", "position:absolute;" +
												"top:0px; " +
												"bottom:0px; " +
												"left:0px; " +
												"right:0px;" +
												"background: rgba(255,255,255, 0.2); z-index: 99999");
			messagePanel = DOM.createDiv();
			Element closeButton = DOM.createDiv();
			int x = (getElement().getClientWidth() / 2) - 150;
			closeButton.setAttribute("id", "messageCloseButton");
			closeButton.setAttribute("class", "closeButton");
			closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
											  "height: 13px; width:13px;display: block;");
			messagePanel.setAttribute("id", "msgPanel");
			messagePanel.setClassName("popupPanel");
			messagePanel.setAttribute("style", "position:absolute; " +
										"padding:5px; " +
										"width:300px;" +
										"background:#39E082; " +
										"left: "+x+"px; " +
										"top:55px;" +
										"color: white;" +
										"border: 1px solid #3DA723;" +
										"border-radius:3px;" +
										"moz-border-radius:3px;" +
										"font-weight: bolder;" +
										"font-size: 85%");
			messagePanel.setInnerHTML(msg);
			
			messagePanel.appendChild(closeButton);
			
			globalOverlay.appendChild(messagePanel);
			DOM.getElementById("app-container").appendChild(globalOverlay);
	//		getElement().appendChild(error);
			messagePanelDisplayed = true;
			
			$("#messageCloseButton").click(new Function(){
				public void f(){
					messagePanel.removeFromParent();
					globalOverlay.removeFromParent();
					messagePanelDisplayed = false;
				}
			});
		}else{
			messagePanel.removeFromParent();
			globalOverlay.removeFromParent();
			messagePanelDisplayed = false;
			displayErrorMessage(messagePanel.getInnerHTML() + "<br/>" + msg);
		}
	}
	
	public void showAppLoader(String msg){
		globalOverlay = DOM.createDiv();
		globalOverlay.setAttribute("style", "position:absolute;" +
											"top:0px; " +
											"bottom:0px; " +
											"left:0px; " +
											"right:0px;" +
											"background: rgba(255,255,255, 0.2); z-index: 99999");
		messagePanel = DOM.createDiv();
		int x = (getElement().getClientWidth() / 2) - 150;
		messagePanel.setAttribute("id", "msgPanel");
		messagePanel.setClassName("popupPanel");
		messagePanel.setAttribute("style", "position:absolute; " +
									"padding:5px; " +
									"width:300px;" +
									"background:#39E082; " +
									"left: "+x+"px; " +
									"top:55px;" +
									"color: white;" +
									"border: 1px solid #3DA723;" +
									"border-radius:3px;" +
									"moz-border-radius:3px;" +
									"font-weight: bolder;" +
									"font-size: 85%");
		messagePanel.setInnerHTML(msg);
		
		globalOverlay.appendChild(messagePanel);
		DOM.getElementById("app-container").appendChild(globalOverlay);	
	}
	
	public void removeAppLoader(){
		globalOverlay.removeFromParent();
		messagePanel.removeFromParent();
	}
	
	public void showLoadingAnimation(){
		Element container = getElement();
			
		loadingAnimation = DOM.createDiv();
		loadingAnimation.setAttribute("style", "position:absolute; top:10px; left:"+ (container.getClientWidth() / 2 - 33) +"px; width:32px; height:32px; padding-left:16px; padding-top:16px;");
		loadingAnimation.setInnerHTML("<img src=\"images/loader.gif\">");
		container.appendChild(loadingAnimation);
	}
	
	public void removeLoadingAnimation(){
		Log.debug("removing loading animation");
		loadingAnimation.removeFromParent();
	}
	
	public void resetWorkspace(){
		resetCanvas();
		if(activeWorkflow != null)
			activeWorkflow.setVisible(false);		
	}
	
	/** WORKFLOW MANAGEMENT **/
	/**
	 * 
	 */
	public void setActiveWorkflow(RGWorkflow wf){
//		activeWorkflow = wf;
		if(!wf.isVisible()){
			tabBar.setActiveWorkflowTab(wf.getId());
			if(openedWorkflowIds.contains(activeWorkflow.getId())){
				//handler if the active workflow already deleted
				activeWorkflow.setVisible(false);
			}
			reconfCanvasPositionFor(wf);
			wf.setVisible(true);
			activeWorkflow = wf;
			propertyPanel.showWorkflowProperty(activeWorkflow);
		}
	}

	public void updateNodeDrawingState(String nodeId, NodeDrawingState state){
		activeWorkflow.updateNodeDrawingState(nodeId, state);
	}
	public boolean isFinishDrawingNodes(){
		return activeWorkflow.isFinishDrawingNodes();
	}
	public String createNewNodeId(){
		latestNId = "node_" + nodeIndex;
		return latestNId;
	}
	
	public void refreshWorkflowNode(final String wfId, final String nodeId){
		if(errorPanelDisplayed || messagePanelDisplayed || popupPanelDisplayed)
			return; //cant do operation when a global overlay panel displayed
		
		String closeButtonId = createUniqueId();
		globalOverlay  = getGlobalOverlay();
		final Element refreshFormPanel = DOM.createDiv();
		Element closeButton = DOM.createDiv();
		int x = (getElement().getClientWidth() / 2) - 175;
		closeButton.setAttribute("id", closeButtonId);
		closeButton.setAttribute("class", "closeButton");
		closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
										  "height: 13px; width:13px;display: block;");
		refreshFormPanel.setAttribute("id", "popupMsgPanel");
		refreshFormPanel.setClassName("popupPanel");
		refreshFormPanel.setAttribute("style", "position:absolute; " +
									"padding:5px; " +
									"width:300px;" +
									"height:135px;" +
									"background:white; " +
									"left: "+x+"px; " +
									"top:55px;" +
									"border: 1px solid #5F5C5C;" +
									"border-radius:3px;" +
									"moz-border-radius:3px;");
		Element cap1 = DOM.createDiv();
		cap1.setInnerText("This workflow has been edited.");
		cap1.setAttribute("style", "padding:5px;text-align:center;");
		Element cap2 = DOM.createDiv();
		cap2.setInnerText("Do you want to reload the node to apply the changes?");
		cap2.setAttribute("style", "padding:5px;text-align:center;");
		Element cap3 = DOM.createDiv();
		cap3.setInnerText("Warning: All node's links and properties will be removed");
		cap3.setAttribute("style", "padding:5px;text-align:center;font-size:85%; color:red;");
		refreshFormPanel.appendChild(cap1);
		refreshFormPanel.appendChild(cap2);
		refreshFormPanel.appendChild(cap3);
		
		Element buttonFCont;
		//name
		
		
		String cancelButtonId = createUniqueId();
		String yesButtonId = createUniqueId();
		buttonFCont = DOM.createDiv();
		buttonFCont.setAttribute("style", "padding-left: 95px;");
		Element cancelButton = DOM.createDiv();
		cancelButton.setInnerText("Cancel");
		cancelButton.setId(cancelButtonId);
		cancelButton.setClassName("popupPanelFormButton");
		Element yesButton = DOM.createDiv();
		yesButton.setInnerText("Yes");
		yesButton.setId(yesButtonId);
		yesButton.setClassName("popupPanelFormButton");
		
		buttonFCont.appendChild(cancelButton);
		buttonFCont.appendChild(yesButton);
		
		refreshFormPanel.appendChild(buttonFCont);
		
		refreshFormPanel.appendChild(closeButton);
		
		globalOverlay.appendChild(refreshFormPanel);
		DOM.getElementById("app-container").appendChild(globalOverlay);

		popupPanelDisplayed = true;
		
		$("#"+closeButtonId).click(new Function(){
			public void f(){
				refreshFormPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
			}
		});
		$("#"+cancelButtonId).click(new Function(){
			public void f(){
				refreshFormPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
			}
		});
		$("#"+yesButtonId).click(new Function(){
			public void f(){
				refreshFormPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
				//get the pos
				Node n = activeWorkflow.getNode(nodeId);
				int x = 0, y = 0;
				if(n != null){
					x = n.getX();
					y = n.getY();
					activeWorkflow.cascadeRemoveNodeById(nodeId);
					createNodeFromWorkflow(wfId, x, y);
				}
				
			}
		});
	}
	
	public void showHideNodeId(){
		activeWorkflow.showHideNodeId();
	}
	
	public void devTest(){
//		doTypeCheck();
		Log.debug("dp left:" + navigationPanel.getRightEdgePos());
	}
	
	public void createNodeFromWorkflow(String wfId, int x, int y){
		//displayErrorMessage("creating node from wfid:" + wfId);
		if(wfId.equals(activeWorkflow.getId())){
			displayErrorMessage("The workflow is being edited, Cannot add a workflow to itself");
			return;
		}
		createNodeByType("workflow:" + wfId, x, y);
	}
	
	public void createNodeByType(String nType, int x, int y){
		if(!activeWorkflow.hasNode(WORKFLOW_INPUT_NODE_ID) || !activeWorkflow.hasNode(WORKFLOW_OUTPUT_NODE_ID)){
			createNewWorkflow("",""); //i guess this is a dead code
		}
		
		activeWorkflow.updateNodeDrawingState(WORKFLOW_INPUT_NODE_ID, NodeDrawingState.DRAWING);
		String nId = createNewNodeId();
		Node n = new RGNode(nType, nId, activeWorkflow, draggerWithHelper);

		activeWorkflow.addNode(nId, n);
		
		nodeIndex += 1;
		
		n.setPosition(x, y);
		n.draw(this);
	}
	
	
	public void createNodeByXml(com.google.gwt.xml.client.Element proc){
		com.google.gwt.xml.client.Element function;
		function = (com.google.gwt.xml.client.Element) proc.getElementsByTagName("function").item(0);
		activeWorkflow.updateNodeDrawingState(proc.getAttribute("id"), NodeDrawingState.DRAWING);
		Node n = new RGNode(function.getAttribute("type"), proc.getAttribute("id"), proc, activeWorkflow, draggerWithHelper);
		activeWorkflow.addNode(proc.getAttribute("id"), n);
		nodeIndex += 1;
		
		int x = Integer.parseInt(proc.getAttribute("x"));
		int y = Integer.parseInt(proc.getAttribute("y"));
		
		n.setPosition(x, y);
		n.draw(this);
	}
	
	public String addWorkflowInputNode(RGWorkflow ownerWf, int x, int y){
		activeWorkflow.updateNodeDrawingState(WORKFLOW_INPUT_NODE_ID, NodeDrawingState.DRAWING);
		Node n = new RGWorkflowInputNode(WORKFLOW_INPUT_NODE_ID, activeWorkflow, draggerWithHelper);
		ownerWf.addNode(WORKFLOW_INPUT_NODE_ID, n);
		nodeIndex += 1;
		
		n.setPosition(x, y);
		n.draw(this);
		
		return "node" + (nodeIndex - 1);
	}
	
	public void addWorkflowInputNodeByXml(com.google.gwt.xml.client.Element node){
		
		int x = Integer.parseInt(node.getAttribute("x"));
		int y = Integer.parseInt(node.getAttribute("y"));
		try{
			activeWorkflow.updateNodeDrawingState(WORKFLOW_INPUT_NODE_ID, NodeDrawingState.DRAWING);
			
			RGWorkflowInputNode n = new RGWorkflowInputNode(WORKFLOW_INPUT_NODE_ID, node, activeWorkflow, draggerWithHelper);

			if(!activeWorkflow.hasNode(WORKFLOW_INPUT_NODE_ID))
				activeWorkflow.addNode(WORKFLOW_INPUT_NODE_ID, n);
			else
				return;
			
			nodeIndex += 1;
			n.setPosition(x,  y);
			n.draw(this);
		}catch (Exception e){};
	}
	
	public String addWorkflowOutputNode(RGWorkflow ownerWf, int x, int y){
		activeWorkflow.updateNodeDrawingState(WORKFLOW_OUTPUT_NODE_ID, NodeDrawingState.DRAWING);
		Node n = new RGWorkflowOutputNode(WORKFLOW_OUTPUT_NODE_ID, activeWorkflow, draggerWithHelper);
		ownerWf.addNode(WORKFLOW_OUTPUT_NODE_ID, n);
		nodeIndex += 1;
		
		n.setPosition(x, y);
		n.draw(this);
		return "node" + (nodeIndex - 1);
	}
	
	public void removeFromActiveNode(String nodeId){
		if(activeNode != null){
			if(activeNode.getId() == nodeId){
				activeNode = null;
				propertyPanel.setActiveNode(null);
			}
		}
	}
	
	public boolean createNewWorkflow(String id, String name){
		if(!openedWorkflowIds.contains(id)){
			if(activeWorkflow != null){
				activeWorkflow.setVisible(false);
			}
			activeWorkflow = new RGWorkflow(id, name, "", "",this);
			addWorkflowInputNode(activeWorkflow, 300, 100);
			addWorkflowOutputNode(activeWorkflow, 800, 500);
			if(tabBar != null){
				tabBar.addTab(activeWorkflow);
			}
			activeWorkflow.setVisible(true);
			openedWorkflowIds.add(activeWorkflow.getId());
			openedWorkflows.put(activeWorkflow.getId(), activeWorkflow);
			
			propertyPanel.showWorkflowProperty(activeWorkflow);
			activeWorkflow.setIdReadOnly(false);
			activeWorkflow.setAsNewWorkflow();
			
			return true;
		}
		
		return false;
	}
	
	public void createNewWorkflowFromSource(){
		displayPopupXmlEditor(null, false);
	}
	
	public void openWorkflow(String wfId){
		if(openedWorkflows.containsKey(wfId)){
			setActiveWorkflow(openedWorkflows.get(wfId));
		}else{
			showAppLoader("Opening workflow...");
			//retrieve workflow
			RService.getWorkflowById(wfId, new AsyncCallback <String>(){
	
				public void onFailure(Throwable arg0) {
					displayErrorMessage("Cannot contact server");
					
				}
	
				public void onSuccess(String arg0) {
					if(!arg0.startsWith("<error>")){
						parseAndDrawWorkflowGraph(arg0);
					}else{
						displayErrorMessage(arg0);
					}
				}
			});
		}
	}
	//Save and run active workflow
	public void saveAndRun(){
		if(activeWorkflow.isNewWorkflow()){
			displayErrorMessage("Workflow file do not exist, please save the workflow");
		}else{
			runWorkflow(activeWorkflow.getId());
		}
	}
	public void runWorkflow(String wfId){
		/*Run workflow with RDFGearsRest*/
		Window.open( RDFGearsRestBaseUrl + "/user/input/" + wfId, "_blank", "");
	}
	public void deleteWorkflow(final String wfId){
		if(errorPanelDisplayed || messagePanelDisplayed || popupPanelDisplayed)
			return; //cant do operation when a global overlay panel displayed
		
		String closeButtonId = createUniqueId();
		globalOverlay  = getGlobalOverlay();
		final Element delFormPanel = DOM.createDiv();
		Element closeButton = DOM.createDiv();
		int x = (getElement().getClientWidth() / 2) - 150;
		closeButton.setAttribute("id", closeButtonId);
		closeButton.setAttribute("class", "closeButton");
		closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
										  "height: 13px; width:13px;display: block;");
		delFormPanel.setAttribute("id", "popupMsgPanel");
		delFormPanel.setClassName("popupPanel");
		delFormPanel.setAttribute("style", "position:absolute; " +
									"padding:5px; " +
									"width:250px;" +
									"height:70px;" +
									"background:white; " +
									"left: "+x+"px; " +
									"top:55px;" +
									"border: 1px solid #5F5C5C;" +
									"border-radius:3px;" +
									"moz-border-radius:3px;");
		Element cap = DOM.createDiv();
		cap.setInnerText("Are you sure to delete this workflow?");
		cap.setAttribute("style", "padding:5px;");
		delFormPanel.appendChild(cap);
		
		Element buttonFCont;
		//name
		
		
		String cancelButtonId = createUniqueId();
		String yesButtonId = createUniqueId();
		buttonFCont = DOM.createDiv();
		buttonFCont.setAttribute("style", "padding-left: 70px;");
		Element cancelButton = DOM.createDiv();
		cancelButton.setInnerText("Cancel");
		cancelButton.setId(cancelButtonId);
		cancelButton.setClassName("popupPanelFormButton");
		Element yesButton = DOM.createDiv();
		yesButton.setInnerText("Yes");
		yesButton.setId(yesButtonId);
		yesButton.setClassName("popupPanelFormButton");
		
		buttonFCont.appendChild(cancelButton);
		buttonFCont.appendChild(yesButton);
		
		delFormPanel.appendChild(buttonFCont);
		
		delFormPanel.appendChild(closeButton);
		
		globalOverlay.appendChild(delFormPanel);
		DOM.getElementById("app-container").appendChild(globalOverlay);

		popupPanelDisplayed = true;
		
		$("#"+closeButtonId).click(new Function(){
			public void f(){
				delFormPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
			}
		});
		$("#"+cancelButtonId).click(new Function(){
			public void f(){
				delFormPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
			}
		});
		$("#"+yesButtonId).click(new Function(){
			public void f(){
				delFormPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
				doDeleteWorkflow(wfId);
			}
		});
	}
	
	public void doDeleteWorkflow(final String wfId){
//		Log.debug("deleting workflow file:" + wfId);
		if(openedWorkflows.containsKey(wfId)){
			closeWorkflow(openedWorkflows.get(wfId));
		}
		showAppLoader("Deleting Workflow...");
		RService.deleteWorkflow(wfId, new AsyncCallback <String>(){

			public void onFailure(Throwable caught) {
				removeAppLoader();
				displayErrorMessage("Cannot connect to server");
				
			}

			public void onSuccess(String result) {
				if(result.startsWith("<error>")){
					removeAppLoader();
					displayErrorMessage(result);
				}else{
					navigationPanel.refreshWorkflowList();
					syncDeleteOperation(wfId);
					removeAppLoader();
					
				}
				
			}
			
		});
	}
	
	/**
	 * create a shallow copy of the workflow
	 * @param wfId: workflow id
	 */
	public void copyWorkflow(String wfId, final boolean replaceOption){
		if(openedWorkflows.containsKey(wfId)){
			displayCopyWorkflowForm(openedWorkflows.get(wfId), replaceOption);
		}else{
			//display loading form
			showAppLoader("Loading...");
			//load workflow
			RService.getWorkflowById(wfId, new AsyncCallback <String>(){

				public void onFailure(Throwable caught) {
					removeAppLoader();
					displayErrorMessage("Cannot connect to server");
					
				}

				public void onSuccess(String result) {
					if(result.startsWith("<error>")){
						removeAppLoader();
						displayErrorMessage(result);
					}else{
						removeAppLoader();
						RGWorkflow wf = parseAndGetWorkflowDetails(result);
						if(wf != null)
							displayCopyWorkflowForm(wf, replaceOption);
					}
					
				}
				
			});
			
			//if success parse and load workflow
			//if not display error message
		}
	}
	
	private RGWorkflow parseAndGetWorkflowDetails(String wfInXml){
		String id, name, desc, cat;
		try{
			Document workflow = XMLParser.parse(wfInXml);
			com.google.gwt.xml.client.Element meta = (com.google.gwt.xml.client.Element) workflow.getElementsByTagName("metadata").item(0);
			id = getWorkflowPropertyFromMeta("id", meta);
			name = getWorkflowPropertyFromMeta("name", meta);
			desc = getWorkflowPropertyFromMeta("description", meta);
			cat = getWorkflowPropertyFromMeta("category", meta);
		
		}catch(Exception e){
			displayErrorMessage("DOM Exception: workflow's xml file is not well-formed");
			return null;
		}
		return new RGWorkflow(id, name, desc, cat , this);
	}
	
	private void displayCopyWorkflowForm(final RGWorkflow wf, boolean replaceOption){
		if(errorPanelDisplayed || messagePanelDisplayed || popupPanelDisplayed)
			return; //cant do operation when a global overlay panel displayed
		
		copyReplace = false;
		String closeButtonId = createUniqueId();
		globalOverlay  = getGlobalOverlay();
		final Element copyFormPanel = DOM.createDiv();
		Element closeButton = DOM.createDiv();
		int x = (getElement().getClientWidth() / 2) - 325;
		closeButton.setAttribute("id", closeButtonId);
		closeButton.setAttribute("class", "closeButton");
		closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
										  "height: 13px; width:13px;display: block;");
		copyFormPanel.setAttribute("id", "popupMsgPanel");
		copyFormPanel.setClassName("popupPanel");
		
		copyFormPanel.setAttribute("style", "position:absolute; " +
									"padding:5px; " +
									"width:600px;" +
									"height:410px;" +
									"background:white; " +
									"left: "+x+"px; " +
									"top:55px;" +
									"border: 1px solid #5F5C5C;" +
									"border-radius:3px;" +
									"moz-border-radius:3px;");
		Element cap = DOM.createDiv();
		cap.setInnerText("Workflow Properties");
		cap.setAttribute("style", "font-size:14px;font-weight:bold;padding:5px;border-bottom: 1px solid black;");
		copyFormPanel.appendChild(cap);
		Element nameLabel, idLabel, descLabel, catLabel;
		nameLabel = DOM.createDiv();
		nameLabel.setInnerText("Workflow Name");
		nameLabel.setClassName("rowHead");
		idLabel = DOM.createDiv();
		idLabel.setInnerHTML("Workflow Unique Id");
		idLabel.setClassName("rowHead");
		
		descLabel = DOM.createDiv();
		descLabel.setInnerText("Description");
		descLabel.setClassName("rowHead");
		catLabel = DOM.createDiv();
		catLabel.setInnerText("Category");
		catLabel.setClassName("rowHead");
		
		Element nameFCont, idFCont, descFCont, catFCont, buttonFCont;
		//name
		nameFCont = DOM.createDiv();
		nameFCont.setClassName("propertyFormContainer");
		Element nameForm = DOM.createInputText();
		nameForm.setAttribute("value", wf.getName());
		nameForm.setClassName("inputString");
		final String nameFormId = createUniqueId();
		nameForm.setId(nameFormId);
		nameFCont.appendChild(nameForm);
		
		//id
		idFCont = DOM.createDiv();
		idFCont.setClassName("propertyFormContainer");
		Element idForm = DOM.createInputText();
		idForm.setAttribute("value", wf.getId() + "-copy");
		idForm.setClassName("inputString");
		final String idFormId = createUniqueId();
		idForm.setId(idFormId);
		idFCont.appendChild(idForm);
		
		Element idDescContainer = DOM.createDiv();
		idDescContainer.setClassName("paramFormHelpText");
		idDescContainer.setInnerText("*Valid Characters: a-z A-Z 0-9 -");
		//desc
		descFCont = DOM.createDiv();
		descFCont.setClassName("propertyFormContainer");
		Element descForm = DOM.createTextArea();
		descForm.setInnerText(wf.getDescription());
		descForm.setClassName("queryText");
		final String descFormId = createUniqueId();
		descForm.setId(descFormId);
		descFCont.appendChild(descForm);
		
		
		catFCont = DOM.createDiv();
		catFCont.setClassName("propertyFormContainer");
		//category dropdown box
		final String catLbId = createUniqueId();
		ListBox catlb = buildCategoryListForm(catLbId, wf.getCategory());
		catFCont.appendChild(catlb.getElement());
		
		Element newCatDesc = DOM.createDiv();
		newCatDesc.setClassName("paramFormHelpText");
		newCatDesc.setInnerText("Fill the category name below to create new category and it will overwrite the category name above");
		catFCont.appendChild(newCatDesc);
		final String catFormId = createUniqueId();
		Element catForm = DOM.createInputText();
		catForm.setAttribute("value", "");
		catForm.setClassName("inputString");
		catForm.setId(catFormId);
		catFCont.appendChild(catForm);
		
		Element replaceOptCont = DOM.createDiv();
		Element replaceCb = DOM.createInputCheck();//new CheckBox("Replace node with the copy");
		replaceCb.setId("copyReplaceOpt");
		Element cbLabel = DOM.createLabel();
		cbLabel.setAttribute("for", "copyReplaceOpt");
		cbLabel.setInnerText("Replace node with the copy");
		replaceOptCont.appendChild(replaceCb);
		replaceOptCont.appendChild(cbLabel);		
		
		String cancelButtonId = createUniqueId();
		String saveButtonId = createUniqueId();
		buttonFCont = DOM.createDiv();
		Element cancelButton = DOM.createDiv();
		cancelButton.setInnerText("Cancel");
		cancelButton.setId(cancelButtonId);
		cancelButton.setClassName("popupPanelFormButton");
		Element saveButton = DOM.createDiv();
		saveButton.setInnerText("Copy");
		saveButton.setId(saveButtonId);
		saveButton.setClassName("popupPanelFormButton");
		
		buttonFCont.appendChild(cancelButton);
		buttonFCont.appendChild(saveButton);
		
		copyFormPanel.appendChild(nameLabel);
		copyFormPanel.appendChild(nameFCont);
		
		copyFormPanel.appendChild(idLabel);
		copyFormPanel.appendChild(idFCont);
		copyFormPanel.appendChild(idDescContainer);
		
		copyFormPanel.appendChild(descLabel);
		copyFormPanel.appendChild(descFCont);
		
		copyFormPanel.appendChild(catLabel);
		copyFormPanel.appendChild(catFCont);
		
		if(replaceOption){
			copyFormPanel.appendChild(replaceOptCont);
		}
		
		copyFormPanel.appendChild(buttonFCont);
		
		copyFormPanel.appendChild(closeButton);
		
		globalOverlay.appendChild(copyFormPanel);
		DOM.getElementById("app-container").appendChild(globalOverlay);

		popupPanelDisplayed = true;
		
		$("#"+closeButtonId).click(new Function(){
			public void f(){
				copyFormPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
			}
		});
		$("#"+cancelButtonId).click(new Function(){
			public void f(){
				copyFormPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
			}
		});
		
		if(replaceOption){
			$("#copyReplaceOpt").change(new Function(){
				public void f(){
					copyReplace = !copyReplace;
				}
			});
		}
		
		$("#"+saveButtonId).click(new Function(){
			public void f(){
				//validate workflow
				if($("#" + idFormId).val().isEmpty()){
					Log.debug("show error message on empty id");
				}else if($("#" + idFormId).val().equalsIgnoreCase(wf.getId())){
					Log.debug("show error message on same id");
				}else{
					String cat = "";
					if(!$("#" + catFormId).val().trim().isEmpty()){
						cat = $("#" + catFormId).val().trim();
					}else{
						cat = $("#"+catLbId).val();
						if(cat.equals("noval"))
							cat = "";
					}
					RGWorkflow copy = new RGWorkflow($("#" + idFormId).val(),
												   $("#" + nameFormId).val(),
												   $("#" + descFormId).val(),
												   cat, getInstance());
					copyFormPanel.removeFromParent();
					globalOverlay.removeFromParent();
					popupPanelDisplayed = false;
					doCopyWorkflowAs(wf, copy, copyReplace);
					
				}
			}
		});
		$("#"+catLbId).change(new Function(){
			public void f(){
				$("#" + catFormId).val("");
			}
		});
	}
	
	ListBox buildCategoryListForm(String id, String selectedCat){
		ListBox lb = new ListBox();
		lb.getElement().setId(id);
		ArrayList <String> cats = getNavigationPanel().getWorkflowCategories();
		int i = 1;
		lb.addItem("Choose Category","novalue");
		for(String cat: cats){
			lb.addItem(cat, cat);
			if(!selectedCat.isEmpty()){
				if(cat.equalsIgnoreCase(selectedCat)){
					lb.setSelectedIndex(i);
				}
			}
			i++;
		}
		if(selectedCat.isEmpty()){
			lb.setSelectedIndex(0);
		}
		
		return lb;
	}
	
	public RGCanvas getInstance(){
		return this;
	}
	/**
	 * do shallow copy of workflow file by providing new workflow properties (name, id, description, category)
	 * @param wf
	 * @param newWf
	 */
	private void doCopyWorkflowAs(final RGWorkflow wf, final RGWorkflow newWf, final boolean replace){
		if(wf.getId().equalsIgnoreCase(newWf.getId())){
			displayErrorMessage("Workflows cannot have a same Id");
		}else{
			//display loading form
			showAppLoader("Copying workflow...");
			//load workflow
			RService.doCopyWorkflowFile(wf.getId(), newWf.getId(), 
													newWf.getName(), 
													newWf.getDescription(), 
													newWf.getCategory(), 
													new AsyncCallback <String>(){

				public void onFailure(Throwable caught) {
					removeAppLoader();
					displayErrorMessage("Cannot connect to server");
				}

				public void onSuccess(String result) {
					if(result.startsWith("<error>")){
						removeAppLoader();
						displayErrorMessage(result);
					}else{
						if(replace){
							Node nestedNode = activeWorkflow.getNestedWorkflowNode(wf.getId());
							if(nestedNode != null){
								nestedNode.setWorkflowId(newWf.getId());
								nestedNode.setNodeDescription(newWf.getDescription());
								nestedNode.setHeaderText(newWf.getName());
							}
						}
						navigationPanel.refreshWorkflowList();
						removeAppLoader();
						displayMessage("Workflow file successfully copied");
					}
					
				}
			});
		}
	}
	
	public void viewOriginalWorkflowSource(String wfId){
		if(openedWorkflows.containsKey(wfId)){
			com.google.gwt.xml.client.Document s = openedWorkflows.get(wfId).getLastSavedSource();
			if(s == null)
				s =  openedWorkflows.get(wfId).exportToXml();
			
			displayFormattedXml(s);
		}
	}
	
//	private void doSaveActiveWorkflowAs(RGWorkflow saveAs){
//		
//	}
	
	public void closeWorkflow(RGWorkflow wf){
		if(wf != null){
			if(openedWorkflows.containsKey(wf.getId())){
				//TODO: check status, if need to be saved,, return false
				wf.clear();
				openedWorkflows.remove(wf.getId());
				openedWorkflowIds.remove(wf.getId());
				tabBar.removeWorkflowTab(wf.getId());
			}
			
			if(openedWorkflowIds.size() < 1){
				createNewWorkflow(createUniqueWorkflowId(), "New Workflow");
			}else if(wf == activeWorkflow){
				RGWorkflow newActive = openedWorkflows.get(openedWorkflowIds.get(0));
				setActiveWorkflow(newActive);
				tabBar.setActiveWorkflowTab(newActive.getId());
			}
		}
		
		return;
	}
	public String createUniqueWorkflowId(){
		String newId = HTMLPanel.createUniqueId();
		while(openedWorkflowIds.contains(newId)){
			newId = HTMLPanel.createUniqueId();
		}
		return newId;
	}
	private String getWorkflowPropertyFromMeta(String tagName, com.google.gwt.xml.client.Element meta){
		com.google.gwt.xml.client.Element p;
		if(meta.getElementsByTagName(tagName).getLength() > 0){
			p = (com.google.gwt.xml.client.Element) meta.getElementsByTagName(tagName).item(0);
			XMLParser.removeWhitespace(p);
			if(p.hasChildNodes())
				return p.getChildNodes().item(0).toString();
		}
		
		return "";
	}
	
	private void parseAndDrawWorkflowGraph(String wfInXml){
		//removeLoadingAnimation();
		removeAppLoader();
		com.google.gwt.xml.client.Element meta, node;
		String id, name, desc, cat;
		final com.google.gwt.xml.client.Element network;
		try{
			Document workflow = XMLParser.parse(wfInXml);
			//test for error
			if(workflow.getFirstChild().getNodeName().equalsIgnoreCase("error")){
				displayErrorMessage("The rdfgears file cannot be found");
				//isEditingWorkflow = false;
				Log.error(wfInXml);
				return;
			}
//			isEditingWorkflow = true;
			meta = (com.google.gwt.xml.client.Element) workflow.getElementsByTagName("metadata").item(0);
			id = getWorkflowPropertyFromMeta("id", meta);
			name = getWorkflowPropertyFromMeta("name", meta);
			desc = getWorkflowPropertyFromMeta("description", meta);
			desc = desc.replace("&lt;", "<");
			desc = desc.replace("&gt;", ">");
			desc = desc.replace("&amp;", "&");
			cat = getWorkflowPropertyFromMeta("category", meta);
			
			if(openedWorkflows.containsKey(id)){
				displayErrorMessage("A workflow with the same id, \""+id+"\", is being opened");
				return;
			}
			
			RGWorkflow openedWf = new RGWorkflow(id, name, desc, cat, this);
			openedWf.setLastSavedSource(workflow);
			openedWorkflowIds.add(openedWf.getId());
			openedWorkflows.put(openedWf.getId(), openedWf);
			setActiveWorkflow(openedWf);
			tabBar.addTab(openedWf);
			openedWf.setIsEditing(true);
			openedWf.setIsModified(false); //bug on creating workflow from xml editor
			
			//parse workflow input node
			node = (com.google.gwt.xml.client.Element) workflow.getElementsByTagName("workflowInputList").item(0);
			addWorkflowInputNodeByXml(node);
			network = (com.google.gwt.xml.client.Element) workflow.getElementsByTagName("network").item(0);
			addWorkflowOutputNode(activeWorkflow, Integer.parseInt(network.getAttribute("x")),
								  Integer.parseInt(network.getAttribute("y")));
			
			//parse all the processors
			final NodeList processors = network.getElementsByTagName("processor");
			for(int i = 0; i < processors.getLength(); i++){
				com.google.gwt.xml.client.Element proc = (com.google.gwt.xml.client.Element) processors.item(i);
				createNodeByXml(proc);
			}
			
			//connect the workflow output port
			timer = new Timer() {
			      public void run() {
			    	if(isFinishDrawingNodes()){
			    		timer.cancel();
			    		timer = null;
			    		if(network.hasAttribute("output")){
							String outputNodeId = network.getAttribute("output");
							if(outputNodeId.startsWith("workflowInputPort:")){
								Node outputNode = activeWorkflow.getNode(WORKFLOW_OUTPUT_NODE_ID);
								Node n = activeWorkflow.getNode(WORKFLOW_INPUT_NODE_ID);
								
								String portName = outputNodeId.substring(18);
								drawConnection(n, n.getPortByPortName(portName), outputNode, outputNode.getPortByPortName(outputNode.getId()));
							}else{
								if(activeWorkflow.hasNode(outputNodeId) && !outputNodeId.equals(WORKFLOW_INPUT_NODE_ID)){
									Node outputNode = activeWorkflow.getNode(WORKFLOW_OUTPUT_NODE_ID);
									Node n = activeWorkflow.getNode(outputNodeId);
									NodePort sourcePort = outputNode.getPortByPortName(outputNode.getId());
									if(sourcePort != null){
										drawConnection(n, n.getPortByPortName(n.getId()), outputNode, outputNode.getPortByPortName(outputNode.getId()));
									}else{
										displayErrorMessage("Port \"" +outputNode.getId()+ "\" is missing on node \"" + outputNode.getName() + "(" + outputNode.getId() + ")\"");
									}
								}else{
									//node missing.. report
								}
							}
						}
			    		for(int i = 0; i < processors.getLength(); i++){
							com.google.gwt.xml.client.Element proc = (com.google.gwt.xml.client.Element) processors.item(i);
							Node to = activeWorkflow.getNode(proc.getAttribute("id"));
							//Log.debug("n:" + proc.toString());
							if(to != null){
								NodeList inPorts = proc.getElementsByTagName("inputPort");
								for(int j = 0; j < inPorts.getLength(); j++){
									com.google.gwt.xml.client.Element port = (com.google.gwt.xml.client.Element) inPorts.item(j);
									if(port.hasChildNodes()){
										NodeList source = port.getElementsByTagName("source");
										if(source.getLength() > 0){
											com.google.gwt.xml.client.Element s = (com.google.gwt.xml.client.Element) source.item(0);
											if(s.hasAttribute("processor")){
												if(!activeWorkflow.hasNode(s.getAttribute("processor"))){
													displayErrorMessage("Workflow file contain outdated node definition: Node \""+s.getAttribute("processor")+"\" is missing");
													break;
												}
								
												Node from = activeWorkflow.getNode(s.getAttribute("processor"));
												NodePort toPort = to.getPortByPortName(port.getAttribute("name"));
												if(toPort == null){
													displayErrorMessage("Workflow file contain outdated node definition: Input port \""+port.getAttribute("name")+"\" is missing on node \""+to.getName()+ "(" + to.getId() + ")\"");
													break;
												}
												
												if(from != null)
													drawConnection(from, from.getPortByPortName(from.getId()), to, toPort);
												
											}else if(s.hasAttribute("workflowInputPort")){
												boolean portsExist = true;
												Node from = activeWorkflow.getNode(WORKFLOW_INPUT_NODE_ID);
												NodePort fromPort = from.getPortByPortName(s.getAttribute("workflowInputPort"));
												if(fromPort == null){
													displayErrorMessage("Port \"" +s.getAttribute("workflowInputPort")+ "\" is missing on node \"" + from.getName() + "(" + from.getId() + ")\"");
													portsExist = false;
												}
												
												NodePort toPort = to.getPortByPortName(port.getAttribute("name"));
												if(toPort == null){
													displayErrorMessage("Port \"" +port.getAttribute("name")+ "\" is missing on node \"" + to.getName() + "(" + to.getId() + ")\"");
													portsExist = false;
												}
												
												if(portsExist)
													drawConnection(from, fromPort, to, toPort);
											}
										}
									}
								}
							}
			    		}
			    		doTypeCheck();
			    	}else{
			    		timer.schedule(100);
			    	}
			      }
			    };

			    // Execute the timer to expire 2 seconds in the future
			timer.schedule(100);
			//timer.scheduleRepeating(100);
			//connect all the processor's ports
			
		}catch (Exception e){
			displayErrorMessage("The rdfgears workflow source contain error or has an invalid format");
			e.printStackTrace();
		}
	}
	
	
	
	public String displayFormattedXml(com.google.gwt.xml.client.Document d){
		RService.formatXml(d.toString(), new AsyncCallback <String>(){
			public void onFailure(Throwable arg0) {
				displayErrorMessage("Cannot connect to server");
			}

			public void onSuccess(String arg0) {
				displayPopupXmlEditor(arg0, true);
			}
			  
		  });
		
		return "";
	}
	
	public void viewActiveWorkflowPortType(){
		if(messagePanelDisplayed || errorPanelDisplayed || popupPanelDisplayed)
			return;
		
		
		globalOverlay  = getGlobalOverlay();
		final Element viewerPanel = DOM.createDiv();
		Element closeButton = DOM.createDiv();
		int x = (getElement().getClientWidth() / 2) - 325;
		closeButton.setAttribute("id", "popupPanelCloseButton");
		closeButton.setAttribute("class", "closeButton");
		closeButton.setAttribute("style", "position:absolute; right:-6px; top:-6px; " +
										  "height: 13px; width:13px;display: block;");
		viewerPanel.setAttribute("id", "popupMsgPanel");
		viewerPanel.setClassName("popupPanel");
		viewerPanel.setAttribute("style", "position:absolute; " +
									"padding:5px; " +
									"width:600px;" +
									"height:600px;" +
									"background:white; " +
									"left: "+x+"px; " +
									"top:55px;" +
									"border: 1px solid #5F5C5C" +
									"border-radius:3px;" +
									"moz-border-radius:3px;");
		Element cap = DOM.createDiv();
		cap.setInnerText("Port's Type");
		cap.setAttribute("style", "font-size:14px;font-weight:bold;padding:5px;height:20px;border-bottom:1px solid black;");
		viewerPanel.appendChild(cap);
		Element container = DOM.createDiv();
		container.setAttribute("style", "font-size:90%; height: 560px; padding:5px;overflow:auto;");
		viewerPanel.appendChild(container);
		
		if(!activeWorkflow.isWellTyped()){
			Element warning = DOM.createDiv();
			warning.setInnerText("Current workflow is not well typed, thus the correct ports type cannot be resolved");
			warning.setAttribute("style", "color:red;");
			container.appendChild(warning);
		}
		//add all active workflow port here.. 
		for(String nId: activeWorkflow.getNodeIds()){
			Node n = activeWorkflow.getNode(nId);
			if(n != null){
				Element t = DOM.createElement("table");
				t.addClassName("portTable");
				Element trHead = DOM.createElement("tr");
				Element tdHead = DOM.createElement("td");
				tdHead.setInnerText(n.getName() + "(" + n.getId() + ")");
				tdHead.setAttribute("colspan", "2");
				tdHead.addClassName("tCaption");
				trHead.appendChild(tdHead);
				
				Element trTitle = DOM.createElement("tr");
				trTitle.addClassName("rowHead");
				Element tdP = DOM.createElement("td");
				tdP.setInnerText("Port Name");
				tdP.addClassName("tableFont");
				tdP.addClassName("tHead");
				
				Element tdT = DOM.createElement("td");
				tdT.setInnerText("Type");
				tdT.addClassName("tableFont");
				tdT.addClassName("tHead");
				trTitle.appendChild(tdP);
				trTitle.appendChild(tdT);
				
				t.appendChild(trHead);
				t.appendChild(trTitle);
				
				//loop the ports
				for(String pN: n.getPortNames()){
					try{
						Element trPort = DOM.createElement("tr");
						Element tdPN = DOM.createElement("td");
						
						if(pN.equals(n.getId()) && !n.getId().equals(WORKFLOW_OUTPUT_NODE_ID))
							tdPN.setInnerText("Ouput");
						else
							tdPN.setInnerText(pN);
						
						tdPN.addClassName("tableFont");
						tdPN.addClassName("tdContent");
						Element tdType = DOM.createElement("td");
						tdType.setInnerText(n.getPortByPortName(pN).getType().getElement().toString());
						tdType.addClassName("tableFont");
						tdType.addClassName("tdContent");
	
						trPort.appendChild(tdPN);
						trPort.appendChild(tdType);
						t.appendChild(trPort);
						
					}catch(Exception e){}
					
				}
				container.appendChild(t);
			}
		}
		
		viewerPanel.appendChild(closeButton);
		
		globalOverlay.appendChild(viewerPanel);
		DOM.getElementById("app-container").appendChild(globalOverlay);
		popupPanelDisplayed = true;
		
		$("#popupPanelCloseButton").click(new Function(){
			public void f(){
				viewerPanel.removeFromParent();
				globalOverlay.removeFromParent();
				popupPanelDisplayed = false;
			}
		});
	}
	/**
	 * notify opened workflow to delete workflow node if a workflow being deleted
	 * @param wfId
	 */
	public void syncDeleteOperation(String wfId){
		for(String oWfId: openedWorkflowIds){
			RGWorkflow wf = openedWorkflows.get(oWfId);
			if(wf != null)
				wf.syncDeleteWorkflowNode(wfId);
		}
	}
	/**
	 * when a workflow is saved, a notification will be sent to other 
	 * opened workflows that have this workflow as node
	 */
	public void syncSaveOperation(String wfId){
		for(String oWfId: openedWorkflowIds){
			RGWorkflow wf = openedWorkflows.get(oWfId);
			if(wf != null && wf != activeWorkflow)
				wf.syncSaveWorkflowNode(wfId);
		}
	}
	
	public boolean saveWorkflow(){
		if((activeWorkflow.getName().length() < 1) || (activeWorkflow.getId().length() < 1)){
			displayErrorMessage("Workflow Name and Workflow Id cannot be empty");
			propertyPanel.showWorkflowProperty(activeWorkflow);
			return false;
		}
		
		if(!activeWorkflow.getId().matches("^[a-zA-Z0-9-_]+$")){
			displayErrorMessage("Workflow id contain illegal characters");
			propertyPanel.showWorkflowProperty(activeWorkflow);
			return false;
		}
		
		//String workflowInXml = .toString();
		final com.google.gwt.xml.client.Document source = activeWorkflow.exportToXml();
		if(activeWorkflow.isEditing()){ //workflow has been saved before
			showAppLoader("Saving Workflow...");
			RService.saveWorkflow(activeWorkflow.getId(), activeWorkflow.getId(), source.toString(),  new AsyncCallback <String>(){
	
				public void onFailure(Throwable arg0) {
					removeAppLoader();
					displayErrorMessage("Cannot connect to server");
				}
	
				public void onSuccess(String arg0) {
					removeAppLoader();
					if(!arg0.contains("<error>")){
						activeWorkflow.setIsEditing(true);
						navigationPanel.refreshWorkflowList();
						activeWorkflow.setIdReadOnly(true);
						activeWorkflow.setLastSavedSource(source);
						syncSaveOperation(activeWorkflow.getId());
					}else{
						displayErrorMessage(arg0);
					}
				}
				  
			  });
		}else{
			RService.saveAsNewWorkflow(activeWorkflow.getId(), activeWorkflow.getId(), source.toString(),  new AsyncCallback <String>(){
				
				public void onFailure(Throwable arg0) {
					removeAppLoader();
					displayErrorMessage("Cannot connect to server");
				}
	
				public void onSuccess(String arg0) {
					removeAppLoader();
					if(!arg0.contains("<error>")){
						activeWorkflow.setIsEditing(true);
						activeWorkflow.setIdReadOnly(true);
						navigationPanel.refreshWorkflowList();
						activeWorkflow.setLastSavedSource(source);
						syncSaveOperation(activeWorkflow.getId());
					}
					if(arg0.contains("error")){
						displayErrorMessage(arg0);
					}else{
						displayMessage(arg0);
					}
				}
				  
			  });
		}
		return true;
	}
}
