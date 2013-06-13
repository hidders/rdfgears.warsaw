package tools;

public class Timer {
	String msg = null;
	long startTime = 0;
	
	public Timer(){
	}
	public void start(String message){
		msg = message;
		System.out.println("Start task: "+msg);
		startTime = System.currentTimeMillis();
	}
	public void end(){
		assert(startTime!=0) : "Error: you must first call start()";
		long endTime = System.currentTimeMillis();
		
		long millis = endTime-startTime;
		
		long seconds = millis/1000;
		long rest = millis%1000;
		
		System.out.println("Finished task: "+msg+": "+seconds+"."+rest+" seconds");
		
	}
	
	public void reset(String message){
		if (message!=null){
			msg = message;
			startTime = 0;
		}
	}
}
