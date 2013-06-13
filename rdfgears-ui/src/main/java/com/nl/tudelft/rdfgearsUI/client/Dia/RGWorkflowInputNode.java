package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;
import com.nl.tudelft.rdfgearsUI.client.RGType;
import com.nl.tudelft.rdfgearsUI.client.RGTypeUtils;

public class RGWorkflowInputNode extends Node{
	String entryGroupId, inputFormId, vals;
	Element elementCache = null;
	private Element formContainer;
	private com.google.gwt.xml.client.Element xmlDef = null;
	private int outputCounter;
	
	private ArrayList <String> fieldFormIds = new ArrayList <String>();
	private Map <String, String> id2EntryId = new HashMap <String, String>();
	
	public RGWorkflowInputNode(String id, RGWorkflow owner, boolean withHelper){
		super(id, owner, withHelper);
		setHeaderText("Inputs");
		isPermanentNode = true;
	}
	
	public RGWorkflowInputNode(String id, com.google.gwt.xml.client.Element _xmlDef, RGWorkflow owner, boolean withHelper){
		super(id, owner, withHelper);
		setHeaderText("Inputs");
		isPermanentNode = true;
		xmlDef = _xmlDef;
	}
	
	@Override
	public void draw(RGCanvas canvas) {
		super.setCanvas(canvas);
		Element c = canvas.getElement();
		header.setClassName("inputNode");
		c.appendChild(root);/*element has to be added to the canvas then can be manipulated*/
		
		/*Manipulate content using DOM API*/
		entryGroupId = canvas.createUniqueId();
		
		addEntryGroupHolder(entryGroupId, "Workflow Inputs");
		if(xmlDef != null){
			if(xmlDef.hasChildNodes()){
				NodeList ports = xmlDef.getElementsByTagName("workflowInputPort");
				for(int i = 0; i < ports.getLength(); i++){
					com.google.gwt.xml.client.Element port = (com.google.gwt.xml.client.Element) ports.item(i);
					addPort(port.getAttribute("name"));
				}
			}
		}
		canvas.updateNodeDrawingState(getId(), NodeDrawingState.DONE);
		setupRootEventHandler();
	}

	@Override
	public void displayProperties(Element container) {
		if(elementCache == null){
			elementCache = DOM.createDiv();
			elementCache.setClassName("propertyContainer");
			Element labelContainer = DOM.createDiv();
			labelContainer.setClassName("propertyLabelContainer");
			labelContainer.setInnerText("Workflow Inputs");
			formContainer = DOM.createDiv();
			formContainer.setClassName("propertyFormContainer");
			
			Element addFieldButton = DOM.createDiv();
			//addFieldButton.setInnerText("+ add input");
			addFieldButton.setInnerHTML("<img src=\"images/add.png\" /> add input");
			String addFieldButtonId = canvas.createUniqueId();
			addFieldButton.setId(addFieldButtonId);
			addFieldButton.setClassName("addFieldButton");
			
			elementCache.appendChild(labelContainer);
			elementCache.appendChild(formContainer);
			elementCache.appendChild(addFieldButton);
			
			Element descContainer = DOM.createDiv();
			descContainer.setClassName("paramFormHelpText");
			descContainer.setInnerText("Define parameters as workfow inputs");
			elementCache.appendChild(descContainer);
			
			container.appendChild(elementCache);
			
			assignAddButtonHandler(addFieldButtonId);
			assignHandler(inputFormId);
			
			//if some port added through API, not UI
			//in this case, the input node created with predefined ports
			if(fieldFormIds.size() > 0){
				for(String fId: fieldFormIds){
					String holderId = canvas.createUniqueId();
					String dbuttonId = canvas.createUniqueId();
					String value = getPortNameByEntryId(id2EntryId.get(fId));
					Element holder = createForm(holderId, fId, dbuttonId, value);
					formContainer.appendChild(holder);
					
					assignHandler(fId);
					assignDelButtonHandler(dbuttonId, holderId, fId);
				}
			}
			
		}else{
			container.appendChild(elementCache);
		}
		
	}
	public void addPort(String value, String type){
		String formCandidateId = canvas.createUniqueId();
		String entryId = canvas.createUniqueId();
		
		fieldFormIds.add(formCandidateId);
		addOutputEntry(entryId, value, new RGType(canvas.getTypeChecker().rename(RGTypeUtils.stringToType(type), getId())), value);
		id2EntryId.put(formCandidateId, entryId);
		outputCounter += 1;
	}
	
	public void addPort(String value){
		String formCandidateId = canvas.createUniqueId();
		String entryId = canvas.createUniqueId();
		
		fieldFormIds.add(formCandidateId);
		addOutputEntry(entryId, value, new RGType(RGTypeUtils.getSimpleVarType(canvas.getTypeChecker().createUniqueTypeName())), value);
		id2EntryId.put(formCandidateId, entryId);
		outputCounter += 1;
	}
	
	private Element createForm(String holderId, String formId, String delButtonId, String value){
		Element holder = DOM.createDiv();
		holder.setId(holderId);
		Element deleteButton = DOM.createDiv();
		//deleteButton.setInnerText("X");
		deleteButton.setInnerHTML("<img src=\"images/del-white.png\"/>");
		deleteButton.setId(delButtonId);
		deleteButton.setAttribute("style", "display:inline;margin-left:5px;");
		
		Element t = DOM.createInputText();
		t.setAttribute("value",value);
		t.setClassName("inputString");
		t.setId(formId);
		
		holder.appendChild(t);
		holder.appendChild(deleteButton);
		
		return holder;
	}
	private String addOutputField(String value){
		String id = canvas.createUniqueId();
		String holderId = canvas.createUniqueId();
		String dbuttonId = canvas.createUniqueId();

		Element holder = createForm(holderId, id, dbuttonId, value);
		
		fieldFormIds.add(id);
		
		if(formContainer != null){
			formContainer.appendChild(holder);
			Element t = holder.getElementsByTagName("input").getItem(0);
			t.focus();
			String entryId = canvas.createUniqueId();
			addOutputEntry(entryId, value, new RGType(RGTypeUtils.getSimpleVarType(canvas.getTypeChecker().createUniqueTypeName())), value);
			redrawPaths();
			
			id2EntryId.put(id, entryId);
			
			assignHandler(id);
			assignDelButtonHandler(dbuttonId, holderId, id);
			
		}else{
			Log.debug("formContainer NULL !!");
		}
		
		outputCounter += 1;
		
		return id;
	}
	
	void removeField(String id){
		
	}

	private void assignDelButtonHandler(String buttonId, final String holderId, final String formId){
		$("#" + buttonId).click(new Function(){
			@Override
			public void f(){
				removeField(holderId);
				fieldFormIds.remove(formId);
				$("#" + holderId).remove();
				removeEntry(id2EntryId.get(formId));
				id2EntryId.remove(formId);
				
//				Log.debug("field removed with id:" + formId);
//				Log.debug("ids:" + fieldFormIds.toString());
			}
		});
	}
	
	private void assignAddButtonHandler(String id){
		$("#" + id).click(new Function(){
			@Override
			public void f(){
				inputFormId = addOutputField("input" + outputCounter);
			}
		});
	}

	private void assignHandler(final String id) {
		$("#" + id).change(new Function(){
			@Override
			public void f(){
				//FIXME: validation, name must be unique, also with fields and inputFields
				updateEntryText(id2EntryId.get(id), $("#" + id).val());
				updatePortNameByEntryId(id2EntryId.get(id), $("#" + id).val());
			}
		});
		
	}

	@Override
	com.google.gwt.xml.client.Element toXml(Document doc) {
		com.google.gwt.xml.client.Element node = doc.createElement("workflowInputList");
		node.setAttribute("x", "" + getX());
		node.setAttribute("y", "" + getY());
		
//		ArrayList <String> pNames = getPortNames();
//		for(String pN: pNames){
//			NodePort p = getPortByPortName(pN);
//			if(p != null){
//				if(p.isConnected()){
//					com.google.gwt.xml.client.Element input = doc.createElement("workflowInputPort");
//					input.setAttribute("name", pN);
//					com.google.gwt.xml.client.Element type = doc.createElement("type");
//					type.appendChild(p.getType().getElement());
//					input.appendChild(type);
//					node.appendChild(input);
//				}
//			}
//		}
		for(int i = 0; i < fieldFormIds.size(); i++){
			String fId = fieldFormIds.get(i);
			String pName = getPortNameByEntryId(id2EntryId.get(fId));
			NodePort p = getPortByPortName(pName);
			if(p != null){
				if(p.isConnected()){
					com.google.gwt.xml.client.Element input = doc.createElement("workflowInputPort");
					input.setAttribute("name", pName);
					com.google.gwt.xml.client.Element type = doc.createElement("type");
					type.appendChild(p.getType().getElement());
					input.appendChild(type);
					node.appendChild(input);
				}
			}
		}
		return node;
	}
	
}
