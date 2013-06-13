package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;

public class RGFunctionParamString extends RGFunctionParam {
	public String pValue = "", inputFormId;
	Node owner;
	
	public RGFunctionParamString(String id, String value, String label, Node n) {
		super(id, label);
		pType = RGFunctionParamType.STRING;
		owner = n;
		pValue = value;
	}

	@Override
	void display(Element container) {
		if(elementCache == null){
			initDisplayElement();
			
			Element t = DOM.createInputText();
			t.setAttribute("value", pValue);
			t.setClassName("inputString");
			inputFormId = owner.canvas.createUniqueId();
			t.setId(inputFormId);
			formContainer.appendChild(t);
			
			if(!desc.equals("")){
				Element descContainer = DOM.createDiv();
				descContainer.setClassName("paramFormHelpText");
				descContainer.setInnerText(desc);
				elementCache.appendChild(descContainer);
			}
			
			container.appendChild(elementCache);
			
			assignHandler(inputFormId);
		}else {
			container.appendChild(elementCache);
			$("#" + inputFormId).attr("value", pValue);
//			Log.debug("display property from cache");
		}
		
	}

	@Override
	void assignHandler(final String id) {
		$("#" + id).blur(new Function(){
			@Override
			public void f(){
				pValue = $("#" + id).val();
				Log.debug("set pValue to : " + pValue);
				$("#" + id).attr("value", pValue);
			}
		});
	}
	
	void setValue(String s){
		pValue = s;
	}

	@Override
	void setValueFromString(String s) {
		pValue = s;
		
	}

	@Override
	com.google.gwt.xml.client.Element toXml(Document doc) {
		com.google.gwt.xml.client.Element var = doc.createElement("config");
		var.setAttribute("param", getId());
		if(pValue != null){
			if(pValue.length() > 0){
				var.appendChild(doc.createTextNode(pValue));
			}
		}
		return var;
	}
}
