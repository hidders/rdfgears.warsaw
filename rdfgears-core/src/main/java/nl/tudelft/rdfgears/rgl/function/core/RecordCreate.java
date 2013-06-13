package nl.tudelft.rdfgears.rgl.function.core;

import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.NNRCFunction;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/** 
 * Create a Record that contains a number of fields. The fields are defined in the initialize() function.
 *   
 * @param fieldNames the names of the ports (and record fields).
 */
public class RecordCreate extends NNRCFunction {
	
	
	public RecordCreate(){
	}
	
	/**
	 * The config map should contain the key "fields", and value should be a ';'-separated list of fieldnames.
	 * Example: 
	 * config.get("fields") => "field1;field2;anotherField" 
	 */
	@Override
	public void initialize(Map<String, String> config) {
		
		/* configure the required inputs based on the ';' separated list of field names */
		String fieldsStr = config.get("fields");
		String[] split = fieldsStr.split(";");
		for (int i=0; i<split.length; i++){
			if (split[i].length()>0)
				requireInput(split[i]);
		}
	}
	

	public RGLValue executeImpl(ValueRow input) {
		/* the ValueRow we want to create a record from, happens to have the same 
		 * set of keys as our input. So we can use our own FieldIndexMap
		 */
		AbstractModifiableRecord rec = ValueFactory.createModifiableRecordValue(this.getFieldIndexMap());
		for (String fieldName : getRequiredInputNames()){
			rec.put(fieldName, input.get(fieldName));
		}
		
		return rec;
	}
	
	@Override
	public RGLType getOutputType(TypeRow inputTypeRow) throws FunctionTypingException {
		TypeRow typerow = new TypeRow();
		
		for (String fieldName : getRequiredInputNames()){
			RGLType type = inputTypeRow.get(fieldName); 
			if (type==null){
				throw new FunctionTypingException("Expected some input on port "+fieldName+". ");
			}
			typerow.put(fieldName, type);
		}
		return RecordType.getInstance(typerow);
	}
}
