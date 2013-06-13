package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.query.client.css.CSS;
import com.google.gwt.query.client.css.Length;
import com.google.gwt.query.client.css.UriValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class RGNavigationPanel extends Composite {

	private static RGNavigationPanelUiBinder uiBinder = GWT.create(RGNavigationPanelUiBinder.class);
	interface RGNavigationPanelUiBinder extends UiBinder<Widget, RGNavigationPanel> {}
	
	private String panelId = "navigation-panel";
	private boolean isVisible = true;
	private RGWorkflowsPanel workflowsPanel;
	private RGCanvas canvas = null;
	
	public RGNavigationPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Label slider;
	@UiField
	HTMLPanel content;
//	@UiField
//	TextBox searchBox;
	
	public RGNavigationPanel(String id, RGCanvas _canvas) {
		canvas = _canvas;
		initWidget(uiBinder.createAndBindUi(this));
		panelId = id;
		getWidget().getElement().setId(id);
		slider.getElement().setId("npSlider");
		workflowsPanel = new RGWorkflowsPanel(canvas);
		content.getElement().setId("navPanelContent");
		content.add(workflowsPanel);
	}
	
	@UiHandler("slider")
	void handleClickOnSlider(ClickEvent e){
		if(isVisible){
			$("#" + panelId).animate("left: '-=240px'", 10); //later change to refer to (current width - 10)
			$("#npSlider").css(CSS.RIGHT.with(Length.px(-25)));
			$("#npSlider").css(CSS.TOP.with(Length.px(0)));
			$("#npSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/right-grey.png")));
			isVisible = false;
		}else{
			$("#" + panelId).animate("left: '+=240px'", 10);
			$("#npSlider").css(CSS.RIGHT.with(Length.px(5)));
			$("#npSlider").css(CSS.TOP.with(Length.px(5)));
			$("#npSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/left-grey.png")));
			isVisible = true;
		}
	}
	@UiHandler("slider")
	void handleMouseOverOnSlider(MouseOverEvent e){
		//$("#npSlider").css(CSS.BACKGROUND_COLOR.with(RGBColor.GREY));
		if(isVisible)
			$("#npSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/left-black.png")));
		else
			$("#npSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/right-black.png")));
	}
	
	@UiHandler("slider")
	void handleMouseOutOnSlider(MouseOutEvent e){
//		$("#npSlider").css(CSS.BACKGROUND_COLOR.with(RGBColor.WHITE));
		if(isVisible)
			$("#npSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/left-grey.png")));
		else
			$("#npSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/right-grey.png")));
	}
	
	public void refreshWorkflowList(){
		workflowsPanel.refreshWorkflowList();
	}
	public ArrayList <String> getWorkflowCategories(){
		return workflowsPanel.getWorkfowCategories();
	}
	public boolean isFinishLoading(){
		return workflowsPanel.isFinishLoading();
	}
	
	public void handleWindowResizeEvent(){
		workflowsPanel.handleWindowResizeEvent();
	}
	
	public int getRightEdgePos(){
		//int width = Integer.parseInt(getElement().getStyle().getWidth().replace("px", ""));
		int width = $("#" + panelId).width();
		int posLeft = $("#" + panelId).left();
		
		return width + posLeft + 3;
	}
	
//	@UiHandler("searchBox")
//	void handleSearchBoxChange(KeyUpEvent e){
//		if(searchBox.getValue().length() > 0)
//			workflowsPanel.setVisible(false);
//		else
//			workflowsPanel.setVisible(true);
//	}
}
