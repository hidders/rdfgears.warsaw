package nl.tudelft.rdfgears.rgl.function.imreal.uuid;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A function to store UUID into the database and linking it to social IDs
 * 
 */
public class RegisterSocialIDFunction extends SimplyTypedRGLFunction {

	/**
	 * The name of the input field providing the uuid
	 */
	public static final String INPUT_UUID = "uuid";

	/**
	 * The name of the input field providing the webid
	 */
	public static final String INPUT_WEBID = "webid";

	/**
	 * The name of the input field providing the webid provider
	 */
	public static final String WEBID_TYPE = "type";

	public RegisterSocialIDFunction() {
		this.requireInputType(INPUT_UUID, RDFType.getInstance());
		this.requireInputType(INPUT_WEBID, RDFType.getInstance());
		this.requireInputType(WEBID_TYPE, RDFType.getInstance());
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

		// extracting the twitter username from the input
		String uuid = rdfValue.asLiteral().getValueString();

		// /////////////////////////////////////////////////////////////////

		// typechecking the input
		rdfValue = inputRow.get(INPUT_WEBID);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		// extracting the twitter username from the input
		String webid = rdfValue.asLiteral().getValueString();

		// ////////////////////////////////////////////////////////////////

		// typechecking the input
		rdfValue = inputRow.get(WEBID_TYPE);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		// extracting the twitter username from the input
		String provider = rdfValue.asLiteral().getValueString();

		// ////////////////////////////////////////////////////////////////

		int existing_uuid;
		try {
			existing_uuid = UUIDDBUtils.findUUIDbyName(uuid);
		} catch (Exception e) {
			e.printStackTrace();
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}

		if (existing_uuid == 0)
			return ValueFactory
					.createNull("The service cannot be executed because the provided UUID is not registered in the system. The \"registerUUID\" service can be used for registering new UUID.");

		try {
			UUIDDBUtils.storeWebid(existing_uuid,webid, provider);
		} catch (Exception e) {
			e.printStackTrace();
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}

		return ValueFactory.createNull("UUID/WebID stored successfully.");
	}

}
