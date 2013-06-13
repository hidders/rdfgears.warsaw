package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

public class SVGPathRenderer extends PathRenderer {
	private Element canvas = null;
	private Element path = null;
	private static final String SVG_NAMESPACE = "http://www.w3.org/2000/svg";
	private double[] line;
	private String defaultColor = "#3583F2";
	private String lineColor = defaultColor;
	
	public SVGPathRenderer(int width, int height, int x, int y){
		canvas = createElementNS(SVG_NAMESPACE, "svg");
		moveCanvasTo(x, y);
		resizeCanvas(width, height);
		path = createElementNS(SVG_NAMESPACE, "path");
		path.setAttribute("stroke-width", ""+3);
		path.setAttribute("stroke", lineColor);
		path.setAttribute("fill", "none");
		canvas.appendChild(path);
		
	}
	
	void moveCanvasTo(int x, int y) {
		canvas.setAttribute("style", "top:" + y + "px;left:"+ x +"px;");
	}

	void resizeCanvas(int w, int h) {
		canvas.setAttribute("width", ""+w);
		canvas.setAttribute("height", ""+h);
		canvas.getStyle().setWidth(w, Style.Unit.PX);
		canvas.getStyle().setHeight(h, Style.Unit.PX);
	}

	void renderBezierLine(double[] _line) {
		line = _line;
		path.setAttribute("d", "M "+line[0]+" "+line[1]+
				" C "+line[2]+" "+line[3]+" "+
				line[4]+" "+line[5]+" "+
				line[6]+" "+line[7]);			
	}

	void draw(Element container) {
		container.appendChild(canvas);
	}

	void remove() {
		canvas.removeFromParent();
	}
	
	private static native Element createElementNS(String svgNS, String name)/*-{ 
    	return document.createElementNS(svgNS, name);
	}-*/;

	@Override
	void changeColor(String color) {
		lineColor = color;
		path.setAttribute("stroke", lineColor);
		renderBezierLine(line);
	}

	@Override
	Element getElement() {
		return canvas;
	}

	@Override
	void changeColor() {
		lineColor = defaultColor;
		path.setAttribute("stroke", lineColor);
		renderBezierLine(line);
	}
	
}
