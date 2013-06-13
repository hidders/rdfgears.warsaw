package com.hp.hpl.jena.sparql.engine.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.util.Convert;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;


/**
 * Copy of the QueryEngineHTTP and HttpQuery class.  
 * 
 * The ***only*** goal of this exercise is to call httpConnection.getErrorStream() in the execCommon() method of 
 * HttpQuery. This is not done by default ARQ, unfortunately.
 * 
 * It would be nice if ARQ would do this getErrorStream, or at least make the more methods public/protected
 * to allow us to do more easy class-extending.  
 * 
 * 
 * @author Eric Feliksik
 * 
 * Based on ARQ. 
 *
 */
public class QueryEngineHTTPWithPublicHTTPQuery extends QueryEngineHTTP {
	private static Logger log = LoggerFactory.getLogger(QueryEngineHTTP.class) ;
	private InputStream retainedConnection;

	public QueryEngineHTTPWithPublicHTTPQuery(String serviceURI,
			String queryString) {
		super(serviceURI, queryString);
		// TODO Auto-generated constructor stub
	}

	public QueryEngineHTTPWithPublicHTTPQuery(String serviceURI, Query query) {
		super(serviceURI, query);
		// TODO Auto-generated constructor stub
	}
	
	public HttpQuery makeHttpQuery()
    {
        HttpQuery httpQuery = new HttpQueryWithErrorMsg(service) ; /* the special HttpQuery class by Eric */
        httpQuery.addParam(HttpParams.pQuery, queryString );
        
        for ( Iterator<String> iter = defaultGraphURIs.iterator() ; iter.hasNext() ; )
        {
            String dft = iter.next() ;
            httpQuery.addParam(HttpParams.pDefaultGraph, dft) ;
        }
        for ( Iterator<String> iter = namedGraphURIs.iterator() ; iter.hasNext() ; )
        {
            String name = iter.next() ;
            httpQuery.addParam(HttpParams.pNamedGraph, name) ;
        }
        
        if ( params != null )
            httpQuery.merge(params) ;
        
        // not supported; if needed, fetch it from superclass
        // httpQuery.setBasicAuthentication(user, password) ;
        return httpQuery ;
    }
	
	/**
	 * Functions are not changed, but they must be declared here in order to prevent them to call 
	 * the PRIVATE version of makeHTTPQuery .... :-(  
	 * 
	 */
	
    public ResultSet execSelect()
    {
    	HttpQuery httpQuery = makeHttpQuery() ;
        // TODO Allow other content types.
        httpQuery.setAccept(HttpParams.contentTypeResultsXML) ;
        InputStream in = httpQuery.exec() ;
        
        ResultSet rs = ResultSetFactory.fromXML(in) ;
        retainedConnection = in; // This will be closed on close()
        return rs ;
    }

    public void close() {
        if (retainedConnection != null) {
            try { retainedConnection.close(); }
            catch (java.io.IOException e) { log.warn("Failed to close connection", e); }
            finally { retainedConnection = null; }
        }
    }

    public Model execConstruct()             { return execConstruct(GraphFactory.makeJenaDefaultModel()) ; }
    
    public Model execConstruct(Model model)  { return execModel(model) ; }

    public Model execDescribe()              { return execDescribe(GraphFactory.makeJenaDefaultModel()) ; }
    
    public Model execDescribe(Model model)   { return execModel(model) ; }

    private Model execModel(Model model)
    {
        HttpQuery httpQuery = makeHttpQuery() ;
        httpQuery.setAccept(HttpParams.contentTypeRDFXML) ;
        InputStream in = httpQuery.exec() ;
        model.read(in, null) ;
        return model ;
    }

}

/**
 * Extend HttpQuery class to read error message, if any
 * @author Eric Feliksik
 *
 */
class HttpQueryWithErrorMsg extends HttpQuery {

	public HttpQueryWithErrorMsg(URL url) {
		super(url);
	}
	public HttpQueryWithErrorMsg(String str) {
		super(str);
	}
	
	protected InputStream execCommon() throws QueryExceptionHTTP
	{
	    try {        
	        responseCode = httpConnection.getResponseCode() ;
	        responseMessage = Convert.decWWWForm(httpConnection.getResponseMessage()) ;
	        
	        // 1xx: Informational 
	        // 2xx: Success 
	        // 3xx: Redirection 
	        // 4xx: Client Error 
	        // 5xx: Server Error 
	        
	        if ( 300 <= responseCode && responseCode < 400 )
	            throw new QueryExceptionHTTP(responseCode, responseMessage) ;
	        
	        // Other 400 and 500 - errors 
	        
	        if ( responseCode >= 400 ){
	        	/* 
	        	 * THIS IS THE POINT OF THIS CODE DUPLICATION; MODIFICATION BY ERIC
	        	 */
	        	/* overload response message by including the error body, if available */
	        	String overloadedResponseMessage = responseMessage; 
	        	if( httpConnection.getErrorStream()!=null){
	        		overloadedResponseMessage += " --- "+getStringFromInputStream(httpConnection.getErrorStream());
	        	}
	            throw new QueryExceptionHTTP(responseCode, overloadedResponseMessage) ;
	        }
	
	        // Request suceeded
	        InputStream in = httpConnection.getInputStream() ;
//	        
//	        if ( false )
//	        {
//	            // Dump the reply
//	            Map<String,List<String>> map = httpConnection.getHeaderFields() ;
//	            for ( Iterator<String> iter = map.keySet().iterator() ; iter.hasNext() ; )
//	            {
//	                String k = iter.next();
//	                List<String> v = map.get(k) ;
//	                System.out.println(k+" = "+v) ;
//	            }
//	            
//	            // Dump response body
//	            StringBuffer b = new StringBuffer(1000) ;
//	            byte[] chars = new byte[1000] ;
//	            while(true)
//	            {
//	                int x = in.read(chars) ;
//	                if ( x < 0 ) break ;
//	                b.append(new String(chars, 0, x, FileUtils.encodingUTF8)) ;
//	            }
//	            System.out.println(b.toString()) ;
//	            System.out.flush() ;
//	            // Reset
//	            in = new ByteArrayInputStream(b.toString().getBytes(FileUtils.encodingUTF8)) ;
//	        }
	        return in ;
	    }
	    catch (IOException ioEx)
	    {
	        throw new QueryExceptionHTTP(ioEx) ;
	    } 
	    catch (JenaException rdfEx)
	    {
	        throw new QueryExceptionHTTP(rdfEx) ;
	    }
	}
	
	
	/**
	 * helper method to make string from inputStream 
	 */
	private String getStringFromInputStream(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String str = "";
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
        	str += inputLine;
        }
        in.close();
        return str;
	}   
	
	
	
	/* *****************************************************************************************************
	 * methods are only duplicated so that they are actually overriding the superclass ones, as those are 
	 * private :-(  
	 */
	

    private String getQueryString()
    {
        if ( queryString == null )
            queryString = super.httpString() ;
        return queryString ;
    }
    
    /** Execute the operation
     * @return Model    The resulting model
     * @throws QueryExceptionHTTP
     */
    public InputStream exec() throws QueryExceptionHTTP
    {
        try {
            if (usesPOST())
                return execPost();
            return execGet();
        } catch (QueryExceptionHTTP httpEx)
        {
            log.trace("Exception in exec", httpEx);
            throw httpEx;
        }
        catch (JenaException jEx)
        {
            log.trace("JenaException in exec", jEx);
            throw jEx ;
        }
    }
    
    
    private InputStream execGet() throws QueryExceptionHTTP
    {
        URL target = null ;
        String qs = getQueryString() ;
        
        ARQ.getHttpRequestLogger().trace(qs) ;
        
        try {
            if ( count() == 0 )
                target = new URL(serviceURL) ; 
            else
                target = new URL(serviceURL+"?"+qs) ;
        }
        catch (MalformedURLException malEx)
        { throw new QueryExceptionHTTP(0, "Malformed URL: "+malEx) ; }
        log.trace("GET "+target.toExternalForm()) ;
        
        try
        {
            httpConnection = (HttpURLConnection) target.openConnection();
            httpConnection.setRequestProperty("Accept", contentTypeResult) ;
            
            int x = httpConnection.getReadTimeout() ;
            
            // By default, following 3xx redirects is true
            //conn.setFollowRedirects(true) ;
            
            // dont do this - Eric Feliksik
            // basicAuthentication(httpConnection) ;
            
            httpConnection.setDoInput(true);
            httpConnection.connect();
            try
            {
                return execCommon();
            }
            catch (QueryExceptionHTTP qEx)
            {
                // Back-off and try POST if something complain about long URIs
                // Broken 
                if (qEx.getResponseCode() == 414 /*HttpServletResponse.SC_REQUEST_URI_TOO_LONG*/ )
                    return execPost();
                throw qEx;
            }
        }
        catch (java.net.ConnectException connEx)
        { throw new QueryExceptionHTTP(QueryExceptionHTTP.NoServer, "Failed to connect to remote server"); }
        catch (IOException ioEx)
        { throw new QueryExceptionHTTP(ioEx); }
    }
    
    private InputStream execPost() throws QueryExceptionHTTP
    {
        URL target = null;
        try { target = new URL(serviceURL); }
        catch (MalformedURLException malEx)
        { throw new QueryExceptionHTTP(0, "Malformed URL: " + malEx); }
        log.trace("POST "+target.toExternalForm()) ;
        
        ARQ.getHttpRequestLogger().trace(target.toExternalForm()) ;

        try
        {
            httpConnection = (HttpURLConnection) target.openConnection();
            httpConnection.setRequestMethod("POST") ;
            httpConnection.setRequestProperty("Accept", contentTypeResult) ;
            httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded") ;
            
            // skip auth - Eric
            // basicAuthentication(httpConnection) ;
            httpConnection.setDoOutput(true) ;
            
            boolean first = true ;
            OutputStream out = httpConnection.getOutputStream() ;
            for ( Iterator<Pair> iter = pairs().listIterator() ; iter.hasNext() ; )
            {
                if ( ! first )
                    out.write('&') ;
                first = false ;
                Pair p = iter.next() ;
                out.write(p.getName().getBytes()) ;
                out.write('=') ;
                String x = p.getValue() ;
                x = Convert.encWWWForm(x) ;
                out.write(x.getBytes()) ;
                ARQ.getHttpRequestLogger().trace("Param: "+x) ;
            }
            out.flush() ;
            httpConnection.connect() ;
            return execCommon() ;
        }
        catch (java.net.ConnectException connEx)
        { throw new QueryExceptionHTTP(-1, "Failed to connect to remote server"); }
        catch (IOException ioEx)
        { throw new QueryExceptionHTTP(ioEx); }
    }
    
}


