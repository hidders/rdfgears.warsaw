package nl.tudelft.rdfgears.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;
import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.SuperType;
import nl.tudelft.rdfgears.rgl.datamodel.value.GraphValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryURIValue;
import nl.tudelft.rdfgears.rgl.function.RGLFunction;
import nl.tudelft.rdfgears.rgl.function.core.RecordProject;
import nl.tudelft.rdfgears.rgl.function.custom.MultiplicationFunction;
import nl.tudelft.rdfgears.rgl.function.custom.Sum5Function;
import nl.tudelft.rdfgears.rgl.function.sparql.SPARQLFunction;
import nl.tudelft.rdfgears.rgl.workflow.ConstantProcessor;
import nl.tudelft.rdfgears.rgl.workflow.FunctionProcessor;

import org.junit.Before;
import org.junit.Test;

import tools.HashBag;
import tools.TestUtil;
import tools.Timer;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;

public class TestProcessorNetwork extends TestCase {
	private static ConstantProcessor lit1generator = new ConstantProcessor(
			Engine.getValueFactory().createLiteralTyped("1.0",
					XSDDatatype.XSDdouble));
	private static ConstantProcessor lit2generator = new ConstantProcessor(
			Engine.getValueFactory().createLiteralTyped("2.0",
					XSDDatatype.XSDdouble));

	@Before
	public void initialize() {
		/* doesn't run */
		assertTrue("this isn't checked, oddly enough", false);
	}

	public static FunctionProcessor getTestedNetwork() {
		/* create a constructFunction with typing definition */
		RGLFunction constructFunction = TestRGLFunctions.getConstructFunction();

		/*
		 * create a constructProcessor with constructFunction semantics, and set
		 * inputs
		 */
		FunctionProcessor constructProcessor = new FunctionProcessor(
				constructFunction);
		constructProcessor.getPort("graph1").setInputProcessor(
				new ConstantProcessor(Data
						.getGraphFromFile("./data/linkedmdb.xml")));

		assertTrue("Processor cannot be well-typed without 'director' input",
				TestUtil.getOutputType(constructProcessor) == null);

		constructProcessor.getPort("director").setInputProcessor(
				new ConstantProcessor(new MemoryURIValue(
						"http://data.linkedmdb.org/resource/director/866")));

		assertTrue("Processor should be well-typed", TestUtil
				.getOutputType(constructProcessor) != null);

		/* get the processor value */
		GraphValue graph = constructProcessor.getResultValue().asGraph();
		assertTrue("Must have 3 entries, but have " + graph.getModel().size(),
				graph.getModel().size() == 3);

		return constructProcessor;
	}

	@Test
	public void testNetwork() {
		getTestedNetwork();
	}

	/**
	 * Get a bag with records from a select query
	 * 
	 * @param size
	 * @return
	 */
	private FunctionProcessor getRecordBagGenerator(int size) {
		/* create a selectFunction with typing definition */
		String queryStr = "PREFIX ont: <http://ont/> \n "
				+ "PREFIX ns1: <http://ns1/> \n "
				+ "SELECT DISTINCT ?val \n"
				+ "WHERE {\n"
				+ "  Graph $g {"
				+ "    <http://ns1/elementWith500availableIntProperties> ont:availableIntProperty ?val. \n"
				+ "  }\n" + "} ORDER BY ?val LIMIT " + size;

		SPARQLFunction selectFunction = new SPARQLFunction();
		selectFunction.initialize(Collections.singletonMap("query", queryStr));
		selectFunction.requireInputType("g", GraphType.getInstance());
		assertTrue(
				"input 'g' was just required, so it must be registered as such",
				selectFunction.getRequiredInputNames().contains("g"));
		/*
		 * create a constructProcessor with constructFunction semantics, and set
		 * inputs
		 */
		FunctionProcessor selectProcessor = new FunctionProcessor(
				selectFunction);
		selectProcessor.getPort("g").setInputProcessor(
				new ConstantProcessor(Data
						.getGraphFromFile("./data/intvalues.xml")));
		assertTrue("A select processor must return a Bag", selectProcessor
				.getResultValue().isBag());

		int select1outputSize = selectProcessor.getResultValue().asBag().size();
		assertTrue("Must have " + size + " entries, but have "
				+ select1outputSize, select1outputSize == size);

		return selectProcessor;
	}

	@Test
	public void testSelectQueryIteration() {
		FunctionProcessor recordBagProc = getRecordBagGenerator(10);
		assertTrue("select query should return a bag", recordBagProc
				.getResultValue().isBag());

		ArrayList<AbstractRecordValue> recordList = new ArrayList<AbstractRecordValue>();
		Iterator<RGLValue> iterator = recordBagProc.getResultValue().asBag()
				.iterator();
		while (iterator.hasNext()) {
			RGLValue recordGen = iterator.next();
			assertTrue(
					"Such valueGenerators happen to return themselves, as they are also records",
					recordGen.isRecord());
			AbstractRecordValue record = recordGen.asRecord();
			assertTrue("Should be RDF value ", record.get("val").isRDFValue());
			recordList.add(record);
		}

		/* we should be able to iterate again, and get the same values */
		iterator = recordBagProc.getResultValue().asBag().iterator();
		int i = 0;
		while (iterator.hasNext()) {
			RGLValue recordGen = iterator.next();
			AbstractRecordValue record = recordGen.asRecord();
			for (String s : record.getRange()) {
				assertTrue(
						"record should be identical with the earlier retrieved record",
						recordList.get(i).asRecord().get(s).compareTo(
								record.get(s)) == 0);
			}
			i++;
		}
	}

	/**
	 * return a bag of numbers
	 * 
	 * @param size
	 * @return
	 */
	private FunctionProcessor getNumberBagGenerator(int size) {

		/*
		 * create two selectors, that iterate over the bag of records, and
		 * output a bag of selected values
		 */
		RecordProject rp = new RecordProject();
		rp.initialize(Collections.singletonMap(
				RecordProject.CONFIGKEY_PROJECTFIELD, "val"));
		FunctionProcessor projector = new FunctionProcessor(rp);
		FunctionProcessor recordBagGen = getRecordBagGenerator(size);
		System.out.println("#>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>###");
		projector.getPort(RecordProject.INPUT_NAME).setInputProcessor(
				recordBagGen);
		projector.getPort(RecordProject.INPUT_NAME).markIteration();

		assertTrue("selector iterates, and thus must output a bag ", projector
				.getResultValue().isBag());
		AbstractBagValue bag = projector.getResultValue().asBag();

		assertTrue(TestUtil.getOutputType(projector) instanceof BagType);
		assertTrue(((BagType) TestUtil.getOutputType(projector)).getElemType() instanceof RDFType);

		// bag.enableMaterializingWrapper();

		// (new ValueSerializerHumane()).serialize(projector.getResultValue());
		assertTrue("selecting tuples shouldn't affect number of records", bag
				.size() == size);
		assertTrue("Two iterators over a bag should be different instances",
				bag.iterator() != bag.iterator());

		return projector;

	}

	@Test
	public void testIteration() {
//		doComplexIteration(4, 4);
//		doComplexIteration(4, 1);
//		doComplexIteration(1, 4);
//		doComplexIteration(0, 4);
//		doComplexIteration(2, 0);
//		doComplexIteration(1, 1);
//		doComplexIteration(1, 0);
//		doComplexIteration(0, 1);
//		doComplexIteration(0, 0);
	}

	// @Test
	public void testLargeBagProd() {
//		final int SIZE = 4; // Cartesian product will be taken
//
//		FunctionProcessor numberBagGenerator1 = getNumberBagGenerator(SIZE);
//
//		assertTrue(TestUtil.getOutputType(numberBagGenerator1) instanceof BagType);
//		assertTrue(numberBagGenerator1.getResultValue().isBag());
//
//		for (RGLValue numberGenerator : numberBagGenerator1.getResultValue()
//				.asBag()) {
//			System.out.println(numberGenerator);
//		}
//
//		FunctionProcessor sumProc = new FunctionProcessor(new Sum5Function());
//		sumProc.getPort(Sum5Function.value1).setInputProcessor(
//				numberBagGenerator1);
//		sumProc.getPort(Sum5Function.value1).markIteration();
//		sumProc.getPort(Sum5Function.value2).setInputProcessor(
//				numberBagGenerator1);
//		sumProc.getPort(Sum5Function.value2).markIteration();
//
//		assertTrue("cannot be ok, missing some values still ", TestUtil
//				.getOutputType(sumProc) == null);
//		sumProc.getPort(Sum5Function.value3).setInputProcessor(lit1generator);
//		sumProc.getPort(Sum5Function.value4).setInputProcessor(lit1generator);
//		sumProc.getPort(Sum5Function.value5).setInputProcessor(lit1generator);
//
//		assertTrue("Should be well-typed",
//				TestUtil.getOutputType(sumProc) != null);
//		assertTrue("Should be well-typed", TestUtil.getOutputType(sumProc)
//				.isSubtypeOf(BagType.getInstance(new SuperType())));
//		assertTrue("Should return a bag value", sumProc.getResultValue()
//				.isBag());
//
//		Timer timer = new Timer();
//		timer.start("Getting bag and requesting size()");
//		AbstractBagValue bag = sumProc.getResultValue().asBag();
//		int bagSize = bag.size();
//		timer.end();
//		timer.start("Iterating over a bag of size " + bagSize);
//		for (RGLValue val : bag) {
//			val.isLiteral(); // test, this will evaluate internally
//		}
//
//		timer.end();

	}

	/**
	 * Test whether iterators are iterating mutually independently.
	 */
	@Test
	public void testIteratorReinstantiation() {
//		FunctionProcessor bagProducer1 = getNumberBagGenerator(10);
//		AbstractBagValue bag = bagProducer1.getResultValue().asBag();
//
//		System.out.println("BAG ###############################: ");
//		System.out.println(bag.getClass().getCanonicalName());
//		// (new ValueSerializerHumane()).serialize(bag);
//
//		System.out.println("Testing the " + bag.getClass());
//		Iterator<RGLValue> it1 = bag.iterator();
//		Iterator<RGLValue> it2 = bag.iterator();
//		Iterator<RGLValue> it3 = bag.iterator();
//
//		assertTrue("bag iterators should be different", it1 != it2);
//		double val;
//		val = it1.next().asLiteral().getValueDouble();
//		assertTrue("Should be 1, not " + val, val == 1);
//
//		assertTrue(it1.hasNext());
//		val = it1.next().asLiteral().getValueDouble();
//		assertTrue("Should be 2, not " + val, val == 2);
//
//		assertTrue(it2.hasNext());
//		val = it2.next().asLiteral().getValueDouble();
//		assertTrue("Should be 1, not " + val, val == 1);
//
//		assertTrue(it3.hasNext());
//		val = it3.next().asLiteral().getValueDouble();
//		assertTrue("Should be 1, not " + val, val == 1);
//
//		assertTrue(it1.hasNext());
//		val = it1.next().asLiteral().getValueDouble();
//		assertTrue("Should be 3, not " + val, val == 3);
//
//		assertTrue(it1.hasNext());
//		val = it1.next().asLiteral().getValueDouble();
//		assertTrue("Should be 4, not " + val, val == 4);
//
//		while (it1.hasNext())
//			it1.next();
//
//		assertTrue(it3.hasNext());
//		val = it3.next().asLiteral().getValueDouble();
//		assertTrue("Should be 3, not " + val, val == 2);
//
//		assertTrue(it2.hasNext());
//		val = it2.next().asLiteral().getValueDouble();
//		assertTrue("Should be 2, not " + val, val == 2);
//
//		assertTrue(it2.hasNext());
//		assertTrue(it3.hasNext());
//
//		while (it2.hasNext())
//			it2.next();
//		while (it3.hasNext())
//			it3.next();

	}

	/* test the types of iterating values, and the cardinality of the resultset */
	private void doComplexIteration(int size1, int size2) {
		System.out.println("doComplexIteration(" + size1 + ", " + size2 + ") ");
		/**
		 * Do a summation of five values: - value1: iterate over selector1
		 * output - value2: iterate over selector2 output - value3: iterate over
		 * selector2 output (again) - value4: constant value - value5: constant
		 * value
		 */
		FunctionProcessor bagProducer1 = getNumberBagGenerator(size1);
		FunctionProcessor bagProducer2 = getNumberBagGenerator(size2);

		assertTrue(TestUtil.getOutputType(bagProducer1) instanceof BagType);
		assertTrue(bagProducer1.getResultValue().isBag());

		for (RGLValue numberGenerator : bagProducer1.getResultValue().asBag()) {
			/* stop being lazy and produce the value */
			assertTrue("I should have a number value ", numberGenerator
					.isRDFValue());
			numberGenerator.asRDFValue();
		}

		FunctionProcessor sumProc = new FunctionProcessor(new Sum5Function());
		sumProc.getPort(Sum5Function.value1).setInputProcessor(bagProducer1);
		sumProc.getPort(Sum5Function.value1).markIteration();

		sumProc.getPort(Sum5Function.value2).setInputProcessor(bagProducer2);
		sumProc.getPort(Sum5Function.value2).markIteration();

		assertTrue("cannot be ok, missing some values still ", TestUtil
				.getOutputType(sumProc) == null);
		sumProc.getPort(Sum5Function.value3).setInputProcessor(bagProducer2);
		sumProc.getPort(Sum5Function.value3).markIteration();

		sumProc.getPort(Sum5Function.value4).setInputProcessor(lit1generator);
		sumProc.getPort(Sum5Function.value5).setInputProcessor(lit2generator);

		assertTrue("must be well-typed",
				TestUtil.getOutputType(sumProc) instanceof BagType);

		/**
		 * check cardinality
		 */
		int card_out = sumProc.getResultValue().asBag().size();
		assertTrue(
				"Summing iterates over 3 ports, and output cardinality should thus be "
						+ size1 + "*" + size2 + "*" + size2 + "==" + size1
						* size2 * size2 + ", but it is " + card_out + ".",
				size1 * size2 * size2 == card_out);

		/**
		 * reset value generators re-check cardinality.
		 */
		bagProducer1.resetProcessorCache();
		bagProducer2.resetProcessorCache();
		sumProc.resetProcessorCache();

		AbstractBagValue sumBag = sumProc.getResultValue().asBag();

		card_out = sumBag.size();

		assertTrue(
				"Summing iterates over 3 ports, and output cardinality should thus be "
						+ size1 + "*" + size2 + "*" + size2 + "==" + size1
						* size2 * size2 + ", but it is " + card_out + ".",
				size1 * size2 * size2 == card_out);

		/**
		 * Check for correct values in both bagProducers
		 */
		int counter = 1;
		Iterator<RGLValue> bp1iter = bagProducer1.getResultValue().asBag()
				.iterator();
		while (bp1iter.hasNext()) {
			RGLValue val = bp1iter.next();
			double d = val.asLiteral().getValueDouble();
			/*
			 * should just be a bigger value every time, as that's in the
			 * DB/query.
			 */
			assertTrue("Expected " + counter + ", but value is " + d,
					d == counter);
			counter++;
		}

		/**
		 * Check for correct values in both bagProducers
		 */
		counter = 1;
		Iterator<RGLValue> bp2iter = bagProducer2.getResultValue().asBag()
				.iterator();
		while (bp2iter.hasNext()) {
			RGLValue val = bp2iter.next();
			double d = val.asRDFValue().getRDFNode().asLiteral().getDouble();
			/*
			 * should just be a bigger value every time, as that's in the
			 * DB/query.
			 */
			assertTrue("Expected " + counter + ", but value is " + d,
					d == counter);
			counter++;
		}

		/**
		 * Check for correct values in sumBag
		 */
		Iterator<RGLValue> bagIter = sumBag.iterator();
		HashBag fbagResult = new HashBag(); /*
											 * a bag to which all Double values
											 * of sumBag are copied
											 */
		while (bagIter.hasNext()) {
			RGLValue bagElem = bagIter.next();
			double d = bagElem.asRDFValue().getRDFNode().asLiteral()
					.getDouble();
			fbagResult.add(new Double(d)); /*
											 * add element to the FunctionalBag,
											 * which allows us to compare it
											 * later
											 */
		}

		/**
		 * Now simulate the addition: do a nested iteration over the
		 * bagProducers, thereby skipping the processor logic and lazy
		 * evaluation. Should yield the same results.
		 */
		HashBag fbagExpected = new HashBag(); /*
											 * a bag to contain all expected
											 * values of sumBag
											 */
		double d4 = lit1generator.getResultValue().asLiteral().getValueDouble();
		double d5 = lit2generator.getResultValue().asLiteral().getValueDouble();

		Iterator<RGLValue> b1iter = bagProducer1.getResultValue().asBag()
				.iterator();
		int b1counter = 1;
		while (b1iter.hasNext()) {
			RGLValue v1 = b1iter.next();
			double d1 = v1.asLiteral().getValueDouble(); /* extract value */
			assertTrue("value should be " + b1counter + ", but is " + d1
					+ " ? ", d1 == b1counter); /*
												 * we fetched them sorted, in
												 * order, and it are simply
												 * integers
												 */

			Iterator<RGLValue> b2iter = bagProducer2.getResultValue().asBag()
					.iterator();
			int b2counter = 1;
			while (b2iter.hasNext()) {
				RGLValue v2 = b2iter.next();
				double d2 = v2.asLiteral().getValueDouble(); /* extract value */
				assertTrue("value should be " + b2counter + ", but is " + d2
						+ " ? ", d2 == b2counter); /*
													 * we fetched them sorted,
													 * in order, and it are
													 * simply integers
													 */

				Iterator<RGLValue> b3iter = bagProducer2.getResultValue()
						.asBag().iterator();
				int b3counter = 1;
				while (b3iter.hasNext()) {
					RGLValue v3 = b3iter.next();
					double d3 = v3.asLiteral().getValueDouble();
					; /* extract value */
					assertTrue("value should be " + b3counter + ", but is "
							+ d3 + " ? ", d3 == b3counter); /*
															 * we fetched them
															 * sorted, in order,
															 * and it are simply
															 * integers
															 */

					/* sum values and add to bag */
					double result = d1 + d2 + d3 + d4 + d5;
					fbagExpected.add(new Double(result));
					// System.out.println(String.format("Simulating %.1f + %.1f + %.1f + %.1f + %.1f = %.1f",
					// d1,d2,d3,d4,d5, result));
					b3counter++;
				}
				b2counter++;
			}
			b1counter++;
		}

		/* check whether the results are the same */
		if (!fbagExpected.equals(fbagResult)) {
			/* error! */
			String errMsg = "Bags should contain same elements with same frequency, but this is not the case! See console output";
			System.out.println(errMsg + "\nExpected: " + fbagExpected);
			System.out.println("Result  : " + fbagResult);
			assertTrue(errMsg, false);
		}

	}

	/**
	 * THIS TEST ISN'T USEFUL AS THE OPTIONAL CLAUSE IS COMMENTED AND WE DO NOT
	 * SUPPORT IT UNFORTUNATELY
	 */
	@Test
	public void testNullBinding() {
		/* create a selectFunction with typing definition */
		int LIMIT = 4;
		String queryStr = "PREFIX ont: <http://ont/> \n "
				+ "PREFIX ns1: <http://ns1/> \n " + "SELECT ?s ?val1 ?val2 \n"
				+ "WHERE {\n" + "  Graph $g {"
				+ "    ?s ont:availableIntProperty ?val1. \n"
				+ "    #OPTIONAL { \n" + /* FIXME: SUPPORT OPTIONALS */
				"      ?s ont:prop2 ?val2. \n" + "    #}\n" + "  }\n"
				+ "} limit " + LIMIT;
		SPARQLFunction selectFunction = new SPARQLFunction();
		selectFunction.initialize(Collections.singletonMap("query", queryStr));

		selectFunction.requireInputType("g", GraphType.getInstance());
		assertTrue(
				"input 'g' was just required, so it must be registered as such",
				selectFunction.getRequiredInputNames().contains("g"));
		/*
		 * create a constructProcessor with constructFunction semantics, and set
		 * inputs
		 */
		FunctionProcessor selectProcessor = new FunctionProcessor(
				selectFunction);
		selectProcessor.getPort("g").setInputProcessor(
				new ConstantProcessor(Data
						.getGraphFromFile("./data/intvalues.xml")));
		assertTrue("A select processor must return a Bag", selectProcessor
				.getResultValue().isBag());
		AbstractBagValue bag = selectProcessor.getResultValue().asBag();
		assertTrue("Must have " + LIMIT + " entries, but have " + bag.size(),
				bag.size() == LIMIT);

		/**
		 * THIS IS A GOOD POINT TO CHECK FOR THE NULLNESS OF SOME
		 * BINDING-VALUES! But I did not yet decide on how to deal with these.
		 */
	}

	@Test
	public void testNetwork2() {
//		/**
//		 * create a selectProcessor with select function semantics, and set
//		 * inputs
//		 */
//		FunctionProcessor selectProcessor = new FunctionProcessor(
//				TestRGLFunctions.getSelectNrOfStudentsFunction());
//		assertTrue("Processor cannot be well-typed without 'graph1' input",
//				TestUtil.getOutputType(selectProcessor) == null);
//		selectProcessor.getPort("graph1").setInputProcessor(
//				new ConstantProcessor(Data
//						.getGraphFromFile("./data/tu-delft-eindhoven.xml")));
//		assertTrue("Processor must be well-typed", TestUtil
//				.getOutputType(selectProcessor) != null);
//
//		/**
//		 * create a selector for the nrOfStudents field. It will iterate over
//		 * the SPARQL-select output bag, and thus return a bag of nrOfStudents
//		 * values.
//		 */
//
//		RecordProject studentProject = new RecordProject();
//		studentProject.initialize(Collections.singletonMap(
//				RecordProject.CONFIGKEY_PROJECTFIELD, "nrOfStudents"));
//
//		FunctionProcessor nrcSelector = new FunctionProcessor(studentProject);
//		assertTrue("Processor cannot be well-typed without 'record' input",
//				TestUtil.getOutputType(nrcSelector) == null);
//		nrcSelector.getPort(RecordProject.INPUT_NAME).setInputProcessor(
//				selectProcessor);
//		assertTrue(
//				"Processor cannot accept a bag in input 'record' without iterating",
//				TestUtil.getOutputType(nrcSelector) == null);
//		nrcSelector.getPort(RecordProject.INPUT_NAME).markIteration(); // we are
//		// entering
//		// a bag
//		// of
//		// records,
//		// but
//		// it
//		// expects
//		// a
//		// record.
//
//		assertTrue("Input should be well-typed", TestUtil
//				.getOutputType(nrcSelector) != null);
//		assertTrue("Should return a bag, as we are iterating", TestUtil
//				.getOutputType(nrcSelector) instanceof BagType);
//		BagType bagType = (BagType) TestUtil.getOutputType(nrcSelector);
//		assertTrue("Input should be an RDF value, but it is "
//				+ bagType.getElemType(),
//				bagType.getElemType() instanceof RDFType);
//
//		/* now create a multiplier that iterates over both inputs */
//		FunctionProcessor multiplyProc2 = new FunctionProcessor(
//				new MultiplicationFunction());
//		multiplyProc2.getPort(MultiplicationFunction.value1).setInputProcessor(
//				nrcSelector);
//		multiplyProc2.getPort(MultiplicationFunction.value2).setInputProcessor(
//				nrcSelector);
//		multiplyProc2.getPort(MultiplicationFunction.value1).markIteration();
//		multiplyProc2.getPort(MultiplicationFunction.value2).markIteration();
//
//		assertTrue("MultiplyProc2 should return a BagType, but says "
//				+ TestUtil.getOutputType(multiplyProc2), TestUtil
//				.getOutputType(multiplyProc2) instanceof BagType);
//		RGLValue value2 = multiplyProc2.getResultValue();
//		assertTrue("Should be a bag", value2.isBag());
//		AbstractBagValue bagVal2 = value2.asBag();
//		assertTrue("Bag should have size 4, but has " + bagVal2.size(), bagVal2
//				.size() == 4);
//
//		/**
//		 * Create a multiplier that squares the number of students. It takes
//		 * Cartesian product of input-bags.
//		 */
//		FunctionProcessor multiplyProc = new FunctionProcessor(
//				new MultiplicationFunction());
//		multiplyProc.getPort(MultiplicationFunction.value1).setInputProcessor(
//				lit1generator);
//
//		/* first iterate only over input 'value2' */
//		assertTrue("Processor should be missing input 'value2'", TestUtil
//				.getOutputType(multiplyProc) == null);
//		multiplyProc.getPort(MultiplicationFunction.value2).setInputProcessor(
//				nrcSelector);
//		assertTrue(
//				"Processor cannot accept a bag in input 'value2' without iterating",
//				TestUtil.getOutputType(multiplyProc) == null);
//		multiplyProc.getPort(MultiplicationFunction.value2).markIteration();
//		assertTrue("MultiplyProc should return a BagType, but says "
//				+ TestUtil.getOutputType(multiplyProc), TestUtil
//				.getOutputType(multiplyProc) instanceof BagType);
//		bagType = (BagType) TestUtil.getOutputType(multiplyProc);
//		assertTrue(
//				"MultiplyProc should return a bag of RDFValues, but returns bag of "
//						+ bagType.getElemType(),
//				bagType.getElemType() instanceof RDFType);
//
//		RGLValue value = multiplyProc.getResultValue();
//		assertTrue("Should be a bag", value.isBag());
//		AbstractBagValue bagVal = value.asBag();
//		assertTrue("Bag should have size 2, but has " + bagVal.size(), bagVal
//				.size() == 2);

	}

}
