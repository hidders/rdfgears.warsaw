package nl.tudelft.rdfgears.rgl.datamodel.value.impl;

import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * MemoryGraphValue is not really a correct name, as it is a ModelGraphValue. 
 * The model can be a MemoryModel or an RDBModel... 
 * 
 * @author Eric Feliksik
 *
 */
public class MemoryGraphValue extends GraphValue {
	private Model model;
	public MemoryGraphValue(Model model){
		this.model = model;
	}
	
	
	/**
	 * Give the Graph Model. 
	 * Maybe we shouldn't do this but instead implement a Facade to access the 
	 * Model, in order to enforce the read-only property of RGL values?   
	 * @return
	 */
	@Override
	public Model getModel(){
		return model;
	}

}
