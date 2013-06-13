package org.persweb.genius.util;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TwitterConstants {

	/**
	 * regular expression for hashtags
	 */
	public static final String REGEX_HASHTAG = "\\s#[^\\s]+";

	/**
	 * regular expression URL
	 */
	public static final String REGEX_URL = "https?://([-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|])";

	/**
	 * regular expression for reply tweets pattern
	 */
	public static final String REPLYREGEX = "^@([a-z0-9_]{1,20})";

	/**
	 * 
	 */
	public static final String REGEX_RETWEET = "(RT|retweet|from|via)\\s*@([a-z0-9_]{1,20}):*";
	
	/**
	 * 
	 */
	public static final String REGEX_ATUSER = "@([a-z0-9_]{1,20})";

	/**
	 * 
	 */
	public static Set<String> LING_ENG_STOP_WORDS;
	
	/**
	 * a list of english stop words (based on <a
	 * href="http://www.cs.cmu.edu/~mccallum/bow/rainbow/" ></a>) but removed
	 * some question words such as 'why', 'who'.
	 */
	static {
		String[] s_stopWords = { "m", "a", "about", "above", "above", "across",
				"after", "afterwards", "again", "against", "all", "almost",
				"alone", "along", "already", "also", "although", "always",
				"am", "among", "amongst", "amoungst", "amount", "an", "and",
				"another", "any", "anyhow", "anyone", "anything", "anyway",
				"anywhere", "are", "around", "as", "at", "back", "be",
				"became", "because", "become", "becomes", "becoming", "been",
				"before", "beforehand", "behind", "being", "below", "beside",
				"besides", "between", "beyond", "bill", "both", "bottom",
				"but", "by", "call", "can", "cannot", "cant", "co", "con",
				"could", "couldnt", "cry", "de", "describe", "detail", "do",
				"done", "down", "due", "during", "each", "eg", "eight",
				"either", "eleven", "else", "elsewhere", "empty", "enough",
				"etc", "even", "ever", "every", "everyone", "everything",
				"everywhere", "except", "few", "fifteen", "fify", "fill",
				"find", "fire", "first", "five", "for", "former", "formerly",
				"forty", "found", "four", "from", "front", "full", "further",
				"get", "give", "go", "had", "has", "hasnt", "have", "he",
				"hence", "her", "here", "hereafter", "hereby", "herein",
				"hereupon", "hers", "herself", "him", "himself", "his", "how",
				"however", "hundred", "ie", "if", "in", "inc", "indeed",
				"interest", "into", "is", "it", "its", "itself", "keep",
				"last", "latter", "latterly", "least", "less", "ltd", "made",
				"many", "may", "me", "meanwhile", "might", "mill", "mine",
				"more", "moreover", "most", "mostly", "move", "much", "must",
				"my", "myself", "name", "namely", "neither", "never",
				"nevertheless", "next", "nine", "no", "nobody", "none",
				"noone", "nor", "not", "nothing", "now", "nowhere", "of",
				"off", "often", "on", "once", "one", "only", "onto", "or",
				"other", "others", "otherwise", "our", "ours", "ourselves",
				"out", "over", "own", "part", "per", "perhaps", "please",
				"put", "rather", "re", "same", "see", "seem", "seemed",
				"seeming", "seems", "serious", "several", "she", "should",
				"show", "side", "since", "sincere", "six", "sixty", "so",
				"some", "somehow", "someone", "something", "sometime",
				"sometimes", "somewhere", "still", "such", "system", "take",
				"ten", "than", "that", "the", "their", "them", "themselves",
				"then", "thence", "there", "thereafter", "thereby",
				"therefore", "therein", "thereupon", "these", "they", "thickv",
				"thin", "third", "this", "those", "though", "three", "through",
				"throughout", "thru", "thus", "to", "together", "too", "top",
				"toward", "towards", "twelve", "twenty", "two", "un", "under",
				"until", "up", "upon", "us", "very", "via", "was", "we",
				"well", "were", "whatever", "whence", "whenever", "whereafter",
				"whereas", "whereby", "wherein", "whereupon", "wherever",
				"whether", "while", "whither", "whoever", "whole", "will",
				"with", "within", "without", "would", "yet", "you", "your",
				"yours", "yourself", "yourselves", "the" };
		LING_ENG_STOP_WORDS = new HashSet<String>(Arrays.asList(s_stopWords));
	}

	public static Map<Timestamp, Long> ESTIMATED_TIMESTAMP_TWEETID = new HashMap<Timestamp, Long>();
	static {
		ESTIMATED_TIMESTAMP_TWEETID.put(
				Timestamp.valueOf("2011-02-28 00:00:01"), 41996001489321984L);
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String content = "RT@cnnbrk: As #G-20 Summit ends, #Obama says he's not caving on #tax cuts http://on.cnn.com/9wY3dk @antoheruser12";
		System.out.println(content.replaceAll(REGEX_RETWEET, "").replaceAll(REGEX_ATUSER, "").trim());
		
	}

}
