package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.xml.client.XMLParser;

public class RGWorkflow {
	private ArrayList<String> nodeIds = new ArrayList <String>();
	private Map <String, Node> nodes = new HashMap<String, Node>();
	private Map <String, NodeDrawingState> nodesDrawingState = new HashMap<String, NodeDrawingState>();
	private boolean isVisible = false;
	private boolean isNewWorkflow = false;
	private boolean isEditingWorkflow = false;
	private boolean isNeedSaving = true;
	private boolean wellTyped = true;
	
	private com.google.gwt.xml.client.Document lastSavedSource;
	private Node lastActiveNode;
	private int draggTopMargin = 0;
	private int draggLeftMargin = 0;
	
	private String id = "";
	private String name = "";
	private String desc = "";
	private String category = "";
	private RGCanvas canvas = null;
	private Element elementCache = null;
	private String nameFormId = HTMLPanel.createUniqueId();
	private String idFormId = HTMLPanel.createUniqueId();
	private String descFormId = HTMLPanel.createUniqueId();
	private String catFormId = HTMLPanel.createUniqueId();
	private String catOptionFormId = HTMLPanel.createUniqueId();
	private ArrayList <String> cats = new ArrayList <String> ();
	private ArrayList <String> catOptionValues = new ArrayList <String> ();
	private ListBox categoriesLb;
	Element idForm;
	private ArrayList <String> latestTypeCheckLog = new ArrayList <String>();
	
	public RGWorkflow (String _id, String _name, String _desc, String _category, RGCanvas _canvas){
		id = _id;
		name = _name;
		desc = _desc;
		category = _category;
		canvas = _canvas;
	}
	
	public void setId(String _id){
		id = _id;
	}
	public String getId(){
		return id.trim();
	}
	
	public void setIsModified(boolean v){
		isNeedSaving = v;
	}
	
	public boolean isModified(){
		return isNeedSaving;
	}
	public boolean isWellTyped(){
		return wellTyped;
	}
	public void setWellTypeness(boolean v){
		wellTyped = v;
	}
	public void setIsEditing(boolean v){
		isEditingWorkflow = v;
	}
	public boolean isEditing(){
		return isEditingWorkflow;
	}
	public com.google.gwt.xml.client.Document getLastSavedSource(){
		return lastSavedSource;
	}
	public void setLastSavedSource(com.google.gwt.xml.client.Document doc){
		lastSavedSource = doc;
	}
	public void setAsNewWorkflow(){
		isNewWorkflow = true;
	}
	public void setName(String _name){
		name = _name;
	}
	public void setAsSavedWorkflow(){
		isNewWorkflow = false;
	}
	
	public boolean isNewWorkflow(){
		return isNewWorkflow;
	}
	
	public String getName(){
		return name.trim();
	}
	public void setDescription(String _desc){
		desc = _desc;
	}
	public String getDescription(){
		return desc;
	}
	public void setCategory(String cat){
		category = cat;
	}
	public void setDraggTopMargin(int v){
		draggTopMargin = v;
	}
	public int getDraggTopMargin(){
		return draggTopMargin;
	}
	public void setDraggLeftMargin(int v){
		draggLeftMargin = v;
	}
	public int getDraggLeftMargin(){
		return draggLeftMargin;
	}
	public String getCategory(){
		return category;
	}
	public void addNode(String nodeId, Node n){
		nodeIds.add(nodeId);
		nodes.put(nodeId, n);
	}
	
	public boolean hasNode(String nodeId){
		return nodeIds.contains(nodeId);
	}
	
	public Node getNestedWorkflowNode(String wfId){
		for(String nId: nodeIds){
			Node n = nodes.get(nId);
			if(n.isAWorkflowNode()){
				if(n.getWorkflowId().equals(wfId)){
					return n;
				}
			}
		}
		return null;
	}
	
	public ArrayList <String> getNodeIds(){
		return nodeIds;
	}
	
	public Map <String, Node> getNodes(){
		return nodes;
	}
	
	public Node getNode(String nodeId){
		if(nodes.containsKey(nodeId)){
			return nodes.get(nodeId);
		}
		return null;
	}
	
	public boolean isFinishDrawingNodes(){
		for(String nId: nodeIds){
			if(nodesDrawingState.containsKey(nId)){
				if(nodesDrawingState.get(nId) != NodeDrawingState.DONE)
				return false;
			}
		}
		
		return true;
	}
	
	public void syncSaveWorkflowNode(String wfId){
		for(String nId: nodeIds){
			Node n = nodes.get(nId);
			if(n != null){
				if(n.isAWorkflowNode() && n.getWorkflowId().equals(wfId)){
					n.showWarningButton(true);
				}
			}
		}
	}
	
	public void syncDeleteWorkflowNode(String wfId){
		@SuppressWarnings("unchecked")
		ArrayList <String> _nodeIds = (ArrayList<String>) nodeIds.clone();
		for(String nId: _nodeIds){
			Node n = nodes.get(nId);
			if(n != null){
				if(n.isAWorkflowNode() && n.getWorkflowId().equals(wfId)){
					cascadeRemoveNodeById(n.getId());
				}
			}
		}
	}
	
	public void showHideNodeId(){
		for(String nId: nodeIds){
			Node n = nodes.get(nId);
			if(n != null){
				if(!n.getId().equals(RGCanvas.WORKFLOW_INPUT_NODE_ID) && !n.getId().equals(RGCanvas.WORKFLOW_OUTPUT_NODE_ID)){
					n.showHideNodeId();
				}
			}
		}
	}
	/**
	 * update drawing state of existing node or 
	 * create one state if the node drawing status do not exist
	 * @param nodeId
	 * @param state
	 */
	public void updateNodeDrawingState(String nodeId, NodeDrawingState state){
		nodesDrawingState.put(nodeId, state);
	}
	
	public void clear(){
		@SuppressWarnings("unchecked")
		ArrayList <String> _nodeIds = (ArrayList<String>) nodeIds.clone();
		for(String nId: _nodeIds){
			Node n = nodes.get(nId);
			if(n != null)
				n.remove();
		}
	}
	/**
	 * only remove reference to this node
	 * @param nodeId
	 */
	public void removeNodeById(String nodeId){
		nodes.remove(nodeId);
		nodeIds.remove(nodeId);
	}
	
	/**
	 * will remove node reference and its element from canvas
	 * @param nodeId
	 */
	public void cascadeRemoveNodeById(String nodeId){
		Node n = nodes.get(nodeId);
		if(n != null){
			removeNodeById(nodeId);
			n.remove();
			canvas.removeFromActiveNode(nodeId);
			canvas.setState(RGCanvasState.NONE);
		}
	}
	
	public void setVisible(boolean v){
		isVisible = v;
		
		for(String nId: nodeIds){
			Node n = nodes.get(nId);
			if(n!=null)
				n.setVisible(v);
		}
	}
	
	public boolean isVisible(){
		return isVisible;
	}
	
	public void setLastActiveNode(Node n){
		lastActiveNode = n;
	}
	
	public Node getLastActiveNode(){
		return lastActiveNode;
	}
	
	public void setTypeCheckLog(ArrayList <String> log){
		latestTypeCheckLog = log;
	}
	
	public ArrayList <String> getTypeCheckLog(){
		return latestTypeCheckLog;
	}
	
	public com.google.gwt.xml.client.Document exportToXml(){
		com.google.gwt.xml.client.Document doc = XMLParser.createDocument();
		com.google.gwt.xml.client.Element root, meta, id, name, desc, cat, wf, network;
		
		root = doc.createElement("rdfgears");
		meta = doc.createElement("metadata");
		id = doc.createElement("id"); 
		id.appendChild(doc.createTextNode(getId()));
		name = doc.createElement("name"); 
		name.appendChild(doc.createTextNode(getName()));
		desc = doc.createElement("description");
		desc.appendChild(doc.createTextNode(getDescription()));
		cat = doc.createElement("category");
		cat.appendChild(doc.createTextNode(getCategory()));
		
		meta.appendChild(id);
		meta.appendChild(name);
		meta.appendChild(desc);
		meta.appendChild(cat);
		meta.appendChild(doc.createElement("password"));
		
		wf = doc.createElement("workflow");
		Node n = nodes.get(RGCanvas.WORKFLOW_INPUT_NODE_ID);
		wf.appendChild(n.toXml(doc));
		n = nodes.get(RGCanvas.WORKFLOW_OUTPUT_NODE_ID);
		network = n.toXml(doc);
		
		for(String nId: nodeIds){
			if(!(nId.equalsIgnoreCase(RGCanvas.WORKFLOW_INPUT_NODE_ID) || nId.equalsIgnoreCase(RGCanvas.WORKFLOW_OUTPUT_NODE_ID))){
				n = nodes.get(nId);
				network.appendChild(n.toXml(doc));
			}
		}
		
		wf.appendChild(network);
		root.appendChild(meta);
		root.appendChild(wf);
		doc.appendChild(root);
		return doc;
	}
	
	public void displayProperties(Element container){
		if(elementCache == null){
			elementCache = DOM.createDiv();
			elementCache.setClassName("propertyContainer");
			
			Element namePropertyHolder = DOM.createDiv();
			Element nameLabelContainer = DOM.createDiv();
			nameLabelContainer.setClassName("propertyLabelContainer");
			nameLabelContainer.setInnerText("Workflow Name");
			Element nameFormContainer = DOM.createDiv();
			nameFormContainer.setClassName("propertyFormContainer");
			Element nameForm = DOM.createInputText();
			nameForm.setAttribute("value", name);
			nameForm.setClassName("inputString");
			nameForm.setId(nameFormId);
			nameFormContainer.appendChild(nameForm);
			
			namePropertyHolder.appendChild(nameLabelContainer);
			namePropertyHolder.appendChild(nameFormContainer);
			
			
			Element idPropertyHolder = DOM.createDiv();
			Element idLabelContainer = DOM.createDiv();
			idLabelContainer.setClassName("propertyLabelContainer");
			idLabelContainer.setInnerText("Workflow Unique Id");
			Element idFormContainer = DOM.createDiv();
			idFormContainer.setClassName("propertyFormContainer");
			idForm = DOM.createInputText();
			idForm.setAttribute("value", id);
			idForm.setAttribute("readonly", "readonly");
			idForm.setClassName("inputString");
			idForm.setId(idFormId);
			idFormContainer.appendChild(idForm);
			
			Element idDescContainer = DOM.createDiv();
			idDescContainer.setClassName("paramFormHelpText");
			idDescContainer.setInnerText("*Valid Characters: a-z A-Z 0-9 - _");
			//elementCache.appendChild(descContainer);
			
			idPropertyHolder.appendChild(idLabelContainer);
			idPropertyHolder.appendChild(idFormContainer);
			idPropertyHolder.appendChild(idDescContainer);
			
			Element descPropertyHolder = DOM.createDiv();
			Element descLabelContainer = DOM.createDiv();
			descLabelContainer.setClassName("propertyLabelContainer");
			descLabelContainer.setInnerText("Workflow Description");
			Element descFormContainer = DOM.createDiv();
			descFormContainer.setClassName("propertyFormContainer");
			Element descForm = DOM.createTextArea();
			descForm.setInnerText(desc);
			descForm.setClassName("queryText");
			descForm.setId(descFormId);
			descFormContainer.appendChild(descForm);
			
			descPropertyHolder.appendChild(descLabelContainer);
			descPropertyHolder.appendChild(descFormContainer);
			
			Element catPropertyHolder = DOM.createDiv();
			Element catLabelContainer = DOM.createDiv();
			catLabelContainer.setClassName("propertyLabelContainer");
			catLabelContainer.setInnerText("Workflow Category");
			Element catFormContainer = DOM.createDiv();
			catFormContainer.setClassName("propertyFormContainer");
			//category dropdown box
			categoriesLb = buildCategoryListForm(category);
			catFormContainer.appendChild(categoriesLb.getElement());
			
			Element newCatDesc = DOM.createDiv();
			newCatDesc.setClassName("paramFormHelpText");
			newCatDesc.setInnerText("Fill the category name below to create new category and it will overwrite the category name above");
			catFormContainer.appendChild(newCatDesc);
			
			Element catForm = DOM.createInputText();
			catForm.setAttribute("value", "");
			catForm.setClassName("inputString");
			catForm.setId(catFormId);
			catFormContainer.appendChild(catForm);
			
			catPropertyHolder.appendChild(catLabelContainer);
			catPropertyHolder.appendChild(catFormContainer);
			
			
			elementCache.appendChild(namePropertyHolder);
			elementCache.appendChild(idPropertyHolder);
			elementCache.appendChild(descPropertyHolder);
			elementCache.appendChild(catPropertyHolder);
			
			Element descContainer = DOM.createDiv();
			descContainer.setClassName("paramFormHelpText");
			descContainer.setInnerText("Configure workflow properties");
			elementCache.appendChild(descContainer);
			
			container.appendChild(elementCache);
			
			//assignAddButtonHandler(addFieldButtonId);
			//assignHandler(inputFormId);
			enableHandler();
		}else{
			container.appendChild(elementCache);
		}
	}
	
	ListBox buildCategoryListForm(String selectedCat){
		ListBox lb = new ListBox();
		lb.getElement().setId(catOptionFormId);
		cats = canvas.getNavigationPanel().getWorkflowCategories();
		int i = 1;
		lb.addItem("Choose Category","novalue");
		for(String cat: cats){
			lb.addItem(cat, cat);
			catOptionValues.add(cat);
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
	
	public void setIdReadOnly(boolean v){
		if(v){
			idForm.setAttribute("readonly", "readonly");
			idForm.addClassName("readOnlyTextBox");
		}else{
			if(idForm.hasAttribute("readonly")){
				idForm.removeAttribute("readonly");
				idForm.removeClassName("readOnlyTextBox");
			}
		}
	}
//	private void refreshCatOptionForm(){
//		cats = canvas.getNavigationPanel().getWorkflowCategories();
//		
//		for(String cat: cats){
//			if(!catOptionValues.contains(cat)){
//				categoriesLb.addItem(cat, cat);
//				catOptionValues.add(cat);
//			}
//		}
//		if(cats.size() > 0){
//			
//		}
//	}
	public void enableHandler(){
		$("#" + nameFormId).blur(new Function(){
			@Override
			public void f(){
				name = $("#" + nameFormId).val();
				$("#" + nameFormId).attr("value", name);
				canvas.getTabBarPanel().refreshTabTitleFor(getId());
			}
		});
		
		$("#" + idFormId).blur(new Function(){
			@Override
			public void f(){
				String newId = $("#" + idFormId).val();
				
				if(newId.matches("^[a-zA-Z0-9-_]+$")){
					if(!newId.equals(id)){
						if(canvas.replaceOpenedWorkflowId(id, newId)){
							id = $("#" + idFormId).val();
							$("#" + idFormId).attr("value", id);
						}else{
							$("#" + idFormId).attr("value", id);
							canvas.displayErrorMessage("Another workflow with the same id is being opened");
						}
					}
				}else{
					canvas.displayErrorMessage("Workflow id contain illegal characters");
					if(DOM.getElementById(idFormId) != null){
						$("#" + idFormId).attr("value", id);
					}
				}
				
			}
		});
		
		$("#" + descFormId).blur(new Function(){
			@Override
			public void f(){
				desc = $("#" + descFormId).val();
				$("#" + descFormId).attr("value", desc);
			}
		});
		$("#" + catOptionFormId).change(new Function(){
			public void f(){
				if($("#" + catOptionFormId).val().equals("novalue")){
					category = "";
				}else{
					category = $("#" + catOptionFormId).val();
					Log.debug("value of: " + catOptionFormId + " -> " +category);
				}
				$("#" + catFormId).val("");
			}
		});
		
//		$("#" + catOptionFormId).mouseover(new Function(){
//			public void f(){
//				Log.debug("over:" + cats.size());
//				Log.debug(categoriesLb.getElement().getId());
//				if($("#" + catOptionFormId).val().isEmpty()){
//					Log.debug("updateing");
//					refreshCatOptionForm();
//				}
//			}
//		});
		
		
		$("#" + catFormId).blur(new Function(){
			@Override
			public void f(){
				category = $("#" + catFormId).val();
				$("#" + catFormId).attr("value", category);
			}
		});
		
	}
}
