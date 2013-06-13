package tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A bag that is backed by a HashMap that maps each entry to an occurrence frequency. 
 * @author Eric Feliksik
 *
 */
public class HashBag {
	Map<Object, Integer> frequency = new HashMap<Object, Integer>();  
	public void add(Object o){
		if(! frequency.containsKey(o)){
			frequency.put(o, new Integer(1));
		}
		else {
			frequency.put(o, new Integer(frequency.get(o).intValue()+1));
		}
	}
	public boolean contains(Object o){
		return getCount(o)>0;
	}
	public int getCount(Object o){
		Integer iObj = frequency.get(o);
		if (iObj==null){
			return 0;
		}
		return iObj.intValue();
	}
	/*
	 * not necessarily contained in bag (possibly frequency==0)  
	 */
	public Set<Object> getElements(){
		return frequency.keySet();
	}
	
	public boolean equals(Object o){
		if (this==o) return true;
		if (!(o instanceof HashBag))
			return false;
		HashBag oBag = (HashBag) o; 
		Iterator<Object> oBagIter = oBag.getElements().iterator();
		
		/* check if everything in oBag has same frequency in this bag */
		while(oBagIter.hasNext()){
			Object elem = oBagIter.next();
			if (oBag.getCount(elem)!=this.getCount(elem)){
				return false;
			}
		}
		
		/* check if everything in thisbag has same frequency in this oBag */
		Iterator<Object> thisBagIter = this.getElements().iterator();
		while(thisBagIter.hasNext()){
			Object elem = thisBagIter.next();
			if (oBag.getCount(elem)!=this.getCount(elem)){
				return false;
			}
		}
		
		return true;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		Iterator<Object> keyIter = frequency.keySet().iterator();
		builder.append("FrequencyBag {{ ");
		while(keyIter.hasNext()){
			Object key = keyIter.next();
			builder.append(key);
			builder.append(':');
			builder.append(frequency.get(key));
			builder.append(" , ");
		}

		builder.append("}} ");
		return builder.toString();
	}
	
}
