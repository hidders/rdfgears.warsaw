package nl.tudelft.rdfgears.tests;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import nl.tudelft.rdfgears.rgl.datamodel.value.BagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MappingBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryURIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.core.RecordCreate;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.FieldIndexMapFactory;
import nl.tudelft.rdfgears.util.row.FieldMappedValueRow;

import org.junit.Test;


public class TestProduct {

	/**
	 * @return a random pseudo-URI
	 */
	private String randomString() {
		Random random = new Random();
		return new BigInteger(130, random).toString(32);
	}

	@Test
	public void foo() {
		List<RGLValue> l1 = new ArrayList<RGLValue>();
		List<RGLValue> l2 = new ArrayList<RGLValue>();

		for (double d = 1.0; d <= 100.0; ++d) {
			l1.add(new MemoryURIValue(randomString()));
			l2.add(new MemoryURIValue(randomString()));
		}

		BagValue bag1 = new ListBackedBagValue(l1);
		BagValue bag2 = new ListBackedBagValue(l2);

		RGLFunction max = new RecordCreate();
		try {
			max.initialize(Collections.singletonMap("fields", "value1;value2;"));
		} catch (WorkflowLoadingException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e.getMessage());
		}

		FunctionProcessor fp = new FunctionProcessor(max);
		fp.getPort("value1").markIteration();
		fp.getPort("value2").markIteration();

		FieldIndexMap fieldIndexMap = FieldIndexMapFactory.create("value1", "value2");
		
		FieldMappedValueRow row = new FieldMappedValueRow(fieldIndexMap);
		row.put("value1", bag1);
		row.put("value2", bag2);

		MappingBagValue bag = new MappingBagValue(row, fp);

		System.out.println(bag.size());
		Iterator<RGLValue> iterator = bag.iterator();
		int i=0;
		while (iterator.hasNext()){
			System.out.println(i++);
			System.out.println(iterator.next());
		}

	}

}