package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;
import static gwtquery.plugins.draggable.client.Draggable.Draggable;

import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.DragFunction;
import gwtquery.plugins.draggable.client.events.DragContext;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.DOM;

//draggable list item
public class WorkflowListItem extends NavigationListItem{
	Element item, labelContainer, descContainer, menuContainer;
	Element menuEdit, menuCopy, menuDel, menuMark, menuRun;
	private RGCanvas canvas;
	private String id;
	private String descText = "";
	private String name = "";
	int sortDescLength = 80;
	private String elementId;
	
	private DraggableOptions draggOptions = new DraggableOptions();
	int x = 0;
	int y = 0;
	public WorkflowListItem(String _id, String label, String desc, RGCanvas _canvas){		
		int h = 31;
		if(desc.length() > 0)
			h = 61;
		descText = desc;
		id = _id;
		name = label;
		canvas = _canvas;
		elementId = canvas.createUniqueId();
		item = DOM.createDiv();
		item.setId(elementId);
		item.setClassName("workflowListItem");
		item.setAttribute("style", "height:" + h +"px;");
		labelContainer = DOM.createDiv();
		labelContainer.setId("label-" + elementId);
		labelContainer.setClassName("workflowListItemLabel");
		labelContainer.setAttribute("style", "height:15px;");
		labelContainer.setInnerText(label);
		item.appendChild(labelContainer);
		
		if(desc.length() > 0){
			descContainer = DOM.createDiv();
			descContainer.setId("desc-" + elementId);
			descContainer.setClassName("workflowListItemDesc");
			descContainer.setAttribute("style", "height:30px;font-size:85%;");
			descContainer.setInnerText(getTruncatedDesc());
			item.appendChild(descContainer);
		}
		
		menuContainer = DOM.createDiv();
		menuContainer.setId("menu-" + elementId);
		menuContainer.setClassName("workflowListItemMenuBar");
		menuContainer.setAttribute("style", "height:16px;");
		
		menuRun = createMenuItem("menu-run-" + elementId, "workflowListItemMenu", "Run");
		menuEdit = createMenuItem("menu-edit-" + elementId, "workflowListItemMenu", "Edit");
		menuCopy = createMenuItem("menu-copy-" + elementId, "workflowListItemMenu", "Copy");
		menuDel = createMenuItem("menu-del-" + elementId, "workflowListItemMenu", "Del");
		//menuMark = createMenuItem("menu-mark-" + elementId, "workflowListItemMenu", "Mark");
		
		menuContainer.appendChild(menuRun);
		menuContainer.appendChild(menuEdit);
		menuContainer.appendChild(menuCopy);
		menuContainer.appendChild(menuDel);
		//menuContainer.appendChild(menuMark);
		
		item.appendChild(menuContainer);
	}
	
	private Element createMenuItem(String menuId, String className, String text){
		Element menuEl = DOM.createDiv();
		menuEl.setId(menuId);
		menuEl.setClassName(className);
		menuEl.setInnerText(text);
		
		return menuEl;
	}
	
	public void enableEventHandler(){
		GQuery helper = $("<div id=\"dragHelper\" class=\"dragHelper\" style=\"width: 100px;height: 70px;\"></div>");
		
		draggOptions.setZIndex(10002);
		draggOptions.setHelper(helper);
		draggOptions.setAppendTo("#canvas");
		
		draggOptions.setOnDrag(new DragFunction(){
			public void f(DragContext context) {
				x = $("#dragHelper").left();
				y = $("#dragHelper").top();
			}
		});
		
		draggOptions.setOnDragStop(new DragFunction(){
			public void f(DragContext context){
				if(x > 250){
					canvas.createNodeFromWorkflow(id, x, y);
				}
				//canvas.addBagUnionNode(x, y);
			}
		});
		
		$("#label-" + elementId).as(Draggable).draggable(draggOptions);
		$("#menu-edit-" + elementId).click(new Function(){
			public void f(){
				canvas.openWorkflow(id);
			}
		});
		$("#menu-copy-" + elementId).click(new Function(){
			public void f(){
				canvas.copyWorkflow(id, false);
			}
		});
		$("#menu-del-" + elementId).click(new Function(){
			public void f(){
				canvas.deleteWorkflow(id);
			}
		});
		
		$("#menu-run-" + elementId).click(new Function(){
			public void f(){
				canvas.runWorkflow(id);
			}
		});
	}
	
	public Element getElement(){
		return item;
	}
	
	private String getTruncatedDesc(){
		if(descText != null){
			if(descText.length() > sortDescLength){
				return descText.substring(0, sortDescLength).replaceAll("\\w+$", "..."); //add ellipsis
			}
		}
		return descText;
	}

	@Override
	String getName() {
		return name;
	}

	@Override
	String getId() {
		return id;
	}

	@Override
	String getDesc() {
		return descText;
	}

	@Override
	void setVisible(boolean v) {
		if(!v){
//			Log.debug("hiding: " + name);
			item.addClassName("hidden");
		}else
			item.removeClassName("hidden");
	}
}
