/**
 * Conversion d'un fichier TEI en un fichier SRT.
 * @author Myriam Majdoub
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

import fr.ortolang.teicorpo.AnnotatedUtterance;
import fr.ortolang.teicorpo.TeiFile.Div;

public class TeiToText extends TeiConverter {

	// Permet d'écrire le fichier de sortie
	private PrintWriter out;
	// Encodage du fichier de sortie
	final static String outputEncoding = "UTF-8";
	// Extension du fichier de sortie
	final static String EXT = ".txt";

	/**
	 * Convertit le fichier TEI donné en argument en un fichier Srt.
	 * 
	 * @param inputName
	 *            Nom du fichier d'entrée (fichier TEI, a donc l'extenstion
	 *            .teiml)
	 * @param outputName
	 *            Nom du fichier de sortie (fichier SRT, a donc l'extenson .txt)
	 */
	public void transform(String inputName, String outputName, TierParams optionsTei) {
		init(inputName, outputName, optionsTei);
		if (this.tf == null)
			return;
		outputWriter();
		conversion();
	}

	/**
	 * Ecriture de l'output
	 */
	public void outputWriter() {
		out = null;
		try {
			FileOutputStream of = new FileOutputStream(outputName, tf.optionsOutput.concat);
			OutputStreamWriter outWriter = new OutputStreamWriter(of, outputEncoding);
			out = new PrintWriter(outWriter, true);
		} catch (Exception e) {
			out = new PrintWriter(System.out, true);
		}
	}

	/**
	 * Conversion
	 */
	public void conversion() {
		// System.out.println("Conversion (" +
		// (Params.forceEmpty?"true":"false") + ") (" + Params.partDisplay + ")
		// (" + Params.tierDisplay + ")");
		// Etapes de conversion
		buildHeader();
		buildText();
	}

	/**
	 * Ecriture de l'en-tête du fichier Srt en fonction du fichier TEI à
	 * convertir
	 */
	public void buildHeader() {
		if (tf.optionsOutput.raw != true && tf.optionsOutput.noHeader != true) {
			out.println("@Fichier_input:\t" + inputName);
			out.println("@Fichier_output:\t" + outputName);
			out.print(tf.transInfo.toString().replaceAll("\t", " "));
		}
		if (tf.optionsOutput.iramuteq == true) {
			out.printf("**** ");
			for (Map.Entry<String, String> entry : optionsOutput.tv.entrySet()) {
			    String key = entry.getKey();
				out.printf("*%s ", key.replaceAll("[^0-9A-Za-z_]+", "_"));
			}
			out.printf("%n");
		}
	}

	/**
	 * Ecriture de la partie transcriptions: énoncés + tiers
	 */
	public void buildText() {
		ArrayList<TeiFile.Div> divs = tf.trans.divs;
		for (Div d : divs) {
			for (AnnotatedUtterance u : d.utterances) {
//				System.err.println(u.toString());
				if (Utils.isNotEmptyOrNull(u.type)) { // gead of a div
					if (u.start != null && !u.start.isEmpty() && tf.optionsOutput.raw != true && tf.optionsOutput.level != 1) {
						float start = Float.parseFloat(u.start);
						out.printf("%f:%f\t", start, start+1);
						String[] splitType = u.type.split("\t");
						if (splitType.length > 1)
							writeDiv(splitType[0], splitType[1]);
						else if (splitType.length == 1)
							out.println(splitType[0]);
						else
							out.println("-");
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
	 */
	public void writeDiv(String type, String themeId) {
		String theme = Utils.cleanString(tf.transInfo.situations.get(themeId));
		out.println(type + "\t" + theme.replaceAll("\\s+", " "));
	}

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
		/* already done in AnnotatedUtterance
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(loc, 1))
				return;
			if (!optionsOutput.isDoDisplay(loc, 1))
				return;
		}
		*/
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

		if (tf.optionsOutput.iramuteq == true) {
			out.printf("-*%s%n", loc);
		} else if (tf.optionsOutput.locutor == true) {
			out.printf("-%s: ", loc);
		}
		// On ajoute les informations temporelles seulement si on a un temps de
		// début et un temps de fin
		if (tf.optionsOutput.raw == true)
			out.println(speechContent);
		else {
			if (Utils.isNotEmptyOrNull(endTime) && Utils.isNotEmptyOrNull(startTime)) {
				float start = Float.parseFloat(startTime);
				float end = Float.parseFloat(endTime);
				out.printf("%f:%f\t", start, end);
				out.println(loc + "\t" + speechContent);
			} else {
				out.println("\t\t" + loc + "\t" + speechContent);
			}
		}
	}

	/**
	 * Ajout des info additionnelles (hors-tiers)
	 * 
	 * @param u
	 */
	public void writeAddInfo(AnnotatedUtterance u) {
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay("com", 1))
				return;
			if (!optionsOutput.isDoDisplay("com", 1))
				return;
		}
		if (optionsOutput.raw == true || optionsOutput.noHeader == true) return;
		// Ajout des informations additionnelles présents dans les fichiers txt
		for (String s : u.coms) {
			String infoType = Utils.getInfoType(s);
			String infoContent = Utils.getInfo(s);
			out.println("\t\t" + infoType + "\t" + infoContent);
		}
	}

	/**
	 * Ecriture des tiers: lignes qui commencent par le signe pourcent %
	 * 
	 * @param tier
	 *            Le tier à écrire, au format : Nom du tier \t Contenu du tier
	 */
	@Override
	public void writeTier(AnnotatedUtterance u, Annot tier) {
		/* already done in AnnotatedUtterance
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(tier.name, 2))
				return;
			if (!optionsOutput.isDoDisplay(tier.name, 2))
				return;
		}
		*/
		//System.out.printf("level=%d%n", optionsOutput.level);
		if (optionsOutput.level == 1) return;
		if (tf.optionsOutput.raw == true)
			out.println(tier.getContent().trim());
		else {
			String type = tier.name;
			String tierContent = tier.getContent();
			String tierLine = "\t\t\t%" + type + "\t" + tierContent.trim();
			out.println(tierLine);
		}
	}

	@Override
	public void createOutput() {
	}

	public static void main(String args[]) throws IOException {
		String usage = "Description: TeiToText convertit un fichier au format TEI en un fichier au format Text (txt)%nUsage: TeiToText [-options] <file."
				+ Utils.EXT + ">%n";
		TeiToText ttc = new TeiToText();
		ttc.mainCommand(args, Utils.EXT, EXT, usage, 6);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		transform(input, output, options);
		if (tf != null) {
			if (options.verbose) System.out.println("Txt: Reading " + input);
			createOutput();
			if (options.verbose) System.out.println("Txt: New file created " + output);
		}
	}

}
