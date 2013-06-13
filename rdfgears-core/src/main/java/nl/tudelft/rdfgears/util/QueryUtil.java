package nl.tudelft.rdfgears.util;

import java.util.List;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RDFValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.ModifiableRecord;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.query.DataSource;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTPWithPublicHTTPQuery;

public class QueryUtil {
	
	static RGLValue notBoundError = Engine.getValueFactory().createNull("SPARQL Variable not bound");
	
	/**
	 * Get a QueryExecution object with the variables in inputRow pre-bound. 
	 * @param inputRow
	 */
	public static QueryExecution createLocalQueryExecution(Query query, ValueRow inputRow){
		
		DataSource dataSource = DatasetFactory.create();
		QuerySolutionMap initialBindings = new QuerySolutionMap();
		
		Engine.getLogger().debug("Will execute query :\n"+query+"\n on the following models: ");
		/** set initial bindings */
		for (String varName : inputRow.getRange()){
			RGLValue value = inputRow.get(varName);
			
			RDFNode graphName = null;
			
			if (value.isGraph()){
				GraphValue graphValue = value.asGraph();
				/* add the graph as a named graph */
				graphName = graphValue.getRDFNode();
				
				/* make sure the graphValue loads suffient data in the model to answer the query */
				graphValue.loadDataForQuery(query, varName);
				
				dataSource.addNamedModel(graphName.asResource().getURI(), graphValue.getModel());
				
				Engine.getLogger().debug("     GRAPH ?"+varName+" ("+graphValue.getModel().size()+" elements)");
				
				
			} else if (value.isRDFValue()) {
				RDFValue rdfValue = value.asRDFValue();
				graphName = rdfValue.getRDFNode();
			} else { /* we cannot handle this, and it should have been prevented by typechecking */
				throw new RuntimeException("We cannot handle the input type "+value.getClass()+", this should have been prevented by typechecking");
			}
			
			//System.out.println("Binding "+attrName+ " to "+rdfNode);
			initialBindings.add(varName, graphName);
		}
		
		
		//Query query = QueryModifier.createBoundQuery(selectResStr, initialBindings);
		QueryExecution qexec = QueryExecutionFactory.create(query, dataSource);
		qexec.setInitialBinding(initialBindings);
		return qexec;
	}
	
	
	
	/**
	 * Get a QueryExecution object with the variables in inputRow pre-bound. 
	 * @param inputRow
	 * @param endpointURI 
	 */
	public static QueryExecution createRemoteQueryExecution(Query query, ValueRow inputRow, String endpointURI) {
		
		//QueryExecution qexec = QueryExecutionFactory.createServiceRequest(endpointURI , query);
		/* hack: use modified HTTP query execution to show error message, if query fails  
		 */
		QueryExecution qexec = new QueryEngineHTTPWithPublicHTTPQuery(endpointURI, query);
		
		// unfortunately, initial bindings are not supported for remote queries in our ARQ version. 
		// This is acknowledged to be a "great shame" on http://openjena.org/wiki/ARQ/Manipulating_SPARQL_using_ARQ (useful doc!)  
		
		// But we can bind it statically by rewriting 'query', using some approach similar to 
		// QueryLimitRewriter. 
		// this will be useful, but this is for later. 
		if (false){
			QuerySolutionMap initialBindings = new QuerySolutionMap();
			
			/** set initial bindings; does not create map to a datasource, like local query  */
			for (String varName : inputRow.getRange()){
				RGLValue value = inputRow.get(varName);
				
				RDFNode graphName = null;
				
				/* we do not accept binding graphs in remote query (although it'd be cool, but it would require
				 * a distributed query engine and that's beyond our current scope) 
				 */
				if (value instanceof RDFValue) {
					RDFValue rdfValue = (RDFValue) value;
					graphName = rdfValue.getRDFNode();
				} else { /* we cannot handle this, and it should have been prevented by typechecking */
					throw new RuntimeException("We cannot handle the input type "+value.getClass()+", this should have been prevented by typechecking");
				}
				
				//System.out.println("Binding "+attrName+ " to "+rdfNode);
				initialBindings.add(varName, graphName);
			}
			qexec.setInitialBinding(initialBindings);
		}
		
		return qexec;
	}
	
	/**
	 * 
	/**
	 * Execute a CONSTRUCT query and put the results in the given model. 
	 * 
	 * Return the number of newly inserted triples. This is NOT equal to the LIMIT of the query, 
	 * as the LIMIT is about the number of pattern matches!  
	 * 
	 * @param qexec The QueryExecution object to execute this select query
	 * @param fiMap The fieldIndexMap based on the projected variables in the select query
	 * @param list the List of RGLValue object in which the resulting RGL Records must be inserted. 
	 * @return the number of query results, i.e. the number of records that were inserted in the list. 
	 */
	public static long executeConstructQuery(QueryExecution qexec, Model model) {
		/*  approach that creates a bag FROM the resultset */
		long newElements = 0;
		try {
			Model resultModel; 
			long sizeBefore = model.size();
			resultModel = qexec.execConstruct(model);
			assert(model==resultModel);
			newElements = resultModel.size() - sizeBefore;
		} catch (com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP e){
			throw (e);		
		} 
		finally { 
			qexec.close(); 
		}
		return newElements;
	}
	
	/**
	 * Create a fieldIndexMap that can be used to instantiate an ValueRow for the RecordValue objects in 
	 * the result of the given SELECT query. This is done based on all the possible project variables of 
	 * (the query SELECT clause). This is not optimal if there is many OPTIONAL variables that are unbound, 
	 * but this is likely to be insignificant. 
	 */
	public static FieldIndexMap createFieldIndexMapForQuery(Query q){
		assert(q.isSelectType()): "query must be SELECT query";
		String[] fieldsAr = new String[q.getProject().getVars().size()];
		int i=0;
		for(Var variable : q.getProject().getVars()){
			fieldsAr[i++] = variable.getVarName();
		}
		return nl.tudelft.rdfgears.util.row.FieldIndexMapFactory.create(fieldsAr);

	}

	/**
	 * Execute a SELECT query and append the results to the given list.  
	 * return the number of newly inserted records.   
	 * 
	 * @param qexec The QueryExecution object to execute this select query
	 * @param fiMap The fieldIndexMap based on the projected variables in the select query
	 * @param list the List of RGLValue object in which the resulting RGL Records must be inserted. 
	 * @return the number of query results, i.e. the number of records that were inserted in the list. 
	 */
	public static long executeSelectQueryToRecordList(QueryExecution qexec, FieldIndexMap fiMap, List<RGLValue> list) {
		long resultCount = 0;
		/*  approach that creates a bag FROM the resultset */
		try {
			
			ResultSet results = qexec.execSelect();
			
			while( results.hasNext()){
				QuerySolution soln = results.nextSolution();
				
				/**
				 * Create an ValueRow and MemoryRecordValue for the given solution.
				 * I once used a different RecordValue implementation that remembered the QuerySolution, but it 
				 * seems that it is more memory-efficient to save the generated RDFValues and MemoryRecord is 
				 * good for that (The reason is that otherwise the RGL-RDFValue must be constructed every time the 
				 * variable is accessed, be better do it at once).
				 * 
				 * This exprRow contains fields for all the possible project variables of the fiMap 
				 * (query select clause). This is not optimal if there is many OPTIONAL variables that are unbound, 
				 * but this is likely to be insignificant.
				 */
				
				
				AbstractModifiableRecord rec  = new ModifiableRecord(fiMap);
				
				for (String fieldName : fiMap.getFieldNameSet()){
					/* do not use soln.varNames() because it gives only the BOUND variables */
						RDFNode rdfNode = soln.get(fieldName);
						
						/**
						 * Sometimes, virtuoso seems to return an RDFNode that serializes as "true"
						 * when I would have expected the variable to be unbound (null)... strange. 
						 */
						
						//System.out.println("RDFNode is: "+rdfNode);
						if (rdfNode==null){
							rec.put(fieldName, notBoundError);
							//System.out.println("ERROR is: "+exprRow.get(fieldName));
						}
						else {
							rec.put(fieldName, Engine.getValueFactory().createRDFValue(rdfNode));
							//System.out.println("RDFNode is : "+rdfNode);
						}	
				}

				list.add(ValueFactory.registerValue(rec)); // register the record when it's complete
				
				//System.out.println("Created Record "+rec);
				
				//System.out.println("Fetched::: "+list.get(list.size()-1));
				resultCount++;
			}
			
		} catch (com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP e){
			qexec.close(); 
			throw (e);		
		} 
		finally { 
			qexec.close(); 
		}
		
		return resultCount;
	}
}


