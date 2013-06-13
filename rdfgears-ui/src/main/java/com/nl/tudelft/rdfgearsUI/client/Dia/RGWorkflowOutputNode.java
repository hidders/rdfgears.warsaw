package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;
import com.nl.tudelft.rdfgearsUI.client.RGType;
import com.nl.tudelft.rdfgearsUI.client.RGTypeUtils;

public class RGWorkflowOutputNode extends Node{
	Element elementCache = null;
	public RGWorkflowOutputNode(String id, RGWorkflow owner, boolean withHelper) {
		super(id, owner, withHelper);
		isPermanentNode = true;
		setHeaderText("Output");
	}

	@Override
	void draw(RGCanvas canvas) {
		super.setCanvas(canvas);
		Element c = canvas.getElement(); 
		header.setClassName("outputNode");
		c.appendChild(root);/* do this before adding another element and set the handler
							   element has to be added to the canvas then can be manipulated*/
		
		addNodeInputPort(new RGType(RGTypeUtils.getSimpleVarType(canvas.getTypeChecker().createUniqueTypeName())));
		canvas.updateNodeDrawingState(getId(), NodeDrawingState.DONE);
		setupRootEventHandler();
	}

	@Override
	void displayProperties(Element container) {
		if(elementCache == null){
			elementCache = DOM.createDiv();
			elementCache.setClassName("propertyContainer");
			Element labelContainer = DOM.createDiv();
			labelContainer.setClassName("propertyLabelContainer");
			labelContainer.setInnerText("Output");
			elementCache.appendChild(labelContainer);
			
			
			Element descContainer = DOM.createDiv();
			descContainer.setClassName("paramFormHelpText");
			descContainer.setInnerText("Workflow Output Node");
			elementCache.appendChild(descContainer);
			
			container.appendChild(elementCache);
			
		}else{
			container.appendChild(elementCache);
		}
		
		
	}

	@Override
	com.google.gwt.xml.client.Element toXml(Document doc) {
		com.google.gwt.xml.client.Element network =  doc.createElement("network");
		com.google.gwt.xml.client.Element outputType =  doc.createElement("output-type");
		if(inputPaths.size() > 0){
			Path p = inputPaths.get(0);
			String sourceId = p.getSourceId();
			
			if(sourceId.equalsIgnoreCase(RGCanvas.WORKFLOW_INPUT_NODE_ID)){
				Node workflowInput = p.getStartNode();
				network.setAttribute("output", "workflowInputPort:" + workflowInput.getPortNameByPortId(p.getStartPortId()));
			}else{
				network.setAttribute("output", sourceId);
			}
			
			outputType.appendChild(p.getEndPort().getType().getElement());
			
			
//			ArrayList <String> pNames = getPortNames();
//			for(String pN: pNames){
//				NodePort port = getPortByPortName(pN);
//				outputType.appendChild(port.)
//				}
//			}
			network.appendChild(outputType);
		}
		network.setAttribute("x", "" + getX());
		network.setAttribute("y", "" + getY());
		
		return network;
	}

}
