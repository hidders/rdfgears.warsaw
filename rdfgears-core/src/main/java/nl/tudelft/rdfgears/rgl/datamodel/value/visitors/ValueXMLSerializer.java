package nl.tudelft.rdfgears.rgl.datamodel.value.visitors;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

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
 * 
 * @author Eric Feliksik
 *
 */
public class ValueXMLSerializer extends ValueSerializer {
	private static XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	IndentingXMLStreamWriter xmlwriter;
	BufferedOutputStream bufferedStream;  

	public ValueXMLSerializer(OutputStream out){
		this.bufferedStream = new BufferedOutputStream(out);
	}
	
	private void initWriter() throws XMLStreamException{
		xmlwriter = new IndentingXMLStreamWriter(xmlOutputFactory.createXMLStreamWriter(bufferedStream));
	}

	

	
	public void serialize(RGLValue value)  {
		
		try {
			initWriter();
			
			/* print xml header */
			xmlwriter.writeStartDocument("utf-8", "1.0");
			xmlwriter.writeStartElement("rgl");
			xmlwriter.writeNamespace("rgl", "http://wis.ewi.tudelft.nl/rgl/datamodel#"); 
			xmlwriter.writeNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#"); 
			xmlwriter.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			
			value.accept(this);
			
			xmlwriter.writeEndElement();
			
			xmlwriter.flush();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(AbstractBagValue bag) {
		try {   
			xmlwriter.writeStartElement("bag");
			int counter = 0;
			for (RGLValue elem : bag ){
				elem.accept(this);
				++counter;
				if (counter % 1000 == 0)
				System.err.println(counter);
			}
			xmlwriter.writeEndElement(); // bag
			
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void visit(GraphValue graph)  {
		try {
			xmlwriter.writeStartElement("graph");
			
			xmlwriter.flush(); // flush xmlwriter before writing RDF/XML directly to bufferedStream
			
			RDFWriter rdfWriter = new com.hp.hpl.jena.xmloutput.impl.Basic();
			rdfWriter.write(graph.getModel(), bufferedStream, null);
			
			xmlwriter.writeEndElement();
			
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(BooleanValue bool) {
		try {
			xmlwriter.writeStartElement("boolean");
			xmlwriter.writeAttribute("value", bool.isTrue() ? "True" : "False"); 
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
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

//	
//	
//	@Override
//	public void visit(LiteralValue literal) {
//		try {
//			Literal jenaLiteral = literal.getRDFNode().asLiteral();
//			writer.print("<rgl:literal");
//			
//			String datatypeURI = jenaLiteral.getDatatypeURI();
//			if (datatypeURI!=null){
//				writer.print(" rdf:datatype=\"");
//				writer.print(datatypeURI);
//				writer.print("\"");
//			} else {
//				String lang = jenaLiteral.getLanguage();
//				if (lang.length()>0){
//					writer.print(" xml:lang=\"");
//					writer.print(lang);
//					writer.print("\"");
//				} 
//			}
//			writer.print(">");
//			writer.print(literal.getValueString());
//			writer.print("</rgl:literal>");
//			
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//	}

	@Override
	public void visit(AbstractRecordValue record) {
		try {
			xmlwriter.writeStartElement("record");
			
			for (String fieldName : record.getRange()){
				RGLValue bindingVal = record.get(fieldName);
				if (! bindingVal.isNull()){
					/* include a binding */
					
					/* print <binding name="fieldName"> */
					xmlwriter.writeStartElement("field");
					xmlwriter.writeAttribute("name", fieldName);
					
					/* print binding value */
					bindingVal.accept(this);
					
					xmlwriter.writeEndElement(); //  </binding>	
				}
				
			}
			
			xmlwriter.writeEndElement(); // </record>			
			
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
	public void visit(RGLNull rglNull) {
		try {
			xmlwriter.writeEmptyElement("null");
			if (rglNull.getErrorMessage()!=null)
				xmlwriter.writeAttribute("message", rglNull.getErrorMessage());
			
		} catch (XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void visit(LazyRGLValue lazyValue) {
		// we cannot deal with this value, let the value evaluate itself and call this visitor 
		// again with right method signature for OO-dispatching
		lazyValue.accept(this);
	}



}
