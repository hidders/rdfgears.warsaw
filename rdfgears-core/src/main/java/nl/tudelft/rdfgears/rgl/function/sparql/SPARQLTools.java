package nl.tudelft.rdfgears.rgl.function.sparql;

import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.Syntax;

public class SPARQLTools {

	/* return true iff the string can be parsed with given syntax */
	public static boolean canParse(String queryString, Syntax syntax) {
		try {
			QueryFactory.create(queryString, syntax);
		} catch (QueryParseException e){
			return false;
		}
		return true;
	}

}
