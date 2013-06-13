package nl.tudelft.rdfgears.rgl.function.sparql;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.util.QueryUtil;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Behavior for local CONSTRUCT querying
 * @author Eric Feliksik
 *
 */
public class LocalConstructBehavior implements SparqlBehavior {
	SPARQLFunction sparqlFunction;
	public LocalConstructBehavior(SPARQLFunction func){
		sparqlFunction = func;
	}
	
	@Override
	public RGLType getOutputType() {
		return GraphType.getInstance();
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		/* do local query */
		QueryExecution qexec = QueryUtil.createLocalQueryExecution(sparqlFunction.getQuery(), inputRow);
		
		Model model = ModelFactory.createDefaultModel();
		
		
		QueryUtil.executeConstructQuery(qexec, model);
		
		Engine.getLogger().debug("Result model has "+model.size()+"  elements.");

		return ValueFactory.createGraphValue(model);
	}

}
