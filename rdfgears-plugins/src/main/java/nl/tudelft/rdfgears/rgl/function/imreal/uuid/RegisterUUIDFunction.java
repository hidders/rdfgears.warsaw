package nl.tudelft.rdfgears.rgl.function.imreal.uuid;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A function to register UUID into the database
 * 
 */
public class RegisterUUIDFunction extends SimplyTypedRGLFunction {

	/**
	 * The name of the input field providing the email address
	 */
	public static final String INPUT_EMAIL = "email";

	public RegisterUUIDFunction() {
		this.requireInputType(INPUT_EMAIL, RDFType.getInstance());
	}

	@Override
	public RGLType getOutputType() {
		return BagType.getInstance(RDFType.getInstance());
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		// typechecking the input
		RGLValue rdfValue = inputRow.get(INPUT_EMAIL);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		// extracting the twitter username from the input
		String email = rdfValue.asLiteral().getValueString();
		
		System.err.println("Attempting to register email adress: "+email);

		try 
		{
			String existingUUID = UUIDDBUtils.findUUIDbyEmail(email);
			
			if(existingUUID != null){
				return ValueFactory.createLiteralPlain(existingUUID, null);
			}
			
			String generatedUUID = UUIDDBUtils.storeNewUUID(email);
			
			return ValueFactory.createLiteralPlain(generatedUUID, null);
			 
		} catch (Exception e) {
			e.printStackTrace();
			UUIDDBUtils.printLoginInformation();
			
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
			
			
		}
	}

}
