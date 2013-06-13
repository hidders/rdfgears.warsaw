package com.nl.tudelft.rdfgearsUI.client.Dia;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.GQuery.Offset;

public class Path{
	private Point start;
	private Point end;
//	private int STROKE_WIDTH = 2;
//	private String STROKE_COLOR = "#fff";
	private String startPortId;
	private String endPortId;
	private NodePort startPort;
	private NodePort endPort;
	private String id;
	private PathRenderer renderer = null;
	private int CANVAS_MARGIN = 40;
	private int canvasWidth = 2 * CANVAS_MARGIN;
	private int canvasHeight = 2 * CANVAS_MARGIN;
	private Point pos = null;
	private Node startNode = null;
	private Node endNode = null;
	private boolean isVisible;
	private boolean isError = false;
	//	protected Path() {}
	public Path(){}
	public Path(int sX, int sY, int eX, int eY){
		start = new Point(sX, sY);
		end = new Point(eX, eY);
		if(Canvas.isSupported()){
			renderer = new CanvasPathRenderer(canvasWidth, canvasHeight, sX - CANVAS_MARGIN , sY - CANVAS_MARGIN);
		}else {
			renderer = new SVGPathRenderer(canvasWidth, canvasHeight, sX - CANVAS_MARGIN , sY - CANVAS_MARGIN);
		}
		pos = new Point(sX - CANVAS_MARGIN, sY - CANVAS_MARGIN);
	}
	
	public void setId(String s){
		id = s;
	}
	
	public String getId(){
		return id;
	}
	
	public void setStart(int x, int y){
		start.setXY(x, y);
	}
	
	public void setEnd(int x, int y){
		end.setXY(x, y);
	}
	/**
	 * update canvas size relative to ref
	 * @param ref static point as reference to the resizing
	 * @param x
	 */
	private int getNewCanvasWidth(Point ref, int x){
		if(x >= ref.getX()){
			//resize to right
			return (2 * CANVAS_MARGIN) + x - ref.getX();
		}else{
			// resize to left
			return (2 * CANVAS_MARGIN) + ref.getX() - x;
		}
	}
	private int getNewCanvasHeight(Point ref, int y){

		if(y >= ref.getY()){
			//resize to bottom
			return (2 * CANVAS_MARGIN) + y - ref.getY();
		}else{
			//resize to top
			return (2 * CANVAS_MARGIN) + ref.getY() - y;
		}
	}
	private int getNewXPos(Point ref, int x){
		if(ref.getX() >= x)
			return x - CANVAS_MARGIN;
		
		return ref.getX() - CANVAS_MARGIN;
	}
	private int getNewYPos(Point ref, int y){
		if(ref.getY() >= y)
			return y - CANVAS_MARGIN;
		
		return ref.getY() - CANVAS_MARGIN;
	}
	/**
	 * Update position by mouse's cursor position
	 * @param x
	 * @param y
	 */
	public void updateStartByClientPos(int x, int y){
		start.setXY(x, y);
		redraw(end, x, y);
	}
	/**
	 * Update position by mouse's cursor position
	 * @param x
	 * @param y
	 */
	public void updateEndByClientPos(int x, int y){
		end.setXY(x, y);
		redraw(start, x, y);
	}
	
	public void updateStartByPortPosition(){
		Offset p = startPort.getCenterCoordinate();
		start.setXY(p.left, p.top);
		redraw(end,p.left, p.top);
	}
	
	public void updateEndByPortPosition(){
		Offset p = endPort.getCenterCoordinate();
		end.setXY(p.left, p.top);
		redraw(start,p.left, p.top);
	}
	/**
	 * change the line color to red
	 */
	public void setAsErrorPath(boolean v){
		if(v){
//			Log.debug("set as error path");
			isError = v;
			renderer.changeColor("#FF2900");
		}else if(isError){
			renderer.changeColor();
			isError = false;
		}
	}
	private void redraw(Point ref, int x, int y){
		//update canvas pos		
		pos.setXY(getNewXPos(ref, x),  getNewYPos(ref, y));
		renderer.moveCanvasTo(pos.getX(), pos.getY());
				
		//update canvas width and height
		renderer.resizeCanvas(getNewCanvasWidth(ref, x), getNewCanvasHeight(ref, y));
		
		renderer.renderBezierLine(getBezierPath());
	}
	
	public void setStartNode(Node n){
		startNode = n;
	}
	public void setEndNode(Node n){
		endNode = n;
	}
	public String getSourceId(){
		return startNode.getId();
	}
	public Node getStartNode(){
		return startNode;
	}
	public Node getEndNode(){
		return endNode;
	}
	public void setStartPortId(String id){
		startPortId = id;
	}
	
	public void setEndPortId(String id){
		endPortId = id;
	}
	public void setStartPort(NodePort p){
		startPort = p;
	}
	
	public void setEndPort(NodePort p){
		endPort = p;
	}
	
	public String getStartPortId(){
		return startPortId;
	}
	
	public String getEndPortId(){
		return endPortId;
	}
	
	public NodePort getStartPort(){
		return startPort;
	}
	
	public NodePort getEndPort(){
		return endPort;
	}
	public void setVisible(boolean v){
		try{
			isVisible = v;
			if(v){
				renderer.getElement().removeClassName("hidden");
			}else{
				renderer.getElement().addClassName("hidden");
			}
		}catch (Exception e){e.printStackTrace();};
	}
	
//	public void setWidth(int sw){
//		STROKE_WIDTH = sw;
//	}
//	
//	public void setColor(String color){
//		STROKE_COLOR = color;
//	}
	/**
	 * get the SVG Patch Command, modify this function to change the path type, (straight, bazier)
	 * 
	 * @return Path command, refer to W3 SVG Patch Specification (http://www.w3.org/TR/SVG/paths.html)
	 */
	private String getPath(){
		return "M" + start.getX() + " " + start.getY() + " L" + end.getX() + " " + end.getY();
	}

	private double[] getBezierPath(){
		//transpose global coordinate (start, end) to local canvas coordinate
		int sX = start.getX() - pos.getX();
		int sY = start.getY() - pos.getY();
		int eX = end.getX() - pos.getX();
		int eY = end.getY() - pos.getY();
		
		double dx = Math.max(Math.abs(sX - eX) / 3, 10);
		
		double x2 = sX + dx;
		double y2 = sY;
		double x3 = eX - dx;
		double y3 = eY;
	
		return new double[]{
				toFixed(sX, 3), //move to
				toFixed(sY, 3), //move to
		        toFixed(x2, 3), //curve to
		        toFixed(y2, 3), //curve to
				toFixed(x3, 3), //curve to
				toFixed(y3, 3),//curve to
				toFixed(eX, 3), //curve to
				toFixed(eY, 3)//curve to
		};
	}
	
	private static final native double toFixed(double val, int places) /*-{
		return parseFloat(val.toFixed(places));
	}-*/;
	
	public void removeAsOutputPath(){
		//remove from end node
		if(endNode != null){
			endNode.removePathReference(getInstance());
		}
		renderer.remove();
		renderer = null;
	}
	
	public void removeAsInputPath(){
		//remove from start node
		if(startNode != null){
			startNode.removePathReference(getInstance());
		}
		renderer.remove();
		renderer = null;
	}
	/**
	 * WARNING: not save, can raise ConcurrentModificationException
	 */
	public void remove(){
		//remove from end node
		if(endNode != null){
			endNode.removePathReference(getInstance());
		}
		if(startNode != null){
			startNode.removePathReference(getInstance());
		}
		renderer.remove();
		renderer = null;
		
	}
	
	public Path getInstance(){
		return this;
	}
	
	public void draw (Element container){
		renderer.draw(container);
	}
}
