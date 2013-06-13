package nl.tudelft.rdfgears.rgl.function.sparql;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.GraphOrRDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionConfigurationException;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.Syntax;


/**
 * A function that can execute SPARQL SELECT/CONSTRUCT queries. 
 * 
 * The query is configured with initialize()
 * 
 * 
 * @author Eric Feliksik
 *
 */
public class SPARQLFunction extends SimplyTypedRGLFunction {
	
	private String queryString;
	private Query query;
	private String endpointURI = null;
	
	SparqlBehavior behavior; 
	
	public void initialize(Map<String, String> config){
		if (getQuery()==null){
			queryString = config.get("query");
			assert(queryString!=null);
			setQuery(queryString);
		}
		assert(getQuery()!=null);
		
		/* configure the required inputs based on the ';' separated list of prebound variable names */
		String bindVarStr = config.get("bindVariables");
		if (bindVarStr!=null){
			String[] split = bindVarStr.split(";");
			for (int i=0; i<split.length; i++){
				if (split[i].length()>0)
					requireInputType(split[i], new GraphOrRDFType()  ); // bound variables can be either graphs or URI/literals
			}	
		}
		/* endpointURI, if any */
		endpointURI = config.get("endpoint");
		configureBehavior();
	}
	
	public SPARQLFunction(){
	}
	
	
	private void configureBehavior(){

		if (getQuery().isSelectType()){ 	/* SELECT */
			if (endpointURI==null){
				behavior = new LocalSelectBehavior(this); /* local */
			} else {
				behavior = new RemoteSelectBehavior(this); /* remote */
			}
			
		} else {
			assert(getQuery().isConstructType()); 	/* CONSTRUCT */
			if (endpointURI==null){
				behavior = new LocalConstructBehavior(this); /* local */
			} else {
				behavior = new RemoteConstructBehavior(this); /* remote */
			}
		} 
	}
	
	protected void setQuery(String queryString){
		try {
			setQuery(QueryFactory.create(queryString, Syntax.syntaxSPARQL));
		} catch (QueryParseException e){
			throw new FunctionConfigurationException("Your SPARQL query is incorrect: \n"+queryString+"\n"+e.getMessage());
		}
	}
	
	private void setQuery(Query query){
		if (!(query.isConstructType() || query.isSelectType())){
			throw new RuntimeException("You must set a CONSTRUCT or SELECT query");
		}
		this.query = query;
	}
	
	protected Query getQuery(){
		return this.query;
	}

	@Override
	public RGLType getOutputType() {
		return behavior.getOutputType();
	}

	/**
	 * The values in the inputRow will be bound to the variables in the query.  
	 * 
	 * This is also the way to pass models; you can use a section
	 * 		GRAPH $mygraph { ... } .
	 * 
	 * Then make sure the inputRow contains a field 'mygraph' with a RGL Graph value. 
	 * 
	 */
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		String computedQueryString = queryString;
		for (String varName: inputRow.getRange()) {
			if (!inputRow.get(varName).isGraph()) {
				computedQueryString = computedQueryString.replace("?" + varName, inputRow.get(varName).toString());
			}
		}
		
		System.err.println(queryString);
		
		setQuery(computedQueryString);
		configureBehavior();
		
		return behavior.simpleExecute(inputRow);
	}
	
	/**
	 * Will return a URI iff this SparqlFunction is a remote (i.e. SPARQL Endpoint) query function. 
	 * @return
	 */
	public String getEndpointURI() {
		return endpointURI;
	}
	
}
