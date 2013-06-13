package nl.tudelft.rdfgears.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.tudelft.rdfgears.rgl.datamodel.value.BagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryLiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;

import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.impl.XSDDouble;

public class TestNesting {
	@Test
	public void nestedBags() {
		List<RGLValue> outerList = new ArrayList<RGLValue>();
		
		int outerSize, nestedSize;
		
		outerSize = 100;
		nestedSize = 100;

		for (int i = 0; i <= outerSize; ++i) {

			List<RGLValue> nestedList = new ArrayList<RGLValue>();

			for (double d = 0.0; d <= nestedSize; ++d) {
				nestedList.add(MemoryLiteralValue.createLiteralTyped(d,
						new XSDDouble("double")));
			}

			outerList.add(new ListBackedBagValue(nestedList));
		}
		BagValue outerBag = new ListBackedBagValue(outerList);
		
		Iterator<RGLValue> outerIt = outerBag.iterator();
		while (outerIt.hasNext()) {
			Iterator<RGLValue> nestedIt = outerIt.next().asBag().iterator();
			while (nestedIt.hasNext()) {
				System.out.println("\t" + nestedIt.next());
			}
		}
		
		// (new ValueSerializer()).serialize(outerBag);
		System.out.println(outerBag);
		
	}
}