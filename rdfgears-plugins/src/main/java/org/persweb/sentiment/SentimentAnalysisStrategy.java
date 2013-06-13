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

import org.persweb.pipe.Pipe;

/**
 * @author Qi Gao <a href="mailto:q.gao@tudelft.nl">q.gao@tudelft.nl</a>
 * @version created on May 10, 2012 3:00:26 PM
 */
public interface SentimentAnalysisStrategy {
		
	/**
	 * Returns the sentiment of given content  
	 * 
	 * @param content content for which the sentiment is extracted
	 * @param pipe the pipe used to process the content.
	 * @return
	 * @throws Exception
	 */
	public double getSentiment(String content, Pipe pipe) throws Exception;
	
	/**
	 * Returns the name of strategy
	 * 
	 * @return the name of strategy
	 */
	public String getName();

}
