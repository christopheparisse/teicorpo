/**
 * Conversion d'un fichier TEI en un fichier SRT.
 * @author Myriam Majdoub
 */
package fr.ortolang.teicorpo;

import java.io.*;
//import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;

import fr.ortolang.teicorpo.TeiFile.Div;

public class TeiToText extends TeiConverter {

	// Permet d'écrire le fichier de sortie
	private PrintWriter out;
	// Encodage du fichier de sortie
	final static String outputEncoding = "UTF-8";
	// Extension du fichier de sortie
	final static String EXT = ".txt";

	// for the tabular output or the metadata in the filename
	String partage = "X";
	String partEducation = "X";

	/**
	 * Convertit le fichier TEI donné en argument en un fichier texte.
	 * 
	 * @param inputName
	 *            Nom du fichier d'entrée (fichier TEI, a donc l'extenstion
	 *            .teiml)
	 * @param outputName
	 *            Nom du fichier de sortie (fichier TXT, a donc l'extenson .txt)
	 */
	public void transform(String inputName, String outputName, TierParams optionsTei) {
		init(inputName, outputName, optionsTei);
		if (this.tf == null)
			return;
		if (outputWriter() == false) {
			System.out.printf("Participant not found in %s%nFile ignored.%n", inputName);
			return;
		}
		conversion();
		closeWriter();
	}

	private void closeWriter() {
		out.close();
	}

	/**
	 * Ecriture de l'output
	 */
	public boolean outputWriter() {
		String newOutputName = outputName;
		if (tf.optionsOutput.partmetadataInFilename == true) {
			String part = Utils.getOnlyElement(tf.optionsOutput.doDisplay);
			if (part == null) {
				System.err.printf("For option partmeta, the option -a must be set exactly once.%n");
				System.exit(1);
			}
			String pathn = Utils.pathname(outputName);
			String partname = tf.transInfo.getParticipantName(part);

			// metadata values to be added
			for (Map.Entry<String, SpkVal> entry : optionsOutput.mv.entrySet()) {
				String key = entry.getKey();
				SpkVal vs = entry.getValue();
				System.out.printf("setTv:mv: %s %s %s%n", key, vs.genericspk, vs.genericvalue);
				if (key.equals("corpus")) {
					if (!vs.genericspk.isEmpty()) {
						System.err.printf("The value of key %s for speaker %s is ignored%n", key, vs.genericspk);
					}
					partname = vs.genericvalue + "@" + partname;
				} else {
					System.err.printf("The value of key %s : %s is ignored%n", key, vs.genericvalue);
				}
			}

			partage = tf.transInfo.getParticipantAge(part);
			// String partrole = tf.transInfo.getParticipantRole(part);
			partEducation = tf.transInfo.getParticipantEducation(part);
			// System.out.printf("PART: (%s) [%s] {%s}%n", part, partname, partage);
			// tf.transInfo.print();
			if (partname.isEmpty()) {
				System.out.printf("Warning: no participant name in file %s%n", outputName);
				System.out.printf("Output name wont be changed. It would be better to edit the original file%n");
			} else {
				if (partage.isEmpty()) {
					partage = "XX.XX";
				} else {
					float age = Float.parseFloat(partage);
					float age100 = age * 100;
					Integer ageround100 = Math.round(age100);
					float ageround = ageround100.floatValue() / 100;
					// System.out.printf("%s %f %f %d %f%n", partage, age, age100, ageround100, ageround);
					partage = Float.toString(ageround);
				}
				if (tf.optionsOutput.csv != true) {
					newOutputName = pathn + "/" + part + "_" + partname + "_" + partage + "{" + partEducation + "}" + ".txt";
					// test if file exists and if yes create a new name
					int addnum = 1;
					while (true) {
						File f = new File(newOutputName);
						if (f.exists()) {
							newOutputName = pathn + "/" + part + "_" + partname + "_" + partage + "{" + partEducation + "}" + "-(" + addnum + ").txt";
							addnum++;
						} else {
							break;
						}
					}
					System.out.printf("export\t%s\t%s%n", outputName, newOutputName);
				}
				// else just keep partage for the tabular output
			}
		}
		out = Utils.openOutputStream(newOutputName, tf.optionsOutput.concat, outputEncoding);
		return true;
	}

	/**
	 * Conversion
	 */
	public void conversion() {
		// System.out.println("Conversion (" + (Params.forceEmpty?"true":"false") + ") (" + Params.partDisplay + ") (" + Params.tierDisplay + ")");
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
			for (Map.Entry<String, SpkVal> entry : optionsOutput.tv.entrySet()) {
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
				if (Utils.isNotEmptyOrNull(u.type)) { // head of a div
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
	 * @param au
	 *            Locuteur
	 * @param speechContent
	 *            Contenu de l'énoncé
	 * @param startTime
	 *            Temps de début de l'énoncé
	 * @param endTime
	 *            Temps de fin de l'énoncé
	 */
	public void writeSpeech(AnnotatedUtterance au, String speechContent, String startTime, String endTime) {
		/* already done in AnnotatedUtterance
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(spkChoice(au), 1))
				return;
			if (!optionsOutput.isDoDisplay(spkChoice(au), 1))
				return;
		}
		*/

		// test if length of line is limited to a certain length
		if (tf.optionsOutput.minlength > 0 || tf.optionsOutput.maxlength > 0) {
			// split speechContent
			String[] wds = speechContent.split("[ \t\r\n\\.\\!\\?]");
			if (wds.length < tf.optionsOutput.minlength) return;
			if (tf.optionsOutput.maxlength != 0 && wds.length > tf.optionsOutput.maxlength) return;
		}

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
			out.printf("-*%s%n", spkChoice(au));
		} else if (tf.optionsOutput.locutor == true) {
			out.printf("-%s: ", spkChoice(au));
		}
		String speechContentTarget;
		if (optionsOutput.target.equals("stanza")) {
			speechContent = speechContent.replace("  ", " ");
			if (speechContent.endsWith(" .")) {
				speechContentTarget = speechContent.substring(0, speechContent.length()-2) + ".";
			} else if (speechContent.endsWith(" ?")) {
				speechContentTarget = speechContent.substring(0, speechContent.length()-2) + "?";
			} else if (speechContent.endsWith(" !")) {
				speechContentTarget = speechContent.substring(0, speechContent.length()-2) + "!";
			} else if (speechContent.endsWith(" /.")) {
				speechContentTarget = speechContent.substring(0, speechContent.length()-3) + ".";
			} else if (speechContent.endsWith(" ...")) {
				speechContentTarget = speechContent.substring(0, speechContent.length()-4) + ".";
			} else if (speechContent.indexOf(".") < 0) {
				speechContentTarget = speechContent + ".";
			} else {
				speechContentTarget = speechContent;
			}
		} else {
			speechContentTarget = speechContent;
		}
		//System.out.printf("(%s) [%s]%n", speechContent, speechContentTarget);
		// On ajoute les informations temporelles seulement si on a un temps de
		// début et un temps de fin
		if (tf.optionsOutput.csv == true) {
			out.print(spkChoice(au) + "\t" + partage + "\t" + speechContentTarget +"\n");
		} else if (tf.optionsOutput.raw == true) {
			if (tf.optionsOutput.tiernames && tf.optionsOutput.partmetadataInFilename != true) {
				out.print("[" + spkChoice(au) + "]");
				if (tf.optionsOutput.tierxmlid) {
					out.println(" <" + au.lastxmlid + "> " + speechContentTarget);
				} else {
					out.println(" " + speechContentTarget);
				}
			} else {
				out.println(speechContentTarget);
			}
		} else {
			if (Utils.isNotEmptyOrNull(endTime) && Utils.isNotEmptyOrNull(startTime)) {
				float start = Float.parseFloat(startTime);
				float end = Float.parseFloat(endTime);
				out.printf("%f:%f\t", start, end);
				out.println(spkChoice(au) + "\t" + speechContentTarget);
			} else {
				out.println("\t\t" + spkChoice(au) + "\t" + speechContentTarget);
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
		String usage = "Description: TeiToText converts a TEI file to a Text file (txt)%nUsage: TeiToText [-options] <file."
				+ Utils.EXT + ">%n";
		TeiToText ttc = new TeiToText();
		ttc.mainCommand(args, Utils.EXT, EXT, usage, 2);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		if (options.partmetadataInFilename == true && options.raw != true) {
			options.raw = true;
			System.err.printf("Raw is set automatically to true when using option partmeta%n");
		}
//		if (options.partmetadataInFilename == true) {
//			System.out.printf("Option partmeta is set. Filter export lines to save the file conversions.%n");
//		}
		transform(input, output, options);
		if (tf != null) {
			if (options.verbose) System.out.println("Txt: Reading " + input);
			createOutput();
			if (options.verbose) System.out.println("Txt: New file created " + output);
		}
	}

}
