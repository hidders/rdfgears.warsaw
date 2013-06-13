package nl.tudelft.rdfgears.rgl.function;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

public class RGLFunctionFactory {
	
	private static Query parseEndpointQuery(String queryString){
		try {
			/* some endpoints (like dbpedia) partially support SPARQL1.1. constructs 
			 * (e.g. SELECT (str(?uri) AS ?uriStr) WHERE {...} )
			 * 
			 * We want to allow this, so we parse with the more liberal SPARQL1.1 syntax. 
			 * 
			 * However, most endpoints don't support all of it, and ARQ doesn't properly return the 
			 * server's syntax error. So let ARQ parse with SPARQL1.1, and if there is a QueryExectionError, 
			 * do some extra checks.  
			 * 
			 */
			return QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
			
		} catch (com.hp.hpl.jena.query.QueryParseException e){
			System.out.println("Cannot parse your SPARQL endpoint query: \n"+queryString+"\n Reason: "+e.getMessage());
		}
		return null;
	}
//	OBSOLETE 
//	public static AbstractSPARQL createSPARQLEndpointFunction(String queryString){
//		Query query = parseEndpointQuery(queryString);
//		if (query==null)
//			return null;
//		
//		AbstractSPARQL func = null;
//		if (query.isConstructType()){
//			func = new RemoteConstruct();
//			
//		} else if (query.isSelectType()){
//			func = new RemoteSelect();
//			
//		} else {
//			throw new RuntimeException("SPARQL query type not supported: We currently only support SELECT/CONSTRUCT queries. Query: "+query.toString());
//		}
//		
//		Map<String,String> initMap = new HashMap<String,String>();
//		initMap.put("query", queryString);
//		func.initialize(initMap);
//		
//		return func;
//	}
}
