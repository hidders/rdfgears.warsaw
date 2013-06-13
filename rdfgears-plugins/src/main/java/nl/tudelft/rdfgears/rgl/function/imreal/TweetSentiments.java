package nl.tudelft.rdfgears.rgl.function.imreal;

import org.persweb.sentiment.eval.USemSentimentAnalysis;

import java.util.HashMap;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.GraphType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

/**
 * A function that computes the "average" sentiment score of a user's last 200 tweets:
 * 
 * (1) score each tweet as positive, negative, neutral
 * (2) compute final score: (pos-neg)/(pos+neg+neutral)
 * 
 * The output format is U-Sem format and makes use of:
 * Ontology: http://marl.gi2mo.org/0.2/ns.html
 * 
 * Output values
 * - positive opinion count
 * - negative opinion count
 * - neutral opinion count
 * - opinion count
 * - overall valency
 * 
 * If as input the UUID is provided as input well, the RDF output gives the UUID handle, otherwise it outputs the Twitter handle. * 
 * 
 * @author Claudia
 */
public class TweetSentiments extends SimplyTypedRGLFunction {

	public static final String INPUT_USERNAME = "username";
	public static final String INPUT_UUID = "uuid";
	public static final int MAXHOURS = 12; /* number of hours 'old' data (i.e. tweets retrieved earlier on) are still considered a valid substitute */

	public TweetSentiments() {
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
	
		HashMap<String,String> tweets = TweetCollector.getTweetTextWithDateAsKey(username, false, MAXHOURS);
		
		int positive = 0;
		int negative = 0;
		int neutral = 0;
		for(String date : tweets.keySet())
		{
			double result = USemSentimentAnalysis.analyzeTweetSentiment(tweets.get(date));
			
			if(result>0)
				positive++;
			else if(result<0)
				negative++;
			else
				neutral++;
			
			System.err.println("date of tweet: "+date+" -> "+positive+"/"+negative+"/"+neutral);
		}
		
		int total = positive + negative + neutral;
		double overall_score = (double)(positive-negative)/(double)(total);
		
		HashMap<String, Double> map = new HashMap<String, Double>();
		//these labels are fixed (MARL ontology)
		map.put("positiveOpinionsCount",(double)positive);
		map.put("neutralOpinionCount",(double)neutral);
		map.put("negativeOpinionCount",(double)negative);
		map.put("opinionCount",(double)total);
		map.put("aggregatesOpinion",overall_score);

		/*
		 * We must now convert the languageMap, that was the result of the
		 * external 'component', to an RGL value.
		 */

		RGLValue userProfile = null;
		try 
		{
			userProfile = UserProfileGenerator.generateProfile(this, (uuid.equals("")==true) ? username : uuid, map);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return ValueFactory.createNull("Error in "
					+ this.getClass().getCanonicalName() + ": "
					+ e.getMessage());
		}
		return userProfile;
	}
	
	
	
}
