package nl.tudelft.rdfgears.rgl.function.imreal;

import java.text.*;
import java.util.*;

/*
 * class represents a single Flickr photo
 * 
 *  * @author Claudia
 */
public class Photo
{
	//2012-11-05 19:49:20
	private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	String id;
	String tags;
	String title;
	private Date date;//made private to ensure that proper parsing takes place!
	double latitude;
	double longitude;
	double estLatitude;
	double estLongitude;
	private double errorKM;
	int accuracy;
	
	Photo()
	{
		latitude = -1000.0;
		longitude = -1000.0;
		estLatitude = -1000.0;
		estLongitude = -1000.0;
		errorKM = -1.0;
		accuracy = 0;
	}
	
	public void parseDate(String input)
	{
		try
		{
			date = simpleDateFormat.parse(input);
		}
		catch(Exception e)
		{
			date = new Date(0l);
		}
	}
	
	public Date getDate()
	{
		return date;
	}
	
	public double getErrorInKM()
	{
		return ( (errorKM>=0) ? errorKM : computeError() );
	}
	
	//according to http://stackoverflow.com/questions/120283/working-with-latitude-longitude-values-in-java
	private double computeError() 
	{
		//a zero accuracy indicates that the image has no geo information!
		if(accuracy==0 || estLatitude<-500 || estLongitude<-500)
		{
			errorKM=-1.0;
		}
		else
		{
		    double earthRadius = 6378.1370;
		    double dLat = Math.toRadians(latitude-estLatitude);
		    double dLng = Math.toRadians(longitude-estLongitude);
		    double sindLat = Math.sin(dLat / 2);
		    double sindLng = Math.sin(dLng / 2);
		    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
		            * Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(estLatitude));
		    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		    errorKM = earthRadius * c;
		}
	    return errorKM;
	}
	
	public String toString()
	{
		java.lang.StringBuilder sb = new java.lang.StringBuilder();
		sb.append("id="+id).append(" tags="+tags);
		sb.append("\n").append("title="+title).append(" date="+date);
		sb.append("\n").append("latitude="+latitude).append(" longitude="+longitude);
		sb.append("\nestLatitude="+estLatitude).append(" estLongitude="+estLongitude);
		sb.append("\n").append(errorKM);
		return sb.toString();
	}
}