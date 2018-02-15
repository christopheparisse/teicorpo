/**
 * 
 */
package fr.ortolang.teicorpo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author christopheparisse
 *
 */
public abstract class GenericMain {

	/*
	 * remplacement d'un fichier par lui-même
	 * option -c replace --> ne pas utiliser TT_EXT et mettre sameFile à vrai.
	 */

	public abstract void mainProcess(String input, String output, TierParams options) throws FileNotFoundException;
	
	public void mainCommand(String[] args, String extensionIn, String extensionOut, String usageString, int style) throws IOException {
		TierParams options = new TierParams();
		// Parcours des arguments
		if (!TierParams.processArgs(args, options, usageString, extensionIn, extensionOut, style)) {
			if (!options.noerror) return;
		}

		if (options.input.size() < 1) {
			System.err.println("Il faut au moins un fichier entrée");
			return; // even with --noerror cannot do anything
		}

/*		if (options.input.size() > 1 && options.output != null) {
			System.err.println("Si plusieurs fichiers entrée, alors un répertoire en sortie ou un fichier pour concat");
		}
*/
		if (options.concat == true && options.output == null) {
			System.err.println("Si option concaténer alors un fichier unique en sortie");
			return; // even with --noerror cannot do anything
		}
		
		if (options.concat == true && options.erase == true && options.eraseDone == false) {
			File f = new File(options.output);
			if (f.exists()) {
				if (f.isDirectory()) {
					System.out.println("\n Erreur :" + options.output
							+ " est un répertoire. Avec l'option -concat, ce doit être un fichier.\n");
					return; // even with --noerror cannot do anything
				}
				f.delete();
			}
			options.eraseDone = true;
		}
		processFiles(options);
	}

	public void processFiles(TierParams options) throws IOException {
		for (int i=0; i < options.input.size(); i++) {
			File f = new File(options.input.get(i));
	
			// Permet d'avoir le nom complet du fichier (chemin absolu, sans signes
			// spéciaux(. et .. par ex))
			String input = f.getCanonicalPath();
	
			if (f.isDirectory()) {
				File[] teiFiles = f.listFiles();
				if (options.concat == true) {
					for (File file : teiFiles) {
						String name = file.getName();
						if (file.isFile() && (name.toLowerCase().endsWith(options.inputFormat))) {
							mainProcess(file.getAbsolutePath(), options.output, options);
						} else if (file.isDirectory()) {
							List <String> li = options.input;
							List <String> newli = new ArrayList<String>();
							newli.add(file.getAbsolutePath());
							options.input = newli;
							processFiles(options);
							options.input = li;
						}
					}
				} else {
					String outputDir = "";
					if (options.output == null) {
						if (input.endsWith("/")) {
							outputDir = input;
						} else {
							outputDir = input + "/";
						}
					} else {
						outputDir = options.output;
						if (!outputDir.endsWith("/")) {
							outputDir = options.output + "/";
						}
						File fdirout = new File(outputDir);
						// Permet d'avoir le nom complet du fichier (chemin absolu, sans
						// signes
						// spéciaux(. et .. par ex))
						String output = fdirout.getCanonicalPath();
						if (input.equals(output)) {
							System.err.println("Attention répertoires entrée et sortie identiques");
						}
					}
					
					File outFile = new File(outputDir);
					if (outFile.exists()) {
						if (!outFile.isDirectory()) {
							System.out.println("\n Erreur :" + outputDir
									+ " est un fichier, vous devez spécifier un nom de dossier pour le stockage des résultats. \n");
							System.exit(1);
						}
					} else {
						new File(outputDir).mkdir();
					}
		
					for (File file : teiFiles) {
						String name = file.getName();
						if (file.isFile() && (name.toLowerCase().endsWith(options.inputFormat))) {
							String outputFileName;
							if (options.commands.contains("replace")) {
								outputFileName = name;
							} else if (options.shortextension) {
								outputFileName = Utils.basename(name);
								if (outputFileName.endsWith(Utils.EXT_PUBLISH))
									outputFileName = outputFileName.substring(0, outputFileName.length() - Utils.EXT_PUBLISH.length()) + options.outputFormat;
								else
									outputFileName = outputFileName + options.outputFormat;
							} else {
								outputFileName = Utils.basename(name) + options.outputFormat;
							}
							
							if (options.verbose == true) {
								System.out.printf("Dir: %s --> %s%n", file.getAbsolutePath(), outputDir + outputFileName);
							}
							mainProcess(file.getAbsolutePath(), outputDir + outputFileName, options);
						} else if (file.isDirectory()) {
							List <String> li = options.input;
							List <String> newli = new ArrayList<String>();
							newli.add(file.getAbsolutePath());
							options.input = newli;
							processFiles(options);
							options.input = li;
						}
					}
				}
			} else {
				/*
				if (!(Utils.validFileFormat(input, Utils.EXT))) {
					System.err.println("Le fichier d'entrée du programme doit avoir l'extension " + Utils.EXT);
					return;
				}
				*/
				String output;
				if (options.output == null) {
					if (options.commands.contains("replace"))
						output = input;
					else {
						output = Utils.pathname(input) + File.separatorChar;
						if (options.shortextension) {
							String bn = Utils.basename(input);
							if (bn.endsWith(Utils.EXT_PUBLISH))
								output += bn.substring(0, bn.length() - Utils.EXT_PUBLISH.length()) + options.outputFormat;
							else
								output += bn + options.outputFormat;
						} else {
							output += Utils.basename(input) + options.outputFormat;
						}
					}
					// output = Utils.pathname(input) + File.separatorChar + Utils.basename(input) + options.outputFormat;
				} else if (new File(options.output).isDirectory()) {
					output = (options.output.endsWith("/")) ? options.output : (options.output + "/");
					if (options.commands.contains("replace")) {
						output += input;
					} else {
						// output = options.output + Utils.basename(input) + options.outputFormat;
						String bn = Utils.basename(input);
						if (options.shortextension) {
							if (bn.endsWith(Utils.EXT_PUBLISH))
								output += bn.substring(0, bn.length() - Utils.EXT_PUBLISH.length()) + options.outputFormat;
							else
								output += bn + options.outputFormat;
						} else {
							output += bn + options.outputFormat;
						}
					}
				} else {
					output = options.output;
				}
	
				if (options.verbose == true) {
					System.out.printf("File: %s --> %s%n", input, output);
				}
				mainProcess(input, output, options);
			}
		}
	}
}
