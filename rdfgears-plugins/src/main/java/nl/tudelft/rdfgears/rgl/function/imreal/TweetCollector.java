package nl.tudelft.rdfgears.rgl.function.imreal;

import java.io.BufferedReader;
import java.io.File;

import nl.tudelft.rdfgears.engine.Config;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import nl.tudelft.rdfgears.engine.Engine;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * A class that either retrieves tweets from file or from the Twitter stream
 * (ideally not file based but DB based). The filename is the twitter user name.
 * 
 * if includeRetweets==true, retweets are included, otherwise they are ignored
 * 
 * maxHoursAllowedOld indicates how old in hours the stored data is allowed to be before it is overwritten.
 * 
 * @author Claudia
 * 
 */
public class TweetCollector 
{
	
	private static final String TWITTER_DATA_FOLDER = Config.getWritableDir()+"twitterData";
	private static DocumentBuilder docBuilder;
	
	static
	{
		try
		{
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static HashMap<String,String> getTweetTextWithDateAsKey(String twitterUsername, boolean includeRetweets, int maxHoursAllowedOld)
	{
		try
		{
			File twitterDataFolder = new File(TWITTER_DATA_FOLDER);
			if(!twitterDataFolder.exists())
				twitterDataFolder.mkdirs();
			
			File f = new File(TWITTER_DATA_FOLDER+"/"+twitterUsername);
			int hours = -1;
			if(f.exists()==true)
			{
				long lastModified = f.lastModified();
				
				long diff = System.currentTimeMillis()-lastModified;
				
				int seconds = (int)(diff/1000L);
				int minutes = seconds/60;
				hours = minutes/60;
				
				
				//if the file is nearly empty, retrieve it again anyway
				if(f.length()<100)
				{
					System.err.println("(Nearly) empty file found, retrieving again ....");
					hours=-1;
				}
			}
			
			/*
			 * if we do not have data yet (or it is too old), retrieve it and store it in a folder
			 */
			if(hours>maxHoursAllowedOld || hours<0)
			{
				String getTweetsURL = "https://api.twitter.com/1/statuses/user_timeline.xml?include_entities=false&include_rts=true&screen_name="+ twitterUsername + "&count=200";
					
				//TODO: only overwrite the original file if we actually manage to get hold of something from Twitter ..
				BufferedWriter bw = new BufferedWriter(new FileWriter(f.toString()));
				URL url = new URL(getTweetsURL);
				Engine.getLogger().debug("In TweetCollector, retrieving live tweets for " + url.toString());
				Engine.getLogger().debug("hours computed: "+hours+", maxHoursAllowedOld: "+maxHoursAllowedOld);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						url.openStream()));

				String inputLine;
				while ((inputLine = in.readLine()) != null) 
				{
					bw.write(inputLine);
					bw.newLine();
				}
				in.close();
				bw.close();
			}
			
			return getTweetTextWithDateAsKeyFromFile(f,includeRetweets);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return new HashMap<String, String>();
	}
	
	
	private static String getTagValue(String tag, Element el) 
	{
	    NodeList list = el.getElementsByTagName(tag).item(0).getChildNodes();

	    Node val = (Node) list.item(0);
        return val.getNodeValue();
	}

	
	/*
	 * method returns up to the last 200 tweets, ignoring RTs
	 * key: created_at
	 * value: tweet text
	 */
	private static HashMap<String,String> getTweetTextWithDateAsKeyFromFile(File f, boolean includeRetweets)
	{
		HashMap<String, String> tweetMap = new HashMap<String, String>();
		
		try
		{
			Document d = docBuilder.parse(new FileInputStream(f));
			NodeList statuses = d.getElementsByTagName("status");
			
			for(int i=0; i<statuses.getLength(); i++)
			{
				Node status = statuses.item(i);
	            if (status.getNodeType() == Node.ELEMENT_NODE) 
	            {
	                Element el = (Element) status;
	
	                String creationDate = getTagValue("created_at",el);
	                String text = getTagValue("text",el);
	                
	                //is it a retweet?
	                NodeList retweetList = el.getElementsByTagName("retweeted_status");
	                if(retweetList.getLength()>0)
	                {
	                	if(includeRetweets==false)
	                		text="";
	                	else
	                	{
	                		Node retweet = retweetList.item(0);
	                		text = getTagValue("text",(Element)retweet);
	                	}
	                }

	                if(text.equals("")==false)
	                	tweetMap.put(creationDate,text);
	            }
			}
			System.err.println("Number of status tweets: "+statuses.getLength());
			System.err.println("Number of tweets serviced: "+tweetMap.size()+" (includeRetweets="+includeRetweets+")");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return tweetMap;
	}	

}
