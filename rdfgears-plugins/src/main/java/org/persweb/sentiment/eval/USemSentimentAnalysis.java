/*********************************************************
 *  Copyright (c) 2010-2012 by Web Information Systems (WIS) Group, 
 *  Delft University of Technology.
 *  Qi Gao, http://wis.ewi.tudelft.nl/index.php/home-qi-gao
 *  
 *  Some rights reserved.
 *
 *  Contact: q.gao@tudelft.nl
 *
 **********************************************************/
package org.persweb.sentiment.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.persweb.pipe.AlphaPipe;
import org.persweb.pipe.Pipe;
import org.persweb.sentiment.LexiconSimpleCount;
import org.persweb.sentiment.SentiWord;
import org.persweb.sentiment.SentiWordNet;
import org.persweb.sentiment.SentimentAnalysisStrategy;

/**
 * @author Qi Gao <a href="mailto:q.gao@tudelft.nl">q.gao@tudelft.nl</a>
 * @version created on Oct 10, 2012 11:52:17 AM
 * 
 */
public class USemSentimentAnalysis {

	private static Set<SentiWord> lexicon = new HashSet<SentiWord>();
	static {
		System.out.println("getting lexicon...");
		lexicon = getLexiconFromSentiWordNet();
		lexicon.addAll(getLexicon());
		System.out.println("size of lexicon:" + lexicon.size());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

	/**
	 * Returns the sentiment of given content, 1 means positive, -1 means
	 * negative and 0 means neutral
	 * 
	 * @param content
	 *            content
	 * @return
	 */
	public static double analyzeTweetSentiment(String content) {
		double sentiment = 0;
		SentimentAnalysisStrategy saStrategy = new LexiconSimpleCount(lexicon);
		Pipe pipe = new AlphaPipe();
		try {
			sentiment = saStrategy.getSentiment(content, pipe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sentiment;
	}

	/**
	 * @return
	 */
	private static Set<SentiWord> getLexicon() {
		Set<SentiWord> lexicon = new HashSet<SentiWord>();
		Set<String> positiveWords = new HashSet<String>(
				Arrays.asList(getContents(
						new File("../temp/rdfgears/lexicon/lexicon-twitter-positive"))
						.split(",")));
		Set<String> negativeWords = new HashSet<String>(
				Arrays.asList(getContents(
						new File("../temp/rdfgears/lexicon/lexicon-twitter-negative"))
						.split(",")));
		for (String word : positiveWords) {
			lexicon.add(new SentiWord(word.trim().toLowerCase(), 1.0, 0.0));
		}

		for (String word : negativeWords) {
			lexicon.add(new SentiWord(word.trim().toLowerCase(), 0.0, 1.0));
		}
		return lexicon;
	}

	/**
	 * @return
	 */
	private static Set<SentiWord> getLexiconFromSentiWordNet() {
		Set<SentiWord> lexicon = new HashSet<SentiWord>();
		SentiWordNet swn = new SentiWordNet();
		HashMap<String, Double> sentiWords = swn.getSentiWords();

		Iterator<String> it = sentiWords.keySet().iterator();
		String key;
		double score;
		while (it.hasNext()) {
			key = it.next();
			score = sentiWords.get(key);
			if (score > 0.25) {
				lexicon.add(new SentiWord(key, 1.0, 0.0));
			} else if (score < -0.25) {
				lexicon.add(new SentiWord(key, 0.0, 1.0));
			}
		}
		return lexicon;
	}

	/**
	 * Fetches the entire contents of a text file, and return it in a String.
	 * This style of implementation does not throw Exceptions to the caller.
	 * 
	 * @param aFile
	 *            is a file which already exists and can be read.
	 */
	private static String getContents(File file) {
		// ...checks on aFile are elided
		StringBuilder contents = new StringBuilder();

		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					file), "UTF-8");
			BufferedReader input = new BufferedReader(read);
			try {
				String line = null; // not declared within while loop
				/*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
				while ((line = input.readLine()) != null) {
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return contents.toString();
	}
}
