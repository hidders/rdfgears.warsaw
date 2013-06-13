package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class RGPropertyPanel extends Composite {

	private static RGPropertyPanelUiBinder uiBinder = GWT.create(RGPropertyPanelUiBinder.class);

	interface RGPropertyPanelUiBinder extends UiBinder<Widget, RGPropertyPanel> {}
	
	private String panelId = "property-panel";
	private boolean isVisible = true;
	private Node activeNode = null;
	private RGWorkflow activeWorkflow = null;
	
	public RGPropertyPanel() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Label slider;
	@UiField
	FlowPanel propertiesContainer;
	
	public RGPropertyPanel(String id) {
		initWidget(uiBinder.createAndBindUi(this));
		panelId = id;
		getWidget().getElement().setId(id);
		slider.getElement().setId("ppSlider");
		propertiesContainer.getElement().setId("propertiesContainer");
	}
	public void setActiveNode(Node n){
		if(n == null){
			propertiesContainer.clear();
			if(activeWorkflow != null){
				activeWorkflow.displayProperties(propertiesContainer.getElement());
			}
		}else{
			activeNode = n;
			propertiesContainer.clear();
			activeNode.displayProperties(propertiesContainer.getElement());
		}
	}
	
	public void showWorkflowProperty(RGWorkflow w){
		propertiesContainer.clear();
		activeWorkflow = w;
		activeWorkflow.displayProperties(propertiesContainer.getElement());
	}
	
	@UiHandler("slider")
	void handleClickOnSlider(ClickEvent e){
		if(isVisible){
			$("#" + panelId).animate("right: '-=295px'", 10); //later change to refer to (current width - 10)
			$("#ppSlider").css(CSS.LEFT.with(Length.px(-25)));
			$("#ppSlider").css(CSS.TOP.with(Length.px(0)));
			$("#ppSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/left-grey.png")));
			isVisible = false;
		}else{
			$("#" + panelId).animate("right: '+=295px'", 10);
			$("#ppSlider").css(CSS.LEFT.with(Length.px(5)));
			$("#ppSlider").css(CSS.TOP.with(Length.px(5)));
			$("#ppSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/right-grey.png")));
			isVisible = true;
		}
	}
	@UiHandler("slider")
	void handleMouseOverOnSlider(MouseOverEvent e){
		if(isVisible)
			$("#ppSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/right-black.png")));
		else
			$("#ppSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/left-black.png")));
	}
	
	@UiHandler("slider")
	void handleMouseOutOnSlider(MouseOutEvent e){
		if(isVisible)
			$("#ppSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/right-grey.png")));
		else
			$("#ppSlider").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/left-grey.png")));
	}
	
	
	/**
	 * to enable event handler at runtime.
	 * NOTE: must be called after the widget (panel) added to the document.
	 */
	public void enableEventHandlers(){

	}
}
