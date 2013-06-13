package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLPanel;

public class RGTab {
	private Element root, label, closeButton;
	private RGWorkflow myWorkflow;
	private RGCanvas canvas;
	private String labelId, closeButtonId;
	public RGTab(RGWorkflow wf, RGCanvas c){
		myWorkflow = wf;
		canvas = c;
		root = DOM.createDiv();
		root.setId(canvas.createUniqueId());
		root.setClassName("workflowTab");
		root.setAttribute("style", "position:relative;" +
								   "display: inline-block;" +
								   "width:100px;" +
								   "height:20px;" +
								   "margin-right:2px;");
		labelId = HTMLPanel.createUniqueId();
		label = DOM.createDiv();
		label.setId(labelId);
		label.setInnerText(myWorkflow.getName());
		label.setAttribute("style", "position:absolute;" +
									"left:3px;" +
									"bottom:1px;" +
									"width:77px;" +
									"height:17px;" +
									"overflow:hidden;" +
									"white-space: nowrap;" +
									"text-overflow:ellipsis;");
		root.appendChild(label);
		
		closeButtonId = HTMLPanel.createUniqueId();
		closeButton = DOM.createDiv();
		closeButton.setId(closeButtonId);
		closeButton.addClassName("tabCloseButton");
		closeButton.setAttribute("style", "position:absolute;" +
									"width:19px;" +
									"height:19px;" +
									"top:1px;" +
									"right:2px;");
		
		root.appendChild(closeButton);
		
	}
	
	public Element getElement(){
		return root;
	}
	
	public void refresh(){
		label.setInnerText(myWorkflow.getName());
	}
	public void remove(){
		root.removeFromParent();
	}
	public void setActive(boolean v){
		if(v)
			root.addClassName("activeWorkflowTab");
		else
			root.removeClassName("activeWorkflowTab");
	}
	
	public void enableEventHandler(){
		$("#" + labelId).click(new Function(){
			public void f(){
				canvas.setActiveWorkflow(myWorkflow);
			}
		});
		
		$("#" + closeButtonId).click(new Function(){
			public void f(){
				canvas.closeWorkflow(myWorkflow);
			}
		});
		
		$("#" + closeButtonId).mouseover(new Function(){
			public void f(){
			}
		});
		
		$("#" + closeButtonId).mouseout(new Function(){
			public void f(){
			}
		});
	}

}
