package nl.tudelft.rdfgears.rgl.function.sparql;

import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.util.row.TypeRow;

import com.hp.hpl.jena.sparql.core.Var;

/**
 * A SPARQL SELECT function implementation for rdfgears. 
 * 
 * @author Eric Feliksik
 *
 */
public abstract class AbstractSelectBehavior implements SparqlBehavior {
	protected SPARQLFunction sparqlFunc;
	private RGLType correctReturnType;

	public AbstractSelectBehavior(SPARQLFunction func){
		sparqlFunc = func;
	}
	
	public RGLType getOutputType() {
		
		if (correctReturnType == null){
			TypeRow recordTypeRow = new TypeRow();
			/* the return type is a bag of records, and the Strings of the records are determined 
			 * by the SELECT clause of the query. 
			 * At least we know that each String in the record will contain something of type RDFValueType. 
			 * 
			 */

			RDFType rdfType = RDFType.getInstance();
			if(sparqlFunc.getQuery()==null) throw new RuntimeException("The query was not initialize()'d");
			for (Var var : sparqlFunc.getQuery().getProject().getVars()){
				/* create a String from the variable name */
				String fieldName = var.getVarName();
				recordTypeRow.put(fieldName, rdfType);
			}
			
			correctReturnType = BagType.getInstance(RecordType.getInstance(recordTypeRow));	
		}
		return correctReturnType;
		
	}
}

