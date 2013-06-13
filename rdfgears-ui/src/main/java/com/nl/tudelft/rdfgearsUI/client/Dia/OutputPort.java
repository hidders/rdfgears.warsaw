package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.nl.tudelft.rdfgearsUI.client.RGType;

public class OutputPort extends NodePort{

	public OutputPort(String pId, Node pNode, RGType t) {
		super(pId, pNode, t);
		isInputPort = false;
		
		e = DOM.createDiv();
		
		e.setAttribute("class", "output-port port");
		e.setAttribute("id", id);
		e.setAttribute("style", "position: absolute; " +
									"width:8px; " +
									"height:8px; " +
									"right:-5px; " +
									"top:50%; " +
									"margin-top: -4px; " +
									"background-image: url('images/outport.png');");
									//"background-color:red; border-radius:50%; moz-border-radius:50%;");
	}
	
	/**
	 * set the output port as the node output port instead of an entry output port
	 * @param entryNum : number of node's entry
	 * @param entryHeight : height of an entry
	 */
	public void setAsNodeOuputPort(int entryNum, int entryHeight){
		e.setAttribute("style", "position: absolute; " +
				"width:8px; " +
				"height:8px; " +
				"right:-5px; " +
				"top:" + (entryNum * entryHeight / 2) + "px;" +
				"margin-top: -4px; " + //(height / 2) + (barHeight /2)
				"background-image: url('images/outport.png');");
//				"background-color:red; border-radius:50%; moz-border-radius:50%;");
	}
	
	@Override
	public void enableEventHandler() {
		$("#"+ id).mouseover(new Function (){
			@Override
			public void f(){
					parentNode.setDraggable(false);
			}
		});
		
		$("#"+ id).mouseout(new Function (){
			@Override
			public void f(){
				parentNode.setDraggable(true);
			}
		});
		
		$("#"+ id).mousedown(new Function (){
			@Override
			public void f(){
			//	Log.debug("set canvas state to: connecting, from output port");
				parentNode.canvas.drawConnectionFrom(parentNode.getInstance(), getInstance());
				parentNode.canvas.setState(RGCanvasState.CONNECTING);
				
			}
		});	
		
	}

	@Override
	public void setActive(boolean s) {}

	@Override
	public boolean isConnected() {
		if(parentNode.getConnectedPath(getId()) != null)
			return true;
		
		return false;
	}

	@Override
	public NodePort getConnectedPort() {
		//only needed by input port
		return null;
	}

	@Override
	public Path getPath() {
		//only needed by input port
		return null;
	}

}
