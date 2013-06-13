package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;

import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;
import com.nl.tudelft.rdfgearsUI.client.RGServiceAsync;
import com.nl.tudelft.rdfgearsUI.client.RGType;
import com.nl.tudelft.rdfgearsUI.client.RGTypeUtils;


public class RGFunctionParamList extends RGFunctionParam {
	RGServiceAsync RService = null;
	String value = null, source;
	Node owner;
	String formId;
	String groupId; // the entry holder id in node
	private boolean isLoadingItem = false;
	private Element loadingAnimation;
	
	private ArrayList <String> itemIds = new ArrayList <String>();
	private Map <String, RGFunctionParamListItem> itemMap = new HashMap<String, RGFunctionParamListItem>();
	
	private ArrayList <String> entryIds = new ArrayList <String>();
	
	public RGFunctionParamList(String id, 
									String val, 
									String label,  
									com.google.gwt.xml.client.Element embeddedXmlSource, String gId, Node n) {
		super(id, label);
		value = val;
		owner = n;
		formId = owner.canvas.createUniqueId();
		groupId = gId;
//		if(source != null){
//			isLoadingItem = true;
//			source = optionSource;
//			RService = owner.canvas.getRemoteService();
//			RService.getListItems(source, new AsyncCallback <String>(){
//
//				public void onFailure(Throwable arg0) {
//					Log.error("RPC Failed");
//				}
//
//				public void onSuccess(String items) {
//					Log.debug("I have to parse this value: " + items);
//					Document itemsDom = XMLParser.parse(items);
//					NodeList options = itemsDom.getElementsByTagName("option");
//					parseListItems(options);
//				}
//				  
//			  });
//		}else{		//options embedded
			//Log.debug("embedded option:" + embeddedXmlSource.toString());
			NodeList options = embeddedXmlSource.getElementsByTagName("option");
			parseListItems(options);
		//}
		if(val != null){
			if(val.length() > 0){
				updateNodeBySelectedItem(val);
			}
		}
	}
	
	void parseListItems(NodeList items){
		com.google.gwt.xml.client.Element item, inputData, typeDef;
		RGType t;
		for(int i = 0; i < items.getLength(); i++){
			item = (com.google.gwt.xml.client.Element) items.item(i);
			
			String itemValue = item.getAttribute("value");
			String itemLabel = item.getAttribute("label");
			RGFunctionParamListItem listItem = new RGFunctionParamListItem(
												i,
												itemValue,
												itemLabel);
			if(item.hasChildNodes()){//input port <data ..> inside the item
				NodeList inputs = item.getElementsByTagName("data");
				for(int j = 0; j < inputs.getLength(); j++){
					inputData = (com.google.gwt.xml.client.Element) inputs.item(j);
					boolean iterate = (inputData.getAttribute("iterate").equalsIgnoreCase("false"))? false:true;
					
					if(inputData.hasChildNodes()){
						typeDef = (com.google.gwt.xml.client.Element) inputData.getElementsByTagName("type").item(0);
						if(typeDef != null){
							t = new RGType(owner.canvas.getTypeChecker().rename(RGTypeUtils.unwrap(typeDef), getId()));
						}else{
							t = new RGType("<var name=\""+ owner.canvas.getTypeChecker().createUniqueTypeName()+"\"/>");
						}
					}else{
						t = new RGType("<var name=\""+ owner.canvas.getTypeChecker().createUniqueTypeName()+"\"/>");
					}
					
					listItem.addInputData(inputData.getAttribute("name"), 
										  inputData.getAttribute("label"),
										  t,
										  iterate);
				}
			}
			itemMap.put(itemValue, listItem);
			itemIds.add(itemValue);
		}
		if(isLoadingItem){
			isLoadingItem = false;
			ListBox lb = buildListForm();
			loadingAnimation.removeFromParent();
			formContainer.appendChild(lb.getElement());
		}
	}
	
	ListBox buildListForm(){
		ListBox lb = new ListBox();
		lb.getElement().setId(formId);
		RGFunctionParamListItem item;
		int i = 1;
		lb.addItem("Select...","novalue");
		for(String itemId: itemIds){
			item = itemMap.get(itemId);
			lb.addItem(item.label, item.value);
			if(value != null){
				if(item.value.equalsIgnoreCase(value)){
					lb.setSelectedIndex(i);
				}
			}
			i++;
		}
		if(value == null){
			lb.setSelectedIndex(0);
		}
		
		return lb;
	}
	
	@Override
	void display(Element container) {
		if(elementCache == null){
			initDisplayElement();			
			
			if(isLoadingItem){
				loadingAnimation = DOM.createDiv();
				loadingAnimation.setAttribute("style", "width:100%; text-align:center;padding-top:4px;");
				loadingAnimation.setInnerHTML("<img src=\"images/loader.gif\">");
				formContainer.appendChild(loadingAnimation);
			}else{
				
				ListBox lb = buildListForm();
				lb.getElement().setClassName("propertyParamList");
			
				formContainer.appendChild(lb.getElement());
			}
			
			if(!desc.equals("")){
				Element descContainer = DOM.createDiv();
				descContainer.setClassName("paramFormHelpText");
				descContainer.setInnerText(desc);
				elementCache.appendChild(descContainer);
			}
			
			container.appendChild(elementCache);
			assignHandler(formId);
		}else{
			container.appendChild(elementCache);
		}
	}
	
	void updateNodeBySelectedItem(String itemKey){
		for(String entryId: entryIds){
			owner.removeEntry(entryId);
		}
		entryIds.clear();
		owner.changeGroupHeaderText(groupId, itemMap.get(itemKey).label);
		RGFunctionParamListItem selectedItem = itemMap.get(itemKey);
		for(int i = 0; i < selectedItem.getInputNum(); i++){
			RGFunctionParamListInputData inputData = selectedItem.getInputDataByIdx(i);
			String newEntryId = owner.canvas.createUniqueId();
			entryIds.add(newEntryId);
			
			owner.addInputEntry(newEntryId, inputData.name, inputData.type, inputData.label, inputData.iterate, groupId);
		}
	}
	
	@Override
	void assignHandler(final String id) {
	 $("#" + id).change(new Function(){
		 @Override
		 public void f(){
			value = $("#"+ id).val();
			Log.debug("selected value: " + $("#"+ id).val());
			updateNodeBySelectedItem(value);
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
		if(value != null){
			if(value.length() > 0){
				var.appendChild(doc.createTextNode(value));
			}
		}
		return var;
	}

}

