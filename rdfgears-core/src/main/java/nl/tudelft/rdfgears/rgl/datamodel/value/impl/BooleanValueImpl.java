package nl.tudelft.rdfgears.rgl.datamodel.value.impl;

import nl.tudelft.rdfgears.rgl.datamodel.value.BooleanValue;

/** 
 * In RGL, a 'False' Boolean is simulated by an empty set. a 'True' Boolean is simulated by a singleton set 
 * containing an empty record.  
 * This allows for some NRC optimalizations. But in our implementation we don't do this. It is just a boolean. 
 *
 * @author Eric Feliksik
 *
 */
public class BooleanValueImpl extends BooleanValue {
	/**
	 * Singleton true/false values
	 */
	private static BooleanValueImpl falseInstance = new BooleanValueImpl(false); 
	private static BooleanValueImpl trueInstance = new BooleanValueImpl(true);
	public static BooleanValueImpl getTrueInstance(){ return trueInstance; }
	public static BooleanValueImpl getFalseInstance(){ return falseInstance; }
	
	boolean isTrue;
	private BooleanValueImpl(boolean val){
		isTrue = val;
	}
	
	@Override
	public boolean isTrue() {
		return isTrue;
	}
//
//	@Override
//	public Iterator<Expression> iterator() {
//		return new Iterator<Expression>(){
//			private boolean hasNext = isTrue;
//			
//			@Override
//			public boolean hasNext() {
//				return hasNext;
//			}
//			@Override
//			public Expression next() {
//				if (! hasNext){
//					throw new RuntimeException("you must call hasNext() first, and a false-boolean has no bag-elements");
//				}
//				hasNext = false;
//				return EmptyRecordValue.getInstance();
//			}
//			@Override
//			public void remove() { /* not implemented */ }
//		};	
//	}
//	
//	@Override
//	public int size() {
//		return isTrue ? 1 : 0; 
//	}
	
	@Override
	public void prepareForMultipleReadings() {
		/* nothing to do */
	}


}
