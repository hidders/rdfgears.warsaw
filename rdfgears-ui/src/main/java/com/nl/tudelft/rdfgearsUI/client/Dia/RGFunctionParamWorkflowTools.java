package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.allen_sauer.gwt.log.client.Log;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;

public class RGFunctionParamWorkflowTools extends RGFunctionParam{
	String openWfButtonId = "XXD9FE8G398";
	String copyWfButtonId = "XXD9FE8G399";
	Node owner;
	public RGFunctionParamWorkflowTools(String id, Node n) {
		super(id, "Tools");
		owner = n;
		Log.debug("workflow tools added for:" + id);
	}

	@Override
	void setValueFromString(String s) {}

	@Override
	void display(Element container) {
		if(elementCache == null){
			initDisplayElement();
			Element copyWf = DOM.createDiv();
			copyWf.setId(copyWfButtonId);
			copyWf.setInnerText("Copy Workflow");
			Element openWf = DOM.createDiv();
			openWf.setId(openWfButtonId);
			openWf.setInnerText("Open Workflow");
			openWf.setClassName("addFieldButton");
			copyWf.setClassName("addFieldButton");
			formContainer.appendChild(openWf);
			
			formContainer.appendChild(copyWf);
			assignHandler(pId);
			container.appendChild(elementCache);
			assignHandler(pId);
		}else {
			container.appendChild(elementCache);
		}
	}

	@Override
	void assignHandler(String id) {
//		final String wfId = id;
		$("#" + openWfButtonId).click(new Function(){
			public void f(){
				owner.canvas.openWorkflow(owner.getWorkflowId());
			}
		});
		
		$("#" + copyWfButtonId).click(new Function(){
			public void f(){
				owner.canvas.copyWorkflow(owner.getWorkflowId(), true);
			}
		});
	}

	@Override
	com.google.gwt.xml.client.Element toXml(Document doc) {
		return null;
	}

}
