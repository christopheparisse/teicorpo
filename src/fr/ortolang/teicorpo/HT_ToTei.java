package fr.ortolang.teicorpo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HT_ToTei {

	/** Encodage des fichiers de sortie et d'entrée. */
	static final String outputEncoding = "UTF-8";
	/** Version tei **/

	static int idNb = 0;

	/** Document Tei à créer. */
	Document docTEI;
	/** Racine du document TEI. */
	Element rootTEI;
	/** Element timeline */
	Element timeline;
	/* Nouvelle timeline */
	Comparator<String> timeCompare = new Comparator<String>() {
		@Override
		public int compare(String s1, String s2) {
			Double f1 = Double.parseDouble(s1);
			Double f2 = Double.parseDouble(s2);
			return f1 < f2 ? -1 : (f1 > f2 ? 1 : 0);
		}
	};
	Map<String, String> newTimeline = new TreeMap<String, String>(timeCompare);
	HashMap<String, String> newTimelineInverse = new HashMap<String, String>();
	int lastIdTimeline = 0; // last id included in the map

	String timelineValueOf(String time) {
		if (time.isEmpty())
			return "";
		try {
			if (Double.parseDouble(time) == 0.0)
				return "T0";
		} catch (Exception e) {
			return "";
		}
		String nt = newTimeline.get(time);
		if (nt != null)
			return nt;
		else {
			lastIdTimeline++;
			nt = "T" + lastIdTimeline;
			newTimeline.put(time, nt);
			newTimelineInverse.put(nt, time);
			return nt;
		}
	}

	String timelineTimeOf(String ref) {
		if (ref.startsWith("#"))
			return newTimelineInverse.get(ref.substring(1));
		else
			return newTimelineInverse.get(ref);
	}

	/** Liste des types de tiers présents dans le corpus */
	HashSet<String> tiersNames;

	String duration;

	HierarchicTrans ht;

	Element mainDiv;

	ArrayList<Element> annotatedUElements;
	
	TierParams options = null;

	static int ID = 1;

	public HT_ToTei(HierarchicTrans hiertrans, TierParams tp) throws IOException {

		tiersNames = new HashSet<String>();
		annotatedUElements = new ArrayList<Element>();
		ht = hiertrans;
		if (tp != null) options = tp;

		DocumentBuilderFactory factory = null;

		try {
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Utils.setDTDvalidation(factory, true);
			docTEI = builder.newDocument();
			rootTEI = docTEI.createElement("TEI");
			rootTEI.setAttribute("version", Utils.versionTEI);
			rootTEI.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
			docTEI.appendChild(rootTEI);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Conversions
		conversion();
	}

	public void conversion() throws DOMException, IOException {
		buildEmptyTEI();
		buildHeader();
		buildText();
	}

	/**
	 * Création d'un Document TEI minimal.
	 */
	public void buildEmptyTEI() {
		Element teiHeader = docTEI.createElement("teiHeader");
		rootTEI.appendChild(teiHeader);
		Element fileDesc = docTEI.createElement("fileDesc");
		teiHeader.appendChild(fileDesc);

		Element titleStmt = this.docTEI.createElement("titleStmt");
		fileDesc.appendChild(titleStmt);
		Element publicationStmt = docTEI.createElement("publicationStmt");
		fileDesc.appendChild(publicationStmt);
		Element notesStmt = docTEI.createElement("notesStmt");
		fileDesc.appendChild(notesStmt);
		Element sourceDesc = this.docTEI.createElement("sourceDesc");
		fileDesc.appendChild(sourceDesc);

		Element profileDesc = docTEI.createElement("profileDesc");
		teiHeader.appendChild(profileDesc);
		Element encodingDesc = docTEI.createElement("encodingDesc");
		teiHeader.appendChild(encodingDesc);
		Element revisionDesc = this.docTEI.createElement("revisionDesc");
		teiHeader.appendChild(revisionDesc);
		Utils.setRevisionInfo(this.docTEI, revisionDesc, ht.filePath, null);

		Element text = docTEI.createElement("text");
		rootTEI.appendChild(text);
		timeline = docTEI.createElement("timeline");
		text.appendChild(timeline);
		Element when = docTEI.createElement("when");
		when.setAttribute("absolute", "0");
		when.setAttribute("xml:id", "T0");
		timeline.appendChild(when);
		timeline.setAttribute("unit", ht.metaInf.time_units);
		Element body = docTEI.createElement("body");
		text.appendChild(body);
	}

	/**
	 * Remplissage du header.
	 * 
	 * @throws IOException
	 * @throws DOMException
	 */
	public void buildHeader() throws DOMException, IOException {
		setFileDescElement();
		setProfileDescElement();
		setEncodingDesc();
		addTemplateDesc();
	}

	public void setFileDescElement() {

		// Ajout publicationStmt
		Element publicationStmt = (Element) this.docTEI.getElementsByTagName("publicationStmt").item(0);
		Element distributor = docTEI.createElement("distributor");
		distributor.setTextContent("tei_corpo");
		publicationStmt.appendChild(distributor);

		// Ajout titleStmt
		Element titleStmt = (Element) this.docTEI.getElementsByTagName("titleStmt").item(0);
		Element title = this.docTEI.createElement("title");
		titleStmt.appendChild(title);
		Element desc = docTEI.createElement("desc");
		title.appendChild(desc);
		desc.setTextContent("Fichier TEI obtenu à partir du fichier " + ht.fileName);

		// Ajout notesStmt
		Element notesStmt = (Element) this.docTEI.getElementsByTagName("notesStmt").item(0);
		Element addNotes = docTEI.createElement("note");
		addNotes.setAttribute("type", "COMMENTS_DESC");
		notesStmt.appendChild(addNotes);

		// SOURCEDESC
		if (docTEI.getElementsByTagName("sourceDesc").getLength() == 0) {
			Element sourceDesc = docTEI.createElement("sourceDesc");
			docTEI.getElementsByTagName("fileDesc").item(0).appendChild(sourceDesc);
		}
		Element sourceDesc = (Element) docTEI.getElementsByTagName("sourceDesc").item(0);
		if (docTEI.getElementsByTagName("recordingStmt").getLength() == 0) {
			Element recordingStmt = docTEI.createElement("recordingStmt");
			sourceDesc.appendChild(recordingStmt);
			Element recording = docTEI.createElement("recording");
			recordingStmt.appendChild(recording);
		}
		Element recording = (Element) docTEI.getElementsByTagName("recording").item(0);
		for (Media m : ht.metaInf.medias) {
			Element media = docTEI.createElement("media");
			media.setAttribute("url", m.url);
			if (!m.type.isEmpty())
				media.setAttribute("mimeType", m.type);
			else
				media.setAttribute("mimeType", Utils.findMimeType(m.url));
			recording.appendChild(media);
		}

		// NOTESSTMT
		for (Map.Entry<String, String> entry : ht.metaInf.additionnalNotes.entrySet()) {
			Element note = docTEI.createElement("note");
			addNotes.appendChild(note);
			note.setAttribute("type", entry.getKey());
			note.setTextContent(entry.getValue());
		}
	}

	public void setAttr(Element e, String attName, String attValue) {
		if (Utils.isNotEmptyOrNull(attValue)) {
			e.setAttribute(attName, attValue);
		}
	}

	public void setProfileDescElement() {
		Node profileDesc = docTEI.getElementsByTagName("profileDesc").item(0);
		
		Element settingDesc = docTEI.createElement("settingDesc");
		Element setting = docTEI.createElement("setting");
		Element activity = docTEI.createElement("activity");
		if (options.situation != null) activity.setTextContent(options.situation);
		setting.appendChild(activity);
		setting.setAttribute("xml:id", "d0");
		settingDesc.appendChild(setting);
		profileDesc.appendChild(settingDesc);
		// ParticDesc
		int loc_nb = 1;
		Element particDesc = docTEI.createElement("particDesc");
		profileDesc.appendChild(particDesc);
		Element listPerson = docTEI.createElement("listPerson");
		particDesc.appendChild(listPerson);
		for (Participant participant : ht.metaInf.participants) {
			Element person = docTEI.createElement("person");
			listPerson.appendChild(person);

			Element alt = docTEI.createElement("alt");
			alt.setAttribute("type", participant.id);
			Element altGrp = docTEI.createElement("altGrp");
			altGrp.appendChild(alt);
			person.appendChild(altGrp);

			if (participant.age == null || participant.age.isEmpty())
				setAttr(person, "age", Utils.normaliseAge(options.defaultAge));
			else
				setAttr(person, "age", Utils.normaliseAge(participant.age));
			setAttr(person, "role", participant.role);
			setAttr(person, "source", participant.corpus);
			if (Utils.isNotEmptyOrNull(participant.sex)) {
				if (participant.sex.toLowerCase().substring(0, 1).equals("m")) {
					person.setAttribute("sex", "1");
				} else if (participant.sex.toLowerCase().substring(0, 1).equals("f")) {
					person.setAttribute("sex", "2");
				} else {
					person.setAttribute("sex", "9");
				}
			}
			if (Utils.isNotEmptyOrNull(participant.name)) {
				Element persName = docTEI.createElement("persName");
				persName.setTextContent(participant.name);
				person.appendChild(persName);
			}

			if (Utils.isNotEmptyOrNull(participant.language)) {
				Element langKnowledge = docTEI.createElement("langKnowledge");
				Element langKnown = docTEI.createElement("langKnown");
				langKnown.setTextContent(participant.language);
				langKnowledge.appendChild(langKnown);
				person.appendChild(langKnowledge);
			}

			// Informations additionnelles
			for (Map.Entry<String, String> entry : participant.adds.entrySet()) {
				Element n = docTEI.createElement("note");
				n.setTextContent(entry.getValue());
				n.setAttribute("type", entry.getKey());
				person.appendChild(n);
			}
			loc_nb++;
		}

		// Ajout des voc controlés
		if (!ht.cvs.isEmpty()) {
			Element textClass = docTEI.createElement("textClass");
			profileDesc.appendChild(textClass);
			for (CV cv : ht.cvs) {
				Element keywords = docTEI.createElement("keywords");
				textClass.appendChild(keywords);
				keywords.setAttribute("scheme", cv.id);
				if (Utils.isNotEmptyOrNull(cv.cv_desc))
					keywords.setAttribute("style", cv.cv_desc);
				if (Utils.isNotEmptyOrNull(cv.cv_desc_lang))
					keywords.setAttribute("xml:lang", cv.cv_desc_lang);
				for (CV_entry entry : cv.entries) {
					Element term = docTEI.createElement("term");
					if (Utils.isNotEmptyOrNull(entry.description))
						term.setAttribute("type", entry.description);
					if (Utils.isNotEmptyOrNull(entry.lang))
						term.setAttribute("xml:lang", entry.lang);
					term.setTextContent(entry.term);
					keywords.appendChild(term);
				}
			}
		}
	}

	public void setEncodingDesc() {
		Element encodingDesc = (Element) docTEI.getElementsByTagName("encodingDesc").item(0);
		Element appInfo = docTEI.createElement("appInfo");
		encodingDesc.appendChild(appInfo);
		Element application = docTEI.createElement("application");
		application.setAttribute("ident", "TeiCorpo");
		application.setAttribute("version", Utils.versionSoft);
		Element desc = this.docTEI.createElement("desc");
		application.appendChild(desc);
		desc.setTextContent("Transcription converted with TeiCorpo and to TEI_CORPO");
		appInfo.appendChild(application);
	}

	public void addTemplateDesc() {

		Element fileDesc = (Element) this.docTEI.getElementsByTagName("fileDesc").item(0);
		Element notesStmt = (Element) fileDesc.getElementsByTagName("notesStmt").item(0);
		Element templateNote = docTEI.createElement("note");
		templateNote.setAttribute("type", "TEMPLATE_DESC");
		notesStmt.appendChild(templateNote);

		// Ajout des locuteurs dans les templates
		/*
		 * Element particDesc = (Element)
		 * this.docTEI.getElementsByTagName("particDesc").item(0); NodeList
		 * persons = particDesc.getElementsByTagName("person"); for(int i = 0;
		 * i<persons.getLength(); i++){ Element person =
		 * (Element)persons.item(i); Element note =
		 * docTEI.createElement("note");
		 * 
		 * if(person.getElementsByTagName("alt").getLength()>0){ Element alt =
		 * (Element)person.getElementsByTagName("alt").item(0);
		 * 
		 * Element noteCode = docTEI.createElement("note");
		 * noteCode.setAttribute("type", "code");
		 * noteCode.setTextContent(alt.getAttribute("type"));
		 * note.appendChild(noteCode);
		 * 
		 * Element noteParent = docTEI.createElement("note");
		 * noteParent.setAttribute("type", "parent"); String parent =
		 * ht.tiersInfo.get(alt.getAttribute("type")).parent;
		 * if(Utils.isNotEmptyOrNull(parent)){
		 * noteParent.setTextContent(parent); } else {
		 * noteParent.setTextContent("-"); } note.appendChild(noteParent);
		 * 
		 * Element noteType = docTEI.createElement("note");
		 * noteType.setAttribute("type", "type"); String type =
		 * ht.tiersInfo.get(alt.getAttribute("type")).type.constraint;
		 * if(Utils.isNotEmptyOrNull(type)){ noteType.setTextContent(type); }
		 * else{ noteType.setTextContent("-"); } note.appendChild(noteType); }
		 * templateNote.appendChild(note);
		 * 
		 * }
		 */
		for (Map.Entry<String, TierInfo> entry : ht.tiersInfo.entrySet()) {
			Element note = docTEI.createElement("note");
			TierInfo tierInfo = entry.getValue();
			// code
			Element noteCode = docTEI.createElement("note");
			noteCode.setAttribute("type", "code");
			noteCode.setTextContent(entry.getKey());
			note.appendChild(noteCode);
			// parent
			Element noteParent = docTEI.createElement("note");
			noteParent.setAttribute("type", "parent");
			if (Utils.isNotEmptyOrNull(tierInfo.parent)) {
				noteParent.setTextContent(tierInfo.parent);
			} else {
				noteParent.setTextContent("-");
			}
			note.appendChild(noteParent);
			// type
			if (Utils.isNotEmptyOrNull(tierInfo.type.constraint)) {
				Element noteType = docTEI.createElement("note");
				noteType.setAttribute("type", "type");
				noteType.setTextContent(tierInfo.type.constraint);
				note.appendChild(noteType);
			}
			// subtype
			if (Utils.isNotEmptyOrNull(tierInfo.type.lgq_type_id)) {
				Element noteSubType = docTEI.createElement("note");
				noteSubType.setAttribute("type", "subtype");
				noteSubType.setTextContent(tierInfo.type.lgq_type_id);
				note.appendChild(noteSubType);
			}
			// scribe
			if (Utils.isNotEmptyOrNull(tierInfo.annotator)) {
				Element noteScribe = docTEI.createElement("note");
				noteScribe.setAttribute("type", "scribe");
				noteScribe.setTextContent(tierInfo.annotator);
				note.appendChild(noteScribe);
			}
			// cv_id
			if (Utils.isNotEmptyOrNull(tierInfo.type.cv_ref)) {
				// System.out.println("Ajout de " + tierInfo.type.cv_ref + "
				// dans " + entry.getKey());
				Element noteCV_ID = docTEI.createElement("note");
				noteCV_ID.setAttribute("type", "cv");
				noteCV_ID.setTextContent(tierInfo.type.cv_ref);
				note.appendChild(noteCV_ID);
			}
			// lang
			if (Utils.isNotEmptyOrNull(tierInfo.lang)) {
				Element noteLang = docTEI.createElement("note");
				noteLang.setAttribute("type", "lang");
				noteLang.setTextContent(tierInfo.lang);
				note.appendChild(noteLang);
				templateNote.appendChild(note);
			}
			// lang_ref
			if (Utils.isNotEmptyOrNull(tierInfo.lang_ref)) {
				Element noteLang = docTEI.createElement("note");
				noteLang.setAttribute("type", "langref");
				noteLang.setTextContent(tierInfo.lang_ref);
				note.appendChild(noteLang);
				templateNote.appendChild(note);
			}
			// graphic_ref
			if (Utils.isNotEmptyOrNull(tierInfo.type.graphic_ref)) {
				Element noteLang = docTEI.createElement("note");
				noteLang.setAttribute("type", "graphicref");
				noteLang.setTextContent(tierInfo.type.graphic_ref);
				note.appendChild(noteLang);
				templateNote.appendChild(note);
			}
			// time_align
			if (Utils.isNotEmptyOrNull(tierInfo.type.time_align ? "true" : "false")) {
				Element noteLang = docTEI.createElement("note");
				noteLang.setAttribute("type", "timealign");
				noteLang.setTextContent(tierInfo.type.time_align ? "true" : "false");
				note.appendChild(noteLang);
				templateNote.appendChild(note);
			}
			templateNote.appendChild(note);
		}
	}

	public void buildTimeline() {
		// write the timeline
		// Get a set of the entries
		Set set = newTimeline.entrySet();

		// Get an iterator
		Iterator it = set.iterator();

		// Display elements
		while (it.hasNext()) {
			Map.Entry me = (Map.Entry) it.next();
			// System.out.print("Key is: "+me.getKey() + " & ");
			// System.out.println("Value is: "+me.getValue());
			Element when = docTEI.createElement("when");
			when.setAttribute("interval", (String) me.getKey());
			when.setAttribute("xml:id", (String) me.getValue());
			when.setAttribute("since", "#T0");
			timeline.appendChild(when);
		}
		/*
		 * Double whenVal = (double) -1; ArrayList<Element> timesList = new
		 * ArrayList<Element>(); for(Entry<String, String> entry :
		 * ht.timeline.entrySet()){ Element when = docTEI.createElement("when");
		 * when.setAttribute("interval", entry.getValue());
		 * when.setAttribute("xml:id", entry.getKey());
		 * when.setAttribute("since", "#T0"); timesList.add(when);
		 * if(Utils.isNotEmptyOrNull(entry.getValue()) &&
		 * Double.parseDouble(entry.getValue())>whenVal){ whenVal =
		 * Double.parseDouble(entry.getValue()); } } if(whenVal!=-1){ //Ajout
		 * durée doc duration = Double.toString(whenVal); Element recording =
		 * (Element)docTEI.getElementsByTagName("recording").item(0);
		 * recording.setAttribute("dur", duration); }
		 * Utils.sortTimeline(timesList); for(Element when : timesList){
		 * timeline.appendChild(when); }
		 */
	}

	public void buildTrans() {
		Element body = (Element) docTEI.getElementsByTagName("body").item(0);
		mainDiv = Utils.createDivHead(docTEI);
		mainDiv.setAttribute("type", "Situation");
		mainDiv.setAttribute("subtype", "d0");
		Utils.setDivHeadAttr(docTEI, mainDiv, "start", "#T0");
		body.appendChild(mainDiv);
		// Création des annotU principaux
		buildMainAnnotUs();
		// Création des sous-parties
	}

	public void buildMainAnnotUs() {
		Double maxTime = 0.0;
		ArrayList<Element> annotWithoutStart = new ArrayList<Element>();
		for (Entry<String, ArrayList<Annot>> entry : ht.hierarchic_representation.entrySet()) {
			String annotType = entry.getKey();
			// System.out.println("annotType: "+annotType);
			// String spk = ht.tiersInfo.get(annotType).participant;
			ArrayList<Annot> annotList = entry.getValue();
			for (Annot annot : annotList) {
				try {
					if (!annot.end.isEmpty()) {
						Double t = Double.parseDouble(annot.end);
						if (t > maxTime)
							maxTime = t;
					} else if (!annot.start.isEmpty()) {
						Double t = Double.parseDouble(annot.start);
						if (t > maxTime)
							maxTime = t;
					}
				} catch(Exception e) {
					
				}
				Element annotUEl = Utils.createAnnotationBloc(docTEI);
				// Set annotatedU attributes
				String startStr = timelineValueOf(annot.start);
				String endStr = timelineValueOf(annot.end);
				Utils.setAttrAnnotationBloc(docTEI, annotUEl, "start", "#" + startStr);
				Utils.setAttrAnnotationBloc(docTEI, annotUEl, "end", "#" + endStr);
				if (Utils.isNotEmptyOrNull(annot.id)) {
					Utils.setAttrAnnotationBloc(docTEI, annotUEl, "xml:id", annot.id);
				} else {
					Utils.setAttrAnnotationBloc(docTEI, annotUEl, "xml:id", "au" + idNb);
					idNb++;
				}
				// System.out.println("annot: "+annotType+ " "+annot.id + " "
				// +annot.name);
				Utils.setAttrAnnotationBloc(docTEI, annotUEl, "who", annotType);
				// buildU//
				Element u = docTEI.createElement("u");
				Element seg = docTEI.createElement("seg");
				seg.setTextContent(annot.getContent());
				u.appendChild(seg);
				annotUEl.appendChild(u);
				if (Utils.isNotEmptyOrNull(startStr)) {
					annotatedUElements.add(annotUEl);
				} else {
					annotWithoutStart.add(annotUEl);
				}
				// Création des dépendances
				buildDependances(annotUEl, annot.dependantAnnotations);
			}
		}
		sortNodes();
		annotatedUElements.addAll(0, annotWithoutStart);
		for (Element annotU : annotatedUElements) {
			// System.out.printf("%s %s %s%n", annotU.getAttribute("who"),
			// timelineTimeOf(annotU.getAttribute("start")),
			// timelineTimeOf(annotU.getAttribute("end")));
			mainDiv.appendChild(annotU);
		}

		// Element lastAU = (Element)
		// mainDiv.getElementsByTagName(Utils.ANNOTATIONBLOC).item(mainDiv.getElementsByTagName(Utils.ANNOTATIONBLOC).getLength()-1);
		// mainDiv.setAttribute("end",lastAU.getAttribute("end"));
		Utils.setDivHeadAttr(docTEI, mainDiv, "end", "#" + timelineValueOf(Double.toString(maxTime)));
	}

	public void buildDependances(Element annotUEl, ArrayList<Annot> depAnnots) {
		String annotType1 = "";
		Element spanGrp = docTEI.createElement("spanGrp");
		Element span = docTEI.createElement("span");
		if (depAnnots != null && !depAnnots.isEmpty()) {
			annotType1 = depAnnots.get(0).name;
			spanGrp.setAttribute("type", annotType1);
			annotUEl.appendChild(spanGrp);
			ArrayList<String> dependAnnotIds = new ArrayList<String>();
			for (int i = 0; i < depAnnots.size(); i++) {
				Annot annot = depAnnots.get(i);
				if (!Utils.isNotEmptyOrNull(annot.id)) {
					annot.id = "s" + ID;
					ID++;
				}
				if (!dependAnnotIds.contains(annot.id)) {
					dependAnnotIds.add(annot.id);
					if (annot.name != annotType1) {
						spanGrp = docTEI.createElement("spanGrp");
						annotUEl.appendChild(spanGrp);
						spanGrp.setAttribute("type", annot.name);
						annotType1 = annot.name;
					}
					span = docTEI.createElement("span");
					span.setTextContent(annot.getContent().trim());
					// System.out.println(annot.content.trim() + " --- >>> " +
					// annot.dependantAnnotations );
					if (Utils.isNotEmptyOrNull(annot.id)) {
						span.setAttribute("xml:id", annot.id);
					}
					if (!ht.tiersInfo.get(annot.name).type.time_align) {
						if (Utils.isNotEmptyOrNull(annot.link))
							span.setAttribute("target", "#" + annot.link);
					} else {
						if (Utils.isNotEmptyOrNull(annot.start)) {
							// System.out.println(annot.start);
							String startStr = timelineValueOf(annot.start);
							span.setAttribute("from", "#" + startStr);
						}
						if (Utils.isNotEmptyOrNull(annot.end)) {
							String endStr = timelineValueOf(annot.end);
							span.setAttribute("to", "#" + endStr);
						}
					}
					spanGrp.appendChild(span);
					buildDependances(span, annot.dependantAnnotations);
				}
			}
		}
	}

	public void buildText() {
		buildTrans();
		buildTimeline();
	}

	public void sortNodes() {
		CompareAnnotU ca = new CompareAnnotU(this);
		Collections.sort(annotatedUElements, ca);
	}

	/**
	 * Affiche la description et l'usage du programme principal.
	 */
	public static void usage() {
		System.err.println(
				"Description: HierarchicalTransToTEI convertit une structure de donnée de transcription de type hiérarchique en un fichier au format TEI");
		System.err.println("Usage: HierarchicalTransToTEI <file.tei>");
		System.err.println("	: -usage ou -help = affichage de ce message\n");
		System.exit(1);
	}
}