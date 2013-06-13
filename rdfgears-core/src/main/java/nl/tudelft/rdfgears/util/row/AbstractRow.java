package nl.tudelft.rdfgears.util.row;

import java.util.Iterator;
import java.util.Set;


public abstract class AbstractRow<E> implements Row<E>{

	@Override 
	public boolean equals(Object object){
		if (this==object){
			return true;
		}
		if (object instanceof Row){
			Row<?> row = (Row<?>) object;
			Set<String> thisRange = this.getRange();
			Set<String> rowRange = row.getRange();
			
			if (! thisRange.equals(rowRange)){
				return false;
			}
			
			/* check whether all elements are equal */
			Iterator<String> thisIter = thisRange.iterator();
			while (thisIter.hasNext()){
				String name = thisIter.next();
				if (! this.get(name).equals(row.get(name))){
					return false;
				}
			}
			return true; /* all elements are equal */
		}
		return false;
	}
}
