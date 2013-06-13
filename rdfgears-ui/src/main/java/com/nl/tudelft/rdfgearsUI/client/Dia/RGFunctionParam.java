/**
 * @author marojahan
 * 
 */
package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

enum RGFunctionParamType {
	STRING, //text box
	TEXT, //text area -> e.g SPARQL Query
	FIELDS, //collection of strings
	INPUT_FIELDS, //fields with input port
	LIST, //drop down box.. from specific source
	QUERY, //text and supported by query editor
	CONSTANT //constant variable
}
/*
 * type -> value
 * 
 * string : [value]
 * option : [value idx]:[value]
 * fields : [id1]:[value];[id2]:[value];..:..
 * input_fields: [id1]:[value];[id2]:[value];..:..
 * text   : [value]
 */
public abstract class RGFunctionParam {
	public String pId;
	public RGFunctionParamType pType;
	public String pLabel;
	public Element holderElement, labelContainer, formContainer;
	public Element elementCache = null;
	public String desc = "";
	
	public RGFunctionParam(String id, String label){
		pId = id;
		pLabel = label;
	}
	public void setDescriptionText(String d){
		desc = d;
		//Log.debug("desc content after set: " + desc);
	}
	public String getId(){
		return pId;
	}
	public void removeElement(){
		holderElement.removeFromParent();
	}
	
	public void initDisplayElement(){
		//Log.debug("desc content from parent: " + desc);
		elementCache = DOM.createDiv();
		elementCache.setClassName("propertyContainer");
		labelContainer = DOM.createDiv();
		labelContainer.setClassName("propertyLabelContainer");
		labelContainer.setInnerText(pLabel);
		formContainer = DOM.createDiv();
		formContainer.setClassName("propertyFormContainer");
		
		elementCache.appendChild(labelContainer);
		elementCache.appendChild(formContainer);
	}
	
	abstract void setValueFromString(String s);
	abstract void display(Element container);
	abstract void assignHandler(final String id);
	abstract com.google.gwt.xml.client.Element toXml(com.google.gwt.xml.client.Document doc);
}
