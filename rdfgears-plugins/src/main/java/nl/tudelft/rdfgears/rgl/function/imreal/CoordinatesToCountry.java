package nl.tudelft.rdfgears.rgl.function.imreal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import nl.tudelft.rdfgears.engine.Engine;
import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RecordType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.datamodel.value.ifaces.AbstractModifiableRecord;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.FieldIndexMap;
import nl.tudelft.rdfgears.util.row.FieldIndexMapFactory;
import nl.tudelft.rdfgears.util.row.TypeRow;
import nl.tudelft.rdfgears.util.row.ValueRow;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Convert a longitude/latitude pair to a country name, using the geonames service. 
 * A geonames username is also required. 
 * 
 * Output is a country literal string. 
 * 
 * This function may be modified to output a record, providing more geonames information
 * available from this service
 * 
 * @author Jasper
 * 
 * edits [Claudia]: agressive caching implemented; anything that rounds to a full degree
 * is considered to be in the same country, retrieved once (alleviates some pressure on the
 * geonames call numbers)
 *
 */
public class CoordinatesToCountry extends SimplyTypedRGLFunction  {
	public static final String INPUT_LONG = "longitude";
	public static final String INPUT_LAT = "latitude";
	public static final String INPUT_GEONAMES_USER = "geonames_user";
	
	
	public static final String FIELD_COUNTRY = "country";
	/* the records we will create contain two fields */
	private static final FieldIndexMap fiMap = FieldIndexMapFactory.create(FIELD_COUNTRY);
	
	static String geonamesServiceUrl = "http://api.geonames.org/countrySubdivision?lat=%s&lng=%s&username=%s";
	private static HashMap<String, String> countryCache = new HashMap<String, String>();
	
	public CoordinatesToCountry(){
		/* specify required inputs and their types */ 
		requireInputType(INPUT_LONG, RDFType.getInstance());
		requireInputType(INPUT_LAT, RDFType.getInstance());
		requireInputType(INPUT_GEONAMES_USER, RDFType.getInstance());
	}

	@Override
	public RGLType getOutputType() {
		TypeRow typeRow = new TypeRow();
		typeRow.put(FIELD_COUNTRY, RDFType.getInstance());
		return RecordType.getInstance(typeRow); 
	}

	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		/* input values are guaranteed to be non-null, as this is a SimplyTypedRGLFunction */
		
		RGLValue inputUser = inputRow.get(INPUT_GEONAMES_USER);
		RGLValue inputLat = inputRow.get(INPUT_LAT);
		RGLValue inputLong = inputRow.get(INPUT_LONG);
		
		if (! (inputUser.isLiteral() && inputLat.isLiteral() && inputLong.isLiteral())){
			return ValueFactory.createNull("input to "+getFullName()+" must be all literals");
		}
		
		/* ok, all literals */ 
		String username = inputUser.asLiteral().getValueString();
		String longitude = inputLong.asLiteral().getValueString();
		String latitude = inputLat.asLiteral().getValueString();
		String country = GetCountryForLocation(longitude, latitude, username);
		
		if (country==null)
			return ValueFactory.createNull("no country found");
			
		AbstractModifiableRecord rec = ValueFactory.createModifiableRecordValue(fiMap);
		rec.put(FIELD_COUNTRY, ValueFactory.createLiteralPlain(country,  null)); // return literal without language tag
		return rec;
	}
	


	public String downloadCountryForLocation(String longitude, String latitude, String geoNamesUserName) {
		String result = "";
		try {
			URL service = new URL(String.format(geonamesServiceUrl, latitude,
					longitude, geoNamesUserName));
			
			Engine.getLogger().warn("Fetching "+service);
			
			URLConnection con = service.openConnection();
			con.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			while (in.ready()) {
				result += in.readLine();
			}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String country = GetCountryFromGeoNamesXML(GetXmlDocumentFromString(result));
		return country;
		
	}
	
	
	public String GetCountryForLocation(String longitude, String latitude, String geoNamesUserName) {
		
		double d1 = Double.parseDouble(longitude);
		double d2 = Double.parseDouble(latitude);
		
		int i_lat = (int)Math.round(d1);
		int i_lng = (int)Math.round(d2);
		
		String key = i_lat+"_"+i_lng;

		String knownLocation;  
		
		if(! countryCache.containsKey( key )){
			knownLocation = downloadCountryForLocation(longitude, latitude, geoNamesUserName);
			countryCache.put(key, knownLocation);
		}
		
		return countryCache.get(key);
	}

	private static Document GetXmlDocumentFromString(String xml) {
		DocumentBuilder db;
		Document doc = null;
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return doc;
	}

	private static String GetCountryFromGeoNamesXML(Document xml) {
		NodeList list = xml.getElementsByTagName("countryName");
		String result = null;
		if (list.getLength() > 0) {
			result = list.item(0).getTextContent();
		}
		return result;
	}
	
	/*
	class Location {
		String latitude;
		String longitude;
		
		Location(String longitude, String latitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}
	
		public boolean equals(Object o) {
			boolean result = false;
			if (o instanceof Location) {
				Location other = (Location) o;
				if (other.latitude.equals(this.latitude)
						&& other.longitude.equals(this.longitude))
					result = true;
			}
			return result;
		}
		public int hashCode(){
			return toString().hashCode();
		}
		
		public String toString(){
			return (longitude+"_"+latitude);
		}
		
	}
	*/

}


