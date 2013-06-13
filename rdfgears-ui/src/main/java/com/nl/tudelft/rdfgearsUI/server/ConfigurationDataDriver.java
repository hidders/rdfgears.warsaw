package com.nl.tudelft.rdfgearsUI.server;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationDataDriver {
	private String basePath = ".";
	private File dataDir;
	private Map <String, String> configs = new HashMap <String, String>();
	
	public ConfigurationDataDriver(String configFilePath){
		//this.basePath = bp;
		//dataDir = new File(basePath + "/data");
		System.out.println("configFilePath basepath:" + configFilePath);
		if(!readConfigFile(configFilePath)){
			System.out.println("Cannot read config file or it contain error.. fix it dude..!!");
		}
	}
	
	public String getBasePath() {
		return basePath;
	}
	
	public File getDataDir() {
		return dataDir;
	}
	
	public boolean readConfigFile(String path){
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		boolean r = true;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document d = dBuilder.parse(new File(path));
			
			Element confRoot = (Element) d.getElementsByTagName("RDFGearsUIConfig").item(0);
			NodeList confEls = confRoot.getChildNodes();
			for(int i = 0; i < confEls.getLength(); i++){
				if(confEls.item(i).getNodeType() == 1){
					Element c = (Element) confEls.item(i);
					configs.put(c.getTagName(), c.getTextContent());
				}
				
			}
			
		
			//basePath = d.getElementsByTagName("BasePath").item(0).getTextContent();
			
			basePath = configs.get("BasePath");
			//test the content of the data dir on base path
			File workflowDir = new File(basePath + "/data/workflows");
			if(!workflowDir.exists()){
				System.out.println("Workflow directory do not exist, " + basePath + "/data/workflows");
				r = false;
			}
			
			File processorDir = new File(basePath + "/data/processors");
			if(!processorDir.exists()){
				System.out.println("Processor directory do not exist, " + basePath + "/data/processors");
				r =  false;
			}
			
			File functionsDir = new File(basePath + "/data/functions");
			if(!functionsDir.exists()){
				System.out.println("Functions directory do not exist, " + basePath + "/data/functions");
				r =  false;
			}
			
		} catch (Exception e) {
			System.out.println("Error while parsing config file, " + path);
			e.printStackTrace();
			return false;
		}
		
		return r;
	}
	
	public String getConfig(String configKey){
		return configs.get(configKey);
	}
	
}
