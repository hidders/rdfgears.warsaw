package com.nl.tudelft.rdfgearsUI.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class DataDriverUtils {
	
	public static String readFileToString(String path) throws IOException {
		StringBuffer fileContent = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					new File(path)));
			String s = "";
			while ((s = br.readLine()) != null) {// end of the file
				fileContent.append(s).append(
						System.getProperty("line.separator"));
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileContent.toString();
	}
	
	public static String formatXml(String rawXml){
		TransformerFactory tfactory = TransformerFactory.newInstance();
        Transformer serializer;
        StreamResult xmlOutput = new StreamResult(new StringWriter());
        //System.out.println("rawXml: " + rawXml);
        try {
            serializer = tfactory.newTransformer();
            //Setup indenting to "pretty print"
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            
            Source XMLSource = new StreamSource(new StringReader(rawXml));
            serializer.transform(XMLSource, xmlOutput);
        } catch (TransformerException e) {
            // this is fatal, just dump the stack and throw a runtime exception
            e.printStackTrace();
            
            throw new RuntimeException(e);
        }
        
        return xmlOutput.getWriter().toString();
	}
}
