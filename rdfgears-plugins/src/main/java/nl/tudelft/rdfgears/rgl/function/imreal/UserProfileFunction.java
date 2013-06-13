package nl.tudelft.rdfgears.rgl.function.imreal;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * This is a wrapper for UserProfileGenerator, to enable the workflow-based
 * output be wrapped into the U-Sem format.
 * 
 * (has nothing to do with userprofile/UserProfile.java)
 * 
 * TODO: change terminology, refactor code
 * 
 * @author Claudia
 */
public class UserProfileFunction extends SimplyTypedRGLFunction {

	public static final String INPUT_WEIGHT = "wo:weight";
	public static final String INPUT_TOPIC = "wi:topic";
	public static final String INPUT_USERID = "rdf:about";//social ID or UUID
	
	public UserProfileFunction() {
		this.requireInputType(INPUT_USERID, RDFType.getInstance());
		this.requireInputType(INPUT_TOPIC, RDFType.getInstance());
		this.requireInputType(INPUT_WEIGHT, RDFType.getInstance());
	}

	public RGLType getOutputType() {
		return GraphType.getInstance();
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		/*
		 * - typechecking guarantees it is an RDFType - simpleExecute guarantees
		 * it is non-null SanityCheck: we must still check whether it is URI or
		 * String, because typechecking doesn't distinguish this!
		 */
		RGLValue rdfValue = inputRow.get(INPUT_USERID);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		String userid = rdfValue.asLiteral().getValueString();

		RGLValue rdfValue2 = inputRow.get(INPUT_TOPIC);
		if (!rdfValue2.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());
		String topic = rdfValue2.asLiteral().getValueString();
		
		RGLValue rdfValue3 = inputRow.get(INPUT_WEIGHT);
		if (!rdfValue3.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());
		String weight = rdfValue3.asLiteral().getValueString();

		RGLValue userProfile = null;
		try 
		{
			userProfile = UserProfileGenerator.generateProfile(userid, topic, weight);
		} 
		catch (Exception e) 
		{
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}
		return userProfile;
	}
}