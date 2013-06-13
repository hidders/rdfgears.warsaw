package nl.tudelft.rdfgears.rgl.datamodel.value;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.rgl.datamodel.value.visitors.RGLValueVisitor;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Abstract class, interface can be MemoryURIValue 
 * (although I don't think other implementations will be relevant)
 * 
 * @author Eric Feliksik
 *
 */
public abstract class URIValue extends RDFValue {
	protected RDFNode node;
	protected URIValue(RDFNode node) {
		this.node = node;
	}

	public String toString(){
		return "<"+ uriString() +">";
	}
	
	public String uriString(){
		return getRDFNode().toString();
	}
	public void accept(RGLValueVisitor visitor){
		visitor.visit(this);
	}
	

	
	@Override
	public URIValue asURI(){
		return this;
	}
	
	@Override
	public boolean isURI(){
		return true;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeUTF(uriString());
	}

	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		node = Engine.getDefaultModel().createResource(in.readUTF());
	}
}
