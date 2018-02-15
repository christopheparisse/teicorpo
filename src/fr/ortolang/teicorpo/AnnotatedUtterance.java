/**
 * @author Myriam Majdoub & Christophe Parisse
 * Représentations des informations d'un énoncé en format texte à partir d'un format TEI.
 * 
 */

package fr.ortolang.teicorpo;

import java.util.ArrayList;
import java.util.HashSet;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.ortolang.teicorpo.Codes;

/**
 * Représentation des objets Utterance
 */
public class AnnotatedUtterance {
	// codes for utterance display
	public Codes codes;
	
	// Element annotatedU
	public Element annotU;
	// Identifiant
	public String id;
	private int nthid;
	// Temps de début(chaîne de caractère, unité = secondes)
	public String start;
	//// Temps de fin(chaîne de caractère, unité = secondes)
	public String end;
	
	// working elements (copied and organized in speeches)
	// Code utilisé pour le locuteur
	public String speakerCode;
	// Nom du locuteur
	public String speakerName;
	// énoncé (avec bruits, pauses...)
	public String speech;
	// Enoncé pur
	public String nomarkerSpeech;
	// mor line for clan
	public String morClan;
	
	// Marque de div: si l'utterance marque le début d'un div, le type de
	// div est spécifié dans ce champ
	// Servira à repérer les divisions dans la transcription
	public String type;
	
	// organized structure
	// Commentaires additionnels hors tiers
	public ArrayList<String> coms;
	// Liste d'énoncés - one Annot per <u>
	public ArrayList<Annot> speeches;
	// Liste de tiers
	public ArrayList<Annot> tiers;
	// Liste des types de tiers
	public HashSet<String> tierTypes;

	public String shortPause;
	public String longPause;
	public String veryLongPause;

	public TeiTimeline teiTimeline;
	public TierParams optionsTEI = null;
	
	AnnotatedUtterance() {
		codes = new Codes();
		codes.standardCodes();
	}

	/**
	 * Impression des utterances : liste des énoncés
	 */
	public String toString() {
		String s = "Id[" + id + "] Type[" + type + "] Start[" + start + "] End[" + end + "] SpkName[" + speakerName + "]\n";
		s += "Speeches[" + speeches.size() + "]\n";
		for (Annot utt : speeches) {
			s += "{" + utt.getContent(false) + "} {" + utt.getContent(true) + "}\n";
		}
		s += "Tiers[" + tiers.size() + "]\n";
		for (Annot tier : tiers) {
			s += "/" + tier.name + "/ /" + tier.getContent(false) + "/\n";
		}
		return s;
	}

	public boolean process(Element annotatedU, TeiTimeline teiTimeline, TransInfo transInfo, TierParams options, boolean doSpan) {
		optionsTEI = options;
		morClan = "";
		this.teiTimeline = teiTimeline;
		// Initialisation des variables d'instances
		if (options != null && options.outputFormat == ".cha") {
			shortPause = Utils.shortPauseCha;
			longPause = Utils.longPauseCha;
			veryLongPause = Utils.veryLongPauseCha;
		} else {
			shortPause = Utils.shortPause;
			longPause = Utils.longPause;
			veryLongPause = Utils.veryLongPause;
		}
		annotU = annotatedU;
		id = Utils.getAttrAnnotationBloc(annotatedU, "xml:id");
		nthid = 0;
		if (teiTimeline != null) {
			start = teiTimeline.getTimeValue(Utils.getAttrAnnotationBloc(annotatedU, "start"));
			end = teiTimeline.getTimeValue(Utils.getAttrAnnotationBloc(annotatedU, "end"));
		} else {
			start = Utils.getAttrAnnotationBloc(annotatedU, "start");
			end = Utils.getAttrAnnotationBloc(annotatedU, "end");
		}
		speakerCode = Utils.getAttrAnnotationBloc(annotatedU, "who");
		coms = new ArrayList<String>();
		speeches = new ArrayList<Annot>();
		tiers = new ArrayList<Annot>();
		tierTypes = new HashSet<String>();
		if (options != null) {
			if (options.isDontDisplay(speakerCode, 1))
				return false;
			if (!options.isDoDisplay(speakerCode, 1))
				return false;
		}
		//System.out.printf("Yes.%n");
		if (transInfo != null)
			speakerName = transInfo.getParticipantName(speakerCode);
		else
			speakerName = "";
		NodeList annotUElements = annotatedU.getChildNodes();
		// Parcours des éléments contenus dans u et construction des
		// variables speech, nomarkerSpeech et speeches en fonction.
		for (int i = 0; i < annotUElements.getLength(); i++) {
			if (Utils.isElement(annotUElements.item(i))) {
				Element annotUEl = (Element) annotUElements.item(i);
				String nodeName = annotUEl.getNodeName();
				/*
				 * Cas élément "u": dans ce cas, on concatène tous les u pour
				 * former nomarkerSpeech, de même pour speech mais en incluant
				 * les éventuels bruits/incidents qui ont lieu lors de l'énoncé
				 * (sous la forme d'une chaîne de caractère prédéfinie). On
				 * ajoute tous les énoncés dans speeches.
				 */
				if (nodeName.equals("u")) {
					nomarkerSpeech = "";
					speech = "";
					NodeList us = annotUEl.getChildNodes();
					processSeg(us);
					//System.out.printf("TTTTT : %s%n", speech);
					speech = Utils.cleanStringPlusEntities(speech);
					nomarkerSpeech = Utils.cleanStringPlusEntities(nomarkerSpeech);
					Annot a = new Annot(speakerName, start, end, speech, nomarkerSpeech);
					if (nthid == 0) {
						a.id = id;
						nthid++;
					} else {
						a.id = id + nthid;
						nthid++;
					}
					speeches.add(a);
					// System.out.printf("TTTTT endofseg: %s %s %s%n", start, end, speech);
				} else if (nodeName.equals("spanGrp") && doSpan == true) {
					// Ajout des tiers
					String type = annotUEl.getAttribute("type");
					// if the tier should not be displayed, skip them
					if (options != null) {
						if (options.level == 1)
							continue;
						if (options.isDontDisplay(type, 2))
							continue;
						if (!options.isDoDisplay(type, 2))
							continue;
					}
					if (type.equals("conll")) {
						String conll = "";
						NodeList spans = annotUEl.getChildNodes();
						for (int k=0; k<spans.getLength(); k++) {
							String conllLine = "";
							Node n = spans.item(k);
							if (n.getNodeName().equals("span")) {
								NodeList spanElts = n.getChildNodes();
								for (int l=0; l<spanElts.getLength(); l++) {
									Node m = spanElts.item(l);
									if (m.getNodeName().equals("spanGrp")) {
										String text = m.getTextContent();
										// String attr = ((Element)m).getAttribute("type");
										conllLine += (conllLine.isEmpty()) ? text.trim() : "|" + text.trim();
									}
								}
							}
							conll += (conll.isEmpty()) ? conllLine : " " + conllLine;
						}
						tiers.add(new Annot("conll", conll));
					} else if (type.equals("treetagger")) {
						// ref.
						// skip the ref element and go directly to w elements
						String morpho = "";
						NodeList refs = annotUEl.getElementsByTagName("w");
						for (int x = 0; x < refs.getLength(); x++) {
							Node w = refs.item(x);
							if (w.getNodeType() == Node.ELEMENT_NODE) {
								Element we = (Element)w;
//								System.err.println(w.toString() + "++" + w.getTextContent());
								if (!morpho.isEmpty()) morpho += " ";
								morpho += we.getAttribute("ana") + "_";
								morpho += we.getAttribute("lemma") + "_";
								morpho += w.getTextContent();
							}
						}
						tiers.add(new Annot("morpho", morpho));
					} else {
						NodeList spans = annotUEl.getElementsByTagName("span");
						for (int y = 0; y < spans.getLength(); y++) {
							Element span = (Element) spans.item(y);
							tiers.add(new Annot(type, processSpan(span)));
							if (!type.equals("pho") && !type.equals("act") && !type.equals("sit") && !type.equals("com")
									&& !type.equals("morpho")) {
								tierTypes.add("other");
							} else
								tierTypes.add(type);
						}
					}
				}
			}
		}
		if (!morClan.isEmpty())
			tiers.add(new Annot("mor", morClan));
		return true;
	}

	static public String processSpan(Element span) {
		String spanContent = "";
		// the span might be decomposed into elements.
		NodeList spanElements = span.getChildNodes();
		for (int k = 0; k < spanElements.getLength(); k++) {
			Node elt = spanElements.item(k);
			if (elt.getNodeType() == Node.TEXT_NODE) {
				if (!spanContent.isEmpty()) spanContent += " ";
				spanContent += elt.getTextContent();
				// text.
			} else {
				// not text.
				// spanContent += " " + elt.getTextContent();
				// this should be processed in including spanGrp and span
				// add all content without special analysis (but special analysis could be interesting in some cases)
				if (!spanContent.isEmpty()) spanContent += " ";
				spanContent += elt.getTextContent();
			}
		}
		return spanContent;
	}

	public void processSeg(NodeList us) {
		//System.out.printf("KKKKK processSeg%n");
		for (int z = 0; z < us.getLength(); z++) {
			Node segChild = us.item(z);
			String segChildName = segChild.getNodeName();
			//int segType = segChild.getNodeType();
			//System.out.printf("%d %s %d%n", z, segChildName, segType);

			// Ajout des pauses: syntaxe = # pour les pauses courtes, sinon
			// ### pour les pauses longues
			if (Utils.isElement(segChild)) {
				Element segChildEl = (Element) segChild;
				if (segChildName.equals("pause")) {
					if (segChildEl.getAttribute("type").equals("short")) {
						// nomarkerSpeech += genericPause;
						speech += shortPause;
					} else if (segChildEl.getAttribute("type").equals("long")) {
						// nomarkerSpeech += genericPause;
						speech += longPause;
					} else if (segChildEl.getAttribute("type").equals("verylong")) {
						// nomarkerSpeech += genericPause;
						speech += veryLongPause;
					} else if (segChildEl.getAttribute("type").equals("chrono")) {
						String chronoPause = " " + (
								(optionsTEI != null && optionsTEI.outputFormat == ".cha")
								? String.format(Utils.specificPause, segChildEl.getAttribute("dur"))
								: String.format(Utils.specificPauseCha, segChildEl.getAttribute("dur"))
								) + " ";
						// nomarkerSpeech += genericPause;
						speech += chronoPause;
					}
				}

				// Ajout des évènement (éléments incident & vocal):
				// syntaxe = * type|subtype|desc1 desc2 ... descN
				else if (segChildName.equals("incident")) {
					String st = segChildEl.getAttribute("subtype");
					String val = getIncidentDesc(segChildEl);
					if (segChildEl.getAttribute("type").equals("pronounce")) {
						String ann = codes.leftEvent + val;
						if (Utils.isNotEmptyOrNull(st))
							ann += " /" + st + "/PHO" + codes.rightEvent + " ";
						else
							ann += codes.rightEvent + " ";
						speech += ann;
					} else if (segChildEl.getAttribute("type").equals("language")) {
						String ann = codes.leftCode;
						if (Utils.isNotEmptyOrNull(st))
							ann += "/" + st;
						if (Utils.isNotEmptyOrNull(val))
							ann += "/LG:" + val + codes.rightCode + " ";
						else
							ann += "/LG" + codes.rightCode + " ";
						speech += ann;
					} else if (segChildEl.getAttribute("type").equals("noise")) {
						String ann = codes.leftEvent + val;
						ann += " /";
						if (Utils.isNotEmptyOrNull(st))
							ann += st + "/";
						ann += "N" + codes.rightEvent + " ";
						speech += ann;
					} else if (segChildEl.getAttribute("type").equals("comment")) {
						String ann = codes.leftEvent + val;
						ann += " /";
						if (Utils.isNotEmptyOrNull(st))
							ann += st + "/";
						ann += "COM" + codes.rightEvent + " ";
						speech += ann;
					} else if (segChildEl.getAttribute("type").equals("background")) {
						String ann = codes.leftEvent + val;
						String tm = getIncidentDescAttr(segChildEl, "time");
						if (tm != null && teiTimeline != null)
							tm = teiTimeline.getTimeValue(tm);
						String lv = getIncidentDescAttr(segChildEl, "level");
						ann += " /";
						if (Utils.isNotEmptyOrNull(st))
							ann += st;
						ann += "/";
						if (Utils.isNotEmptyOrNull(tm))
							ann += tm;
						ann += "/";
						if (Utils.isNotEmptyOrNull(lv))
							ann += lv;
						ann += "/B" + codes.rightEvent + " ";
						speech += ann;
					} else {
						String ann = codes.leftCode + val;
						if (Utils.isNotEmptyOrNull(st))
							ann += " /" + st;
						ann += "/";
						switch (segChildEl.getAttribute("type")) {
						case "lexical":
							ann += "LX";
							break;
						case "entities":
							ann += "NE";
							break;
						}
						ann += codes.rightCode + " ";
						speech += ann;
					}
				} else if (segChildName.equals("vocal")) {
					String vocal = "";
					try {
						vocal += segChildEl.getElementsByTagName("desc").item(0).getTextContent();
					} finally {
						nomarkerSpeech += vocal + " ";
						speech += codes.leftCode + vocal + "/VOC " + codes.rightCode;
					}
				} else if (segChildName.equals("seg")) {
					processSeg(segChildEl.getChildNodes());
				} else if (segChildName.equals("anchor") && !segChildEl.getAttribute("synch").startsWith("#au")) {
					String sync = "";
					if (teiTimeline != null)
						sync = teiTimeline.getTimeValue(segChildEl.getAttribute("synch"));
					else
						sync = segChildEl.getAttribute("synch");
					// creer une ligne avec speech, nomarkerSpeech, addspeech
					Annot a = new Annot(speakerName, start, sync, speech, nomarkerSpeech);
					if (nthid == 0) {
						a.id = id;
						nthid++;
					} else {
						a.id = id + nthid;
						nthid++;
					}
					speeches.add(a);
					// System.out.printf("anchor: %s %s %s %s%n", speakerName,
					// start, sync, speech);
					start = sync;
					speech = "";
					nomarkerSpeech = "";
				}
				// Tiers de type "morpho"
				else if (segChildName.equals("morpho")) {
					String tierName = "morpho";
					String tierMorpho = "";
					NodeList cN = ((Element) segChild).getElementsByTagName("w");
					for (int j = 0; j < cN.getLength(); j++) {
						Node w = cN.item(j);
						tierMorpho += w.getTextContent();
						NamedNodeMap attrs = w.getAttributes();
						for (int jz = 0; jz < attrs.getLength(); jz++) {
							Node att = attrs.item(jz);
							tierMorpho += "|" + att.getNodeName() + ":" + att.getNodeValue();
						}
						tierMorpho = " ";
					}
					tiers.add(new Annot(tierName, tierMorpho));
					tierTypes.add("morpho");
				}
				// Splitted into <w> "w"
				else if (segChildName.equals("w")) {
					String ana = segChildEl.getAttribute("ana");
					String lemma = segChildEl.getAttribute("lemma");
					String w = segChildEl.getTextContent();
					speech += (lemma.isEmpty())
							? w 
							: " " + w;
					nomarkerSpeech += w + " ";
					morClan += lemma + "|" + ana + " ";
				}
				else {
					speech += segChildEl.getTextContent() + " ";
					nomarkerSpeech += segChildEl.getTextContent() + " ";
				}
			} else if (segChild.getNodeName().equals("#text")) {
				String content = segChild.getTextContent() + " ";
				if (Utils.isNotEmptyOrNull(content.trim())) {
					speech += content;
					nomarkerSpeech += content;
					// Dans speeches, pour chaque énoncé (seg), il peut y
					// avoir des marques temporelles
					// On ajoute donc chaque énoncé sous cette forme
					// start;end__speech
				}
			}
		}
	}

	// Récupération d'un type de tier donné (passé en paramètre)

	public ArrayList<Annot> getTier(String typeTier) {
		ArrayList<Annot> t = new ArrayList<Annot>();
		if (typeTier.equals("pho") || typeTier.equals("act") || typeTier.equals("sit") || typeTier.equals("com")
				|| typeTier.equals("morpho")) {
			for (Annot tier : this.tiers) {
				if (typeTier.equals(tier.name)) {
					t.add(tier);
				}
			}
		} else {
			for (Annot tier : this.tiers) {
				String tpT = tier.name;
				if (!tpT.equals("pho") && !tpT.equals("act") && !tpT.equals("sit") && !tpT.equals("com")
						&& !tpT.equals("morpho")) {
					t.add(tier);
				}
			}
		}
		return t;
	}

	public String joinString(String[] stringSplit, int begin, int end) {
		String sentence = "";
		for (int i = begin; i < end; i++) {
			sentence += stringSplit[i] + " ";
		}
		return sentence;
	}

	public String getIncidentDescAttr(Element incident, String attName) {
		NodeList descs = incident.getElementsByTagName("desc");
		if (attName.isEmpty()) {
			for (int i = 0; i < descs.getLength(); i++) {
				Element desc = (Element) descs.item(i);
				if (desc.getAttribute("type") == null) {
					return desc.getTextContent();
				}
			}
			return "";
		} else {
			for (int i = 0; i < descs.getLength(); i++) {
				Element desc = (Element) descs.item(i);
				if (desc.getAttribute("type").equals(attName)) {
					return desc.getTextContent();
				}
			}
			return "";
		}
	}

	public String getIncidentDesc(Element incident) {
		NodeList descs = incident.getElementsByTagName("desc");
		String v = "";
		for (int i = 0; i < descs.getLength(); i++) {
			Element desc = (Element) descs.item(i);
			v += desc.getTextContent();
		}
		return v;
	}
}
