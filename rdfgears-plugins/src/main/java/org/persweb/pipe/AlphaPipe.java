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
package org.persweb.pipe;

import java.util.ArrayList;
import java.util.List;

import org.persweb.genius.util.TwitterConstants;

/**
 * @author Qi Gao <a href="mailto:q.gao@tudelft.nl">q.gao@tudelft.nl</a>
 * @version created on May 10, 2012 4:03:22 PM
 */
public class AlphaPipe implements Pipe {

	public static void main(String args[]) {
		testPipe();
	}

	/**
	 * 
	 */
	public AlphaPipe() {
		super();
	}

	/**
	 * 
	 */
	private static void testPipe() {
		String str = "RT @yokoono: You don't have to be famous. you don't have to be rich, you don't have to have an http://eteat.com @user12";
		Pipe pipe = new AlphaPipe();
		System.out.println(pipe.processPipe(str));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.persweb.sentiment.pipe.Pipe#processPipe(java.lang.String)
	 */
	@Override
	public List<String> processPipe(String content) {
		List<String> words = new ArrayList<String>();
		content = content.replaceAll(TwitterConstants.REGEX_URL, "").replaceAll(
				TwitterConstants.REGEX_RETWEET, "").replaceAll(
				TwitterConstants.REGEX_ATUSER, "").trim();
		String[] splited = content.split(" ");
		String word;
		for (int i = 0; i < splited.length; i++) {
			//TODO: to remove the punctuation, but now also remove the ones in the middle of words such as don't
//			word = splited[i].trim().toLowerCase().replaceAll("[^\\w]", "");
			word = splited[i].trim().toLowerCase().replace(",", "").replace(".", "");
			if (!word.isEmpty()
					&& !TwitterConstants.LING_ENG_STOP_WORDS.contains(word))
				words.add(word);
		}
		return words;
	}

}
