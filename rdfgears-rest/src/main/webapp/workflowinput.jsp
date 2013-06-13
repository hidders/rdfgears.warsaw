<%@page import="com.sun.corba.se.spi.protocol.RequestDispatcherRegistry"%>
<%@page import="com.sun.corba.se.spi.protocol.RequestDispatcherDefault"%>
<%@page import="java.io.PrintWriter"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="nl.tudelft.rdfgears.engine.*" %>
<%@ page import="nl.tudelft.rdfgears.rgl.workflow.*" %>

<%@ page import="java.util.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>RDF Gears workflow execution - input</title>
</head>
<body>
<!-- 
    <form action=" " method="POST">
        First Name: <input type="text" name="firstName" size="20"><br>
        Surname: <input type="text" name="surname" size="20">
        <br><br>
        <input type="submit" value="Submit">
    </form>
     -->
     
<!--     ServletOutputStream output = response.getOutputStream(); -->
		
<!-- 		//response.setContentType("application/xml;charset=utf-8"); -->
		
<!-- 		response.setContentType("text/html;charset=utf-8"); -->
<!-- 		response.setStatus(HttpServletResponse.SC_BAD_REQUEST); -->
		
<!--     	Engine.getLogger().debug("Request for workflow "+workflow.getFullName()); -->
		
		
		<%

			if (request.getAttribute("javax.servlet.forward.servlet_path")==null){
				throw new RuntimeException("Don't call this path directly; call the dispatcher. ");
			}
		
		Workflow workflow; 
		List<String> requiredInputNames; 
		String formattedWorkflowName = "[None]";
		String workflowId;
		try {
//  		workflow = WorkflowLoader.loadWorkflow(workflowName);
			workflowId = (String) request.getAttribute("rdfgears.workflowId");
			if (workflowId==null){
				out.print("No workflow Id was specified. It was not set by the dispatching servlet.");
			}
			workflow = WorkflowLoader.loadWorkflow(workflowId);
			
 			requiredInputNames = workflow.getRequiredInputNames();
 			
 			formattedWorkflowName = "<span style=\"font-family: monospace\">"+workflowId+"</span>";
		}
		catch (Exception e){
			out.print("Error 1: "+e);
			e.printStackTrace(new PrintWriter(out));			
			return;
		}
		
		
		%>

<%--     	Provide workflow input parameters for workflow <%= workflow.getFullName() %> <br/> --%>
		
		
		<img src="<%= request.getContextPath() %>/img/rdf-gears-logo-tiny.png" />
		<b>RDF Gears workflow input form.</b> 
		<br/>
		<br/>
		<br/>
		<br/> 
		
		<form name="input" action="<%= request.getContextPath() %>/user/execute<%= workflowId %>" method="get">
		
		<% if (requiredInputNames.size()==0) {	%>
			Workflow <%= formattedWorkflowName %> has no inputs. <br/>
		<% } else {%>
			Workflow <%= formattedWorkflowName %> requires the following inputs:<br/><br/> 
			<% for (String inputName : requiredInputNames ){  %>
				&nbsp;&nbsp;&nbsp;&nbsp;<%= inputName %>: <input type="text" name="<%= inputName %>" size="70"/><br/>
			<% } %>
		<% } %>
		<br/>
		<pre><input type="submit" value="run workflow"/></pre>
		</form>
		
		<br/><br/>
		<p>On this page you can enter the input parameters for a service. All inputs must be specified using RDF types. Which type is needed depends on the parameter of the service.<br />
To specify a URI use the following format &lt;###&gt;<br />
<i>Example: &lt;http://dbpedia.org/resource/Delft&gt; </i><br />
To specify text use "###" <br />
to specify text in a particular language append the text with '@' and the language code. <br />
<i>Example: "la pomme"@fr </i><br />
Numbers can be specified in two ways. Numbers can be typed normally, so without quotes, which works for almost all services.
Numbers can also be specified with their fully qualified type. <br />
<i>Example: "1.2"^^&lt;http://www.w3.org/2001/XMLSchema#double&gt; </i><br />
<i>Example: "10"^^&lt;http://www.w3.org/2001/XMLSchema#integer&gt;</i>
</p>
	
		
    
</body>
</html>