package nl.tudelft.rdfgears.rgl.function.sparql;

import java.util.List;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;
import nl.tudelft.rdfgears.util.QueryUtil;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.query.QueryExecution;


/**
 * Behavior for local SELECT querying
 * @author Eric Feliksik
 *
 */
public class LocalSelectBehavior extends AbstractSelectBehavior {
	
	public LocalSelectBehavior(SPARQLFunction func) {
		super(func);
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		List<RGLValue> backingList = ValueFactory.createBagBackingList();
		/* do local query */
		QueryExecution qexec = QueryUtil.createLocalQueryExecution(sparqlFunc.getQuery(), inputRow);
		QueryUtil.executeSelectQueryToRecordList(qexec, QueryUtil.createFieldIndexMapForQuery(sparqlFunc.getQuery()), backingList);
		return new ListBackedBagValue(backingList); 
	}
	
}

