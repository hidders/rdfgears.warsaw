package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Map;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.rdf.model.Model;


/**
 * Converts a bag of record to a graph. The records should contain the 's', 'p' and 'o' names, with: 
 * 's' contains a URIValue.
 * 'p' contains a URIValue. 
 * 'o' contains a URIValue or LiteralValue.
 * 
 * Note these URI constraints are not guaranteed by typechecking, as we typecheck only for the RDFValue supertype.
 * 
 * Function returns RGL-NULL if the bag or some record thereof is NULL. 
 * 
 * NULL elements in the (s,p,o) fields of the record gracefully discard the triple from the result graph. 
 * 
 * Ideally, the typing system would be modified so that this function could be largely implemented in the asGraph()
 * function of a bag. But that would require we change the typechecking theory (graph is a theoretical subtype of bag)
 * and the implementatation (create BagType() funtions instead of comparisons with 'instanceof', among other things).  
 */
public class BagToGraph extends SimplyTypedRGLFunction  {
	public static String bag = "bag";
	
	public BagToGraph(){
		TypeRow row = new TypeRow();
		row.put("s", RDFType.getInstance());
		row.put("p", RDFType.getInstance());
		row.put("o", RDFType.getInstance());
		requireInputType(bag, BagType.getInstance(RecordType.getInstance(row))); 
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		RGLValue bagValue = inputRow.get(bag);
		if (bagValue.isNull())
			return bagValue; // return null
		
		BagBackedGraphValue graph = new BagBackedGraphValue(bagValue.asBag());
		
		if (graph.getModel()==null){
			return ValueFactory.createNull(null);
		}
		
		return graph;
	}
	
	@Override
	public void initialize(Map<String, String> config) {
		// nothing to do 
	}

	@Override
	public RGLType getOutputType() {
		return GraphType.getInstance();
	}

}



class BagBackedGraphValue extends GraphValue {
	private Model model; // the model, if generated already.
	
	// if null after initialization, we are actually a NULL value.
	// this overloading is not very useful right now, but it illustrates the concept that we could
	// even postpone model creation upon instantiation, and only have it called when isNull() of isGraph() is 
	// called. 
	private AbstractBagValue bag;    
	
	public BagBackedGraphValue(AbstractBagValue bag){
		this.bag = bag;
		
		try {
			Model tentativeModel = ValueFactory.createModel();
			
			for (RGLValue elem : bag ){
				if (! elem.isNull()){
					AbstractRecordValue rec = elem.asRecord();
					
					RGLValue subj = rec.get("s");
					RGLValue pred = rec.get("p");
					RGLValue obj = rec.get("o");
					
					if (! (subj.isNull() || pred.isNull() || obj.isNull())){
						tentativeModel.add(
								subj.asRDFValue().getRDFNode().asResource(), 
								tentativeModel.createProperty(pred.asRDFValue().getRDFNode().asResource().getURI()), 
								obj.asRDFValue().getRDFNode()
							);	
					} else {
						// ignore this triple, as one of values is NULL so we cannot create it 
					}
				} else {
					Engine.getLogger().warn("Warning, could not convert bag to graph because the bag contains a NULL record");
					return;
				} 
			}	
			
			model = tentativeModel; // success
		} catch (Exception e){
			Engine.getLogger().error("Could not create a graph-model from the bag!");
			Engine.getLogger().error(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see rdfgears.rgl.datamodel.value.GraphValue#getModel()
	 */
	@Override
	public Model getModel(){
		return model;
	}
	
	/**
	 * Return the bag that defined us.
	 * 
	 * Note that currently, isBag() returns false.  
	 */
	public AbstractBagValue asBag(){
		return bag; 
	}

}
