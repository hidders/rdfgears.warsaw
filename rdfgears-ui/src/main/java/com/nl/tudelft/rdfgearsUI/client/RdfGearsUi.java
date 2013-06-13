package com.nl.tudelft.rdfgearsUI.client;

import com.nl.tudelft.rdfgearsUI.client.Dia.RGCanvas;
import com.nl.tudelft.rdfgearsUI.client.Dia.RGLogger;
import com.nl.tudelft.rdfgearsUI.client.Dia.RGMenuBar;
import com.nl.tudelft.rdfgearsUI.client.Dia.RGNavigationPanel;
import com.nl.tudelft.rdfgearsUI.client.Dia.RGPropertyPanel;
import com.nl.tudelft.rdfgearsUI.client.Dia.RGTabBar;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class RdfGearsUi implements EntryPoint {
  private RGServiceAsync RGService = null;
  private Timer t;
  RGNavigationPanel rpanel;
  RGPropertyPanel ppanel;
  RGLogger debugPanel;
  
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
	  HTMLPanel menuContainer = HTMLPanel.wrap(Document.get().getElementById("menu-container"));
	  final RGCanvas canvas = new RGCanvas("canvas");
	  debugPanel = new RGLogger("debugger-panel");
	  canvas.setRemoteService(getServiceInstance());
	  
	  canvas.setMargin(45, 0);
	  
	  canvas.showAppLoader("Loading application...");
	  
	  RGMenuBar menu = new RGMenuBar(canvas);
	  RGTabBar tabBar = new RGTabBar(canvas);
	  rpanel = new RGNavigationPanel("navigation-panel", canvas);
	  ppanel = new RGPropertyPanel("property-panel");
	  canvas.setTypeChecker(new RGTypeChecker(canvas, debugPanel));
	  
	  canvas.setNavigationPanel(rpanel);
	  canvas.setPropertyPanel(ppanel);
	  canvas.setTabBarPanel(tabBar);
	  
	  HTMLPanel workspace = HTMLPanel.wrap(Document.get().getElementById("workspace"));
	  workspace.add(rpanel);
	  workspace.add(ppanel);
	  workspace.add(debugPanel);
	  //RootPanel.get("workspace").add(rpanel, 0, 0);
	  
	  //RootPanel.get("workspace").add(ppanel);
	  
	  menuContainer.add(menu);
	  menuContainer.add(tabBar);
	  
	  t = new Timer(){

		@Override
		public void run() {
			if(rpanel.isFinishLoading()){
				t.cancel();
				t = null;
				canvas.createNewWorkflow("new-workflow-id", "New Workflow");
				canvas.removeAppLoader();
			}else
				t.schedule(100);
		}
		  
	  };
	  
	  t.schedule(100);
	  debugPanel.minimize();
	  Window.addResizeHandler(new ResizeHandler(){

		public void onResize(ResizeEvent event) {
			canvas.handleWindowResizeEvent();
		}
	  });
	  //get config through RPC service
	  RGService.getConfig("RDFGearsRestUrl", new AsyncCallback <String>(){

		public void onFailure(Throwable arg0) {
			canvas.displayErrorMessage("Cannot connect to server, RPC Failed");
		}

		public void onSuccess(String arg0) {
			canvas.setRdfGearsRestBaseUrl(arg0);
		}
		  
	  });
  }
  
  RGServiceAsync getServiceInstance(){
	  if(RGService == null){
		  RGService = (RGServiceAsync) GWT.create(RGService.class);
		 // ((ServiceDefTarget)RGService).setServiceEntryPoint("RGService");
		  Log.debug("serviceEntryPoint: " + ((ServiceDefTarget)RGService).getServiceEntryPoint());
	  }
	  return RGService;
  }
  
  
}
