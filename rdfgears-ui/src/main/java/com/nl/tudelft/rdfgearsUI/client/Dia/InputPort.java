package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.nl.tudelft.rdfgearsUI.client.RGType;

public class InputPort extends NodePort{
	private String indId;
	Element deletePathButton;
	Path p = null;
	public InputPort(String pId, Node pNode, RGType t) {
		super(pId, pNode, t);
		
		indId = "ind-" + pId;
		
		Element p = DOM.createDiv();
		p.setAttribute("class", "input-port port");
		p.setAttribute("id", id);
		p.setAttribute("style", "position: absolute; " +
									"width:8px; " +
									"height:8px; " +
									"left:1px;; " +
									"top:50%; " +
									"margin-top: -4px; " +
									"background-image: url('images/inport.png');");
									//"background-color:green; border-radius:50%");
		e = DOM.createDiv();
		e.setId(indId);
		e.setAttribute("class", "inputPortInd");
		e.setAttribute("style", "position:absolute;" +
										"left:-7px;" +
										"top:50%;" +
										"margin-top:-6px;" +
										"width: 10px;" +
										"height: 10px;");
		
		e.appendChild(p);
		
		deletePathButton = DOM.createDiv();
		deletePathButton.setAttribute("id", "dp-" + pId);
		deletePathButton.setClassName("deletePathButton");
		deletePathButton.setAttribute("style", "position:absolute;" +
											   "left: -11px;" +
											   "width: 11px;" +
											   "height: 10px;" +
											   "background-image: url('images/del-p.png');" +
											   "background-repeat: no-repeat;" +
											   "display: none;");
		e.appendChild(deletePathButton);
	}
	
	public void setAsNodeInputPort(int entryNum, int entryHeight, int headerHeight){
		e.setAttribute("style", "position:absolute;" +
				"left:-7px;" +
				"top: " + ((entryNum * entryHeight / 2) - (headerHeight / 2)) + "px;" +
				"margin-top:-6px;" +
				"width: 10px;" +
				"height: 10px;");
	}
	
	@Override
	public void enableEventHandler() {
		$("#"+ indId).mouseover(new Function (){
			@Override
			public void f(){
				parentNode.setDraggable(false);
				if(parentNode.canvas.isConnecting() && parentNode.getConnectedPath(getId()) == null && isActive){
					parentNode.canvas.setTargetPortCandidate(getInstance());
				}
				
				if(parentNode.canvas.getState() == RGCanvasState.NONE){
					p = parentNode.getConnectedPath(getId());
					if(p != null){
						deletePathButton.getStyle().setDisplay(Display.BLOCK);
						parentNode.canvas.setState(RGCanvasState.REMOVING_PATH);
					}
				}
			}
		});
		
		$("#"+ indId).mouseout(new Function (){
			@Override
			public void f(){
				parentNode.setDraggable(true);
				if(parentNode.canvas.getState() == RGCanvasState.REMOVING_PATH){
					deletePathButton.getStyle().setDisplay(Display.NONE);
					parentNode.canvas.setState(RGCanvasState.NONE);
				}
				p = null;
				if(parentNode.canvas.isConnecting() && isActive){
					//Log.info("reset candidate target port");
					parentNode.canvas.resetTargetPortCandidate();
				}
			}
		});
		
		$("#" + indId).mouseup(new Function (){
			public void f(){
				//Log.info("mouse up on :"+indId);
				if(parentNode.canvas.isConnecting() && parentNode.getConnectedPath(getId()) == null && isActive){
					if(!parentNode.canvas.getActivePath().getStartNode().isCyclicTo(parentNode.getId())){
						parentNode.canvas.setTargetNode(parentNode.getInstance());
						parentNode.canvas.setState(RGCanvasState.CONNECTED);
					}else{
						parentNode.canvas.resetTargetPortCandidate();
						parentNode.canvas.displayErrorMessage("Cannot create connection, it will create cyclic graph");
					}
						
				}
			}
		});
		
		$("#" + "dp-" + getId()).click(new Function(){
			public void f(){
				if(p != null){
					p.remove();
					p = null;
					deletePathButton.getStyle().setDisplay(Display.NONE);
					parentNode.setIsModified();
					parentNode.canvas.doTypeCheck();
				}
			}
		});
		
	}

	@Override
	public void setActive(boolean s) {
		isActive = s;
		if(s){
			e.addClassName("inputPortActive");
		}else{
			e.removeClassName("inputPortActive");
		}
		
	}

	@Override
	public boolean isConnected() {
		if(parentNode.getConnectedPath(getId()) != null)
			return true;
		
		return false;
	}

	@Override
	public NodePort getConnectedPort() {
		if(isInputPort && isConnected()){
			return parentNode.getConnectedPath(getId()).getStartPort();
		}
		return null;
	}

	@Override
	public Path getPath() {
		return parentNode.getConnectedPath(getId());
	}

}
