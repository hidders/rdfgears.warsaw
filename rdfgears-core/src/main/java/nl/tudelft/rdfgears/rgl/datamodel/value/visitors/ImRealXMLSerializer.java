package nl.tudelft.rdfgears.rgl.datamodel.value.visitors;

import java.io.OutputStream;
import java.util.Set;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.BooleanValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLNull;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.URIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.workflow.LazyRGLValue;

import com.hp.hpl.jena.rdf.model.RDFWriter;


/**
 * A visitor that serializes an RGL Value, writing it to an Output Stream. 
 * The serializer needs to know the RGL type in advance. If it is a graph, RDF/XML is serialized. 
 * 
 * If it is a Bag of records of RDFValues, SPARQL-SELECT result is serialized. This functionality is implemented by 
 * this visitor.  
 * 
 * Otherwise, RGL/XML is serialized. 
 * 
 * @author Eric Feliksik
 *
 */
public class ImRealXMLSerializer extends ValueSerializer {
	private static XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	IndentingXMLStreamWriter xmlwriter;
	
	OutputStream rawStream;  
	RGLType resultType; 
	
	Set<String> selectFields; 
	
	public ImRealXMLSerializer(RGLType resultType, OutputStream out){
		rawStream = out; 
		this.resultType = resultType; 
	}
	
	private void initWriter() throws XMLStreamException{
		xmlwriter = new IndentingXMLStreamWriter(xmlOutputFactory.createXMLStreamWriter(rawStream));
	}

	public void serialize(RGLValue value) {
		
		if (getSparqlSelectResultValues(resultType)){
			serializeAsSparqlResult(value);	
		} else if(resultType.isGraphType()) {
			RDFWriter rdfWriter = new com.hp.hpl.jena.xmloutput.impl.Basic();
			rdfWriter.write(value.asGraph().getModel(), rawStream, null);
		} else {
			(new ValueXMLSerializer(rawStream)).serialize(value);
		}
		
		
	}
	
	
	private void serializeAsSparqlResult(RGLValue value) {

		try {
			initWriter();
			
		    xmlwriter.writeStartDocument();

			xmlwriter.writeStartElement("sparql");
			xmlwriter.writeStartElement("head");
			
			for (String varname : selectFields){
				xmlwriter.writeEmptyElement("variable");
				xmlwriter.writeAttribute("name", varname);
			}
			
			xmlwriter.writeEndElement(); // </head>
			value.accept(this); // serialize the bag of records

			
			xmlwriter.writeEndElement(); // </sparql>
			
			xmlwriter.flush();
			xmlwriter.close();
			
		} catch (XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/* return whether the type can be modelled as SPARQL SELECT result.
	 * 
	 * If so, set the selectFields (Set<String>) as a side-effect. 
	 *   
	 */
	private boolean getSparqlSelectResultValues(RGLType type) {
		
		if (!type.isBagType())
			return false;
		
		
		BagType btype = (BagType) type;
		
		RGLType belem = btype.getElemType(); 
		if (!belem.isRecordType())
			return false;
		
		RecordType recType = (RecordType) belem;
		
		/* check whether all fields contain RDF values (URI/Literal) */
		for (String field : recType.getRange()){
			if (! recType.getFieldType(field).isRDFValueType()){
				return false;
			}	
		}
		
				
		selectFields = recType.getRange();
		
		return true;
	}

	@Override
	public void visit(AbstractBagValue bag) {
		try {   
			xmlwriter.writeStartElement("results");
			
			for (RGLValue elem : bag){
				elem.accept(this); // serialize record as <result>
			}
			
			xmlwriter.writeEndElement(); // </results>
			
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(GraphValue graph) {
		throw new RuntimeException("not implemented"); // should never be called 
	}
	
	@Override
	public void visit(BooleanValue bool) {
		throw new RuntimeException("not implemented"); // should never be called 
//		// we could, however, simulate True/False values with xsd:boolean RDFValues 
//		try {
//			String s = bool.isTrue() ? "True" : "False";
//			writer.print(s);
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
		
	}
	
	@Override
	public void visit(AbstractRecordValue record) {
		
		try {
			
			if (! record.isNull()){

				xmlwriter.writeStartElement("result");
				
				for (String fieldName : record.getRange()){
					
					RGLValue bindingVal = record.get(fieldName);
					
					if (! bindingVal.isNull()){
						/* include a binding */
						
						/* print <binding name="fieldName"> */
						xmlwriter.writeStartElement("binding");
						xmlwriter.writeAttribute("name", fieldName);
						
						/* print binding value */
						bindingVal.accept(this);
						
						xmlwriter.writeEndElement(); //  </binding>	
					}
					
				}
				
				xmlwriter.writeEndElement(); // </result>
				
			}
			
			
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void visit(URIValue uri) {
		try {
			xmlwriter.writeStartElement("uri");
			xmlwriter.writeCharacters(uri.uriString());
			xmlwriter.writeEndElement(); // </uri>
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void visit(LiteralValue literal) {
		try {
			xmlwriter.writeStartElement("literal");
			if (literal.getLiteralType()!=null){
				xmlwriter.writeAttribute("datatype", literal.getLiteralType().getURI());
			} else if (literal.getLanguageTag()!=null){
				xmlwriter.writeAttribute("lang", literal.getLanguageTag());
			}
			
			xmlwriter.writeCharacters(literal.getValueString());
			xmlwriter.writeEndElement(); // </literal>
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void visit(RGLNull rglNull) {
		throw new RuntimeException("not implemented"); // should never be called 
	}
	
	@Override
	public void visit(LazyRGLValue lazyValue) {
		// we cannot deal with this value, let the value evaluate itself and call this visitor 
		// again with right method signature for OO-dispatching
		lazyValue.accept(this);
	}

}
