package nl.tudelft.rdfgears.rgl.function.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.OrderedBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.StreamingBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.exception.FunctionTypingException;
import nl.tudelft.rdfgears.rgl.function.AtomicRGLFunction;
import nl.tudelft.rdfgears.util.ArrayIterator;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.FieldIndexMapFactory;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * Receives a bag of records and a comparator that compares on certain fields, e.g. a bag of type 
 * 		Bag(Record(< f1:RDFValueType, f2:RDFValueType, f3:RDFValueType, f4:RDFValueType>))
 * grouped on fields f1, f2, would return a bag of type:
 * 		Bag(Record(< f1:RDFValueType, f2:RDFValueType, group:Record<f1:RDFValueType, f2:RDFValueType, f3:RDFValueType, f4:RDFValueType>>))
 * 
 * So note that the records are still complete (containing all fields). 
 * 
 * @author Eric Feliksik
 *
 */
public class BagGroup extends AtomicRGLFunction {
	public static final String INPUT_PORT_NAME = "bag_of_records"; // a record
	public static final String GROUP_FIELD = "group"; // a record
	public static final String CONFIG_GROUP_BY_FIELD = "groupByField";
	
	private RGLType requiredType;
	
	private String[] groupingFields; // the list of fields on which input bag is grouped. That means excluding the 'group' field.  
	private FieldIndexMap fiMapWithGroupField; // fieldmap for all fields, including the 'group' fields. 
	private RecordComparator comp; 
	
	
	public BagGroup(){
	}

	/**
	 * config requires a field 'groupFields' mapping to a String, which is a ";" separated list of fieldNames that 
	 * should together be used for grouping.  
	 */
	@Override
	public void initialize(Map<String, String> config) {
		requireInput(INPUT_PORT_NAME);
		
		String fieldsStr = config.get(CONFIG_GROUP_BY_FIELD);
		if (fieldsStr==null){
			throw new RuntimeException("Need a groupFields initialization parameter");
		}
		groupingFields = fieldsStr.split(";");
		comp  = new RecordComparatorVarField(groupingFields); 
		
		/* create a fieldIndexMap for records containg the grouped fields, and a "group" field mapping to a bag of records of that group */
		String[] outputRecordFields = Arrays.copyOf(groupingFields, groupingFields.length + 1);
		outputRecordFields[outputRecordFields.length - 1 ] = GROUP_FIELD; 
		fiMapWithGroupField = FieldIndexMapFactory.create(outputRecordFields);
		
		/** configure the required type */
		TypeRow typeRow = new TypeRow();
		for (String field : groupingFields){
			typeRow.put(field, RDFType.getInstance());
		}
		requiredType = BagType.getInstance(RecordType.getInstance(typeRow));
		
	}
	
	

	@Override
	public RGLValue executeImpl(ValueRow inputRow) {
		RGLValue input = inputRow.get(INPUT_PORT_NAME);
		if (input.isNull())
			return input; // return null
		GroupedBag groupedBag = new GroupedBag(input.asBag());
		return groupedBag;
	}
	

	@Override
	public RGLType getOutputType(TypeRow inputTypes) throws FunctionTypingException {
		RGLType givenType = inputTypes.get(INPUT_PORT_NAME);
		if (! givenType.isSubtypeOf(requiredType)){
			throw new FunctionTypingException(INPUT_PORT_NAME, requiredType, givenType);
		}
		
		RecordType recordType = (RecordType) ((BagType)givenType).getElemType();
		
		/** configure the required type */
		TypeRow typeRow = new TypeRow();
		for (String field : groupingFields){
			typeRow.put(field, RDFType.getInstance());
		}
		typeRow.put(GROUP_FIELD, BagType.getInstance(recordType)); // put the entire record in the grouped field
		return BagType.getInstance(RecordType.getInstance(typeRow));
	}

	/**
	 * @author Eric Feliksik
	 * 
	 */
	class GroupedBag extends OrderedBagValue {
		private AbstractRecordValue[] records;
		private int fromIndex, toIndex; // range of records relevant to us. toIndex is exclusive, thus may not be valid index. 
		private int size = -1; // not yet determined. 
		
		public GroupedBag(AbstractBagValue bag) {
			if (bag instanceof GroupedBag){
				/* We have to provide a grouped view (grouped by field X) on a subsection of an array that was already 
				 * sorted on another record field Y.   
				 * We could use the GroupedBag's internal records array to sort only the relevant part, as they all have the 
				 * same Y value anyway, and we only have to sort that subsection.  
				 * But this will go wrong in very specific circumstances, namely when other ArrayIterators are instantiated that are 
				 * iterating over this part.
				 */
			}
			if (true){ // if we cannot reuse parent array  
				ArrayList<AbstractRecordValue> recordList = new ArrayList<AbstractRecordValue>();
				for (RGLValue val : bag){
					if (val.isNull()){
						/* null records are unexpected, although a SPARQL-SELECT result record may null-bindings */  
						Engine.getLogger().warn("In grouping, we found a NULL record. This is not expected, at least not from a SPARQL query...");
					} else {
						recordList.add(val.asRecord());
					}
				}
				records = new AbstractRecordValue[recordList.size()]; 
				recordList.toArray(records); /* duplicates memory consumption, but the ArrayList conveniently scaled for us while we didn't knew result size */
				
				/* use entire array... from/toIndex would be useful if we could re-use partially sorted array from parent GroupedBag */
				fromIndex = 0; 
				toIndex = records.length; 
			}
			
			Arrays.sort(records, fromIndex, toIndex, comp);
		}
		
		/* we don't really do complete bagcaching, but we cache the bag we read (in sorted array) so reconstruction is cheap */ 
		public boolean benefitsFromMaterialization(){
			return true; 
		}
		
		@Override
		public Iterator<RGLValue> iterator() {
			return new GroupedBagIterator();
		}
		
		/**
		 * Calculate size, not remembering /generating output. 
		 */
		@Override
		public int size() {
			if (size < 0){
				int finger = fromIndex; 
				size = 0;
				AbstractRecordValue comparableRecord = records[finger]; // we will make a bag of values that all have the same groupable-variables as this
				finger++; // we already check that one :-)
				while(finger < toIndex){
					AbstractRecordValue rv = records[finger++];
					if (!(comp.compare(comparableRecord, rv)==0)){ //  rv is in different group
						size++;
						comparableRecord = rv;
					}
				}
			}
			return size; 
		}
		
		class GroupedBagIterator implements Iterator<RGLValue> {
			/* small FIXME: 
			 * make this iterator count, and set size if iterating finished. 
			 */
			private int groupFromIndex = fromIndex;
			
			@Override
			public boolean hasNext() {
				return groupFromIndex < toIndex;
			}

			@Override
			public AbstractRecordValue next() {
				
				AbstractRecordValue comparableRecord = records[groupFromIndex]; // make a bag of values that all have the same groupable-variables as this
				
				// find first index not in our group. comparableRecord is by definition in our group
				int groupToIndex = groupFromIndex+1;  
				
				while(groupToIndex < toIndex){
					AbstractRecordValue rv = records[groupToIndex];
					if (comp.compare(comparableRecord, rv)!=0) //  rv is in different group
						break;
					
					groupToIndex++;
				}
				AbstractModifiableRecord modRec = ValueFactory.createModifiableRecordValue(fiMapWithGroupField);
				
				/* fill grouping values */
				for (String groupingKey : groupingFields ){
					modRec.put(groupingKey, comparableRecord.get(groupingKey));
				}
				
				/* fill 'group' field */
				modRec.put(GROUP_FIELD, new SubBag(records, groupFromIndex, groupToIndex));
				
				groupFromIndex = groupToIndex; // next group starts where this one ended
				
				return modRec; 
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("not implemented");
			}
		}


		@Override
		public void prepareForMultipleReadings() {
			// nothing to do: we are prepared as we store all elements from the input bag in a local array.
		}
	}
}


/**
 * Subbag is a generic bag that takes an array of elements and iterates only over the elements
 * from fromIndex to toIndex, exclusive.  
 *  
 * @author af09017
 *
 */
class SubBag extends StreamingBagValue {
	private int fromIndex;
	private int toIndex;
	private RGLValue[] records;

	public SubBag(RGLValue[] records, int fromIndex, int toIndex){
		this.records = records;
		this.fromIndex = fromIndex;
		this.toIndex = toIndex;
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Iterator<RGLValue> getStreamingBagIterator() {
		return new ArrayIterator(records, fromIndex, toIndex);
	}

	@Override
	public int size() {
		return toIndex - fromIndex;
	}	
}


/**
 * Compare records based on the equality of the entries, given a number of relevant fieldNames to compare
 * @author af09017
 *
 */
interface RecordComparator extends Comparator<AbstractRecordValue> {
}

/**
 * 
 * Compare record on any number of field names. 
 * @author Eric Feliksik
 *
 */
class RecordComparatorVarField implements RecordComparator {
	String[] compareFields;
	public RecordComparatorVarField(String[] compareFields){
		if (compareFields == null || compareFields.length==0)
			throw new IllegalArgumentException("compareFields array cannot be null or 0-size");
		this.compareFields = compareFields;
	}
	
	@Override
	public int compare(AbstractRecordValue o1, AbstractRecordValue o2) {
		for (int fieldNr = 0; fieldNr < compareFields.length; fieldNr++){
			RGLValue v1 = o1.get(compareFields[fieldNr]);
			RGLValue v2 = o2.get(compareFields[fieldNr]);
			int cmp = v1.compareTo(v2);
			if (cmp!=0 ){
				return cmp; // found a relevant field with unequal entry
			}
		}
		return 0; // all relevant fields contain equal entries 
	}
}

