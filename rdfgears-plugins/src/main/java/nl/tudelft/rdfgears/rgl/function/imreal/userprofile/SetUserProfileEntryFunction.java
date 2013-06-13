package nl.tudelft.rdfgears.rgl.function.imreal.userprofile;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A function to store user profile entries into the database
 * 
 */
public class SetUserProfileEntryFunction extends SimplyTypedRGLFunction {

	/**
	 * The name of the input field providing the uuid
	 */
	public static final String INPUT_UUID = "uuid";

	/**
	 * The name of the input field providing the topic
	 */
	public static final String INPUT_TOPIC = "topic";

	/**
	 * The name of the input field providing the value
	 */
	public static final String INPUT_VALUE = "value";

	public SetUserProfileEntryFunction() {
		this.requireInputType(INPUT_UUID, RDFType.getInstance());
		this.requireInputType(INPUT_TOPIC, RDFType.getInstance());
		this.requireInputType(INPUT_VALUE, RDFType.getInstance());
	}

	@Override
	public RGLType getOutputType() {
		return BagType.getInstance(RDFType.getInstance());
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		// typechecking the input
		RGLValue rdfValue = inputRow.get(INPUT_UUID);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		String uuid = rdfValue.asLiteral().getValueString();

		// ////////////////////////////////////////////////////////////////

		// typechecking the input
		rdfValue = inputRow.get(INPUT_TOPIC);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		String topic = rdfValue.asLiteral().getValueString();

		// ////////////////////////////////////////////////////////////////

		// typechecking the input
		rdfValue = inputRow.get(INPUT_VALUE);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		String value = rdfValue.asLiteral().getValueString();

	
		try {
			UserProfileDBUtils.storeProfileEntry(uuid, topic, value);
		} catch (Exception e) {
			e.printStackTrace();
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}

		return ValueFactory
				.createNull("The user profile entry has been stored successfully.");
	}
}
