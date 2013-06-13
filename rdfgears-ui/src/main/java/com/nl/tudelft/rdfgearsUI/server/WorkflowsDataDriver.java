package com.nl.tudelft.rdfgearsUI.server;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
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
import org.w3c.dom.NodeList;

public class WorkflowsDataDriver {
	
	private ConfigurationDataDriver configurationDataDriver;
	private String workflowsDir;
	
	public WorkflowsDataDriver(ConfigurationDataDriver configurationDataDriver) {
		this.configurationDataDriver = configurationDataDriver;
		workflowsDir = configurationDataDriver.getBasePath() + "/data/workflows/";
	}
	
	 public String getWorkflowFileAsNode(String wfId){
         String fContent = getWorkflowFile(wfId);
         if(fContent.startsWith("<error>")){
                 return "<error>Workflow's file cannot be found</error>";
         }
         
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder;
         Document wfDoc;
         Element proc, desc, inputs, func, param, output;
         StreamResult result = new StreamResult(new StringWriter());
         try {
                 dBuilder = dbFactory.newDocumentBuilder();
                 Document wfXml = dBuilder.parse(new ByteArrayInputStream(fContent.getBytes()));
                 //transform workflow to node
                 Element metadata = (Element) wfXml.getElementsByTagName("metadata").item(0);
                 String id = metadata.getElementsByTagName("id").item(0).getTextContent();
                 String name = metadata.getElementsByTagName("name").item(0).getTextContent();
                 String des = metadata.getElementsByTagName("description").item(0).getTextContent();
                 wfDoc = dBuilder.newDocument();
                 
                 des = des.replace("&lt;", "<");
                 des = des.replace("&gt;", ">");
                 des = des.replace("&amp;", "&");
                 
                 proc = wfDoc.createElement("processor");
                 proc.setAttribute("label", name);
                 desc = wfDoc.createElement("description");
                 if(des != null)
                 desc.appendChild(wfDoc.createTextNode(des));
                 
                 proc.appendChild(desc);
                 
                 inputs = wfDoc.createElement("inputs");
                 func = wfDoc.createElement("function");
                 func.setAttribute("type", "custom-java");
                 param = wfDoc.createElement("param");
                 param.setAttribute("name", "implementation");
                 param.setAttribute("value", "workflow:"+ id);
                 func.appendChild(param);
                 inputs.appendChild(func);
                 
                 //parse the input port
                 Element wfInputList = (Element) wfXml.getElementsByTagName("workflowInputList").item(0);
                 NodeList inputPorts = wfInputList.getElementsByTagName("workflowInputPort");
                 for(int i = 0; i < inputPorts.getLength(); i++){
                         Element inputP = (Element) inputPorts.item(i);
                         String inName = inputP.getAttribute("name");
                         
                         Element data = wfDoc.createElement("data");
                         data.setAttribute("iterate", "false");
                         data.setAttribute("name", inName);
                         data.setAttribute("label", inName);
                         if(inputP.getElementsByTagName("type").getLength() > 0){
                                 Element t = (Element) wfDoc.importNode(inputP.getElementsByTagName("type").item(0), true);
                                 data.appendChild(t);
                         }
                         inputs.appendChild(data);
                 }
                 proc.appendChild(inputs);
                 
                 Element network = (Element) wfXml.getElementsByTagName("network").item(0);
         
                 if(network.hasAttribute("output")){
                         output = wfDoc.createElement("output");
                         if(wfXml.getElementsByTagName("output-type").getLength() > 0){
                                 Element wOutput = (Element) wfDoc.importNode(wfXml.getElementsByTagName("output-type").item(0), true);
                                 wfDoc.renameNode(wOutput, null, "type");
                                 output.appendChild(wOutput);
                         }
                         
                         proc.appendChild(output);
                 }
                 
                 wfDoc.appendChild(proc);
                 Transformer t = TransformerFactory.newInstance().newTransformer();
                 DOMSource source = new DOMSource(wfDoc);
                 t.transform(source, result);
                 
         } catch (Exception e) {
                 e.printStackTrace();
                 return "<error>Workflow's file has an invalid format</error>";
         }
//       System.out.println(result.getWriter().toString());
         return result.getWriter().toString();
 }
	  public String getWorkflowFile(String wfId){
          wfId = wfId.trim();
          
          File functionsDir = new File(workflowsDir);
          DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
          DocumentBuilder dBuilder;
//        System.out.println("Looking for wf with id:" + wfId);
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
                                          
                                          Element meta = (Element) d.getElementsByTagName("metadata").item(0);
                                          if(meta != null){
                                                  if(meta.hasChildNodes()){
                                                          if(meta.getElementsByTagName("id").getLength() > 0){
                                                                  Element id = (Element) meta.getElementsByTagName("id").item(0);
                                                                  if(id.hasChildNodes()){
                                                                          String ids = id.getTextContent().trim();
                                                                          if(wfId.equals(ids)){
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
          return "<error>Workflow's file cannot be found</error>";
  }
	  
	  public String getWorkflowDirContent(){
          File operatorDir = new File(workflowsDir);
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
                          root = doc.createElement("workflows");
                          doc.appendChild(root);
                          for(File op: operators){
                                  if(op.isDirectory()){
                                          //root.appendChild(readAllOperatorXmlFiles(op, doc));
                                  }else{
                                          Document d = dBuilder.parse(op);
                                          d.getDocumentElement().normalize();
                                          Element wf = (Element) d.getElementsByTagName("rdfgears").item(0);
                                          
                                          Element meta = (Element) wf.getElementsByTagName("metadata").item(0);
//                                        System.out.println("id size:" + meta.getElementsByTagName("id").getLength());
//                                        Element ide = (Element) meta.getElementsByTagName("id").item(0);
//                                        System.out.println("id:" + ide.getTextContent());
                                          String id = meta.getElementsByTagName("id").item(0).getTextContent();
                                          String name = meta.getElementsByTagName("name").item(0).getTextContent();
                                          String desc = meta.getElementsByTagName("description").item(0).getTextContent();
                                          
                                          Element newWf = doc.createElement("item");
                                          //newNode.setAttribute("id", FilenameUtils.removeExtension(op.getName()));
                                          newWf.setAttribute("id", id);
                                          newWf.setAttribute("name", name);
                                          if(desc != null){
                                                  if(desc.length() > 0){
                                                          Element descEl = doc.createElement("description");
                                                          descEl.appendChild(doc.createTextNode(desc));
                                                          newWf.appendChild(descEl);
                                                  }
                                          }
                                          if(meta.getElementsByTagName("category").getLength() > 0){
                                                  String cat = meta.getElementsByTagName("category").item(0).getTextContent();
                                                  if(cat.trim().length() > 0){
                                                          if(categoryNames.contains(cat)){
                                                                  catName2Element.get(cat).appendChild(newWf);
                                                          }else{
                                                                  categoryNames.add(cat);
                                                                  Element catElement = doc.createElement("category");
                                                                  catElement.setAttribute("name", cat);
                                                                  catElement.appendChild(newWf);
                                                                  catName2Element.put(cat, catElement);
                                                          }
                                                  }else{
                                                          root.appendChild(newWf);
                                                  }
                                          }else{
                                                  root.appendChild(newWf);
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
          
          //System.out.println(result.getWriter().toString());
          return result.getWriter().toString();
  }
	  
	  public String doCopyWorkflowFile(String wfId, String newId, String newName, String newDesc, String newCat){
          if(isWorkflowIdExist(newId)){
                  return "<error> Workflow with the same Id already exist </error>";
          }else{
                  String currentWfContent = getWorkflowFile(wfId);
                  if(currentWfContent.startsWith("<error>")){
                          return "<error>Workflow cannot be found </error>";
                  }else{
                          DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                          DocumentBuilder dBuilder;
                          
                          StreamResult result = new StreamResult(new StringWriter());
                          try {
                                  dBuilder = dbFactory.newDocumentBuilder();
                                  Document wfXml = dBuilder.parse(new ByteArrayInputStream(currentWfContent.getBytes()));
                                  
                                  Element newIdEl = wfXml.createElement("id");
                                  newIdEl.appendChild(wfXml.createTextNode(newId));
                                  Element newNameEl = wfXml.createElement("name");
                                  newNameEl.appendChild(wfXml.createTextNode(newName));
                                  Element newDescEl = wfXml.createElement("description");
                                  if(!newDesc.isEmpty()) 
                                          newDescEl.appendChild(wfXml.createTextNode(newDesc));
                                  
                                  Element newCatEl = wfXml.createElement("category");
                                  if(!newCat.isEmpty())
                                          newCatEl.appendChild(wfXml.createTextNode(newCat));
                                  
                                  //transform workflow to node
                                  Element metadata = (Element) wfXml.getElementsByTagName("metadata").item(0);
                                  metadata.replaceChild(newIdEl, metadata.getElementsByTagName("id").item(0));
                                  metadata.replaceChild(newNameEl, metadata.getElementsByTagName("name").item(0));
                                  metadata.replaceChild(newDescEl, metadata.getElementsByTagName("description").item(0));
                                  metadata.replaceChild(newCatEl, metadata.getElementsByTagName("category").item(0));
                                  
                                  Transformer t = TransformerFactory.newInstance().newTransformer();
                                  DOMSource source = new DOMSource(wfXml);
                                  t.transform(source, result);
                                  saveWofkflowFile(newId, newId, result.getWriter().toString());
                          }catch (Exception e){
                                  e.printStackTrace();
                                  return "<error>Workflow cannot be found </error>";
                          }
                  }
          }
          return "<success>Workflow successfully copied</success>";
  }
	  
  public boolean isWorkflowIdExist(String wfId){
          wfId = wfId.trim();
          
          File functionsDir = new File(workflowsDir);
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
                                          
                                          Element meta = (Element) d.getElementsByTagName("metadata").item(0);
                                          if(meta != null){
                                                  if(meta.hasChildNodes()){
                                                          if(meta.getElementsByTagName("id").getLength() > 0){
                                                                  Element id = (Element) meta.getElementsByTagName("id").item(0);
                                                                  if(id.hasChildNodes()){
                                                                          String ids = id.getTextContent().trim();
//                                                                        System.out.println("WFID:" + wfId + " ID:" + ids);
                                                                          if(wfId.equals(ids)){
                                                                                  return true;
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
          
          return false;
  }
  
  public String saveWofkflowFile(String filename, String wfId, String fileContent){
          try {
                  FileWriter fw = new FileWriter(workflowsDir + filename + ".xml");
                  BufferedWriter out = new BufferedWriter(fw);
                  out.write(DataDriverUtils.formatXml(fileContent));
                  out.close();
          } catch (IOException e) {
                  e.printStackTrace();
                  return "<error>File with name \""+filename+"\" cannot be created</error>";
          }               
          return "<success>Workflow file successfully saved</success>";
  }
  public String deleteWorkflowFile(String wfId){
		File f = new File(workflowsDir + wfId + ".xml");
          try{
                  if(f.exists()){
                          f.delete();
                  }
          }catch (Exception e){
                  e.printStackTrace();
          }
          
          return "<success> file deleted </success>";
  }
}
