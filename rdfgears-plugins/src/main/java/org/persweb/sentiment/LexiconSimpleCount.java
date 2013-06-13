/*********************************************************
 *  Copyright (c) 2011 by Web Information Systems (WIS) Group, 
 *  Delft University of Technology.
 *  Qi Gao, http://wis.ewi.tudelft.nl/index.php/home-qi-gao
 *  
 *  Some rights reserved.
 *
 *  Contact: q.gao@tudelft.nl
 *
 **********************************************************/
package org.persweb.sentiment;

import java.util.List;
import java.util.Set;

import org.persweb.pipe.Pipe;

/**
 * @author Qi Gao <a href="mailto:q.gao@tudelft.nl">q.gao@tudelft.nl</a>
 * @version created on May 10, 2012 3:02:41 PM
 */
public class LexiconSimpleCount implements SentimentAnalysisStrategy {

	private Set<SentiWord> lexicon = null;
	private String name = "lexion-based strategy (term frequency)";
	
	/**
	 * 
	 * @param lexicon Seniword lexicon
	 */
	public LexiconSimpleCount(Set<SentiWord> lexicon) {
		super();
		this.lexicon = lexicon;
	}
	
	
	/**
	 * 
	 * @return the name of strategy
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.persweb.sentiment.SentimentAnalysisStrategy#getSentiment(java.lang.String, org.persweb.sentiment.Pipe)
	 */
	@Override
	public double getSentiment(String content, Pipe pipe) throws Exception {
		if (lexicon != null && !lexicon.isEmpty()) {
			List<String> words = pipe.processPipe(content);
			double positiveSum = 0.0;
			double negativeSum = 0.0;
			for(SentiWord sentiword : lexicon) {
				if(words.contains(sentiword.getTerm())) {
					positiveSum += sentiword.getPositiveScore();
					negativeSum += sentiword.getNegativeScore();
					
				}
			}
			if(positiveSum > negativeSum) {
				return 1.0;
			} else if(positiveSum < negativeSum) {
				return -1.0;
			}
			return 0.0;
		} else {
			String msg = "ERROR: no existing lexicon";
			throw new Exception(msg);
		}
	}

}
