package fr.ortolang.teicorpo;
/**
 * @author Christophe Parisse
 * charge un fichier CHAT
 * fonctions utilitaires pour accéder aux données
 *
 * usage principal: initialise une structure de données qui permet de balayer et tester toutes les lignes de CHAT
 * les informations de métadonnées (champs Participants et ID sont analysées et extraites séparement
 * des variables spécifiques sont crées pour contenir ces données (voir ID et chatFilename, mediaFilename,
 * mediaType, birth, date, location, transcriber)
 * nbMainLines() : nombre de lignes principales
 * ml(i) accès à la ligne principale de numéro 'i' (toutes les lignes y compris les \@)
 * mlc(i) accès à la ligne principale nettoyée de numéro 'i' (toutes les lignes y compris les \@)
 * startMl(i) : timecode de début de la ligne 'i'
 * endMl(i) : timecode de fin de la ligne 'i'
 * nbTiers(i) : nombre de tiers de la ligne 'i'
 * t(i,j) : tiers 'j' de la ligne 'i'
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFile {
	/** All input will use this encoding */
	final static String inputEncoding = "UTF-8";
	boolean wellFormed = true;

	public static String loc(String ln) {
		int n = ln.indexOf(':');
		if (n == -1) return ln;
		return ln.substring(1,n);
	}

	public static String ctn(String ln) {
		int n = ln.indexOf(':');
		if (n == -1) return ln;
		return ln.substring(n+1,ln.length()).trim();
	}

	public class ID {
		String code;
		String name;
		String role;
		// dans l'@ID
		String language;
		String corpus;
		//code;
		String age;
		String sex;
		String group;
		String SES;
		// role;
		String education;
		String customfield;
	}

	String chatFilename;
	String mediaFilename;
	String mediaType;
	String birth;
	String date;
	String location;
	String transcriber;
	String situation;
	String[] lang;
	String timeDuration;
	String timeStart;
	ArrayList<String> comments = new ArrayList<String>();
	ArrayList<String> gemes = new ArrayList<String>();
	ArrayList<String> otherInfo = new ArrayList<String>();
	ArrayList<ID> ids= new ArrayList<ID>();
	boolean inMainLine = false;

	void findInfo(boolean verbose, TierParams tparams) {
		if (!tparams.inputFormat.equals(".cha")) return;
		// find all types of information and preprocess it.
		int sz = nbMainLines();
		boolean inHeader = true;
		Set<String> idsMap= new HashSet<String>();;
		for (int i=0; i<sz; i++) {
			if (ml(i).startsWith("*")) {
				inHeader = false;
				ChatLine cl = new ChatLine(ml(i));
				// System.out.printf("== * (%s)(%s)%n", cl.head, cl.tail);
				if (cl.head.length() > 1) {
					String code = cl.head.substring(1);
					if (tparams.target.equals("dinlang")) {
						// if in a list of special name, replace it
						String oc = code;
						// System.out.printf("IN *%n");
						code = equivalence(code, tparams);
						// System.out.printf("Changed (%s) --> (%s) %n", oc, code);
					}
					if (!idsMap.contains(code)) {
						idsMap.add(code);
						ID nid = new ID();
						nid.code = code;
						if (nid.code.isEmpty())
							nid.code = "UNK";
						if (nid.code.equals("UNK"))
							nid.role = "Unknown";
						ids.add(nid);
					}
				}
			}
			if ( ml(i).toLowerCase().startsWith("@participants") ) {
				String[] rls = ml(i).split("[:,]+");
				for (int k = 1; k < rls.length; k++) {
					String[] wds = rls[k].trim().split("\\s+");
					String trueCode = wds[0];
					if (tparams.target.equals("dinlang")) {
						// if in a list of special name, replace it
						String oc = trueCode;
						// System.out.printf("IN @participants%n");
						trueCode = equivalence(trueCode, tparams);
						// System.out.printf("Part Changed (%s) --> (%s) %n", oc, trueCode);
					}
					// System.out.printf("L=%s='%s'/'%s'%n", rls[k], wds[0], trueCode);
					if (wds.length == 2) {
						ID nid = new ID();
						nid.code = trueCode;
						nid.role = wds[1];
						ids.add(nid);
						idsMap.add(trueCode);
						// System.out.printf("ID2: %s %s%n", nid.code, nid.role);
					} else if (wds.length == 3) {
						ID nid = new ID();
						nid.code = trueCode;
						nid.name = wds[1];
						nid.role = wds[2];
						ids.add(nid);
						idsMap.add(trueCode);
						// System.out.printf("ID3: %s %s %s%n", nid.code, nid.role, nid.name);
					} else {
						System.err.printf("Bad ID: %s%n", rls[k]);
					}
				}
			} else if ( ml(i).toLowerCase().startsWith("@media") ) {
				String[] wds = ml(i).split("[\\s:,]+");
				if (wds.length == 3) {
					// try to find the mediafile according to type
					mediaFilename = wds[1];
					mediaType =  wds[2];
				} else if (wds.length == 2) {
					// type is not defined
					mediaFilename = wds[1];
					mediaType =  "audio";
				} 
			} else if ( ml(i).toLowerCase().startsWith("@id") ) {
				String[] wds = ml(i).split("\\|");
				if (wds.length < 3) {
					System.err.println("error on IDs for " + ml(i));
					continue;
				}
				boolean found = false;
				String trueCode = wds[2];
				if (tparams.target.equals("dinlang")) {
					// if in a list of special name, replace it
					String oc = trueCode;
					// System.out.printf("IN @ID%n");
					trueCode = equivalence(trueCode, tparams);
					// System.out.printf("Changed (%s) --> (%s) %n", oc, trueCode);
				}
				for (ID id: ids) {
					if ( id.code.equals(trueCode) ) {
						found = true;
						// dans l'@ID
						if (wds.length > 0) {
							String[] details = wds[0].split(":\\s*");
							//System.err.printf("ID: %s %d%n", wds[0], details.length);
							if(details.length >= 2){
								//System.err.printf("ET: %s%n", details[0]);
								//System.err.printf("LANG: %s%n", details[1]);
								id.language = details[1];
							}
						}
						if (wds.length > 1) id.corpus = wds[1].replaceAll("\\s+", " ");
						//code;
						if (wds.length > 3) id.age = wds[3].replaceAll("\\s+", " ");
						if (wds.length > 4) id.sex = wds[4].replaceAll("\\s+", " ");
						if (wds.length > 5) id.group = wds[5].replaceAll("\\s+", " ");
						if (wds.length > 6) id.SES = wds[6].replaceAll("\\s+", " ");
						// role;
						if (wds.length > 8) id.education = wds[8].replaceAll("\\s+", " ");
						if (wds.length > 9) id.customfield = wds[9].replaceAll("\\s+", " ");
					}
				}
				if (found == false) {
					System.err.println("error on ID " + ml(i) + "not found in participants - added to the list");
					ID nid = new ID();
					// dans l'@ID
					if (wds.length > 0) {
						String[] details = wds[0].split(":\\s*");
						if(details.length >= 2){
							nid.language = details[1];
						}
					}
					if (wds.length > 1) nid.corpus = wds[1];
					if (wds.length > 2) nid.code = trueCode;
					if (wds.length > 3) nid.age = wds[3];
					if (wds.length > 4) nid.sex = wds[4];
					if (wds.length > 5) nid.group = wds[5];
					if (wds.length > 6) nid.SES = wds[6];
					if (wds.length > 7) nid.role = wds[7];
					if (wds.length > 8) nid.education = wds[8];
					if (wds.length > 9) nid.customfield = wds[9];
					ids.add(nid);
					idsMap.add(trueCode);
				}
			} else if ( ml(i).toLowerCase().startsWith("@location") ) {
				location = ml(i);
			} else if ( ml(i).toLowerCase().startsWith("@date") ) {
				date = ml(i);
			} else if ( ml(i).toLowerCase().startsWith("@birth") ) {
				if ( ml(i).indexOf("of CHI") != -1 )
					birth = ml(i);
			} else if ( ml(i).toLowerCase().startsWith("@comment") ) {
				if ( ml(i).indexOf("coder") != -1 ||  ml(i).indexOf("Coder") != -1)
					transcriber = ml(i);
				else{
					comments.add(ml(i));
				}
			} else if ( ml(i).toLowerCase().startsWith("@transcriber") ) {
				transcriber = ml(i);
			}
			else if ( ml(i).toLowerCase().startsWith("@situation") && inHeader) {
				situation = ml(i);
			}
			else if ( ml(i).toLowerCase().startsWith("@time Duration") ) {
				timeDuration = ml(i);
			}
			else if ( ml(i).toLowerCase().startsWith("@g") || ml(i).toLowerCase().startsWith("@bg") || ml(i).toLowerCase().startsWith("@eg")  || ml(i).toLowerCase().startsWith("@situation")) {
				gemes.add(ml(i));
			}
			else if ( ml(i).toLowerCase().startsWith("@languages") ) {
				int k = ml(i).indexOf(':');
				if (k < 0)
					k = ml(i).indexOf(' ');
				if (k < 0)
					k = ml(i).indexOf('\t');
				if (k>0) {
					String tail = ml(i).substring(k+1).trim();
					String w[] = tail.split("[\\s,]+");
					if (w.length > 0) lang = w;
				}
			}
			else if ( ml(i).toLowerCase().startsWith("@time start") ) {
				try{
					timeStart = ml(i).split("\t")[1];
				}
				catch(Exception e){
					if(!ml(i).split("\t|\\s")[1].contains(":")){
						timeStart = ml(i).split("\t|\\s")[1];
					}
				}
			}
			else if(ml(i).startsWith("@") && ml(i).split("\t").length>1 && inHeader ){
				otherInfo.add(ml(i));
			}
		}

		if(!verbose) return;
		System.out.println("chat_filename : " + chatFilename );
		System.out.println("media_filename : " + mediaFilename );
		System.out.println("media_type : " + mediaType );
		System.out.println("birth : " + birth );
		System.out.println("date : " + date );
		System.out.println("location : " + location );
		System.out.println("situation : "  + situation );
		System.out.println("transcriber : " + transcriber );
		System.out.println("language : " + lang );

		for (String com:comments){
			System.out.println("com :  " + com);
		}

		for (String info:otherInfo){
			System.out.println("info :  " + info);
		}

		for (ID id: ids) {
			System.out.println("NAME : " + id.name );
			System.out.println("ID-language : " + id.language );
			System.out.println("ID-corpus : " + id.corpus );
			System.out.println("ID-code : " + id.code );
			System.out.println("ID-age : " + id.age );
			System.out.println("ID-sex : " + id.sex );
			System.out.println("ID-group : " + id.group );
			System.out.println("ID-SES : " + id.SES );
			System.out.println("ID-role : " + id.role );
			System.out.println("ID-education : " + id.education );
			System.out.println("ID-customfield : " + id.customfield );
		}
	}

	String ageChild() {
		if (ids==null) return "";
		return age("CHI");
	}

	String age(String part) {
		if (ids==null) return "";
		for (ID id: ids)
			if ( id.code.equals(part) ) return id.age;
		return "";
	}

	int ageJour(String part) {
		if (ids==null) return -1;
		for (ID id: ids)
			if ( id.code.equals(part) ) {
				int jours = 0;
				String patternStr = "(\\d+);(\\d+).(\\d+)";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(id.age);
				if (matcher.find()) {
					jours = Integer.parseInt(matcher.group(1)) * 365;
					jours += Integer.parseInt(matcher.group(2)) * 30;
					jours += Integer.parseInt(matcher.group(3));
					return jours;
				}
				patternStr = "(\\d+);(\\d+).";
				pattern = Pattern.compile(patternStr);
				matcher = pattern.matcher(id.age);
				if (matcher.find()) {
					jours = Integer.parseInt(matcher.group(1)) * 365;
					jours += Integer.parseInt(matcher.group(2)) * 30;
					return jours;
				}
				patternStr = "(\\d+);(\\d+)";
				pattern = Pattern.compile(patternStr);
				matcher = pattern.matcher(id.age);
				if (matcher.find()) {
					jours = Integer.parseInt(matcher.group(1)) * 365;
					jours += Integer.parseInt(matcher.group(2)) * 30;
					return jours;
				}
				patternStr = "(\\d+);";
				pattern = Pattern.compile(patternStr);
				matcher = pattern.matcher(id.age);
				if (matcher.find()) {
					jours = Integer.parseInt(matcher.group(1)) * 365;
					return jours;
				}
				patternStr = "(\\d+)";
				pattern = Pattern.compile(patternStr);
				matcher = pattern.matcher(id.age);
				if (matcher.find()) {
					jours = Integer.parseInt(matcher.group(1)) * 365;
					return jours;
				}
			}
		return -1;
	}

	String corpus(String part) {
		if (ids==null) return "";
		for (ID id: ids)
			if ( id.code.equals(part) ) return id.corpus;
		return "";
	}

	String name(String part) {
		if (ids==null) return "";
		for (ID id: ids)
			if ( id.code.equals(part) ) return id.name;
		return "";
	}

	String role(String part) {
		if (ids==null) return "";
		for (ID id: ids)
			if ( id.code.equals(part) ) return id.role;
		return "";
	}

	String code(int c) {
		if (c < 0) return "";
		if (c >= ids.size()) return "";
		return ids.get(c).role;
		/*
		int i = 0;
		for (ID id: ids) {
			if ( i == c ) return id.role;
			i++;
		}
		return "";
		*/
	}

	ID id(String part) {
		if (ids==null) return null;
		for (ID id: ids)
			if ( id.code.equals(part) ) return id;
		return null;
	}

	class Tier {
		String tier;
		int nl;
		Tier(String t) {
			tier = t;
			nl = -1;
		}
		Tier(String t, int n) {
			tier = t;
			nl = n;
		}
	}

	class MainTier {
		String mainLine;
		String mainCleaned;
		String mainRaw;
		int startTime;
		int endTime;
		int nl;
		List<Tier> tiers;

		MainTier(String ml) {
			mainRaw = ml;
			String patternStr = ".*\\x15(\\d+)_(\\d+)\\x15";
			Pattern pattern = Pattern.compile(patternStr);
			Matcher matcher = pattern.matcher(ml);
			if (matcher.find()) {
				startTime = Integer.parseInt(matcher.group(1));
				endTime = Integer.parseInt(matcher.group(2));
				mainLine = ml.replaceAll("\\x15\\d+_\\d+\\x15",""); // replaceFirst
				mainLine = mainLine.replaceAll("\\t", " ");
				mainLine = mainLine.replaceAll("\\p{C}", "");
			} else {
				patternStr = ".*\\x15(.*?)_(\\d+)_(\\d+)\\x15";
				pattern = Pattern.compile(patternStr);
				matcher = pattern.matcher(ml);
				if (matcher.find()) {
					/*
					if (!matcher.group(1).isEmpty()) {
						System.err.println("pat found: " + matcher.group(1));
					}
					*/
					startTime = Integer.parseInt(matcher.group(2));
					endTime = Integer.parseInt(matcher.group(3));
					mainLine = ml.replaceAll("\\x15.*?\\x15",""); // replaceFirst
					mainLine = mainLine.replaceAll("\\t", " ");
					mainLine = mainLine.replaceAll("\\p{C}", "");
				} else {
					startTime = -1;
					endTime = -1;
					mainLine = ml;
					mainLine = mainLine.replaceAll("\\t", " ");
					mainLine = mainLine.replaceAll("\\p{C}", "");
				}
			}
			mainCleaned = ConventionsToChat.clean(mainLine);
			nl = -1;
			tiers = null;
		}

		MainTier(String ml, int start, int end) {
			mainRaw = ml;
			startTime = start;
			endTime = end;
			mainLine = ml;
			mainCleaned = ConventionsToChat.clean(ml);
			nl = -1;
			tiers = null;
		}

		MainTier(String ml, String style) {
			if (style.equals("noparticipant")) {
				ml = "*LOC: " + ml;
			} else {
				ml = "*" + ml.trim();
			}
			mainRaw = ml;
			mainLine = ml;
			mainCleaned = ConventionsToChat.clean(mainLine);
			nl = -1;
			tiers = null;
		}

		MainTier(String ml, int n) {
			this(ml);
			nl = n;
		}
		void addTier(String tier) {
			if (tiers==null)
				tiers = new LinkedList<Tier>();
			tiers.add(new Tier(tier.replaceAll("\\x15\\d+_\\d+\\x15","")));//tier
		}
		void addTier(String tier, int n) {
			if (tiers==null)
				tiers = new LinkedList<Tier>();
			tiers.add(new Tier(tier, n));
		}
		String ml() {
			return mainLine;
		}
		String mlc() {
			return mainCleaned;
		}
		int start() {
			return startTime;
		}
		int end() {
			return endTime;
		}
		String t(int n) {
			return tiers.get(n).tier;
		}
		int mlLNB() {
			return nl;
		}
		int tLNB(int n) {
			return tiers.get(n).nl;
		}
		void majtime(int ts, int te) {
			startTime = ts;
			endTime = te;
		}
	}

	List<MainTier> mainLines;

	ChatFile() {
		/**
		 * initialise une donnée de type ChatFile
		 */
		mainLines = new ArrayList<MainTier>();
	}

	void addML(String ml) {
		/*
		 * split into parts if the are more than one "\\x15.*?\\x15"
		 */
		Pattern pattern = Pattern.compile("\\x15.*?\\x15");
		Matcher matcher = pattern.matcher(ml);
		
		// look for the first pattern
		if (!matcher.find()) {
			// no pattern at all
			// process whole line directly
//			System.out.println("DIRECT:" + ml);
			mainLines.add( new MainTier(ml) );
			return;
		}
		// store the pattern
		int start = 0; // beginning of line
		int end = matcher.end(); // end of pattern
		ChatLine cl = new ChatLine(ml);

//		System.out.println("SE1: " + start + " " + end + "||||" + ml);

        while (matcher.find()) {    
            // found the pattern "+matcher.group()+" starting at index "+    
            // matcher.start()+" and ending at index "+matcher.end());
        	// find another pattern
        	// process the previous one
        	if (start == 0) {
        		mainLines.add( new MainTier(ml.substring(start, end)) );
//        		System.out.println("SENEXT: " + start + " " + end + "||||" + ml.substring(start, end));
        	} else {
        		mainLines.add( new MainTier(cl.type() + ":\t" + ml.substring(start, end)) );       		
//        		System.out.println("SENEXT(U): " + start + " " + end + "||||" + cl.type() + ":\t" + ml.substring(start, end));
        	}
    		start = end+1; // follows previous pattern
            end = matcher.end(); // end of current pattern
        }    
		
        // final part up to end of line
    	if (start == 0) {
//    		System.out.println("SEFINAL: " + start + " " + end + "||||" + ml.substring(start));
    		mainLines.add( new MainTier(ml) );
    	} else {
    		mainLines.add( new MainTier(cl.type() + ":\t" + ml.substring(start, end)) );       		
//    		System.out.println("SENFINAL(U): " + start + " " + end + "||||" + cl.type() + ":\t" + ml.substring(start, end));
    	}
	}
/*	void addML(String ml, int n) {
		mainLines.add( new MainTier(ml, n) );
	}
*/
	void addT(String ml) {
		MainTier last = mainLines.get( mainLines.size()-1 );
		last.addTier( ml );
	}
/*	void addT(String ml, int n) {
		MainTier last = mainLines.get( mainLines.size()-1 );
		last.addTier( ml, n );
	}
*/
	String equivalence(String part, TierParams tparams) {
//		System.out.printf("EQUIVALENCE FOR [%s]%n", part);
		for (Map.Entry<String, SpkVal> entry : tparams.tv.entrySet()) {
			String key = entry.getKey();
			if (key.equals(part)) {
				// System.out.printf("FOUND>> [%s] [%s] [%s]%n", part, key, entry.getValue().genericvalue);
				return entry.getValue().genericvalue;
			}
		}
		// System.out.printf("NOT FOUND>> [%s]%n", part);
		return part;
	}

	void insertML(String ml, TierParams tparams) {
		if ( ml.startsWith("%") ) {
			if (inMainLine == false) {
				inMainLine = true;
				addML("*UNK:\t."); // adds an empty line
			}
			addT(ml);
		} else {
			if ( ml.startsWith("*") ) {
				inMainLine = true;
//				System.out.printf("Dinlang (%s)%n", tparams.target);
				if (tparams.target.equals("dinlang")) {
					// extract name of participant
//					System.out.printf("Dinlang passed%n");
					ChatLine cl = new ChatLine(ml);
					// if in a list of special name, replace it
					String code = cl.head.substring(1);
//					System.out.printf("IN insertML%n");
					String dinlangName = equivalence(code, tparams);
					// System.out.printf("Changed (%s) --> (%s) %n", code, dinlangName);
					// split the line in a main line plus a secondary line
					// get time is there is one
					String patternStr = "(.*)(\\x15\\d+_\\d+\\x15)(.*)";
					Pattern pattern = Pattern.compile(patternStr);
					Matcher matcher = pattern.matcher(cl.tail);
					if (matcher.find()) {
						// add the main line with the time in it
						addML("*" + dinlangName + ":\tfra " + matcher.group(2));
						// add the secondary line
						addT("%" + Utils.languagingScript + ":\t" + matcher.group(1) + " " + matcher.group(3));
					} else {
						// add the main line (without time)
						addML("*" + dinlangName + ":\tfra");
						// add the secondary line
						addT("%" + Utils.languagingScript + ":\t" + cl.tail);
					}
				} else {
					addML(ml);
				}
			} else {
				inMainLine = false;
				addML(ml);
			}
		}
	}

	void load(String fn, TierParams tparams) throws IOException {
		if (tparams.inputFormat.equals(".txt")) {
			loadText(fn, tparams);
			return;
		}
		if (tparams.inputFormat.equals(".srt")) {
			loadSrt(fn, tparams);
			return;
		}
		chatFilename = fn;
		String line = "";
		String ml = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new InputStreamReader(new FileInputStream(fn), inputEncoding) );
			while((line = reader.readLine()) != null) {
				// Traitement du flux de sortie de l'application si besoin est
				if ( line.startsWith(" ") ) {
					ml += line;
				} else if ( line.startsWith("\t") ) {
					ml += " " + line.substring(1);
				} else {
					// process previous line if not empty
					if ( ! ml.equals("") ) {
						insertML(ml, tparams);
					}
					ml = line;
				}
			}
		}
		catch (FileNotFoundException fnfe) {
			System.err.println("Erreur fichier : " + fn + " indisponible");
			System.exit(1);
			return;
		}
		catch(IOException ioe) {
			System.err.println("Erreur sur fichier : " + fn );
			ioe.printStackTrace();
			System.exit(1);
		}
		finally {
			if ( !ml.equals("") )
				insertML(ml, tparams);
			if (reader != null) reader.close();
		}
	}

	void loadText(String fn, TierParams tparams) throws IOException {
		chatFilename = fn;
		String line = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new InputStreamReader(new FileInputStream(fn), inputEncoding) );
			while((line = reader.readLine()) != null) {
				mainLines.add( new MainTier(line, tparams.normalize));
			}
		}
		catch (FileNotFoundException fnfe) {
			System.err.println("Erreur fichier : " + fn + " indisponible");
			System.exit(1);
			return;
		}
		catch(IOException ioe) {
			System.err.println("Erreur sur fichier : " + fn );
			ioe.printStackTrace();
			System.exit(1);
		}
		finally {
			if (reader != null) reader.close();
		}
	}

	void loadSrt(String fn, TierParams tparams) throws IOException {
		chatFilename = fn;
		String line = "";
		String ml = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new InputStreamReader(new FileInputStream(fn), inputEncoding) );
			while((line = reader.readLine()) != null) {
				if (line.isEmpty()) continue;
				// read an srt triplet
				// first number
				int nb;
				int start = -1;
				int end = -1;
				try {
					nb = Integer.parseInt(line);
				} catch (Exception e) {
					nb = 0;
					System.out.printf("Error srt: bad number at %s%n", line);
				}
				if ((line = reader.readLine()) == null) {
					System.out.printf("Error srt: incomplete last element%n", nb);
					break;
				}
				//System.out.printf("srt: %d%n", nb);
				String regex = "([\\d:,]+)\\s+-{2}\\>\\s+([\\d:,]+)";
				Pattern p = Pattern.compile(regex);
				Matcher m = p.matcher(line);
				if (m.find()) {
					String sbegin = m.group(1);
					String send = m.group(2);
					// System.out.printf("found: %s %s%n", m.group(1), m.group(2));
					// START
					regex = "(\\d+):(\\d+):(\\d+),(\\d+)";
					p = Pattern.compile(regex);
					m = p.matcher(sbegin);
					if (m.find()) {
						int h = Integer.parseInt(m.group(1));
						int mn = Integer.parseInt(m.group(2));
						int s = Integer.parseInt(m.group(3));
						int ms = Integer.parseInt(m.group(4));
						start = (h * 3600 + mn * 60 + s) * 1000 + ms;
					} else {
						regex = "(\\d+):(\\d+):(\\d+)";
						p = Pattern.compile(regex);
						m = p.matcher(sbegin);
						if (m.find()) {
							int h = Integer.parseInt(m.group(1));
							int mn = Integer.parseInt(m.group(2));
							int s = Integer.parseInt(m.group(3));
							start = (h * 3600 + mn * 60 + s) * 1000;
						}
					}
					// END
					regex = "(\\d+):(\\d+):(\\d+),(\\d+)";
					p = Pattern.compile(regex);
					m = p.matcher(send);
					if (m.find()) {
						int h = Integer.parseInt(m.group(1));
						int mn = Integer.parseInt(m.group(2));
						int s = Integer.parseInt(m.group(3));
						int ms = Integer.parseInt(m.group(4));
						end = (h * 3600 + mn * 60 + s) * 1000 + ms;
					} else {
						regex = "(\\d+):(\\d+):(\\d+)";
						p = Pattern.compile(regex);
						m = p.matcher(send);
						if (m.find()) {
							int h = Integer.parseInt(m.group(1));
							int mn = Integer.parseInt(m.group(2));
							int s = Integer.parseInt(m.group(3));
							end = (h * 3600 + mn * 60 + s) * 1000;
						}
					}
				} else {
					System.out.printf("srt: cannot process: %s%n", line);
				}
				String content = "*SRT";
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) break;
					content += " " + line.trim();
				}
				// System.out.printf("content: %d %d %d %s%n", nb, start, end, content);
				mainLines.add( new MainTier(content, start, end));
			}
		}
		catch (FileNotFoundException fnfe) {
			System.err.println("Erreur fichier : " + fn + " indisponible");
			System.exit(1);
			return;
		}
		catch(IOException ioe) {
			System.err.println("Erreur sur fichier : " + fn );
			ioe.printStackTrace();
			System.exit(1);
		}
		finally {
			if ( !ml.equals("") )
				insertML(ml, tparams);
			if (reader != null) reader.close();
		}
	}

	public int nbMainLines() {
		return mainLines.size();
	}

	public String ml(int n) {
		return mainLines.get(n).ml();
	}

	public String mlc(int n) {
		return mainLines.get(n).mlc();
	}

	public int startMl(int n) {
		return mainLines.get(n).start();
	}

	public int endMl(int n) {
		return mainLines.get(n).end();
	}

  public void majtime(int n, int ts, int te) {
		mainLines.get(n).majtime(ts, te);
	}

	public int nbTiers(int n) {
		if ( mainLines.get(n).tiers == null ) return 0;
		return mainLines.get(n).tiers.size();
	}

	public String t(int n, int t) {
		return mainLines.get(n).t(t);
	}

	public String filename() {
		return chatFilename;
	}

	public void dumpHeader() {
		System.out.println( "Filename : " + filename() );
		System.out.println( "Nb Lines : " + nbMainLines() );
		int nbids = ids.size();
		System.out.println( "Nb IDs : " + nbids );
		System.out.println("chat_filename : " + chatFilename );
		System.out.println("media_filename : " + mediaFilename );
		System.out.println("media_type : " + mediaType );
		System.out.println("birth : " + birth );
		System.out.println("date : " + date );
		System.out.println("location : " + location );
		System.out.println("situation : "  + situation );
		System.out.println("transcriber : " + transcriber );
		System.out.println("language : " + lang );
		for (String com:comments){
			System.out.println("com :  " + com);
		}

		for (String info:otherInfo){
			System.out.println("info :  " + info);

		}
		for (ID id: ids) {
			System.out.println("NAME : " + id.name );
			System.out.println("ID-language : " + id.language );
			System.out.println("ID-corpus : " + id.corpus );
			System.out.println("ID-code : " + id.code );
			System.out.println("ID-age : " + id.age );
			System.out.println("ID-sex : " + id.sex );
			System.out.println("ID-group : " + id.group );
			System.out.println("ID-SES : " + id.SES );
			System.out.println("ID-role : " + id.role );
			System.out.println("ID-education : " + id.education );
			System.out.println("ID-customfield : " + id.customfield );
		}
	}

	public void dump() {
		dumpHeader();

		int sz = nbMainLines();

		for (int i=0; i<sz; i++) {
			System.out.println( i + ": (" + startMl(i) + ") (" + endMl(i) + ") " + ml(i) );
			System.out.println( i + ": (" + startMl(i) + ") (" + endMl(i) + ") " + mlc(i) );
			int tsz = nbTiers(i);

			for (int j=0; j<tsz; j++) {
				System.out.println( j + "- " + t(i,j) );
			}
		}
	}

	public void cleantime_inmemory(int style) {
		int last_te = -1;
		int last_ts = -1;
		int last_i = -1;
		int missing = 1;
		int i;
		int first=0;

		for (i=0; i < nbMainLines(); i++) {
			String tp = ml(i);
			if (tp.startsWith("*")) {
				if ( missing == 1 ) {
					first = i;
					missing = 0;
				}
				if ( endMl(i) > 0 ) {
					break;
				}
			}
		}
		// i tells where there is the first bullet and first tells where there is the first line where there should be a bullet.
		if ( i == nbMainLines() ) {
			// no bullet at all - note that it could be possible to spread the time across ALL the transcription
			System.out.println("Warning: no bullet in file - impossible to cleantime");
			return;
		}

		// if the first bullet does not correspond to the first line, then we move that bullet to the first line.
		if ( i != first ) {
			int ts = startMl(i);
			int te = endMl(i);
			majtime(i, -1, -1);
			majtime(first, ts, te);
		}

		missing = 0; // count how many missing lines between two bullets.
		for (i=0; i < nbMainLines(); i++) {
			String tp = ml(i);
			if (tp.startsWith("*")) {
				int ts = startMl(i);
				int te = endMl(i);
//				System.out.println("found bullet at " + i + " " + ts + " " + te + " missing " + missing);
				if (ts != -1) { // this means that there is a legal bullet on that line
					if (missing > 0 ) { // it is necessary to propagate
						// propagate time after bullet
						if (style == 0) { // equal length between all lines
							int d = (last_te-last_ts)/(missing+1);
							int k = 0;
							for (; k < missing; k++) {
								int new_ts = last_ts + k*d;
							  int new_te = last_ts + k*d + d;
								// upgrade last_i with new_ts and new_te
								majtime(last_i, new_ts, new_te);
								// find new line to be bulleted
								String tstop;
								do {
									last_i++;
									tstop = ml(last_i);
								} while ( !tstop.startsWith("*") );
							}
							// propagate to last
							int new_ts = last_ts + k*d;
							int new_te = last_te ;
							// upgrade last_i with new_ts and new_te
							majtime(last_i, new_ts, new_te);
						} else {
							// compute the length in words of the line to be bulleted
							int k;
							int li = last_i;
							int total_nw = 0;
							int nw[] = new int[missing+1];
							for (k=0; k <= missing; k++) {
								// count number of words
								String w[] = mlc(li).split("\\s+");
								nw[k] = w.length;
								total_nw += nw[k];
								// find new line to be bulleted
								String tstop;
								do {
									li++;
									tstop = ml(li);
								} while ( !tstop.startsWith("*") );
							}
							// compute the time increment for each word
							int d = (last_te-last_ts)/(total_nw+1);
							int decal = 0; // compute current new bullet time
							// bullet the lines according to their number of words
							for (k=0; k < missing; k++) {
								// compute length in milliseconds
								int lg = nw[k] * d ;
								int new_ts = last_ts + decal;
								int new_te = last_ts + decal+ lg;
								decal += lg;
								// upgrade last_i with new_ts and new_te
								majtime(last_i, new_ts, new_te);
								// find new line to be bulleted
								String tstop;
								do {
									last_i++;
									tstop = ml(last_i);
								} while ( !tstop.startsWith("*") );
							}
							// propagate to last
							int new_ts = last_ts + decal;
							int new_te = last_te ;
							// upgrade last_i with new_ts and new_te
							majtime(last_i, new_ts, new_te);
						}
					}
					// store last line with a bullet
					last_ts = ts;
					last_te = te;
					last_i = i;
					missing = 0;
				} else {
					// count how many lines have no bullet
					missing++;
				}
			}
		}

		if (missing > 0 ) {
			// propagate time after the last bullet of the file
			int d = (last_te-last_ts)/(missing+1); // equal time between all new bullets.
			int k = 0;
			for (; k < missing; k++) {
				int new_ts = last_ts + k*d;
				int new_te = last_ts + k*d + d;
				// upgrade last_i with new_ts and new_te
				majtime(last_i, new_ts, new_te);
				last_i++;
			}
			// propagate to very last line
			int new_ts = last_ts + k*d;
			int new_te = last_te ;
			// upgrade last_i with new_ts and new_te
			majtime(last_i, new_ts, new_te);
		}
	}

	public void init(String fn) throws Exception {
		/**
		 * lit le contenu d'un fichier.
		 * lit et décompose les entetes
		 * exception en cas de fichier absent ou incorrect.
		 * @param fn fichier Chat à lire
		 */
		TierParams tp = new TierParams();
		load(fn, tp);
		findInfo(false, tp);
		cleantime_inmemory(1);
	}

	public static void main(String[] args) throws Exception {
		TierParams tp = new TierParams();
		ChatFile cf = new ChatFile();
		cf.load(args[0], tp);
		cf.findInfo(false, tp);
		cf.dump();
	}
};
