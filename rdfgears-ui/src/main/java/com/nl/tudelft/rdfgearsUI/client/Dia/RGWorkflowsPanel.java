package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import java.util.ArrayList;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.query.client.Function;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.nl.tudelft.rdfgearsUI.client.RGServiceAsync;

public class RGWorkflowsPanel extends Composite {

	private static RGWorkflowsPanelUiBinder uiBinder = GWT
			.create(RGWorkflowsPanelUiBinder.class);

	interface RGWorkflowsPanelUiBinder extends
			UiBinder<Widget, RGWorkflowsPanel> {
	}
	
	interface PanelStyle extends CssResource {
		String tabHeaderNormal();
		String tabHeaderSelected();
		String panelHidden();
		String panelVisible();
	}
	
	@UiField
	Label workflowsTab;
	@UiField
	Label operatorsTab;
	@UiField
	Label functionsTab;
	@UiField
	HTMLPanel content;
	@UiField
	PanelStyle style;
	
	private String selectedTabId;
	private Label selectedTab;
	private RGCanvas canvas = null;
	private Element operatorListHolder = null;
	private Element functionListHolder = null;
	private Element workflowListHolder = null;
	private Element workflowTabContentHolder = null;
	private Element searchBoxContainer;
	private ArrayList <WorkflowListCategory> wfListCategoryArr;
	private boolean isWfListFiltered = false;
	private ArrayList <String> workflowCategories = new ArrayList <String>();
//	private TextBox searchBox = null;
	private Element searchBox = null;
	String searchBoxId = "wfSearchBox";
	private boolean workflowListLoaded = false;
	
	RGServiceAsync RGService = null;
	
	public RGWorkflowsPanel(RGCanvas _canvas) {
		canvas = _canvas;
		RGService = canvas.getRemoteService();
		
		initWidget(uiBinder.createAndBindUi(this));
		getWidget().getElement().setId("workflowsPanel");
		getWidget().getElement().setClassName(style.panelVisible());
		
		workflowsTab.getElement().setId("wfTab");
		workflowsTab.getElement().addClassName(style.tabHeaderSelected());
		selectedTabId = "wfTab";
		selectedTab = workflowsTab;
		
		operatorsTab.getElement().setId("opTab");
		operatorsTab.getElement().addClassName(style.tabHeaderNormal());
		
		functionsTab.getElement().setId("functTab");
		functionsTab.getElement().addClassName(style.tabHeaderNormal());
		
		content.getElement().setId("wfListContent");
		showWorkflowsTabContent();
	}
	
	@UiHandler("workflowsTab")
	void handleClickOnWorkflowsTab(ClickEvent e){
		if(!selectedTabId.equals("wfTab")){
			workflowsTab.addStyleName(style.tabHeaderSelected());
			selectedTab.getElement().addClassName(style.tabHeaderNormal());
			selectedTab.getElement().removeClassName(style.tabHeaderSelected());
			workflowsTab.removeStyleName(style.tabHeaderNormal());
			selectedTabId = "wfTab";
			selectedTab = workflowsTab;
			
			showWorkflowsTabContent();
		}
	}
	
	@UiHandler("operatorsTab")
	void handleClickOnOperatorsTab(ClickEvent e){
		if(!selectedTabId.equals("opTab")){
			operatorsTab.addStyleName(style.tabHeaderSelected());
			selectedTab.getElement().addClassName(style.tabHeaderNormal());
			selectedTab.getElement().removeClassName(style.tabHeaderSelected());
			operatorsTab.removeStyleName(style.tabHeaderNormal());
			selectedTabId = "opTab";
			selectedTab = operatorsTab;
			
			showOperatorsTabContent();
		}
	}
	
	@UiHandler("functionsTab")
	void handleClickOnFunctionsTab(ClickEvent e){
		if(!selectedTabId.equals("functTab")){
			functionsTab.addStyleName(style.tabHeaderSelected());
			selectedTab.getElement().addClassName(style.tabHeaderNormal());
			selectedTab.getElement().removeClassName(style.tabHeaderSelected());
			functionsTab.removeStyleName(style.tabHeaderNormal());
			selectedTabId = "functTab";
			selectedTab = functionsTab;
			
			showFunctionsTabContent();
		}
	}
	
	public void setVisible(boolean v){
		if(v){
			getWidget().getElement().setClassName(style.panelVisible());
		}else{
			getWidget().getElement().setClassName(style.panelHidden());
		}
	}
	
	public void showFunctionsTabContent(){
		if(functionListHolder == null){
			content.getElement().setInnerText("");
			content.getElement().appendChild(createLoadingAnimation());
			functionListHolder = DOM.createDiv();
			RGService.getFunctionList(new AsyncCallback <String> (){
				public void onFailure(Throwable arg0) {
					Log.debug("RPC Failed");
				}

				public void onSuccess(String arg0) {
//					parseAndBuildFunctionList(arg0);
					parseAndBuildProcessorList(functionListHolder, arg0);
					functionListHolder.setAttribute("style", "position:relative; overflow:auto;" +
							 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 40 ) + "px;");
				}
			});
		}else{
			content.getElement().setInnerText("");
			content.getElement().appendChild(functionListHolder);
			functionListHolder.setAttribute("style", "position:relative; overflow:auto;" +
					 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 40 ) + "px;");
		}
	}
	
	public void showOperatorsTabContent(){
//		Element operatorList = DOM.createElement("ul");
		if(operatorListHolder == null){
			content.getElement().setInnerText("");
			content.getElement().appendChild(createLoadingAnimation());
			operatorListHolder = DOM.createDiv();
			RGService.getOperatorList(new AsyncCallback <String> (){
				public void onFailure(Throwable arg0) {
					Log.debug("RPC Failed");
				}

				public void onSuccess(String arg0) {
					//parseAndBuildOperatorList(arg0);
					parseAndBuildProcessorList(operatorListHolder, arg0);
					operatorListHolder.setAttribute("style", "position:relative; overflow:auto;" +
							 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 40 ) + "px;");
				}
			});
		}else{
			content.getElement().setInnerText("");
			content.getElement().appendChild(operatorListHolder);
			operatorListHolder.setAttribute("style", "position:relative; overflow:auto;" +
					 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 40 ) + "px;");
		}
	}
	
	public void parseAndBuildProcessorList(Element listHolder, String listXml){
		ArrayList <WorkflowListCategory> categoryArr = new ArrayList <WorkflowListCategory>();
		WorkflowListCategory noCategory = new WorkflowListCategory(canvas.createUniqueId(), "Uncategorized");
		
		com.google.gwt.xml.client.Element cat, item;
		
		Document operators = XMLParser.parse(listXml);
//		operators.normalize(); //raise error on IE
		NodeList categories = (operators.getElementsByTagName("operators").item(0)).getChildNodes();
		//Log.debug(listXml.toString());
		for(int i = 0; i < categories.getLength(); i++){
			try{
				cat = (com.google.gwt.xml.client.Element) categories.item(i);
				if(cat.getTagName().equalsIgnoreCase("category")){
					String catName = cat.getAttribute("name");
					WorkflowListCategory c = new WorkflowListCategory(canvas.createUniqueId(), cat.getAttribute("name"));
					NodeList catItems = cat.getElementsByTagName("item");
					for(int j = 0; j < catItems.getLength(); j++){
						item = (com.google.gwt.xml.client.Element) catItems.item(j);
						c.addItem(new OperatorListItem(item.getAttribute("id"), item.getAttribute("name"), canvas));
						//c.addItem(new WorkflowListItem(catName + "_2F_" +item.getAttribute("id"), item.getAttribute("name"), canvas));
					}
					categoryArr.add(c);
					listHolder.appendChild(c.getContentElement());
				}else if(cat.getTagName().equalsIgnoreCase("item")){
					noCategory.addItem(new OperatorListItem(cat.getAttribute("id"), cat.getAttribute("name"), canvas));
				}
			}catch (Exception e){};
		}
		if(noCategory.numOfItem() > 0)
			listHolder.appendChild(noCategory.getContentElement());
		
		content.getElement().setInnerText("");
		content.getElement().appendChild(listHolder);
		categoryArr.add(noCategory);
		for(int i = 0; i < categoryArr.size(); i++){
			categoryArr.get(i).enableHandlers();
		}
//		if(noCategory.numOfItem() > 0)
//			noCategory.enableHandlers();
	}
	public void refreshWorkflowList(){
		workflowTabContentHolder = null;
		if(selectedTabId.equals("wfTab")){
			showWorkflowsTabContent();
		}
	}
	public void showWorkflowsTabContent(){
		if(workflowTabContentHolder == null){
			content.getElement().setInnerText("");
			content.getElement().appendChild(createLoadingAnimation());
			workflowTabContentHolder = DOM.createDiv();
			searchBoxContainer = DOM.createDiv();
			searchBox = DOM.createInputText();
			searchBox.setId(searchBoxId);
			searchBox.setClassName("wfSearchInputBox");
			searchBoxContainer.appendChild(searchBox);
//			searchBox = new TextBox();
//			searchBox.getElement().setId(searchBoxId);
//			searchBox.getElement().setClassName("wfSearchInputBox");
//			searchBoxContainer.appendChild(searchBox.getElement());
			
			searchBoxContainer.setClassName("wfSearchContainer");
			Element searchIcon = DOM.createDiv();
			//searchIcon.setInnerHTML("<img src=\"images/search-grey.png\" class=\"searchIcon\"/>");
			Element iconImg = DOM.createImg();
			iconImg.setAttribute("src", "images/search-grey.png");
			iconImg.setClassName("searchIcon");
			searchIcon.appendChild(iconImg);
			searchIcon.setClassName("searchIconContainer");
			searchBoxContainer.appendChild(searchIcon);
			workflowTabContentHolder.appendChild(searchBoxContainer);
			
			workflowListHolder = DOM.createDiv();
			
			RGService.getWorkflowList(new AsyncCallback <String> (){
				public void onFailure(Throwable arg0) {
					Log.debug("RPC Failed");
				}
				
				public void onSuccess(String arg0) {
					parseAndBuildWorkflowList(arg0);
					workflowListHolder.setAttribute("style", "position:relative; overflow:auto;" +
							 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 70 ) + "px;");
					workflowListLoaded = true;
				}
			});
		}else{
			content.getElement().setInnerText("");
			content.getElement().appendChild(workflowTabContentHolder);
			workflowListHolder.setAttribute("style", "position:relative; overflow:auto;" +
					 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 70 ) + "px;");
		}
	}
	
	private void enableSearchBoxEventHandler(){
		$("#" + searchBoxId).change(new Function(){
			@Override
			public void f(){
				String keyword = $("#" + searchBoxId).val().trim();
				Log.debug("change detected:" + keyword);
				if(keyword.isEmpty()){
					clearWorkflowListFilter();
				}else{
					filterWorkflowList(keyword);
				}
			}
		});
	}
	private void filterWorkflowList(String keyword){
		String [] keywords = keyword.split(" ");
		for(WorkflowListCategory wl: wfListCategoryArr){
			wl.filter(keywords);
		}
	}
	
	private void clearWorkflowListFilter(){
		for(WorkflowListCategory wl: wfListCategoryArr){
			wl.clearFilter();
		}
	}
	public void parseAndBuildWorkflowList(String wfInXml){
		workflowCategories.clear();
		wfListCategoryArr = new ArrayList <WorkflowListCategory>();
		WorkflowListCategory noCategory = new WorkflowListCategory("unCatWorkflows", "Uncategorized");
		
		com.google.gwt.xml.client.Element cat, item, desc;
		
		Document workflows = XMLParser.parse(wfInXml);
//		workflows.normalize(); //raise an error on IE
		NodeList categories = (workflows.getElementsByTagName("workflows").item(0)).getChildNodes();
		//Log.debug(wfInXml.toString());
		for(int i = 0; i < categories.getLength(); i++){
			try{
				//Log.debug(categories.item(i).getClass().toString());
				cat = (com.google.gwt.xml.client.Element) categories.item(i);
				if(cat.getTagName().equalsIgnoreCase("category")){
					String catName = cat.getAttribute("name");
					workflowCategories.add(catName);
					WorkflowListCategory c = new WorkflowListCategory(canvas.createUniqueId(), catName);
					NodeList catItems = cat.getElementsByTagName("item");
					for(int j = 0; j < catItems.getLength(); j++){
						item = (com.google.gwt.xml.client.Element) catItems.item(j);
						String descText = "";
						if(item.hasChildNodes()){
							desc = (com.google.gwt.xml.client.Element) item.getElementsByTagName("description").item(0);
							if(desc.hasChildNodes()){
								descText = desc.getFirstChild().getNodeValue();
							}
						}
						c.addItem(new WorkflowListItem(item.getAttribute("id"), item.getAttribute("name"), descText, canvas));
						//c.addItem(new WorkflowListItem(catName + "_2F_" +item.getAttribute("id"), item.getAttribute("name"), canvas));
					}
					wfListCategoryArr.add(c);
					workflowListHolder.appendChild(c.getContentElement());
				//	Log.debug("c element:" + c.getElement().toString());
				}else if(cat.getTagName().equalsIgnoreCase("item")){
					String descText = "";
					if(cat.hasChildNodes()){
						desc = (com.google.gwt.xml.client.Element) cat.getElementsByTagName("description").item(0);
						if(desc.hasChildNodes()){
							descText = desc.getFirstChild().getNodeValue();
						}
					}
					noCategory.addItem(new WorkflowListItem(cat.getAttribute("id"), cat.getAttribute("name"), descText, canvas));
				}
			}catch (Exception e){};
		}
		if(noCategory.numOfItem() > 0){
			workflowListHolder.appendChild(noCategory.getContentElement());
			wfListCategoryArr.add(noCategory);
		}
		content.getElement().setInnerText("");
//		Log.debug(workflowListHolder.toString());
		workflowTabContentHolder.appendChild(workflowListHolder);
		content.getElement().appendChild(workflowTabContentHolder);
		enableSearchBoxEventHandler();
		
		for(int i = 0; i < wfListCategoryArr.size(); i++){
			wfListCategoryArr.get(i).enableHandlers();
		}
		
//		if(noCategory.numOfItem() > 0)
//			noCategory.enableHandlers();
	}
	
	public Element createLoadingAnimation(){
		Element loadingAnimation = DOM.createDiv();
		loadingAnimation.setAttribute("style", "width:100%; text-align:center;padding-top:10px;");
		loadingAnimation.setInnerHTML("<img src=\"images/loader.gif\">");
		
		return loadingAnimation;
	}
	
	public ArrayList <String> getWorkfowCategories(){
		return workflowCategories;
	}
	
	public boolean isFinishLoading(){
		return workflowListLoaded;
	}
	
	public void handleWindowResizeEvent(){
		if(selectedTabId.equals("wfTab")){
			workflowListHolder.setAttribute("style", "position:relative; overflow:auto;" +
					 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 70 ) + "px;");
		}else if(selectedTabId.equals("opTab")){
			operatorListHolder.setAttribute("style", "position:relative; overflow:auto;" +
					 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 40 ) + "px;");
		}else if(selectedTabId.equals("funcTab")){
			functionListHolder.setAttribute("style", "position:relative; overflow:auto;" +
					 "height:" + (DOM.getElementById("navPanelContent").getClientHeight() - 40 ) + "px;");
		}
	}
}
