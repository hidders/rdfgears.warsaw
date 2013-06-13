package nl.tudelft.rdfgears.tests;

import junit.framework.TestCase;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.util.row.TypeRow;

import org.junit.Before;
import org.junit.Test;


public class TestTypes extends TestCase {
	RecordType r1, r2_a, r2_b, complexRecord;
	BagType b1, b2, b3_a, b3_b, b3_c;
	GraphType g1_a,g1_b;
	
	@Before protected void initialize() {
		/* doesn't run */
		assertTrue("this isn't checked, oddly enough", false);
	}
	
    @Test public void testRecord2() {

		g1_a = GraphType.getInstance();
		g1_b = GraphType.getInstance();

		/**
		 * make rows 
		 */
		
		TypeRow tempRow; // local variable we use when instantiating record
		
		/* r1 has type Record(<a:Graph>) */
		tempRow = new TypeRow();  
		tempRow.put("graph", g1_a);
		r1 = RecordType.getInstance(tempRow);
		
		/* r2 has type Record(<b:Graph>, c:r1) */
		tempRow = new TypeRow(); 
		tempRow.put("graphName", g1_a);
		tempRow.put("rowName", r1);
		r2_a = RecordType.getInstance(tempRow);
		
		/* r3 has type Record(<b:Graph, c:r1>) - should be equal to r2 */
		tempRow = new TypeRow(); 
		tempRow.put("graphName", g1_b);
		tempRow.put("rowName", r1);
		r2_b = RecordType.getInstance(tempRow);
		
		
		/**
		 * make bags
		 */
		/* b1 has type {Graph} */
		b1 = BagType.getInstance(g1_a);
		b2 = BagType.getInstance(r1);
		b3_a = BagType.getInstance(r2_a);
		b3_b = BagType.getInstance(r2_a);
		b3_c = BagType.getInstance(r2_b);
		
		/* One complex element: Row(Bag(Row(...))) */
		tempRow = new TypeRow(); 
		tempRow.put("graphName", b3_a);
		tempRow.put("rowName", r1);
		complexRecord = RecordType.getInstance(tempRow);
		
		
    	assertTrue("must be instantiated", g1_a!=null);
    	assertTrue("must be instantiated", g1_b!=null);
    	assertTrue("must be instantiated", r1!=null);
    	assertTrue("must be instantiated", r2_a!=null);
    	assertTrue("must be instantiated", r2_b!=null);
    	assertTrue("must be instantiated", complexRecord!=null);
    	
    	
    	assertTrue("must be instantiated", b1!=null);
    	assertTrue("must be instantiated", b2!=null);
    	assertTrue("must be instantiated", b3_a!=null);
    	assertTrue("must be instantiated", b3_b!=null);
    	assertTrue("must be instantiated", b3_c!=null);
    	
    	assertTrue("graphtypes are identical objects of singleton ", g1_a==g1_b);
    	assertTrue("test", true);
    	
    	assertTrue("graphtypes must always be equal", g1_a.equals(g1_a));
    	
    	//assertTrue("graphtypes must always be equal", g1_a.equals(g1_b));
    	    	
		assertTrue("Graph must not be equal to row", ! g1_a.equals(r1));
		assertTrue("Rows must be different ", ! r1.equals(r2_a));
		assertTrue("Row equals itself", r1.equals(r1));
		assertTrue("Rows must be equal", r2_a.equals(r2_b));
		assertTrue("Bag can not equal record", !b1.equals(r1));
		assertTrue("Bag equals itself", b2.equals(b2));
		assertTrue("Bag can not equal graph", !b1.equals(g1_a));
		assertTrue("Bags must be equal ", b3_a.equals(b3_b));
		
		assertTrue("Bags must be equal ", b3_a.equals(b3_c));
		assertTrue("Records must not be equal, although keys are the same", ! r2_a.equals(complexRecord));
		assertTrue("Record does not equal Bag", ! b1.equals(complexRecord));
		assertTrue("Record does not equal Graph", ! g1_a.equals(complexRecord));
		   

    }
    
}
