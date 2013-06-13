package com.nl.tudelft.rdfgearsUI.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RGServiceAsync {
	void getNode(String nType, AsyncCallback<String> async);
	void getListItems(String source, AsyncCallback<String> async);
	void getOperatorList(AsyncCallback<String> async);
	void getFunctionList(AsyncCallback<String> async);
	void getWorkflowList(AsyncCallback<String> async);
	void getWorkflowById(String wfId, AsyncCallback<String> async);
	void saveWorkflow(String filename, String name, String content, AsyncCallback<String> async);
	void saveAsNewWorkflow(String filename, String name, String content, AsyncCallback<String> async);
	void formatXml(String rawXml, AsyncCallback<String> async);
	void doCopyWorkflowFile(String wfId, String newId, String newName, String newDesc, String newCat, AsyncCallback<String> async);
	void deleteWorkflow(String wfId, AsyncCallback<String> async);
	void getConfig(String confKey, AsyncCallback<String> async);
}
