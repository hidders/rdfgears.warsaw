package nl.tudelft.rdfgears.rgl.function.imreal;

import java.io.BufferedReader;
import nl.tudelft.rdfgears.engine.Config;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.net.URL;
import java.util.*;

import nl.tudelft.rdfgears.engine.Engine;

/**
 * A class that either retrieves Flickr images from file or from the Flickr stream
 * (ideally not file based but DB based). The filename is the Flickr NSID (not the username)!
 * 
 * maxHoursAllowedOld indicates how old in hours the stored data is allowed to be before it is overwritten.
 * 
 *  * @author Claudia
 */

public class ImageCollector 
{
	
	private static final String FLICKR_DATA_FOLDER = Config.getWritableDir()+"flickrData"; 
	private static final int MAX_NUM_IMAGES = 2000;/* maximum number of images that should be retrieved */
	private static String FLICKR_API_KEY;
	private static final String FLICKR_API_KEY_FILE = Config.getWritableDir()+"flickr_api_key";
	
	public static ArrayList<Photo> getPhotos(String flickrUsername, int maxHoursAllowedOld)
	{
		ArrayList<Photo> photos = new ArrayList<Photo>();
		
		try
		{
			readFlickrKey();
			String nsid = getNSID(flickrUsername);
			
			//if we have no NSID, just return an empty list of photos
			if(nsid.length()<3)
			{
				System.err.println("No NSID found!");
				return photos;
			}
			

			File flickrDataFolder = new File(FLICKR_DATA_FOLDER);
			if(!flickrDataFolder.exists())
				flickrDataFolder.mkdirs();
			
			File f = new File(FLICKR_DATA_FOLDER+"/"+nsid);
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
				retrieveAndStoreImages(nsid);
				Engine.getLogger().debug("In ImageCollector, retrieving new images for " + nsid);
			}

			return getPhotos(nsid);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return new ArrayList<Photo>();
	}
	
	
	private static void readFlickrKey()
	{
		/* read the Flickr API key from file */
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(FLICKR_API_KEY_FILE));
			String line;
			while( (line=br.readLine())!=null)
			{
				FLICKR_API_KEY = line;
				break;
			}
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * method checks if hte flickrUser is a NSID or a user name;
	 * in the latter case an attempt is made to convert it to an NSID
	 */
	private static String getNSID(String flickrUser)
	{
		System.err.println("converting from user name to nsid!");
		
		String nsidCall[] = {"http://api.flickr.com/services/rest/?method=flickr.people.findByUsername&api_key=", "&username=", "&format=rest"};

		String nsid = null;
		
		//are we dealing with an NSID?
		if ( flickrUser.contains("@N") == true)
			nsid = flickrUser;
		else
		{
			String tmp = flickrUser;
			//TODO: proper encoding call!
			tmp = tmp.replace(' ','+');
			String call = nsidCall[0]+FLICKR_API_KEY+nsidCall[1]+tmp+nsidCall[2];
			
			System.err.println("call: "+call);
			
			try
			{
				URL url = new URL(call);
					
				BufferedReader in = new BufferedReader(new InputStreamReader(
						url.openStream()));
		
				String inputLine;
				while( (inputLine = in.readLine())!=null)
				{
					String tofind = " nsid=\"";
					int index = inputLine.indexOf(tofind);
					if(index>0)
						nsid = inputLine.substring( index+tofind.length(), inputLine.lastIndexOf('"') );
					
					System.err.println("[output] "+inputLine);
				}
			
				/* instead of an error message, return an empty map */
				if(nsid==null)
					return "";
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.err.println("In ImageCollector, converting username "+flickrUser+" to NSID: "+nsid);
		}
		
		System.err.println("ImageCollector, flickrUser="+flickrUser+" turned into NSID="+nsid);
		return nsid;
	}
	
	
	

	/*
	 * method takes an NSID, retrieves them from Flickr and stores them in a file
	 */
	private static void retrieveAndStoreImages(String nsid)
	{
		System.err.println("ImageCollector.retrieveAndStoreImages()");
		String photoCall[]= {"http://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=", "&user_id=", "&extras=date_taken%2Cgeo%2Ctags&per_page=500&page=", "&format=rest"};

		int page=1;
		int totalNumPages=-1;//number of available pages (read from the first call)
		int maxNumPages=MAX_NUM_IMAGES/500;//500 images per page

		try
		{	
			int lineNum=0;
			BufferedWriter bw = new BufferedWriter(new FileWriter(FLICKR_DATA_FOLDER+"/"+nsid));
			bw.write("<photos>");
			bw.newLine();
			
			while(page<maxNumPages && (page<=totalNumPages || totalNumPages<0))
			{
				String call = photoCall[0]+FLICKR_API_KEY+photoCall[1]+nsid+photoCall[2]+page+photoCall[3];

				URL url = new URL(call);
				
				System.err.println("Call: "+url.toString());
	
				BufferedReader in = new BufferedReader(new InputStreamReader(
						url.openStream()));
		
				String inputLine;
				while( (inputLine = in.readLine())!=null)
				{
					lineNum++;
					
					if( inputLine.contains("<photo ") )
					{
						bw.write(inputLine);
						bw.newLine();
					}
					else if( inputLine.contains("Invalid API Key"))
					{
						System.err.println("Flickr API key may be invalid!");
						System.err.println("=> "+inputLine);
					}
					else
						;
					
					if(totalNumPages<0 && inputLine.contains("<photos page="))
						totalNumPages = Integer.parseInt( inputLine.substring(  inputLine.indexOf(" pages=\"")+" pages=\"".length(), inputLine.indexOf("\" perpage")   )  );
				}
				
				page++;
				Thread.sleep(500l);//be nice to Flickr
				
				System.err.println("Page "+page+" found for "+nsid+" (number of lines: "+lineNum+")");
			}
			bw.write("</photos>");
			bw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}



	/*
	 * reads the Flickr data from file
	 */
	private static ArrayList<Photo> getPhotos(String nsid)
	{
		ArrayList<Photo> photoList = new ArrayList<Photo>();
		
		try
		{
			Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(FLICKR_DATA_FOLDER+"/"+nsid));
			NodeList photos = d.getElementsByTagName("photo");
			
			for(int i=0; i<photos.getLength(); i++)
			{
				Node photoNode = photos.item(i);
				
				if(photoNode.hasAttributes())
				{
					Photo p = new Photo();
					NamedNodeMap attrs = photoNode.getAttributes();  
					for(int j = 0 ; j<attrs.getLength() ; j++) 
					{
				        Attr attribute = (Attr)attrs.item(j);
				        
				        //System.err.println("[name] "+attribute.getName()+" --- [value] "+attribute.getValue());
				        
				        if(attribute.getName().equals("title"))
				        	p.title = attribute.getValue();
				        else if(attribute.getName().equals("id"))
				        	p.id = attribute.getValue();
				        else if(attribute.getName().equals("datetaken"))
				        	p.parseDate(attribute.getValue());
				        else if(attribute.getName().equals("latitude"))
				        	p.latitude = Double.parseDouble(attribute.getValue());
				        else if(attribute.getName().equals("longitude"))
				        	p.longitude = Double.parseDouble(attribute.getValue());
				        else if(attribute.getName().equals("accuracy"))
				        	p.accuracy = Integer.parseInt(attribute.getValue());
				        else if(attribute.getName().equals("tags"))
				        	p.tags = attribute.getValue();
				      }
					photoList.add(p);
				}
			}

			System.err.println("Number of collected images: "+photoList.size());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		return photoList;
	}
}
