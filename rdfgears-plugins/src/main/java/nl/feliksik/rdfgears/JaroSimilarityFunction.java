package nl.feliksik.rdfgears;


import java.util.Map;

import nl.tudelft.rdfgears.engine.ValueFactory;
import nl.tudelft.rdfgears.rgl.datamodel.type.RDFType;
import nl.tudelft.rdfgears.rgl.datamodel.type.RGLType;
import nl.tudelft.rdfgears.rgl.datamodel.value.RGLValue;
import nl.tudelft.rdfgears.rgl.function.SimplyTypedRGLFunction;
import nl.tudelft.rdfgears.util.row.ValueRow;

public class JaroSimilarityFunction extends SimplyTypedRGLFunction {
	public static final String INPUT_1 = "s1";
	public static final String INPUT_2 = "s2";
	
	@Override
	public void initialize(Map<String, String> config) {
		this.requireInputType(INPUT_1, RDFType.getInstance());
		this.requireInputType(INPUT_2, RDFType.getInstance());
	}

	@Override
	public RGLType getOutputType() {
		return RDFType.getInstance();
	}
	
	@Override
	public RGLValue simpleExecute(ValueRow inputRow) {
		RGLValue val1 = inputRow.get(INPUT_1);
		RGLValue val2 = inputRow.get(INPUT_2);
		if (!val1.isLiteral() || ! val2.isLiteral())
			return ValueFactory.createNull("JaroSimilarity can only compare literals");
		
		String str1 = inputRow.get(INPUT_1).asLiteral().getValueString();
		String str2 = inputRow.get(INPUT_2).asLiteral().getValueString();
		double d = jaro(str1, str2);
		return ValueFactory.createLiteralDouble(d); 
	}


	
	/* 
	 * Copied from Silk 2.0 (Robert Isele, Anja Jentzsch, Chris Bizer), and ported to Java.  
	 */
	private static double jaro(String string1, String string2) { 
        //get half the length of the string rounded up - (this is the distance used for acceptable transpositions)
        int halflen = ((Math.min(string1.length(), string2.length())) / 2) + ((Math.min(string1.length(), string2.length())) % 2);

        //get common characters
        StringBuilder common1 = getCommonCharacters(string1, string2, halflen);
        StringBuilder common2 = getCommonCharacters(string2, string1, halflen);

        //check for zero in common
        if (common1.length() == 0 || common2.length() == 0) {
            return 0;
        }
        /*
        //check for same length common strings returning 0.0 is not the same
        if (common1.length != common2.length) {
            return 0.0
        }
        */

        //get the number of transpositions
        int transpositions = 0;

        for (int i=0; i<=Math.min(common1.length()-1, common2.length()-1); i++){
            if (common1.charAt(i) != common2.charAt(i))
            {
                transpositions++;
            }
        }

        transpositions = transpositions / 2;

        //calculate jaro metric
        return ( common1.length() / ((double) string1.length()) + 
        		 common2.length() / ((double) string2.length()) + 
        		(common1.length() - transpositions) / ((double)common1.length()) 
               ) / 3.0;
    }


    /**
     * returns a string buffer of characters from string1 within string2 if they are of a given
     * distance separation from the position in string1.
     *
     * @param string1
     * @param string2
     * @param distanceSep
     * @return a string buffer of characters from string1 within string2 if they are of a given
     *         distance separation from the position in string1
     */
    private static StringBuilder getCommonCharacters(String string1, String string2, int distanceSep){
    	StringBuilder returnCommons = new StringBuilder();
    	StringBuilder copy = new StringBuilder(string2);

        for (int i=0; i<string1.length(); i++){
        	char string1Char = string1.charAt(i);
            boolean foundIt = false; 
            int j = Math.max(0, i - distanceSep); 
            while (!foundIt && j < Math.min(i + distanceSep + 1, string2.length()))
            {
                if (copy.charAt(j) == string1Char)
                {
                    foundIt = true;
                    returnCommons.append(string1Char);
                    copy.setCharAt(j, '0');
                }
                j++;
            }
        }
        
        return returnCommons;
    }


    
    
	
}
