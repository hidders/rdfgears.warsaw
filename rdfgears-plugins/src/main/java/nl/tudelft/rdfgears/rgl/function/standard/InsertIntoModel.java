package nl.tudelft.rdfgears.rgl.function.standard;

import java.util.Map;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.valuefactory.ValueFactoryIface;
import nl.tudelft.rdfgears.rgl.datamodel.type.BooleanType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;


/**
 * A function that inserts a triple into a given model. 
 * It produces side-effects and should therefore not be used at all. 
 * 
 * It expects an s,p,o-record (triple) and a model name. 
 * 
 * @author Eric Feliksik
 *
 */
public class InsertIntoModel extends AtomicRGLFunction  {
	public static String triple = "spo-record";
	public static String modelName = "modelName";
	
	public InsertIntoModel() {
		requireInput(triple);
		requireInput(modelName);
	}
	
	@Override
	public void initialize(Map<String, String> config) {
	}

	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		
		RGLValue mName = inputRow.get(modelName);
		RGLValue tripVal = inputRow.get(triple);
		
		if (mName.isNull() || tripVal.isNull()){
			return ValueFactoryIface.falseValue;
		}
		
		
		AbstractRecordValue rec = tripVal.asRecord();
		// not really stable solution to just getString (URI's won't be convertable to paths)
		// but ok for now. 
		Model model = Engine.getModelManager().getModel(mName.asLiteral().getValueString()); 
		
		RDFNode subj = rec.get("s").asRDFValue().getRDFNode();
		RDFNode pred = rec.get("p").asRDFValue().getRDFNode();
		RDFNode obj = rec.get("o").asRDFValue().getRDFNode();
		model.add(subj.asResource(), model.createProperty(pred.toString()), obj);
		
		return ValueFactoryIface.trueValue;
	}

	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		RGLType tripleType = inputTypes.get(triple);
		
		if (!inputTypes.get(modelName).isSubtypeOf(RDFType.getInstance())){
			throw new FunctionTypingException("An RDF Value must be used to identify the model with");
		}
		
		/* create an spo record type */
		TypeRow spoRow = new TypeRow();
		spoRow.put("s", RDFType.getInstance());
		spoRow.put("p", RDFType.getInstance());
		spoRow.put("o", RDFType.getInstance());
		RecordType spoRecordType = RecordType.getInstance(spoRow);
		
		if (! tripleType.isSubtypeOf(spoRecordType)){
			throw new FunctionTypingException("must receive an s,p,o-record type ");
		}
			
		return BooleanType.getInstance();
		
	}
	

}

