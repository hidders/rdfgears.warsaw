package com.nl.tudelft.rdfgearsUI.client.Dia;

import com.google.gwt.dom.client.Element;

public abstract class PathRenderer {
	abstract void moveCanvasTo(int x, int y);
	abstract void resizeCanvas(int w, int h);
	/**
	 * render a bezier line
	 * @param line[0] start x
	 * @param line[1] start y
	 * @param line[2] control point 1 x
	 * @param line[3] control point 1 y
	 * @param line[4] control point 2 x
	 * @param line[5] control point 2 y
	 * @param line[6] end point x
	 * @param line[7] end point y
	 */
	abstract void renderBezierLine (double [] line);
	
	abstract void draw(Element container);
	abstract void changeColor(String color);
	abstract void remove();
	abstract Element getElement();
	abstract void changeColor();
}
