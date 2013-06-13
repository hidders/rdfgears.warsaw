package nl.tudelft.rdfgears.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Level;

/**
 * This configuration system is a bit chaotic and may be adapted to use a better mechanism.  
 * 
 * This idea is to not make the properties map available, as we like the properties to be referenced statically 
 * (i.e. not by means of a string key), to make them more easy to find throughout the code.  
 * 
 * @author Eric Feliksik
 *
 */
public class Config {
	public static final String DEFAULT_DB_PATH = "./bdb";
	public static final String DEFAULT_WORKFLOW_PATHLIST = ".:./workflows:./gearflows";	
	public static final String DEFAULT_CONFIG_FILE = "./rdfgears.config";
	private static final Level  DEFAULT_LOG_LEVEL = Level.INFO;
	private static final int DEFAULT_SPARQL_RETRY_MAX = 3;
	private static final long DEFAULT_SPARQL_RETRY_PAUSE = 1000; // milliseconds
	
	private static final String STRING_TRUE  = "true"; 
	private static final String STRING_FALSE = "false";
	public static final String DEFAULT_RGL_SERIALIZATION_FORMAT = "xml"; 
	
	private List<String> workflowPathList;
	
	private Properties configMap = new Properties();
	
	public List<String> getWorkflowPathList(){
		return workflowPathList;
	}
	
	public int getRemoteSparqlConstructBatchSize(){
		try {
			return Integer.parseInt(configMap.getProperty("remote_sparql_construct_batchsize", "throw parse error"));
		} catch (NumberFormatException e){
			return 2000; // do not batch
		} 
	}
	

	public int getRemoteSparqlSelectBatchSize() {
		return 2000;
//		try {
//			System.out.println(Integer.parseInt(configMap.getProperty("remote_sparql_select_batchsize", "throw parse error")));
//			return Integer.parseInt(configMap.getProperty("remote_sparql_select_batchsize", "throw parse error"));
//		} catch (NumberFormatException e){
//			System.out.println(e);
//			return 2000; // do not batch
//		} 
	}
	
	public String getPathToWorkFiles(){
		return configMap.getProperty("path_to_work_files", "./files_generated/");
	}
	
	
	/** 
	 * reinitialize the configuration 
	 */
	public Config(String fileName){
		
		if (fileName!=null){
			try {
				configMap.load(this.getClass().getClassLoader().getResourceAsStream(fileName));
				
				//configMap.load(new FileInputStream(fileName) ); // may not work in .jar files, etc
			} catch (Exception e) {
				Engine.getLogger().error("Cannot open configuration file '"+fileName+"' for reading");
			}
		}
		
		configurePath(configMap.getProperty("workflow_path_list", DEFAULT_WORKFLOW_PATHLIST));
	}
	
	/**
	 * Disable lazy loading, load EVERYTHING when executing remote SPARQL query, to find SPARQL endpoint errors ASAP
	 */
	public boolean do_greedyLoadingOfRemoteQueries(){
		return isTrue(configMap.getProperty("greedy_loading_of_remote_queries", STRING_TRUE));
	}
	
	public void configurePath(String workflowPathListString) {
		String[] pathArray = workflowPathListString.split(":");
		workflowPathList = new ArrayList<String>(pathArray.length);
		for (String path : pathArray){
			workflowPathList.add(path);
		}
	}

	public Level getDebugLevel() {
		String logLevelStr = configMap.getProperty("log_level");
		return Level.toLevel(logLevelStr, DEFAULT_LOG_LEVEL);
	}


	public int getSparqlRetryMax() {
		String intStr = configMap.getProperty("sparql_retry_max");
		if (intStr == null)
			return DEFAULT_SPARQL_RETRY_MAX;
			
		try {
			return Integer.parseInt(intStr);
		} catch (NumberFormatException e){
			return DEFAULT_SPARQL_RETRY_MAX;
		}
	}
	
	public long getSparqlRetryPause() {
		String longStr = configMap.getProperty("sparql_retry_pause");
		if (longStr == null)
			return DEFAULT_SPARQL_RETRY_PAUSE;
			
		try {
			return Long.parseLong(longStr);
		} catch (NumberFormatException e){
			return DEFAULT_SPARQL_RETRY_PAUSE;
		}
	}

	public void setDebugLevel(String level) {
		configMap.put("log_level", level);
	}
	
	public void setDiskBased() {
		configMap.put("is_disk_based", Boolean.toString(true));
	}

	/**
	 * Return true iff the given string should be considered 'true'
	 * @param str
	 * @return
	 */
	private static boolean isTrue(String str){
		return str.toLowerCase().equals(STRING_TRUE);
	}

	public boolean isDiskBased() {
		return Boolean.parseBoolean(configMap.getProperty("is_disk_based"));
	}

	public static String getWritableDir() {
		String path = System.getProperty("java.io.tmpdir")+"/rdfgears/";
		File dir = new File(path);
		dir.mkdirs();
		return path;
	}

}
