package nl.rdfgears.tudelft.webservice;

import javax.servlet.http.HttpServletRequest;

public class WorkflowTools {

	
	public static String getWorkflowId(HttpServletRequest request){
		String[] ar = request.getPathInfo().split("/");
		
		if (ar.length<3)
			return null;
		
		StringBuilder sb = new StringBuilder();
		for (int i=2; i<ar.length; i++)
			sb.append(ar[i]);
		
		return sb.toString();
	}
	
	
}
