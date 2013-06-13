package com.nl.tudelft.rdfgearsUI.server;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProcessorsDataDriver {
	
	private ConfigurationDataDriver configurationDataDriver;
	
	public ProcessorsDataDriver(ConfigurationDataDriver configurationDataDriver) {
		this.configurationDataDriver = configurationDataDriver;
	}

	public String getProcessorFromFile(String filePath){
		File p = new File(configurationDataDriver.getBasePath() + "/data/processors/" + filePath + ".xml");
		if(p.exists()){
			try {
				p = null;
				return DataDriverUtils.readFileToString(configurationDataDriver.getBasePath() + "/data/processors/" + filePath + ".xml");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return "<error> processor not found !!: "+ configurationDataDriver.getDataDir().getAbsolutePath() + "/processors/" + filePath + ".xml" +"</error>";
	}
	
	public String getOperatorDirContent(){
		File operatorDir = new File(configurationDataDriver.getBasePath() + "/data/processors");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		Element root = null; 
		StreamResult result = new StreamResult(new StringWriter());
		ArrayList <String> categoryNames = new ArrayList <String>();
		Map <String, Element> catName2Element = new HashMap <String, Element>();
		
		try {
			if(operatorDir.isDirectory()){
				File operators[] = operatorDir.listFiles();
				
				dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.newDocument();
				root = doc.createElement("operators");
				doc.appendChild(root);
				for(File op: operators){
					if(op.isDirectory()){
						//root.appendChild(readAllOperatorXmlFiles(op, doc));
					}else{
						Document d = dBuilder.parse(op);
						d.getDocumentElement().normalize();
						Element proc = (Element) d.getElementsByTagName("processor").item(0);
						Element newNode = doc.createElement("item");
						//newNode.setAttribute("id", FilenameUtils.removeExtension(op.getName()));
						String fileName = op.getName();
						newNode.setAttribute("id", fileName.substring(0, fileName.lastIndexOf(".")));
						newNode.setAttribute("name", proc.getAttribute("label"));
						if(proc.hasAttribute("category")){
							String cat = proc.getAttribute("category");
							if(cat.trim().length() > 0){
								if(categoryNames.contains(cat)){
									catName2Element.get(cat).appendChild(newNode);
								}else{
									categoryNames.add(cat);
									Element catElement = doc.createElement("category");
									catElement.setAttribute("name", cat);
									catElement.appendChild(newNode);
									catName2Element.put(cat, catElement);
								}
							}else{
								root.appendChild(newNode);
							}
						}else{
							root.appendChild(newNode);
						}
					}
				}
				
				for(String cname: categoryNames){
					if(catName2Element.containsKey(cname)){
						root.appendChild(catName2Element.get(cname));
					}
				}
				
				Transformer t = TransformerFactory.newInstance().newTransformer();
				DOMSource source = new DOMSource(doc);
				t.transform(source, result);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		
		return result.getWriter().toString();
	}
	
}
