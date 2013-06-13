package nl.tudelft.rdfgears.util;

import java.text.ParseException;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;

public class ValueParser {


	/**
	 * Create a typed literal from a literal serialized in N-Triples format (no namespace abbreviation allowed)
	 * 
	 * FIXME TODO : Very simple and inefficient parsing algorithm. Inefficient is no problem currently (only used for workflow parsing), 
	 * but it would be nice to use a more solid one (e.g. from Jena)
	 * 
	 * @param serializedRDFVal
	 * @return
	 */
	public static RGLValue parseNTripleValue(String serializedRDFVal) throws ParseException {
		String str = serializedRDFVal.trim();
		
		char cFirst = str.charAt(0);
		char cLast = str.charAt(str.length()-1);
		if(cFirst=='<'){
			return ValueFactory.createURI( parseBracketedURI(str));
		} else if (cFirst=='"'){
			
			// this is a Literal
			
			/** find the string part */
			int lastQuote = str.lastIndexOf("\""); // last " character must be the end of string, as language tags and URI shouldn't contain these
			if (lastQuote<1) // not found
				throw new ParseException("Literal is not closed by \"", lastQuote);
			
			String stringPart = str.substring(1,lastQuote);
			
			if (str.length()-1 == lastQuote ){ // it is the last character, just a plain literal without language tag
				
				return ValueFactory.createLiteralPlain(stringPart, null);
				
			} else {
				/* we must parse the type / language tag. There is a minimum of two more characters required for the type */
				String typePart = str.substring(lastQuote+1);
				if (typePart.length() < 3)
					throw new ParseException("Cannot parse N-Triples value: '"+str+"', expect a type prefixed with two dakjes ('^^') or an @lang tag ", lastQuote);
				
				if (typePart.charAt(0)=='^'){ // typed literal
					if (typePart.charAt(1)!='^'){
						throw new ParseException("Cannot parse N-Triples value: '"+str+"', expect a type prefixed with two dakjes ('^^') :-) ", lastQuote);
					}
					String typeURI = typePart.substring(2, typePart.length());
					
					String type = parseBracketedURI(typeURI);
					
					/* 
					 * FIXME TODO ugly, fix this. I don't know how to deal with all types! 
					 * 
					 */
					XSDDatatype dtype = getDataTypeFromURI(type);
					
					return ValueFactory.createLiteralTyped(stringPart, dtype);
					
				} else {
					// plain literal 
					String langTag = null;
					if (typePart.charAt(0)=='@'){ // with language tag
						langTag = typePart.substring(1);
					}
					
					return ValueFactory.createLiteralPlain(stringPart, langTag);
				}
			}
			
		} else if (serializedRDFVal.toLowerCase().equals("true")){
			return ValueFactory.createTrue();
		} else if (serializedRDFVal.toLowerCase().equals("false")){
			return ValueFactory.createFalse();
		} else if (serializedRDFVal.toLowerCase().equals("null")){
			return ValueFactory.createNull(null);
		}
		else {
			throw new ParseException("N-Triple encoded node should start with '<' (URI) or '\"' (literal)", 0);
		}
		
		
	}

	private static XSDDatatype getDataTypeFromURI(String uri) throws ParseException {
		XSDDatatype dtype = null;
		if (uri.equals(XSDDatatype.XSDboolean.getURI())){
			dtype = XSDDatatype.XSDboolean;
		} else if (uri.equals(XSDDatatype.XSDdouble.getURI())){
			dtype = XSDDatatype.XSDdouble;
		} else if (uri.equals(XSDDatatype.XSDstring.getURI())){
			dtype = XSDDatatype.XSDstring;
		} else if (uri.equals(XSDDatatype.XSDint.getURI())){
			dtype = XSDDatatype.XSDint;
		} else if (uri.equals(XSDDatatype.XSDinteger.getURI())){
			dtype = XSDDatatype.XSDinteger;
		} else  {
			throw new ParseException("I don't know the datatype "+uri, 0);
		}
		return dtype;
	}

	private static String parseBracketedURI(String str) throws ParseException {
		char cFirst = str.charAt(0);
		char cLast = str.charAt(str.length()-1);
		if(cFirst!='<' || cLast!='>')
			throw new ParseException("Cannot parse N-Triples value: '"+str+"', a URI should start with '<' and end with '>'. ", 0);
		
		return str.substring(1, str.length()-1);
	}

}
