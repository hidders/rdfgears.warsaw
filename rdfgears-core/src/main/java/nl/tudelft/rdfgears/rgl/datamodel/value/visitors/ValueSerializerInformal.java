package nl.tudelft.rdfgears.rgl.datamodel.value.visitors;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;

import nl.tudelft.rdfgears.rgl.datamodel.value.BooleanValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLNull;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.URIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.workflow.LazyRGLValue;
import nl.tudelft.rdfgears.util.BufferedIndentedWriter;

import com.hp.hpl.jena.n3.N3JenaWriter;
import com.hp.hpl.jena.rdf.model.RDFWriter;


/**
 * A visitor that serializes an RGL Value, writing it to an Output Stream. 
 * 
 * @author Eric Feliksik
 *
 */
public class ValueSerializerInformal extends ValueSerializer {
	
	BufferedIndentedWriter writer; 
	
	public ValueSerializerInformal(){
		this(System.out);
	}
	
	public ValueSerializerInformal(OutputStream out){
		this.writer = new BufferedIndentedWriter(out);
	}

	public ValueSerializerInformal(Writer writer){
		this.writer = new BufferedIndentedWriter(writer);
	}
	
	
	
	public void serialize(RGLValue value){
		
		value.accept(this);
		try {
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(AbstractBagValue bag) {
		try {   
			int elemCounter = 0;
			Iterator<RGLValue> iter = bag.iterator();
			writer.print("{{");
			writer.incIndent();
			
			boolean hasValues = iter.hasNext();
			while(iter.hasNext()){
				writer.newline();
				elemCounter++;
				writer.print("(elem:"+elemCounter+") = ");
				RGLValue val = iter.next();
				val.accept(this);
			}
			writer.outdent();
			if (hasValues)
				writer.newline();
			writer.print("}}");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public void visit(GraphValue graph) {
		RDFWriter rdfWriter = new N3JenaWriter();
		rdfWriter.write(graph.getModel(), writer, null);
	}
	
	@Override
	public void visit(BooleanValue bool) {
		try {
			String s = bool.isTrue() ? "True" : "False";
			writer.print(s);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}
	
	@Override
	public void visit(LiteralValue literal) {
		try {
			writer.print(literal.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void visit(AbstractRecordValue record) {
		
		try {  
			writer.print("[ ");
			writer.incIndent();
			
			for (String fieldName : record.getRange()){
				writer.newline();
				writer.print(fieldName);
				writer.print(":");
				RGLValue rglValue = record.get(fieldName);
				rglValue.accept(this);
				
				writer.print(", ");
			}
			writer.outdent();
			writer.newline();
			writer.print("]");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void visit(URIValue uri) {
		try {
			writer.print("<");
			writer.print(uri.uriString());
			writer.print(">");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}


	@Override
	public void visit(RGLNull rglNull) {
		try {
			writer.print("<null (");
			writer.print(rglNull.getErrorMessage());
			writer.print(")>");
		} catch (IOException ex) {
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
