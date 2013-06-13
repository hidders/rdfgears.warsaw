package nl.tudelft.rdfgears.rgl.function.sparql;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.bindings.BatchedRemoteQueryingBagBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.BagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.util.QueryUtil;
import nl.tudelft.rdfgears.util.RenewableIterator;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.ValueRow;

import arq.query;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.serializer.QueryLimitRewriter;
import com.sleepycat.bind.tuple.TupleBinding;

/**
 * Behavior for batched remote SELECT querying
 * 
 * @author Eric Feliksik
 * 
 */
public class RemoteSelectBehavior extends AbstractSelectBehavior {
	SPARQLFunction func;

	public RemoteSelectBehavior(SPARQLFunction func) {
		super(func);
		this.func = func;
		assert (func != null);
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		//System.err.println(func.getQuery().toString(Syntax.syntaxSPARQL));
		/* it never hurts to cache downloaded data */
		AbstractBagValue queryingBagValue = new BatchedRemoteQueryingBagValue(
				sparqlFunc.getQuery(), inputRow, sparqlFunc.getEndpointURI()); // create
		// remote
		// query

		if (Engine.getConfig().do_greedyLoadingOfRemoteQueries()) {
			// keep remote sparql results in memory
			queryingBagValue.prepareForMultipleReadings();

			// disable lazy loading, load EVERYTHING, to find SPARQL endpoint
			// errors ASAP
			Iterator<RGLValue> iterator = queryingBagValue.iterator();
			while (iterator.hasNext())
				iterator.next();

//			Engine.getLogger()
//					.debug("remote query gave " + queryingBagValue.size()
//							+ " results");
		}

		return queryingBagValue;
	}

	/**
	 * For batched remote SELECT querying.
	 * 
	 * Takes a query, an inputRow with binding variables (currently not used,
	 * not implemented) and an endpointURI to execute the query on.
	 * 
	 * The resulting bag lazily queries the endpoint in batches of
	 * QUERY_BATCH_SIZE results. This is necessary because some endpoints
	 * implicitly LIMIT 2000.
	 * 
	 * @author Eric Feliksik
	 * 
	 */
	public static class BatchedRemoteQueryingBagValue extends StreamingBagValue {

		public final static int QUERY_BATCH_SIZE = Engine.getConfig()
				.getRemoteSparqlSelectBatchSize(); /* the size of each batch */

		// whether or not we should query in batches. Can be set to false by
		// configuring batch-size==0.
		private boolean doQueryBatching = QUERY_BATCH_SIZE != 0;

		private FieldIndexMap queryFieldMap;
		private transient Query originalQuery;
		private ValueRow inputRow;
		private String endpointURI;

		/**
		 * provide a non-null endpointURI to create lazybag for endpoint
		 * querying
		 */
		public BatchedRemoteQueryingBagValue(Query query, ValueRow inputRow,
				String endpointURI) {
			this.inputRow = inputRow;
			this.originalQuery = query;
			this.endpointURI = endpointURI;
			this.queryFieldMap = QueryUtil.createFieldIndexMapForQuery(query);
		}

		public BatchedRemoteQueryingBagValue(long id,
				Map<Long, Integer> iteratorPosition, Query query,
				ValueRow inputRow, String endpointURI) {
			this(query, inputRow, endpointURI);
			this.myId = id;
//			this.iteratorPosition = iteratorPosition;
		}

		public BatchedRemoteQueryingBagValue(long id,
				Map<Long, Integer> iteratorPosition,
				MaterializingBag materializingBag, Query query,
				ValueRow inputRow, String endpointURI) {
			this(id, iteratorPosition, query, inputRow, endpointURI);
			this.materializingBag = new MaterializingBag(materializingBag,
					getFirstStreamingBagIterator());

		}

		@Override
		public TupleBinding<RGLValue> getBinding() {
			return new BatchedRemoteQueryingBagBinding(originalQuery, inputRow,
					endpointURI);
		}

		private Query getOriginalQuery() {
			return originalQuery;
		}

		@Override
		protected Iterator<RGLValue> getStreamingBagIterator() {
			return new RemoteQueryIter();
		}

		@Override
		public int size() {
			return BagValue.getNaiveSize(this);
		}
		
		private void writeObject(ObjectOutputStream out)  throws IOException {
			out.writeUTF(originalQuery.toString(Syntax.syntaxSPARQL));
		}
		
		private void readObject(ObjectInputStream in) 
				throws IOException, ClassNotFoundException {
			originalQuery = QueryFactory.create(in.readUTF(), Syntax.syntaxSPARQL);
		}

		private int nextElemNr = 0;

		class RemoteQueryIter implements RenewableIterator<RGLValue>, Serializable {
			/*
			 * the next element the iterator points to
			 */

			/* a buffer of remotely fetched values */
			private ArrayList<RGLValue> buffer = new ArrayList<RGLValue>(
					QUERY_BATCH_SIZE > 0 ? QUERY_BATCH_SIZE : 16);

			/*
			 * index of the next element in the buffer, related to nextElemNr
			 * (see invariant)
			 */
			private int nextElemBufferIx = 0;

			private int buffersIterated = 0; // the number of buffers we already
			// consumed

			private int fetchedResults = 0; // number of results we fetched
			private boolean haveQueried = false; // whether one query was
			// already executed
			private boolean thereMayBeMoreBatches = true;

			public RemoteQueryIter() {
				fillBuffer(buffer);
			}

			/**
			 * Assumes the buffer is filled
			 */
			@Override
			public boolean hasNext() {
				checkInvariant();
				if (nextElemBufferIx >= buffer.size()) {
					return false; // The buffer is filled, but doesn't contain
					// that many elements;
				}
				return true;
			}

			private void checkInvariant() {
				assert (doQueryBatching == (QUERY_BATCH_SIZE != 0));
				assert (nextElemBufferIx + buffersIterated * QUERY_BATCH_SIZE == nextElemNr);
			}

			@Override
			public RGLValue next() {
				checkInvariant();
				RGLValue result = buffer.get(nextElemBufferIx);
				assert (result != null);
				nextElemNr++;
				nextElemBufferIx++;
				if (doQueryBatching && nextElemBufferIx >= QUERY_BATCH_SIZE) {
					buffersIterated++;
					nextElemBufferIx = 0;
					fillBuffer(buffer);
				}

				checkInvariant();
				return result;
			}

			@Override
			public void remove() {
				assert (false);
			}

			/*
			 * *****************************************************************
			 * *****************************************************************
			 * 
			 * Batched remote query mechanism ; only works if
			 * isEndpointQuery()==true, so this may be moved to a separate class
			 * 
			 * *****************************************************************
			 * *****************************************************************
			 */

			private boolean mustRepeatQuery() {
				if (!doQueryBatching)
					return false;

				if (getOriginalQuery().hasLimit()
						&& getOriginalQuery().getLimit() <= QUERY_BATCH_SIZE) {
					return false; /*
								 * query already has a strict limit, no need to
								 * batch it
								 */
				}
				return true;
			}

			/**
			 * @return the offset for the next query
			 */
			private long getNextOffset() {
				assert (mustRepeatQuery());
				long originalOffset = getOriginalQuery().hasOffset() ? getOriginalQuery()
						.getOffset() : 0;
				return originalOffset + fetchedResults;
			}

			/**
			 * @return the limit for the next query, assuming the next query
			 *         should be batched
			 */
			private long getNextLimit() {
				if (originalQuery.hasLimit()
						&& fetchedResults + QUERY_BATCH_SIZE > originalQuery
								.getLimit()) {
					// fetching QUERY_BATCH_SIZE elements would exceed original
					// limit
					assert (fetchedResults <= originalQuery.getLimit()) : "We already fetched more than we should have. ";
					return originalQuery.getLimit() - fetchedResults;
				} else {
					return QUERY_BATCH_SIZE;
				}
			}

			/**
			 * Fetch results per QUERY_BATCH_SIZE. This is not only lazy, but
			 * more importantly, it's necessary because Virtuoso endpoints often
			 * give a max of QUERY_BATCH_SIZE results. So we request
			 * repetitively a LIMIT of QUERY_BATCH_SIZE, shifting the OFFSET by
			 * QUERY_BATCH_SIZE every time, until we receive less than
			 * QUERY_BATCH_SIZE results. This idea is taken from Silk.
			 * 
			 * @return true iff we may have more results after this buffer.
			 */
			private void fillBuffer(List<RGLValue> buffer) {
				buffer.clear(); // empty buffer
				if (!thereMayBeMoreBatches)
					return;

				/* endpoints are lazily queried in batches */
				int resultsInThisBatch;

				Query executeQuery = getOriginalQuery();
				/**
				 * create and do query execution
				 */
				if (mustRepeatQuery()) {
					long limit = getNextLimit();
					if (limit == 0) {
						return;
					}
					// manipulate originalquery with repetitively shifted
					// limit/offset
					executeQuery = QueryLimitRewriter.rewrite(
							getOriginalQuery(), limit, getNextOffset());

				} else {
					thereMayBeMoreBatches = false;
					/*
					 * if we have already queried, we are done! if not, just
					 * execute the originalQuery and be done.
					 */
					if (haveQueried)
						return;
				}

				/*
				 * We pass null as inputRow because unfortunately, initial
				 * bindings are not supported for remote queries in our ARQ
				 * version. Maybe later, when we can rewrite the query and bind
				 * it ourselves.
				 */
				QueryExecution qexec = QueryUtil.createRemoteQueryExecution(
						executeQuery, inputRow, endpointURI);

				/***
				 * Note the code duplication with RemoteConstructBehavior
				 */
				// try until max retries is reached
				int sparqlFailures = 0;
				while (true) {
					try {
						Engine.getLogger().info(
								"Executing remote query on " + endpointURI
										+ "\n" + executeQuery);
						resultsInThisBatch = (int) QueryUtil
								.executeSelectQueryToRecordList(qexec,
										queryFieldMap, buffer);
						if (doQueryBatching && buffer.size() > QUERY_BATCH_SIZE) {
							Engine.getLogger()
									.warn("Buffer size is "
											+ buffer.size()
											+ ", but was expected to be <="
											+ QUERY_BATCH_SIZE
											+ ". So your SPARQL endpoint is giving more values than requested!");
						}
						break; // done!
					} catch (com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP executionException) {
						sparqlFailures++;

						String msg = "Error in remote query execution, QueryExceptionHTTP for query: \n";
						msg += executeQuery.toString();
						msg += "\nThe server responded: "
								+ executionException.getMessage();
						int maxFailures = Engine.getConfig()
								.getSparqlRetryMax();
						if (sparqlFailures >= maxFailures) {
							throw new RuntimeException(msg); // we give up!
						} else {
							long pause = Engine.getConfig()
									.getSparqlRetryPause();
							Engine.getLogger().warn(msg);
							Engine.getLogger().warn(
									"I will attempt a retry (" + sparqlFailures
											+ "/" + maxFailures + ") in "
											+ pause + " ms");

							try {
								Thread.sleep(pause);
							} catch (InterruptedException e) {
								Engine.getLogger()
										.warn("SPARQL-endpoint pause was interrupted.");
							}

						}
					}
				}

				fetchedResults += resultsInThisBatch;
				haveQueried = true;
			}
		}

		@Override
		protected Iterator<RGLValue> getFirstStreamingBagIterator() {
			// TODO Auto-generated method stub
			return getStreamingBagIterator();
		}
	}

}
