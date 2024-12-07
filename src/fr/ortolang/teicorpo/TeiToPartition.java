package fr.ortolang.teicorpo;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.*;

public class TeiToPartition {

	// Liste des informations sur les tiers
	ArrayList<TierInfo> tierInfos;
	// Liste des tiers
	TreeMap<String, ArrayList<Annot>> tiers;
	TreeMap<String, NewTier> newTiers;
	// index for creating new ids
	int idIncr = 1;

	// timeline information
	TeiTimeline timeline;

	Document teiDoc;
	XPath teiXPath;
	public TierParams optionsOutput;

	void init(XPath xpath, Document tei, TierParams optionsTei) {
		if (optionsTei == null) optionsTei = new TierParams();
		System.err.println("teitopartition init");
		optionsOutput = optionsTei;
		teiDoc = tei;
		teiXPath = xpath;
		timeline = new TeiTimeline();
		// récupérer la timeline
		timeline.buildTimeline(teiDoc);
		// info tiers et types
		getTierInfo();
		// contenu des tiers
		getTiers();
	}

	public static String getLgqConstraint(ArrayList<TierInfo> ti, String type) {
		for (TierInfo tie : ti) {
			if (tie.tier_id.equals(type))
				return tie.linguistType.constraint;
		}
		return "";
	}

	public static TierInfo getTierInfoElement(ArrayList<TierInfo> ti, String type) {
		for (TierInfo tie : ti) {
			if (tie.tier_id.equals(type))
				return tie;
		}
		return null;
	}

	// Construction d'une structure intermédiaire contenant les annotations de
	// la transcription
	// Structure = map
	// Clé = Nom du tier
	// Valeur = liste des annotations de ce type
	public void getTiers() {
		tiers = new TreeMap<String, ArrayList<Annot>>();
		newTiers = new TreeMap<String, NewTier>();
		NodeList annotationGrps = null;
		try {
			annotationGrps = TeiDocument.getAllAnnotationBloc(this.teiXPath, this.teiDoc);
		} catch (XPathExpressionException e) {
			System.out.println("xpath error on list of annotationBlock");
			e.printStackTrace();
			System.exit(1);
		}
		for (int i = 0; i < annotationGrps.getLength(); i++) {
			Element annotGrp = (Element) annotationGrps.item(i);
			AnnotatedUtterance au = new AnnotatedUtterance();
			System.out.printf("tml=%s [%s]%n", this.timeline != null ? "on": "off", annotGrp.toString());
			au.processAnnotatedU(annotGrp, this.timeline, null, optionsOutput, false);
			// System.out.printf("processAU: %s%n", au.toString());
			NodeList annotGrpElmts = annotGrp.getChildNodes();
			String choiceTag = au.speakerChoice(optionsOutput);
			if (!Utils.isNotEmptyOrNull(choiceTag))
				continue; // this is a note and not an utterance
			if (optionsOutput != null) {
				if (optionsOutput.isDontDisplay(choiceTag, 1))
					continue;
				if (!optionsOutput.isDoDisplay(choiceTag, 1))
					continue;
			}
			// String timeSG = (start.isEmpty() || end.isEmpty()) ? "ref" : "time"; // ref is impossible on u tags in ELAN
			// si le nombre d'annotation de au.speeches > 1 il faudrait changer le statut des spans qui en dépendent
			for (Annot a: au.speeches) {
				a.timereftype = "time";
				String lgqt = "";
				for (TierInfo ti : tierInfos) {
					if (ti.tier_id.equals(choiceTag))
						lgqt = ti.linguistType.lgq_type_id == null ? LgqType.DEFAULT_LING_TYPE : ti.linguistType.lgq_type_id;
				}
				addElementToMap(tiers, choiceTag, a, lgqt, "-");
			}
			
			for (int j = 0; j < annotGrpElmts.getLength(); j++) {
				if (TeiDocument.isElement(annotGrpElmts.item(j))) {
					Element annotElmt = (Element) annotGrpElmts.item(j);
					if (annotElmt.getNodeName().equals("spanGrp")) {
						String at = annotElmt.getAttribute("type");
						if (at != null && at.equals("TurnInformation"))
							continue;
						spanGrpCase(tiers, annotElmt, au.lastxmlid, choiceTag, "time", au.start, au.startStamp, au.end, au.endStamp);
					}
				}
			}
		}
		/*
		// dump tiers
		for(Entry<String, ArrayList<Annot>> entry : tiers.entrySet()){
			String tierName = entry.getKey();
			ArrayList<Annot> tierContent = entry.getValue();
			System.out.printf("%s%n", tierName);
			for (Annot a: tierContent) {
				System.out.printf("   %s%n", a.toString());
			}
		}
		*/
	}

	// Traitement des spanGrp pour ajout dans la structure Map
	public void spanGrpCase(TreeMap<String, ArrayList<Annot>> tiers, Element spanGrp, String id, String name,
			String timeref, String start, String startStamp, String end, String endStamp) {
		String typeSG = spanGrp.getAttribute("type");
//		System.err.printf("Test2 spanGrpCase %s ID %s%n", typeSG, id);
		if (optionsOutput != null) {
			if (optionsOutput.level == 1)
				return;
			if (optionsOutput.isDontDisplay(typeSG, 2))
				return;
			//System.err.printf(">>> %s%n", typeSG);
			if (!optionsOutput.isDoDisplay(typeSG, 2))
				return;
		}
		//System.err.printf("spanGrpCase %s%n", typeSG);
		NodeList spans = spanGrp.getChildNodes();
		String previousId = "";
		if (spans == null)
			return;
		int ntot = 0;
		for (int z = 0; z < spans.getLength(); z++) {
			Node nodespan = spans.item(z);
			// System.out.printf("%d %s %d %n", z, nodespan.getNodeName(),
			// nodespan.getNodeType());
			if (!nodespan.getNodeName().equals("span"))
				continue;
			ntot++;
		}
		Double timelength = -1.0;
		try {
			timelength = (Double.parseDouble(end) - Double.parseDouble(start)) / ntot;
		} catch (Exception e) {
			timelength = -1.0;
		}
		//System.out.printf("XX: %s %s %f %n", start, end, timelength);
		int nth = 0;
		for (int z = 0; z < spans.getLength(); z++) {
			Node nodespan = spans.item(z);
			// System.err.printf("%d %s %d%n", z, nodespan.getNodeName(), nodespan.getNodeType());
			if (!nodespan.getNodeName().equals("span"))
				continue;
			Element span = (Element) nodespan;
			Annot annot = new Annot();
			/*
			 * deals with all types of span content.
			 */
			String s = AnnotatedUtterance.processSpan(span);
			//System.out.printf("[%s]%n",s);
			annot.setContent(s);
			String spid = span.getAttribute("xml:id");
			if (!spid.isEmpty())
				annot.id = spid;
			else
				annot.id = "x" + idIncr++; // was id; ??? why cannot be the same id as the link ?
//			System.out.printf("%d %d %s %s %s %s {%s} %s %n", z, span.getNodeType(), typeSG, id, name, span.getTagName(), annot.getContent(), annot.id);
			// if (span.hasAttribute("target")){
			if (!LgqType.isTimeType(getLgqConstraint(tierInfos, typeSG))) {
				// System.out.printf("%s is ref%n", typeSG);
				annot.timereftype = "ref";
				String tg = span.getAttribute("target");
				if (!tg.isEmpty())
					annot.link = tg.substring(1);
				else
					annot.link = id;
				if (!previousId.isEmpty())
					annot.previous = previousId;
				previousId = annot.id;
				/*
				 * add equivalence in time in case it is necessary
				 */
				//System.out.printf("++ %d %s %n", z, annot);
				if (timelength >= 0.0) {
					Double refstart = ((double)nth) * timelength + Double.parseDouble(start);
					Double refend = (((double)nth) + 1.0) * timelength + Double.parseDouble(start);
					// System.out.printf("-- %d %f %f %n", nth, refstart, refend);
					if (nth != 0) {
						annot.start = Double.toString(refstart);
						annot.startStamp = Utils.createTimeStamp(annot.id, Utils.timestamp1000(annot.start));
						annot.end = Double.toString(refend);
						annot.endStamp = (annot.end.equals(end))
								? endStamp
								: Utils.createTimeStamp(annot.id, Utils.timestamp1000(Double.toString(refend)));
					} else {
						annot.start = start;
						annot.startStamp = startStamp;
						annot.end = Double.toString(refend);
						annot.endStamp = (annot.end.equals(end))
								? endStamp
								: Utils.createTimeStamp(annot.id, Utils.timestamp1000(Double.toString(refend)));
					}
				}
//				System.out.printf("ref %s (%s) %n", annot.link, annot.previous);
			} else {
				// System.out.printf("%s is time%n", typeSG);
				annot.timereftype = "time";
				NamedNodeMap nnn = span.getAttributes();
				/*
				System.err.printf("YY0: [%d] {%s} %s%n", nnn.getLength(), span.getNodeName(), span.getTextContent());
				for (int ii = 0; ii < nnn.getLength(); ii++) {
					System.err.printf("YY00: %s %s%n", nnn.item(ii).getNodeName(), nnn.item(ii).getTextContent());
				}
				*/
				String tstart = span.getAttribute("from");
				String tend = span.getAttribute("to");
				//System.err.printf("YY1: %s %s %n", tstart, tend);
				if (Utils.isNotEmptyOrNull(tstart)) {
					String ttstart = timeline.getTimeValue(Utils.refID(tstart));
					if (ttstart.equals(start)) {
						annot.start = start;
						annot.startStamp = startStamp;
					} else {
						annot.start = ttstart;
						annot.startStamp = Utils.createTimeStamp(annot.id, Utils.timestamp1000(ttstart));
					}
				}
				if (Utils.isNotEmptyOrNull(tend)) {
					String ttend = timeline.getTimeValue(Utils.refID(tend));
					if (ttend.equals(end)) {
						annot.end = end;
						annot.endStamp = endStamp;
					} else {
						annot.end = ttend;
						annot.endStamp = Utils.createTimeStamp(annot.id, Utils.timestamp1000(ttend));
					}
				}
			}
			String lgqt = "";
			for (TierInfo ti : tierInfos) {
				//System.out.printf("QQQ %s %s %s%n", ti.tier_id, typeSG, ti.linguistType.lgq_type_id);
				if (ti.tier_id.equals(typeSG))
					lgqt = ti.linguistType.lgq_type_id == null ? LgqType.DEFAULT_LING_TYPE : ti.linguistType.lgq_type_id;
			}
			//System.out.printf("Z %d time %s %s%n", nth, annot.start, annot.end);
			addElementToMap(tiers, typeSG, annot, lgqt, name);
			NodeList spanGrps = span.getChildNodes();
			for (int l = 0; l < spanGrps.getLength(); l++) {
				Node nodeSpanGrp = spanGrps.item(l);
				if (!nodeSpanGrp.getNodeName().equals("spanGrp"))
					continue;
				Element subSpanGrp = (Element) nodeSpanGrp;
				spanGrpCase(tiers, subSpanGrp, annot.id, name, annot.timereftype, annot.start, annot.startStamp, annot.end, annot.endStamp);
			}
			nth++;
		}
	}

	void addElementToMap(TreeMap<String, ArrayList<Annot>> map, String type, Annot annot, String lingType,
			String topparent) {
		// Créer le nouveau nom du tier
		// stocker la référence du type ling de ce nom.
		String truename;
		if (!topparent.isEmpty() && !topparent.equals("-")) {
			if (optionsOutput.target.equals("dinlang")) {
				truename = type + "-" + topparent;
			} else {
				truename = topparent + "-" + type;
			}
			NewTier nt = new NewTier(truename, type, lingType, topparent);
			// System.out.printf("-: %s %s %s %s %n", truename, type, lingType,
			// topparent);
			newTiers.put(truename, nt);
		} else
			truename = type;
		annot.topParent = topparent;
		if (map.containsKey(truename)) {
			map.get(truename).add(annot);
		} else {
			ArrayList<Annot> newAnnotList = new ArrayList<Annot>();
			newAnnotList.add(annot);
			map.put(truename, newAnnotList);
		}
	}

	// Elements linguistic_type
	void getTierInfo() {
		tierInfos = new ArrayList<TierInfo>();
		Element teiHeader = (Element) this.teiDoc.getElementsByTagName("teiHeader").item(0);
		NodeList notes = teiHeader.getElementsByTagName("note");
		for (int j = 0; j < notes.getLength(); j++) {
			Element note = (Element) notes.item(j);
			if (note.getAttribute("type").equals("TEMPLATE_DESC")) {
				// System.out.println("found the TEMPLATE notes");
				NodeList templateChildren = note.getChildNodes();
				for (int y = 0; y < templateChildren.getLength(); y++) {
					TierInfo ti = new TierInfo();
					Node nd = templateChildren.item(y);
					if (!nd.getNodeName().equals("note"))
						continue;
					NodeList templateNote = nd.getChildNodes();
					for (int z = 0; z < templateNote.getLength(); z++) {
						Node nd2 = templateNote.item(z);
						if (!nd2.getNodeName().equals("note"))
							continue;
						Element elt = (Element) nd2;
						if (elt.getAttribute("type").equals("code")) {
							ti.tier_id = elt.getTextContent();
						} else if (elt.getAttribute("type").equals("graphicref")) {
							ti.linguistType.graphic_ref = elt.getTextContent();
						} else if (elt.getAttribute("type").equals("parent")) {
							ti.parent = elt.getTextContent();
						} else if (elt.getAttribute("type").equals("type")) {
							ti.linguistType.constraint = elt.getTextContent();
						} else if (elt.getAttribute("type").equals("subtype")) {
							ti.linguistType.lgq_type_id = elt.getTextContent();
						} else if (elt.getAttribute("type").equals("scribe")) {
							ti.annotator = elt.getTextContent();
						} else if (elt.getAttribute("type").equals("lang")) {
							ti.lang = elt.getTextContent();
						} else if (elt.getAttribute("type").equals("langref")) {
							ti.lang_ref = elt.getTextContent();
						} else if (elt.getAttribute("type").equals("cv")) {
							ti.linguistType.cv_ref = elt.getTextContent();
						}
					}

					// System.out.printf("QQQ: (%s) %s%n", ti.tier_id, ti.linguistType.toString());

					if (Utils.isNotEmptyOrNull(ti.linguistType.constraint)) {
						// if there is a constraint, respect it
						if (Utils.isEmptyOrNull(ti.linguistType.lgq_type_id)) {
							if (optionsOutput.target.equals("dinlang") && Utils.isEmptyOrNull(ti.linguistType.lgq_type_id)) {
								ti.linguistType.lgq_type_id = "lng-aud";
								ti.linguistType.constraint = "";
							} else {
								ti.linguistType.lgq_type_id = ti.tier_id == null ? ti.linguistType.constraint : ti.tier_id; // creates a new name
							}
						}
					} else {
						if (optionsOutput.target.equals("dinlang") && Utils.isEmptyOrNull(ti.linguistType.lgq_type_id)) {
							ti.linguistType.lgq_type_id = "lng-aud";
							ti.linguistType.constraint = "";
						} else {
							if (Utils.isEmptyOrNull(ti.linguistType.lgq_type_id) || ti.linguistType.lgq_type_id.equals("-")) ti.linguistType.lgq_type_id = LgqType.DEFAULT_LING_TYPE; // no ling type
							ti.linguistType.constraint = ""; // no ling type, the ling are
							// another option is that the ling are named as the tiers are.
						}
					}

					//System.out.printf("QQQ2: %s%n", ti.linguistType.toString());

					// System.out.println(ti.toString());
					// System.out.println(lgqType.getAttribute("LINGUISTIC_TYPE_REF"));
					tierInfos.add(ti);
					// annot_doc.appendChild(lgqType);
				}
			}
		}
		// if no users, should do something
		if (tierInfos.size() < 1) {
			// Create a default participant.
			TierInfo ti = new TierInfo();
			ti.tier_id = "spk1";
			tierInfos.add(ti);
			// add this speaker to all annotations
			NodeList annotationGrps = null;
			try {
				annotationGrps = TeiDocument.getAllAnnotationBloc(this.teiXPath, this.teiDoc);
			} catch (XPathExpressionException e) {
				System.out.println("xpath error on list of annotationBlock");
				e.printStackTrace();
				System.exit(1);
			}
			for (int i = 0; i < annotationGrps.getLength(); i++) {
				Element annotGrp = (Element) annotationGrps.item(i);
				annotGrp.setAttribute("who", "spk1");
			}
		}
		getParticipantNames();
	}

	// Récupération des noms des participants
	void getParticipantNames() {
		NodeList participantsInfo = this.teiDoc.getElementsByTagName("person");
		for (int i = 0; i < participantsInfo.getLength(); i++) {
			Element person = (Element) participantsInfo.item(i);
			NodeList cn = person.getChildNodes();
			for (int j = 0; j < cn.getLength(); j++) {
				if (TeiDocument.isElement(cn.item(j))) {
					Element child = (Element) cn.item(j);
					if (child.getNodeName().equals("altGrp")) {
						NodeList nnl = child.getElementsByTagName("alt");
						for (int z = 0; z < nnl.getLength(); z++) {
							Element alt = (Element)nnl.item(z);
							if (alt.hasAttribute("type")) {
								for (TierInfo ti : tierInfos) {
									// NodeList pn =
									// person.getElementsByTagName("persName");
									// System.err.println(ti.toString());
									if (ti.tier_id.equals(alt.getAttribute("type"))) {
										if (person.getElementsByTagName("persName").getLength() > 0) {
											ti.participant = person.getElementsByTagName("persName").item(0)
													.getTextContent();
										} else {
											ti.participant = "";
										}
									}
								}
							}
						}
					}
				}
			}
		}

	}

	public static String getOriginalFormat(Document doc) {
		NodeList trDesc = doc.getElementsByTagName("transcriptionDesc");
		Element item;
		if (trDesc.getLength() < 1) {
			return "";
		} else {
			item = ((Element)trDesc.item(0));
		}
		String ident = item.getAttribute("ident");
//		String version = item.getAttribute("version");
		return ident;
	}

	public String originalFormat() {
		return getOriginalFormat(teiDoc);
	}
}
