package nl.rdfgears.tudelft.webservice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebGearsAction {

	private HttpServletRequest request;
	private HttpServletResponse response;
	private String username; 
	private String workflowId;
	private String action;
	private String[] splitPathInfo; 

	public WebGearsAction(HttpServletRequest request,
			HttpServletResponse response) {
		this.request = request;
		this.response = response;
		
		

		splitPathInfo = request.getPathInfo().split("/");
		try {
			username = splitPathInfo[0];
			action = splitPathInfo[1];
			
			workflowId = ""; 
			for (int i=2; i<splitPathInfo.length; i++){
				workflowId += "/"+splitPathInfo[i]; 
			}
		} catch (ArrayIndexOutOfBoundsException e){
			throw(e); // incorrect request 
		}
	}

	public void execute() {
		// TODO Auto-generated method stub
		
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public String getUsername() {
		return username;
	}


}
