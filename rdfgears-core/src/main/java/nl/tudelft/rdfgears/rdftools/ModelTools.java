package nl.tudelft.rdfgears.rdftools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class ModelTools {

	public static boolean equalModels(Model a, Model b){
		
		boolean equal = a.size()==b.size();
		if (! equal)
			return false;
		
		StmtIterator iter = a.listStatements();
		while (iter.hasNext()){
			Statement s = iter.next();
			if (!b.contains(s)){
				System.out.println("Missing statement in 2nd dataset: "+s);
				return false;
			}
		}
		return equal;
	}
	
	public static void printModel(Model m){ 
		StmtIterator statements = m.listStatements();
		int counter = 0;
		while(statements.hasNext()){
			Statement sm = statements.next();
			System.out.println(sm);
		}
		System.out.println("Printed "+m.size()+" elements of model. ");
		
	}
	
	public static void writeModel(Model model, String fileName){
		try {
			Writer writer = new FileWriter(fileName);
			model.write(writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Replace blank nodes; hopefully we don't have to do this with this function, 
	 * but can we do it with a custom ARQ function instead. 
	 * @param in
	 * @return
	 */
	public static Model replaceBlankNodes(Model in){
		
		String filterBlank = " CONSTRUCT { \n" +
							" #  ?s ?p ?o. \n " +
							"   ?s_iri ?p ?o_iri. \n " +
							" } \n" +
							" WHERE { \n " +
							"     ?s ?p ?o. \n " +
							"     FILTER (isBlank(?s) || isBlank(?o)) \n " +
							" 	  BIND(afn:createIRI(?s) AS ?s_iri) \n " +
							" 	  BIND(afn:createIRI(?o) AS ?o_iri) \n " +
							" } \n ";
		
		String filterNotBlank = " CONSTRUCT { \n" +
							"   ?s ?p ?o. \n " +
							" } \n" +
							" WHERE { \n " +
							"     ?s ?p ?o. \n " +
							"     FILTER (!(isBlank(?s) || isBlank(?o)) ) \n " +
							" } \n ";

		
		Query fBlankQ = QueryFactory.create(filterBlank) ;
		QueryExecution qBlankExec = QueryExecutionFactory.create(fBlankQ, in);
		
		Model blankModel = qBlankExec.execConstruct(); 
		
		Query fNotBlankQ = QueryFactory.create(filterNotBlank) ;
		QueryExecution qNotBlankExec = QueryExecutionFactory.create(fNotBlankQ, in);
		
		Model notBlankModel = qNotBlankExec.execConstruct();
		
		System.out.println(" Non-blanknodes has size "+notBlankModel.size());
		
		Model res = blankModel.add(notBlankModel); 
		
		System.out.println(" Total has size "+res.size());
		return res ;
	}
	
	public static void writeGraphImpression(Model m){
		writeGraphImpression(m, 20);
	}
	

	public static void writeRegularGraphImpression(Model m, int maxTriples){
		
	}
	
	/** 
	 * Count the number of marked nodes in a model, and return the integer. 
	 * It is a very slow method! 
	 * Improvement: 
	 *  - remember parsed query (will not help much)
	 *  - count via Jena API, if possible (hopefully faster)
	 */
	private static int countMarkedNodes(Model m){
		String countQueryStr = " SELECT ( COUNT(?markedNode) AS ?count) WHERE { "+
		//"<"+GearsFactory.getRootNode().getURI()+"> "+ // subject
		//"<"+GearsFactory.getMarkerProperty().getURI()+"> "+ // predicate
		"?markedNode  } ";
		Query query = QueryFactory.create(countQueryStr) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, m);
		
		int markedElements;
		try {
			  ResultSet results = qexec.execSelect() ;
			  markedElements = results.next().get("count").asLiteral().getInt();
		} finally { 
			  qexec.close() ; 
		}
		return markedElements;
	}
	
	/*
	 * Write a bit of info about a graph, with maxLines lines of graph data (excl. empty lines). 
	 */
	public static void writeGraphImpression(Model m, int maxLines){
		if (m.size()==0){
			System.out.println("(empty graph)");
			return;
		}
		
		int markedCount = countMarkedNodes(m);
		if (markedCount==0){
			/* this is not an annotated graph. Process it as a regular graph */ 
			System.out.println("Regular graph with "+m.size()+" triples."); 
		} else {
			/* print annotated graph info */
			System.out.println("Annotated graph with "+markedCount+" marked nodes, "+m.size()+" triples.");
		}
		PartialModelWriter.writePartialModel(m, maxLines, "| ");
		if (true) 
			return;
		
		assert(false) : "the rest doesn't compile anymore ";
		
		/*
		
		//NodeIterator markedNodes = m.listObjectsOfProperty(GearsFactory.getRootNode(), GearsFactory.getMarkerProperty());
		
		// print annotated graph impression 
		Model newModel = ModelFactory.createDefaultModel();
		newModel.setNsPrefix("rdfgears", com.hp.hpl.jena.sparql.function.library.createIRI.namespace);
		
		int printedTriples = 0;
		boolean loop = true;
		while (markedNodes.hasNext() && loop ){
			RDFNode node = markedNodes.next();
			StmtIterator tripleIter = m.listStatements(node.asResource(),  (Property) null, (RDFNode) null);
			while(tripleIter.hasNext() && loop ){
				Statement triple = tripleIter.next();
				newModel.add(triple);
				
				printedTriples++;
				loop = printedTriples < maxLines;
			}
			tripleIter.close();
		}
		
		markedNodes.close();
		
		PartialModelWriter.writePartialModel(m, maxLines, "| ");
		*/
	}
	
}
