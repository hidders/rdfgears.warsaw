package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

public class RGMenuBar extends Composite {
	private RGCanvas canvas;
	private static RGMenuBarUiBinder uiBinder = GWT
			.create(RGMenuBarUiBinder.class);

	interface RGMenuBarUiBinder extends UiBinder<Widget, RGMenuBar> {
	}
	@UiField
	MenuBar menubar;
	
	@UiField MenuItem newWf;
	@UiField MenuItem newWfFromSource;
	@UiField MenuItem saveWf;
	@UiField MenuItem exportWf;
	//@UiField MenuItem devTest;
	@UiField MenuItem saveRunWf;
	@UiField MenuItem showHideNodeId;
	@UiField MenuItem viewLastSavedWfSource;
	@UiField MenuItem viewPortType;
	
	public RGMenuBar(RGCanvas _canvas) {
		canvas = _canvas;
		initWidget(uiBinder.createAndBindUi(this));
		menubar.getElement().addClassName("menuBar-horizontal");
		
		newWf.setCommand(new Command(){
			public void execute() {
				canvas.createNewWorkflow(canvas.createUniqueWorkflowId(), "New-Workflow");
			}
			
		});
		newWfFromSource.setCommand(new Command(){
			public void execute() {
				canvas.createNewWorkflowFromSource();
			}
			
		});
		saveWf.setCommand(new Command(){
			public void execute() {
				canvas.saveWorkflow();
			}
			
		});
		
		saveRunWf.setCommand(new Command(){
			public void execute(){
				canvas.saveAndRun();
			}
		});
		
		showHideNodeId.setCommand(new Command(){
			public void execute(){
				canvas.showHideNodeId();
			}
		});
		
		viewLastSavedWfSource.setCommand(new Command(){
			public void execute() {
				canvas.viewOriginalWorkflowSource(canvas.getActiveWorkflow().getId());
			}
			
		});
		
		exportWf.setCommand(new Command(){
			public void execute() {
				canvas.displayFormattedXml(canvas.getActiveWorkflow().exportToXml());
			}
			
		});
		
		viewPortType.setCommand(new Command(){
			public void execute() {
				canvas.viewActiveWorkflowPortType();
			}
			
		});
		
//		devTest.setCommand(new Command(){
//			public void execute() {
//				canvas.devTest();
//			}
//			
//		});
		
	}
	
	

}
