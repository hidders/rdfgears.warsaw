package com.nl.tudelft.rdfgearsUI.server;

import java.io.File;
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
import org.w3c.dom.NodeList;

public class FunctionsDataDriver {
	
	private ConfigurationDataDriver configurationDataDriver;
	
	public FunctionsDataDriver(ConfigurationDataDriver configurationDataDriver) {
		this.configurationDataDriver = configurationDataDriver;
	}
	
	public String getFunctionsDirContent(){
		File functionsDir = new File(configurationDataDriver.getBasePath() + "/data/functions");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		Document doc = null;
		Element root = null; 
		StreamResult result = new StreamResult(new StringWriter());
		ArrayList <String> categoryNames = new ArrayList <String>();
		Map <String, Element> catName2Element = new HashMap <String, Element>();
		
		try {
			if(functionsDir.isDirectory()){
				File operators[] = functionsDir.listFiles();
				
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
						String fId = "";
						NodeList params = proc.getElementsByTagName("param");
						for(int i = 0; i < params.getLength(); i++){
							Element prm = (Element) params.item(i);
							if(prm.hasAttribute("name")){
								if(prm.getAttribute("name").equals("implementation")){
									if(prm.hasAttribute("value")){
										fId = prm.getAttribute("value").trim();
									}
								}
							}
						}
						newNode.setAttribute("id", "function:"+fId);
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
	
	public String getFunctionFile(String fId){
		fId = fId.trim();
		
		File functionsDir = new File(configurationDataDriver.getBasePath() + "/data/functions");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try {
			if(functionsDir.isDirectory()){
				File operators[] = functionsDir.listFiles();
				dBuilder = dbFactory.newDocumentBuilder();
				for(File op: operators){
					if(op.isDirectory()){
						//root.appendChild(readAllOperatorXmlFiles(op, doc));
					}else{
						Document d = dBuilder.parse(op);
						d.getDocumentElement().normalize();
						
						NodeList params = d.getElementsByTagName("param");
						for(int i = 0; i < params.getLength(); i++){
							Element prm = (Element) params.item(i);
							if(prm.hasAttribute("name")){
								if(prm.getAttribute("name").equals("implementation")){
									if(prm.hasAttribute("value")){
										String fId1 = prm.getAttribute("value").trim();
										if(fId1.equals(fId)){
											return DataDriverUtils.readFileToString(op.getAbsolutePath());
										}
									}
								}
							}
						}
						
					}
				}
				
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		return "<error> Function's definition file cannot be found !!</error>";
	}
}
