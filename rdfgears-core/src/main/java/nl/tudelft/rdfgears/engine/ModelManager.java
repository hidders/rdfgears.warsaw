package nl.tudelft.rdfgears.engine;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.n3.N3JenaWriter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * A manager providing graphs that can be manipulated in the background.
 * 
 * So be very aware that this is for side-effects!  
 * 
 * TODO: make these models persistent on the disk, if possible. That'd be much nicer! 
 * 
 * @author Eric Feliksik
 *
 */
public class ModelManager {
	private Map<String, Model> modelMap = new HashMap<String, Model>(); 

	protected ModelManager(){
		
	}
	
	/**
	 * Returns a buffered writer to the file with given name. 
	 * If the file cannot be opened, a RuntimeException will be thrown.
	 * The writer should *NOT* be closed the the user of this function. 
	 * At the end of the workflow execution Engine.close() will be called
	 * to close it.   
	 * 
	 * @param filename
	 * @return
	 */
	public Model getModel(String modelName){
		Model model = modelMap.get(modelName);
		
		if (model==null){
			model = ModelFactory.createDefaultModel();
			modelMap.put(modelName, model);
		}
		
		return model;
	}
	
	/**
	 * Write models to disk. 
	 * 
	 */
	public void close(){
		/**
		 * use model name as filename 
		 */
		for (String modelName : modelMap.keySet()){
			Model model = modelMap.get(modelName);
			
			String fileName = modelName + ".n3";
			
			RDFWriter rdfWriter = new N3JenaWriter();

			/*  Actually we shouldn't use the fileManager for this, as people
			 * may not be aware that creating a model will create a similarly-named
			 * file (so it's better to create a file here locally).
			 * 
			 * But for now, it will work.
			 */
			Writer writer = Engine.getFileManager().getFileWriter(fileName);
			rdfWriter.write(model, writer, null);
			
			model.close();
		}
	}
}
