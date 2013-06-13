package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.core.client.JavaScriptObject;

public class UtilJSWrapper {
	private JavaScriptObject editor;
	/**
	 * set a textarea as code mirror sparql query editor
	 * @param editorId
	 */
	public static native JavaScriptObject setAsCMSparqlEditor(String editorId) /*-{
	  return new $wnd.CodeMirror.fromTextArea($doc.getElementById(editorId), {
        mode: "application/x-sparql-query",
        tabMode: "indent",
        matchBrackets: true,
        lineNumbers: true
      });
	}-*/;
	
	/**
	 * set a textarea as code mirror sparql query editor
	 * @param editorId
	 */
	public static native JavaScriptObject setAsCMXmlViewer(String containerId) /*-{
	  return new $wnd.CodeMirror.fromTextArea($doc.getElementById(containerId), {
     	mode: {name: "xml", alignCDATA: true},
        lineNumbers: true,
        readOnly: true
      });
	}-*/;
	
	public static native JavaScriptObject setAsCMXmlEditor(String containerId) /*-{
	  return new $wnd.CodeMirror.fromTextArea($doc.getElementById(containerId), {
   		mode: {name: "xml", alignCDATA: true},
      	lineNumbers: true
    });
	}-*/;
	/**
	 * editor must be CodeMirror object
	 * @param editor
	 * @return
	 */
	public static native String getEditorValue(JavaScriptObject editor) /*-{
		return editor.getValue();
	}-*/;
	
	public static native void setEditorValue(JavaScriptObject editor, String value) /*-{
		editor.setValue(value);
	}-*/;
	
	public static native void setEditorSize(JavaScriptObject editor, int width, int height) /*-{
		editor.getWrapperElement().style.height = height + 'px';
		editor.getScrollerElement().style.height = height + 'px';
		editor.refresh();
	}-*/;
	
	public static native void setFocus(String elementId) /*-{
		$doc.getElementById(elementId).focus();
	}-*/;
}
