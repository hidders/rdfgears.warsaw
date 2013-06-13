package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.xml.client.Document;

import java.util.ArrayList;
import com.allen_sauer.gwt.log.client.Log;

public class RGFunctionParamFields extends RGFunctionParam{
	//TODO: set attribute of minimum num of fields
	private String vals = null;
	private ArrayList <String> fieldFormIds = new ArrayList <String>();
	
	Node owner;
	
	public RGFunctionParamFields(String id, String value, String label, Node n) {
		super(id, label);
		pType = RGFunctionParamType.FIELDS;
		owner = n;
		vals = value;
	}

	@Override
	void display(Element container) {
		if(elementCache == null){
			initDisplayElement();
			
			Element addFieldButton = DOM.createDiv();
			//addFieldButton.setInnerText("+ add input");
			addFieldButton.setInnerHTML("<img src=\"images/add.png\" /> add field");
			String addFieldButtonId = owner.canvas.createUniqueId();
			addFieldButton.setId(addFieldButtonId);
			addFieldButton.setClassName("addFieldButton");
			
			elementCache.appendChild(labelContainer);
			elementCache.appendChild(formContainer);
			elementCache.appendChild(addFieldButton);
			
			
			if(!desc.equals("")){
				Element descContainer = DOM.createDiv();
				descContainer.setClassName("paramFormHelpText");
				descContainer.setInnerText(desc);
				elementCache.appendChild(descContainer);
			}
			
			container.appendChild(elementCache);
			if(vals != null)
				if(vals.contains(";")){
					String[] vs = vals.split(";");
					for(String v: vs){
						addField(v);
					}
				}else{
					addField("");
				}
			
			assignAddButtonHandler(addFieldButtonId);
		}else{
			container.appendChild(elementCache);
		}
		
	}
	
	String addField(String value){
		String id = owner.canvas.createUniqueId();
		String holderId = owner.canvas.createUniqueId();
		String dbuttonId = owner.canvas.createUniqueId();
		
		Element holder = DOM.createDiv();
		holder.setId(holderId);
		Element deleteButton = DOM.createDiv();
		//deleteButton.setInnerText("X");
		deleteButton.setInnerHTML("<img src=\"images/del-white.png\"/>");
		deleteButton.setId(dbuttonId);
		deleteButton.setAttribute("style", "display:inline;margin-left:5px;");
		
		Element t = DOM.createInputText();
		t.setAttribute("value",value);
		t.setClassName("inputString");
		t.setId(id);
		fieldFormIds.add(id);
		
		holder.appendChild(t);
		holder.appendChild(deleteButton);
		
		if(formContainer != null){
			formContainer.appendChild(holder);
			t.focus();
			assignHandler(id);
			assignDelButtonHandler(dbuttonId, holderId, id);
			
		}else{
			Log.debug("formContainer NULL !!");
		}
		
		return id;
	}
	
	void removeField(String id){
		
	}
	
	void assignDelButtonHandler(String buttonId, final String holderId, final String formId){
		$("#" + buttonId).click(new Function(){
			@Override
			public void f(){
				removeField(holderId);
				fieldFormIds.remove(formId);
				$("#" + holderId).remove();
				collectFieldVals();
				
//				Log.debug("field removed with id:" + formId);
//				Log.debug("ids:" + fieldFormIds.toString());
			}
		});
	}
	
	void collectFieldVals(){
		vals = "";
		
		for(int i = 0; i < fieldFormIds.size(); i++){
			String fId = fieldFormIds.get(i);
			vals = vals + $("#" + fId).val() + ";";
		}
//		Log.debug("collected vals:" + vals);
	}
	
	void assignAddButtonHandler(String id){
		$("#" + id).click(new Function(){
			@Override
			public void f(){
				addField("");
			}
		});
		
//		$("#" + id).mouseover(new Function(){
//			@Override
//			public void f(){
//				$("#" + id).html("<img src=\"images/del-red.png\"/>");
//			}
//		});
//		$("#" + id).mouseout(new Function(){
//			@Override
//			public void f(){
//				$("#" + id).html("<img src=\"images/del-white.png\"/>");
//			}
//		});
	}
	
	@Override
	void assignHandler(String id) {
		$("#" + id).blur(new Function(){
			@Override
			public void f(){
				collectFieldVals();
			}
		});
		
	}

	@Override
	void setValueFromString(String s) {
		
	}

	@Override
	com.google.gwt.xml.client.Element toXml(Document doc) {
		com.google.gwt.xml.client.Element var = doc.createElement("config");
		var.setAttribute("param", getId());
		if(vals != null){
			if(vals.length() > 0){
				var.appendChild(doc.createTextNode(vals));
			}
		}
		return var;
	}

}
