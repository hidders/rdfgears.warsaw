package nl.tudelft.rdfgears.rdftools;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import nl.tudelft.rdfgears.engine.Engine;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;


public class RDFTools {
	static final String HEXES = "0123456789ABCDEF";
	

	
	private static String getHex( byte [] raw ) {
		if ( raw == null ) {
		  return null;
		}
		final StringBuilder hex = new StringBuilder( 2 * raw.length );
		for ( final byte b : raw ) {
		  hex.append(HEXES.charAt((b & 0xF0) >> 4))
		     .append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}
	

	
	public static Model constructQuery(DataSource dataSource, String queryStr){
		Query query = QueryFactory.create(queryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, dataSource);
		Model resultModel = qexec.execConstruct() ;
		qexec.close(); 
		return resultModel;
	}
	

	
	
	public static Model constructQuery(String queryService, String queryStr, boolean useCache){
		Model returnModel;
		String hash = "no-hash";
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest((queryService+queryStr).getBytes());
			hash = getHex(digest);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String cacheFileName = "/tmp/"+hash+".xml";

		
		if (useCache){
			try {
				returnModel = Engine.getValueFactory().createModel().read(new FileInputStream(cacheFileName), "http://dont_care");
				return returnModel;
			}
			catch(Exception e){
				System.out.println("No cache file found: "+cacheFileName+", loading from service");
			}
		}
		
		Query query = QueryFactory.create(queryStr);
		
		assert(false): "not implemented"; 
		/*
		QueryExecution qexec = QueryExecutionFactory.create(query, dataSource);
		returnModel = (new Operation(queryStr)).executeConstruct(queryService);
		// save to cache  
		ModelTools.writeModel(returnModel, cacheFileName);
		
		return returnModel;
		
		*/
		return null;
		
	}
	
}
