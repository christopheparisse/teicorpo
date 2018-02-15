package fr.ortolang.teicorpo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ElanToHT {

	HierarchicTrans ht;
	/** Fichier Elan **/
	public File eafFile;
	/** Document issu du fichier Elan. */
	public Document docEAF;
	// acces Xpath
	public XPathFactory xPathfactory;
	public XPath xpath;
	
	public HashMap <String, ArrayList <Element>> refInfo;

	public ElanToHT(File eafFile) throws IOException {
		ht = new HierarchicTrans();
		this.eafFile = eafFile;
		ht.fileName = eafFile.getName();
		ht.filePath = eafFile.getCanonicalPath();

		// lecture du document Elan
		this.docEAF = null;
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			Utils.setDTDvalidation(factory, false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			this.docEAF = builder.parse(eafFile);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			// e.printStackTrace();
			System.exit(1);
		}
		/*
		this.xPathfactory = XPathFactory.newInstance();
		this.xpath = xPathfactory.newXPath();
		this.xpath.setNamespaceContext(new NamespaceContext() {
			public String getNamespaceURI(String prefix) {
				System.out.println("prefix called " + prefix);
				if (prefix == null) {
					throw new IllegalArgumentException("No prefix provided!");
				} else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
					System.out.println("default prefix called");
					return "";
				} else if (prefix.equals("tei")) {
					System.out.println("tei prefix called");
					return "http://www.tei-c.org/ns/1.0";
				} else if (prefix.equals("xsi")) {
					return "http://www.w3.org/2001/XMLSchema-instance";
				} else {
					return XMLConstants.NULL_NS_URI;
				}
			}

			public Iterator<?> getPrefixes(String val) {
				return null;
			}

			public String getPrefix(String uri) {
				return null;
			}
		});
		*/
		refInfo = new HashMap<String, ArrayList<Element>>();
		createRefInfo();
		// Récupération des informations sur le fichier
		ht.metaInf = new MetaInf_elan(docEAF, eafFile);
		ht.initial_format = "Elan";
		// Construction de la timeline
		buildTimeline();
		// Récupération des voc contrôlés
		getCvs();
		// Récupération des noms de tiers principaux
		getTiersInfo();
		// Construction de l'arborescence de la transcription
		buildTiers();
	}

	public void buildTimeline() {
		Element tl = (Element) this.docEAF.getElementsByTagName("TIME_ORDER").item(0);
		NodeList time_slots = tl.getElementsByTagName("TIME_SLOT");
		ht.times = new ArrayList<String>();
		for (int i = 0; i < time_slots.getLength(); i++) {
			Element time_slot = (Element) time_slots.item(i);
			String id = time_slot.getAttribute("TIME_SLOT_ID");
			String time_val = time_slot.getAttribute("TIME_VALUE");
			ht.timeline.put(id, time_val);
			ht.times.add(time_val);
		}
	}
	
	public void createRefInfo() {
		NodeList tiers = this.docEAF.getElementsByTagName("TIER");
		for (int i = 0; i < tiers.getLength(); i++) {
			Element tier = (Element) tiers.item(i);
			String nameTier = tier.getAttribute("TIER_ID");
			NodeList refs = tier.getElementsByTagName("REF_ANNOTATION");
			for (int j = 0; j < refs.getLength(); j++) {
				Element ref = (Element) refs.item(j);
				String nameRef = ref.getAttribute("ANNOTATION_REF");
				String n = nameTier + "+" + nameRef;
				ArrayList<Element> al = refInfo.get(n);
				if (al != null)
					al.add(ref);
				else {
					al = new ArrayList<Element>();
					al.add(ref);
					refInfo.put(n, al);
				}
			}
		}
	}

	public void getTiersInfo() {
		ht.mainTiersNames = new ArrayList<String>();
		ht.tiersInfo = new HashMap<String, TierInfo>();
		NodeList tiers = this.docEAF.getElementsByTagName("TIER");
		NodeList lgqTypes = this.docEAF.getElementsByTagName("LINGUISTIC_TYPE");
		for (int i = 0; i < tiers.getLength(); i++) {
			TierInfo tierInfo = new TierInfo();
			Element tier = (Element) tiers.item(i);
			tierInfo.type.lgq_type_id = tier.getAttribute("LINGUISTIC_TYPE_REF");
			Element lgqTypeElement = getlgqType(tierInfo.type.lgq_type_id, lgqTypes);
			if (lgqTypeElement == null) {
				System.err.printf("No type for %s:%s (ignored)%n", tier.getAttribute("TIER_ID"), tierInfo.type.lgq_type_id);
				continue;
			}
			String name = tier.getAttribute("TIER_ID");
			tierInfo.tier_id = tier.getAttribute("TIER_ID");
			tierInfo.participant = tier.getAttribute("PARTICIPANT");
			//System.err.printf("%s (%s)%n", lgqTypeElement.toString(), tierInfo.type.lgq_type_id);
			//System.err.println(lgqTypeElement.getAttribute("CONTROLLED_VOCABULARY_REF"));
			tierInfo.parent = tier.getAttribute("PARENT_REF");
			tierInfo.annotator = tier.getAttribute("ANNOTATOR");
			tierInfo.type.time_align = Boolean.parseBoolean(lgqTypeElement.getAttribute("TIME_ALIGNABLE"));
			tierInfo.lang = tier.getAttribute("DEFAULT_LOCALE");
			tierInfo.lang_ref = tier.getAttribute("LANG_REF");
			if (!tier.hasAttribute("PARENT_REF")) {
				tierInfo.type.constraint = "-";
				ht.mainTiersNames.add(name);
			} else {
				tierInfo.type.constraint = lgqTypeElement.getAttribute("CONSTRAINTS");
			}
			// System.out.println(tierInfo.toString());
			ht.tiersInfo.put(name, tierInfo);
		}
		// Reconstitution des dépendances
		for (Map.Entry<String, TierInfo> entry : ht.tiersInfo.entrySet()) {
			entry.getValue().dependantsNames = getSubTierListNames(entry.getKey());
		}
	}

	public Element getlgqType(String infoType, NodeList lgqTypes) {
		for (int i = 0; i < lgqTypes.getLength(); i++) {
			Element lgqType = (Element) lgqTypes.item(i);
			if (lgqType.getAttribute("LINGUISTIC_TYPE_ID").equals(infoType)) {
				return lgqType;
			}
		}
		return null;
	}

	public void getCvs() {

		ht.cvs = new ArrayList<CV>();
		NodeList Cvs = docEAF.getElementsByTagName("CONTROLLED_VOCABULARY");
		for (int i = 0; i < Cvs.getLength(); i++) {
			Element cv_el = (Element) Cvs.item(i);
			CV cv = new CV_elan(cv_el);
			ht.cvs.add(cv);
		}
	}

	public String getTimeValue(String timeId) {
		if (Utils.isNotEmptyOrNull(timeId)) {
			try {
				return ht.timeline.get(timeId.split("#")[1]);
			} catch (Exception e) {
				return ht.timeline.get(timeId.split("#")[0]);
			}
		}
		return "-1";
	}

	public Double doubleValue(String timeValue) {
		if (Utils.isNotEmptyOrNull(timeValue)) {
			return Double.parseDouble(timeValue);
		} else {
			return (double) -1;
		}
	}

	public void buildTiers() {
		ht.hierarchic_representation = new HashMap<String, ArrayList<Annot>>();
		ht.languages = new HashSet<String>();
		// Parcours des tiers principaux
		ArrayList<Element> mainTiers = getmainTiers();
		for (Element tierElement : mainTiers) {
			ArrayList<Annot> annots = new ArrayList<Annot>();
			ht.languages.add(tierElement.getAttribute("DEFAULT_LOCALE"));
			String name = tierElement.getAttribute("TIER_ID");
			TierInfo tierInfo = ht.tiersInfo.get(name);
			String annotElName = "ALIGNABLE_ANNOTATION";
			/// *** A utiliser que pour les subdiv? ***///
			if (!tierInfo.type.time_align) {
				annotElName = "REF_ANNOTATION";
			}
			NodeList annotsNodes = tierElement.getElementsByTagName(annotElName);
			if (annotsNodes.getLength() == 0) {
				// Cas où un tier ne contient aucune annotation : on supprime le
				// tier
				/*
				 * Annot annot = new Annot(); annot.name = name;
				 * annot.timereftype = "time"; annots.add(annot); addAnnot(name,
				 * annots);
				 */
				continue;
			}
			for (int i = 0; i < annotsNodes.getLength(); i++) {
				Element annotEl = (Element) annotsNodes.item(i);
				Annot annot = new Annot();
				annot.name = name;
				annot.dependantAnnotations = new ArrayList<Annot>();
				annot.setContent(annotEl.getTextContent().trim());
				annot.id = annotEl.getAttribute("ANNOTATION_ID");
				annot.start = this.getTimeValue("#" + annotEl.getAttribute("TIME_SLOT_REF1"));
				annot.end = this.getTimeValue("#" + annotEl.getAttribute("TIME_SLOT_REF2"));
				annot.timereftype = "time";
				annots.add(annot);
				// print("\tID: " + tier.id + " CONTENT: " + tier.content + "
				// START: " + tier.start + " END: " + tier.end );
				// System.out.println("\tID: " + tier.id + " CONTENT: " +
				// tier.content + " START: " + tier.start + " END: " + tier.end
				// );
			}
			addAnnot(name, annots);
		}
	}

	public void addAnnot(String name, ArrayList<Annot> annots) {
		ht.hierarchic_representation.put(name, annots);
		ht.tiersInfo.get(name).dependantsNames = getSubTierListNames(name);
		buildSubTiers(name, annots);
	}

	public void buildSubTiers(String annotType, ArrayList<Annot> annots) {
		ArrayList<String> dependantsTiersNames = ht.tiersInfo.get(annotType).dependantsNames;
		for (String tName : dependantsTiersNames) {
			// System.err.println(">buildSubTiers Tier: " + annotType + " dependent: " + tName);
			if (ht.tiersInfo.get(tName).type.time_align) {
				//System.err.println("Construction par alignement temporel");
				buildSubTimeAlignableTiers(annotType, annots, tName);
			} else {
				//System.err.println("Construction par référence symbolique");
				buildSubRefAlignableTiers(annotType, annots, tName);
			}
		}
		//long heapFreeSize = Runtime.getRuntime().freeMemory(); 
		//System.err.println("<buildSubTiers " + heapFreeSize);
	}

	public void buildSubTimeAlignableTiers(String annotType, ArrayList<Annot> annots, String subAnnotName) {
		// annotType: nom du parent immédiat?
		// annots: container pour le résultat
		// subElement : element de tête d'un tier dépendant
		// subAnnotName : nom du tier dépendant
		// System.out.println("Construction de " + subAnnotName + " appartenant
		// à " + annotType);
		// System.out.println("Valeurs temporelles initialles pour le
		// sous-tier");
		Element subElement = getTierByTierName(subAnnotName);
		NodeList subElementAnnots = subElement.getElementsByTagName("ALIGNABLE_ANNOTATION");
		// Now we must fill all possible empty time elements in the
		// transcription
		int sublg = subElementAnnots.getLength(), j;
		Double[] startTimes = new Double[sublg];
		Double[] endTimes = new Double[sublg];
		for (j = 0; j < sublg; j++) {
			Element align_annot = (Element) subElementAnnots.item(j);
			String align_annot_start = align_annot.getAttribute("TIME_SLOT_REF1");
			String align_annot_end = align_annot.getAttribute("TIME_SLOT_REF2");
			String align_annot_start_time = this.getTimeValue(align_annot_start);
			String align_annot_end_time = this.getTimeValue(align_annot_end);
			if (!align_annot_start_time.isEmpty())
				startTimes[j] = Double.parseDouble(align_annot_start_time);
			else
				startTimes[j] = (double) -1.0;
			if (!align_annot_end_time.isEmpty())
				endTimes[j] = Double.parseDouble(align_annot_end_time);
			else
				endTimes[j] = (double) -1.0;
			/*
			 * System.out.printf( "%d :: %s :: %s :: %s :: %s :: {%s} %n", j,
			 * align_annot_start_time, align_annot_end_time, align_annot_start,
			 * align_annot_end, align_annot.getTextContent().trim());
			 */
		}
		for (j = 0; j < sublg; j++) {
			if (endTimes[j] == (double) -1.0) {
				// find the next endtime that is not null
				int k = j + 1;
				for (; k < sublg; k++) {
					if (endTimes[k] != (double) -1.0) {
						break;
					}
				}
				if (k == sublg) {
					// not found. cannot do much.
					if (j > 0)
						endTimes[j] = endTimes[j] + 1; // one ms long
				} else {
					// divide the gap and fill it
					Double ecart = (endTimes[k] - startTimes[j]) / (k - j + 1);
					for (int g = j; g < k; g++) {
						endTimes[g] = startTimes[j] + (g - j + 1) * ecart;
						startTimes[g + 1] = endTimes[g];
					}
				}
			}
		}
		/*
		 * System.out.println("Après rectification"); for (j= 0; j<sublg; j++){
		 * System.out.printf( "%d :: %f :: %f :: %n", j, startTimes[j],
		 * endTimes[j]); }
		 */
		// System.out.println("Construction du sous-tier");
		ArrayList<Annot> subAnnots = new ArrayList<Annot>();
		if (sublg > 0) {
			for (Annot annot : annots) {
				for (j = 0; j < sublg; j++) {
					Element align_annot = (Element) subElementAnnots.item(j);
					Double align_annot_start = startTimes[j];
					Double align_annot_end = endTimes[j];
					Double cont_annot_start = Double.parseDouble(annot.start);
					Double cont_annot_end = Double.parseDouble(annot.end);
					// System.out.println("start: " + annot.start + " " +
					// cont_annot_start + " end: " + annot.end + " " +
					// cont_annot_end);
					if (isInclude(cont_annot_start, cont_annot_end, align_annot_start, align_annot_end)) {
						// les nouvelles annotations sont ajoutés avec des
						// valeurs temporelles, pas des étiquettes
						// ce qui permet de gérer les valeurs nouvelles et de
						// recréer une timeline à la fin
						addNewAnnot(annot, align_annot, subAnnots, subAnnotName, Double.toString(align_annot_start),
								Double.toString(align_annot_end));
						/*
						 * System.out.printf(
						 * "%d :: %s %s %s {%s} :: %f :: %f :: %f :: %f :: %n",
						 * j, align_annot.getAttribute("ANNOTATION_ID"),
						 * align_annot.getTagName(), subAnnotName,
						 * align_annot.getTextContent().trim(),
						 * cont_annot_start, cont_annot_end, align_annot_start,
						 * align_annot_end);
						 */
					}
				}
			}
		}
		if (ht.tiersInfo.get(subAnnotName).dependantsNames.size() > 0) {
			// System.out.println(subAnnotName + " -> " +
			// tiersInfo.get(subAnnotName).dependantsNames);
			buildSubTiers(subAnnotName, subAnnots);
		}
	}

	/**
	 * buildSubRefAlignableTiers
	 * @param annotType le nom du tier parent dont dépendent les annots
	 * @param annots la liste des annotations dont il faut chercher les enfants
	 * @param subAnnotName nom du tier dépendant
	 */
	public void buildSubRefAlignableTiers(String annotType, ArrayList<Annot> annots, String subAnnotName) {
		/*
		Element subElement = getTierByTierName(subAnnotName);
		NodeList subElementAnnots = subElement.getElementsByTagName("REF_ANNOTATION");
		 */
		for (Annot annot : annots) {
			// chercher les REF_ANNOTATION dependant du tier annotType dont l'attribut ANNOTATION_REF est annot.id
			/*
			NodeList nl = null;
	        XPathExpression expr;
			try {
				expr = xpath.compile("/ANNOTATION_DOCUMENT/TIER[@TIER_ID=\""+ subAnnotName + "\"]//REF_ANNOTATION[@ANNOTATION_REF=\"" + annot.id + "\"]");
				nl = (NodeList) expr.evaluate(this.docEAF, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
			for (int j = 0; j < nl.getLength(); j++) {// Obligé de
				Element ref_annot = (Element) nl.item(j);
				addNewAnnot(annot, ref_annot, subAnnots, subAnnotName, null, null);
			}
			*/
			ArrayList<Annot> subAnnots = new ArrayList<Annot>();
			String n = subAnnotName + "+" + annot.id;
			ArrayList<Element> al = refInfo.get(n);
			if (al != null) {
				// System.err.printf("%s:%d%n", n, al.size());
				for (Element e: al) {
					addNewAnnot(annot, e, subAnnots, subAnnotName, null, null);
				}
			}
			if (ht.tiersInfo.get(subAnnotName).dependantsNames.size() > 0) {
				// System.err.println(subAnnotName + " -> " +
				//		ht.tiersInfo.get(subAnnotName).dependantsNames);
				buildSubTiers(subAnnotName, subAnnots);
			}
		}
	}

	public void addNewAnnot(Annot annot, Element annotEl, ArrayList<Annot> annots, String subAnnotName, String start,
			String end) {
		Annot subAnnot = new Annot();
		subAnnot.setContent(annotEl.getTextContent());
		subAnnot.id = annotEl.getAttribute("ANNOTATION_ID");
		subAnnot.name = subAnnotName;
		if (annotEl.getTagName().equals("REF_ANNOTATION")) {
			subAnnot.timereftype = "ref";
			subAnnot.link = annot.id;
		} else {
			subAnnot.timereftype = "time";
			subAnnot.start = start;
			subAnnot.end = end;
		}
		if (annot.dependantAnnotations == null)
			annot.dependantAnnotations = new ArrayList<Annot>();
		annot.dependantAnnotations.add(subAnnot);
		annots.add(subAnnot);
	}

	public ArrayList<String> getSubTierListNames(String tierName) {
		ArrayList<String> subTiersNames = new ArrayList<String>();
		for (Map.Entry<String, TierInfo> entry : ht.tiersInfo.entrySet()) {
			if (entry.getValue().parent.equals(tierName)) {
				subTiersNames.add(entry.getKey());
			}
		}
		return subTiersNames;
	}

	public Element getTierByTierName(String tierName) {
		NodeList tiers = docEAF.getElementsByTagName("TIER");
		for (int i = 0; i < tiers.getLength(); i++) {
			Element t = (Element) tiers.item(i);
			if (t.getAttribute("TIER_ID").equals(tierName)) {
				return t;
			}
		}
		return null;
	}

	public ArrayList<Element> getmainTiers() {
		ArrayList<Element> mainTiers = new ArrayList<Element>();
		NodeList tiers = docEAF.getElementsByTagName("TIER");
		for (int i = 0; i < tiers.getLength(); i++) {
			Element tier = (Element) tiers.item(i);
			if (ht.mainTiersNames.contains(tier.getAttribute("TIER_ID"))) {
				mainTiers.add(tier);
			}
		}
		return mainTiers;
	}

	public boolean isInclude(Double mainStart, Double mainEnd, Double subStart, Double subEnd) {
		boolean val;
		try {
			val = ((subStart >= mainStart) && (mainEnd >= subEnd));
		} catch (Exception e) {
			val = false;
		}
		// System.out.println(this.getTimeValue(mainStart) + " ::: "
		// +this.getTimeValue(mainEnd) + " ::: " + this.getTimeValue(subStart) +
		// " ::: " + this.getTimeValue(subEnd) + " --> " + val);
		return val;
	}

	public class MetaInf_elan extends MetaInf {
		MetaInf_elan(Document docEAF, File eafFile) {
			super();
			Element annot_doc = docEAF.getDocumentElement();
			Element header = (Element) docEAF.getElementsByTagName("HEADER").item(0);
			NodeList tiers = docEAF.getElementsByTagName("TIER");
			NodeList mediaDescriptors = header.getElementsByTagName("MEDIA_DESCRIPTOR");
			NodeList properties = header.getElementsByTagName("PROPERTY");
			// time_units = header.getAttribute("TIME_UNITS");
			if (header.getAttribute("TIME_UNITS").equals("seconds")) {
				time_units = "s";
			} else if (header.getAttribute("TIME_UNITS").equals("milliseconds")) {
				time_units = "ms";
			}
			author = annot_doc.getAttribute("AUTHOR");
			format = annot_doc.getAttribute("FORMAT");
			version = annot_doc.getAttribute("VERSION");
			getMedias(mediaDescriptors);
			date = annot_doc.getAttribute("DATE");
			getParticipantsAndTranscribers(tiers);
			getAdditionnalNotes(properties);
		}

		public void getMedias(NodeList mediaDescriptors) {
			medias = new ArrayList<Media>();
			for (int i = 0; i < mediaDescriptors.getLength(); i++) {
				Element mediaDesc = (Element) mediaDescriptors.item(i);
				Media m = new Media(mediaDesc.getAttribute("MIME_TYPE"), mediaDesc.getAttribute("MEDIA_URL"));
				medias.add(m);
			}
		}

		public void getParticipantsAndTranscribers(NodeList tiers) {
			participants = new ArrayList<Participant>();
			transcribers = new HashSet<String>();
			for (int i = 0; i < tiers.getLength(); i++) {
				Element t = (Element) tiers.item(i);
				String participantName = t.getAttribute("PARTICIPANT");
				String transcriber = t.getAttribute("ANNOTATOR");
				Participant p = new Participant();
				p.name = participantName;
				p.id = t.getAttribute("TIER_ID");
				participants.add(p);
				if (Utils.isNotEmptyOrNull(transcriber)) {
					transcribers.add(transcriber);
				}
			}
		}

		public void getAdditionnalNotes(NodeList properties) {
			additionnalNotes = new HashMap<String, String>();
			for (int i = 0; i < properties.getLength(); i++) {
				Element property = (Element) properties.item(i);
				additionnalNotes.put(property.getAttribute("NAME"), property.getTextContent());
			}
		}
	}

	public class CV_elan extends CV {
		public CV_elan(Element CV) {
			super();
			this.id = CV.getAttribute("CV_ID");
			NodeList cvdesc = CV.getElementsByTagName("DESCRIPTION");
			if (cvdesc != null && cvdesc.getLength() > 0) {
				this.cv_desc = ((Element) (cvdesc.item(0))).getTextContent();
				this.cv_desc_lang = ((Element) (cvdesc.item(0))).getAttribute("LANG_REF");
			}
			NodeList terms = CV.getElementsByTagName("CVE_VALUE");
			for (int j = 0; j < terms.getLength(); j++) {
				Element cv_entry_el = (Element) terms.item(j);
				CV_entry cv_entry = new CV_entry(cv_entry_el.getTextContent().trim(),
						cv_entry_el.getAttribute("DESCRIPTION"), cv_entry_el.getAttribute("LANG_REF"));
				entries.add(cv_entry);
			}
		}
	}

	public static void main(String args[]) throws IOException {
		ElanToHT ef = new ElanToHT(new File(args[0]));
		for (Map.Entry<String, ArrayList<Annot>> entry : ef.ht.hierarchic_representation.entrySet()) {
			System.out.println("******************  " + entry.getKey().toUpperCase() + "  ******************");
			// System.out.println(ef.tiersInfo.get(entry.getKey()).dependantsNames);
			if (entry.getValue().size() > 49) {
				for (Annot annot : entry.getValue().subList(0, 50)) {
					System.out.println(annot.AnnotToString(""));
				}
			}
		}
		// System.out.println(ef.tiersInfo.get("phase geste
		// MD").dependantsNames);

		for (Map.Entry<String, TierInfo> entry : ef.ht.tiersInfo.entrySet()) {
			System.out.println(entry.getKey());
			TierInfo ti = entry.getValue();
			System.out.println(ti.annotator + " " + ti.lang + " " + ti.parent + " " + ti.participant + " " + ti.type
					+ " " + ti.dependantsNames);
		}

	}
}