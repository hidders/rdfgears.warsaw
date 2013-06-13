package nl.tudelft.rdfgears.rgl.function.standard;


import java.io.UnsupportedEncodingException;
import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * 
 * Build a URL from a base URL and a record containing GET parameters. The parameter values 
 * are modelled as plain literals; URI's are converted to a string, and for literals the language
 * and datatype are ignored. 
 * 
 * This functions thus looses the RDF nature of the get parameters, and is therefor not suitable 
 * for services that require N-Triple style encoding (such as RDF Gears Webservice itself!).  
 * 
 * @author Eric Feliksik
 *
 */
public class BuildURL extends AtomicRGLFunction {
	public static final String INPUT_BASE = "base_url";
	public static final String INPUT_PARAMS = "get_params";
	
	
	public BuildURL(){
		this.requireInput(INPUT_BASE);
		this.requireInput(INPUT_PARAMS);
	}


	@Override
	public void initialize(Map<String, String> config) {
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws WorkflowCheckingException {
		RGLType urlType = inputTypes.get(INPUT_BASE);
		if (!urlType.isRDFValueType()){
			throw new FunctionTypingException(INPUT_BASE, RDFType.getInstance(), urlType);  
		}
		
		RGLType paramType = inputTypes.get(INPUT_PARAMS);
		
		if (!paramType.isRecordType()){
			throw new FunctionTypingException(INPUT_BASE, RecordType.getInstance(new TypeRow()), paramType); // any record would do  
		}
		
		RecordType paramRec = (RecordType) paramType;
		for (String field : paramRec.getRange()){
			RGLType fieldType = paramRec.getFieldType(field);
			
			if (!fieldType.isRDFValueType()){ // the record must contain RDF values (e.g. string literals)
				TypeRow tr = new TypeRow();
				tr.put(field, RDFType.getInstance());
				throw new FunctionTypingException(INPUT_PARAMS, RecordType.getInstance(tr), paramType);   
			}	
		}
		
		// Everything ok. We return a URL. 
		return RDFType.getInstance(); 
	}


	@Override
	public RGLValue executeImpl(ValueRow inputRow)  {
		RGLValue uri = inputRow.get(INPUT_BASE);
		RGLValue params = inputRow.get(INPUT_PARAMS);
		
		if (uri.isNull())
			return uri;
		if (params.isNull())
			return params;
		
		AbstractRecordValue paramRec = params.asRecord();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(uri.asURI().uriString());
		boolean putAmpersand = false;
		for (String field : paramRec.getRange()){
			if (putAmpersand){
				sb.append('&');
			} else { // put question mark the first time
				sb.append('?');
				putAmpersand = true;
			}
			
			sb.append(field);
			sb.append('=');
			
			try { 
				RGLValue val = paramRec.get(field);
				String s; 
				if (val.isURI()){
					s = val.asURI().uriString();
				}
				else {
					s = val.asLiteral().getValueString();
				}
				
				String encoded = java.net.URLEncoder.encode(s, "UTF-8");
				sb.append(encoded);
			} catch (UnsupportedEncodingException e){
				throw new RuntimeException("Cannot encode UTF-8, I don't understand this. ");
			}
		}
		
		return ValueFactory.createURI(sb.toString());
	}
	

    
    
	
}
