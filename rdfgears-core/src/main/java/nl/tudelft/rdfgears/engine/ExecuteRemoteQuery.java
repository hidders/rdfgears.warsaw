package nl.tudelft.rdfgears.engine;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class ExecuteRemoteQuery {
	
	/* Execute SELECT query in QUERY_FILE on ENDPOINT */
	public static final String ENDPOINT = "http://dbpedia.org/sparql";
	public static final String QUERY_FILE = "/home/af09017/Desktop/queries/dbpedia1.txt";
	
	private static String readFile(String pathname) throws IOException {
	    StringBuilder stringBuilder = new StringBuilder();
	    Scanner scanner = new Scanner(new File(pathname));

	    try {
	        while(scanner.hasNextLine()) {        
	            stringBuilder.append(scanner.nextLine() + "\n");
	        }
	    } finally {
	        scanner.close();
	    }
	    return stringBuilder.toString();
	}

	private static Query getQuery(String filePath){
		String queryStr = null;
		try {
			queryStr = readFile(filePath);
			return QueryFactory.create(queryStr, Syntax.syntaxSPARQL_11);
		} catch (QueryParseException e) {
			Engine.getLogger().error("Cannot parse query: ");
			Engine.getLogger().error(queryStr);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args){
		Query query = getQuery(QUERY_FILE);
		
		QueryEngineHTTP serviceRequest = QueryExecutionFactory.createServiceRequest(ENDPOINT, query); 
		ResultSet rs = serviceRequest.execSelect();
		System.out.println("Query results: ");
		while (rs.hasNext()){
			QuerySolution qs = rs.next();
			Iterator<String> varNames = qs.varNames();
			while(varNames.hasNext()){
				String varName = varNames.next();
				System.out.print(varName+" = "+qs.get(varName)+" ; ");
			}
			System.out.println();
		}
	}
}
