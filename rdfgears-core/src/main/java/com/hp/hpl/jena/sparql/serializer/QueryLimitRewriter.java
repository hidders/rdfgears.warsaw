package com.hp.hpl.jena.sparql.serializer;

import org.openjena.atlas.io.IndentedLineBuffer;
import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode;

/**
 * 
 * 
 * In ARQ, queries cannot easily be modified/cloned (e.g. see Query.cloneQuery(), they serializer/parse it). 
 * 
 * So this is also the way we modify the query. 
 * 
 * 
 * @author Eric Feliksik
 *
 */
public class QueryLimitRewriter extends QuerySerializer {

	/**
	 * Breaking news: these days there is documentation available for ARQ query manipulation.
	 * 
	 * See http://openjena.org/wiki/ARQ/Manipulating_SPARQL_using_ARQ on how these visitors work. 
	 */

	private long limit = Query.NOLIMIT;
	private long offset = Query.NOLIMIT;
	
	QueryLimitRewriter(IndentedWriter iwriter,
			FormatterElement formatterElement, FmtExpr formatterExpr,
			FormatterTemplate formatterTemplate) {
		super(iwriter, formatterElement, formatterExpr, formatterTemplate);	
	}
	
	public long getLimit(){ return limit; }
	public long getOffset (){ return offset; }
	
    public void visitLimit(Query query)
    {
       out.print("LIMIT   "+getLimit()) ;
       out.newline() ;
    }
    
    public void visitOffset(Query query)
    {
        out.print("OFFSET  "+getOffset()) ;
        out.newline() ;
    }
    
    /**
     * rewrite the given query with the given limit/offset 
     * @param limit
     * @param offset
     * @return
     */
    public static Query rewrite(Query query, long limit, long offset){
    	/* taken from com.hp.hpl.jena.sparql.serializer.Serializer */
		IndentedLineBuffer iwriter = new IndentedLineBuffer() ;
		
		// For the query pattern
        SerializationContext cxt1 = new SerializationContext(query, new NodeToLabelMapBNode("b", false) ) ;
        // For the construct pattern
        SerializationContext cxt2 = new SerializationContext(query, new NodeToLabelMapBNode("c", false)  ) ;
        
        QueryLimitRewriter rewriter =  new QueryLimitRewriter(iwriter, 
				new FormatterElement(iwriter, cxt1), 
				new FmtExpr(iwriter, cxt1), 
				new FmtTemplate(iwriter, cxt2));
        rewriter.setLimit(limit);
        rewriter.setOffset(offset); 
        
    	query.visit(rewriter);
    	iwriter.flush() ;
    	String queryString = iwriter.getBuffer().toString();
    	
    	return QueryFactory.create(queryString, query.getSyntax());
    }

	private void setOffset(long offset) {
		this.offset = offset;
	}

	private void setLimit(long limit) {
		this.limit = limit;
	}
    
    
    
	
	
}
