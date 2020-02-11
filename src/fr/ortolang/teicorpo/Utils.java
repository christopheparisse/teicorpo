package fr.ortolang.teicorpo;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class Utils {
	
	public static final int styleLexicoTxm = 2;
	public static String EXT = ".tei_corpo.xml";
	public static String EXT_PUBLISH = ".tei_corpo";
	public static boolean teiStylePure = false;
	
	public static String shortPause = " # ";
	public static String longPause = " ## ";
	public static String veryLongPause = " ### ";
	public static String specificPause = "#%s";
	public static String shortPauseCha = " (.) ";
	public static String longPauseCha = " (..) ";
	public static String veryLongPauseCha = " (...) ";
	public static String specificPauseCha = "(%s)";

	public static boolean isNotEmptyOrNull(String s){
		return s!= null && !s.isEmpty() && s != "";
	}

	public static String cleanString(String s){
		return s.trim().replaceAll(" {2,}", " ").replaceAll("\n", "").trim();
	}

	public static String justSpaces(String s) {
		if (s == null) return "";
		return s.replaceAll("\\s+", " ").trim();
	}

	public static String cleanStringPlusEntities(String s) {
		return s.replaceAll(" {2,}", " ")
				.replaceAll("\n", "").trim()
				.replaceAll("&quot;", "\"") // 34 22
				.replaceAll("&amp;", "&") // 38 26
				.replaceAll("&#39;", "\'") // 39 27
				.replaceAll("&lt;", "<") // 60 3C
				.replaceAll("&gt;", ">"); // 62 3E
	}

	public static String cleanEntities(String s) {
		return s.replaceAll("&quot;", "\"") // 34 22
				.replaceAll("&amp;", "&") // 38 26
				.replaceAll("&#39;", "'") // 39 27
				.replaceAll("&lt;", "<") // 60 3C
				.replaceAll("&gt;", ">"); // 62 3E
	}

	public static String setEntities(String s) {
		return s.replaceAll("\"", "&quot;") // 34 22
				.replaceAll("&", "&amp;") // 38 26
				.replaceAll("'", "&#39;") // 39 27
				.replaceAll("<", "&lt;") // 60 3C
				.replaceAll(">", "&gt;"); // 62 3E
	}

	public static String join(String... args) {
		String result = "";
		for(String st : args){
			result += st + " ";
		}
		return result;
	}

	public static String join2(String... args) {
		String result = "";
		for(String st : args){
			result += st + "_";
		}
		return result;
	}

	public static String joinString(String[] stringSplit, int begin, int end) {
		String sentence = "";
		for (int i = begin; i < end; i++) {
			sentence += stringSplit[i] + " ";
		}
		return sentence;
	}

	public static String join(String[] s, String delimiter) {
		StringBuffer buffer = new StringBuffer();
		int i = 0;
		while (i < s.length-1) {
			buffer.append(s[i]);
			buffer.append(delimiter);
			i++;
		}
		buffer.append(s[i]);
		return buffer.toString();
	}
	
	public static String getInfo2(String line){
		if (isNotEmptyOrNull(line)){
			try{
				String [] tab = line.split("] ");
				String info = "";
				for (int i = 1; i< tab.length; i++){
					info += tab[i] + " ";
				}
				return info.substring(0, info.length()-1);
			}
			catch(StringIndexOutOfBoundsException e){}
		}
		return "";
	}

	public static String getInfo(String line){
		if (isNotEmptyOrNull(line)){
			try{
				String [] tab = line.split("\t");
				String info = "";
				for (int i = 1; i< tab.length; i++){
					info += tab[i] + " ";
				}
				return info.substring(0, info.length()-1);
			}
			catch(StringIndexOutOfBoundsException e){}
		}
		return "";
	}

	public static String getInfoType(String line){
		try{
			return line.split("\t")[0].split(":")[0];
		}
		catch(Exception e){
			return "";
		}
	}

	public static int toHours(float t) {
		t = t / 60;
		t = t / 60;
		return (int)t;
	}

	public static int toMinutes(float t) {
		t = t / 60;
		return (int)(t % 60);
	}

	public static int toXMinutes(float t) {
		t = t / 60;
		return (int)(t);
	}

	public static int toSeconds(float t) {
		return (int)(t % 60);
	}

	public static int toMilliSeconds(float t) {
		return (int)(t * 1000);
	}

	public static int toCentiSeconds(float t) {
		return (int)(t/10);
	}

	public static String printDouble(double value, int precision) {
		if (value <= 0.0) return "0";
		double intpart = Math.floor(value);
	    BigDecimal bd = new BigDecimal(value);
	    BigDecimal bdintpart = new BigDecimal(intpart);
	    bd = bd.setScale(precision, RoundingMode.HALF_UP);
	    BigDecimal bdb = bd.subtract(bdintpart);
	    // System.out.println(bd + " " + bdb);
	    if (bdb.compareTo(new BigDecimal("0E-15")) <= 0)
		    return bdintpart.toString() + ".0";
	    bdb = bdb.setScale(precision, RoundingMode.HALF_UP);
	    return bdintpart.toString() + "." + bdb.toString().substring(2);
	}

	public static String fullbasename(String fileName) {
		int p = fileName.lastIndexOf('.');
		if (p >= 0)
			return fileName.substring(0, p);
		else
			return fileName;
	}

	public static String fullbasename(File file) {
		String fileName = file.toString();
		return fullbasename(fileName);
	}

	public static String lastname(String fn) {
		int p = fn.lastIndexOf(File.separatorChar);
		if (p<0) return fn;
		return fn.substring(p+1);
	}

	public static String pathname(String fn) {
		int p = fn.lastIndexOf(File.separatorChar);
		if (p<0) return "";
		return fn.substring(0, p);
	}

	public static String basename(String filename) {
		String bn = lastname(filename);
		int p = bn.lastIndexOf('.');
		if (p >= 0)
			return bn.substring(0, p);
		else
			return bn;
	}

	public static String basename(File file) {
		String filename = file.toString();
		return basename(filename);
	}

	public static String extname(String fn) {
		int p = fn.lastIndexOf('.');
		if (p >= 0)
			return fn.substring(p);
		else
			return "";
	}

	public static boolean validFileFormat(String fileName, String extension) {
		return fileName.toLowerCase().endsWith(extension);
	}

	public static String convertMonthStringToInt(String monthString){
		if(monthString.toLowerCase().equals("jan")){
			return "01";
		}
		else if(monthString.toLowerCase().equals("feb") || monthString.toLowerCase().equals("fev")){
			return "02";
		}
		else if(monthString.toLowerCase().equals("mar")){
			return "03";
		}
		else if(monthString.toLowerCase().equals("apr")){
			return "04";
		}
		else if(monthString.toLowerCase().equals("may")){
			return "05";
		}
		else if(monthString.toLowerCase().equals("jun")){
			return "06";
		}
		else if(monthString.toLowerCase().equals("jul")){
			return "07";
		}
		else if(monthString.toLowerCase().equals("aug")){
			return "08";
		}
		else if(monthString.toLowerCase().equals("sep")){
			return "09";
		}
		else if(monthString.toLowerCase().equals("oct")){
			return "10";
		}
		else if(monthString.toLowerCase().equals("nov")){
			return "11";
		}
		else if(monthString.toLowerCase().equals("dec")){
			return "12";
		}
		return monthString;
	}

	public static void setMimeType(Element media, String mediaName){
		//Video
		if(mediaName.endsWith(".mp4")){
			media.setAttribute("mimeType", "video/mp4");
		}
		else if(mediaName.endsWith(".webm")){
			media.setAttribute("mimeType", "video/webm");
		}
		else if(mediaName.endsWith(".ogv")){
			media.setAttribute("mimeType", "video/ogg");
		}
		//Audio
		else if(mediaName.endsWith(".mp3")){
			media.setAttribute("mimeType", "audio/mpeg");
		}
		else if(mediaName.endsWith(".ogg")){
			media.setAttribute("mimeType", "audio/ogg");
		}
		else if(mediaName.endsWith(".wav")){
			media.setAttribute("mimeType", "audio/wav");
		}
	}
	
	public static void sortTimeline(ArrayList<Element> timeline){
		CompareTimeline ct = new CompareTimeline();		
		Collections.sort(timeline, ct);
	}

	public static String findClosestMedia(String dir, String fn, String type) {
		// TODO Auto-generated method stub
		String[] extsVideo = { "-480p.mp4",".mp4","-720p.mp4","-240p.mp4",
				"-480p.webm",".webm","-720p.webm","-240p.webm",
				".mov",".mpg",".avi",".fv", "ogv" };
		String[] extsAudio = { ".aif", ".wav", ".mp3" };
		int p = fn.lastIndexOf(".");
		if (p >= 0) {
			String ext = fn.substring(p);
			if (java.util.Arrays.asList(extsVideo).indexOf(ext) >= 0)
				return fn;
			if (java.util.Arrays.asList(extsAudio).indexOf(ext) >= 0)
				return fn;
		}
		if (Utils.isNotEmptyOrNull(dir))
			fn = dir + File.separator + fn;
		String cleanFn = fn;
		try {
			File ffn = new File(fn);
			cleanFn = ffn.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			cleanFn = fn;
		}
		p = cleanFn.lastIndexOf(".");
		String fnbase = (p >= 0) ? cleanFn.substring(0,p) : cleanFn;
		if (type.indexOf("video") >= 0) {
			for (int i=0; i < extsVideo.length; i++) {
				File file = new File(fnbase + extsVideo[i]);
				if (file.exists())
					return file.getName();
			}
		}
		if (type.indexOf("audio") >= 0) {
			for (int i=0; i < extsAudio.length; i++) {
				File file = new File(fnbase + extsAudio[i]);
				if (file.exists())
					return file.getName();
			}
		}
		if (type.indexOf("video") >= 0)
			return Utils.basename(fn) + ".mp4";
		if (type.indexOf("audio") >= 0)
			return Utils.basename(fn) + ".wav";
		else
			return Utils.basename(fn);
	}
	
	public static String findMimeType(String fn) {
		String ext = "";
		int p = fn.lastIndexOf(".");
		if (p >= 0) {
			ext = fn.substring(p);
		}
		if (ext.isEmpty()) {
			return "unknown";
		} else {
			if (ext.equals(".wav"))
				return "audio/x-wav";
			else if (ext.equals(".mp3"))
				return "audio/mp3";
			else if (ext.equals(".m4a"))
				return "audio/m4a";
			else if (ext.equals(".mov"))
				return "video/quicktime";
			else if (ext.equals(".mp4"))
				return "video/mp4";
			else if (ext.equals(".mpg"))
				return "video/mpg";
			else if (ext.equals(".webm"))
				return "video/webm";
			else
				return "unknown";
		}
	}

	public static String refID(String refid) {
		if (refid.startsWith("#"))
			return refid.substring(1);
		return refid;
	}

	/*
	public static void main(String[] args) {
        final String xmlStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+
                                "<Emp id=\"1\"><name>Pankaj</name><age>25</age>\n"+
                                "<role>Developer</role><gen>Male</gen></Emp>";
        Document doc = convertStringToDocument(args.length<1 ? xmlStr : args[0]);
         
        String str = convertDocumentToString(doc, false);
        System.out.println(str);
    }
    */
 
    public static String convertDocumentToString(Document doc, boolean withHeader) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            // below code to remove XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            if (withHeader)
            	return output;
            else
            	return output.replaceFirst("<.*?>", "");
        } catch (TransformerException e) {
            e.printStackTrace();
        }
         
        return null;
    }

    public static Document convertStringToDocument(String xmlStr) {
        try
        {

			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlStr));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc = db.parse(is);
            return doc;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
    }

	public static Element convertStringToElement(String xmlStr) {
//    	System.out.printf("CVT: %s%n", xmlStr);
    	Document doc = convertStringToDocument(xmlStr);
//		System.out.printf("%d%n", doc.getDocumentElement().getChildNodes().getLength());
    	return (doc != null) ? doc.getDocumentElement() : null;
	}

	public static void createFile(Document domDoc, String outputFileName) {
		Source source = new DOMSource(domDoc);
		Result resultat = new StreamResult(outputFileName);
		try {
			// Configuration du transformer
			TransformerFactory fabrique2 = TransformerFactory.newInstance();
			Transformer transformer = fabrique2.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			// Transformation
			transformer.transform(source, resultat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    public static List<String> loadTextFile(String filename) throws IOException {
		List<String> ls = new ArrayList<String>();
		String line = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new InputStreamReader(new FileInputStream(filename)) );
			while((line = reader.readLine()) != null) {
				// Traitement du flux de sortie de l'application si besoin est
				ls.add(line.trim());
			}
		}
		catch (FileNotFoundException fnfe) {
			System.err.println("Erreur fichier : " + filename + " indisponible");
			System.exit(1);
			return null;
		}
		catch(IOException ioe) {
			System.err.println("Erreur sur fichier : " + filename );
			ioe.printStackTrace();
			System.exit(1);
			return null;
		}
		finally {
			if (reader != null) reader.close();
		}
		return ls;
	}

	public static String normaliseAge(String age) {
		try {
			// case y;m.d
			Pattern pp = Pattern.compile("(\\d+);(\\d+)\\.(\\d+)");
			Matcher mm = pp.matcher(age);
			boolean bb = mm.matches();
			if (bb) {
				double y = Double.parseDouble(mm.group(1));
				double m = Double.parseDouble(mm.group(2));
				double d = Double.parseDouble(mm.group(3));
				return Double.toString(Math.floor((y + (m*30.5 + d)/365.0) * 10) / 10);
			}
			// case y;m
			pp = Pattern.compile("(\\d+);(\\d+)");
			mm = pp.matcher(age);
			bb = mm.matches();
			if (bb) {
				double y = Double.parseDouble(mm.group(1));
				double m = Double.parseDouble(mm.group(2));
				return Double.toString(Math.floor((y + (m*30.5)/365.0) * 10) / 10);
			}
			// case y;m.
			pp = Pattern.compile("(\\d+);(\\d+)\\.");
			mm = pp.matcher(age);
			bb = mm.matches();
			if (bb) {
				double y = Double.parseDouble(mm.group(1));
				double m = Double.parseDouble(mm.group(2));
				return Double.toString(Math.floor((y + (m*30.5)/365.0) * 10) / 10);
			}
			// case y;
			pp = Pattern.compile("(\\d+);");
			mm = pp.matcher(age);
			bb = mm.matches();
			if (bb) {
				double y = Double.parseDouble(mm.group(1));
				return Double.toString(Math.floor(y * 10) / 10);
			}
			double d = Double.parseDouble(age);
			if (d < 0.0 || d > 120.0) {
				System.err.println("age hors limites: (" + age + ")");
				return "40.1";
			}
		} catch (Exception e) {
			System.err.println("age anormal: " + age);
			return "40.1";
		}
		return age;
	}

	public static String normaliseActivity(String a) {
		String [] s = {"jeu", "discours", "leçon", "interaction", "entretien", "interview", "enfantadulte"};
		if (java.util.Arrays.asList(s).indexOf(a) >= 0)
			return a;
		System.err.printf("activité inconnue: %s%n", a);
		return null;
	}

	public static boolean testAndCreateDir(String dirname) {
		File outFile = new File(dirname);
		if (outFile.exists()) {
			if (!outFile.isDirectory()) {
				System.out.println("\n Erreur :"+ dirname + " est un fichier, vous devez spécifier un nom de dossier pour le stockage des résultats. \n");
				return false;
			} else {
				new File(dirname).mkdir();
			}
		}
		return true;
	}

    public static PrintWriter openOutputStream(String outputName, boolean concat, String outputEncoding) {
		try {
			FileOutputStream of = new FileOutputStream(outputName, concat);
			OutputStreamWriter outWriter = new OutputStreamWriter(of, outputEncoding);
			return new PrintWriter(outWriter, true);
		} catch (Exception e) {
			return new PrintWriter(System.out, true);
		}
    }
}
