package tools;

public class Profiling {

	public static double getUsedMemoryBytes(){
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}
	
	public static void printUsedMemoryString(){
		System.out.println( String.format("Memory used: %.2f GB",getUsedMemoryBytes()/(1024*1024d*1024)));		
	}
	
	public static void collectGarbage(){
		System.out.print("Will collect garbage on request, ");
		printUsedMemoryString();
		System.out.print("collecting...");
		System.gc();
		System.out.print("ok, ");
		printUsedMemoryString();
		
		
	}
	
	public static void sleep(int seconds){
		System.out.println("Sleeping for "+seconds+" seconds.");
		try {
			Thread.sleep((long) seconds*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println("Sleep interrupted. ");
			e.printStackTrace();
		}
	}
	
}
