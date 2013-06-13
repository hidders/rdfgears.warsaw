package nl.tudelft.rdfgears.tests;

import java.io.FileInputStream;
import java.util.HashMap;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Provide data for testing
 * 
 * @author Eric Feliksik
 *
 */
public class Data {
	
	private static HashMap<String, GraphValue> loadedGraphs = new HashMap<String, GraphValue>();
	public static String movieConstructQuery = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"PREFIX movie: <http://data.linkedmdb.org/resource/movie/> \n" +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
		"CONSTRUCT { \n" +
		"    ?director rdf:type movie:director . \n" +
		"    ?director movie:director_name ?name . \n" +
		"    ?director rdfs:label ?label . \n" +
		"} \n" +
		"WHERE { \n" +
		"	GRAPH $graph1 {" +
		"     ?director rdf:type movie:director . \n" +
		"     ?director movie:director_name ?name . \n" +
		"     ?director rdfs:label ?label . \n" +
		"   }" +
		"} \n" +
		"LIMIT 100";

	public static String movieSelectQuery = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"PREFIX movie: <http://data.linkedmdb.org/resource/movie/> \n" +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
		" SELECT ?director ?name ?label \n"+
		"WHERE { \n" +
		"	GRAPH $graph1 {" +
		"     ?director rdf:type movie:director . \n" +
		"     ?director movie:director_name ?name . \n" +
		"     ?director rdfs:label ?label . \n" +
		"   }" +
		"} \n" +
		"LIMIT 100";
	
	public static String dbpediaSelectQuery = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"PREFIX dc: <http://purl.org/dc/terms/> \n" +
		"PREFIX dbpedia: <http://dbpedia.org/ontology/> \n" +
		"PREFIX movie: <http://data.linkedmdb.org/resource/movie/> \n" +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
		"SELECT ?film ?director ?label \n"+
		"WHERE { \n" +
		"	GRAPH $graph1 { \n" +
		"      ?film rdf:type dbpedia:Film . \n" +
		"      ?film dbpedia:director ?director . \n" +
		"      OPTIONAL { \n " +
		"          ?director rdfs:label ?label . \n" +
		"      }\n " +
		"	} \n" +
		"} \n" +
		"LIMIT 100";
	
	public static String dbpediaConstructQuery = 
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		"PREFIX dc: <http://purl.org/dc/terms/> \n" +
		"PREFIX dbpedia: <http://dbpedia.org/ontology/> \n" +
		"PREFIX movie: <http://data.linkedmdb.org/resource/movie/> \n" +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n" +
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
		"CONSTRUCT {\n"+
		"      ?film rdf:type dbpedia:Film . \n" +
		"      ?film dbpedia:director ?director . \n" +
		"      ?director rdfs:label ?label . \n" +
		"} \n " +
		"WHERE { \n" +
		"	GRAPH $graph1 { \n" +
		"      ?film rdf:type dbpedia:Film . \n" +
		"      OPTIONAL { \n " +
		"          ?film dbpedia:director ?director .\n" +
		"      } \n" +
		"      OPTIONAL { \n " +
		"          ?director rdfs:label ?label . \n" +
		"      }\n " +
		"	} \n" +
		"} \n" +
		"LIMIT 20";
	

	public static GraphValue getGraphFromFile(String fileName){
		if (fileName.charAt(0)=='.'){
			fileName = Engine.startupPath + "/"+fileName;
		}
		GraphValue graph = loadedGraphs.get(fileName);
		if (graph==null){
			Model returnModel;
			try {
				returnModel = ValueFactory.createModel().read(new FileInputStream(fileName), "http://dont_care");
			}
			catch (java.io.FileNotFoundException e){
				e.printStackTrace();
				System.out.println("No file found: "+fileName);
				throw new RuntimeException(e.getMessage());
			}
			catch (Exception e){
				System.out.println("Unexpected Exception: ");
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}

			graph = ValueFactory.createGraphValue(returnModel);
			loadedGraphs.put(fileName, graph);
		}
		
		return graph;
		
	}
}
