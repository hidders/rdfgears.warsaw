package com.nl.tudelft.rdfgearsUI.client;

import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class RGTypeUtils {
	public RGTypeUtils (){}
	
	public static Element stringToType(String s){
		Element t = (Element) XMLParser.parse(s).getFirstChild();
		return t;
	}
	
	/**
	 * un-wrap type from <type> T </type> -> T and remove white space on first level of the element tree
	 * @param type
	 * @return
	 */
	public static Element unwrap(Element type){
		if(type.getTagName().equalsIgnoreCase("type")){
			Element myT = cleanWhiteSpace(type);
			if(myT.hasChildNodes()){
				NodeList childs = myT.getChildNodes();
				for(int i = 0; i < childs.getLength(); i++){
					if(childs.item(i).getNodeType() == 1){
						return cleanWhiteSpace((Element) childs.item(i));
					}
				}
			}
		}
		
		return getSimpleVarType("alpha");
	}
	/**
	 * remove whitespace from first level child
	 * Input:
	 * <a>
	 * 	<b>
	 * 		<c/>
	 * 	</b>
	 * </a>	
	 * 
	 * output:
	 * <a><b>
	 * 	<c/>
	 * </b></a>
	 * @param e
	 * @return
	 */
	public static Element cleanWhiteSpace(Element e){
		NodeList childs = e.getChildNodes();
		for(int i = 0; i < childs.getLength(); i++){
			//Element c = (Element) childs.item(i);
			if((childs.item(i).getNodeType() == 3) && !childs.item(i).getNodeValue().matches("\\S")){
				e.removeChild(childs.item(i));
				i--;
			}
		}
		
		return e;
	}
	
	public static String getTagName(RGTypeName tn){
		String tagName = "null";
		switch(tn){
		case VARIABLE:
			tagName = "var";
			break;
		case TUPLE:
			tagName = "tuple";
			break;
		case EXTENDIBLE_TUPLE:
			tagName = "etuple";
			break;
		case BOOLEAN:
			tagName = "bool";
			break;
		case INTEGER:
			tagName = "int";
			break;
		case BAG:
			tagName = "bag";
			break;
		}
		return tagName;
	}
	
	public static Element getSimpleVarType(String name){
		Element t = XMLParser.createDocument().createElement("var");
		t.setAttribute("name", name);
		return t;
	}
	
	public static Element getSimpleBoolType(){
		return XMLParser.createDocument().createElement("bool");
	}
	
	public static Element getSimpleIntType(){
		return XMLParser.createDocument().createElement("int");
	}
}
