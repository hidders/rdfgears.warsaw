package nl.tudelft.rdfgears.rgl.function.core;

import java.util.Map;

import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.NNRCFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

public class RecordProject extends NNRCFunction {
	public static final String CONFIGKEY_PROJECTFIELD = "projectField";
	public static final String INPUT_NAME  = "record"; // name of input variable 
	
	private String fieldName; // the field we project
	public RecordProject(){
		this.requireInput(INPUT_NAME);
	}
	
	@Override
	public void initialize(Map<String, String> config) {
		this.fieldName = config.get(CONFIGKEY_PROJECTFIELD);
	}
	
	public RGLValue executeImpl(ValueRow input) {
		RGLValue r = input.get(INPUT_NAME);
		
		if (r.isNull())
			return r; // return the NULL value
		
		return r.asRecord().get(fieldName);
	}
	
	@Override
	public RGLType getOutputType(TypeRow inputTypeRow) throws FunctionTypingException {
		RGLType actualInputType = inputTypeRow.get(INPUT_NAME);
		
		TypeRow typeRow = new TypeRow();
		typeRow.put(fieldName, new SuperType());
		RGLType requiredInputType = RecordType.getInstance(typeRow);
		
		if (actualInputType.isSubtypeOf(requiredInputType)){
			RGLType fieldType = ((RecordType)actualInputType).getFieldType(fieldName);
			assert(fieldType!=null);
			return fieldType;
		} else {
			throw new FunctionTypingException(INPUT_NAME, requiredInputType, actualInputType);
		}
	}
}
