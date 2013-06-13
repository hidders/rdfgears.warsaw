package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;

public class RGFunctionParamDescription extends RGFunctionParam {
	String descText;
	Element descContainer;
	public RGFunctionParamDescription(String id, String Label, String desc) {
		super(id, Label);
		descText = desc;
	}

	@Override
	void setValueFromString(String s) {
		descText = s;
		descContainer.setInnerText(descText);
	}

	@Override
	void display(Element container) {
		if(elementCache == null){
			initDisplayElement();
			descContainer = DOM.createDiv();
			descContainer.setInnerHTML(descText);
			formContainer.appendChild(descContainer);
			container.appendChild(elementCache);
		}else {
			container.appendChild(elementCache);
		}
	}

	@Override
	void assignHandler(String id) {
		
	}

	@Override
	com.google.gwt.xml.client.Element toXml(Document doc) {
		return null;
	}

}
