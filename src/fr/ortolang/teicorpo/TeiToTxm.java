/**
 * Conversion d'un fichier TEI en un fichier TXM.
 * @author Christophe Parisse
 */
package fr.ortolang.teicorpo;

import java.io.IOException;
//import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fr.ortolang.teicorpo.TeiFile.Div;
import org.w3c.dom.NodeList;

public class TeiToTxm extends TeiConverter {

	// Document Txm à écrire
	public Document txmDoc;
	// Extension du fichier de sortie
	final static String EXT = ".txm.xml";
	
	Element txm; // root of document
	Element texthead; // put all divs inside texthead
	Element divhead; // put all utterances inside divhead
	Map<String, String> locAges; // precomputed locuteur ages
	static public String defaultAge = "40";

	/**
	 * Convertit le fichier TEI donné en argument en un fichier Txm.
	 * 
	 * @param inputName
	 *            Nom du fichier d'entrée (fichier TEI, a donc l'extenstion
	 *            .teiml)
	 * @param outputName
	 *            Nom du fichier de sortie (fichier TXM, a donc l'extenson .txm.xml)
	 */
	public void transform(String inputName, String outputName, TierParams optionsTei) {
		if (!optionsTei.rawLine) optionsTei.outputFormat = ".txm";
		init(inputName, outputName, optionsTei);
		if (this.tf == null)
			return;
		optionsOutput.computeMvValues(tf);
		typeDiv = "";
		locAges = new HashMap<String, String>();
		outputWriter();
		conversion();
		createOutput();
	}

	@Override
	public void outputWriter() {
		txmDoc = null;
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			txmDoc = builder.newDocument();
			txm = txmDoc.createElement("txm");
			txmDoc.appendChild(txm);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createOutput() {
		Utils.createFile(txmDoc, outputName);
	}

	/**
	 * Conversion
	 */
	public void conversion() {
		// Etapes de conversion
		buildHeader();
		buildText();
	}

	/**
	 * Ecriture de l'en-tête du fichier Srt en fonction du fichier TEI à
	 * convertir
	 */
	public void buildHeader() {
		txm.setAttribute("file", inputName);
		texthead = txmDoc.createElement("text");
		txm.appendChild(texthead);
	}

	/**
	 * Ecriture de la partie transcriptions: énoncés + tiers
	 */
	public void buildText() {
		ArrayList<TeiFile.Div> divs = tf.trans.divs;
		if (divs.size() == 1) {
			// sets the attribute of the main div to the text
			// and then read normally each subdiv
			divhead = texthead;
			Div d = divs.get(0);
			setTv(divhead, "");
			if (Utils.isNotEmptyOrNull(d.themeId)) divhead.setAttribute("id", d.themeId);
			if (Utils.isNotEmptyOrNull(d.theme)) divhead.setAttribute("theme", d.theme);
			processDiv(d);
		} else {
			for (Div d : divs) {
				// creates a div and set correct attributes
				divhead = txmDoc.createElement("div");
				texthead.appendChild(divhead);
				// all other elements will be included in head one by one to divhead
				setTv(divhead, "");
				if (Utils.isNotEmptyOrNull(d.themeId)) divhead.setAttribute("id", d.themeId);
				if (Utils.isNotEmptyOrNull(d.theme)) divhead.setAttribute("theme", d.theme);
				processDiv(d);
			}
		}
	}

	private void processDiv(Div d) {
		for (AnnotatedUtterance u : d.utterances) {
			if (Utils.isNotEmptyOrNull(u.type)) {
				// create a new div
				// add the themeid or the theme to the div (theme for clan and themeid for transcriber)
				divhead = txmDoc.createElement("div");
				texthead.appendChild(divhead);
				setTv(divhead, "");
				if (Utils.isNotEmptyOrNull(u.themeId)) divhead.setAttribute("id", u.themeId);
				if (Utils.isNotEmptyOrNull(u.theme)) divhead.setAttribute("theme", u.theme);
			}
			bgCase(u);
			writeUtterance(u);
		}
	}

	/**
	 * Ecriture des div: si c'est un div englobant: G, si c'est un sous-div:
	 * Bg/Eg
	 * 
	 * @param type
	 *            le type de div à écrire
	 * @param themeId
	 *            le theme du div à écrire
	public void writeDiv(String type, String themeId) {
		String theme = Utils.cleanString(tf.transInfo.situations.get(themeId));
		System.out.println("DIV" + type + "\t" + theme);
	}
	 */

	/**
	 * Ecriture d'un énonce: lignes qui commencent par le symbole étoile *
	 * 
	 * @param u
	 *            Utterance (contains Locuteur)
	 * @param speechContent
	 *            Contenu de l'énoncé
	 * @param startTime
	 *            Temps de début de l'énoncé
	 * @param endTime
	 *            Temps de fin de l'énoncé
	 */
	public void writeSpeech(AnnotatedUtterance u, String speechContent, String startTime, String endTime) {
		// System.err.println("writeSpeech: " + optionsOutput.syntaxformat);
/*		if (optionsOutput.syntaxformat.equals("conll")) { // || optionsOutput.syntax.equals("treetagger")) {
			System.out.println("skip writeSpeech");
			return;
		}
*/
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(spkChoice(u))) // TODO: loc
				return;
			if (!optionsOutput.isDoDisplay(spkChoice(u))) // TODO: loc
				return;
		}
		// System.err.println("writeSpeech: " + optionsOutput.syntaxformat);
		if (!optionsOutput.syntaxformat.equals("ref") && !optionsOutput.syntaxformat.equals("conll")) {
			// System.err.println("writeSpeech0: " + optionsOutput.syntaxformat);
			// get loc age
			String age = getLocAge(u.speakerCode);
			Element e = generateUStart(u, startTime, endTime, age);
			generateU(e, speechContent, u);
		}
		// if this is the ref case or the conll, the result will be written by the tier in writeTier function
	}

	Element generateUStart(AnnotatedUtterance u, String startTime, String endTime, String age) {
		// System.err.println(loc + ' ' + startTime + ' ' + endTime + ' ' + age);
		// Si le temps de début n'est pas renseigné, on mettra par défaut le
		// temps de fin (s'il est renseigné) moins une seconde.
		if (!Utils.isNotEmptyOrNull(startTime)) {
			if (Utils.isNotEmptyOrNull(endTime)) {
				float start = Float.parseFloat(endTime) - 1;
				startTime = Float.toString(start);
			}
		}
		// Si le temps de fin n'est pas renseigné, on mettra par défaut le temps
		// de début (s'il est renseigné) plus une seconde.
		else if (!Utils.isNotEmptyOrNull(endTime)) {
			if (Utils.isNotEmptyOrNull(startTime)) {
				float end = Float.parseFloat(startTime) + 1;
				endTime = Float.toString(end);
			}
		}

		Element p = null;
		if (optionsOutput.writtentext == false) {
			p = txmDoc.createElement("p");
			String s = "<meta><br/>[" + spkChoice(u) + "]</meta>";
			if (Utils.isNotEmptyOrNull(endTime) && Utils.isNotEmptyOrNull(startTime)) {
				s += "<meta>[TIME " + Double.toString(Double.parseDouble(startTime)) + " ]</meta>";
				// elt.setAttribute("end", Double.toString(Double.parseDouble(endTime)));
				// u.setTextContent(speechContent);
			}
			Element e = Utils.convertStringToElement("<segline>" + s + "</segline>");
			if (e != null) {
				Node imported = txmDoc.adoptNode(e.cloneNode(true)); // .importNode(e, true);
				appendAllChildren(p, imported);
				// p.appendChild(imported);
			} else {
				p.setTextContent(s);
			}
		}
		Element elt = txmDoc.createElement("u");
		// On ajoute les informations temporelles seulement si on a un temps de
		// début et un temps de fin
		if (Utils.isNotEmptyOrNull(endTime) && Utils.isNotEmptyOrNull(startTime)) {
			elt.setAttribute("who", spkChoice(u)); //.replaceAll("[ _]", "-"));
			elt.setAttribute("start", Double.toString(Double.parseDouble(startTime)));
			elt.setAttribute("end", Double.toString(Double.parseDouble(endTime)));
			// u.setTextContent(speechContent);
		} else {
			elt.setAttribute("who", spkChoice(u)); //.replaceAll("[ _]", "-"));
			elt.setAttribute("start", "");
			elt.setAttribute("end", "");
			// u.setTextContent(speechContent);
		}
		// u.setAttribute("loc", loc);
		if (age != null) elt.setAttribute("age", age);
		setTv(elt, spkChoice(u));
		if (optionsOutput.writtentext == false) {
			divhead.appendChild(p);
			p.appendChild(elt);
		} else {
			divhead.appendChild(elt);
		}
		return elt;
	}

	private void appendAllChildren(Element p, Node imported) {
		NodeList nl = imported.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			p.appendChild(nl.item(i));
		}
	}

	@Override
	public void writeAddInfo(AnnotatedUtterance u) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeTier(AnnotatedUtterance au, Annot tier) {
		// System.err.println("writeTier1: " + tier.toString() + " syntf " + optionsOutput.syntaxformat + " name: " + tier.name);
		if (optionsOutput.syntaxformat.equals("conll") && tier.name.equals("conll")) {
			// System.out.println("writeTier2: " + tier.toString());
			// get loc age
			String age = getLocAge(au.speakerCode);
			Element u = generateUStart(au, au.start, au.end, age);
			String spkcode = au.speakerCode; // .replaceAll("[ _]", "-");
			// tier.name
			if (tier.dependantAnnotations != null) {
				// System.out.println("dep: " + tier.dependantAnnotations.toString());
				if (optionsOutput.tiernames) {
					Element we = txmDoc.createElement("w");
					// we.setTextContent(w);
					we.setTextContent("["+spkcode+"]");
                    if (optionsOutput.tiernamescontent) {
						setCode(we, spkChoice(au), age);
						setTv(we, spkChoice(au));
						setDiv(we);
                    }
					u.appendChild(we);
				}
				for (int i=0; i < tier.dependantAnnotations.size(); i++) {
					Annot aw = tier.dependantAnnotations.get(i);
					// System.out.println("writeConnl3 " + aw.toString());
					Element we = txmDoc.createElement("w");
					setCode(we, spkChoice(au), age);
					setTv(we, spkChoice(au));
					for (int k=0; k < aw.dependantAnnotations.size(); k++) {
						Annot kw = aw.dependantAnnotations.get(k);
						if (kw.name.equals("word")) {
							String m = kw.getContent().trim(); //.replaceAll("[ _]", "-");
							we.setTextContent(m);
							if (optionsOutput.sandhi) {
								setSandhiInfo(m, we);
							}
//						} else if (kw.name.equals("pos")) {
//							we.setAttribute("pos", kw.getContent().trim().replaceAll("[ _]", "-"));
//						} else if (kw.name.equals("lemma")) {
//							we.setAttribute("lemma", kw.getContent().trim().replaceAll("[ _]", "-"));
						} else {
							we.setAttribute(kw.name, kw.getContent().trim()); //.replaceAll("[ _]", "-"));
						}
					}
					u.appendChild(we);
				}
			}
		} else if (optionsOutput.syntaxformat.equals("ref") && tier.name.equals("ref")) {
			// System.err.println("writeTier3: " + tier.toString());
			// get loc age
			String age = getLocAge(au.speakerCode);
			Element u = generateUStart(au, au.start, au.end, age);
			String spkcode = au.speakerCode; //.replaceAll("[ _]", "-");
			// tier.name
			
			if (tier.pptRef != null) {
				//System.out.println(tier.pptRef);
				if (optionsOutput.tiernames) {
					Element we = txmDoc.createElement("w");
					// we.setTextContent(w);
					we.setTextContent("["+spkcode+"]");
                    if (optionsOutput.tiernamescontent) {
                    	setCode(we, spkChoice(au), age);
                    	setTv(we, spkChoice(au));
						we.setAttribute("pos", "SENT");
						we.setAttribute("lemma", "_");
                    }
					u.appendChild(we);
				}
				for (int x = 0; x < tier.pptRef.getLength(); x++) {
					Node w = tier.pptRef.item(x);
					if (w.getNodeType() == Node.ELEMENT_NODE) {
						Element wo = (Element)w;
						Element we = txmDoc.createElement("w");
//						System.err.println(w.toString() + "++" + w.getTextContent());
						setCode(we, spkChoice(au), age);
						setTv(we, spkChoice(au));
						String m = wo.getAttribute("pos");
						m = m.trim(); //.replaceAll("[ _]", "-");
						we.setAttribute("pos", m);

						m = wo.getAttribute("lemma");
						m = m.trim(); //.replaceAll("[ _]", "-");
						we.setAttribute("lemma", m);
						
						m = wo.getTextContent().trim(); //.replaceAll("[ _]", "-");
						we.setTextContent(m);
						if (optionsOutput.sandhi) {
							setSandhiInfo(m, we);
						}

						u.appendChild(we);
					}
				}
			}
			//System.out.println(u);
		}
	}

	private void setCode(Element we, String spkcode, String age) {
		if (!spkcode.isEmpty()) {
			we.setAttribute("loc", spkcode);
			we.setAttribute("age", age);
		}
	}

	private void setTv(Element we, String spkcode) {
		// fixed values to be added
		for (Map.Entry<String, SpkVal> entry : optionsOutput.tv.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().genericvalue; //.replaceAll("[ _]", "-");
			we.setAttribute(key, value);
		}
		// metadata values to be added
		for (Map.Entry<String, SpkVal> entry : optionsOutput.mv.entrySet()) {
			String key = entry.getKey();
			SpkVal vs = entry.getValue();
//			System.out.printf("setTv:mv: %s %s %s%n", key, vs.genericspk, vs.genericvalue);
			if (vs.genericspk.isEmpty()) {
				if (!vs.genericvalue.isEmpty()) we.setAttribute(key, vs.genericvalue);
			} else {
				if (vs.list.containsKey(spkcode)) {
					String c = vs.list.get(spkcode);
					if (!c.isEmpty()) we.setAttribute(key, c);
				}
			}
		}
	}

	private void setDiv(Element we) {
		if (!typeDiv.isEmpty())
			we.setAttribute("div", typeDiv);
	}

	private void setSandhiInfo(String word, Element we) {
		String init = word.substring(0,1);
		we.setAttribute("initletter", init);
		switch(init) {
			case "a":
			case "e":
			case "i":
			case "o":
			case "u":
			case "é":
			case "è":
			case "ê":
			case "ë":
			case "à":
			case "â":
			case "ä":
			case "ô":
			case "ö":
			case "ù":
			case "û":
			case "ü":
			case "y":
				we.setAttribute("initclass", "voy");
				break;
			case "h":
				we.setAttribute("initclass", "h");
				break;
			default:
				we.setAttribute("initclass", "cons");
				break;
		}
		String last = word.substring(word.length()-1);
		we.setAttribute("finalletter", last);
		switch(last) {
			case "z":
			case "s":
			case "x":
				we.setAttribute("lastclass", "liaison-z");
				break;
			case "r":
				we.setAttribute("lastclass", "liaison-r");
				break;
			case "k":
			case "c":
				we.setAttribute("lastclass", "liaison-k");
				break;
			case "t":
				we.setAttribute("lastclass", "liaison-t");
				break;
			case "n":
				we.setAttribute("lastclass", "liaison-n");
				break;
			default:
				we.setAttribute("lastclass", "none");
				break;
		}
		if (word.endsWith("re"))
			we.setAttribute("lastclass", "ench-r");
		else if (word.endsWith("ne"))
			we.setAttribute("lastclass", "ench-n");
		else if (word.endsWith("l"))
			we.setAttribute("lastclass", "ench-l");
		else if (word.endsWith("ne"))
			we.setAttribute("lastclass", "ench-n");
		else if (word.endsWith("que"))
			we.setAttribute("lastclass", "ench-k");
	}

	void generateU(Element elt, String speechContent, AnnotatedUtterance u) {
		if (optionsOutput.utterance) {
			// do not split line
			// in this case the marks for tiernames are automatically included
			// so it is not necessary to add them

			// speechContent was modified here to surround the [] and {} information with <meta> tags
			// this is handled by the option outputFormat == "TXM"
			Element e = Utils.convertStringToElement("<segline>" + speechContent + "</segline>");
			if (e != null) {
				Node imported = txmDoc.adoptNode(e.cloneNode(true)); // .importNode(e, true);
				appendAllChildren(elt, imported);
				// elt.appendChild(imported);
			} else {
				elt.setTextContent(speechContent);
			}
			// but we can add the attributes information
			if (optionsOutput.tiernamescontent) {
				String age = getLocAge(u.speakerCode);
				setCode(elt, spkChoice(u), age);
				setTv(elt, spkChoice(u));
				setDiv(elt);
			}
			return;
		}

		/*
		 * Tokenize the line.
		 */
		// String[] s = speechContent.split("\\s+");
		ArrayList<String> p = Tokenizer.splitTextTT(speechContent);
		// for (int ti = 0; ti < p.size(); ti++) out.printf("%s%n", p.get(ti));
		/*
		 * write word information
		 */
		String age = getLocAge(u.speakerCode);
		if (optionsOutput.tiernames) {
			Element we = txmDoc.createElement("w");
			// we.setTextContent(w);
			we.setTextContent("[" + spkChoice(u) + "]");
    		if (optionsOutput.tiernamescontent) {
				setCode(we, spkChoice(u), age);
				setTv(we, spkChoice(u));
				setDiv(we);
            }
			elt.appendChild(we);
		}
		// for (String w: s) {
		for (int ti = 0; ti < p.size(); ti++) {
			Element we = txmDoc.createElement("w");
			// we.setTextContent(w);
			we.setTextContent(p.get(ti));
			setCode(we, spkChoice(u), age);
			setTv(we, spkChoice(u));
			setDiv(we);
			elt.appendChild(we);
		}
	}

	private String getLocAge(String loc) {
		// is it computed already
		if (locAges.containsKey(loc)) // speeds up process so that ages already computed are not recomputed
			return locAges.get(loc);
		// get loc age
		ArrayList<TeiParticipant> part = getParticipants();
		for (TeiParticipant tp: part) {
			if (tp == null || tp.id == null || loc == null) continue;
			if (tp.id.equals(loc)) {
				String s = tp.adds.get("ageinfo");
				System.out.printf("Loc %s Age %s AgeInfo %s%n", loc, tp.age, s);
				try {
					// normalize age for TXM
					// see if it is CLAN format.
					int p = tp.age.indexOf(';');
					if (p > 0) {
						String y = tp.age.substring(0, p);
						int ageyear = Integer.parseInt(y);
						if (ageyear >= 10) {
							System.out.printf("Age found for %s is %s (simplified to %s)%n", loc, tp.age, y);
							locAges.put(loc, y);
							return y;
						}
						String t = tp.age.substring(p+1);
						p = t.indexOf('.');
						String m, d;
						if (p > 0) {
							m = t.substring(0, p);
							d = t.substring(p+1);
						} else {
							m = t;
							d = "0";
						}
						int numberofdays = Integer.parseInt(m) * 12 + Integer.parseInt(d);
						String sage = y + Integer.toString(numberofdays / 365);
						System.out.printf("Age found for %s is %s%n", loc, sage);
						locAges.put(loc, sage);
						return sage;
					}
					// case of the eslo numbers (two digits with a / in between)
					if (tp.age.equals("+ de 65")) {
						System.out.printf("Age found for %s is %s%n", loc, tp.age);
						String sage = "65";
						locAges.put(loc, sage);
						return sage;
					}
					Pattern pp = Pattern.compile("(\\d+)\\s?/\\s?(\\d+)");
					Matcher mm = pp.matcher(tp.age);
					double trueage;
					if (mm.matches()) {
						double d1 = Double.parseDouble(mm.group(1));
						double d2 = Double.parseDouble(mm.group(2));
						trueage = (d1 + d2)/2;
					} else {
						// case of floating point numbers
						trueage = Float.parseFloat(tp.age);
					}
					String sage;
					if (trueage >= 10.0) {
						int ageint = (int) Math.round(trueage);
						sage = Integer.toString(ageint);
					} else {
						sage = String.format("%0.1d", trueage);
					}
					System.out.printf("Age found for %s is %s (simplified to %s)%n", loc, tp.age,sage);
					locAges.put(loc, sage);
					return sage;
				} catch (Exception e) {
					System.err.printf("Warning: age is not a standard number for speaker %s: default age set at %s%n", loc, defaultAge);
					locAges.put(loc, defaultAge);
					return defaultAge;
				}
			}
		}
		// if here loc was not found !
		System.err.printf("Warning: speaker was not found: standard age provided: %s%n", defaultAge);
		locAges.put(loc, defaultAge);
		return defaultAge;
	}

	/**
	 * Ecriture des tiers: lignes qui commencent par le signe pourcent %
	 * 
	 * Le tier à écrire, au format : Nom du tier \t Contenu du tier
	 * Pas pour l'instant dans txm
	public void writeTier(Annot tier) {
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(tier.name))
				return;
			if (!optionsOutput.isDoDisplay(tier.name))
				return;
		}
		String type = tier.name;
		String tierContent = tier.content;
		String tierLine = "%" + type + ": " + tierContent.trim();
//		out.println(tierLine);
	}
	*/
	
	public static void main(String args[]) throws IOException {
		String usage = "Description: TeiToTxm converts a TEI file to a TXM file%nUsage: TeiToTxm [-options] <file."
				+ Utils.EXT + ">%n";
		TeiToTxm ttc = new TeiToTxm();
		ttc.mainCommand(args, Utils.EXT, EXT, usage, 2);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		transform(input, output, options);
		if (tf != null) {
//			System.out.println("Reading " + input);
			createOutput();
//			System.out.println("New file created " + output);
		}
	}
}
