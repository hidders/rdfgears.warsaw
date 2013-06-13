package nl.tudelft.rdfgears.rgl.function.imreal;

import java.util.ArrayList;

import nl.tudelft.rdfgears.engine.Config;
import java.util.HashMap;
import java.util.List;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.BagType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.ModifiableRecord;
import nl.tudelft.rdfgears.rgl.datamodel.value.impl.bags.ListBackedBagValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;

import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.FieldIndexMapFactory;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import java.util.*; 
import java.io.*;
import java.text.*;


/**
 * Given a flickr username, estimate the geo-long/lat. 
 * 
 * 
 * @author Claudia
 *
 */
enum MONTH {
	JANUARY 	("Jan",1),
	FEBURARY	("Feb",2),
	MARCH		("Mar",3),
	APRIL 		("Apr",4),
	MAY			("May",5),
	JUNE		("Jun",6),
	JULY 		("Jul",7),
	AUGUST		("Aug",8),
	SEPTEMBER	("Sep",9),
	OCTOBER 	("Oct",10),
	NOVEMBER	("Nov",11),
	DECEMBER	("Dec",12);
    
	private final int monthInt;
	private final String name;

    MONTH(String name, int monthInt) {
        this.monthInt = monthInt;
        this.name = name;
    }
    
    public static int getMonthNumber(String name)
    {
    	for(MONTH m : MONTH.values())
    		if(m.name.equals(name))
    			return m.monthInt;
    	return -1;
    }
}


/*
 * Class represents a single world region (term distribution of pictures taken in that region).
 * Each region is represented by its latitude/longitude center
 */
class Region
{
	double latCenter;
	double lngCenter;
	
	double regionLength;
	
	HashMap<String,Integer> terms;
	
	Region(double latCenter, double lngCenter)
	{
		this.latCenter = latCenter;
		this.lngCenter = lngCenter;
		
		terms = new HashMap<String, Integer>();
	}
}


public class FlickrPhotoLocator  extends SimplyTypedRGLFunction  {
	
	/* named inputs */ 
	public static final String INPUT_FLICKR_USERNAME = "flickrUser";
	public static final String INPUT_TWITTER_USERNAME = "twitterUser";
	public static final String INPUT_UUID = "uuid";
	
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");//Twitter date parser
	
	/* fieldnames in output records */ 
	public static final String FIELD_LONGITUDE = "longitude";
	public static final String FIELD_LATITUDE = "latitude";
	public static final String FIELD_ESTIM_LONGITUDE = "estimatedLong";
	public static final String FIELD_ESTIM_LATITUDE = "estimatedLat";
	public static final String FIELD_ESTIM_ERROR = "estimatedErrorKm";
	public static final String FIELD_DATE_TAKEN= "dateTaken";
	public static final String FIELD_FLICKR_ACCURACY = "flickrAccuracy";

	private static final String REGION_FILE = Config.getWritableDir()+"region.out";
	private static final ArrayList<Region> worldRegions = new ArrayList<Region>();
	private static boolean regionsRead = false;
	
	private static final double MU = 2000.0; /* language model smoothing parameter */
	private static final HashMap<String, Double> corpusTF = new HashMap<String, Double>();//collection language model is stored here
	private static double corpus_tf = 0.0; //total number of tokens in the corpus
	private static final double epsilon = 0.0000000001;
	private static final double MAX_DAYS_DIFFERENCE = 5;/*the maximum number of days the photo date and tweet date may be apart for the tweet to still count towards the photo */
	

	
	FieldIndexMap fiMap = FieldIndexMapFactory.create(	FIELD_LONGITUDE, 
														FIELD_LATITUDE, 
														FIELD_ESTIM_LONGITUDE, 
														FIELD_ESTIM_LATITUDE, 
														FIELD_ESTIM_ERROR, 
														FIELD_DATE_TAKEN,
														FIELD_FLICKR_ACCURACY);
	public FlickrPhotoLocator()
	{
		/* required input type is always RDFType */ 
		requireInputType(INPUT_FLICKR_USERNAME, RDFType.getInstance());
		requireInputType(INPUT_TWITTER_USERNAME, RDFType.getInstance());
		requireInputType(INPUT_UUID, RDFType.getInstance());
	}
	
	@Override
	public RGLType getOutputType() {
		/* this function returns a type Bag(Record(< .... >)) */
		TypeRow typeRow = new TypeRow();
		typeRow.put(FIELD_LONGITUDE, RDFType.getInstance());
		typeRow.put(FIELD_LATITUDE, RDFType.getInstance());
		typeRow.put(FIELD_ESTIM_LONGITUDE, RDFType.getInstance());
		typeRow.put(FIELD_ESTIM_LATITUDE, RDFType.getInstance());
		typeRow.put(FIELD_ESTIM_ERROR, RDFType.getInstance());
		typeRow.put(FIELD_DATE_TAKEN, RDFType.getInstance());
		typeRow.put(FIELD_FLICKR_ACCURACY, RDFType.getInstance());
		return BagType.getInstance(RecordType.getInstance(typeRow));
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		/* input values are guaranteed to be non-null, as this is a SimplyTypedRGLFunction */
		RGLValue inputTwitterUser = inputRow.get(INPUT_TWITTER_USERNAME);
		RGLValue inputFlickrUser = inputRow.get(INPUT_FLICKR_USERNAME);
		RGLValue uuid = inputRow.get(INPUT_UUID);
				
		if (!inputTwitterUser.isLiteral() || !inputTwitterUser.isLiteral() || !uuid.isLiteral() ){
			return ValueFactory.createNull("input to "+getFullName()+" must be all literals");
		}
		
		/* ok, all literals */
		String twitterUser = inputTwitterUser.asLiteral().getValueString();
		String flickrUser = inputFlickrUser.asLiteral().getValueString();
		String uuidUser = uuid.asLiteral().getValueString();

		HashMap<String, String> tweets = (twitterUser.equals("")==true) ? new HashMap<String, String>() : TweetCollector.getTweetTextWithDateAsKey(twitterUser, false, 24*14);//2 week old data is okay
		ArrayList<Photo> photos = (flickrUser.equals("")==true) ? new ArrayList<Photo>() : ImageCollector.getPhotos(flickrUser, 24*12);
		
		try 
		{ 
			return getPhotosWithEstimatedLocations(tweets, photos, twitterUser, flickrUser, uuidUser);
		} 
		catch (Exception e)
		{
			return ValueFactory.createNull("Exception in "+getFullName()+": "+e.getMessage());
		}
	}
	

	/*
	 * create term distributions for world regions (read from file)
	 * file format, one region per line:
	 * [latitudeCenter] [longitudeCenter] [term1] [tf_in_region1] [tf_in_corpus1] [term2] [tf_in_region2] [tf_in_corpus2] .... [totalTermCountRegion]
	 */
	private static void readRegionFile()
	{
		if(regionsRead == true)
			return;
		
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(REGION_FILE));
			String line;
			int lineNum=0;
			while((line=br.readLine())!=null)
			{
				lineNum++;
				
				//read number of tokens in the corpus
				if(lineNum==1)
				{
					corpus_tf = Double.parseDouble(line);
					continue;
				}	
				
				//read the collection language model (term, tfc) pairs
				if(lineNum==2)
				{
					String tokens[] = line.split("\\s+");
					for(int i=0; i<tokens.length; i+=2)
					{
						String token = tokens[i];
						double tfc = Double.parseDouble(tokens[i+1]);
						corpusTF.put(token,tfc);
					}
					continue;
				}
				
				//each additional line is one world region
				String tokens[] = line.split("\\s+");
				if(tokens.length<10)
					continue;
				
				double latCenter = Double.parseDouble(tokens[0]);
				double lngCenter = Double.parseDouble(tokens[1]);
				Region r = new Region(latCenter,lngCenter);
				worldRegions.add(r);
				
				double regionLength = 0.0;
				for(int i=3; i<tokens.length; i+=2)
					regionLength += Double.parseDouble(tokens[i]);
				r.regionLength = regionLength;
				
				for(int i=2; i<tokens.length; i+=2)
				{
					String term = tokens[i];
					int tf = Integer.parseInt(tokens[i+1]);
					
					r.terms.put(term,tf);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.err.println("Region models read: "+worldRegions.size());
		regionsRead = true;
	}
	
	 
	private RGLValue getPhotosWithEstimatedLocations(
			HashMap<String, String> tweets, ArrayList<Photo> photos,
			String twitterUser, String flickrUser, String uuid)
			throws IOException {
		List<RGLValue> bagList = ValueFactory.createBagBackingList();
		readRegionFile();

		for (Photo photo : photos) {
			StringBuilder sb = new StringBuilder();
			Region bestMatch = null;
			double bestMatchProb = 0.0;

			String[] titleTokens = photo.title.toLowerCase().split("\\s+");
			String[] tagTokens = photo.tags.toLowerCase().split("\\s+");
			String[] tweetTokens = {};
			Date photoDate = photo.getDate();
			long milliseconds1 = photoDate.getTime();

			for (String tweetDate : tweets.keySet()) {
				Date d = null;
				long milliseconds2;
				try {
					d = simpleDateFormat.parse(tweetDate);
					milliseconds2 = d.getTime();
				} catch (Exception e) {
					milliseconds2 = 0l;
				}

				double diff = Math.abs(milliseconds1 - milliseconds2);
				diff = diff / 1000.0;// seconds
				diff = diff / 60.0;// minutes diff = diff/60.0;//hours
				diff = diff / 24.0;// days
				if (diff <= (double) MAX_DAYS_DIFFERENCE)
					sb.append(" ").append(tweets.get(tweetDate));
			}

			if (sb.length() > 0)
				tweetTokens = sb.toString().split("\\s+");

			for (Region r : worldRegions) {
				// compute 'query' likelihood
				double prob = 0.0;

				// for title and tag tokens!
				for (int t = 0; t < 3; t++) {
					for (String token : ((t == 0) ? titleTokens
							: ((t == 1) ? tagTokens : tweetTokens))) {
						double tf = 0.0;
						if (r.terms.containsKey(token))
							tf = (double) r.terms.get(token);

						double tfc = 0.0;
						if (corpusTF.containsKey(token))
							tfc = corpusTF.get(token);

						double logProb = Math.log(epsilon);
						if (tfc > 0)
							logProb = Math.log((tf + MU * (tfc / corpus_tf))
									/ (r.regionLength + MU));
						else
							continue;

						prob += logProb;
					}
				}

				if (bestMatch == null || prob > bestMatchProb) {
					bestMatch = r;
					bestMatchProb = prob;
				}
			}

			if (bestMatch != null) {
				photo.estLatitude = bestMatch.latCenter;
				photo.estLongitude = bestMatch.lngCenter;
			}

			AbstractModifiableRecord locRecord = ValueFactory
					.createModifiableRecordValue(fiMap);
			locRecord.put(FIELD_LONGITUDE,
					ValueFactory.createLiteralDouble(photo.longitude));
			locRecord.put(FIELD_LATITUDE,
					ValueFactory.createLiteralDouble(photo.latitude));
			locRecord.put(FIELD_ESTIM_LATITUDE,
					ValueFactory.createLiteralDouble(photo.estLatitude));
			locRecord.put(FIELD_ESTIM_LONGITUDE,
					ValueFactory.createLiteralDouble(photo.estLongitude));
			locRecord.put(FIELD_ESTIM_ERROR,
					ValueFactory.createLiteralDouble(photo.getErrorInKM()));
			locRecord.put(FIELD_DATE_TAKEN, ValueFactory.createLiteralPlain(
					photo.getDate().toString(), null));
			locRecord.put(FIELD_FLICKR_ACCURACY,
					ValueFactory.createLiteralPlain("" + photo.accuracy, null));

			bagList.add(locRecord);
		}
		return new ListBackedBagValue(bagList);
	}
		
}
