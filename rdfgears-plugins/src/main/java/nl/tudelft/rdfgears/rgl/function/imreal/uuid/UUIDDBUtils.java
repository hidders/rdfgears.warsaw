package nl.tudelft.rdfgears.rgl.function.imreal.uuid;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class UUIDDBUtils {
	
	//PATH to the DB login information
	private static final String PATH_TO_LOGIN_INFO = "../temp/rdfgears/config.properties";
	public static String dbURL = null;
	public static String username = null;
	public static String password = null;
	
	static
	{
		getLoginInformation();
	}
	
	public static void printLoginInformation()
	{
		System.err.println("dbURL: "+dbURL);
		System.err.println("username: "+username);
	}
	

	// JDBC driver name and database URL
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	/**
	 * Insert statement used to store (UUID, web ID, provider) triples
	 */
	private static final String INSERT_WEBID_STATEMENT = "INSERT IGNORE INTO uuid_webid VALUES (?, ?, ?)";

	/**
	 * Insert statement used to store (uuid, email) pairs
	 */
	private static final String INSERT_UUID_STATEMENT = "INSERT INTO uuid (uuid, email) VALUES (?, ?)";

	/**
	 * Select statement to find if there is already an uuid associated with the
	 * provided email address
	 */
	private static final String FIND_UUID_BY_EMAIL_STATEMENT = "SELECT uuid FROM uuid WHERE email = ?";
	
	/**
	 * Select statement to retrieve the email of already existing UUID
	 */
	private static final String FIND_EMAIL_BY_UUID_STATEMENT = "SELECT email FROM uuid WHERE uuid = ?";

	/**
	 * Select statement to find if there is already an uuid for the provided
	 * name
	 */
	private static final String FIND_UUID_BY_NAME = "SELECT id FROM uuid WHERE uuid = ?";

	/**
	 * Select statement that is used to retrieve all (web id, provider) pairs
	 * corresponding to provided uuid
	 */
	public static final String SELECT_UUID_DETAILS_STATEMENT = "SELECT webid, provider FROM uuid_webid LEFT JOIN uuid ON uuid_webid.uuid_id=uuid.id WHERE uuid.uuid = ?";

	/**
	 * Select statement that is used to retrieve the social id for uuid and provider
	 */
	public static final String SELECT_SOCIALID_FROM_UUID_STATEMENT = "SELECT webid FROM uuid_webid LEFT JOIN uuid ON uuid_webid.uuid_id=uuid.id WHERE uuid.uuid = ? AND provider = ?";

	
	/*
	 * reads information from a local file
	 */
	public static void getLoginInformation()
	{
		Properties prop = new Properties();
		 
    	try {
               //load a properties file
    		prop.load(new FileInputStream(PATH_TO_LOGIN_INFO));
 
               //get the property values
            dbURL = "jdbc:mysql://" + prop.getProperty("database");
    		username = prop.getProperty("dbuser");
    		password = prop.getProperty("dbpassword");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Stores (web ID, provider) pair for a given UUID
	 */
	public static void storeWebid(int uuid, String webid, String provider)
			throws SQLException, ClassNotFoundException {

		Connection conn = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(dbURL, username, password);

			// STEP 4: Execute a query
			PreparedStatement stmt = conn
					.prepareStatement(INSERT_WEBID_STATEMENT);

			stmt.setInt(1, uuid);
			stmt.setString(2, webid);
			stmt.setString(3, provider);
			
			System.err.println("SQL statement: "+stmt.toString());
			
			stmt.executeUpdate();

		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
	}

	/**
	 * Generates new uuid and stores the (uuid, email) pair.
	 */
	public static String storeNewUUID(String email) throws SQLException,
			ClassNotFoundException {

		Connection conn = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(dbURL, username, password);

			// STEP 4: Execute a query
			PreparedStatement stmt = conn
					.prepareStatement(INSERT_UUID_STATEMENT);

			String uuid = UUID.randomUUID().toString();
			stmt.setString(1, uuid);
			stmt.setString(2, email);
			System.err.println("SQL statement: "+stmt.toString());
			stmt.executeUpdate();

			return uuid;

		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
	}

	/**
	 * Looks for an existing uuid for the provided email address.
	 * 
	 * @return null if no uuid was found
	 */
	public static String findUUIDbyEmail(String email) throws SQLException,
			ClassNotFoundException {
		Connection conn = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(dbURL, username, password);

			// STEP 4: Execute a query
			PreparedStatement stmt = conn
					.prepareStatement(FIND_UUID_BY_EMAIL_STATEMENT);

			stmt.setString(1, email);
			System.err.println("SQL statement: "+stmt.toString());
			ResultSet resultSet = stmt.executeQuery();

			if (resultSet.next()) { // process results one row at a time
				return resultSet.getString(1);
			}

			return null;

		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
	}
	
	/**
	 * Looks for the email for the provided uuid.
	 * 
	 * @return null if no uuid was found
	 */
	public static String findEmailbyUUID(String uuid) throws SQLException,
			ClassNotFoundException {
		Connection conn = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(dbURL, username, password);

			// STEP 4: Execute a query
			PreparedStatement stmt = conn
					.prepareStatement(FIND_EMAIL_BY_UUID_STATEMENT);

			stmt.setString(1, uuid);
			System.err.println("SQL statement: "+stmt.toString());
			ResultSet resultSet = stmt.executeQuery();

			if (resultSet.next()) { // process results one row at a time
				return resultSet.getString(1);
			}

			return null;

		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
	}

	/**
	 * Looks for an existing uuid for the provided name.
	 * 
	 * @return 0 if no uuid was found
	 */
	public static int findUUIDbyName(String uuid) throws SQLException,
			ClassNotFoundException {
		Connection conn = null;
		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(dbURL, username, password);

			// STEP 4: Execute a query
			PreparedStatement stmt = conn
					.prepareStatement(FIND_UUID_BY_NAME);

			stmt.setString(1, uuid);
			System.err.println("SQL statement: "+stmt.toString());
			ResultSet resultSet = stmt.executeQuery();

			if (resultSet.next()) { // process results one row at a time
				return resultSet.getInt(1);
			}

			return 0;

		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
	}

	/**
	 * Retrieves the web IDs and providers for the provided uuid
	 */
	public static List<SimpleEntry<String, String>> retrieve(String uuid) throws SQLException,
			ClassNotFoundException {

		Connection conn = null;
		List<SimpleEntry<String, String>> result = new ArrayList<AbstractMap.SimpleEntry<String, String>>();

		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(dbURL, username, password);

			// STEP 4: Execute a query
			PreparedStatement stmt = conn
					.prepareStatement(SELECT_UUID_DETAILS_STATEMENT);

			stmt.setString(1, uuid);
			System.err.println("SQL statement: "+stmt.toString());
			ResultSet resultSet = stmt.executeQuery();

			while (resultSet.next()) { // process results one row at a time
				String webid = resultSet.getString(1);
				String provider = resultSet.getString(2);

				result.add(new SimpleEntry<String, String>(webid, provider));
			}

		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try

		return result;
	}

	/**
	 * Retrieves the web IDs and providers for the provided uuid
	 */
	public static String retrieve(String uuid, String provider) throws SQLException,
			ClassNotFoundException {

		Connection conn = null;

		try {
			// STEP 2: Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// STEP 3: Open a connection
			conn = DriverManager.getConnection(dbURL, username, password);

			// STEP 4: Execute a query
			PreparedStatement stmt = conn
					.prepareStatement(SELECT_SOCIALID_FROM_UUID_STATEMENT);

			stmt.setString(1, uuid);
			stmt.setString(2, provider);
			
			System.err.println("SQL statement: "+stmt.toString());

			ResultSet resultSet = stmt.executeQuery();

			if (resultSet.next()) { // process results one row at a time
				return resultSet.getString(1);
			}

		} 
		finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}// end finally try
		}// end try
		return "";
	}
}
