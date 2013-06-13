package nl.tudelft.rdfgears.rgl.function;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.tudelft.rdfgears.engine.diskvalues.valuemanager.ValueManager;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.exception.WorkflowCheckingException;
import nl.tudelft.rdfgears.rgl.exception.WorkflowLoadingException;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.FieldIndexMapFactory;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * An RGLFunction can convert a Row<RGLValue> to an output RGLValue with the
 * execute function. An instance of RGLFunction can be reused for many
 * ReadingProcessors.
 * 
 * @author Eric Feliksik
 * 
 */
public abstract class RGLFunction implements Externalizable {

	private FieldIndexMap fieldIndexMap = null;

	/**
	 * contains same variables as fieldIndexMap, but ordered
	 */
	private ArrayList<String> requiredInputList = new ArrayList<String>(); 

	public boolean isLazy = true;

	/**
	 * Initialize the function with a configuration map.
	 * 
	 * Reconsider whether this function is necessary;;; very few functions need
	 * it (standard functions, e.g. record-project). Maybe it should be a
	 * separate class extending RGLFunction.
	 */
	public abstract void initialize(Map<String, String> config)
			throws WorkflowLoadingException;

	/**
	 * Perform the function of this class on the input row, and return the
	 * result.
	 * 
	 * @param inputRow
	 * @return the execution result value.
	 */
	public abstract RGLValue execute(ValueRow inputRow);

	/**
	 * The typechecking mechanism for this function. given a row over types,
	 * return the output type. If the processor is not defined for the input
	 * row, it must throw a TypingException. This function should never return
	 * null.
	 * 
	 * @param inputTypes
	 *            a mapping of names to input types.
	 * @return the output type
	 * @throws WorkflowCheckingException
	 */
	public abstract RGLType getOutputType(TypeRow inputTypes)
			throws WorkflowCheckingException;

	public final List<String> getRequiredInputNames() {
		return requiredInputList;
	}

	/**
	 * 
	 * @return
	 */
	public final FieldIndexMap getFieldIndexMap() {
		if (fieldIndexMap == null)
			fieldIndexMap = FieldIndexMapFactory.create(requiredInputList);
		return this.fieldIndexMap;
	}

	/**
	 * register an input name in this function. Should first use this function
	 * to set all names, and THEN you can call other functions, like
	 * getNumberOfArguments(), getArgumentNumber(), getRequiredInputNames(),
	 * etcetera.
	 * 
	 * @param field
	 */
	public final void requireInput(String field) {
		if (!requiredInputList.contains(field)) {
			requiredInputList.add(field);
		}
	}

	public boolean isLazy() {
		return this.isLazy;
	}

	public String getShortName() {
		String packageName = getClass().getPackage().getName();
		return getFullName().substring(packageName.length() + 1); // remove
																	// package
																	// name and
																	// the dot.
	}

	public String getFullName() {
		return getClass().getCanonicalName();
	}

	public String getRole() {
		return "java-function";
	}

	private transient int fId;
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		fId = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(ValueManager.registerFunction(this));		
	}
	
	public Object readResolve() {
		return ValueManager.getFunction(fId);
	}

}
