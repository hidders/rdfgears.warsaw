package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;

public class RGFunctionParamText extends RGFunctionParam{

	public String pValue = "", inputFormId;
	public Element inputForm;
	Node owner;
	
	public RGFunctionParamText(String id, String value, String label, Node n) {
		super(id, label);
		pType = RGFunctionParamType.TEXT;
		owner = n;
		pValue = value;
	}

	@Override
	void display(Element container) {
		if(elementCache == null){
			initDisplayElement();
			Element inputForm = DOM.createTextArea();
			inputForm.setInnerText(pValue);
			inputFormId = owner.canvas.createUniqueId();
			inputForm.setClassName("queryText");
			inputForm.setId(inputFormId);
			formContainer.appendChild(inputForm);
			
//			Log.debug("desc content: " + desc);
			if(!desc.equals("")){
				Element descContainer = DOM.createDiv();
				descContainer.setClassName("paramFormHelpText");
				descContainer.setInnerText(desc);
				elementCache.appendChild(descContainer);
			}
			
			container.appendChild(elementCache);
			
			assignHandler(inputFormId);
		
		}else {
//			Log.debug("display property from cache");
			container.appendChild(elementCache);
			$("#" + inputFormId).text(pValue);
//			Log.debug("pValue : " + pValue);
		}
		
	}

	@Override
	void assignHandler(final String id) {
		$("#" + id).blur(new Function(){
			@Override
			public void f(){
				pValue = $("#" + id).val();
//				Log.debug("set pValue to : " + pValue);
			}
		});
	}
	
	void setValue(String s){
		pValue = s;
		pValue = pValue.replace("&lt;", "<");
		pValue = pValue.replace("&gt;", ">");
		pValue = pValue.replace("&amp;", "&");
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
				//String v = pValue.replace("<", "&lt;");
				//v = v.replace(">", "&gt;");
				var.appendChild(doc.createTextNode(pValue));
			}
		}
		return var;
	}

}
