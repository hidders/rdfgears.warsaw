package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;
import java.util.ArrayList;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.css.CSS;
import com.google.gwt.user.client.DOM;

public class WorkflowListCategory {
	ArrayList <NavigationListItem> items = new ArrayList <NavigationListItem>();
	ArrayList <NavigationListItem> hiddenItems = new ArrayList <NavigationListItem>();
	Element root, arrow, label, itemHolder;
	String labelId;
	boolean visible = true;
	String name;
	public WorkflowListCategory(String id, String _name){
		labelId = id;
		name = _name;
		root = DOM.createDiv();
		root.setClassName("navListCategory");
		arrow = DOM.createDiv();
		arrow.setId("arrow-" + id);
		arrow.setClassName("catArrowExpanded");
		root.appendChild(arrow);
		label = DOM.createDiv();
		label.setId(id);
		label.setInnerText(name);
		label.setClassName("listCatLabel");
		root.appendChild(label);
		itemHolder = DOM.createDiv();
		itemHolder.setId(id+"-items");
		itemHolder.setAttribute("style", "padding-left: 5px;");
		root.appendChild(itemHolder);
	}
	public void addItem(NavigationListItem item){
		items.add(item);
		itemHolder.appendChild(item.getElement());
	}
	
	public Element getContentElement(){
		return root;
	}
	public int numOfItem(){
		return items.size();
	}
	public void filter(String[] keyWords){
		for(NavigationListItem it: items){
			for(int i = 0; i < keyWords.length; i++){
				boolean inId = it.getId().toLowerCase().contains(keyWords[i].toLowerCase());
				boolean inName = it.getName().toLowerCase().contains(keyWords[i].toLowerCase());
				boolean inDesc = it.getDesc().toLowerCase().contains(keyWords[i].toLowerCase());

				if(!inId && !inName && !inDesc){
					if(!hiddenItems.contains(it)){
						it.setVisible(false);
						hiddenItems.add(it);
					}
				}else{
					it.setVisible(true);
					hiddenItems.remove(it);
				}
			}
		}
		
		if(hiddenItems.size() == items.size()){
			setVisible(false);
		}else{
			setVisible(true);
		}
	}
	
	public void clearFilter(){
		setVisible(true);
		for(NavigationListItem it: hiddenItems){
			it.setVisible(true);
		}
		hiddenItems.clear();
	}
	private void setVisible(boolean v){
		if(!v)
			root.addClassName("hidden");
		else
			root.removeClassName("hidden");
	}
	public void enableHandlers(){
		$("#" + labelId).click(new Function(){
			public void f(){
				if(visible){
					$("#"+labelId+"-items").css(CSS.DISPLAY.with(Display.NONE));
					arrow.addClassName("catArrowCollapsed");
					visible = false;
				}else{
					$("#"+labelId+"-items").css(CSS.DISPLAY.with(Display.BLOCK));
					arrow.removeClassName("catArrowCollapsed");
					visible = true;
				}
			}
		});
		for(NavigationListItem item: items){
			item.enableEventHandler();
		}
	}
}
