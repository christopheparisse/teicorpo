/**
 * module ImportToTei
 * contains the basic and commun function used to create or add information in a TEI file
 * usefull for importing from a transcription format to a TEI file
 * handles the addition of elements to the header, handling the timeline, opening, creating, closing and writing a TEI file 
 * extends GenericMain
 */

package fr.ortolang.teicorpo;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.lang.String;
import javax.xml.xpath.*;

import org.w3c.dom.*;

public abstract class ImportToTei extends GenericMain {
	static String EXT = ".txt";

	/** new TEI document (output).  */
	Document docTEI;
	/** root of TEI document. */
	Element rootTEI;
	// acces XpathTest
	public XPathFactory xPathfactory;
	public XPath xpath;
	/** Liste des types de tiers présents dans le fichier */
	HashSet<String> tiersNames;
	// timeline
	ArrayList<String> times;
	ArrayList<Element> timeElements;
	Double maxTime = 0.0;
	int whenId = 0;
	// params
	TierParams optionsTEI = null;


	public void addTimeline() {
		Utils.sortTimeline(timeElements);
		Element timeline = (Element) docTEI.getElementsByTagName("timeline").item(0);
		for (Element when : timeElements) {
			timeline.appendChild(when);
		}
	}

	public void setDivTimes() {
		NodeList divs = null;
		try {
			divs = TeiDocument.getAllDivs(this.xpath, this.docTEI);
		} catch (XPathExpressionException e1) {
			e1.printStackTrace();
		}
		// System.out.println("nb divs: " + divs.getLength());
		for (int i = 0; i < divs.getLength(); i++) {
			Element div = (Element) divs.item(i);
			// System.out.println(i + " " + div.getAttribute("type"));
			NodeList annotUElmts = null;
			try {
				annotUElmts = TeiDocument.getSomeAnnotationBloc(this.xpath, div);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			// System.out.println(i + " :- " + annotUElmts.getLength());
			if (annotUElmts.getLength() != 0) {
				String start = "";
				String end = "";
				for (int j = 0; j < annotUElmts.getLength(); j++) {
					Element currentU = (Element) annotUElmts.item(j);
					// System.out.println(j + " " +
					// currentU.getAttribute("type"));
					String time = TeiDocument.getAttrAnnotationBloc(currentU, "start"); // ERROR ?
					// System.out.println("start: "+time);
					if (Utils.isNotEmptyOrNull(time) && start == "") {
						start = time;
					}
					time = TeiDocument.getAttrAnnotationBloc(currentU, "end"); // ERROR ?
					// System.out.println("end: "+time);
					if (Utils.isNotEmptyOrNull(time)) {
						end = time;
					}
				}
				String startId = addTimeToTimeline(getTimeValue(start));
				String endId = addTimeToTimeline(getTimeValue(end));
				TeiDocument.setDivHeadAttr(this.docTEI, div, "start", startId);
				TeiDocument.setDivHeadAttr(this.docTEI, div, "end", endId);
			}
		}
	}

	/**
	 * Ajoute au document l'information de la durée de l'enregistrement.
	 */
	public void setDurDate() {
		Element recording = (Element) this.docTEI.getElementsByTagName("recording").item(0);
		NodeList mediaList = recording.getElementsByTagName("media");
		if (mediaList.getLength() > 0) {
			Element media = (Element) recording.getElementsByTagName("media").item(0);
			media.setAttribute("dur-iso", String.valueOf(maxTime));
		} else {
			Element media = docTEI.createElement("media");
			recording.appendChild(media);
			media.setAttribute("dur-iso", String.valueOf(maxTime));
		}
	}

	public String getTimeValue(String timeId) {
		if (Utils.isNotEmptyOrNull(timeId)) {
			return times.get(Integer.parseInt(timeId.split("#T")[1]));
		}
		return "";
	}

	public String addTimeToTimeline(String time) {
		if (time.equals("0")) {
			return "#T0";
		} else if (Utils.isNotEmptyOrNull(time)) {
			Double t = Double.parseDouble(time);
			if (t > maxTime)
				maxTime = t;
			String id = "";
			if (times.contains(time)) {
				id = "T" + times.indexOf(time);
			} else {
				times.add(time);
				Element when = docTEI.createElement("when");
				when.setAttribute("interval", time);
				whenId++;
				id = "T" + whenId;
				when.setAttribute("xml:id", id);
				when.setAttribute("since", "#T0");
				// timeline.appendChild(when);
				timeElements.add(when);
			}
			return "#" + id;
		} else {
			return "";
		}
	}

	/**
	 * Création d'un fichier TEI "vide": contenant les éléments principaux soit:
	 * <br>
	 * <ul>
	 * <li><strong>teiHeader</strong>: contient des informations sur le document
	 * <br>
	 * <li><strong>fileDesc</strong>: contient une description du document<br>
	 * <li><strong>profileDesc</strong>: contient une description du contenu du
	 * document<br>
	 * <li><strong>revisionDesc</strong>: contient des informations sur les
	 * versions et révisions du document<br>
	 * <li><strong>text</strong>: contient le contenu de la transcription<br>
	 * <li><strong>div</strong>: contient une partie du document, distinguée par
	 * son thème ("desc")<br>
	 * </ul>
	 */
	public void buildTEI(String fname) {
		Element teiHeader = TeiDocument.findOrCreate(docTEI, rootTEI, "teiHeader");
		Element fileDesc = TeiDocument.findOrCreate(docTEI, teiHeader, "fileDesc");
		Element titleStmt = TeiDocument.findOrCreate(docTEI, fileDesc, "titleStmt");
		Element publicationStmt = TeiDocument.findOrCreate(docTEI, titleStmt, "publicationStmt");

		// Ajout publicationStmt
		Element distributor = TeiDocument.findOrCreate(docTEI, publicationStmt, "distributor");
		distributor.setTextContent("tei_corpo");

		Element notesStmt = TeiDocument.findOrCreate(docTEI, fileDesc, "notesStmt");
		Element sourceDesc = TeiDocument.findOrCreate(docTEI, fileDesc, "sourceDesc");

		Element profileDesc = TeiDocument.findOrCreate(docTEI, teiHeader, "profileDesc");
		Element encodingDesc = TeiDocument.findOrCreate(docTEI, teiHeader, "encodingDesc");
		encodingDesc.setAttribute("style", Version.versionTEI);
		Element revisionDesc = TeiDocument.findOrCreate(docTEI, teiHeader, "revisionDesc");
		TeiDocument.setRevisionInfo(docTEI, revisionDesc, fname, null, optionsTEI.test);

		Element text = TeiDocument.findOrCreate(docTEI, rootTEI, "text");
		Element timeline = TeiDocument.findOrCreate(docTEI, text, "timeline");
		Element body = TeiDocument.findOrCreate(docTEI, text, "body");

		Element when = docTEI.createElement("when");
		when.setAttribute("absolute", "0");
		when.setAttribute("xml:id", "T" + whenId);
		// timeline.appendChild(when);
		timeElements.add(when);
		timeline.setAttribute("unit", "s");
		times.add("0.0");
	}

	/**
	 * Remplissage du header.
	 * 
	 * @throws IOException
	 * @throws DOMException
	 */
	public void buildHeader(String info, boolean written) throws DOMException, IOException {
		////// Mise à jour de l'élémentFileDesc: titre + info enregistrement+
		////// date + lieu+ éventuelles notes
		setFileDesc(info);
		setProfileDesc(written);
		setEncodingDesc();
	}

	/**
	 * Mise à jour de l'élémént encodingDesc: informations sur le logiciel qui a
	 * généré le document d'origine
	 */
	public void setEncodingDesc() {
		Element encodingDesc = (Element) docTEI.getElementsByTagName("encodingDesc").item(0);
		Element appInfo = this.docTEI.createElement("appInfo");
		encodingDesc.appendChild(appInfo);
		Element application = this.docTEI.createElement("application");
		application.setAttribute("ident", "TeiCorpo");
		application.setAttribute("version", Version.versionSoft);
		appInfo.appendChild(application);
		Element desc = this.docTEI.createElement("desc");
		application.appendChild(desc);
		desc.setTextContent("Transcription converted with TeiCorpo to TEI_CORPO - Soft: " + Version.versionSoft);
	}

	/**
	 * Mise à jour de l'élément <strong>fileDesc</strong>, correspondant à la
	 * description formelle du fichier.<br>
	 * Contient les informations sur le fichiers et sur l'enregistrement dont
	 * provient la transcription.
	 * 
	 * @throws IOException
	 * @throws DOMException
	 */
	public void setFileDesc(String descinfo) throws DOMException, IOException {
		// titleStmt
		Element titleStmt = (Element) this.docTEI.getElementsByTagName("titleStmt").item(0);
		Element title = docTEI.createElement("title");
		titleStmt.appendChild(title);
		Element desc = docTEI.createElement("desc");
		title.appendChild(desc);
		desc.setTextContent(descinfo);

		// sourceDesc
		Element sourceDesc = (Element) this.docTEI.getElementsByTagName("sourceDesc").item(0);
		Element recordingStmt = docTEI.createElement("recordingStmt");
		sourceDesc.appendChild(recordingStmt);
		Element recording = docTEI.createElement("recording");
		recordingStmt.appendChild(recording);
		// Element media
		Element media = docTEI.createElement("media");
		recording.appendChild(media);
		if (optionsTEI.mediaName != null) {
			media.setAttribute("mimeType", Utils.findMimeType(optionsTEI.mediaName));
			media.setAttribute("url", optionsTEI.mediaName);
		}
	}

	// Construction de l'élément profileDesc : settingDesc + Stmt
	/**
	 * Mise à jour de l'élément <strong>profileDesc</strong>, correspondant à la
	 * description sémantique du fichier.<br>
	 * Contient les informations sur les s et les situations de
	 * l'enregistrement.
	 */
	public void setProfileDesc(boolean written) {
		Element profileDesc = (Element) docTEI.getElementsByTagName("profileDesc").item(0);
		setSettingDesc(profileDesc);
		Element particDesc = (Element) docTEI.getElementsByTagName("particDesc").item(0);
		if (particDesc == null)
			setParticDesc(profileDesc, written);
	}

	/**
	 * Mise à jour des éléments <strong>person</strong> et de leurs attributs.
	 * 
	 * @param profileDesc
	 */
	public void setParticDesc(Element profileDesc, boolean written) {
		Element particDesc = docTEI.createElement("particDesc");
		profileDesc.appendChild(particDesc);
		Element listPerson = docTEI.createElement("listPerson");
		particDesc.appendChild(listPerson);

		if (written == true) {
			Element person = docTEI.createElement("person");
			listPerson.appendChild(person);
			Element name = docTEI.createElement("persName");
			name.setTextContent("writtentext");
			person.appendChild(name);
			person.setAttribute("source", "orfeo");
			Element altGrp = docTEI.createElement("altGrp");
			Element alt = docTEI.createElement("alt");
			alt.setAttribute("type", "writtentext");
			person.appendChild(altGrp);
			altGrp.appendChild(alt);
			Element langKnowledge = docTEI.createElement("langKnowledge");
			Element langKnown = docTEI.createElement("langKnown");
			langKnown.setTextContent("français");
			langKnown.setAttribute("tag", "fra");
			langKnowledge.appendChild(langKnown);
			person.appendChild(langKnowledge);
			person.setAttribute("sex", "9");
		}
	}

	/**
	 * Mise à jour de l'élément <strong>settingDesc</strong>: contient les
	 * situations qui ont eu lieues lors de l'enregistrement.
	 * 
	 * @param profileDesc
	 *            L'élément <strong>profileDesc</strong> auqel est rattaché le
	 *            <strong>settingDesc</strong>.
	 */
	public void setSettingDesc(Element profileDesc) {
		Element settingDesc = (Element) docTEI.getElementsByTagName("settingDesc").item(0);
		if (settingDesc == null)
			settingDesc = docTEI.createElement("settingDesc");
		profileDesc.appendChild(settingDesc);

		Element langUsage = docTEI.createElement("langUsage");
		profileDesc.appendChild(langUsage);
		Element language = docTEI.createElement("language");
		// here we should find the real name for language and the ISO code
		language.setAttribute("ident", "fra");
		language.setTextContent("fra");
		langUsage.appendChild(language);
	}

	/**
	 * creation du premier div
	 */
	public Element setFirstDiv() {
		Element profileDesc = (Element) docTEI.getElementsByTagName("profileDesc").item(0);
		Element settingDesc = TeiDocument.findOrCreate(docTEI, profileDesc, "settingDesc");
		Element setting = docTEI.createElement("setting");

		Element activity = docTEI.createElement("activity");
		setting.appendChild(activity);
		Element body = (Element) docTEI.getElementsByTagName("body").item(0);
		Element div = TeiDocument.createDivHead(docTEI);
		body.appendChild(div);
		settingDesc.appendChild(setting);
		return div;
	}

	public static void addTemplateDesc(Document doc) {
		Element fileDesc = (Element) doc.getElementsByTagName("fileDesc").item(0);
		Element notesStmt = (Element) fileDesc.getElementsByTagName("notesStmt").item(0);
		Element templateNote = doc.createElement("note");
		templateNote.setAttribute("type", "TEMPLATE_DESC");
		notesStmt.appendChild(templateNote);

		// Ajout des locuteurs dans les templates
		Element particDesc = (Element) doc.getElementsByTagName("particDesc").item(0);
		if (particDesc == null) {
			Element note = doc.createElement("note");

			Element noteType = doc.createElement("note");
			noteType.setAttribute("type", "type");
			noteType.setTextContent("-");
			note.appendChild(noteType);

			Element noteParent = doc.createElement("note");
			noteParent.setAttribute("type", "parent");
			noteParent.setTextContent("-");
			note.appendChild(noteParent);
			templateNote.appendChild(note);

			Element noteCode = doc.createElement("note");
			noteCode.setAttribute("type", "code");
			noteCode.setTextContent("_");
			note.appendChild(noteCode);

			return;
		}
		NodeList persons = particDesc.getElementsByTagName("person");
		for (int i = 0; i < persons.getLength(); i++) {
			Element person = (Element) persons.item(i);
			Element note = doc.createElement("note");

			Element noteType = doc.createElement("note");
			noteType.setAttribute("type", "type");
			noteType.setTextContent("-");
			note.appendChild(noteType);

			Element noteParent = doc.createElement("note");
			noteParent.setAttribute("type", "parent");
			noteParent.setTextContent("-");
			note.appendChild(noteParent);
			if (person.getElementsByTagName("alt").getLength() > 0) {
				Element alt = (Element) person.getElementsByTagName("alt").item(0);

				Element noteCode = doc.createElement("note");
				noteCode.setAttribute("type", "code");
				noteCode.setTextContent(alt.getAttribute("type"));
				note.appendChild(noteCode);

			}
			templateNote.appendChild(note);
		}
	}

	public static void addTemplateTiersNames(Document doc, Set<String> tiersNames) {
		if (tiersNames != null) {
			for (String t : tiersNames) {
				insertTemplate(doc, t, "Symbolic_Association", "annotationBlock");
			}
		}
	}

	public static void insertTemplate(Document doc, String code, String type, String parent) {
		Element templateNote = getTemplate(doc);
		if (templateNote == null) {
			System.err.println("serious error: no template");
			return;
		}
		
		Element note = doc.createElement("note");

		Element noteCode = doc.createElement("note");
		noteCode.setAttribute("type", "code");
		noteCode.setTextContent(code);
		note.appendChild(noteCode);

		Element noteType = doc.createElement("note");
		noteType.setAttribute("type", "type");
		noteType.setTextContent(type);
		note.appendChild(noteType);

		Element noteParent = doc.createElement("note");
		noteParent.setAttribute("type", "parent");
		noteParent.setTextContent(parent);
		note.appendChild(noteParent);

		templateNote.appendChild(note);
	}

	public static Element getTemplate(Document doc) {
		NodeList nlNotesStmt = doc.getElementsByTagName("notesStmt");
		if (nlNotesStmt.getLength() < 1) return null;
		NodeList notesStmt = nlNotesStmt.item(0).getChildNodes();
		for (int i=0; i < notesStmt.getLength(); i++) {
			Node n = notesStmt.item(i);
			if (n.getNodeName().equals("note")) {
				Element e = (Element)n;
				if (e.getAttribute("type").equals("TEMPLATE_DESC")) {
					return e;
				}
			}
		}
		return null;
	}

	/*
	 * String deleteControlChars(String line){ StringBuffer buff = new
	 * StringBuffer(); char c; for (int pos = 0; pos < line.length(); pos++) { c
	 * = line.charAt(pos); if (Character.isISOControl(c)) { buff.append(' '); }
	 * else { buff.append(c); } } return buff.toString().replaceAll("\\x21",
	 * ""); }
	 */

	public void removeNote(Element add) {
		try {
			Element notesStmt = (Element) docTEI.getElementsByTagName("notesStmt").item(0);
			NodeList notes = notesStmt.getElementsByTagName("note");
			for (int i = 0; i < notes.getLength(); i++) {
				Element note = (Element) notes.item(i);
				if (note.getTextContent().equals(add.getTextContent())) {
					notesStmt.removeChild(note);
				}
			}
		} catch (Exception e) {
		}
	}

	public String toSeconds(String time_milliseconds) {
		return Float.toString(Float.parseFloat(time_milliseconds) / 1000);
	}

	public static String convTerm(String s) {
		String patternStr = "([^#])(\\+\\.\\.\\.|\\+/\\.|\\+!\\?|\\+//\\.|\\+/\\?|\\+\"/\\.|\\+\"\\.|\\+//\\?|\\+\\.\\.\\?|\\+\\.|\\.|\\?|!\\s*$)";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(s);
		if (matcher.find()) {
			s = s.replace(matcher.group(), matcher.group(1) + " {" + matcher.group(2)) + " /T}";
		}
		return s;
	}
}

