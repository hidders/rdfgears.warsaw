package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;

public class RGFunctionParamConstant extends RGFunctionParam{
	public String pValue = "", inputFormId;
	Node owner;
	
	public RGFunctionParamConstant(String id, String value, Node n) {
		super(id, "");
		pType = RGFunctionParamType.CONSTANT;
		owner = n;
		pValue = value;
	}

	@Override
	void setValueFromString(String s) {
		pValue = s;
		
	}
	
	void setValue(String s){
		pValue = s;
	}

	@Override
	void display(Element container) {
		if(elementCache == null){
			initDisplayElement();
			
			if(!desc.equals("")){
				Element descContainer = DOM.createDiv();
				descContainer.setClassName("paramFormHelpText");
				descContainer.setInnerText(desc);
				elementCache.appendChild(descContainer);
				container.appendChild(elementCache);
			}
		}else {
			if(!desc.equals(""))
				container.appendChild(elementCache);
		}	
	}

	@Override
	com.google.gwt.xml.client.Element toXml(Document doc) {
		com.google.gwt.xml.client.Element var = doc.createElement("config");
		var.setAttribute("param", getId());
		if(pValue != null){
			if(pValue.length() > 0){
				if(owner.isAWorkflowNode())
					var.appendChild(doc.createTextNode("workflow:" + owner.getWorkflowId()));
				else
					var.appendChild(doc.createTextNode(pValue));
				
			}
		}
		return var;
	}

	@Override
	void assignHandler(String id) {
		
	}
}
