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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ortolang.teicorpo.AnnotatedUtterance;
import fr.ortolang.teicorpo.TeiFile.Div;

public class TeiToSrt extends TeiConverter {

	// Permet d'écrire le fichier de sortie
	private PrintWriter out;
	// Encodage du fichier de sortie
	final static String outputEncoding = "UTF-8";
	// Extension du fichier de sortie
	final static String EXT = ".srt";
	TierParams optionsOutput;

	int srtNumber = 1; // compte le nombre de sous-titres produits dans le
						// fichier srt pour écrire les entêtes.

	/**
	 * Convertit le fichier TEI donné en argument en un fichier Srt.
	 * 
	 * @param inputName
	 *            Nom du fichier d'entrée (fichier TEI, a donc l'extenstion
	 *            .teiml)
	 * @param outputName
	 *            Nom du fichier de sortie (fichier SRT, a donc l'extenson .srt)
	 */
	public void transform(String inputName, String outputName, TierParams optionsTei) {
		init(inputName, outputName, optionsTei);
		if (this.tf == null)
			return;
		optionsOutput = optionsTei;
		outputWriter();
		conversion();
	}

	/**
	 * Ecriture de l'output
	 */
	public void outputWriter() {
		out = null;
		try {
			FileOutputStream of = new FileOutputStream(outputName);
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
		srtNumber = 1;
		// Etapes de conversion
		buildHeader();
		buildText();
	}

	/**
	 * Ecriture de l'en-tête du fichier Srt en fonction du fichier TEI à
	 * convertir
	 */
	public void buildHeader() {
		if (srtNumber > 1)
			out.println("");
		out.printf("%d\n", srtNumber);
		srtNumber++;
		out.printf("%02d:%02d:%02d,%03d --> %02d:%02d:%02d,%03d\n", 0, 0, 0, 0, TimeDivision.toHours(1),
				TimeDivision.toMinutes(1), TimeDivision.toSeconds(1), TimeDivision.toMilliSeconds(1));
		out.println(inputName);
	}

	/**
	 * Ecriture de la partie transcriptions: énoncés + tiers
	 */
	public void buildText() {
		ArrayList<TeiFile.Div> divs = tf.trans.divs;
		for (Div d : divs) {
			for (AnnotatedUtterance u : d.utterances) {
				if (Utils.isNotEmptyOrNull(u.type)) { // head of a div
					if (!u.start.isEmpty()) {
						float start = Float.parseFloat(u.start);
						if (srtNumber > 1)
							out.println("");
						out.printf("%d\n", srtNumber);
						srtNumber++;
						out.printf("%02d:%02d:%02d,%03d --> %02d:%02d:%02d,%03d\n", TimeDivision.toHours(start),
								TimeDivision.toMinutes(start), TimeDivision.toSeconds(start),
								TimeDivision.toMilliSeconds(start), TimeDivision.toHours(start + 1),
								TimeDivision.toMinutes(start + 1), TimeDivision.toSeconds(start + 1),
								TimeDivision.toMilliSeconds(start + 1));
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
		out.println(type + "\t" + theme);
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
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(loc))
				return;
			if (!optionsOutput.isDoDisplay(loc))
				return;
		}
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

		// On ajoute les informations temporelles seulement si on a un temps de
		// début et un temps de fin
		if (Utils.isNotEmptyOrNull(endTime) && Utils.isNotEmptyOrNull(startTime)) {
			float start = Float.parseFloat(startTime);
			float end = Float.parseFloat(endTime);
			if (srtNumber > 1)
				out.println("");
			out.printf("%d\n", srtNumber);
			srtNumber++;
			out.printf("%02d:%02d:%02d,%03d --> %02d:%02d:%02d,%03d\n", TimeDivision.toHours(start),
					TimeDivision.toMinutes(start), TimeDivision.toSeconds(start), TimeDivision.toMilliSeconds(start),
					TimeDivision.toHours(end), TimeDivision.toMinutes(end), TimeDivision.toSeconds(end),
					TimeDivision.toMilliSeconds(end));
			out.println(loc + ": " + speechContent);
		} else if (optionsOutput.forceEmpty) {
			out.println(loc + ": " + speechContent);
		}
	}

	/**
	 * Ajout des info additionnelles (hors-tiers)
	 * 
	 * @param u
	 */
	public void writeAddInfo(AnnotatedUtterance u) {
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay("com"))
				return;
			if (!optionsOutput.isDoDisplay("com"))
				return;
		}
		if (optionsOutput.noHeader == true) return;
		// Ajout des informations additionnelles présents dans les fichiers srt
		for (String s : u.coms) {
			String infoType = Utils.getInfoType(s);
			String infoContent = Utils.getInfo(s);
			out.println(infoType + ' ' + infoContent);
		}
	}

	/**
	 * Ecriture des tiers: lignes qui commencent par le signe pourcent %
	 * 
	 * @param tier
	 *            Le tier à écrire, au format : Nom du tier \t Contenu du tier
	 */
	public void writeTier(AnnotatedUtterance u, Annot tier) {
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(tier.name))
				return;
			if (!optionsOutput.isDoDisplay(tier.name))
				return;
		}
		if (optionsOutput.level <= 1) return;
		String type = tier.name;
		String tierContent = tier.getContent();
		String tierLine = "%" + type + ": " + tierContent.trim();
		out.println(tierLine);
	}

	public void createOutput() {
	}

	public static void main(String args[]) throws IOException {
		String usage = "Description: TeiToSrt convertit un fichier au format TEI en un fichier au format Srt%nUsage: TeiToSrt [-options] <file."
				+ Utils.EXT + ">%n";
		TeiToSrt ttc = new TeiToSrt();
		ttc.mainCommand(args, Utils.EXT, EXT, usage, 0);
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
