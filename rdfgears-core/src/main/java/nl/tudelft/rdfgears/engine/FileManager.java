package nl.tudelft.rdfgears.engine;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * A manager providing BufferedWriter objects for files that have been opened. 
 * @author Eric Feliksik
 *
 */
public class FileManager {
	private Map<String, BufferedWriter> writerMap = new HashMap<String, BufferedWriter>(); 

	protected FileManager(){
		
	}
	
	/**
	 * Returns a buffered writer to the file with given name. 
	 * 
	 * Filename is relative to the path_to_work_files directory specified in the config.  
	 * 
	 * If the file cannot be opened, a RuntimeException will be thrown.
	 * The writer should *NOT* be closed the the user of this function. 
	 * At the end of the workflow execution Engine.close() will be called
	 * to close it.   
	 * 
	 * @param filename
	 * @return
	 */
	public Writer getFileWriter(String fileName){
		String fullFileName = Engine.getConfig().getPathToWorkFiles() + "/" + fileName;
		BufferedWriter bufferedWriter = writerMap.get(fullFileName);
		
		if (bufferedWriter==null){
			FileOutputStream fout;
			try
			{
			    // Open an output stream
			    fout = new FileOutputStream(fullFileName);
			    OutputStreamWriter outWriter = new OutputStreamWriter(fout, "UTF-8");
			    bufferedWriter = new BufferedWriter(outWriter);
			    writerMap.put(fullFileName, bufferedWriter);
			}
			catch (IOException e)
			{
				Engine.getLogger().warn("Unable to write to file '"+fullFileName+"'");
				Engine.getLogger().warn("Does the directory exist? ");
				throw new RuntimeException(e);
			}
			
			Engine.getLogger().info("Opened file "+fullFileName+" for writing");
		}
		
		return bufferedWriter;
	}
	
	/**
	 * close all openened writers. This flushes the files and closes them.  
	 */
	public void close(){
		for (Writer writer : writerMap.values()){
			try {
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
