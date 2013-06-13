package nl.rdfgears.tudelft.webservice.requests;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.Optimizer;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.ImRealXMLSerializer;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.ValueSerializer;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.util.ValueParser;
import nl.tudelft.rdfgears.util.row.HashValueRow;
import nl.tudelft.rdfgears.util.row.TypeRow;

public class WorkflowExecutionRequestGET extends WorkflowExecutionRequest {

	public WorkflowExecutionRequestGET(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,	WorkflowLoadingException {
		super(request, response);
		
	}

	public void configureWorkflowInputs(){
		/* check whether all inputs are provided */
		valueRow = new HashValueRow();
		typeRow = new TypeRow();
		for (String inputName : getWorkflow().getRequiredInputNames()){
			String strVal = request.getParameter(inputName);
			
			if (strVal==null){
				throw new IllegalArgumentException("The GET parameter '"+inputName+"' needs a value");
			}
    		try {
    			RGLValue val = ValueParser.parseNTripleValue(strVal);
    			assert(val!=null);
    			assert(inputName!=null);
    			valueRow.put(inputName, val);
    			typeRow.put(inputName, RDFType.getInstance()); // typechecking doesn't distinguish URI/literal
			} catch (Exception e){
				e.printStackTrace();
    			throw new IllegalArgumentException("Cannot use input value '"+strVal+"' (given as for input '"+inputName+"'): "+e.getMessage());
    		}
		}
	}
	
	public void execute() throws IOException {
		ServletOutputStream output = response.getOutputStream();
		
		response.setContentType("application/xml;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
			
			try {
				RGLType returnType = Engine.typeCheck(workflow, typeRow);
				/* optimize */
				workflow = (new Optimizer()).optimize(workflow, false);
				Engine.getLogger().debug("Loaded workflow "+workflow.getFullName()+"; executing. ");
				ValueSerializer serializer = new ImRealXMLSerializer(returnType, output);
				
				serializer.serialize(workflow.execute(valueRow));
				
				
			} catch (WorkflowCheckingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
}
