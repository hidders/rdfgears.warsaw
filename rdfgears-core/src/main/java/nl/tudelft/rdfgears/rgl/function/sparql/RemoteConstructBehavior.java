package nl.tudelft.rdfgears.rgl.function.sparql;


import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.util.QueryUtil;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.serializer.QueryLimitRewriter;

/**
 * Behavior for batched remote CONSTRUCT querying. 
 * 
 * Note that it may query a bit more elements than specified in the LIMIT, see the javadoc for the 
 * getFetchedResultCount() function.  
 * 
 * 
 * @author Eric Feliksik
 *
 */
public class RemoteConstructBehavior implements SparqlBehavior {
	SPARQLFunction sparqlFunction;
	
	public RemoteConstructBehavior(SPARQLFunction func) {
		this.sparqlFunction = func;
		this.originalQuery = sparqlFunction.getQuery();
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		this.inputRow = inputRow;
		Model model = createFullModel();
		
		return ValueFactory.createGraphValue(model);
		
	}

	@Override
	public RGLType getOutputType() {
		return GraphType.getInstance();
	}
	

	/**
	 * Similar to the BatchedRemoteQueryingBagValue . 
	 * 
	 *  ************* Code duplication. **************** 
	 * 
	 * When refactoring, consider this. 
	 * 
	 * But do note that the LIMIT keyword means the number of pattern matches, and the assumptions 
	 * made in RemoteSelectBehavior do NOT hold here!!!! 
	 *  
	 * @author Eric Feliksik
	 *
	 */

	/* the size of each batch. This is NOT the number of triples, but the number of pattern matches! */
	public final static int QUERY_BATCH_SIZE = Engine.getConfig().getRemoteSparqlConstructBatchSize();

	// whether or not we should query in batches. Can be set to false by configuring batch-size==0. 
	private boolean doQueryBatching = QUERY_BATCH_SIZE != 0; 

	/* number of *batches* we fetched. The number of triples doesn't tell us the number of 
	 * results to administer the LIMIT, as this is about pattern-matches.
	 */ 
	private int fetchedBatches = 0; 
	
	private boolean haveQueried = false; // whether one query was already executed 
	private boolean thereMayBeMoreBatches = true; 

	private Query originalQuery;
	private ValueRow inputRow;
	
	private Model createFullModel() {
		Model model = Engine.getValueFactory().createModel();
		
		while (thereMayBeMoreBatches){
			fillBuffer(model);
		}
		return model;
	}

	private boolean mustRepeatQuery(){
		if (!doQueryBatching)
			return false;
		
		if (getOriginalQuery().hasLimit() && getOriginalQuery().getLimit() <= QUERY_BATCH_SIZE){
			 return false; /* query already has a strict limit, no need to batch it */  
		}
		return true;
	}

	/**
	 * @return the offset for the next query, assuming it must be repeated
	 */
	private long getNextOffset(){
		assert(mustRepeatQuery());
		long originalOffset = getOriginalQuery().hasOffset() ? getOriginalQuery().getOffset() : 0; 
		return originalOffset + getFetchedResultCount();
	}
	
	/**
	 * The number of pattern matches we got. This is *not* the number of triples. 
	 * 
	 * Note this method does *NOT* give the correct result, as the last batch 
	 * may not have been full. 
	 * 
	 * But we could not easily see how often the CONSTRCT pattern was matched, we could only see
	 * the number of resulting triples. 
	 * 
	 * We could, however, do a local SELECT query on the resulting graph, counting the number of 
	 * pattern matches (i.e. number of records). 
	 * 
	 * But for now we don't care. 
	 *  
	 * @return
	 */
	private long getFetchedResultCount(){
		return (fetchedBatches * QUERY_BATCH_SIZE);
	}

	/**
	 * @return the limit for the next query, assuming is must be repeated
	 */
	private long getNextLimit(){
		assert(mustRepeatQuery());
		if (originalQuery.hasLimit() && getNextOffset() + QUERY_BATCH_SIZE > originalQuery.getLimit()){
			// fetching QUERY_BATCH_SIZE elements would exceed original limit
			assert(getFetchedResultCount() <= originalQuery.getLimit()) : "We already fetched more than we should have. ";
			return originalQuery.getLimit() - getFetchedResultCount();
		}
		else {
			// Note that this MAY exceed the original limit, if one was set, due to the fact that LIMIT
			// applies to the number of pattern matches, not to the number of triples. 
			// We cannot easily see the number of pattern matches we had... 
			// 
			return QUERY_BATCH_SIZE;
		}
	}

	/**
	 * Fetch results per QUERY_BATCH_SIZE. Necessary because Virtuoso endpoints often give a max of  
	 * QUERY_BATCH_SIZE results. So we request repetitively a LIMIT of  QUERY_BATCH_SIZE, 
	 * shifting the OFFSET by QUERY_BATCH_SIZE every time, until we receive less than QUERY_BATCH_SIZE results. 
	 * This idea is taken from Silk. 
	 * @return true iff we may have more results after this buffer.  
	 */
	private void fillBuffer(Model model) {
		if (!thereMayBeMoreBatches)
			return;
		
		/* endpoints are lazily queried in batches */
		long triplesInThisBatch;
		
		Query executeQuery = getOriginalQuery();
		/**
		 * create and do query execution
		 */
		if (mustRepeatQuery()){
			long limit = getNextLimit();
			if (limit == 0){
				return;
			}
			// manipulate originalquery with repetitively shifted limit/offset
			executeQuery = QueryLimitRewriter.rewrite(getOriginalQuery(), limit, getNextOffset());
		} else {
			thereMayBeMoreBatches = false;
			/* if we have already queried, we are done! 
			 * if not, just execute the originalQuery and be done. 
			 */
			if (haveQueried)
				return;
		}
		
		/***
		 * Note the code duplication with RemoteSelectBehavior
		 */
		// try until max retries is reached
		int sparqlFailures = 0;
		while (true) {
			
			try {
					
				/* We pass null as inputRow because unfortunately, initial bindings are not 
				 * supported for remote queries in our ARQ version. 
				 * Maybe later, when we can rewrite the query and bind it ourselves. */
				QueryExecution qexec = QueryUtil.createRemoteQueryExecution(executeQuery, inputRow, sparqlFunction.getEndpointURI());
				
				Engine.getLogger().info("Executing remote query on "+sparqlFunction.getEndpointURI()+"\n"  +executeQuery);
				triplesInThisBatch = QueryUtil.executeConstructQuery(qexec, model);
				// comparison is pointless, LIMIT and the number of triples are not directly related 
//				if (triplesInThisBatch>QUERY_BATCH_SIZE){
//					Engine.getLogger().warn("Fetched "+triplesInThisBatch+" elements in CONSTRUCT query, but was expected to be <="+QUERY_BATCH_SIZE +". So your SPARQL endpoint is giving more values than requested!");
//				} 
				break; // ok, done!
			}	
			catch (com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP executionException){
				sparqlFailures++;
				
				String msg = "Error in remote query execution, QueryExceptionHTTP for query: \n";
				msg += executeQuery.toString();
				msg += "\nThe server responded: "+executionException.getMessage();
				int maxFailures = Engine.getConfig().getSparqlRetryMax();
				if (sparqlFailures>=maxFailures){
					throw new RuntimeException(msg); // we give up!
				}
				else {
					long pause = Engine.getConfig().getSparqlRetryPause();
					Engine.getLogger().warn(msg);
					Engine.getLogger().warn("I will attempt a retry ("+sparqlFailures+"/"+maxFailures+") in "+pause+" ms");
					
					try {
						Thread.sleep(pause);
					} catch (InterruptedException e) {
						Engine.getLogger().warn("SPARQL-endpoint pause was interrupted.");
					}
					
				}
			}
		}
		
		haveQueried = true;
		
		/* note this flag behaves different from the implementation of lazy select mechanism */

		// these are not directly related for CONSTRUCT, so cannot use it as in SELECT. 
		// If we have less triples than batch size, then we are sure that the number of results 
		// was smaller than the given limit. 
		// But with this heuristic we may still query once too many. 
		thereMayBeMoreBatches = (triplesInThisBatch >= QUERY_BATCH_SIZE); 
		fetchedBatches++;
	}
		
		
	private Query getOriginalQuery(){
		return originalQuery;
	}

	
}

