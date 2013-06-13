package nl.tudelft.rdfgears.rgl.function.core;

import java.util.Map;
import java.util.Set;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.NNRCFunction;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.FieldIndexMapFactory;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * Record 'join' operator of the Nested Relational Calculus
 * Takes to records, r1 and r2. If for all fields 'A' in the intersection of the ranges of r1 and r2, 
 * it holds that r1(A)==r2(A), then the output is the singleton bag with the record r3 such that dom(r3) 
 * is the union of dom(r1) and dom(r2), and r3(X) is r1(X) (if X in dom(r1)) or r2(X) (if X in dom(r2)) 
 * 
 * @author Eric Feliksik
 *
 */
public class RecordJoin extends NNRCFunction {
	public static final String record1 = "record1"; // a record
	public static final String record2 = "record2"; // a record
	public RecordJoin(){
		this.requireInput(record1);
		this.requireInput(record2);
//		this.isLazy = false; // TODO: research when this is advantageous
	}

	@Override
	public void initialize(Map<String, String> config) {
		/* nothing to be done */
	}
	
	public RGLValue executeImpl(ValueRow input) {
		AbstractRecordValue r1 = input.get(record1).asRecord();
		AbstractRecordValue r2 = input.get(record2).asRecord();

		Set<String> range1 = r1.getRange();
		Set<String> range2 = r1.getRange();
		
		for (String r1name : range1 ){
			if (range2.contains(r1name) &&  ( ! r1.get(r1name).equals(r2.get(r1name)) )){
				/* fields with same name, but different value. Cannot join, so return empty bag */
				return ValueFactory.createBagEmpty(); 
			}
		}
		
		/* ok, all elements in the intersection of the keys are equal. Make a new record. */
		FieldIndexMap fiMap = FieldIndexMapFactory.create(range1, range2);
		AbstractModifiableRecord rec  = ValueFactory.createModifiableRecordValue(fiMap);
		
		for (String key : fiMap.getFieldNameSet()){
			if (range1.contains(key))
				rec.put(key, r1.get(key));
			else // then it must be in r2
				rec.put(key, r2.get(key));
		}
				
		return ValueFactory.createBagSingleton(rec);
	}
	

	@Override
	
	public RGLType getOutputType(TypeRow inputTypeRow) throws FunctionTypingException {
		
		if (! (inputTypeRow.get(record1) instanceof RecordType)) // no fields required for typerow
			throw new FunctionTypingException(record1, RecordType.getInstance(new TypeRow()), inputTypeRow.get(record1));
		
		
		if (! (inputTypeRow.get(record2) instanceof RecordType))
			throw new FunctionTypingException(record2, RecordType.getInstance(new TypeRow()), inputTypeRow.get(record2));
		
		RecordType r1 = (RecordType) inputTypeRow.get(record1);
		RecordType r2 = (RecordType) inputTypeRow.get(record2);
		
		TypeRow typerow = new TypeRow();
		for (String key : r1.getRange())
			typerow.put(key, r1.getFieldType(key));
		
		/* note that the fieldtype of overlapping fields will be overwriten by this 2nd loop.
		 * But the records will only join if the fields (and thus the types) are equal. */
		for (String key : r2.getRange())
			typerow.put(key, r2.getFieldType(key));
		
		RecordType outputRecType = RecordType.getInstance(typerow);
		return BagType.getInstance(outputRecType);
	}
	
	public String toString(){
		return super.toString() + "[record-join]";
	}
//
//	@Override
//	public static RGLFunction loadFromXML(Element functionElement) {
//		return new RecordJoin();
//	}
//	
}
