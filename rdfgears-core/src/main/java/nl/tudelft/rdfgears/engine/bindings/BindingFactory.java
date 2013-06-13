package nl.tudelft.rdfgears.engine.bindings;

import java.util.List;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.engine.bindings.idvalues.IdBagBinding;
import nl.tudelft.rdfgears.engine.bindings.idvalues.IdModifiableRecordBinding;
import nl.tudelft.rdfgears.engine.bindings.idvalues.IdRGLBinding;
import nl.tudelft.rdfgears.engine.bindings.idvalues.IdRecordBinding;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.idvalues.IdRenewablyIterableBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractRecordValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryLiteralValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.MemoryURIValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.EmptyBag;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.SingletonBag;
import nl.tudelft.rdfgears.rgl.function.core.BagCategorize.CategoryBag;
import nl.tudelft.rdfgears.rgl.function.core.BagFlatten.FlattenedBagValue;
import nl.tudelft.rdfgears.rgl.function.core.BagUnion.UnionBagValue;
import nl.tudelft.rdfgears.rgl.function.standard.FilterFunction.FilteringBagValue;
import nl.tudelft.rdfgears.rgl.workflow.LazyRGLValue;

import com.sleepycat.bind.tuple.TupleBinding;

public class BindingFactory {
	public static TupleBinding<List<RGLValue>> createListBinding() {
		if (ValueFactory.isDiskBased()) {
			return new DiskListBinding();
		} else {
			return new RGLListBinding();
		}
	}

	/**
	 * This method should only be used to get binding when deserializing objects
	 * from DB.
	 * 
	 * For the purpose of serialization always use RGLValue.getBinding().
	 * 
	 * @param className
	 *            Class of which object we would like to deserialize from DB
	 * @return Binding designed to read object.
	 * 
	 * 
	 */
	public static TupleBinding<RGLValue> createBinding(String className) {
		try {
			// Engine.getLogger().info(className);
			if (isIdClassName(className)) {
				if (className.equals(IdModifiableRecord.class.getName())) {
					return new IdModifiableRecordBinding();
				} else if (className.equals(IdRecordValue.class.getName())) {
					return new IdRecordBinding();
				} else if (className.equals(IdRenewablyIterableBag.class
						.getName())) {
					return new IdBagBinding();
				} else {
					return new IdRGLBinding();
				}
			} else if (className.equals(MemoryLiteralValue.class.getName())) {
				return new MemoryLiteralBinding();
			} else if (className.equals(MemoryURIValue.class.getName())) {
				return new MemoryURIBinding();
			} else if (className.equals(LazyRGLValue.class.getName())) {
				return new LazyRGLBinding();
			} else if (isRecordClassName(className)) {
				return new RecordBinding();
			} else if (isBagClassName(className)) {
				if (className.equals(UnionBagValue.class.getName())) {
					return new UnionBagBinding();
				} else if (className.equals(SingletonBag.class.getName())) {
					return new SingletonBagBinding();
				} else if (className.equals(EmptyBag.class.getName())) {
					return new EmptyBagBinding();
				} else if (className.equals(ListBackedBagValue.class.getName())) {
					return new ListBackedBagBinding();
				} else if (className.equals(FilteringBagValue.class.getName())) {
					return new FilteringBagBinding();
				} else if (className.equals(FlattenedBagValue.class.getName())) {
					return new FlattenedBagBinding();
				} else if (className.equals(CategoryBag.class.getName())) {
					return new CategoryBagBinding();
				} else {
					Engine.getLogger().info("NaiveBagBinding for " + className);
					return new NaiveBagBinding();
				}
			} else {
				Engine.getLogger().debug(
						"Don't know the binding for" + className
								+ ", returning the PoorMans one.");
				return new MemoryBinding();
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Class " + className + " not found.");
		}
	}

	public static boolean isBagClassName(String className)
			throws ClassNotFoundException {
		return isAssignable(AbstractBagValue.class, className);
	}

	private static boolean isRecordClassName(String className)
			throws ClassNotFoundException {
		return isAssignable(AbstractRecordValue.class, className);
	}

	private static boolean isIdClassName(String className)
			throws ClassNotFoundException {
		return isAssignable(IdRGLValue.class, className);
	}

	private static boolean isAssignable(Class<?> cls, String className)
			throws ClassNotFoundException {
		return cls.isAssignableFrom(Class.forName(className));
	}
}