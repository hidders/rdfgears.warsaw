package nl.tudelft.rdfgears.util;
//package com.hp.hpl.jena.sparql.syntax;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.syntax.ElementAssign;
import com.hp.hpl.jena.sparql.syntax.ElementBind;
import com.hp.hpl.jena.sparql.syntax.ElementDataset;
import com.hp.hpl.jena.sparql.syntax.ElementExists;
import com.hp.hpl.jena.sparql.syntax.ElementFetch;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementMinus;
import com.hp.hpl.jena.sparql.syntax.ElementNamedGraph;
import com.hp.hpl.jena.sparql.syntax.ElementNotExists;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementService;
import com.hp.hpl.jena.sparql.syntax.ElementSubQuery;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;

public class QueryReConstructVisitor extends ElementVisitorBase
{
	private Query originalQuery;
	private String graphVarName;
	
	
	
	public QueryReConstructVisitor(Query originalQuery, String graphVarName){
		this.originalQuery = originalQuery;
		this.graphVarName = graphVarName;
	}
    
    public void visit(ElementTriplesBlock el)   {
    	
    }

    public void visit(ElementFilter el)         { 
    	
    }
    
    public void visit(ElementAssign el)         { }

    public void visit(ElementBind el)           { }

    public void visit(ElementUnion el)          { }

    public void visit(ElementDataset el)        { }

    public void visit(ElementOptional el)       { }

    public void visit(ElementGroup el)          {
    }

    public void visit(ElementNamedGraph el)     { }

    public void visit(ElementExists el)         { }
    
    public void visit(ElementNotExists el)      { }
    
    public void visit(ElementMinus el)          { }

    public void visit(ElementService el)        { }
    
    public void visit(ElementFetch el)          { }

    public void visit(ElementSubQuery el)       { }

    public void visit(ElementPathBlock el)      { }
    
    
//	
//	private Set<Var> acc ;
//    
//    @Override
//    public void visit(ElementTriplesBlock el)
//    {
//        for (Iterator<Triple> iter = el.patternElts() ; iter.hasNext() ; )
//        {
//            Triple t = iter.next() ;
//            VarUtils.addVarsFromTriple(acc, t) ;
//        }
//    }
//
//    @Override
//    public void visit(ElementPathBlock el) 
//    {
//        for (Iterator<TriplePath> iter = el.patternElts() ; iter.hasNext() ; )
//        {
//            TriplePath tp = iter.next() ;
//            // If it's triple-izable, then use the triple. 
//            if ( tp.isTriple() )
//                VarUtils.addVarsFromTriple(acc, tp.asTriple()) ;
//            else
//                VarUtils.addVarsFromTriplePath(acc, tp) ;
//        }
//    }
//    
//    // Variables here are non-binding.
//    //@Override public void visit(ElementExists el)       { }
//    //@Override public void visit(ElementNotExists el)    { }
//    //@Override public void visit(ElementMinus el)        { }
//
////      public void visit(ElementFilter el)
////      {
////      el.getExpr().varsMentioned(acc);
////      }
//
//    @Override
//    public void visit(ElementNamedGraph el)
//    {
//        VarUtils.addVar(acc, el.getGraphNameNode()) ;
//    }
//    
//    @Override
//    public void visit(ElementSubQuery el)
//    {
//        el.getQuery().setResultVars() ;
//        VarExprList x = el.getQuery().getProject() ;
//        acc.addAll(x.getVars()) ;
//    }
//    
//    @Override
//    public void visit(ElementAssign el)
//    {
//        acc.add(el.getVar()) ;
//    }
//    
//    @Override
//    public void visit(ElementBind el)
//    {
//        acc.add(el.getVar()) ;
//    }
//    
//        @Override
//        public void visit(ElementService el)
//        {
//            // Although if this isn't defined elsewhere the query won't work.
//            VarUtils.addVar(acc, el.getServiceNode()) ;
//        }
}
	
