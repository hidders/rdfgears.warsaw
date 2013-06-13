package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.GQuery.Offset;
import com.nl.tudelft.rdfgearsUI.client.RGType;

public abstract class NodePort {
	protected String id;
	protected Element e;
	protected RGType type;
	protected Node parentNode;
	protected boolean isInputPort = true;
	protected boolean isActive = false;
	public NodePort(String pId, Node pNode, RGType t){
		this.id = pId;
		this.parentNode = pNode;
		this.type = t;
	}
	
	public String getId(){
		return id;
	}
	
	public Element getElement(){
		return e;
	}
	
	public RGType getType(){
		return type;
	}
	public Node getParentNode(){
		return parentNode;
	}

	
	public Offset getCenterCoordinate(){
		Offset pos = $("#" + id).offset();
		if(isInputPort){
			pos.top += (parentNode.canvas.getTopMargin() + 3); //place it in the middle of the port
			pos.left += parentNode.canvas.getLeftMargin();
		}else{
			pos.top += (parentNode.canvas.getTopMargin() + 3); //place it in the middle of the port
			pos.left += (4 + parentNode.canvas.getLeftMargin());
		}
		return pos;
	}
	public NodePort getInstance(){
		return this;
	}
	public abstract void setActive(boolean s);
	public abstract void enableEventHandler();
	public abstract boolean isConnected();
	public abstract NodePort getConnectedPort();
	public abstract Path getPath();
	
}
