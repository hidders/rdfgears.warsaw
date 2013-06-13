package com.nl.tudelft.rdfgearsUI.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("RGService")
public interface RGService extends RemoteService{
	public String getNode(String nType);
	public String getListItems(String source);
	public String getOperatorList();
	public String getFunctionList();
	public String getWorkflowList();
	public String getWorkflowById(String wfId);
	public String saveWorkflow(String filename, String name, String content);
	public String saveAsNewWorkflow(String filename, String name, String content);
	public String formatXml(String rawXml);
	public String doCopyWorkflowFile(String wfId, String newId, String newName, String newDesc, String newCat);
	public String deleteWorkflow(String wfId);
	public String getConfig(String confKey);
}
