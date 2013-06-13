package nl.tudelft.rdfgears.rgl.function.imreal.userprofile;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.rgl.function.imreal.UserProfileGenerator;
import nl.tudelft.rdfgears.rgl.function.imreal.uuid.UUIDDBUtils;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A function that provides a user's profile entries
 * 
 */
public class GetUserProfileEntryFunction extends SimplyTypedRGLFunction {

	/**
	 * The name of the input field providing the uuid
	 */
	public static final String INPUT_UUID = "uuid";
	
	/**
	 * The name of the input field providing the topic
	 */
	public static final String INPUT_TOPIC = "topic";

	public GetUserProfileEntryFunction() {
		this.requireInputType(INPUT_UUID, RDFType.getInstance());
		this.requireInputType(INPUT_TOPIC, RDFType.getInstance());
	}

	@Override
	public RGLType getOutputType() {
		return GraphType.getInstance();
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		// typechecking the input
		RGLValue rdfValue = inputRow.get(INPUT_UUID);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		String uuid = rdfValue.asLiteral().getValueString();

		// typechecking the input
		rdfValue = inputRow.get(INPUT_TOPIC);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		String topic = rdfValue.asLiteral().getValueString();

		try {
			String email = UUIDDBUtils.findEmailbyUUID(uuid);
			return UserProfileGenerator.constructUserProfileEntryProfile(uuid, UserProfileDBUtils.retrieveUserProfile(uuid, topic));
		} catch (Exception e) {
			e.printStackTrace();
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}

	}
}
