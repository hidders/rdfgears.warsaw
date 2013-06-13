package nl.tudelft.rdfgears.tests.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;
 
public class TestHTTPRequest extends TestCase {
	
	@Test
	public void testGet(){
		//String requestUrl = "http://www.google.be";
		String requestUrl = "http://dbpedia.org/sparql?"+
			"default-graph-uri=http%3A%2F%2Fdbpediax.xorg&a=xx&"+
			"query=PREFIX++rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0D%0APREFIX++dbpedia%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0D%0APREFIX++rdf%3A++%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E%0D%0A%0D%0ASELECT+DISTINCT++%28str%28%3Flabel_lang%29+AS+%3Flabel%29+%3Fmov%0D%0AWHERE%0D%0A++{++%3Fmov+rdf%3Atype+dbpedia%3AFilm.%0D%0A++++%3Fmov+dbpedia%3Adirector+%3Fdir.%0D%0A++++%3Fdir+rdfs%3Alabel+%3Flabel_lang.+%0D%0A++}%0D%0AOFFSET++0%0D%0ALIMIT+++10%0D%0A";
		
		
        try {
            URL url = new URL(requestUrl.toString());
            
            
            //URLEncoder.encode(urlPart, "UTF-8");
            
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //conn.setRequestProperty("Accept", "application/sparql-results+xml,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            //conn.setRequestProperty("Accept", "application/xhtml+xml,application/xml;q=1.0,text/html;q=0.9,*/*;q=0.8");
            
            conn.setDoOutput(true);
            
            conn.connect();
            int responseCode = conn.getResponseCode();
            System.out.println("Content type  = "+conn.getContentType());
            System.out.println("response code is "+responseCode);
            

            if (conn.getErrorStream()!=null){
            	System.out.println("ERROR: ----------------");
            	printInputStream(conn.getErrorStream());
            }
            
        	System.out.println("RESPONSE: ----------------");
            
            System.out.println(conn.getResponseMessage());
            
            
            printInputStream(conn.getInputStream());
            
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private void printInputStream(InputStream inputStream) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
        	System.out.println(inputLine);
        }
        in.close();
	}   
}