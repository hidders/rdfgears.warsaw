package nl.tudelft.rdfgears.rgl.datamodel.value;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;
import nl.tudelft.rdfgears.rgl.exception.ComparisonNotDefinedException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class GraphValue extends DeterminedRGLValue {
	static String uriBase = "http://wis.ewi.tudelft.nl/RdfGears/graphURI/";
	static int uriCounter = 0;
	
	private Resource uriNode; // a random URI to determine this graph
	private Model model;
	
	public Resource getURI(){
		return uriNode;
	}
	
	/** 
	 * Get the ARQ Model for this RGL Graph.
	 * Note that 
	 * Note that this may be a heavy call if the RGL Graph is a virtual remote graph, as the entire
	 * graph then has to be downloaded. 
	 * 
	 * It is better to call getModel(Query query), and let the implementation determine whether it can/should 
	 * optimize the model to return. 
	 * @return
	 */
	public Model getModel(){
		return model;
	}
	
	public GraphType getType() {
		return GraphType.getInstance();
	}

	
	public synchronized RDFNode getRDFNode() {
		if (uriNode==null){
			/* make a random identifier */
			uriNode = Engine.getDefaultModel().createResource(uriBase + uriCounter);
			uriCounter++;	
		}
		return uriNode;
	}

	@Override
	public GraphValue asGraph(){
		return this;
	}
	
	/**
	 * Convert this graph value to an iterable bag of records
	 */
	@Override
	public AbstractBagValue asBag(){
		assert(false) : "not implemented yet, but it's easy to do ";
		return null;
	}
	
	@Override
	public boolean isBag(){
		return false; // may become true
	}
	
	@Override
	public boolean isGraph(){
		return true; // may become true
	}
	
	

	/** 
	 * load the data in the model to contain the patterns described in query under the GRAPH { } clause identified
	 * with varname graphClauseVarName. 
	 * 
	 * This method does nothing, unless it is overridden by a subclass. 
	 * @param query
	 * @param graphClauseVarName
	 */
	public void loadDataForQuery(Query query, String graphClauseVarName){
		/* nothing to do by default */
		
		// FIXME: REMOVE if unused
	}
	
	public void accept(RGLValueVisitor visitor){
		visitor.visit(this);
	}
	

	public int compareTo(RGLValue v2) {
		// but may be implemented by subclass. It must be determined what is comparable, i think it'd be elegant to make as much as possible comparable.
		throw new ComparisonNotDefinedException(this, v2);
	}
	

	@Override
	public void prepareForMultipleReadings() {
		/* nothing to do */
	}

}
