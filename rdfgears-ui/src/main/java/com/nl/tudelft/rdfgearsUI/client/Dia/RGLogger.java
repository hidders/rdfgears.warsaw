package com.nl.tudelft.rdfgearsUI.client.Dia;

import static com.google.gwt.query.client.GQuery.$;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.query.client.css.CSS;
import com.google.gwt.query.client.css.UriValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class RGLogger extends Composite{
	private boolean isMinimized = false;
	private boolean isMaximized = false;
	private boolean isNormal = false;
	private int height = 250, left, right;
	private int headerHeight = 25;
	@UiField 
	Label minButton;
	@UiField
	HTMLPanel content;
	
	private static RGLoggerUiBinder uiBinder = GWT
			.create(RGLoggerUiBinder.class);

	interface RGLoggerUiBinder extends UiBinder<Widget, RGLogger> {
	}

	public RGLogger(String id) {
		initWidget(uiBinder.createAndBindUi(this));
		getWidget().getElement().setId(id);
		minButton.getElement().setId("loggerMinButton");
	}
	
	@UiHandler("minButton")
	void handleClickOnMinButton(ClickEvent e){
		if(!isMinimized)
			minimize();
		else
			restore();
	}
	
	@UiHandler("minButton")
	void handleMouseOverOnMinButton(MouseOverEvent e){
		if(isMinimized)
			$("#loggerMinButton").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/up-black.png")));
		else
			$("#loggerMinButton").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/down-black.png")));
	}
	
	@UiHandler("minButton")
	void handleMouseOutOnMinButton(MouseOutEvent e){
		if(isMinimized)
			$("#loggerMinButton").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/up-grey.png")));
		else
			$("#loggerMinButton").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/down-grey.png")));
	}
	
	public void setHeight(int h){
		height = h;
		getElement().getStyle().setHeight(height, Unit.PX);
	}
	
	public void setLeft(int l){
		left = l;
		getElement().getStyle().setLeft(left, Unit.PX);
	}
	
	public void setRight(int r){
		right = r;
		getElement().getStyle().setRight(right, Unit.PX);
	}
	
	public void minimize(){
		if(!isMinimized){
			isMinimized = true;
			isMaximized = false;
			isNormal = false;
			getElement().getStyle().setHeight(headerHeight, Unit.PX);
//			minButton.getElement().getStyle().setBackgroundImage("url('images/up-grey.png')");
			$("#loggerMinButton").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/up-grey.png")));
		}
	}
	
	public void maximized(){
		if(!isMaximized){
			isMinimized = false;
			isMaximized = true;
			isNormal = false;
		}
	}
	
	public void restore(){
		if(!isNormal){
			isMinimized = false;
			isMaximized = false;
			isNormal = true;
			getElement().getStyle().setHeight(height, Unit.PX);
//			minButton.getElement().getStyle().setBackgroundImage("url('images/down-grey.png')");
			$("#loggerMinButton").css(CSS.BACKGROUND_IMAGE.with(UriValue.url("images/down-grey.png")));
		}
	}
	
	public void error(String msg){
		Element msgEl = DOM.createDiv();
		msgEl.setInnerText(msg);
		msgEl.setClassName("logError");
		content.getElement().appendChild(msgEl);
	}
	
	public void debug(String msg){
		Element msgEl = DOM.createDiv();
		msgEl.setInnerText(msg);
		msgEl.setClassName("logDebug");
		content.getElement().appendChild(msgEl);
	}
	
	public void info(String msg){
		Element msgEl = DOM.createDiv();
		msgEl.setInnerText(msg);
		msgEl.setClassName("logInfo");
		content.getElement().appendChild(msgEl);
	}
	
	public void clear(){
		content.getElement().setInnerText("");
	}
	
	public void display(ArrayList <String> messages){
		if(messages != null){
			for(String msg: messages){
				if(msg.startsWith("[ERROR]"))
					error(msg);
				else if(msg.startsWith("[DEBUG]"))
					debug(msg);
				else
					info(msg);
			}
		}
	}

}
