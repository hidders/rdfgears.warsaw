package nl.tudelft.rdfgears.rgl.datamodel.type;

import java.util.Set;

import nl.tudelft.rdfgears.util.row.TypeRow;


public class RecordType extends RGLType {
	private TypeRow row = null;
	private RecordType(TypeRow row){
		this.row = row;
	}
	/* FIXME
	 * Singleton to allow returning the same RecordType for the same row type, if we may want this later
	 * Now it's not an effective singleton... 
	 */
	public static synchronized RecordType getInstance(TypeRow row){
		return new RecordType(row);
	}
	
	/**
	 * fixme: maybe not return the row, but give accessor methods for the row?
	 * @return
	 */
	private TypeRow getTypeRow(){
		return row;
	}
	
	public Set<String> getRange(){
		return row.getRange();
	}
	
	public RGLType getFieldType(String String){
		return  getTypeRow().get(String);
	}
	
	public boolean equals(Object that){
		if (that instanceof RecordType){
			return this.getTypeRow().equals(((RecordType)that).getTypeRow());
		}
		return false;
	}
	@Override
	public boolean isType(RGLType type) {
		// TODO Auto-generated method stub
		return this.equals(type);
	}
	

	/**
	 * A Record is a supertype of an otherType if otherType is a RecordType, and 
	 * the TypeRow of this Record is a superType of the other records TypeRow. 
	 *   
	 */
	@Override
	public boolean isSupertypeOf(RGLType otherType) {
		if(otherType instanceof RecordType){
			return this.getTypeRow().isSupertypeOf(((RecordType) otherType).getTypeRow());
		}
		return false;
	}
	


	@Override
	public boolean isRecordType(){
		return true;
	}

	
	public String toString(){
		// types are not SO deeply nested, so no need to recursively use the StringBuilder
		StringBuilder builder = new StringBuilder();
		builder.append("Record(< ");
		
		for (String field: getRange()){
			builder.append(field);
			builder.append(":");
			builder.append(row.get(field));
			builder.append(", ");
		}
		
		builder.append(" >)");
		return builder.toString();
	}
	
}
