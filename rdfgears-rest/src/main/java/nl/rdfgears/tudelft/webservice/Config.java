package nl.rdfgears.tudelft.webservice;

import javax.servlet.http.HttpServletResponse;

import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.ValueSerializer;

public class Config {
	public static ValueSerializer getStorageSerializer(String resultId){
		return null;
	}

	public static ValueSerializer getClientSerializer(HttpServletResponse response){
		return null;
	}
	
}
