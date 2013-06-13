package nl.tudelft.rdfgears.util;

import java.util.HashMap;
public class FieldName_backup {
	private String name = null;
	private static HashMap<String,String> instances = new HashMap<String,String>();
	private FieldName_backup(String s){
		this.name = s;
	}
	
	/**
	 * Create a fieldname string, using sanity checking.
	 * Give an instance of an AttributeName with name 'name'; reuse an old one if possible.
	 * @param name
	 * @return
	 */
	public static synchronized String create(String name){
		/* fixme: do some sanity checks on the name */
		
		if ( !instances.containsKey(name)) {
			String a = new String(name);
			instances.put(name, a);
			return a;
		}
		return instances.get(name);
	}
	
}
