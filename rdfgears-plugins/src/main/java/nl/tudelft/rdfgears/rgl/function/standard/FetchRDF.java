package nl.tudelft.rdfgears.rgl.function.standard;
        
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RDFValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A simple function to fetch Linked Data from the web.  
 * @author Eric Feliksik
 *
 */
public class FetchRDF extends SimplyTypedRGLFunction  {
	public static String uriPort = "uri";
	
	public FetchRDF(){
		this.requireInputType(uriPort, RDFType.getInstance()); 
	}

	public RGLType getOutputType() {
		return GraphType.getInstance();
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		RDFValue uriRDFValue = inputRow.get(uriPort).asRDFValue();
		String urlStr; 
		
		if (uriRDFValue.isURI()){
			urlStr = uriRDFValue.asURI().uriString();
		} else {
			// is literal
			urlStr = uriRDFValue.asLiteral().getValueString();	
		}
		
		try {
			Model m = ValueFactory.createModel();
			
			RDFReaderFImpl rdfReader = new RDFReaderFImpl();
			/* code copied from com.hp.hpl.jena.rdf.arp.JenaReader#read(Model m, String url) 
			 * because we want to set a connection timeout */
			
			RDFReader reader = rdfReader.getReader();
			Engine.getLogger().debug("Fetching RDF from "+urlStr);
			
            URLConnection conn = new URL(urlStr).openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
			conn.setRequestProperty("accept", "application/rdf+xml, application/xml; q=0.8, text/xml; q=0.7, application/rss+xml; q=0.3, */*; q=0.2");
            String encoding = conn.getContentEncoding();
            if (encoding == null)
            	reader.read(m, conn.getInputStream(), urlStr);
            else
                reader.read(m, new InputStreamReader(conn.getInputStream(),
                        encoding), urlStr);
            

			Engine.getLogger().debug("Fetched "+m.size()+" triples.");
            return ValueFactory.createGraphValue(m);
            
		}
		catch (Exception e ){
			return ValueFactory.createNull(urlStr+" : "+e.getMessage());
        } 
		
//		catch (FileNotFoundException e) {
//            throw new DoesNotExistException(urlStr);
//        } catch (IOException e) {
//            throw new JenaException(e);
//        }
			
			
		
	}

}
