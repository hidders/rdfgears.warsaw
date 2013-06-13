package nl.tudelft.rdfgears.util.row;

import java.util.Collection;
public class FieldIndexMapFactory {

	/** 
	 * Create a FieldIndexMap from the given two sets of range values.
	 * They MUST be disjoint, otherwise Stuff Will Fail. 
	 */
	public static FieldIndexMap create(Collection<String> range1, Collection<String> range2){
		FieldIndexHashMap fiMap = new FieldIndexHashMap();
		for (String s : range1)
			fiMap.addFieldName(s);
		for (String s : range2)
			fiMap.addFieldName(s);
		
		return fiMap;
	}
	

	/** 
	 * Create a FieldIndexMap from the given two sets of range values.
	 * They MUST be disjoint, otherwise Stuff Will Fail. 
	 */
	public static FieldIndexMap create(Collection<String> range){
		
		// create hashmap  -- seems not much slower than fieldIndexArrayMap 
		FieldIndexHashMap fiMap = new FieldIndexHashMap();
		for(String s: range)
			fiMap.addFieldName(s);
		
		return fiMap;
	}
	

	/** 
	 * Create a FieldIndexMap from an array of fields. Must not contain duplicates/null 
	 */
	public static FieldIndexMap create(String... fields){
		// create hashmap  -- seems not much slower than fieldIndexArrayMap 
		FieldIndexHashMap fiMap = new FieldIndexHashMap();
		for(String s: fields)
			fiMap.addFieldName(s);
		
		return fiMap;
	}

}
	

//	
//
//
//package nl.tudelft.rdfgears.util.row;
//
//import java.util.Collection;
//
//public class FieldIndexMapFactory {
//
//	/** 
//	 * Create a FieldIndexMap from the given two sets of range values.
//	 * They MUST be disjoint, otherwise Stuff Will Fail. 
//	 */
//	public static FieldIndexMap create(Collection<String> range1, Collection<String> range2){
//		String fieldAr[] = new String[range1.size() + range2.size()];
//		int i=0;
//		for (String s: range1){
//			fieldAr[i++] = s;
//		}
//		for (String s : range2){
//			fieldAr[i++] = s;
//		}
//		return new FieldIndexArrayMap(fieldAr);
//	}
//	
//
//	/** 
//	 * Create a FieldIndexMap from the given two sets of range values.
//	 * They MUST be disjoint, otherwise Stuff Will Fail. 
//	 */
//	public static FieldIndexMap create(Collection<String> range){
//		
//		// create hashmap  -- seems not much slower than fieldIndexArrayMap 
////		FieldIndexHashMap fiMap = new FieldIndexHashMap();
////		for(String field : range){
////			fiMap.addFieldName(field);
////		}
////		return fiMap;
//		
//		
////		// create arraymap 
//		String fieldAr[] = new String[range.size()];
//		int i=0;
//		for (String s: range){
//			fieldAr[i++] = s;
//		}
//		return new FieldIndexArrayMap(fieldAr);
//	}
//	
//
//	/** 
//	 * Create a FieldIndexMap from an array of fields. Must not contain duplicates/null 
//	 */
//	public static FieldIndexMap create(String[] fields){
//		// create hashmap  -- seems not much slower than fieldIndexArrayMap 
////		FieldIndexHashMap fiMap = new FieldIndexHashMap();
////		for(String field : fields){
////			fiMap.addFieldName(field);
////		}
////		return fiMap;
//		
//		// create arraymap 
//		return new FieldIndexArrayMap(fields);
//	}
//	
//
//	/**
//	 * Create FieldIndexMap with no fields (for empty row) 
//	 */
//	public static FieldIndexMap create(){
//		return new FieldIndexArrayMap(new String[0]);
//	}
//	
//	/**
//	 * Create FieldIndexMap with 1 field 
//	 */
//	public static FieldIndexMap create(String field1){
//		String[] fields = new String[1];
//		fields[0] = field1;
//		return new FieldIndexArrayMap(fields);
//	}
//
//	/**
//	 * Create FieldIndexMap with 2 fields 
//	 */
//	public static  FieldIndexMap create(String field1, String field2){
//		String[] fields = new String[2];
//		fields[0] = field1;
//		fields[1] = field2;
//		return new FieldIndexArrayMap(fields);
//
//	}
//
//	/**
//	 * Create FieldIndexMap with 3 fields 
//	 */
//	public static FieldIndexMap create(String field1, String field2, String field3){
//		String[] fields = new String[3];
//		fields[0] = field1;
//		fields[1] = field2;
//		fields[2] = field3;
//		return new FieldIndexArrayMap(fields);
//	}
//	
//	/**
//	 * Create FieldIndexMap with 4 fields 
//	 */
//	public static FieldIndexMap create(String field1, String field2, String field3, String field4){
//		String[] fields = new String[4];
//		fields[0] = field1;
//		fields[1] = field2;
//		fields[2] = field3;
//		fields[3] = field4;
//		return new FieldIndexArrayMap(fields);
//	}
//}
//	
//
//	
