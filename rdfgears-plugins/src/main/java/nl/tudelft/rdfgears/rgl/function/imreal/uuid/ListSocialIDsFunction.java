package nl.tudelft.rdfgears.rgl.function.imreal.uuid;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.rgl.function.imreal.UserProfileGenerator;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A function that provides a list of web IDs for a given UUID
 * 
 */
public class ListSocialIDsFunction extends SimplyTypedRGLFunction {

	/**
	 * The name of the input field providing the uuid
	 */
	public static final String INPUT_UUID = "uuid";

	public ListSocialIDsFunction() {
		this.requireInputType(INPUT_UUID, RDFType.getInstance());
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

		// extracting the twitter username from the input
		String uuid = rdfValue.asLiteral().getValueString();

		try {
			return UserProfileGenerator.constructSocialIDListProfile(uuid,
					UUIDDBUtils.retrieve(uuid));
		} catch (Exception e) {
			e.printStackTrace();
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}

	}
}
