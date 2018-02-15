/**
 * @author Myriam Majdoub
 * Représentations des informations d'un document TEI.
 */

package fr.ortolang.teicorpo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TeiFile {

	// Document TEI à lire
	public Document teiDoc;
	// acces Xpath
	public XPathFactory xPathfactory;
	public XPath xpath;
	// Informations sur la transcription
	public TransInfo transInfo;
	// Transcription
	public Trans trans;
	// Langage du discours
	public String language;
	// compute the timeline
	public TeiTimeline teiTimeline;

	// paramètres
	public TierParams optionsOutput;
	// Lignes principales de la transcriptions (liste d'utterances)
	ArrayList<AnnotatedUtterance> mainLines = new ArrayList<AnnotatedUtterance>();

	// Validation du document Tei par la dtd
	boolean validation = false;

	public TeiFile(File teiFile, TierParams options) {
		optionsOutput = options;
		DocumentBuilderFactory factory = null;
		Element root = null;
		teiTimeline = new TeiTimeline();
		try {
			factory = DocumentBuilderFactory.newInstance();
			Utils.setDTDvalidation(factory, validation);
			DocumentBuilder builder = factory.newDocumentBuilder();
			teiDoc = builder.parse(teiFile);
			root = teiDoc.getDocumentElement();
			xPathfactory = XPathFactory.newInstance();
			xpath = xPathfactory.newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
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

				public Iterator<?> getPrefixes(String val) {
					return null;
				}

				public String getPrefix(String uri) {
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		teiTimeline.buildTimeline(this.teiDoc);
		transInfo = new TransInfo((Element) root.getElementsByTagName("teiHeader").item(0));
		trans = new Trans((Element) root.getElementsByTagName("text").item(0), this);
		transInfo.fileLocation = teiFile.getAbsolutePath();
		language = root.getAttribute("xml:lang");
	}

	public int mainLinesSize() {
		return mainLines.size();
	}


	/**
	 * Représentation de la transcription: situations + énoncés + tiers
	 */
	public class Trans {
		// Liste des div
		public ArrayList<Div> divs = new ArrayList<Div>();
		// Liste des types de tier présents dans la transcription
		public HashSet<String> tierTypes = new HashSet<String>();
		// Situation principale
		public String sit;
		// Autres info
		// public ArrayList<String> infos = new ArrayList<String> ();

		public Trans(Element text, TeiFile tf) {
			// Liste d'éléments contenus dans la transcription (élément text)
			Element body = (Element) text.getElementsByTagName("body").item(0);

			// TEST if there is one div and nothing else
			// at top level there can be only elements nodes
			NodeList bodyChildren = body.getChildNodes();
			int first = -1;
			for (int i = 0; i < bodyChildren.getLength(); i++) {
				Node n = bodyChildren.item(i);
				if (Utils.isElement(n)) {
					if (n.getNodeName().equals("div")) {
						if (first != -1) {
							// more than one div: no episode
							first = -2;
							break;
						}
						first = i;
					} else {
						// not only divs
						first = -2;
						break;
					}
				}
			}

			sit = "";
			if (first >= 0) {
				Element ep = (Element) bodyChildren.item(first);
				// only one div so it's the episode
				// un seul div c'est l'épisode
				// on cherche les infos program et air_date
				// sinon on met type + substype dans program
				String attr = Utils.getDivHeadAttr(ep, "subtype");
				if (Utils.isNotEmptyOrNull(attr))
					sit = tf.transInfo.situations.get(attr);
				String theme = tf.transInfo.situations.get(ep);
				Div d = new Div(tf, ep, attr, theme);
				divs.add(d); // la situation
				for (AnnotatedUtterance u : d.utterances) {
					tierTypes.addAll(u.tierTypes);
				}
			} else {
				String attr = Utils.getDivHeadAttr(body, "subtype");
				if (Utils.isNotEmptyOrNull(attr))
					sit = tf.transInfo.situations.get(attr);
				String theme = Utils.getDivHeadAttr(body, "type");;
				Div d = new Div(tf, body, attr, theme);
				divs.add(d); // la situation
				for (AnnotatedUtterance u : d.utterances) {
					tierTypes.addAll(u.tierTypes);
				}
			}
		}

		/*
		private void processDivAndAnnotation(NodeList bodyChildren, TeiFile tf) {
			for (int i = 0; i < bodyChildren.getLength(); i++) {
				if (Utils.isElement(bodyChildren.item(i))) {
					Element elmt = (Element) bodyChildren.item(i);
					if (elmt.getTagName().equals("div")) {
						String id = Utils.getDivHeadAttr(elmt, "subtype");
						String theme = tf.transInfo.situations.get(id);
						// System.out.printf("BeforeDiv: %s %s %n", id, theme);
						Div d = new Div(tf, elmt, id, theme);
						divs.add(d); // la situation
						for (AnnotatedUtterance u : d.utterances) {
							tierTypes.addAll(u.tierTypes);
						}
					}
				}
			}
		}
		*/
	}

	/**
	 * Représentation des Div
	 */
	public class Div {
		// Thème/sujet du div
		public String theme;
		// Identifiant du thème
		public String themeId;
		// Temps de début du div (en secondes)
		public String start;
		// Temps de fin du div (en secondes)
		public String end;
		// Type du div
		public String type;
		// liste des Utterances contenues dans le Div
		public ArrayList<AnnotatedUtterance> utterances = new ArrayList<AnnotatedUtterance>();
		// Element div
		public Element divElement;

		public Div(TeiFile tf, Element div, String id, String theme) {
			// initialisation des variables d'instances
			this.theme = theme;
			this.themeId = id;
			this.type = Utils.getDivHeadAttr(div, "type");
			this.start = teiTimeline.getTimeValue(Utils.getDivHeadAttr(div, "start"));
			this.end = teiTimeline.getTimeValue(Utils.getDivHeadAttr(div, "end"));
			divElement = div;
			// Noeuds contenus dans le div
			NodeList ch = div.getChildNodes();
			// Fin de sous-div
			boolean eg = false;
			boolean first = true;
			// Parcours des éléments contenus dans le div (au niveau 1)
			for (int i = 0; i < ch.getLength(); i++) {
				if (Utils.isElement(ch.item(i))) {
					Element el = (Element) ch.item(i);
					if (Utils.isAnnotatedBloc(el)) {
						if (getNote(el) != null) {
							Element note = getNote(el);
							AnnotatedUtterance lastU = getLastAnnotU();
							if (lastU != null) {
								lastU.coms.add(note.getAttribute("type") + "\t" + note.getTextContent());
							}
						} else {
							AnnotatedUtterance utt = new AnnotatedUtterance();
							utt.codes = optionsOutput.codes;
							utt.process(el, teiTimeline, transInfo, optionsOutput, true);
							// Si c'est le premier u, on lui ajoute le type du
							// div
							if (first == true) {
								utt.type = this.type + "\t" + this.themeId;
								if (this.type.toLowerCase().startsWith("bg")) {
									// Si le type est Bg, on indique qu'il
									// s'agit d'un sous-div
									eg = true;
								}
								first = false;
							}
							// Ajout de l'utterance: dans la liste d'utterances
							// du div, et dans la liste d'utterance principale
							// de la transcription
							this.utterances.add(utt);
							mainLines.add(utt);
							//System.out.printf("%d %s%n", utt.tierTypes.size(), utt.speeches.get(0));
						}
					}
					// Cas sous-div
					else if (el.getNodeName().equals("div")) {
						String elid = Utils.getDivHeadAttr(el, "subtype");
						String eltheme = tf.transInfo.situations.get(elid);
						Div subdiv = new Div(tf, el, elid, eltheme);
						this.utterances.addAll(subdiv.utterances);
					}
				}
			}
			if (eg) {
				AnnotatedUtterance lastU = getLastAnnotU();
				lastU.type = "Eg\t" + this.themeId;
			}
		}

		public AnnotatedUtterance getLastAnnotU() {
			AnnotatedUtterance lastAnnotU = null;
			try {
				lastAnnotU = this.utterances.get(utterances.size() - 1);
			} catch (Exception e) {
				if (!this.utterances.isEmpty()) {
					lastAnnotU = this.utterances.get(0);
				}
			}
			return lastAnnotU;
		}
	}

	public static Element getNote(Element e) {
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals("note")) {
				return (Element) nl.item(i);
			}
		}
		return null;
	}

	// Vérifie si l'élément contient au moins un div
	public static boolean containsDiv(Element div) {
		NodeList cn = div.getChildNodes();
		int l = cn.getLength();
		for (int i = 0; i < l; i++) {
			if (cn.item(i).getNodeName().equals("div")) {
				return true;
			}
		}
		return false;
	}

	// Récupération d'un type de note
	public static Element getNote(String type, Element notesStmt) {
		Element addNotes = (Element) notesStmt.getElementsByTagName("note").item(0);
		NodeList notes = addNotes.getElementsByTagName("note");
		for (int i = 0; i < notes.getLength(); i++) {
			if (Utils.isElement(notes.item(i))) {
				Element note = (Element) notes.item(i);
				if (note.getAttribute("type").equals(type)) {
					return note;
				}
			}
		}
		return null;
	}

	public static int getDivOcc(NodeList nl) {
		int divOcc = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals("div")) {
				divOcc++;
			}
		}
		return divOcc;
	}

	// Main
	public static void main(String[] args) {
		TeiFile tf = new TeiFile(new File(args[0]), null);
		for (Div d : tf.trans.divs) {
			for (AnnotatedUtterance u : d.utterances) {
				System.out.print(u.toString());
			}
		}
	}

	public String originalFormat() {
		return TeiToPartition.getOriginalFormat(teiDoc);
	}
}
