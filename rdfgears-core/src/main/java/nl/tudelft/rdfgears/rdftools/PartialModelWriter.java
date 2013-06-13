package nl.tudelft.rdfgears.rdftools;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.Model;

public class PartialModelWriter extends FilterOutputStream {

	public static void writePartialModel(Model m, int maxLines, String linePrefix){
		
		StringWriter sw = new StringWriter();
		m.write(sw, "N3");
		sw.getBuffer();
		StringBuffer buf = sw.getBuffer();
		
		int startPos = 0;
		boolean prefixDone = false;
		int lineCount = 0;
		
		while (lineCount < maxLines){
			int toPos = buf.indexOf("\n", startPos);
			if (toPos == -1){
				break;
			}
			String s = buf.substring(startPos, toPos);
			
			if (prefixDone || ! s.startsWith("@prefix")){
				if (prefixDone){			// do not print the first newline after prefix declaration
					System.out.print(linePrefix);
					System.out.println(s);
				}
				prefixDone = true;
				
				if (! s.equals("")){
					lineCount++;
				}
			}
			
			startPos = toPos+1;
		}
		
		return;
		
	}
	
	/**** 
	 * everything below is crap. 
	 */
	
	int newLines = 0;
	int maxLines;
	boolean prefixesAreProcessed = true; // whether we have processed all the prefixes already
	boolean silent = false; // go silent if we see prefix declarations.
	boolean lineEnd = true; // we just printed a newline character
	
	StringBuilder stringBuilder = new StringBuilder();
	
	public PartialModelWriter(OutputStream out, int maxLines) {
		super(out);
		this.maxLines = maxLines;
	}
//
//	public static void writePartialModel(OutputStream out, Model m, int lines){
//		
//		
// 		this is a mess!!! 
//		
//		//OutputStream os = new InterruptingOutputStream(System.out, lines);
//		PartialModelWriter modelStream = new PartialModelWriter(out, lines); 
//		try {
//			m.write(modelStream, "N3", "http://www.st.ewi.tudelft.nl/RDFGears/0/");
//		} catch (RuntimeException e) {
//			// we are done printing, continue silently 
//		}
//	}
	
	public void write(byte[] b) {
		System.out.println("write(byte[] b) called, b="+b);
	}
	
	private void writeLine(byte[] b, int off, int len) {
		//stringBuilder.append(b, off, len)
		if ( ! prefixesAreProcessed){
			
		}
		try {
			System.out.print("### ");
			System.out.flush();
			out.write(b, off, len);
		} catch (IOException e) {
			e.printStackTrace(); // should never happen: constructor allows only PrintStream, which doesn't throw
		}
	}
	
	public void write(byte[] b, int off, int len) {
		int startPos = off; // position in array we start printing
		int stopPos = len + off; // first position we should not print 
		
		/* process line by line */
		while (startPos < stopPos){
			int endPos = startPos;
			while (endPos < stopPos){
				if (b[endPos]=='\n'){
					endPos++;
					lineEnd = true;
					break;
				}
				endPos++;
			}
			writeLine(b, startPos, endPos-startPos);
			startPos = endPos;
		}
		if (true) return ;
		
		
		///////////////////
//
//		if (newLines>maxLines){
//			throw new RuntimeException("Written "+maxLines+" lines");
//		}

		
		if (lineEnd){
			String s = new String(b, off, "@prefix".length());
			if (s.equals("@prefix")){
				System.out.println("WHOOO PREFIX COMING");
				silent = true;
			}
		}
		else {
			System.out.println(b.toString()+ "<<< no PREFIX");
		}
		
		
		
		try {
			out.write(b, off, len);
			if (newLines>maxLines){
				throw new RuntimeException("Written "+maxLines+" lines");
			}

		} catch (IOException e) {
			e.printStackTrace(); // should never happen: constructor allows only PrintStream, which doesn't throw
		}
	}
	public void write(int b) {
		System.out.println("write("+b+") called");
	}
	
}