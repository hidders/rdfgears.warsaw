package nl.tudelft.rdfgears.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A tool to do indented writing
 * 
 * 
 * @author Eric Feliksik
 *
 */
public class BufferedIndentedWriter extends java.io.Writer {
	private BufferedWriter writer;
	private int currentIndent = 0;
	private String indentationStr = "  "; // some spaces
	
	private boolean newlineFlag = false;
	
	public BufferedIndentedWriter(OutputStream out){
		this(new OutputStreamWriter(out));
	}

	public BufferedIndentedWriter(Writer writer){
		this.writer = new BufferedWriter(writer);
	}
	
	
	public void print(String s) throws IOException{
		writer.write(s);
	}
	
	public void incIndent(){
		currentIndent++;
	}
	
	public void outdent(){
		currentIndent--;
	}
	
	private void writeIndentation() throws IOException{
		for (int i=0; i<currentIndent; i++){
			writer.write(indentationStr);
		}
	}
	
	public void resetNewlineFlag(){
		newlineFlag = false;
	}
	public boolean getNewlineFlag(){
		return newlineFlag;
	}
	
	public void newline() throws IOException{
		newlineFlag = true;
		writer.newLine();
		writeIndentation();
	}

	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		writer.write(cbuf, off, len);
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}
