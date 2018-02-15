/**
 * reading praat files
 * file taken from ELAN open sources
 * adaptation by Christophe Parisse
 */

package fr.ortolang.teicorpo;

/**
 * A class that converts Praat special character sequences to the corresponding
 * unicode character.
 */
public class PraatSpecialChars {
    private final String bs = "\\";
    private final char quote = '"';

    /** a look up table for Praat backslash sequences (special symbols) */
    public final String[][] lookUp = {
            // european symbols
            {"\\a\"", "\u00e4" },
            { "\\a\u2019", "\u00e1" },
            { "\\a\u2018", "\u00e0" },
            { "\\a^", "\u00e2" },
            { "\\e\"", "\u00eb" },
            { "\\e\u2019", "\u00e9" },
            { "\\e\u2018", "\u00e8" },
            { "\\e^", "\u00ea" },
            { "\\i\"", "\u00ef" },
            { "\\i\u2019", "\u00ed" },
            { "\\i\u2018", "\u00ec" },
            { "\\i^", "\u00ee" },
            { "\\o\"", "\u00f6" },
            { "\\o\u2019", "\u00f3" },
            { "\\o\u2018", "\u00f2" },
            { "\\o^", "\u00f4" },
            { "\\u\"", "\u00fc" },
            { "\\u\u2019", "\u00fa" },
            { "\\u\u2018", "\u00f9" },
            { "\\u^", "\u00fb" },
            { "\\y\"", "\u00ff" },
            { "\\A\"", "\u00c4" },
            { "\\A\u2019", "\u00c1" },
            { "\\A\u2018", "\u00c0" },
            { "\\A^", "\u00c2" },
            { "\\E\"", "\u00cb" },
            { "\\E\u2019", "\u00c9" },
            { "\\E\u2018", "\u00c8" },
            { "\\E^", "\u00ca" },
            { "\\I\"", "\u00cf" },
            { "\\I\u2019", "\u00cd" },
            { "\\I\u2018", "\u00cc" },
            { "\\I^", "\u00ce" },
            { "\\O\"", "\u00d6" },
            { "\\O\u2019", "\u00d3" },
            { "\\O\u2018", "\u00d2" },
            { "\\O^", "\u00d4" },
            { "\\U\"", "\u00dc" },
            { "\\U\u2019", "\u00da" },
            { "\\U\u2018", "\u00d9" },
            { "\\U^", "\u00db" },
            { "\\Y\"", "\u0178" },
            { "\\a~", "\u00e3" },
            { "\\n~", "\u00f1" },
            { "\\o~", "\u00f5" },
            { "\\A~", "\u00c3" },
            { "\\N~", "\u00d1" },
            { "\\O~", "\u00d5" },
            { "\\ae", "\u00e6" },
            { "\\o/", "\u00f8" },
            { "\\ao", "\u00e5" },
            { "\\Ae", "\u00c6" },
            { "\\O/", "\u00d8" },
            { "\\Ao", "\u00c5" },
            { "\\c,", "\u00e7" },
            { "\\C,", "\u00c7" },
            { "\\ss", "\u00df" },
            { "\\!d", "\u00a1" },
            { "\\?d", "\u00bf" },
            { "\\cu", "\u20ac" },
            { "\\Lp", "\u00a3" },
            { "\\Y=", "\u00a5" },
            { "\\fd", "\u0192" },
            { "\\c/", "\u00a2" },
            { "\\SS", "\u00a7" },
            { "\\||", "\u00b6" },
            { "\\co", "\u00a9" },
            { "\\re", "\u00ae" },
            { "\\tm", "\u2122" },
            { "\\a_", "\u00aa" },
            { "\\o_", "\u00ba" },
            { "\\<<", "\u00ab" },
            { "\\>>", "\u00bb" },
            // mathematical symbols
            {"\\.c", "\u00b7" },
            { "\\xx", "\u00d7" },
            { "\\:-", "\u00f7" },
            { "\\/d", "\u002f" },
            { "\\dg", "\u00b0" },
            { "\\\u2019p", "\u2032" },
            { "\\\"p", "\u2033" },
            { "\\--", "\u2013" },
            { "\\+-", "\u00b1" },
            { "\\<_", "\u2264" },
            { "\\>_", "\u2265" },
            { "\\=/", "\u2260" },
            { "\\no", "\u00ac" },
            { "\\an", "\u2227" },
            { "\\or", "\u2228" },
            { "\\At", "\u2200" },
            { "\\Er", "\u2203" },
            { "\\.3", "\u2234" },
            { "\\oc", "\u221d" },
            { "\\=3", "\u2261" },
            { "\\~~", "\u2248" },
            { "\\Vr", "\u221a" },
            { "\\<-", "\u2190" },
            { "\\->", "\u2192" },
            { "\\<>", "\u2194" },
            { "\\<=", "\u21d0" },
            { "\\=>", "\u21d2" },
            { "\\eq", "\u21d4" },
            

            { "\\^|", "\u2191" },
            { "\\=~", "\u2245" },
            { "\\_|", "\u2193" },
            { "\\oo", "\u221E" },
            { "\\Tt", "\u27c2" }, // or \u22a5
            { "\\O|", "\u2205" },
            { "\\ni", "\u2229" },
            { "\\uu", "\u222a" },
            { "\\c=", "\u221E" },
            { "\\e=", "\u2208" },
            { "\\dd", "\u2202" },
            { "\\ox", "\u2297" },
            { "\\o+", "\u2295" },
            { "\\su", "\u221E" },
            { "\\in", "\u222b" },
            
            // greek letters
            {"\\al", "\u03b1" },
            { "\\be", "\u03b2" },
            { "\\ga", "\u03b3" },
            { "\\de", "\u03b4" },
            { "\\ep", "\u03b5" },
            { "\\ze", "\u03b6" },
            { "\\et", "\u03b7" },
            { "\\te", "\u03b8" },
            { "\\io", "\u03b9" },
            { "\\ka", "\u03ba" },
            { "\\la", "\u03bb" },
            { "\\mu", "\u03bc" },
            { "\\nu", "\u03bd" },
            { "\\xi", "\u03be" },
            { "\\on", "\u03bf" },
            { "\\pi", "\u03c0" },
            { "\\ro", "\u03c1" },
            { "\\si", "\u03c3" },
            { "\\ta", "\u03c4" },
            { "\\up", "\u03c5" },
            { "\\fi", "\u03c6" },
            { "\\ci", "\u03c7" },
            { "\\ps", "\u03c8" },
            { "\\om", "\u03c9" },
            { "\\t2", "\u03d1" },
            { "\\s2", "\u03c2" },
            { "\\f2", "\u03d5" },
            { "\\o2", "\u03d6" },
            // capitals
            {"\\Al", "\u0391" },
            { "\\Be", "\u0392" },
            { "\\Ga", "\u0393" },
            { "\\De", "\u0394" },
            { "\\Ep", "\u0395" },
            { "\\Ze", "\u0396" },
            { "\\Et", "\u0397" },
            { "\\Te", "\u0398" },
            { "\\Io", "\u0399" },
            { "\\Ka", "\u039a" },
            { "\\La", "\u039b" },
            { "\\Mu", "\u039c" },
            { "\\Nu", "\u039d" },
            { "\\Xi", "\u039e" },
            { "\\On", "\u039f" },
            { "\\Pi", "\u03a0" },
            { "\\Ro", "\u03a1" },
            { "\\Si", "\u03a3" },
            { "\\Ta", "\u03a4" },
            { "\\Up", "\u03a5" },
            { "\\Fi", "\u03a6" },
            { "\\Ci", "\u03a7" },
            { "\\Ps", "\u03a8" },
            { "\\Om", "\u03a9" },
            // phonetic symbols
            {"\\bs", "\\" },
            { "\\bu", "\u2022" },
            { "\\cl", "\u2663" },
            { "\\di", "\u2666" },
            { "\\he", "\u2665" },
            { "\\sp", "\u2660" },
        };

    /**
     * Creates a new PraatSpecialChars instance
     */
    public PraatSpecialChars() {
        super();
    }

    /**
     * Replaces the Praat special symbol sequences in the specified string  by
     * their corresponding unicode character.
     *
     * @param value the string to convert
     *
     * @return the converted string
     */
    public String convertSpecialChars(String value) {
        if (value == null) {
            return null;
        }

        StringBuffer buf = new StringBuffer(value);
        String repla = null;
        int index = -1;

        while ((index = buf.indexOf(bs, index + 1)) > -1) {
            if (index < (buf.length() - 2)) {
                repla = lookupSequence(buf.substring(index, index + 3));

                if (repla != null) {
                    if ((buf.charAt(index + 2) == quote) &&
                            (buf.charAt(index + 3) == quote)) {
                        buf.replace(index, index + 4, repla);
                    } else {
                        buf.replace(index, index + 3, repla);
                    }
                }
            }
        }

        return buf.toString();
    }

    /**
     * Finds the corresponding unicode character of the specified sequence.
     *
     * @param seq the Praat special symbol sequence
     *
     * @return the unicode character as a String (or null)
     */
    public String lookupSequence(String seq) {
        for (int i = 0; i < lookUp.length; i++) {
            if (lookUp[i][0].equals(seq)) {
                return lookUp[i][1];
            }
        }

        return null;
    }
    
    /**
     * Checks for the presence of ISO control characters that can prevent saving as XML.
     * These characters are replaced by spaces.
     * 
     * @param value the annotation value to check
     * @return a new string if anything changed, the passed string otherwise
     */
    public String replaceIllegalXMLChars(String value) {
    	if (value != null) {
    		boolean changed = false;
    		char[] ch = value.toCharArray();
    		char c;
    		for (int i = 0; i < ch.length; i++) {
    			c = ch[i];
    			if (Character.isISOControl(c)) {// illegal in XML
    				ch[i] = '\u0020';// maybe better to remove?
    				changed = true;
    			}
    		}
    		if (changed) {
    			return new String(ch);
    		} else {
    			return value;
    		}
    	}
    	
    	return null;
    }
}
