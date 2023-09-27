package fr.ortolang.teicorpo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TeiToElan extends GenericMain {

	TeiToPartition ttp = null;

	// Extension du fichier de sortie: .eaf
	public static String EXT = ".eaf";

	// Nom du fichier teiml à convertir
	String inputName;
	// Nom du fichier de sortie
	String outputName;

	// Document elan
	Document elanDoc;
	// Element ANNOTATION_DOCUMENT: racine du document Elan)
	Element annot_doc;
	Element time_order;

	// maps for the cv
	Map<String, HashMap<String, String>> cvs;

	// Document TEI à lire
	public Document teiDoc;
	// acces XpathTest
	public XPathFactory teiXPathfactory;
	public XPath teiXpath;

	// Validation du document Tei par la dtd
	boolean validation = false;

	long tstart = 0;

	// new timeline
	Map<String, String> elanTimeline = new TreeMap<String, String>();
	// not to be used any more - int lastIdTimeline = 0; // last id included in the map

	private boolean setDefault = false;

	static Element getFirstElementByTagName(Document doc, String name) throws Exception {
		NodeList nl = doc.getElementsByTagName(name);
		if (nl.getLength() < 1) {
			System.err.printf("No element for %s%n", name);
			throw new Exception("getFistElementByTagName");
		}
		return (Element)nl.item(0);
	}

	// Constructeur à partir du nom du fichier TEI et du nom du fichier de
	// sortie au format Elan
	public boolean transform(String inputName, String outputName, TierParams optionsTei) throws Exception {
		ttp = new TeiToPartition();
		if (optionsTei == null) optionsTei = new TierParams();
		DocumentBuilderFactory factory = null;
//		tstart = Utils.timeStamp("start", 0);
		try {
			File teiFile = new File(inputName);
			factory = DocumentBuilderFactory.newInstance();
			TeiDocument.setDTDvalidation(factory, optionsTei.dtdValidation);
			DocumentBuilder builder = factory.newDocumentBuilder();
			teiDoc = builder.parse(teiFile);
			teiXPathfactory = XPathFactory.newInstance();
			teiXpath = teiXPathfactory.newXPath();
			teiXpath.setNamespaceContext(new NamespaceContext() {
				public String getNamespaceURI(String prefix) {
					System.out.println("prefix called " + prefix);
					if (prefix == null) {
						throw new IllegalArgumentException("No prefix provided!");
					} else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
						System.out.println("default prefix called");
						return "http://www.tei-c.org/ns/1.0";
					} else if (prefix.equals("tei")) {
						System.out.println("tei prefix called");
						return "http://www.tei-c.org/ns/1.0";
					} else if (prefix.equals("xsi")) {
						return "http://www.w3.org/2001/XMLSchema-instance";
					} else {
						return XMLConstants.NULL_NS_URI;
					}
				}

				public Iterator<String> getPrefixes(String val) {
					return null;
				}

				public String getPrefix(String uri) {
					return null;
				}
			});
			ttp.init(teiXpath, teiDoc, optionsTei);
		} catch(FileNotFoundException e) {
			System.err.println("The file " + inputName + " doesn't exist.");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		this.inputName = inputName;
		this.outputName = outputName;
//		tstart = Utils.timeStamp("load", tstart);
		outputWriter();
//		tstart = Utils.timeStamp("outputWriter", tstart);
		conversion();
//		tstart = Utils.timeStamp("conversion", tstart);
		return true;
	}

	// Ecriture du fichier de sortie
	public boolean outputWriter() throws Exception {
		if (!ttp.optionsOutput.model.isEmpty()) {
			// use the model as a starting template
			System.out.printf("Using template: %s%n", ttp.optionsOutput.model);
			DocumentBuilderFactory templateFactory = null;
			try {
				File templateFile = new File(ttp.optionsOutput.model);
				templateFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = templateFactory.newDocumentBuilder();
				elanDoc = builder.parse(templateFile);
			} catch(FileNotFoundException e) {
				System.err.println("The template " + ttp.optionsOutput.model + " doesn't exist.");
				return false;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			annot_doc = getFirstElementByTagName(elanDoc, "ANNOTATION_DOCUMENT");
		} else {
			// create a new file from scratch
			elanDoc = null;
			DocumentBuilderFactory factory = null;
			try {
				factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				elanDoc = builder.newDocument();
				annot_doc = elanDoc.createElement("ANNOTATION_DOCUMENT");
				elanDoc.appendChild(annot_doc);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		return true;
	}

	// Conversion du fichier TEI vers Elan
	public void conversion() throws Exception {
		// Construction de l'en-tête
		buildHeader();
		// Constructing controlled vocabularies from the TEI file
		List<Element> cvList = buildControlledVocabularies();
		// add controlled vocabularies
		for (Element e : cvList) {
			if (!ttp.optionsOutput.model.isEmpty()) {
				String ID = e.getAttribute("CV_ID");
				// if a model exist, all only cv which do not exist ? or replace old ones ?
				Node n = annot_doc.getOwnerDocument().getElementById(ID);
				if (n == null) {
					annot_doc.appendChild(e);
				} else {
					// nothing or replace ? here it is replacing.
					annot_doc.removeChild(n);
					annot_doc.appendChild(e);
				}
			} else {
				annot_doc.appendChild(e);
			}
		}
		// timeline
		buildTimeline(time_order);
//		tstart = Utils.timeStamp("timeline", tstart);
		// Building the tiers
		buildTiers();
//		tstart = Utils.timeStamp("tiers", tstart);
		// Construction of linguistic types
		buildLgqTypes();
		if (ttp.optionsOutput.model.isEmpty()) {
			// Construction of Elan constraints
			buildConstraints();
		} // otherwise they are in the template
	}

	/**
	 * Get a date of the form "2014-03-05T15:06:29+01:00".
	 * 
	 * @return
	 */
	private static String getDate() {
		SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		final Calendar calendar = Calendar.getInstance();
		String dateString = dateFmt.format(calendar.getTime());

		return addTimezone(calendar, dateString);
	}

	private static String addTimezone(Calendar calendar, String dateString) {
		int GMTOffsetInMinutes = calendar.getTimeZone().getRawOffset() / (60 * 1000);

		char sign = '+';

		if (GMTOffsetInMinutes < 0) {
			sign = '-';
			GMTOffsetInMinutes = -GMTOffsetInMinutes;
		}

		final int hours = GMTOffsetInMinutes / 60;
		final int minutes = GMTOffsetInMinutes % 60;

		String strOffset = String.format("%02d:%02d", hours, minutes);

		return dateString + sign + strOffset;
	}

	// Information contenues dans l'élément annotation_doc et dans l'élément
	// header
	public void buildHeader() throws Exception {
		// first compute date
		String date = "";
		try {
			date = (String) this.teiXpath.compile("//appInfo/date/text()").evaluate(this.teiDoc, XPathConstants.STRING);
		} catch (Exception e) {
			date = "";
		}
		if (date.isEmpty()) {
			date = getDate();
		}
		// then insert
		if (!ttp.optionsOutput.model.isEmpty()) {
			Element header = getFirstElementByTagName(elanDoc, "HEADER");
			if (ttp.optionsOutput.test)
				annot_doc.setAttribute("DATE", "2018-09-10");
			else
				annot_doc.setAttribute("DATE", date);
			annot_doc.setAttribute("AUTHOR", "TEI_CORPO Converter");
			annot_doc.setAttribute("FORMAT", "2.8");
			annot_doc.setAttribute("VERSION", "2.8");
			// Element timeline
			time_order = getFirstElementByTagName(elanDoc, "TIME_ORDER");
		} else {
			annot_doc.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			annot_doc.setAttribute("xsi:noNamespaceSchemaLocation", "http://www.mpi.nl/tools/elan/EAFv2.8.xsd");
			Element header = elanDoc.createElement("HEADER");
			if (ttp.optionsOutput.test)
				annot_doc.setAttribute("DATE", "2018-09-10");
			else
				annot_doc.setAttribute("DATE", date);
			annot_doc.setAttribute("AUTHOR", "TEI_CORPO Converter");
			annot_doc.setAttribute("FORMAT", "2.8");
			annot_doc.setAttribute("VERSION", "2.8");
			annot_doc.appendChild(header);
			buildHeaderElement(header);
			// Element timeline
			time_order = elanDoc.createElement("TIME_ORDER");
			annot_doc.appendChild(time_order);
		}
	}

	// Complétion des informations du header
	void buildHeaderElement(Element header) {
		try {
			// Attribut de ANNOTATION_DOCUMENT
			Element teiHeader = (Element) this.teiDoc.getElementsByTagName("teiHeader").item(0);
			/*
			 * Element app =
			 * (Element)teiHeader.getElementsByTagName("application/appInfo").
			 * item(0); annot_doc.setAttribute("FORMAT",
			 * app.getAttribute("version")); annot_doc.setAttribute("VERSION",
			 * app.getAttribute("version"));
			 */
			// Mise à jour du header
			// Attributs
			// media_file + elements media
			// besoin units et time
			XPathExpression expr = this.teiXpath.compile("//recordingStmt/recording/media");
			NodeList medias = (NodeList) expr.evaluate(this.teiDoc, XPathConstants.NODESET);
			if (medias.getLength() > 0) {
				header.setAttribute("MEDIA_FILE", "");
				header.setAttribute("TIME_UNITS", "milliseconds");
				for (int i = 0; i < medias.getLength(); i++) {
					Element elt = (Element) medias.item(i);
					String url = elt.getAttribute("url");
					String mimeType = elt.getAttribute("mimeType");
					File mediaFile = new File(url);
					String mediaName = mediaFile.getName();
					Element eafMedia = elanDoc.createElement("MEDIA_DESCRIPTOR");
					eafMedia.setAttribute("MEDIA_URL", url);
					eafMedia.setAttribute("MIME_TYPE", mimeType);
					eafMedia.setAttribute("RELATIVE_MEDIA_URL", "./" + mediaName);
					header.appendChild(eafMedia);
				}
			}
			// properties : notes additionnelles
			NodeList notes = teiHeader.getElementsByTagName("note");
			for (int j = 0; j < notes.getLength(); j++) {
				Element note = (Element) notes.item(j);
				if (note.getAttribute("type").equals("COMMENTS_DESC")) {
					NodeList propertyNotes = note.getElementsByTagName("note");
					for (int y = 0; y < propertyNotes.getLength(); y++) {
						Element propertyNote = (Element) propertyNotes.item(y);
						Element property = elanDoc.createElement("PROPERTY");
						property.setAttribute("NAME", propertyNote.getAttribute("type"));
						property.setTextContent(propertyNote.getTextContent());
						header.appendChild(property);
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error in processing the header");
		}
	}

	/*
	String nottobeusedtimelineValueOf(String time) {
		if (time.isEmpty())
			return "";
		String nt = newTimeline.get(time);
		if (nt != null)
			return nt;
		else {
			lastIdTimeline++;
			nt = "T" + lastIdTimeline;
			newTimeline.put(time, nt);
			return nt;
		}
	}
	*/

	// Remplissage de la timeline à partir de la timeline au format TEI : copie
	// exacte: mêmes identifiants et valeurs
	void buildTimeline(Element time_order) {
		try {
			// initializes all values in the timeline
			for (Map.Entry<String, ArrayList<Annot>> entry : ttp.tiers.entrySet()) {
				System.out.printf("ENTRY: %s%n", entry.getKey());
				for (Annot a : entry.getValue()) {
					if (a.timereftype.equals("time")) {
						// System.out.printf("ITEM: %s%n", a.toString());
						if (!Utils.isNotEmptyOrNull(a.start)) continue;
						if (!Utils.isNotEmptyOrNull(a.end)) continue;
						elanTimeline.put(a.startStamp, Utils.timestamp1000(a.start));
						elanTimeline.put(a.endStamp, Utils.timestamp1000(a.end));
						// System.out.printf("ITEM2: %s%n", a.toString());
					}
					// else if (a.timereftype.equals("ref")){}
				}
			}

			// write the timeline
			// Get a set of the entries
			Set set = elanTimeline.entrySet();
			// Get an iterator
			Iterator it = set.iterator();
			// write elements
			while (it.hasNext()) {
				Map.Entry me = (Map.Entry) it.next();
				// System.out.print("Key is: "+me.getKey() + " & ");
				// System.out.println("Value is: "+me.getValue());
				Element time_slot = elanDoc.createElement("TIME_SLOT");
				time_slot.setAttribute("TIME_SLOT_ID", (String) me.getKey());
				time_slot.setAttribute("TIME_VALUE", (String) me.getValue());
				time_order.appendChild(time_slot);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error in processing the timeline: " + e.toString());
		}
	}

	// Elements linguistic_type
	void buildLgqTypes() {
		if (setDefault == true) {
			Element lgqType = elanDoc.createElement("LINGUISTIC_TYPE");
			lgqType.setAttribute("LINGUISTIC_TYPE_ID", "default");
			lgqType.setAttribute("TIME_ALIGNABLE", "true");
			lgqType.setAttribute("GRAPHIC_REFERENCES", "false");
			annot_doc.appendChild(lgqType);
		}
		Set<String> namesLgqTypes = new HashSet<String>();
		for (int j = 0; j < ttp.tierInfos.size(); j++) {
			TierInfo ti = (TierInfo) ttp.tierInfos.get(j);
			// if the type does not exist, and it is not already in the template
			if (!namesLgqTypes.contains(ti.linguistType.lgq_type_id) && getElementByAttribute(annot_doc.getOwnerDocument(), "LINGUISTIC_TYPE", "LINGUISTIC_TYPE_ID", ti.linguistType.lgq_type_id) == null) {
				System.out.printf("Creating linguistic type: %s for %s%n", ti.linguistType.lgq_type_id, ti.tier_id);
				namesLgqTypes.add(ti.linguistType.lgq_type_id);
				Element lgqType = elanDoc.createElement("LINGUISTIC_TYPE");
				lgqType.setAttribute("LINGUISTIC_TYPE_ID", ti.linguistType.lgq_type_id);
				if (!ti.linguistType.constraint.equals("-"))
					lgqType.setAttribute("CONSTRAINTS", ti.linguistType.constraint);
				if (LgqType.isTimeType(ti.linguistType.constraint))
					lgqType.setAttribute("TIME_ALIGNABLE", "true");
				else
					lgqType.setAttribute("TIME_ALIGNABLE", "false");
				if (Utils.isNotEmptyOrNull(ti.linguistType.cv_ref))
					lgqType.setAttribute("CONTROLLED_VOCABULARY_REF", ti.linguistType.cv_ref);
				lgqType.setAttribute("GRAPHIC_REFERENCES",
						Utils.isNotEmptyOrNull(ti.linguistType.graphic_ref) ? ti.linguistType.graphic_ref : "false");
				annot_doc.appendChild(lgqType);
			}
		}
	}

	private Element getElementByAttribute(Document ownerDocument, String tagname, String attrname, String attrcontent) {
		NodeList nl = ownerDocument.getElementsByTagName(tagname);
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			Element e = (Element) nl.item(i);
			if (e.getAttribute(attrname).equals(attrcontent)) return e;
		}
		return null;
	}

	// Elements constraint : always the same values in Elan files
	void buildConstraints() {
		Element constraintTimeDiv = elanDoc.createElement("CONSTRAINT");
		constraintTimeDiv.setAttribute("DESCRIPTION",
				"Time subdivision of parent annotation's time interval, no time gaps allowed within this interval");
		constraintTimeDiv.setAttribute("STEREOTYPE", "Time_Subdivision");
		Element constraintSymbDiv = elanDoc.createElement("CONSTRAINT");
		constraintSymbDiv.setAttribute("DESCRIPTION",
				"Symbolic subdivision of a parent annotation. Annotations refering to the same parent are ordered");
		constraintSymbDiv.setAttribute("STEREOTYPE", "Symbolic_Subdivision");
		Element constraintSymbAssoc = elanDoc.createElement("CONSTRAINT");
		constraintSymbAssoc.setAttribute("DESCRIPTION", "1-1 association with a parent annotation");
		constraintSymbAssoc.setAttribute("STEREOTYPE", "Symbolic_Association");
		Element constraintInclIn = elanDoc.createElement("CONSTRAINT");
		constraintInclIn.setAttribute("DESCRIPTION",
				"Time alignable annotations within the parent annotation's time interval, gaps are allowed");
		constraintInclIn.setAttribute("STEREOTYPE", "Included_In");
		annot_doc.appendChild(constraintTimeDiv);
		annot_doc.appendChild(constraintSymbDiv);
		annot_doc.appendChild(constraintSymbAssoc);
		annot_doc.appendChild(constraintInclIn);
	}

	// Reconstruction du vocabulaire contrôlé
	List<Element> buildControlledVocabularies() {
		List<Element> listCV = new ArrayList<Element>();
		cvs = new HashMap<String, HashMap<String, String>>();
		if (this.teiDoc.getElementsByTagName("textClass").getLength() > 0) {
			Element textClass = (Element) this.teiDoc.getElementsByTagName("textClass").item(0);
			NodeList keywordsList = textClass.getElementsByTagName("keywords");
			for (int i = 0; i < keywordsList.getLength(); i++) {
				Element keywords = (Element) keywordsList.item(i);
				String ID = keywords.getAttribute("scheme");
				HashMap<String, String> cvn = new HashMap<String, String>();
				Element controlled_voc = elanDoc.createElement("CONTROLLED_VOCABULARY");
				controlled_voc.setAttribute("CV_ID", ID);
				cvs.put(keywords.getAttribute("scheme"), cvn);
				Element description = elanDoc.createElement("DESCRIPTION");
				description.setTextContent(keywords.getAttribute("style"));
				description.setAttribute("LANG_REF", keywords.getAttribute("xml:lang"));
				controlled_voc.appendChild(description);
				NodeList terms = keywords.getElementsByTagName("term");
				for (int j = 0; j < terms.getLength(); j++) {
					Element term = (Element) terms.item(j);
					Element cvEntry = elanDoc.createElement("CV_ENTRY_ML");
					cvEntry.setAttribute("CVE_ID", "cveid" + j);
					Element cveValue = elanDoc.createElement("CVE_VALUE");
					cveValue.setTextContent(term.getTextContent());
					cvn.put(term.getTextContent(), "cveid" + j);
					cveValue.setAttribute("DESCRIPTION", term.getAttribute("type"));
					cveValue.setAttribute("LANG_REF", term.getAttribute("xml:lang"));
					cvEntry.appendChild(cveValue);
					controlled_voc.appendChild(cvEntry);
				}
				listCV.add(controlled_voc);
			}
		}
		return listCV;
	}

	// Ajout des attributs des élement TIER
	private String setTierAtt(Element tier, String type) {
		//System.out.printf("setTierAtt(type): %s%n", type);
		for (TierInfo ti : ttp.tierInfos) {
			if (ti.tier_id.equals(type)) {
				if (Utils.isNotEmptyOrNull(ti.participant))
					tier.setAttribute("PARTICIPANT", ti.participant);
				if (Utils.isNotEmptyOrNull(ti.lang))
					tier.setAttribute("DEFAULT_LOCALE", ti.lang);
				if (Utils.isNotEmptyOrNull(ti.annotator))
					tier.setAttribute("ANNOTATOR", ti.annotator);
				if (Utils.isNotEmptyOrNull(ti.lang_ref))
					tier.setAttribute("LANG_REF", ti.lang_ref);
				if (Utils.isNotEmptyOrNull(ti.parent) && !ti.parent.equals("-")) {
					tier.setAttribute("PARENT_REF", ti.parent);
					// System.out.printf("HH: %s %s %s %n", tier.getTagName(), type, ti.parent);
					/*
					// chercher dans newTiers le nouveau nom du parent
					for (Map.Entry<String, NewTier> entry : ttp.newTiers.entrySet()) {
						if (entry.getValue().oldID.equals(ti.parent)) {
							tier.setAttribute("PARENT_REF", entry.getValue().newID);
							// on change l'ancien
						}
					}
					*/
				}
				if (Utils.isNotEmptyOrNull(ti.linguistType.lgq_type_id))
					tier.setAttribute("LINGUISTIC_TYPE_REF", ti.linguistType.lgq_type_id);
				else {
					tier.setAttribute("LINGUISTIC_TYPE_REF", "default");
					setDefault = true;
				}
				return ti.linguistType.cv_ref;
			}
		}
		//System.out.printf("setTierAtt(type): default%n");
		tier.setAttribute("LINGUISTIC_TYPE_REF", "default");
		setDefault = true;
		return "";
	}

	private String setTierAtt(Element tier, NewTier newTier, String type) {
		//System.out.printf("setTierAtt(new): %s %s%n", newTier.toString(), type);
		for (TierInfo ti : ttp.tierInfos) {
			// System.out.printf("---: %s%n", ti.toString());
			if (ti.tier_id.equals(newTier.oldID)) {
				if (Utils.isNotEmptyOrNull(ti.participant))
					tier.setAttribute("PARTICIPANT", ti.participant);
				if (Utils.isNotEmptyOrNull(ti.lang))
					tier.setAttribute("DEFAULT_LOCALE", ti.lang);
				if (Utils.isNotEmptyOrNull(ti.annotator))
					tier.setAttribute("ANNOTATOR", ti.annotator);
				if (Utils.isNotEmptyOrNull(ti.lang_ref))
					tier.setAttribute("LANG_REF", ti.lang_ref);
				if (Utils.isNotEmptyOrNull(newTier.lingType))
					tier.setAttribute("LINGUISTIC_TYPE_REF", newTier.lingType);
				else {
					tier.setAttribute("LINGUISTIC_TYPE_REF", "default");
					setDefault = true;
				}
				// System.out.printf("1KK: %s (%s)%n", ti.toString(), newTier.toString());
				if (Utils.isNotEmptyOrNull(ti.parent)) {
					//System.out.printf("WW: %s %s %s %s%n", newTier.oldID, ti.parent, type, tier.getAttribute("LINGUISTIC_TYPE_REF"));
					tier.setAttribute("PARENT_REF", newTier.parent);
//					int p = type.indexOf("-");
//					if (p >= 1) {
//						tier.setAttribute("PARENT_REF", type.substring(0, p));
//					} else {
//						// valeur par defaut ?
//						tier.setAttribute("PARENT_REF", ti.parent);
//						// s'il n'y en a pas (même nom)
//						// chercher dans newTiers le nouveau nom du parent
//						for (Map.Entry<String, NewTier> entry : ttp.newTiers.entrySet()) {
//							//System.out.printf("++: %s %s %n", newTier.oldID, entry.getValue().newID);
//							if (entry.getValue().oldID.equals(ti.parent)) {
//								tier.setAttribute("PARENT_REF", entry.getValue().newID);
//								// on change l'ancien
//								System.out.printf("KK: %s %s %n", newTier.oldID, entry.getValue().newID);
//							}
//						}
//					}
				}
				return ti.linguistType.cv_ref;
			}
		}
		//System.out.printf("setTierAtt(new): default%n");
		tier.setAttribute("LINGUISTIC_TYPE_REF", "default");
		setDefault = true;
		return "";
	}

	// Tiers...
	void buildTiers() {
		// ttp.getTiers();
		boolean norm =  ttp.optionsOutput.normalize.equals("none") ? false : true;
		String origformat = ttp.originalFormat();
		NodeList nl = this.annot_doc.getChildNodes();
		for (Map.Entry<String, ArrayList<Annot>> entry : ttp.tiers.entrySet()) {
			String nameOfTier = entry.getKey();
			Element tier = null;
			if (!ttp.optionsOutput.model.isEmpty()) {
				// try do find a tier: "//TIER[TIER_ID='" + type + "']";
				for (int i = 0; i < nl.getLength(); i++) {
					Node elt = (Node) nl.item(i);
					if (elt.getNodeName().equals("TIER") && ((Element)elt).getAttribute("TIER_ID").equals(nameOfTier)) {
						// System.out.printf("Existing Tier ENTRY: %s%n", type);
						tier = (Element) elt;
						break;
					}
				}
			}

			if (tier == null) {
				// not found or no template: create new tier
				tier = elanDoc.createElement("TIER");
				// System.out.printf("New Tier ENTRY: %s%n", type);
				tier.setAttribute("TIER_ID", nameOfTier);
				this.annot_doc.appendChild(tier);
			}

//			tstart = Utils.timeStamp("bt:" + entry.getKey() + " " + entry.getValue().size(), tstart);
			String cvref;
			if (ttp.newTiers.containsKey(nameOfTier)) {
				// System.out.printf("0KK: %s %s (%s)%n", tier.getNodeName(), type, ttp.newTiers.get(type));
				cvref = setTierAtt(tier, ttp.newTiers.get(nameOfTier), nameOfTier);
			} else
				// System.out.printf("0HH: %s %s%n", tier.getNodeName(), type);
				cvref = setTierAtt(tier, nameOfTier);
			for (Annot a : entry.getValue()) {
				// System.out.println(a.toString());
				Element annot = elanDoc.createElement("ANNOTATION");
				if (a.timereftype.equals("time")) {
//					tstart = Utils.timeStamp("time:" + a.name, tstart);
					// System.out.printf("TIME: (%s) %s%n", a.timereftype, a.toString());
					Element align_annot = elanDoc.createElement("ALIGNABLE_ANNOTATION");
					align_annot.setAttribute("ANNOTATION_ID", a.id);
					align_annot.setAttribute("TIME_SLOT_REF1", a.startStamp);
					align_annot.setAttribute("TIME_SLOT_REF2", a.endStamp);
					// System.out.printf("ANNOT ID: (%s) TIME1 %s TIME2 %s%n", a.id, createTimeStamp(a.id, a.start), createTimeStamp(a.id, a.end));
					// System.out.printf("STAMP ANNOT ID: (%s) TIME1 %s TIME2 %s%n", a.id, a.startStamp, a.endStamp);
					annot.appendChild(align_annot);
					Element annotationValue = elanDoc.createElement("ANNOTATION_VALUE");
					String str = a.getContent(ttp.optionsOutput.rawLine);
//					tstart = Utils.timeStamp("time2:" + a.name, tstart);
					if (norm != false)
						// is it a top tier ?
						str = (a.topParent == "-") ? NormalizeSpeech.parseText(str, origformat, ttp.optionsOutput) : str;
					annotationValue.setTextContent(str);
//					tstart = Utils.timeStamp("time3:" + a.name, tstart);
					if (Utils.isNotEmptyOrNull(cvref)) {
						Map<String, String> cvi = cvs.get(cvref);
						String cve = cvi.get(a.getContent(ttp.optionsOutput.rawLine));
						if (Utils.isNotEmptyOrNull(cve))
							align_annot.setAttribute("CVE_REF", cve);
					}
					align_annot.appendChild(annotationValue);
//					tstart = Utils.timeStamp("time append:" + a.name, tstart);
					annot.appendChild(align_annot);
				} else if (a.timereftype.equals("ref")) {
					Element ref_annot = elanDoc.createElement("REF_ANNOTATION");
//					tstart = Utils.timeStamp("ref:" + a.name, tstart);
//					System.out.printf("REF: (%s) %s%n", a.timereftype, a.toString());
					ref_annot.setAttribute("ANNOTATION_ID", a.id);
					ref_annot.setAttribute("ANNOTATION_REF", a.link);
					if (Utils.isNotEmptyOrNull(a.previous))
						ref_annot.setAttribute("PREVIOUS_ANNOTATION", a.previous);
					annot.appendChild(ref_annot);
					Element annotationValue = elanDoc.createElement("ANNOTATION_VALUE");
					String str = a.getContent(ttp.optionsOutput.rawLine);
//					tstart = Utils.timeStamp("ref2:" + a.name, tstart);
					if (norm != false)
						// is it a top tier ?
						str = (a.topParent == "-") ? NormalizeSpeech.parseText(str, origformat, ttp.optionsOutput) : str;
					annotationValue.setTextContent(str);
//					tstart = Utils.timeStamp("ref3:" + a.name, tstart);
					if (Utils.isNotEmptyOrNull(cvref)) {
						Map<String, String> cvi = cvs.get(cvref);
						String cve = cvi.get(a.getContent(ttp.optionsOutput.rawLine));
						if (Utils.isNotEmptyOrNull(cve))
							ref_annot.setAttribute("CVE_REF", cve);
					}
					ref_annot.appendChild(annotationValue);
//					tstart = Utils.timeStamp("ref append:" + a.name, tstart);
					annot.appendChild(ref_annot);
				}
//				tstart = Utils.timeStamp("final append:" + a.name, tstart);
				tier.appendChild(annot);
			}
		}
	}

	// Création du fichier de sortie
	public void createOutput() {
		Utils.createFile(elanDoc, outputName);
	}

	public static void main(String args[]) throws IOException {
		TierParams.printVersionMessage(false);
		String usage = "Description: TeiToElan converts a TEI file to a ELAN file%nUsage: TeiToElan [-options] <file"
				+ Utils.EXT + ">%n";
		TeiToElan tte = new TeiToElan();
		//System.out.println(args);
		tte.mainCommand(args, Utils.EXT, EXT, usage, 0);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		try {
			if (!transform(input, output, options)) return;
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println("Reading " + input);
		createOutput();
//		System.out.println("New file created " + output);
	}
}
