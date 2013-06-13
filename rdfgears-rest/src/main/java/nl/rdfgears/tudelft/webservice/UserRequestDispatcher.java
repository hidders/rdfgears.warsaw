package nl.rdfgears.tudelft.webservice;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MyRequestDispatcher
 */
public class UserRequestDispatcher extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserRequestDispatcher() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
//    @WebServlet(name="HelloServlet1", urlPatterns={"/HelloServlet1"})
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pathInfo = request.getPathInfo();
		if (pathInfo==null){
			response.getWriter().print("Not a correct request: "+request.getRequestURI());
			return;
		}
		
		String[] splitPathInfo = request.getPathInfo().split("/"); /* splitPathInfo[0]=="", as path starts with '/' */
		
		if (splitPathInfo.length==0){ // pathInfo was "/", last hit was omitted
			response.getWriter().print("You must specify an action");
			
			return;	
		}
		/* ok. */
		
//		/* get username */
//		request.setAttribute("rdfgears.username", splitPathInfo[0]);
//		
		
		
		/* get action */ 
		String action = splitPathInfo[1]; 
		request.setAttribute("rdfgears.action", action);
		
		String workflowId = "";
		for (int i=2; i<splitPathInfo.length; i++){
			workflowId += "/"+splitPathInfo[i];
		}
		request.setAttribute("rdfgears.workflowId", workflowId); 
		
		// forget the whole path, all given data is now assumed to be set as attributes. 
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+action); 
		if (dispatcher!=null){
			dispatcher.forward(request, response);	
		} else {
			response.getWriter().print("Action not supported: "+action);
		}
		
		
	}
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	

}
