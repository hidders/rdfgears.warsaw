package com.nl.tudelft.rdfgearsUI.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

enum RGTypeName {
	VARIABLE, //<var name=""/>
	INTEGER, //<int/>
	BOOLEAN, //<bool/>
	TUPLE, //<tuple> <field name=""> T </field> </tuple>
	EXTENDIBLE_TUPLE, // <etuple> <field name=""> T </field> </etuple>
	BAG, // <bag> T </bag>
	NULL //no inner type
}

public class RGType {
	private Element typeInXml; //keep the original type in xml format
	private Element substitutedType; //holder to the type after unifications
	//private Element e; //type's xml element
	private RGTypeName typeName;
	private String varName = ""; //for vatriable type
	private RGType subType = null;
	private ArrayList <RGType> varValues = new ArrayList <RGType>();
	private Map <String, RGType> tupleFields = null;
	private ArrayList <String> tupleFieldNames = new ArrayList <String>();
	private RGType parentTuple; //to detect cyclic tuple type
	private boolean iterate = false;
	
	public RGType(){
		typeInXml = XMLParser.createDocument().createElement("null");
		substitutedType = XMLParser.createDocument().createElement("null");
		typeName = RGTypeName.NULL;
		//e = XMLParser.createDocument().createElement("null");
	}
	
	public RGType(RGType t){
		typeInXml = (Element) t.getBaseType().cloneNode(true);
		substitutedType = (Element) t.getElement().cloneNode(true);
		parseType();
	}
	
	public RGType(String xmlType){
		typeInXml = (Element) XMLParser.parse(xmlType).getFirstChild();
		substitutedType = (Element) XMLParser.parse(xmlType).getFirstChild();
		parseType();
	}
	
	public RGType(Element xmlType){
		typeInXml = xmlType;
		substitutedType = (Element) typeInXml.cloneNode(true);
		parseType();
	}
	
	public RGType(Element xmlType, boolean iterate){
		typeInXml = xmlType;
		substitutedType = (Element) typeInXml.cloneNode(true);
		
		if(iterate)
			substitutedType = (Element) XMLParser.createDocument().createElement("bag").appendChild(substitutedType);
		
		parseType();
	}
	
	private void parseType(){
		String tString = substitutedType.getTagName();
		if(tString.equalsIgnoreCase(RGTypeUtils.getTagName(RGTypeName.VARIABLE))){
			varName = substitutedType.getAttribute("name");			
			typeName = RGTypeName.VARIABLE;
		}else if(tString.equalsIgnoreCase(RGTypeUtils.getTagName(RGTypeName.INTEGER))){
			typeName = RGTypeName.INTEGER;
		}else if(tString.equalsIgnoreCase(RGTypeUtils.getTagName(RGTypeName.BOOLEAN))){
			typeName = RGTypeName.BOOLEAN;
		}else if(tString.equalsIgnoreCase(RGTypeUtils.getTagName(RGTypeName.TUPLE))){
			typeName = RGTypeName.TUPLE;
		}else if(tString.equalsIgnoreCase(RGTypeUtils.getTagName(RGTypeName.EXTENDIBLE_TUPLE))){
			typeName = RGTypeName.EXTENDIBLE_TUPLE;
		}else if(tString.equalsIgnoreCase(RGTypeUtils.getTagName(RGTypeName.BAG))){
			typeName = RGTypeName.BAG;
		}else{
			typeName = RGTypeName.NULL;
		}
	}
	
	public RGTypeName getTypeName(){
		return typeName;
	}
	
	public String getVarName(){
		return varName;
	}
	
	public void setParentTuple(RGType t){
		if(t.getTypeName() == RGTypeName.TUPLE || t.getTypeName() == RGTypeName.EXTENDIBLE_TUPLE){
			parentTuple = t;
		}
	}
	
	public RGType getParentTupe(){
		return parentTuple;
	}
	
	public Element getElement(){
		Element e;
		Document doc = XMLParser.createDocument();
		if(typeName == RGTypeName.BAG){
			e = doc.createElement("bag");
			if(getSubType().getTypeName() != RGTypeName.NULL){
				e.appendChild(getSubType().getElement());
				return e;
			}
		}else if(typeName == RGTypeName.TUPLE){
			e = doc.createElement(RGTypeUtils.getTagName(RGTypeName.TUPLE));
			ArrayList <String> fNames = getTupleFieldNames();
			for(String fName: fNames){
				Element f = doc.createElement("field");
				f.setAttribute("name", fName);
				RGType ft = getTupleFieldType(fName);
				f.appendChild(ft.getElement());
				e.appendChild(f);
			}
			return e;
		}else if(typeName == RGTypeName.EXTENDIBLE_TUPLE){
			e = doc.createElement(RGTypeUtils.getTagName(RGTypeName.EXTENDIBLE_TUPLE));
			ArrayList <String> fNames = getTupleFieldNames();
			for(String fName: fNames){
				Element f = doc.createElement("field");
				f.setAttribute("name", fName);
				RGType ft = getTupleFieldType(fName);
				f.appendChild(ft.getElement());
				e.appendChild(f);
			}
			return e;
		}
		//else
		return substitutedType;
	}
	
	public Element getBaseType(){
		return typeInXml;
	}
	
	public RGType getSubType(){
		if(subType == null){
			if(typeName == RGTypeName.BAG && substitutedType.hasChildNodes()){
				NodeList childs = substitutedType.getChildNodes();
				for(int i = 0; i < childs.getLength(); i++){
					try{
						if(childs.item(i).getNodeType() == 1){
							Element c = (Element) childs.item(i);
							String cs = c.getTagName();
							if(cs.equalsIgnoreCase("var") || 
							   cs.equalsIgnoreCase("int") ||
							   cs.equalsIgnoreCase("bool") || 
							   cs.equalsIgnoreCase("tuple") || 
							   cs.equalsIgnoreCase("etuple") || 
							   cs.equalsIgnoreCase("bag")){
								
								subType = new RGType(c);	
								return subType;
							}
						}
					}catch (Exception e){}
				}
				//no matching pattern
				if(subType == null) 
					subType = new RGType();
			}else{
				subType = new RGType(); //return RGTypeName.NULL
			}
		}
		
		return subType;
	}
	
	public Map <String, RGType> getTupleFields(){
		if(tupleFields == null && (typeName == RGTypeName.TUPLE || typeName == RGTypeName.EXTENDIBLE_TUPLE)){
			tupleFields = new HashMap <String, RGType>();
			
//			NodeList fields = substitutedType.getElementsByTagName("field");
			NodeList fields = substitutedType.getChildNodes();
			for(int i = 0; i< fields.getLength(); i++){
				if(fields.item(i).getNodeType() == 1){
					Element field = (Element) fields.item(i);
					if(field.getTagName().equalsIgnoreCase("field")){
						Element f = RGTypeUtils.cleanWhiteSpace((Element) fields.item(i));
						if(f.hasChildNodes()){
							tupleFields.put(f.getAttribute("name"), new RGType((Element)f.getFirstChild()));
						}else{
							tupleFields.put(f.getAttribute("name"), new RGType());
						}
						tupleFieldNames.add(f.getAttribute("name"));
					}
				}
			}
		}
		return tupleFields;
	}
	
	public ArrayList <String> getTupleFieldNames(){
		if(tupleFields == null){
			getTupleFields();
		}
		return tupleFieldNames;
	}
	public RGType getTupleFieldType(String fieldName){
		if(getTupleFields().containsKey(fieldName)){
			return tupleFields.get(fieldName);
		}
		
		return new RGType(); //return NULL type
	}
	/**
	 * add r fields to this tuple
	 * @param r
	 */
	public void mergeTupleWith(RGType r){
		ArrayList <String> rFNames = r.getTupleFieldNames();
		Map <String, RGType> rFields = r.getTupleFields();
		for(String fN: rFNames){
			if(!getTupleFieldNames().contains(fN)){
				addTupleField(fN, rFields.get(fN));
			}
		}
	}
	
	public boolean addTupleField(String fName, RGType fType){
		if(typeName == RGTypeName.TUPLE || typeName == RGTypeName.EXTENDIBLE_TUPLE){
			if(!getTupleFields().containsKey(fName)){
				tupleFields.put(fName, fType);
				tupleFieldNames.add(fName);
				Element f = XMLParser.createDocument().createElement("field");
				f.setAttribute("name", fName);
				f.appendChild(fType.getElement());
				substitutedType.appendChild(f);
			}
		}
		
		return false;
	}
	/**
	 * method to manipulate variable
	 * @return
	 */
	public RGType getVal(){
		if((typeName == RGTypeName.VARIABLE) && (varValues.size() > 0)){
			return varValues.get(varValues.size() - 1);
		}
		
		return null;
	}
	
	public void substitute(RGType v){
		//varValues.add(v);
		substitutedType = (Element) v.getElement();
		parseType();
		subType = null;
		tupleFields = null;
		tupleFieldNames.clear();
	}
	
	/**
	 * restore the type value to the original value
	 * 
	 */
	public void revert(){
		substitutedType = (Element) typeInXml.cloneNode(true);
		
		if(iterate){
			Element st = substitutedType;
			substitutedType = (Element) XMLParser.createDocument().createElement("bag");//.appendChild(substitutedType);
			substitutedType.appendChild(st);
		}
		
		parseType();
		tupleFields = null;
		tupleFieldNames.clear();
		subType = null;
	}
	public void setIterate(boolean v){
		iterate = v;
	}
	/**
	 * undo last substitution
	 */
	public void undo(){
		if(varValues.size() > 0)
			varValues.remove(varValues.size() - 1);
	}
	
	public String toString(){
		return typeName.toString();
	}
}
