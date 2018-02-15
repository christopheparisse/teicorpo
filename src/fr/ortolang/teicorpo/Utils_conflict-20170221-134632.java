package fr.ortolang.teicorpo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Utils {
	
	public static final int styleLexicoTxm = 2;
	public static String EXT = ".tei_corpo.xml";
	public static String EXT_PUBLISH = ".tei_corpo";
	public static String ANNOTATIONBLOC = "annotationBlock";
	public static String versionTEI = "0.9";
	public static String versionSoft = "1.28"; // full version with Elan, Clan, Transcriber and Praat
	public static String versionDate = "03/03/2017 18:00";
	public static String TEI_ALL = "http://ct3.ortolang.fr/tei-corpo/tei_all.dtd";
	public static String TEI_CORPO_DTD = "http://ct3.ortolang.fr/tei-corpo/tei_corpo.dtd";
	public static boolean teiStylePure = false;
	
	public static String shortPause = " # ";
	public static String longPause = " ## ";
	public static String veryLongPause = " ### ";
	public static String specificPause = "#%s";
	public static String shortPauseCha = " (.) ";
	public static String longPauseCha = " (..) ";
	public static String veryLongPauseCha = " (...) ";
	public static String specificPauseCha = "(%s)";
	
	public static String leftBracket = "⟪"; // 27EA - "❮"; // "⟨" 27E8 - "❬" 
	public static String rightBracket = "⟫"; // 27EB - "❯"; // "⟩" 27E9 - "❭" - 276C à 2771 ❬ ❭ ❮ ❯ ❰ ❱ 
	public static String leftEvent = "⟦"; // 27E6 - "『"; // 300E - "⌈"; // u2308 
	public static String rightEvent = "⟧"; // 27E7 - "』"; // 300F - "⌋"; // u230b
	public static String leftParent = "⁅"; // 2045 // "⁘"; // 2058 // "⁑" // 2051
	public static String rightParent = "⁆"; // 2046 // "⁘"; // 2058
	public static String leftCode = "⌜"; // 231C - "⁌"; // 204C
	public static String rightCode = "⌟"; // 231F - "⁍"; // 204D

	public static String teiCorpoDtd() {
		// return teiStylePure == true ? TEI_ALL : TEI_CORPO_DTD;
		return TEI_ALL;
	}

	public static boolean isElement(Node n){
		return n.getNodeType() == Node.ELEMENT_NODE;
	}

	public static boolean isText(Node n){
		return n.getNodeType() == Node.TEXT_NODE;
	}

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

	public static String cleanStringPlusEntities(String s){
		return s.replaceAll(" {2,}", " ")
				.replaceAll("\n", "").trim()
				.replaceAll("&quot;", "\"") // 34 22
				.replaceAll("&amp;", "&") // 38 26
				.replaceAll("&#39;", "\'") // 39 27
				.replaceAll("&lt;", "<") // 60 3C
				.replaceAll("&gt;", ">"); // 62 3E
	}

	public static String cleanEntities(String s){
		return s.replaceAll("&quot;", "\"") // 34 22
				.replaceAll("&amp;", "&") // 38 26
				.replaceAll("&#39;", "\'") // 39 27
				.replaceAll("&lt;", "<") // 60 3C
				.replaceAll("&gt;", ">"); // 62 3E
	}

	public static String join(String... args) {
		String result = "";
		for(String st : args){
			result += st + " ";
		}
		return result;
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
	
	/**
	 * Création du fichier TEI à partir de la transcription originale. 
	 * @param outputFileName
	 *            Nom du fichier TEI à créer.
	 */
	public static void createFile(String outputFileName, Document d) {

		Source source = new DOMSource(d);

		Result resultat = new StreamResult(outputFileName);

		try {
			// Configuration du transformer
			TransformerFactory fabrique2 = TransformerFactory.newInstance();
			Transformer transformer = fabrique2.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, Utils.teiCorpoDtd());

			// Transformation
			transformer.transform(source, resultat);
			// System.out.println("Fichier TEI créé : " + outputFileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setDTDvalidation(DocumentBuilderFactory factory,	boolean b) {
		try {
			factory.setValidating(b);
			factory.setFeature("http://xml.org/sax/features/namespaces", b);
			factory.setFeature("http://xml.org/sax/features/validation", b);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",	b);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", b);
		}
		catch (Exception e) {
			System.err.println("Votre fichier n'est pas conforme à la DTD passée en argument");
			e.printStackTrace();
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

	public static Element createDivHead(Document docTEI) {
		Element div = docTEI.createElement("div");
		Element head = docTEI.createElement("head");
		div.appendChild(head);
		return div;
	}

	public static void setDivHeadAttr(Document docTEI, Element div, String type, String value) {
		NodeList head = div.getElementsByTagName("head");
		if (head.getLength() == 0) {
			System.err.println("Div should contain Head");
			try {
				throw new Exception();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Element note = docTEI.createElement("note");
		note.setAttribute("type", type);
		note.setTextContent(value);
		head.item(0).appendChild(note);
	}

	public static String getDivHeadAttr(Element annotU, String attrName) {
		String a = annotU.getAttribute(attrName);
		if (a == null || a.isEmpty())
			return getHeadAttr(annotU, attrName);
		return a;
	}

	public static String getHeadAttr(Element annotU, String attrName) {
		NodeList nl = annotU.getElementsByTagName("head");
		if (nl == null) return "";
		Element head = (Element)nl.item(0);
		if (head == null) return "";
		NodeList notes = head.getElementsByTagName("note");
		for (int i=0; i < notes.getLength(); i++) {
			Element e = (Element)(notes.item(i));
			String a = e.getAttribute("type");
			if (a.equals(attrName))
				return e.getTextContent();
		}
		return "";
	}

	public static Element createAnnotationBloc(Document docTEI) {
		Element bloc;
		if (Utils.teiStylePure == true) {
			// simulate non existing TEI element with actual existing elements
			bloc = docTEI.createElement("div");
			bloc.setAttribute("type", Utils.ANNOTATIONBLOC);
			Element head = docTEI.createElement("head");
			bloc.appendChild(head);
		} else {
			// use extensions of TEI for Oral
			bloc = docTEI.createElement(Utils.ANNOTATIONBLOC);
		}
		return bloc;
	}

	public static void setAttrAnnotationBloc(Document docTEI, Element bloc, String type, String value) {
		if (Utils.teiStylePure == true) {
			setDivHeadAttr(docTEI, bloc, type, value);
		} else {
			bloc.setAttribute(type, value);
		}
	}

	public static NodeList getAllDivs(XPath xpath, Document docTEI) throws XPathExpressionException {
		NodeList nl;
		if (Utils.teiStylePure == true) {
	        XPathExpression expr = xpath.compile("//div[not(@type=\"" + Utils.ANNOTATIONBLOC + "\")]");
	        nl = (NodeList) expr.evaluate(docTEI, XPathConstants.NODESET);
		} else {
			nl = docTEI.getElementsByTagName("div");
		}
		return nl;
	}

	public static NodeList getAllAnnotationBloc(XPath xpath, Document docTEI) throws XPathExpressionException {
		NodeList nl;
		if (Utils.teiStylePure == true) {
	        XPathExpression expr = xpath.compile("//div[@type=\"" + Utils.ANNOTATIONBLOC + "\"]");
	        nl = (NodeList) expr.evaluate(docTEI, XPathConstants.NODESET);
		} else {
			nl = docTEI.getElementsByTagName(Utils.ANNOTATIONBLOC);
		}
		return nl;
	}

	public static NodeList getSomeAnnotationBloc(XPath xpath, Element eltTop) throws XPathExpressionException {
		NodeList nl;
		if (Utils.teiStylePure == true) {
			NodeList childs = eltTop.getChildNodes();
			return childs;
//			for (int i=0; i < childs.getLength(); i++) {
//				if (((Element)childs.item(i)).getAttribute("type") == Utils.ANNOTATIONBLOC) nl.add(childs.item(i));
//			}
//			return nl;
//	        XPathExpression expr = xpath.compile("//div[@type=\"" + Utils.ANNOTATIONBLOC + "\"]");
//	        nl = (NodeList) expr.evaluate(eltTop, XPathConstants.NODESET);
		} else {
			nl = eltTop.getElementsByTagName(Utils.ANNOTATIONBLOC);
		}
		return nl;
	}

	public static String getAttrAnnotationBloc(Element annotU, String attrName) {
		if (Utils.teiStylePure == true) {
			return getHeadAttr(annotU, attrName);
		} else {
			return annotU.getAttribute(attrName);
		}
	}

	public static boolean isAnnotatedBloc(Element el) {
		if (Utils.teiStylePure == true) {
			return (el.getAttribute("type").equals(Utils.ANNOTATIONBLOC)) ? true : false;
		} else {
			return (el.getNodeName().equals(Utils.ANNOTATIONBLOC)) ? true : false;
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
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try 
        {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
            return doc;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
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

	public static void setAttrAnnotationBlocSupplement(Document docTEI, Element annotatedU, String string,
			String attValue) {
		NodeList spanGrpList = annotatedU.getElementsByTagName("spanGrp");
		if (spanGrpList != null && spanGrpList.getLength() > 0) {
			for (int i=0; i<spanGrpList.getLength(); i++) {
				Element spanGrp = (Element) spanGrpList.item(i);
				if (spanGrp.getAttribute("type").equals("TurnInformation")) {
					NodeList spanList = spanGrp.getElementsByTagName("span");
					if (spanList != null && spanList.getLength() > 0) {
						for (int j=0; j<spanList.getLength(); j++) {
							Element span = (Element) spanList.item(j);
							if (span.getAttribute("type").equals(string)) {
								span.setAttribute("type", string);
								return;
							}
						}
					}
					// si pas trouvé
					// ou si pas de span dans le spanGrp TurnInformation
					Element s = docTEI.createElement("span");
					s.setAttribute("type", string);
					s.setTextContent(attValue);
					spanGrp.appendChild(s);
					return;
				}
			}
		}
		// si pas de spanGrp s'appelent TurnInformation
		// ou si pas encore de spanGrp
		Element sg = docTEI.createElement("spanGrp");
		sg.setAttribute("type", "TurnInformation");
		Element s = docTEI.createElement("span");
		s.setAttribute("type", string);
		s.setTextContent(attValue);
		sg.appendChild(s);
		annotatedU.appendChild(sg);
	}
	
	public static void setDocumentName(Document docTEI, String name) {
		NodeList revDesc = docTEI.getElementsByTagName("revisionDesc");
		NodeList list = ((Element)revDesc.item(0)).getElementsByTagName("list");
		Element item = docTEI.createElement("item");
		Element desc = docTEI.createElement("desc");
		item.setTextContent(name);
		desc.setTextContent("docname");
		item.appendChild(desc);
		for (int i=0; i < list.getLength(); i++) {
			NodeList d = ((Element)list.item(i)).getElementsByTagName("desc");
			if (d != null && d.getLength() > 0) {
				if (((Element)d.item(0)).getTextContent().equals("docname")) {
					((Element)d.item(0)).setTextContent(name);
					return;
				}
			}
		}
		// if not done
		((Element)list.item(0)).appendChild(item);
	}
	
	public static void setTranscriptionDesc(Document docTEI, String id, String version, String desc) {
		NodeList trDesc = docTEI.getElementsByTagName("transcriptionDesc");
		Element item;
		if (trDesc.getLength() < 1) {
			NodeList encDesc = docTEI.getElementsByTagName("encodingDesc");
			if (encDesc.getLength() < 1) {
				System.err.println("manque encoding desc: information non ajoutée");
				return;
			}
			item = docTEI.createElement("transcriptionDesc");
			((Element)encDesc.item(0)).appendChild(item);
		} else {
			item = ((Element)trDesc.item(0));
		}
		if (!id.isEmpty()) {
			item.setAttribute("ident", id);
		}
		if (!version.isEmpty()) {
			item.setAttribute("version", version);
		}
		if (!desc.isEmpty()) {
			Element d;
			NodeList list = item.getElementsByTagName("desc");
			if (list.getLength() < 1) {
				d = docTEI.createElement("desc");
				item.appendChild(d);
			} else {
				d = ((Element)list.item(0));
			}
			d.setTextContent(desc);
		}
	}

	public static void setRevisionInfo(Document docTEI, Element revisionDesc, String input, String output) {
		if (revisionDesc ==  null) {
			NodeList revDesc = docTEI.getElementsByTagName("revisionDesc");
			if (revDesc == null || revDesc.getLength() < 1) {
				System.err.println("cannot set revisionDesc");
				return;
			}
			revisionDesc = ((Element)revDesc.item(0));
		}
		NodeList nlist = revisionDesc.getElementsByTagName("list");
		Element list;
		if (nlist == null || nlist.getLength() < 1) {
			list = docTEI.createElement("list");
			revisionDesc.appendChild(list);
		} else
			list = ((Element)nlist.item(0));

		Element item = docTEI.createElement("item");
		Element desc = docTEI.createElement("desc");
		item.setTextContent(
				new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(Calendar.getInstance().getTime()));
		desc.setTextContent("date");
		list.appendChild(item);
		item.appendChild(desc);

		if (input != null) {
			item = docTEI.createElement("item");
			desc = docTEI.createElement("desc");
			item.setTextContent(input);
			desc.setTextContent("from");
			list.appendChild(item);
			item.appendChild(desc);
		}

		if (output != null || input != null) {
			item = docTEI.createElement("item");
			desc = docTEI.createElement("desc");
			if (output != null)
				item.setTextContent(output);
			else
				item.setTextContent(Utils.fullbasename(input) + Utils.EXT);
			desc.setTextContent("to");
			list.appendChild(item);
			item.appendChild(desc);
		}
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
				return Double.toString(y + (m*30.5 + d)/365.0);
			}
			// case y;m
			pp = Pattern.compile("(\\d+);(\\d+)");
			mm = pp.matcher(age);
			bb = mm.matches();
			if (bb) {
				double y = Double.parseDouble(mm.group(1));
				double m = Double.parseDouble(mm.group(2));
				return Double.toString(y + (m*30.5)/365.0);
			}
			// case y;m.
			pp = Pattern.compile("(\\d+);(\\d+)\\.");
			mm = pp.matcher(age);
			bb = mm.matches();
			if (bb) {
				double y = Double.parseDouble(mm.group(1));
				double m = Double.parseDouble(mm.group(2));
				return Double.toString(y + (m*30.5)/365.0);
			}
			// case y;
			pp = Pattern.compile("(\\d+);");
			mm = pp.matcher(age);
			bb = mm.matches();
			if (bb) {
				double y = Double.parseDouble(mm.group(1));
				return Double.toString(y);
			}
			double d = Double.parseDouble(age);
			if (d < 0.0 || d > 120.0) {
				System.err.println("age hors limites: (" + age + ")");
				return "40.02";
			}
		} catch (Exception e) {
			System.err.println("age anormal: " + age);
			return "40.03";
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

	static String getText(Element e) {
		String s = "";
		NodeList nle = e.getChildNodes();
		for (int i = 0; i < nle.getLength(); i++) {
			// System.out.printf("-- %d %s %n", i, nle.item(i));
			Node ei = nle.item(i);
			if (Utils.isText(ei)) {
				s += ei.getTextContent();
			}
		}
		return s;
	}
}
