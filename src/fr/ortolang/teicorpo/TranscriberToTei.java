/**
 * @author Myriam Majdoub
 * TranscriberToTei permet de convertir un ou plusieurs fichiers au format Transcriber en un format TEI prédéfini pour la transcription de données orales.
 */

package fr.ortolang.teicorpo;

import java.io.File;
//import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TranscriberToTei extends GenericMain {

	// ***Variables statiques

	/** Encodage des fichiers de sortie et d'entrée. */
	static final String outputEncoding = "UTF-8";
	/** Id des éléments u */
	private static int utteranceId;
	static int whenId;

	// ***Variables d'instance

	/** Fichier d'entrée, au format .trs. */
	private File inputTRS;
	/** Document issu du fichier Transcriber. */
	private Document docTRS;
	/** Document du fichier TEI. */
	public Document docTEI;
	/** Racine du document TEI. */
	private Element rootTEI;
	// acces Xpath
	public XPathFactory xPathfactory;
	public XPath xpath;
	/** Racine du document Transcriber. */
	private Element rootTRS;
	/** Element timeline */
	// Element timeline;
	/** Liste des types de tiers présents dans le corpus */
	HashSet<String> tiersNames;
	ArrayList<String> times;
	ArrayList<Element> timeElements;
	Double maxTime = 0.0;
	
	private TierParams optionsTEI = null;

	/**
	 * décrit par la DTD TEI_CORPO_DTD.
	 * 
	 * @param inputFile
	 *            : fichier à convertir, au format Transcriber
	 */
	public void transform(File inputFile, TierParams options) {
		optionsTEI = options;
		
		utteranceId = 0;
		whenId = 0;
		times = new ArrayList<String>();
		timeElements = new ArrayList<Element>();
		tiersNames = new HashSet<String>();
		this.inputTRS = inputFile;

		// Création des documents TRS (à partir du parsing du fichier
		// Transcriber) et TEI (nouveau document)
		this.docTRS = null;
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			Utils.setDTDvalidation(factory, options.dtdValidation);
			DocumentBuilder builder = factory.newDocumentBuilder();
			this.docTRS = builder.parse(this.inputTRS);
			this.rootTRS = this.docTRS.getDocumentElement();
			this.docTEI = builder.newDocument();
			/*
			 * this.xPathfactory = XPathFactory.newInstance(); this.xpath =
			 * xPathfactory.newXPath(); this.xpath.setNamespaceContext(new
			 * NamespaceContext() { public String getNamespaceURI(String prefix)
			 * { System.out.println("prefix called " + prefix); if (prefix ==
			 * null) { throw new IllegalArgumentException("No prefix provided!"
			 * ); } else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
			 * System.out.println("default prefix called"); return
			 * "http://www.tei-c.org/ns/1.0"; } else if (prefix.equals("tei")) {
			 * System.out.println("tei prefix called"); return
			 * "http://www.tei-c.org/ns/1.0"; } else if (prefix.equals("xsi")) {
			 * return "http://www.w3.org/2001/XMLSchema-instance"; } else {
			 * return XMLConstants.NULL_NS_URI; } }
			 * 
			 * public Iterator<?> getPrefixes(String val) { return null; }
			 * 
			 * public String getPrefix(String uri) { return null; } });
			 */
			this.rootTEI = this.docTEI.createElement("TEI");
			rootTEI.setAttribute("version", Utils.versionTEI);
			this.rootTEI.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
			this.docTEI.appendChild(rootTEI);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			// e.printStackTrace();
			System.exit(1);
		}
		// Conversion
		this.conversion();
		String version = "none";
		NamedNodeMap map = this.rootTRS.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			String attName = map.item(i).getNodeName();
			String attValue = map.item(i).getNodeValue();
			if (attName == "version" && attValue != "") {
				version = attValue;
			}
		}
		Utils.setTranscriptionDesc(docTEI, "transcriber", version, "no information on TRS format");
	}

	/**
	 * Méthode de conversion: crée le document TEI à partir des informations
	 * contenues dans le document au format Transcriber.
	 */
	public void conversion() {
		this.buildEmptyTEI();
		this.setPublicationStmtElement();
		this.setNotesStmtElement();
		this.setProfileDescElement();
		this.setEncodingDesc();
		this.setTextElement();
		addTemplateDesc();
		addTimeline();
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
	public void buildEmptyTEI() {
		Element teiHeader = this.docTEI.createElement("teiHeader");
		this.rootTEI.appendChild(teiHeader);
		Element fileDesc = this.docTEI.createElement("fileDesc");
		teiHeader.appendChild(fileDesc);

		Element titleStmt = this.docTEI.createElement("titleStmt");
		fileDesc.appendChild(titleStmt);
		Element publicationStmt = docTEI.createElement("publicationStmt");
		fileDesc.appendChild(publicationStmt);
		Element notesStmt = docTEI.createElement("notesStmt");
		fileDesc.appendChild(notesStmt);
		Element sourceDesc = this.docTEI.createElement("sourceDesc");
		fileDesc.appendChild(sourceDesc);

		Element profileDesc = this.docTEI.createElement("profileDesc");
		teiHeader.appendChild(profileDesc);
		Element encodingDesc = this.docTEI.createElement("encodingDesc");
		teiHeader.appendChild(encodingDesc);

		Element revisionDesc = this.docTEI.createElement("revisionDesc");
		teiHeader.appendChild(revisionDesc);
		Utils.setRevisionInfo(this.docTEI, revisionDesc, this.inputTRS.getAbsolutePath(), null);

		///
		teiHeader.appendChild(revisionDesc);
		Element text = this.docTEI.createElement("text");
		this.rootTEI.appendChild(text);
		Element timeline = this.docTEI.createElement("timeline");
		text.appendChild(timeline);
		Element when = this.docTEI.createElement("when");
		when.setAttribute("absolute", "0");
		when.setAttribute("xml:id", "T" + whenId);
		// timeline.appendChild(when);
		timeElements.add(when);
		timeline.setAttribute("unit", "s");
		times.add("0.0");
		Element body = docTEI.createElement("body");
		text.appendChild(body);
	}

	/**
	 * Construction de l'élément <strong>titleStmt</strong> à partir des
	 * informations issues du document TRS:<br>
	 * contient les informations sur le titre du document.
	 * 
	 */
	public void buildTitleStmtElement() {
		Element titleStmt = (Element) this.docTEI.getElementsByTagName("titleStmt").item(0);
		Element title = this.docTEI.createElement("title");
		titleStmt.appendChild(title);
		title.setTextContent("Fichier TEI obtenu à partir du fichier Transcriber " + this.inputTRS.getName());
	}

	/**
	 * Mise à jour de l'élément encodingDesc: informations sur le logiciel qui a
	 * généré le document d'origine (ici Transcriber)
	 */
	public void setEncodingDesc() {
		Element encodingDesc = (Element) docTEI.getElementsByTagName("encodingDesc").item(0);
		Element appInfo = this.docTEI.createElement("appInfo");
		encodingDesc.appendChild(appInfo);
		Element application = this.docTEI.createElement("application");
		application.setAttribute("ident", "TeiCorpo");
		application.setAttribute("version", Utils.versionSoft);
		appInfo.appendChild(application);
		Element desc = this.docTEI.createElement("desc");
		application.appendChild(desc);
		desc.setTextContent("Transcription converted with TeiCorpo and to TEI_CORPO - Soft version: "
				+ Utils.versionSoft);
	}

	/**
	 * Ajout de notes dans le documents TEI.<br>
	 * Ajoute les informations dont on a pas l'équivalent au format TEI.
	 * 
	 * @param attName
	 * @param attValue
	 */
	public void addNotesElements(String attName, String attValue) {
		Node addNotes = this.docTEI.getElementsByTagName("notesStmt").item(0).getFirstChild();
		Element note = this.docTEI.createElement("note");
		addNotes.appendChild(note);
		if (attValue != "") {
			note.setAttribute("type", attName);
			note.setTextContent(attValue);
		}
	}

	/**
	 * Ajout d'informations dans l'élément <strong>sourceDesc.</strong><br>
	 * Contient les informations sur le document source soit:<br>
	 * <ul>
	 * <li>les informations d'enregistrement
	 * <li>de date et lieu d'enregistrement
	 * <li>le nom et l'emplacement du fichier (adresses relative et absolue)
	 * </ul>
	 * 
	 * @param attName
	 *            Nom de l'attribut à ajouter au document TEI.
	 * @param attValue
	 *            Valeur de l'attribut.
	 */
	public void addSourceDesc(String attName, String attValue) {
		Element sourceDesc = (Element) this.docTEI.getElementsByTagName("sourceDesc").item(0);
		if (this.docTEI.getElementsByTagName("recordingStmt").getLength() == 0) {
			Element recordingStmt = this.docTEI.createElement("recordingStmt");
			sourceDesc.appendChild(recordingStmt);
			Element recording = this.docTEI.createElement("recording");
			recordingStmt.appendChild(recording);
			Element media = this.docTEI.createElement("media");
			recording.appendChild(media);
		}
		Element recording = (Element) this.docTEI.getElementsByTagName("recording").item(0);
		Element media = (Element) recording.getElementsByTagName("media").item(0);
		if (attName == "audio_filename" && attValue != "") {
			String sameMedia = this.inputTRS.getName();
			sameMedia = sameMedia.substring(0, sameMedia.length() - 3) + "wav";
			media.setAttribute("url", /* this.inputTRS.getParent() + "/" + */ sameMedia);
			media.setAttribute("mimeType", Utils.findMimeType(sameMedia));
			// utiliser ? attValue
		} else if (attName == "elapsed_time") {
			recording.setAttribute("dur", attValue);
		}
	}

	/**
	 * Mise à jour des attributs et élëments de l'élément
	 * <strong>publicationStmt</strong> à partir des informations contenues dans
	 * le document TRS.
	 */
	public void setPublicationStmtElement() {
		Element publicationStmt = (Element) this.docTEI.getElementsByTagName("publicationStmt").item(0);

		// Ajout publicationStmt
		Element distributor = docTEI.createElement("distributor");
		distributor.setTextContent("tei_corpo");
		publicationStmt.appendChild(distributor);
	}

	/**
	 * Mise à jour des attributs et élëments de l'élément document TRS.
	 */
	public void setNotesStmtElement() {
		Element notesStmt = (Element) this.docTEI.getElementsByTagName("notesStmt").item(0);

		// Ajout notesStmt
		Element addNotes = docTEI.createElement("note");
		addNotes.setAttribute("type", "COMMENTS_DESC");
		notesStmt.appendChild(addNotes);

		NamedNodeMap map = this.rootTRS.getAttributes();
		this.buildTitleStmtElement();
		for (int i = 0; i < map.getLength(); i++) {
			String attName = map.item(i).getNodeName();
			String attValue = map.item(i).getNodeValue();
			if (attName == "audio_filename" && attValue != "") {
				this.addSourceDesc(attName, attValue);
			} else if (attName == "xml:lang" && attValue != "") {
				this.rootTEI.setAttribute(attName, attValue);
			} else if (attName == "version_date" && attValue != "") {
				this.addNotesElements(attName, attValue);
			} else {
				this.addNotesElements(attName, attValue);
			}
		}
	}

	/**
	 * Mise à jour des attributs et éléments de l'élément
	 * <strong>profileDesc</strong> à partir des informations contenues dans le
	 * document TRS.<br>
	 * Contient les informations de thèmes du document et des persons.
	 */
	public void setProfileDescElement() {
		Element settingDesc = this.docTEI.createElement("settingDesc");
		Element particDesc = this.docTEI.createElement("particDesc");
		Element listPerson = this.docTEI.createElement("listPerson");
		Node profileDesc = this.docTEI.getElementsByTagName("profileDesc").item(0);
		profileDesc.appendChild(settingDesc);
		profileDesc.appendChild(particDesc);
		particDesc.appendChild(listPerson);
		this.setsettingDescElement(settingDesc);
		this.setparticDescElement(listPerson);
	}

	/**
	 * Mise à jour des attributs et éléments de l'élément
	 * <strong>settingDesc</strong> à partir des informations contenues dans le
	 * document TRS. Liste les thèmes (<strong>desc</strong>) du document.
	 * 
	 * @param settingDesc
	 *            L'élément <strong>settingDesc</strong> à mettre à jour.
	 */
	public void setsettingDescElement(Element settingDesc) {
		int settingAdded = 0;
		Element t = (Element) this.docTRS.getElementsByTagName("Topics").item(0);
		if (t != null && t.hasAttribute("desc")) {
			Element setting = docTEI.createElement("setting");
			Element activity = docTEI.createElement("activity");
			if (optionsTEI.situation != null) activity.setTextContent(optionsTEI.situation);
			setting.appendChild(activity);
			setting.setAttribute("xml:id", "d0");
			settingDesc.appendChild(setting);
			settingAdded++;
		}
		NodeList topics = this.rootTRS.getElementsByTagName("Topic");
		for (int i = 0; i < topics.getLength(); i++) {
			Node topic = topics.item(i);
			NamedNodeMap map = topic.getAttributes();
			Node id = map.getNamedItem("id");
			Node descTRS = map.getNamedItem("desc");
			Element setting = this.docTEI.createElement("setting");
			Element activity = docTEI.createElement("activity");
			setting.appendChild(activity);
			settingDesc.appendChild(setting);
			setting.setAttribute("xml:id", id.getNodeValue());
			activity.setTextContent(descTRS.getNodeValue());
			settingAdded++;
		}
		if (settingAdded == 0) {
			// there must be at least one setting
			Element setting = this.docTEI.createElement("setting");
			settingDesc.appendChild(setting);
			Element p = this.docTEI.createElement("activity");
			setting.appendChild(p);
			if (optionsTEI.situation != null)
					p.setTextContent(optionsTEI.situation);
			else
				p.setTextContent("no setting information");
			setting.setAttribute("xml:id", "d0");
		}
	}

	/**
	 * Mise à jour des attributs et éléments de l'élément
	 * <strong>particDesc</strong> à partir des informations contenues dans le
	 * document TRS.<br>
	 * Liste les persons du documents et leurs attributs.
	 * 
	 * @param particDesc
	 *            L'élément à mettre à jour.
	 */
	public void setparticDescElement(Element particDesc) {
		NodeList speakers = this.rootTRS.getElementsByTagName("Speaker");
		for (int i = 0; i < speakers.getLength(); i++) {
			Node speaker = speakers.item(i);
			NamedNodeMap map = speaker.getAttributes();
			Element person = this.docTEI.createElement("person");
			particDesc.appendChild(person);
			person.setAttribute("age", Utils.normaliseAge(optionsTEI.defaultAge));
			for (int j = 0; j < map.getLength(); j++) {
				Node att = map.item(j);
				String attName = att.getNodeName();
				String attValue = att.getNodeValue();
				if (attValue != "") {
					if (attName == "type") {
						if (attValue.equals("male") || attValue.equals("female") || attValue.equals("unknown")) {
							if (attValue.equals("male")) {
								person.setAttribute("sex", "1");
							} else if (attValue.equals("female")) {
								person.setAttribute("sex", "2");
							} else {
								person.setAttribute("sex", "9");
							}
						} else {
							person.setAttribute("role", attValue);
						}
					} else if (attName == "name") {
						Element name = docTEI.createElement("persName");
						name.setTextContent(attValue);
						person.appendChild(name);
					} else if (attName == "id") {
						Element altGrp = docTEI.createElement("altGrp");
						Element alt = docTEI.createElement("alt");
						alt.setAttribute("type", attValue);
						person.appendChild(altGrp);
						altGrp.appendChild(alt);
					} else {
						if (Utils.isNotEmptyOrNull(attValue)) {
							Element n = docTEI.createElement("note");
							n.setTextContent(attValue);
							n.setAttribute("type", attName);
							person.appendChild(n);
						}
					}
				}
			}
		}
	}

	/**
	 * Construction de l'élément <strong>text</strong>.<br>
	 * Contient la transcription.
	 */
	public void setTextElement() {
		Element body = (Element) this.docTEI.getElementsByTagName("body").item(0);
		Element episode = (Element) this.docTRS.getElementsByTagName("Episode").item(0);
		Element divEpisode = Utils.createDivHead(this.docTEI);
		divEpisode.setAttribute("type", "Situation");
		divEpisode.setAttribute("subtype", "d0");
		body.appendChild(divEpisode);
		NamedNodeMap attrs = episode.getAttributes();
		if (attrs.getLength() != 0) {
			for (int i = 0; i < attrs.getLength(); i++) {
				Utils.setDivHeadAttr(this.docTEI, divEpisode, attrs.item(i).getNodeName(),
						attrs.item(i).getNodeValue());
			}
		}
		NodeList textContent = episode.getChildNodes();
		for (int i = 0; i < textContent.getLength(); i++) {
			Node section = textContent.item(i);
			if (section.getNodeName() == "Section") {
				this.addDivElement(divEpisode, section);
			}
		}
		setDurDate();
	}

	/**
	 * Ajoute les éléments <strong>div</strong> avec leurs attributs au document
	 * TEI à partir des informations contenues dans le document Transcriber.<br>
	 * Les éléments <strong>div</strong> correspondent aux
	 * <strong>section</strong> du format Transcriber.
	 * 
	 * @param text
	 *            L'élément <strong>text</strong> auquel se rattachent les
	 *            éléments <strong>div</strong>.
	 * @param section
	 *            Le noeud <strong>section</strong> issu du document Transcriber
	 */
	public void addDivElement(Element top, Node section) {
		Element div = Utils.createDivHead(docTEI);
		top.appendChild(div);
		NamedNodeMap attributes = section.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node att = attributes.item(i);
			String attName = att.getNodeName();
			String attValue = att.getNodeValue();
			if (attName == "type") {
				div.setAttribute(attName, attValue);
			} else if (attName == "topic") {
				div.setAttribute("subtype", attValue);
			}
			// Case startTime & endTime
			else if (attName == "startTime") {
				String startId = addTimeToTimeline(attValue);
				// div.setAttribute("start", startId);
				Utils.setDivHeadAttr(this.docTEI, div, "start", startId);
			}
		}
		this.setDivElement(section, div);
	}

	/**
	 * Ajoute les éléments <strong>u</strong> dans les <strong>div</strong> au
	 * document TEI à partir des informations contenues dans le document
	 * Transcriber.<br>
	 * Met à jour l'attribut "who" dans le cas ou le <strong>turn</strong> ne
	 * contient qu'un "speaker".<br>
	 * Les éléments <strong>u</strong> correspondent aux <strong>turn</strong>
	 * dans le document Transcriber.
	 * 
	 * @param section
	 *            L'élément <strong>section</strong> du document Transcriber.
	 * @param div
	 *            L'élément <strong>div</strong> à construire à partir de
	 *            l'élément <strong>section</strong>.
	 */
	public void setDivElement(Node section, Element div) {
		NodeList turns = section.getChildNodes();
		for (int i = 0; i < turns.getLength(); i++) {
			Node current = turns.item(i);
			if (current.getNodeName() == "Turn") {
				Element turn = (Element) current;
				String[] speakers = turn.getAttribute("speaker").split(" ");
				if (speakers.length == 1) {
					Element annotatedU = Utils.createAnnotationBloc(this.docTEI);
					div.appendChild(annotatedU);
					this.setU_Id(annotatedU);
					Utils.setAttrAnnotationBloc(this.docTEI, annotatedU, "who", turn.getAttribute("speaker"));
					this.set_AU_attributes(annotatedU, (Element) current);
					this.setElementUnique((Element) current, annotatedU, div);
				} else {
					this.buildDecompoTurnMultiSpk((Element) current, div, speakers);
				}
			}
		}
		Element sect = (Element) section;
		try {
			String endId = addTimeToTimeline(sect.getAttribute("endTime"));
			// div.setAttribute("end", endId);
			Utils.setDivHeadAttr(this.docTEI, div, "end", endId);
		} catch (Exception e) {
			System.out.println("Erreur dans le traitement des turns : " + e.getMessage());
		}
	}

	/**
	 * Met à jour les attributs de l'élément <strong>u</strong> à partir des
	 * attributs de l'élément <strong>turn</strong>.
	 * 
	 * @param u
	 *            L'élément <strong>u</strong> à mettre à jour.
	 * @param turn
	 *            L'élément <strong>turn</strong> du document Transcriber.
	 */
	public void set_AU_attributes(Element annotatedU, Element turn) {
		NamedNodeMap attrs = turn.getAttributes();
		for (int j = 0; j < attrs.getLength(); j++) {
			Node att = attrs.item(j);
			String attName = att.getNodeName();
			String attValue = att.getNodeValue();
			if (attName == "startTime") {
				String startId = addTimeToTimeline(attValue);
				Utils.setAttrAnnotationBloc(this.docTEI, annotatedU, "start", startId);
			} else if (attName == "endTime") {
				String endId = addTimeToTimeline(attValue);
				Utils.setAttrAnnotationBloc(this.docTEI, annotatedU, "end", endId);
			} else if (attName == "mode") {
				Utils.setAttrAnnotationBlocSupplement(this.docTEI, annotatedU, "mode", attValue);
			} else if (attName == "fidelity") {
				Utils.setAttrAnnotationBlocSupplement(this.docTEI, annotatedU, "fidelity", attValue);
			} else if (attName == "channel") {
				Utils.setAttrAnnotationBlocSupplement(this.docTEI, annotatedU, "channel", attValue);
			}
		}
	}

	/**
	 * Ajoute à l'élément <strong>u</strong> les éléments correspondant aux
	 * éléments contenus par l'élément <strong>turn</strong> dans le document
	 * Transcriber.<br>
	 * Traite le cas des <strong>turn</strong> contenant plus d'un "speaker":
	 * dans ce cas 2 éléments <strong>u</strong> distincts sont crées.
	 * 
	 * @param turn
	 *            L'élément <strong>turn</strong> à convertir.
	 * @param u
	 *            L'élément <strong>u</strong> à mettre à jour.
	 * @param div
	 *            Le père de l'élément <strong>u</strong> (un élément
	 *            <strong>div</strong>).
	 */
	public void setElementUnique(Element turn, Element annotatedU, Element div) {
		NodeList children = turn.getChildNodes();
		Element u = this.docTEI.createElement("u");
		Element seg = docTEI.createElement("seg");
		annotatedU.appendChild(u);
		String sync = "-1";
		Element spangrp = null;
		for (int i = 0; i < children.getLength(); i++) {
			Node elmt = children.item(i);
			String elmtName = elmt.getNodeName();
			String elmtValue = elmt.getNodeValue();
			NamedNodeMap attrs = children.item(i).getAttributes();
			String annotatedU_start = Utils.getAttrAnnotationBloc(annotatedU, "start");
			if (elmtName == "Sync") {
				sync = attrs.item(0).getNodeValue();
				String ref_sync = addTimeToTimeline(sync);
				if (sync != "-1" && Utils.isNotEmptyOrNull(annotatedU_start) && !ref_sync.equals(annotatedU_start)) {
					// System.out.printf("anchor (%s) (%s)%n", sync,
					// getTimeValue(annotatedU_start));
					this.addSynchro(sync, u);
					seg = docTEI.createElement("seg");
				}
			} else if (elmtName == "#text") {
				setTextTrsElement(elmtValue, sync, annotatedU, u, seg, spangrp, false);
			} else if (elmtName == "Comment") {
				spangrp = setCommentTrsElement(elmt, div, spangrp, annotatedU, u, seg);
			} else if (elmtName == "Background" || elmtName == "Event" || elmtName == "Vocal") {
				setEventsTrsElement(elmtName, annotatedU, u, seg, spangrp, attrs);
			}
		}
		addSpanGrp(spangrp, annotatedU);
	}

	public Element addComment(Element el, String infoType, String content) {
		Element spanGrp = docTEI.createElement("spanGrp");
		Element span = this.docTEI.createElement("span");
		spanGrp.setAttribute("type", infoType);
		span.setTextContent(content);
		spanGrp.appendChild(span);
		this.tiersNames.add(infoType);
		return spanGrp;
	}

	public void buildDecompoTurnMultiSpk(Element turn, Element div, String [] speakers) {
		ArrayList<Element> annotatedUs = new ArrayList<Element>();
		ArrayList<Element> spanGrps = new ArrayList<Element>();
		ArrayList<String> ids = new ArrayList<String>();
		// Création d'un annotU par speaker
		for (int i=0; i < speakers.length; i++) {
			Element au = Utils.createAnnotationBloc(this.docTEI);
			setU_Id(au);
			ids.add(Utils.getAttrAnnotationBloc(au, "xml:id"));
			Utils.setAttrAnnotationBloc(this.docTEI, au, "who", speakers[i]);
			div.appendChild(au);
			annotatedUs.add(au);
			set_AU_attributes(au, turn);
			Element u = this.docTEI.createElement("u");
			au.appendChild(u);
			Element spanGrp = null;
			spanGrps.add(spanGrp);
		}
		String sync = "-1";
		Element firstWho = (Element) turn.getElementsByTagName("Who").item(0);
		Element currentAU;
		Element currentSpanGrp;
		try {
			String sFirstWho = firstWho.getAttribute("nb");
			int nth = Integer.parseInt(sFirstWho) - 1;
			currentAU = annotatedUs.get(nth);
			currentSpanGrp = spanGrps.get(nth);
		} catch (Exception e) {
			currentAU = annotatedUs.get(0);
			currentSpanGrp = spanGrps.get(0);
		}
		Element u = (Element) currentAU.getElementsByTagName("u").item(0);
		NodeList turnChildren = turn.getChildNodes();
		Element seg;
		NodeList segs = u.getElementsByTagName("seg");
		int nbseg = segs.getLength();
		if (nbseg > 0) {
			seg = (Element)(segs.item(nbseg-1));
		} else {
			seg = docTEI.createElement("seg");
		}
		// addLinks(ids, annotatedUs);
		for (int i = 0; i < turnChildren.getLength(); i++) {
			Node elmt = turnChildren.item(i);
			String elmtName = elmt.getNodeName();
			String elmtValue = elmt.getNodeValue();
			NamedNodeMap attrs = turnChildren.item(i).getAttributes();
			if (elmtName == "Sync") {
				sync = attrs.item(0).getNodeValue();
			} else if (elmtName == "Who") {
				int nb = Integer.parseInt(((Element) elmt).getAttribute("nb")) -1;
				try {
					currentAU = annotatedUs.get(nb);
					currentSpanGrp = spanGrps.get(nb);
				} catch (Exception e) {
					currentAU = annotatedUs.get(0);
					currentSpanGrp = spanGrps.get(0);
				}
				u = (Element) currentAU.getElementsByTagName("u").item(0);
				seg = docTEI.createElement("seg");
				String ref_sync = addTimeToTimeline(sync);
				String startTurn = turn.getAttribute("startTime");
				String annotatedU_start = addTimeToTimeline(startTurn);
				if (Utils.isNotEmptyOrNull(annotatedU_start) && !ref_sync.equals(annotatedU_start))
					this.addSynchro(sync, u);
				u.appendChild(seg);
			} else if (elmtName == "#text") {
				setTextTrsElement(elmtValue, sync, currentAU, u, seg, currentSpanGrp, true);
			} else if (elmtName == "Comment") {
				currentSpanGrp = setCommentTrsElement(elmt, div, currentSpanGrp, currentAU, u, seg);
			} else if (elmtName == "Background" || elmtName == "Event" || elmtName == "Vocal") {
				setEventsTrsElement(elmtName, currentAU, u, seg, currentSpanGrp, attrs);
			}
		}
	}

	public void addSpanGrp(Element spangrp, Element annotatedU) {
		if (spangrp != null) {
			annotatedU.appendChild(spangrp);
		}
	}

	public void setTextTrsElement(String elmtValue, String sync, Element annotatedU, Element u, Element seg,
			Element spangrp, boolean link) {
		String annotatedU_start = Utils.getAttrAnnotationBloc(annotatedU, "start");
		if (!Utils.isNotEmptyOrNull(elmtValue.trim()) && !sync.equals("-1") && Utils.isNotEmptyOrNull(annotatedU_start)
				&& !sync.equals(getTimeValue(annotatedU_start)) && !link) {
			seg = docTEI.createElement("seg");
			u.appendChild(seg);
		} else {
			String content = elmtValue.trim();
			if (!content.isEmpty()) {
				String[] s = content.split("\\s");
				String segContent = "";
				for (String el : s) {
					if (el.matches("(\\s|^)((\\+(\\s|$)?){2,})(\\s|$)") || el.matches("(\\s|^)(\\/\\/\\/+)(\\s|$)")
							|| el.matches("(\\s|^)\\+(\\s|$)")) {
						Element pause = docTEI.createElement("pause");
						Node textContent = docTEI.createTextNode(segContent);
						seg.appendChild(textContent);
						if (Utils.isNotEmptyOrNull(segContent.trim())) {
							annotatedU.appendChild(u);
							addSpanGrp(spangrp, annotatedU);
							u.appendChild(seg);
						}
						seg.appendChild(pause);
						segContent = "";
						if (el.matches("(\\s|^)(\\+\\s?){3,}(\\s|$)") || el.matches("(\\s|^)/{3,}(\\s|$)")) {
							pause.setAttribute("type", "verylong");
						} else if (el.matches("(\\s|^)(\\+\\s?){2}(\\s|$)")) {
							pause.setAttribute("type", "long");
						} else if (el.matches("(\\s|^)\\+(\\s|$)")) {
							pause.setAttribute("type", "short");
						}
					} else {
						segContent += el += " ";
					}
				}
				if (Utils.isNotEmptyOrNull(segContent.trim())) {
					Node textContent = docTEI.createTextNode(segContent);
					seg.appendChild(textContent);
					u.appendChild(seg);
					annotatedU.appendChild(u);
					addSpanGrp(spangrp, annotatedU);
				}
			}
		}
	}

	public Element setCommentTrsElement(Node elmt, Element div, Element spanGrp, Element au, Element u, Element seg) {
		String comContent = ((Element) elmt).getAttribute("desc");
//		System.out.printf("setComment: %s%n", comContent);
		if (comContent.contains(":\t")) {
			try {
				String infoType = Utils.getInfoType(comContent);
				String infoContent = Utils.getInfo(comContent);
				if (infoType.startsWith("@")) {
					Element add = this.docTEI.createElement("add");
					add.setTextContent(infoContent);
					add.setAttribute("type", infoType.substring(1));
					div.appendChild(add);
				} else {
					spanGrp = addComment(au, infoType, infoContent);
				}
			} catch (Exception e) {
				spanGrp = addComment(au, "com", comContent);
			}
		} else {
			Element incident = this.docTEI.createElement("incident");
			incident.setAttribute("type", "comment");
			incident.setAttribute("subtype", "previous");
			Element desc = this.docTEI.createElement("desc");
			desc.setTextContent(((Element) elmt).getAttribute("desc"));
			incident.appendChild(desc);
			seg.appendChild(incident);
			u.appendChild(seg);
//				spanGrp = addComment(au, "com", comContent);
		}
		return spanGrp;
	}

	public void setEventsTrsElement(String elmtName, Element au, Element u, Element seg, Element spanGrp,
			NamedNodeMap attrs) {
		if (elmtName == "Background" || elmtName == "Event") {
			Element incident = this.docTEI.createElement("incident");
			this.setIncidentAttributes(incident, elmtName, attrs);
			seg.appendChild(incident);
			u.appendChild(seg);
			au.appendChild(u);
			addSpanGrp(spanGrp, au);
		} else if (elmtName == "Vocal") {
			Element vocal = this.docTEI.createElement("vocal");
			vocal.setTextContent(attrs.item(0).getNodeValue());
			seg.appendChild(vocal);
			u.appendChild(seg);
		}
	}

	/**
	 * Traitement des éléments <strong>sync</strong> du document Transcriber:
	 * <br>
	 * dans le cas où un élément <strong>sync</strong> est présent, on ajoute un
	 * élément anchor à l'élément au annotatedU qui le contient.
	 * 
	 * @param sync
	 *            La valeur de l'élément <strong>sync</strong>.
	 * @param e
	 *            AnnotatedU qui va contenir l'ancre <strong>sync</strong>
	 * @return Retourne la valeur de <strong>sync</strong>, réinitialisé.
	 */
	public String addSynchro(String sync, Element e) {
		if (sync != "-1") {
			Element anchor = docTEI.createElement("anchor");
			String synchId = addTimeToTimeline(sync);
			anchor.setAttribute("synch", synchId);
			e.appendChild(anchor);
			sync = "-1";
		}
		return sync;
	}

	/**
	 * Mise à jour (incrémentation) et attribution de l'identifiant des éléments
	 * <strong>u</strong>.
	 * 
	 * @param u
	 *            L'élément <strong>annotatedU</strong> auquel on attribut
	 *            l'identifiant.
	 */
	public void setU_Id(Element annotatedU) {
		String IdU = "au" + utteranceId;
		Utils.setAttrAnnotationBloc(this.docTEI, annotatedU, "xml:id", IdU);
		utteranceId++;
	}

	/**
	 * Récupération des locuteurs d'un <strong>turn</strong>.
	 * 
	 * @param turn
	 *            L'élément turn dont on veut récupérer les locuteurs.
	 * @return Retourne la liste des locuteurs: liste des identifiants.
	 */
	public String[] getSpeakers(Element turn) {
		String spk = turn.getAttribute("speaker");
		return spk.split(" ");
	}

	/**
	 * Mise à jour des attributs et éléments de l'élément
	 * <strong>incident</strong> dans le document TEI.<br>
	 * Cet élément correspond à 2 éléments dans le format Transcriber:
	 * <strong>event</strong> et <strong>background</strong>.
	 * 
	 * @param incident
	 *            L'élément <strong>incident</strong> à mettre à jour.
	 * @param type
	 *            Le type de l'élément correspondant dans le format Transcriber:
	 *            "event" ou "background".
	 * @param attrs
	 *            La liste des attributs de l'élément provenant du document
	 *            Transcriber.
	 */
	public void setIncidentAttributes(Element incident, String type, NamedNodeMap attrs) {
		if (type == "Background") {
			incident.setAttribute("type", "background");
			for (int i = 0; i < attrs.getLength(); i++) {
				String attName = attrs.item(i).getNodeName();
				String attValue = attrs.item(i).getNodeValue();
				if (attName == "time") {
					String startId = addTimeToTimeline(attValue);
					Element desc = this.docTEI.createElement("desc");
					desc.setAttribute("type", "time");
					desc.setTextContent(startId);
					incident.appendChild(desc);
				} else if (attName == "type") {
					// noise|lexical|pronounce|language|entities
					incident.setAttribute("subtype", attValue);
				} else if (attName == "level") {
					Element desc = this.docTEI.createElement("desc");
					desc.setAttribute("type", "level");
					desc.setTextContent(attValue);
					incident.appendChild(desc);
				}
			}
		} else {
			for (int i = 0; i < attrs.getLength(); i++) {
				String attName = attrs.item(i).getNodeName();
				String attValue = attrs.item(i).getNodeValue();
				if (attName == "type") {
					// noise|lexical|pronounce|language|entities
					incident.setAttribute("type", attValue);
				} else if (attName == "desc") {
					Element desc = this.docTEI.createElement("desc");
					desc.setTextContent(attValue);
					incident.appendChild(desc);
				} else if (attName == "extent") {
					incident.setAttribute("subtype", attValue);
				}
			}
		}
	}

	public String getTimeValue(String timeId) {
		return times.get(Integer.parseInt(timeId.split("#T")[1]));
	}

	public String addTimeToTimeline(String time) {
		String id = "";
		if (!Utils.isNotEmptyOrNull(time)) {
			return time;
		}
		Double t = Double.parseDouble(time);
		if (t > maxTime)
			maxTime = t;
		if (Float.parseFloat(time) == 0) {
			id = "T0";
		} else if (times.contains(time)) {
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
	}

	public void addTemplateDesc() {

		Element fileDesc = (Element) this.docTEI.getElementsByTagName("fileDesc").item(0);
		Element notesStmt = (Element) fileDesc.getElementsByTagName("notesStmt").item(0);
		Element templateNote = docTEI.createElement("note");
		templateNote.setAttribute("type", "TEMPLATE_DESC");
		notesStmt.appendChild(templateNote);

		// Ajout des locuteurs dans les templates
		Element particDesc = (Element) this.docTEI.getElementsByTagName("particDesc").item(0);
		NodeList persons = particDesc.getElementsByTagName("person");
		for (int i = 0; i < persons.getLength(); i++) {
			Element person = (Element) persons.item(i);
			Element note = docTEI.createElement("note");

			Element noteType = docTEI.createElement("note");
			noteType.setAttribute("type", "type");
			noteType.setTextContent("-");
			note.appendChild(noteType);

			Element noteParent = docTEI.createElement("note");
			noteParent.setAttribute("type", "parent");
			noteParent.setTextContent("-");
			note.appendChild(noteParent);
			if (person.getElementsByTagName("alt").getLength() > 0) {
				Element alt = (Element) person.getElementsByTagName("alt").item(0);

				Element noteCode = docTEI.createElement("note");
				noteCode.setAttribute("type", "code");
				noteCode.setTextContent(alt.getAttribute("type"));
				note.appendChild(noteCode);

			}
			templateNote.appendChild(note);
		}
		for (String tierName : this.tiersNames) {
			Element note = docTEI.createElement("note");

			Element noteCode = docTEI.createElement("note");
			noteCode.setAttribute("type", "code");
			noteCode.setTextContent(tierName);
			note.appendChild(noteCode);

			Element noteType = docTEI.createElement("note");
			noteType.setAttribute("type", "type");
			noteType.setTextContent(LgqType.SYMB_ASSOC);
			note.appendChild(noteType);

			String parent = Utils.ANNOTATIONBLOC;
			if (tierName.toLowerCase().equals(Utils.ANNOTATIONBLOC)) {
				parent = "-";
			}
			Element noteParent = docTEI.createElement("note");
			noteParent.setAttribute("type", "parent");
			noteParent.setTextContent(parent);
			note.appendChild(noteParent);

			templateNote.appendChild(note);
		}
	}

	public void addTimeline() {
		Utils.sortTimeline(timeElements);
		Element timeline = (Element) docTEI.getElementsByTagName("timeline").item(0);
		for (Element when : timeElements) {
			timeline.appendChild(when);
		}
	}

	public void setDurDate() {
		Element sourceDesc = (Element) this.docTEI.getElementsByTagName("sourceDesc").item(0);
		if (this.docTEI.getElementsByTagName("recordingStmt").getLength() == 0) {
			Element recordingStmt = this.docTEI.createElement("recordingStmt");
			sourceDesc.appendChild(recordingStmt);
			Element recording = this.docTEI.createElement("recording");
			recordingStmt.appendChild(recording);
		}
		Element recording = (Element) this.docTEI.getElementsByTagName("recording").item(0);
		Element date = this.docTEI.createElement("date");
		recording.appendChild(date);
		String d = this.rootTRS.getAttribute("version_date");
		if (Utils.isNotEmptyOrNull(d)) {
			date.setTextContent(d);
		}
		date.setAttribute("dur", String.valueOf(maxTime));
		// date.setTextContent(date.getTextContent()); // ??
	}

	/**
	 * Programme principal: convertit un fichier au format Transcriber en un
	 * fichier au format TEI.
	 * 
	 * @param args
	 *            Liste des aruments du programme.
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		TierParams.printVersionMessage();
		TranscriberToTei tr = new TranscriberToTei();
		tr.mainCommand(args, ".trs", Utils.EXT, "Description: TranscriberToTei convertit un fichier au format Transcriber en un fichier au format TEI", 2);
		// if (!(Utils.validFileFormat(input, ".trs") || Utils.validFileFormat(input, ".trs.xml") || Utils.validFileFormat(input, ".xml"))) {
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		// System.out.println("Lecture de " + input);
		transform(new File(input), options);
		Utils.setDocumentName(docTEI, Utils.lastname(output));
		Utils.createFile(output, docTEI);
		// System.out.println("New file created " + output);
	}
}
