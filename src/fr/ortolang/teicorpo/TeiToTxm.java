/**
 * Conversion d'un fichier TEI en un fichier TXM.
 * @author Christophe Parisse
 */
package fr.ortolang.teicorpo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.ortolang.teicorpo.AnnotatedUtterance;
import fr.ortolang.teicorpo.TeiFile.Div;

public class TeiToTxm extends TeiConverter {

	// Document Txm à écrire
	public Document txmDoc;
	// Extension du fichier de sortie
	final static String EXT = ".txm.xml";
	
	Element txm; // root of document
	Element head; // put all utterances inside text
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
		init(inputName, outputName, optionsTei);
		if (this.tf == null)
			return;
		typeDiv = "";
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
		createFile(outputName);
	}

	public void createFile(String outputFileName) {
		Source source = new DOMSource(this.txmDoc);
		Result resultat = new StreamResult(outputFileName);
		try {
			// Configuration du transformer 
			TransformerFactory fabrique2 = TransformerFactory.newInstance();
			Transformer transformer = fabrique2.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			// Transformation
			transformer.transform(source, resultat);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		head = txmDoc.createElement("text");
		txm.appendChild(head);
		for (Map.Entry<String, String> entry : optionsOutput.tv.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    Element elt = txmDoc.createElement("div");
		    elt.setAttribute(key, value.replaceAll("\\s+", "_"));
		    head.appendChild(elt);
		    head = elt;
			// out.printf("<%s=%s>%n", key, value.replaceAll("\\s+", "_"));
		}
	}

	/**
	 * Ecriture de la partie transcriptions: énoncés + tiers
	 */
	public void buildText() {
		ArrayList<TeiFile.Div> divs = tf.trans.divs;
		for (Div d : divs) {
			// System.out.println("DIV: " + d.type + " <" + d.theme + ">");
			/*
			if (d.type.toLowerCase().equals("bg") || d.type.toLowerCase().equals("g")) {
				typeDiv = d.theme;
			} else {
				typeDiv = "";
			}
			*/
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
				writeUtterance(u);
			}
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
		if (optionsOutput.syntax.equals("tt") || optionsOutput.syntax.equals("treetagger"))
			return;
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(loc))
				return;
			if (!optionsOutput.isDoDisplay(loc))
				return;
		}
		Element u = generateUStart(loc, startTime, endTime);
		generateU(u, speechContent, loc);
	}

	Element generateUStart(String loc, String startTime, String endTime) {
		// System.out.println(loc + ' ' + startTime + ' ' + endTime +' ' +
		// speechContent);
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

		Element u = txmDoc.createElement("u");
		// On ajoute les informations temporelles seulement si on a un temps de
		// début et un temps de fin
		if (Utils.isNotEmptyOrNull(endTime) && Utils.isNotEmptyOrNull(startTime)) {
			u.setAttribute("who", loc);
			u.setAttribute("start", Double.toString(Double.parseDouble(startTime)));
			u.setAttribute("end", Double.toString(Double.parseDouble(endTime)));
			// u.setTextContent(speechContent);
		} else {
			u.setAttribute("who", loc);
			u.setAttribute("start", "");
			u.setAttribute("end", "");
			// u.setTextContent(speechContent);
		}
		head.appendChild(u);
		return u;
	}

	@Override
	public void writeAddInfo(AnnotatedUtterance u) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeTier(AnnotatedUtterance au, Annot tier) {
		if (optionsOutput.syntax.equals("tt") || optionsOutput.syntax.equals("treetagger")) {
			// System.out.println("writeTier: " + tier.name);
			if (tier.name.equals("treetagger")) {
				Element u = generateUStart(au.speakerCode, au.start, au.end);
				// System.out.println("writeTier: " + tier.toString());
				// get loc age
				String age = "";
				ArrayList<TeiParticipant> part = getParticipants();
				for (TeiParticipant tp: part) {
					if (tp == null || tp.id == null || au.speakerCode == null) continue;
					if (tp.id.equals(au.speakerCode)) {
						age = tp.age;
					}
				}
				// tier.name
				if (tier.dependantAnnotations != null) {
					for (int i=0; i < tier.dependantAnnotations.size(); i++) {
						Annot aw = tier.dependantAnnotations.get(i);
						Element we = txmDoc.createElement("w");
						if (!au.speakerCode.isEmpty()) {
							we.setAttribute("loc", au.speakerCode);
							we.setAttribute("age", age);
						}
						for (Map.Entry<String, String> entry : optionsOutput.tv.entrySet()) {
						    String key = entry.getKey();
						    String value = entry.getValue();
							we.setAttribute(key, value);
						}
						for (int k=0; k < aw.dependantAnnotations.size(); k++) {
							Annot kw = aw.dependantAnnotations.get(k);
							if (kw.name.equals("word")) {
								we.setTextContent(kw.getContent().trim());
							} else if (kw.name.equals("pos")) {
								we.setAttribute("pos", kw.getContent().trim());
							} else if (kw.name.equals("lemma")) {
								we.setAttribute("lemma", kw.getContent().trim());
							}
						}
						u.appendChild(we);
					}
				}
			}
		}
	}

	void generateU(Element u, String speechContent, String loc) {
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
		String age = "";
		ArrayList<TeiParticipant> part = getParticipants();
		for (TeiParticipant tp: part) {
			if (tp == null || tp.id == null || loc == null) continue;
			if (tp.id.equals(loc)) {
				age = tp.age;
			}
		}
		// for (String w: s) {
		for (int ti = 0; ti < p.size(); ti++) {
			Element we = txmDoc.createElement("w");
			// we.setTextContent(w);
			we.setTextContent(p.get(ti));
			if (!loc.isEmpty()) {
				we.setAttribute("loc", loc);
				we.setAttribute("age", age);
			}
			for (Map.Entry<String, String> entry : optionsOutput.tv.entrySet()) {
			    String key = entry.getKey();
			    String value = entry.getValue();
				we.setAttribute(key, value);
			}
			if (!typeDiv.isEmpty())
				we.setAttribute("div", typeDiv);
			u.appendChild(we);
		}
	}

	/**
	 * Ecriture des tiers: lignes qui commencent par le signe pourcent %
	 * 
	 * @param tier
	 *            Le tier à écrire, au format : Nom du tier \t Contenu du tier
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
		String usage = "Description: TeiToTxm convertit un fichier au format TEI en un fichier au format Txm%nUsage: TeiToTxm [-options] <file."
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
