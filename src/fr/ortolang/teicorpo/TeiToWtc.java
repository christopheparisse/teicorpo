/**
 * Conversion d'un fichier TEI en un fichier TXM.
 * @author Christophe Parisse
 */
package fr.ortolang.teicorpo;

import fr.ortolang.teicorpo.TeiFile.Div;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

//import java.io.FilenameFilter;

public class TeiToWtc extends TeiConverter {

	// Document Txm à écrire
	public Document txmDoc;
	// Extension du fichier de sortie
	final static String EXT = ".wtc";

	int spid = 1;
	String typeDiv;

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
		if (optionsTei.baseName.isEmpty() || optionsTei.mediaName == null) {
			System.err.println("Options -m (medianame) and -b (basename) must be provived (cannot be empty). Uncertain output.");
		}
		init(inputName, outputName, optionsTei);
		if (this.tf == null)
			return;
		typeDiv = "";
		conversion();
	}

	@Override
	public void outputWriter() {

	}

	/**
	 * Conversion
	 */
	public void conversion() {
		// Etapes de conversion
		buildHeader();
		buildText();
		System.out.printf("</text>%n");
		System.out.printf("</txmcorpus>%n");
	}

	@Override
	public void createOutput() {

	}

	/**
	 * Ecriture de l'en-tête du fichier Srt en fonction du fichier TEI à
	 * convertir
	 */
	public void buildHeader() {
		if (optionsOutput.mediaName == null) optionsOutput.mediaName = Utils.basename(inputName);

		System.out.printf("<txmcorpus lang=\"%s\">%n", "fr"); // , optionsOutput.language); // à mettre plus tard
		System.out.printf("<text project=\"%s\" base=\"%s\" id=\"%s\" audiofilename=\"%s\" version=\"%s\">%n", "default", optionsOutput.baseName, Utils.basename(inputName), Utils.basename(optionsOutput.mediaName), "0.0.0");
		/*
		for (Map.Entry<String, String> entry : optionsOutput.tv.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    Element elt = txmDoc.createElement("div");
		    elt.setAttribute(key, value.replaceAll("\\s+", "_"));
		    head.appendChild(elt);
		    head = elt;
			// out.printf("<%s=%s>%n", key, value.replaceAll("\\s+", "_"));
		}
		*/
	}

	/**
	 * Ecriture de la partie transcriptions: énoncés + tiers
	 */
	public void buildText() {
		ArrayList<Div> divs = tf.trans.divs;
		int id = 1;
		for (Div d : divs) {
			// System.out.println("DIV: " + d.type + " <" + d.theme + ">");
			/*
			if (d.type.toLowerCase().equals("bg") || d.type.toLowerCase().equals("g")) {
				typeDiv = d.theme;
			} else {
				typeDiv = "";
			}
			*/
			System.out.printf("<div id=\"%d\" type=\"%s\" starttime=\"%s\" endtime=\"%s\">%n", id, "spoken", d.start, d.end);
			id++;
			for (AnnotatedUtterance u : d.utterances) {
				if (u.type != null) {
					String[] splitType = u.type.split("\t");
					if (splitType != null && splitType.length >= 2) {
						if (splitType[0].toLowerCase().equals("bg") || splitType[0].toLowerCase().equals("g")) {
							String theme = Utils.cleanString(tf.transInfo.situations.get(splitType[1]));
							typeDiv = theme;
						}
					}
				}
				writeUtterance(u); // cette fonction prépare et balance writeSpeech(u.speakerCode, speech, start, end); ansi que writeTier(u, tier) s'il y a des tiers
			}
			System.out.printf("</div>%n");
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
	 * @param loc
	 *            Locuteur
	 * @param speechContent
	 *            Contenu de l'énoncé
	 * @param startTime
	 *            Temps de début de l'énoncé
	 * @param endTime
	 *            Temps de fin de l'énoncé
	 */
	public void writeSpeech(String loc, String speechContent, String startTime, String endTime) {
		// System.err.println("writeSpeech: " + optionsOutput.syntaxformat);
/*		if (optionsOutput.syntaxformat.equals("conll")) { // || optionsOutput.syntax.equals("treetagger")) {
			System.out.println("skip writeSpeech");
			return;
		}
*/
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(loc))
				return;
			if (!optionsOutput.isDoDisplay(loc))
				return;
		}
		// System.err.println("writeSpeech: " + optionsOutput.syntaxformat);
		// L'action ci-dessous n'est exécutée que si les instructions d'écriture de la syntaxe n'ont pas été données
		if (!optionsOutput.syntaxformat.equals("ref") && !optionsOutput.syntaxformat.equals("conll")) {
			// System.err.println("writeSpeech0: " + optionsOutput.syntaxformat);
			generateUStart(loc, startTime, endTime, null);
			generateU(speechContent, loc);
			System.out.printf("</u>%n");
			System.out.printf("</sp>%n");
		}
		// if this is the ref case or the conll, the result will be written by the tier in writeTier function
	}

	void generateUStart(String loc, String startTime, String endTime, String age) {
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

		String locclean = loc.replaceAll("[ _]", "-");
		// On ajoute les informations temporelles seulement si on a un temps de
		// début et un temps de fin
		String st, et;
		if (Utils.isNotEmptyOrNull(endTime) && Utils.isNotEmptyOrNull(startTime)) {
			st = Double.toString(Double.parseDouble(startTime));
			et = Double.toString(Double.parseDouble(endTime));
		} else {
			st = "";
			et = "";
		}
		// <sp id="4" overlap="false" time="0:00:04" speaker="spk2" starttime="4.698" endtime="6.372">
		// <u s="4.698" time="0:00:04" spkid="spk2" spk="André Morange" check="no" dialect="native" accent="" scope="local">
		System.out.printf("<sp id=\"%d\" speaker=\"%s\" starttime=\"%s\" endtime=\"%s\">%n", spid, locclean, st, et);
		spid++;
		System.out.printf("<u s=\"%s\" spkid=\"%s\" age=\"%s\">%n", st, locclean, getAge(locclean));
		for (Map.Entry<String, ValSpk> entry : optionsOutput.tv.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue().genericvalue.replaceAll("[ _]", "-");
			System.out.printf(" %s=\"%s\"", key, value);
		}
	}

	@Override
	public void writeAddInfo(AnnotatedUtterance u) {
		// TODO Auto-generated method stub
		
	}

	String getAge(String participant) {
		String age = "";
		ArrayList<TeiParticipant> part = getParticipants();
		for (TeiParticipant tp: part) {
			if (tp == null || tp.id == null || participant == null) continue;
			if (tp.id.equals(participant)) {
				// normalize age for TXM
				try {
					float fage = Float.parseFloat(tp.age);
					String sage = String.format("%04.1f",fage);
					age = sage;
				} catch (Exception e) {
					System.err.printf("Warning: age is not a standard number: %s%n", tp.age);
					age = tp.age;
				}
			}
		}
		return age;
	}

	@Override
	public void writeTier(AnnotatedUtterance au, Annot tier) {
		// System.err.println("writeTier1: " + tier.toString() + " syntf " + optionsOutput.syntaxformat + " name: " + tier.name);
		if (optionsOutput.syntaxformat.equals("conll") && tier.name.equals("conll")) {
			// System.out.println("writeTier2: " + tier.toString());
			// get loc age
			String age = getAge(au.speakerCode);
			generateUStart(au.speakerCode, au.start, au.end, age);
			String spkcode = au.speakerCode.replaceAll("[ _]", "-");
			// tier.name
			if (tier.dependantAnnotations != null) {
				// System.out.println("dep: " + tier.dependantAnnotations.toString());
				if (optionsOutput.tiernames) {
					String w = "["+spkcode+"]";
					w += "\t" + spkcode;
					w += "\t" + age;
					w += "\t" + typeDiv;
					for (Map.Entry<String, ValSpk> entry : optionsOutput.tv.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue().genericvalue.replaceAll("[ _]", "-");
						w += "\t" + value;
					}
					// tags ???
					w += "\tSENT\t_";
					System.out.println(w);
				}
				for (int i=0; i < tier.dependantAnnotations.size(); i++) {
					Annot aw = tier.dependantAnnotations.get(i);
					// System.out.println("writeConnl3 " + aw.toString());
					// get data that in reproduced in every words
					String locinfo = "\t" + spkcode + "\t" + age  + "\t" + typeDiv;
					for (Map.Entry<String, ValSpk> entry : optionsOutput.tv.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue().genericvalue.replaceAll("[ _]", "-");
						locinfo += "\t" + value;
					}
					// the words
					String word = "";
					String lemma = "";
					String pos = "";
					for (int k=0; k < aw.dependantAnnotations.size(); k++) {
						Annot kw = aw.dependantAnnotations.get(k);
						if (kw.name.equals("word")) {
							word = kw.getContent().trim().replaceAll("[ _]", "-");
							/*
							if (optionsOutput.sandhi) {
								setSandhiInfo(m, we);
							}
							*/
						} else if (kw.name.equals("pos")) {
							pos = kw.getContent().trim().replaceAll("[ _]", "-");
						} else if (kw.name.equals("lemma")) {
							lemma = kw.getContent().trim().replaceAll("[ _]", "-");
						}
					}
					System.out.printf("%s%s\t%s\t%s%n", word, locinfo, pos, lemma);
				}
			}
		} else if (optionsOutput.syntaxformat.equals("ref") && tier.name.equals("ref")) {
			// System.err.println("writeTier3: " + tier.toString());
			// get loc age
			String age = getAge(au.speakerCode);
			generateUStart(au.speakerCode, au.start, au.end, age);
			String spkcode = au.speakerCode.replaceAll("[ _]", "-");
			// tier.name
			
			if (tier.pptRef != null) {
				//System.out.println(tier.pptRef);
				if (optionsOutput.tiernames) {
					String w = "["+spkcode+"]";
					w += "\t" + spkcode;
					w += "\t" + age;
					w += "\t" + typeDiv;
					for (Map.Entry<String, ValSpk> entry : optionsOutput.tv.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue().genericvalue.replaceAll("[ _]", "-");
						w += "\t" + value;
					}
					// tags ???
					w += "\tSENT\t_";
					System.out.println(w);
				}
				for (int x = 0; x < tier.pptRef.getLength(); x++) {
					Node w = tier.pptRef.item(x);
					if (w.getNodeType() == Node.ELEMENT_NODE) {
						// get data that in reproduced in every words
						String locinfo = "\t" + spkcode + "\t" + age  + "\t" + typeDiv;
						for (Map.Entry<String, ValSpk> entry : optionsOutput.tv.entrySet()) {
							String key = entry.getKey();
							String value = entry.getValue().genericvalue.replaceAll("[ _]", "-");
							locinfo += "\t" + value;
						}

						Element wo = (Element)w;
						String pos = wo.getAttribute("pos").trim().replaceAll("[ _]", "-");
						String lemma = wo.getAttribute("lemma").trim().replaceAll("[ _]", "-");
						String word = wo.getTextContent().trim().replaceAll("[ _]", "-");
						/*
						if (optionsOutput.sandhi) {
							setSandhiInfo(m, we);
						}
						*/
						System.out.printf("%s%s\t%s\t%s%n", word, locinfo, pos, lemma);
					}
				}
			}
			//System.out.println(u);
		}
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

	void generateU(String speechContent, String loc) {
		/*
		 * Tokenize the line.
		 */
		// String[] s = speechContent.split("\\s+");
		ArrayList<String> p = Tokenizer.splitTextTT(speechContent);
		// for (int ti = 0; ti < p.size(); ti++) out.printf("%s%n", p.get(ti));
		/*
		 * write word information
		 */
		// get loc age
		String age = getAge(loc);
		// ça	Sonia Branca-Rosoff (ENQ)	Andre_Morange_H_58_Mo-v2, Sonia Branca-Rosoff (ENQ), 0:05:43	w_andre_morange_h_58_mov2_1247	N/A	0		ça	PRO:cls	present	ça
		// là	André Morange	Andre_Morange_H_58_Mo-v2, André Morange, 0:12:07	w_andre_morange_h_58_mov2_2560	N/A	0		là	ADV	present	là
		if (optionsOutput.tiernames) {
			String w = "["+loc+"]";
			w += "\t" + loc;
			w += "\t" + age;
			w += "\t" + typeDiv;
			for (Map.Entry<String, ValSpk> entry : optionsOutput.tv.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().genericvalue;
				w += "\t" + value;
			}
			System.out.println(w);
		}
		// for (String w: s) {
		for (int ti = 0; ti < p.size(); ti++) {
			String w = p.get(ti);
			w += "\t" + loc + "\t" + age  + "\t" + typeDiv;
			for (Map.Entry<String, ValSpk> entry : optionsOutput.tv.entrySet()) {
			    String key = entry.getKey();
			    String value = entry.getValue().genericvalue;
				w += "\t" + value;
			}
			System.out.println(w);
		}
	}

	/**
	 * Ecriture des tiers: lignes qui commencent par le signe pourcent %
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
		String usage = "Description: TeiToWtc converts a TEI file to a WTC file%nUsage: TeiToWtc [-options] <file."
				+ Utils.EXT + ">%n";
		TeiToWtc ttc = new TeiToWtc();
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
