package nl.tudelft.rdfgears.rgl.workflow;


public class ProcessorInputPort {
	
	private FunctionProcessor proc;
	private String portName;
	public ProcessorInputPort(FunctionProcessor proc, String portName){
		this.proc = proc;
		this.portName = portName;
		
	}
	public FunctionProcessor getProcessor(){
		return proc;
	}
	public String getPortName(){
		return portName;
	}
	public boolean equals(Object object){
		if (this==object){
			return true;
		}
		if (object instanceof ProcessorInputPort){
			ProcessorInputPort port = (ProcessorInputPort ) object;
			return this.proc==port.proc && this.portName == port.portName; 
		}
		return false;
	}
}
