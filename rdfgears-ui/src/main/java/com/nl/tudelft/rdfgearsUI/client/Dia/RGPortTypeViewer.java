package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;

public class RGPortTypeViewer extends RGFunctionParam {
	Node myNode;
	Element content = DOM.createDiv();
	public RGPortTypeViewer(Node n){
		super(n.canvas.createUniqueId(), "Ports Type");
		myNode = n;
	}
	@Override
	void setValueFromString(String s) {
		
	}

	@Override
	void display(Element container) {
		if(elementCache == null){
			initDisplayElement();
			Element refreshButton = DOM.createDiv();
			//addFieldButton.setInnerText("+ add input");
			refreshButton.setInnerHTML("<img src=\"images/refresh.png\" /> Refresh");
			String refreshButtonId = myNode.canvas.createUniqueId();
			refreshButton.setId(refreshButtonId);
			refreshButton.setClassName("addFieldButton");
			formContainer.appendChild(refreshButton);
			
			refreshContent();
			
			formContainer.appendChild(content);
			container.appendChild(elementCache);
			assignHandler(refreshButtonId);
		}else{
			container.appendChild(elementCache);
		}
		
		
	}
	
	private void refreshContent(){
		content.setInnerText("");
		for(String pN: myNode.getPortNames()){
			try{
				Element t = DOM.createElement("table");
				t.addClassName("portTable");
				Element trPortName = DOM.createElement("tr");
				Element tdPortName = DOM.createElement("td");
				
				if(pN.equals(myNode.getId()) && !myNode.getId().equals(RGCanvas.WORKFLOW_OUTPUT_NODE_ID))
					tdPortName.setInnerText("Ouput:");
				else
					tdPortName.setInnerText(pN + ":");
				
				tdPortName.addClassName("tableFont");
				tdPortName.addClassName("tdContent");
				
				trPortName.appendChild(tdPortName);
				
				Element trPortType = DOM.createElement("tr");
				Element tdPortType = DOM.createElement("td");
				
				
				tdPortType.setInnerText(myNode.getPortByPortName(pN).getType().getElement().toString());
				tdPortType.addClassName("tableFont");
				tdPortType.addClassName("tdContent");
				trPortType.appendChild(tdPortType);
				
				t.appendChild(trPortName);
				t.appendChild(trPortType);
				
				content.appendChild(t);
				
			}catch(Exception e){}
		}
	}
	@Override
	void assignHandler(String id) {
		$("#" + id).click(new Function(){
			@Override
			public void f(){
				refreshContent();
			}
		});
	}

	@Override
	com.google.gwt.xml.client.Element toXml(Document doc) {
		return null;
	}
}
