package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;
import static gwtquery.plugins.draggable.client.Draggable.Draggable;
import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.DragFunction;
import gwtquery.plugins.draggable.client.events.DragContext;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.DOM;

public class OperatorListItem extends NavigationListItem{
	Element item;
	RGCanvas canvas;
	String id;
	String name;
	String elementId;
	int x = 0;
	int y = 0;
	private DraggableOptions draggOptions = new DraggableOptions();
	
	public OperatorListItem(String _id, String text, RGCanvas _canvas){
		id = _id;
		name = text;
		canvas = _canvas;
		elementId = canvas.createUniqueId();
		item = DOM.createDiv();
		item.setId(elementId);
		item.setClassName("operatorListItem");
		item.setAttribute("style", "");
		item.setInnerText(text);
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
					canvas.createNodeByType(id, x, y);
				}
				//canvas.addBagUnionNode(x, y);
			}
		});
		
		$("#" + elementId).as(Draggable).draggable(draggOptions);
	}
	
	public Element getElement(){
		return item;
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
		return null;
	}

	@Override
	void setVisible(boolean v) {
		if(!v)
			item.addClassName("hidden");
		else
			item.removeClassName("hidden");
		
	}
}
