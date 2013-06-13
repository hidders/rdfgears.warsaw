package nl.tudelft.rdfgears.engine.bindings;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.diskvalues.DatabaseManager;
import nl.tudelft.rdfgears.engine.diskvalues.DiskList;
import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.value.BagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.LiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.RenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryLiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryURIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.SingletonBag;
import nl.tudelft.rdfgears.rgl.function.core.BagUnion;
import nl.tudelft.rdfgears.rgl.function.core.BagUnion.UnionBagValue;
import nl.tudelft.rdfgears.rgl.function.standard.FilterFunction;
import nl.tudelft.rdfgears.rgl.function.standard.NotNull;
import nl.tudelft.rdfgears.util.row.FieldIndexHashMap;
import nl.tudelft.rdfgears.util.row.FieldMappedValueRow;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.impl.XSDDouble;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.DatabaseEntry;

public class BindingsTest {

	private static final int SIZE = 10;

	public static RenewablyIterableBag getBagOfDouble() {
		return (RenewablyIterableBag) ValueFactory
				.registerValue(new ListBackedBagValue(getListOfDouble()));
	}

	private RenewablyIterableBag getBagOfDoubleAndNull() {
		return (RenewablyIterableBag) ValueFactory
				.registerValue(new ListBackedBagValue(
						getDiskListOfDoubleAndNull()));
	}

	private static DiskList getDiskListOfDouble() {
		DiskList list = new DiskList();
		for (double i = 0.0; i < SIZE; ++i) {
			list.add(MemoryLiteralValue.createLiteralTyped(i, new XSDDouble(
					"double")));
		}
		return list;
	}
	
	private static List<RGLValue> getListOfDouble() {
		List<RGLValue> list = new ArrayList<RGLValue>();
		for (double i = 0.0; i < SIZE; ++i) {
			list.add(MemoryLiteralValue.createLiteralTyped(i, new XSDDouble(
					"double")));
		}
		return list;
	}

	private DiskList getDiskListOfDoubleAndNull() {
		DiskList list = new DiskList();
		for (double i = 0.0; i < SIZE; ++i) {
			list.add(MemoryLiteralValue.createLiteralTyped(i, new XSDDouble(
					"double")));
			list.add(ValueFactory.createNull("baba"));
		}
		return list;
	}

	private AbstractRecordValue getRecord() {
		FieldIndexHashMap fiMap = new FieldIndexHashMap();

		for (char a = 'a'; a < 'z'; ++a) {
			fiMap.addFieldName(String.valueOf(a));
		}

		FieldMappedValueRow row = new FieldMappedValueRow(fiMap);

		for (char a = 'a'; a < 'z'; ++a) {
			row.put(String.valueOf(a), getBagOfDouble());
		}

		return new MemoryRecordValue(row);
	}

	private LiteralValue getMemoryLiteral(double d) {
		return MemoryLiteralValue
				.createLiteralTyped(d, new XSDDouble("double"));
	}

	private LiteralValue getMemoryLiteral(String s) {
		return MemoryLiteralValue.createPlainLiteral(s, "en");
	}

	public static RGLValue thereAndBack(RGLValue val) {
		DatabaseEntry dbe = new DatabaseEntry();
		val.getBinding().objectToEntry(val, dbe);
		return val.getBinding().entryToObject(dbe);
	}

	private DiskList thereAndBack(DiskList val) {
		DatabaseEntry dbe = new DatabaseEntry();
		DiskListBinding tb = new DiskListBinding();
		tb.objectToEntry(val, dbe);
		return (DiskList) tb.entryToObject(dbe);
	}

	public static boolean sameDoubleBags(AbstractBagValue bag1,
			AbstractBagValue bag2) {
		Iterator<RGLValue> it1 = bag1.iterator();
		Iterator<RGLValue> it2 = bag2.iterator();

		for (int i = 0; i < SIZE; ++i) {
			double d1 = it1.next().asLiteral().getValueDouble();
			double d2 = it2.next().asLiteral().getValueDouble();
			assertTrue(d1 + "!=" + d2, d1 == d2);
		}

		return true;
	}

	private static void printBag(AbstractBagValue bag) {
		for (RGLValue v : bag) {
			System.out.println(v);
		}
	}

	@Before
	public void before() {
		DatabaseManager.initialize();
	}

	@Test
	public void testSimpleBinidngs() {
		LiteralValue l = getMemoryLiteral(1.0);
		assertTrue(thereAndBack(l).asLiteral().getValueDouble() == l
				.asLiteral().getValueDouble());

		l = getMemoryLiteral("foo");
		assertTrue(
				l.asLiteral().getValueString() + " != "
						+ thereAndBack(l).asLiteral().getValueString(),
				thereAndBack(l).asLiteral().getValueString()
						.equals(l.asLiteral().getValueString()));

		MemoryURIValue uri = new MemoryURIValue("dbpedia.org");
		assertTrue(uri.compareTo(thereAndBack(uri)) == 0);

		IdRGLValue dv1 = new IdRGLValue(1152);
		IdRGLValue dv2 = (IdRGLValue) thereAndBack(dv1);

		assertTrue(dv1.getId() == dv2.getId());
	}

	@Test
	public void testRGLListSimple() {
		List<RGLValue> list1 = new ArrayList<RGLValue>();
		TupleBinding<List<RGLValue>> binding = new RGLListBinding();
		DatabaseEntry entry = new DatabaseEntry();

		for (double d = 0.0; d < 100.0; ++d) {
			list1.add(getMemoryLiteral(d));
		}

		binding.objectToEntry(list1, entry);
		List<RGLValue> list2 = binding.entryToObject(entry);

		for (int i = 0; i < list1.size(); ++i) {
			assertTrue(list1.get(i).asLiteral()
					.compareTo(list2.get(i).asLiteral()) == 0);
		}

	}

	@Test
	public void testRGLListComplex() {
		List<RGLValue> list1 = new ArrayList<RGLValue>();
		TupleBinding<List<RGLValue>> binding = new RGLListBinding();
		DatabaseEntry entry = new DatabaseEntry();

		for (int i = 0; i < 100; ++i) {
			list1.add(getBagOfDouble());
		}

		binding.objectToEntry(list1, entry);
		List<RGLValue> list2 = binding.entryToObject(entry);

		for (int i = 0; i < list1.size(); ++i) {
			assertTrue(sameDoubleBags(list1.get(i).asBag(), list2.get(i)
					.asBag()));
		}
	}

	@Test
	public void testEmptyBag() {
		AbstractBagValue emptyBag = ValueFactory.createBagEmpty();
		List<RGLValue> backingList = new DiskList();

		for (int i = 0; i < 100; ++i) {
			backingList.add(emptyBag);
		}

		BagValue outer = new ListBackedBagValue(backingList);

		assertTrue(outer.size() == 100);
		assertTrue(outer.asBag().iterator().next().isBag());
		assertTrue(outer.asBag().iterator().next().asBag().size() == 0);
	}

	@Test
	public void testNaiveBag() {
		AbstractBagValue bag1 = getBagOfDouble();

		ComplexBinding naive = new NaiveBagBinding();
		DatabaseEntry entry = new DatabaseEntry();
		naive.objectToEntry(bag1, entry);

		AbstractBagValue bag2 = naive.entryToObject(entry).asBag();

		assertTrue(sameDoubleBags(bag1, bag2));

	}

	@Test
	public void testSingletonBag() {
		SingletonBag b = new SingletonBag(ValueFactory.createLiteralDouble(2.0));

		SingletonBag c = (SingletonBag) thereAndBack(b);

		assertTrue(b.iterator().next().asLiteral().getValueDouble() == c
				.iterator().next().asLiteral().getValueDouble());
	}

	@Test
	public void testRecord() {
		AbstractRecordValue record1 = getRecord();
		AbstractRecordValue record2 = thereAndBack(record1).asRecord();
		for (String s : record2.getRange()) {
			assertTrue(sameDoubleBags(record2.get(s).asBag(), getBagOfDouble()));
		}
	}

	@Test
	public void testJCSRecord() {
		AbstractRecordValue record1 = getRecord();
		AbstractRecordValue record2 = getRecord();
		ValueManager.registerValue(record1);
		ValueManager.registerValue(record2);
		AbstractRecordValue record3 = (AbstractRecordValue) ValueManager
				.fetchValue(record1.getId());
		for (String s : record3.getRange()) {
			assertTrue(sameDoubleBags(record3.get(s).asBag(), getBagOfDouble()));
		}

		// for (RGLValue v : record3.get("a").asBag()) {
		// System.out.println(v);
		// }
	}

//	@Test
//	public void testUnionBag() {
//		RenewablyIterableBag bag1 = getBagOfDouble();
//		RenewablyIterableBag bag2 = getBagOfDouble();
//
//		UnionBagValue union1 = BagUnion.createUnionBag(ValueFactory.getNewId(),
//				new HashMap<Long, Integer>(), bag1, bag2);
//
//		UnionBagValue union3 = BagUnion.createUnionBag(ValueFactory.getNewId(),
//				new HashMap<Long, Integer>(), bag1, bag2);
//
//		union1.prepareForMultipleReadings();
//
//		Iterator<RGLValue> it = union1.iterator();
//		it.next();
//		// for (int i = 0; i < 50; ++i) {
//		// it.next();
//		// }
//
//		AbstractBagValue union2 = thereAndBack(union1).asBag();
//
//		// printBag(union1);
//		System.out.println();
//		printBag(union3);
//		// assertTrue(sameDoubleBags(union1, union2));
//		assertTrue(sameDoubleBags(union2, union3));
//		assertTrue(sameDoubleBags(union2, union3));
//	}

	@Test
	public void testDiskListBinding() {
		DiskList dl1 = getDiskListOfDouble();
		DiskList dl2 = getDiskListOfDouble();
		DiskList dl3 = thereAndBack(dl1);

		BagValue bag1 = new ListBackedBagValue(dl2);
		BagValue bag2 = new ListBackedBagValue(dl3);

		assertTrue(sameDoubleBags(bag1, bag2));
	}

	@Test
	public void listIterator() {
		DiskList dl = getDiskListOfDouble();

		assertTrue(dl.listIterator().next().asLiteral().getValueDouble() == 0.0);
		assertTrue(dl.listIterator(2).next().asLiteral().getValueDouble() == 2.0);

		assertTrue(!dl.listIterator(0).hasPrevious());

	}

//	@Test
//	public void testFilterFunction() {
//		RenewablyIterableBag inputBag = getBagOfDoubleAndNull();
//
//		BagValue b1 = FilterFunction.createFilteringBag(
//				ValueFactory.getNewId(), inputBag, new NotNull());
//
//		BagValue b2 = FilterFunction.createFilteringBag(
//				ValueFactory.getNewId(), inputBag, new NotNull());
//
//		BagValue b3 = FilterFunction.createFilteringBag(
//				ValueFactory.getNewId(), inputBag, new NotNull());
//
//		BagValue b4 = FilterFunction.createFilteringBag(
//				ValueFactory.getNewId(), inputBag, new NotNull());
//
//		b1.prepareForMultipleReadings();
//		Iterator<RGLValue> it;
//
//		it = b1.asBag().iterator();
//
//		for (int i = 0; i < 2; ++i) {
//			it.next();
//		}
//
//		it = b1.asBag().iterator();
//
//		for (int i = 0; i < 5; ++i) {
//			it.next();
//		}
//
//		AbstractBagValue c1 = thereAndBack(b1).asBag();
//		AbstractBagValue c2 = thereAndBack(b2).asBag();
//
//		assertTrue("Bags have different contents", sameDoubleBags(b3, c1));
//		assertTrue("Bags have different contents", sameDoubleBags(b4, c2));
//	}
}
