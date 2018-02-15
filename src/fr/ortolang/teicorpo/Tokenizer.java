/**
 * Tokenize.java
 * Author: Christophe Parisse
 * see below for origin - translation into java
 */

/**
########################################################################
#                                                                      #
#  tokenization script for tagger preprocessing                        #
#  Author: Helmut Schmid, IMS, University of Stuttgart                 #
#          Serge Sharoff, University of Leeds                          #
#  Groovy Translation: mdecorde                                        #
#  Description:                                                        #
#  - splits input text into tokens (one token per line)                #
#  - cuts off punctuation, parentheses etc.                            #
#  - disambiguates periods                                             #
#  - preserves SGML markup                                             #
#                                                                      #
########################################################################
*/

package fr.ortolang.teicorpo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	// characters which have to be cut off at the beginning of a word
	/** All input will use this encoding */
	final static String inputEncoding = "UTF-8";

	/** The P char. */
	static String PChar = "\\[{(\\`\"‚„†‡‹‘’“”•–—›";
	// characters which have to be cut off at the end of a word

	/** The F char. */
	static String FChar = "\\]}'`\"),;:!?%‚„…†‡‰‹‘’“”•–—›";
	// character sequences which have to be cut off at the beginning of a word

	/** The P clitic. */
	static String PClitic = "";
	// character sequences which have to be cut off at the end of a word

	/** The F clitic. */
	static String FClitic = "";

	/** The punctuation. */
	static String NonAlphaSymbols = "`£*$,;:?./+=#@!-_()\'\"\\n\\r";

	/** The Token. */
	static HashMap<String, Integer> Token = new HashMap<String, Integer>();

	/**
	 * Instantiates a new tT tokenizer.
	 *
	 * @param lang
	 *            the lang
	 * @param abbr
	 *            the abbr
	 */
	public static void init(String lang, String abbr) {
		switch (lang) {
		case "en": // English
			FClitic = "'(s|re|ve|d|m|em|ll)|n\'t";
			break;
		case "it": // Italian
			PClitic = "[dD][ae]ll\'|[nN]ell\'|[Aa]ll\'|[lLDd]\'|[Ss]ull\'|[Qq]uest\'|[Uu]n\'|[Ss]enz\'|[Tt]utt\'";
			break;
		case "fr": // French
			PClitic = "[dcjlmnstDCJLNMST]\'|[Qq]u\'|[Jj]usqu\'|[Ll]orsqu\'";
			FClitic = "-t-elles?|-t-ils?|-t-on|-ce|-elles?|-ils?|-je|-la|-les?|-leur|-lui|-mes?|-m\'|-moi|-nous|-on|-toi|-tu|-t\'|-vous|-en|-y|-ci|-elle|-il";
			break;
		}

		if (abbr != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(abbr), inputEncoding));
				String line = reader.readLine();
				while (line != null) {
					line = line.replaceAll("^[ \t\r\n]+", "");
					line = line.replaceAll("^[ \t\r\n]+$", "");
					if (!(line.matches("^#") || line.matches("\\s$"))) // ignore
																		// comments
					{
						Token.put(line, 1);
					}
					line = reader.readLine();
				}
				if (reader != null)
					reader.close();
			} catch (FileNotFoundException fnfe) {
				System.err.println("Erreur fichier : " + abbr + " indisponible");
			} catch (IOException ioe) {
				System.err.println("Erreur sur fichier : " + abbr);
				ioe.printStackTrace();
			}
		}
	}
	
	/**
	 * splitText.
	 *
	 * @param input the input
	 * @return the output
	 */
	public static ArrayList<String> splitTextTT(String line) {
		ArrayList<String> al = new ArrayList<String>();
		String buffer = "";
		// println "line: "+line
		line = line.replaceAll("\t"," "); // replace newlines and tab characters with blanks
		line = line.replaceAll("\n"," "); // replace newlines and tab characters with blanks
		
		String replace = line.replaceAll("(<[^<> ]*) ([^<>]*>)", "$1ÿ$2");
		while(replace != line) // replace blanks within SGML tags
		{
			line = replace;
			replace = replace.replaceAll("(<[^<> ]*) ([^<>]*>)", "$1ÿ$2");
		}
		line = replace;
		
		line = line.replaceAll(" ", "þ"); // replace whitespace with a special character
		// tr/\377\376/ \377/; // 377=ÿ , 376=þ
		line = line.replaceAll("ÿ", " "); // restore SGML tags
		line = line.replaceAll("þ", " ÿ"); // restore SGML tags
		//line = line.replaceAll(/ÿ/," ");
		
		// prepare SGML-Tags for tokenization
		line = line.replaceAll("(<[^<>]*>)", "ÿ$1ÿ");
		line = line.replaceAll("^ÿ","");
		line = line.replaceAll("ÿ$","");
		line = line.replaceAll("ÿÿÿ*","ÿ");
		
		String[] split1 = line.split("ÿ");
		boolean finish = true;
		String suffix = "";
		Pattern p;
		Matcher m;

		for (int i=0; i < split1.length; i++) {
			String s = split1[i];
			if (Pattern.matches("^<.*>$", s)) {
				// SGML tag
				buffer += s;
				addAList(al, buffer);
				buffer = "";
			} else { // add a blank at the beginning and the end of each segment
				s = " " + s + " ";
				// insert missing blanks after punctuation
				s = s.replaceAll("(\\.\\.\\.)", " ... ");
				s = s.replaceAll("([;\\!\\?])([^ ])", "$1 $2");
				s = s.replaceAll("([.,:])([^ 0-9.])", "$1 $2");

				String[] sSplit = s.split(" ");
				for (int j=0; j < sSplit.length; j++) {
					String s2 = sSplit[j];
					
					finish = false;
					suffix = "";
					// separate punctuation and parentheses from words
					while(!finish) 
					{
						//println "suffix: "+suffix
						finish = true;
						// 	cut off preceding punctuation
						p = Pattern.compile("^([" + PChar + "])(.*)$");
						m = p.matcher(s2);
						if(m.matches()) {
							s2 = m.group(2);
							buffer += m.group(1);
							addAList(al, buffer);
							buffer = "";
							finish = false;
						}
						
						// 	cut off trailing punctuation
						p = Pattern.compile("^(.*)([" + FChar + "])$");
						m = p.matcher(s2);
						if(m.matches()) {
							s2 = m.group(1);
							suffix = m.group(2) + "\n" + suffix;
							finish = false;
							
							if (s2.length() == 0)
								buffer += suffix;
						}
					
						// cut off trailing periods if punctuation precedes
						p = Pattern.compile("([" + FChar + "])\\.$");
						m = p.matcher(s2);
						if(m.matches()) {
							suffix = ".\n" + suffix;
							if(s2.length() == 0)
							{
								 s2 = m.group(1);
							}
							else
							{
								suffix = m.group(1) + "\n" + suffix;
								s2 = "";
							}
							finish = false;
						}
					}// end while
					
					// handle explicitly listed tokens
					if(Token.containsKey(s2))
					{
						buffer += s2;
						addAList(al, buffer);
						buffer = suffix;
						continue;
					}
					
					// abbreviations of the form A. or U.S.A.
					if(Pattern.matches("^([A-Za-z-]\\.)+$", s2))	
					{
						buffer += s2;
						addAList(al, buffer);
						buffer = suffix;
						continue;
					}
					
					// disambiguate periods
					p = Pattern.compile("^(..*)\\.$");
					m = p.matcher(s2);
					Pattern p2 = Pattern.compile("^[0-9]+\\.$");
					Matcher m2 = p2.matcher(s);
					if(m.matches() &&
						(!s2.equals("...")) && 
						!m2.matches())
					{
						s2 = m.group(1);
						suffix = ".\n" + suffix;
						if(Token.containsKey(s2))
						{
							buffer += s2;
							addAList(al, buffer);
							buffer = suffix;
							continue;
						}
					}
					
					// cut off clitics
					if(PClitic.length() > 0) {
						while (true) {
							p = Pattern.compile("^(" + PClitic + ")(.*)");
							m = p.matcher(s2);
							if(m.matches()) {
								s2 = m.group(2);
								buffer += m.group(1);
								addAList(al, buffer);
								buffer = "";
							} else
								break;
						}
					}

					if(FClitic.length() > 0) {
						while (true) {
							p = Pattern.compile("(.*)(" + FClitic + ")$");
							m = p.matcher(s2);
							if(m.matches()) {
								s2 = m.group(1);
								buffer += m.group(2);
								addAList(al, buffer);
								buffer = "";
							} else
								break;
						}
					}
					if(s2.length() > 0) {
						buffer += s2;
						addAList(al, buffer);
						buffer = suffix;
					}
				}
			}
		}
		if (!buffer.isEmpty())
			addAList(al, buffer);
		return al;
	}

	private static void addAList(ArrayList<String> al, String buffer) {
		if (isOnlyMadeOf(buffer, NonAlphaSymbols)) {
			buffer = ".";
		}
		int p = buffer.indexOf("\n");
		if (p < 0)
			al.add(buffer);
		else {
			if (p != 0)
				al.add(buffer.substring(0,p));
			if (p != buffer.length() - 1)
				al.add(buffer.substring(p+1));
		}
	}

	private static boolean isOnlyMadeOf(String s, String symb) {
		if (s.matches("^[" + symb + "]+$")) {
			// contains only listed chars
			return true;
		} else {
			// contains other chars
			return false;
		}
	}

	public static void main(String[] args) {
		Tokenizer.init("fr", null);
		String ns = Utils.join(args);
		ArrayList<String> p = Tokenizer.splitTextTT(ns);
		for (int i = 0; i < p.size(); i++)
			System.out.print(" {" + p.get(i) + "}");
		System.out.println("");
	}

	/*
	 * public static ArrayList<String> splitText(String s) { String ws =
	 * "[\\p{Z}\\p{C}]+"; // caractères blancs String ps =
	 * "[\\p{Ps}\\p{Pe}\\p{Pi}\\p{Pf}\\p{Po}\\p{S}]"; // ponctuations
	 * séparateurs String ap = "['‘’]"; // apostrophes String fp =
	 * "[.!?]+|\\.\\.|\\.\\.\\.|…|\\|\\+\\.\\.\\.|\\+\\.\\?|\\+\\.\\!|"; // fin
	 * de phrase System.err.println(s); String[] b = s.split(ws);
	 * ArrayList<String> p = new ArrayList<String>(); for (String be: b) {
	 * System.err.println("be: " + be); String[] bes = be.split("((?<=" + ps +
	 * ")|(?=" + ps + "))"); for (String bese: bes) { System.err.println(
	 * "bese: " + bese); p.add(bese); } } return p; }
	 * 
	 * public static void main(String[] args) { String ns = Utils.join(args);
	 * ArrayList<String> p = splitText(ns); for (int i=0; i<p.size(); i++)
	 * System.out.print(" {" + p.get(i) + "}"); System.out.println(""); }
	 */
}