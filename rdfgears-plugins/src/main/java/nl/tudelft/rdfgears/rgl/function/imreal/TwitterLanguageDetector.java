
package nl.tudelft.rdfgears.rgl.function.imreal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import nl.tudelft.rdfgears.engine.Config;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * A function to detect twitter languages based on a Twitter username.
 * 
 * If as input the UUID is provided as input well, the RDF output gives the UUID handle, otherwise it outputs the Twitter handle.
 * 
 * @author Claudia
 */
public class TwitterLanguageDetector extends SimplyTypedRGLFunction {

	public static final String INPUT_USERNAME = "username";
	public static final String INPUT_UUID = "uuid";
	
	public static final int MAXHOURS = 2*24; /* number of hours 'old' data (i.e. tweets retrieved earlier on) are still considered a valid substitute */

	/*
	 * profiles can only be loaded once, otherwise the language library crashes.
	 * Static because multiple instances of this RGLFunctions may exist.
	 */
	public static boolean profilesLoaded = false;

	public TwitterLanguageDetector() {
		this.requireInputType(INPUT_USERNAME, RDFType.getInstance());
		this.requireInputType(INPUT_UUID, RDFType.getInstance());
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
		RGLValue rdfValue = inputRow.get(INPUT_USERNAME);
		if (!rdfValue.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());

		// we are happy, value can be safely cast with .asLiteral().
		String username = rdfValue.asLiteral().getValueString();

		RGLValue rdfValue2 = inputRow.get(INPUT_UUID);
		if (!rdfValue2.isLiteral())
			return ValueFactory.createNull("Cannot handle URI input in "
					+ getFullName());
		String uuid = rdfValue2.asLiteral().getValueString();
		
		HashMap<String, Double> languageMap;
		try 
		{
			languageMap = detectLanguage(username);
		} catch (Exception e) 
		{
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}

		/*
		 * We must now convert the languageMap, that was the result of the
		 * external 'component', to an RGL value.
		 */

		RGLValue userProfile = null;
		try 
		{
			userProfile = UserProfileGenerator.generateProfile(this, (uuid.equals("")==true) ? username : uuid, languageMap);
		} 
		catch (Exception e) 
		{
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}
		return userProfile;
	}


	/**
	 * will throw Exception on failure
	 * 
	 * @param twitterUser
	 * @return
	 * @throws LangDetectException
	 * @throws IOException
	 */
	protected HashMap<String, Double> detectLanguage(String twitterUser)
			throws LangDetectException, IOException {
		
		HashMap<String,String> tweets = TweetCollector.getTweetTextWithDateAsKey(twitterUser, true, MAXHOURS);

		/* *************
		 * The dir with the language profiles is assumed to be stored in the
		 * tmpdir. As it is read-only, it may be nicer to package it in the jar
		 * instead.... But the jar directory contents are not easily listed by
		 * the langdetect tool.
		 */
		File profileDir = new File(Config.getWritableDir()
				+ "/imreal-language-profiles"); /*
												 * should be cross-platform and
												 * work in webapps
												 */
		if (!profilesLoaded) {
			DetectorFactory.loadProfile(profileDir);
			profilesLoaded = true;
		}

		HashMap<String, Double> languageMap = new HashMap<String, Double>();

		for(String key : tweets.keySet())
		{
			String tweetText = tweets.get(key);

			// language of the tweet
			Detector detect = DetectorFactory.create();
			detect.append(tweetText);
			String lang = detect.detect();
			if (languageMap.containsKey(lang) == true) 
			{
				double val = languageMap.get(lang) + 1;
				languageMap.put(lang, val);
			} 
			else
				languageMap.put(lang, 1.0);
		}
		return languageMap;
	}

}
