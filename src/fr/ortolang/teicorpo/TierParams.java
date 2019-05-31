package fr.ortolang.teicorpo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

/**
 * local temporary structures description of relation between tiers
 */
class DescTier {
	String tier;
	String type;
	String parent;
	String workingTier;
	String workingParent;

	public String toString() {
		String s = "Tier: " + tier + " type(" + type + ") parent(" + parent + ")";
		return s;
	}
	DescTier() {
		tier = null;
		type = null;
		parent = null;
		workingTier = null;
		workingParent = null;
	};

	/**
	 * check and return type value
	 */
	static String whichType(String t) {
		if (t.equals("-"))
			return LgqType.ROOT;
		if (t.equals("assoc"))
			return LgqType.SYMB_ASSOC;
		if (t.equals("incl"))
			return LgqType.INCLUDED;
		if (t.equals("symbdiv"))
			return LgqType.SYMB_DIV;
		if (t.equals("timediv"))
			return LgqType.TIME_DIV;
		if (t.equals("point"))
			return LgqType.POINT;
		if (t.equals("timeint"))
			return LgqType.TIME_INT;
		return null;
	}
	public String workingToString() {
		String s = "Tier: " + workingTier + " type(" + type + ") parent(" + workingParent + ")";
		return s;
	}
}

class TierParams {
	boolean verbose;
	boolean forceEmpty;
	boolean rawLine;
	boolean sectionDisplay;
	List <String> input;
	String output;
	String mediaName;
	String encoding;
	String options;
	boolean nospreadtime;
	boolean detectEncoding;
	boolean clearChatFormat;
	HashSet<String> commands;  // for -c parameter
	HashSet<String> doDisplay;
	HashSet<String> dontDisplay;
	TreeMap<String, String> tv;
	int level;
	boolean raw;
	boolean noHeader;
	boolean iramuteq;
	boolean concat;
	String outputFormat;
	String inputFormat;
	boolean locutor;
	boolean dtdValidation;
	boolean erase;
	boolean eraseDone;
	ArrayList<DescTier> ldt = new ArrayList<DescTier>();
	boolean shortextension;
	String defaultAge;
	String situation;
	boolean ignorePraatNumbering;
	//String syntax;
	String normalize;
	String target;
	String model;
	boolean noerror;
	String syntaxformat;
	boolean sandhi;
	Codes codes;
	String program;
	public boolean test;
	boolean absolute;
	boolean tiernames;
    boolean tiernamescontent;

	TierParams() {
		noerror = false;
		verbose = false;
		input = new ArrayList<String>();
		output = null;
		mediaName = null;
		encoding = null;
		detectEncoding = true;
		commands = new HashSet<String>();
		doDisplay = new HashSet<String>();
		dontDisplay = new HashSet<String>();
		level = 0; // all levels
		forceEmpty = true;
		rawLine = false;
		noHeader = false;
		clearChatFormat = false;
		raw = false;
		sectionDisplay = false;
		options = "";
		nospreadtime = false;
		iramuteq = false;
		concat = false;
		erase = true;
		eraseDone = false;
		tv = new TreeMap<String, String>();
		locutor = false;
		dtdValidation = false;
		outputFormat = "";
		inputFormat = "";
		shortextension = false;
		defaultAge = "40.0";
		situation = null;
		ignorePraatNumbering = false;
		syntaxformat = "w";
		//syntax = "";
		model = "";
		program = "";
		normalize ="";
		target ="";
		sandhi = false;
		codes = new Codes();
		codes.standardCodes();
		absolute = false;
		test = false;
		tiernames = false;
		tiernamescontent = false;
	}
	void addCommand(String s) {
		commands.add(s);
	}
	void addDoDisplay(String s) {
		doDisplay.add(s.toLowerCase());
	}
	void addDontDisplay(String s) {
		dontDisplay.add(s.toLowerCase());
	}
	void addTv(String info) {
		int p = info.indexOf(":");
		if (p<1 || p >= info.length()) {
			System.err.println("error: txm information ignored (missing :) => " + info);
		} else {
			tv.put(info.substring(0, p), info.substring(p+1));
		}
	}
	void setLevel(int l) {
		level = l;
	}
	/*
	boolean isLevel(int t) {
		return (level == 0 || t <= level) ? true : false; // si level == 0 all else if 't' is <= level
	}
	*/
	String praatParamsToString() {
		String s = "Encoding: " + ((encoding==null)?"none":encoding);
		for (int i=0; i < ldt.size(); i++) {
			s += " " + ldt.get(i).toString();
		}
		return s;
	}
	
	private static boolean test(String s, HashSet<String> dd) {
		s = s.toLowerCase();
		for (String f: dd) {
			//System.out.printf("%s %s%n", s, f);
			if (f.startsWith("*") && f.endsWith("*")) {
				if (s.indexOf(f.substring(1, f.length()-2)) >= 0) return true;
			} else if (f.startsWith("*")) {
				if (s.startsWith(f.substring(1))) return true;
			} else if (f.endsWith("*")) {
				if (s.endsWith(f.substring(0, f.length()-1))) return true;
			} else {
				if (s.equals(f)) return true;
			}
		}
		return false;
	}
	
	boolean isDoDisplay(String s) {
		return isDoDisplay(s,0);
	}
	boolean isDontDisplay(String s) {
		return isDontDisplay(s,0);
	}
	boolean isDoDisplay(String s, int level) {
		if (doDisplay.size() < 1) return true;
		if (level >= 3) {
			if (TierParams.test("=3", doDisplay)) return true;
		}
		if (level == 2) {
			if (TierParams.test("=2", doDisplay)) return true;
		}
		if (level == 1) {
			if (TierParams.test("=1", doDisplay)) return true;
		}
		return TierParams.test(s, doDisplay);
	}
	boolean isDontDisplay(String s, int level) {
		if (dontDisplay.size() < 1) return false;
		if (level >= 3) {
			if (TierParams.test("=3", dontDisplay)) return true;
		}
		if (level == 2) {
			if (TierParams.test("=2", dontDisplay)) return true;
		}
		if (level == 1) {
			if (TierParams.test("=1", dontDisplay)) return true;
		}
		return TierParams.test(s, dontDisplay);
	}

	public static void printVersionMessage() {
    	System.out.println("TeiCorpo (version "+ Utils.versionSoft +") " + Utils.versionDate + " Version TEI_CORPO: " + Utils.versionTEI);
	}
	
	public static void printUsageMessage(String mess, String ext1, String ext2, int style) {
		System.err.printf(mess);
		System.err.println("         (without option or -i) name of the file or directory where are located the TEI files to be converted.");
		if (!ext1.isEmpty())
			System.err.println("            The files have for extension " + ext1);
		System.err.println("         -o name of the output file or name of the output diretory for all results");
		System.err.println("            -if this option is not specified, the output file will have the same name as the input");
		System.err.println("               file but with the extension that matches the required format");
		System.err.println("            -if this option is specified, the output file will have the exact name provided.");
		System.err.println("            -if a directory is provided as input and no -o option is provided, result files");
		System.err.println("               will be in the same directory as input files.");
		System.err.println("            -otherwise option -o has to point to a directory.");

		if (style == 4) {
			System.err.println("         -from format_of_input_files: chat elan praat trs tei");
			System.err.println("         -to format_of_output_files: chat elan praat trs tei iramuteq txm lexico letrameur srt subthtml\n");
		}
		
		System.err.println("         -n level: imbrication level (1 means main lines, 2 means secondary lines)");
		System.err.println("         -a name : the tier with this name is produced as output (generic characters are accepted)");
		System.err.println("         -s name : the tier with this name is removed from output (generic characters are accepted)");
		System.err.println("         -rawline : exports utterances with removing spoken language special codes");
		System.err.println("         -normalize format : normalization is done from this format - possible options: clan ca");
		System.err.println("         -target format : normalization is done towards this format - possible options: praat\n");

		System.err.println("         -short : file extensions do not contain tei_corpo");
		System.err.println("         -p parameter_file: contains parameters with the same format as the command line, one parameter per line.");
		System.err.println("         --noerror : errors in the parameter are considered as warnings");
		System.err.println("         --dtd - this option allows to validate the format of the XML files");
		/*
		System.err.println("            with this option specified, Transcriber dtd (Trans-14.dtd) must be in the same\n" +
						   "            directory as the transcriber file. Downloading DTD for Transcriber : \n" +
						   "            http://sourceforge.net/p/trans/git/ci/master/tree/etc/trans-14.dtd\n");
		*/

		if (style == 4) {
			System.err.println("         -stdevent ascci standard format for events in text mode");
			System.err.println("         -advevent advanced unicode format for event in text mode");
		}
		if (style == 5) {
			printImportPartitionMessage();
		}
		if (style == 3) {
			System.err.println("         *** paramater for editing TEI files***");
			System.err.printf ("         -c commands:%n\t-c media=value%n\t-c mimetype=value%n\t-c docname=value%n\t-c chgtime=value%n\t-c replace%n");
		}
		if (style == 2) {
			System.err.println("         *** parameter for export to TXM/Iramuteq/Le Trameur ***");
			System.err.println("         -tv \"type:value\" : a parameter type:value is added to <u> or <w> tags for txm or lexico or le trameur");
			System.err.println("         -section : add a section indication at the end of each utterance (for lexico/le trameur)");
			System.err.println("         -tiernames : print the value of the locutors and tiernames in the transcriptions");
			System.err.println("         -tiernamescontent : add all fields in tiernames as for other words");
			System.err.println("         -sandhi : specific information for the analyse of liaisons");
		}
		if (style == 6) {
			System.err.println("         *** parameters for export in Iramuteq ***");
			System.err.println("         -raw : text is exported after removing all locutors or spoken language marking");
			System.err.println("         -tv \"type:value\" : a parameter type:value is added to <u> or <w> tags for txm or lexico or le trameur");
			System.err.println("         -iramuteq : headers for iramuteq");
			System.err.println("         -concat : concatenate result files for iramuteq");
			System.err.println("         -append : do not erase destination file before processing - to be used with concat");
		}
		if (style == 7) {
			System.err.println("         -syntaxformat : format of syntatic annotation (conll / ref / w)");
			System.err.println("         -model name : model file for syntax");
			System.err.println("         -program name : executable file for syntax");
		}
		if (style == 8) {
			System.err.println("         -absolute : syntax of included tools/ library in html");
		}
		System.err.println("	     -usage or -help = display this message");
		// System.exit(1);
	}

	private static void printImportPartitionMessage() {
		System.err.println("         *** parameter for importing partition files ***");
		System.err.println("         -m name/address of media file");
		System.err.println("         -e encoding (by default detect encoding)");
		System.err.println("         -d default UTF8 encoding ");
		System.err.println("         -t tiername type parent (describe relations between tiers)");
		System.err.println("              available types: - assoc incl symbdiv timediv");
	}
	
	public static void frPrintUsageMessage(String mess, String ext1, String ext2, int style) {
		System.err.printf(mess);
		System.err.println("         :(sans option ou -i) nom du fichier ou repertoire où se trouvent les fichiers Tei à convertir.");
		if (!ext1.isEmpty())
			System.err.println("            Les fichiers ont pour extension " + ext1);
		System.err.println("         -o nom du fichier de sortie au format Elan (.eaf) ou du repertoire de résultats");
		System.err.println("            -si cette option n'est pas spécifiée, le fichier de sortie aura le même nom que le fichier d'entrée avec l'extension qui correspond au format demandé");
		System.err.println("            -si cette option est spécifiée, le fichier de sortie aura le même nom exact indiqué.");
		System.err.println("            -si on donne un repertoire en entrée et que l'option -o n'est pas spécifiée, les résultats seront générés dans le même répertoire.");
		System.err.println("            -sinon l'option -o doit désigner un nom  de répertoire.");
		System.err.println("         -p fichier_de_parametres: contient les paramètres sous leur format ci-dessous, un jeu de paramètre par ligne.");
		System.err.println("         -n niveau: niveau d'imbrication (1 pour lignes principales)");
		System.err.println("         -a name : le locuteur/champ name est produit en sortie (caractères génériques acceptés)");
		System.err.println("         -s name : le locuteur/champ name est suprimé de la sortie (caractères génériques acceptés)");
		System.err.println("         -rawline : exporte des énoncés sans marqueurs spéficiques de l'oral");
		System.err.println("         -normalize format : normalisation réalisée à partir du format indiqué en paramètre - options possibles: clan");
		System.err.println("         -target format : normalisation réalisée en direction du format indiqué en paramètre");
		System.err.println("         -short : les extensions fichiers autres que TEI_CORPO ne contiennent pas tei_corpo");
		System.err.println("         --noerror : considère les erreurs dans les paramètres comme des warnings");
		System.err.println("         --dtd cette option permet de vérifier que les fichiers sont conformes à leur dtd");
		/*
		System.err.println("            si cette option est spécifiée, la dtd (Trans-14.dtd) doit se trouver dans le même repertoire que le fichier Transcriber\n"
						+ "\t\tTéléchargement de la DTD de Transcriber : http://sourceforge.net/p/trans/git/ci/master/tree/etc/trans-14.dtd");
		*/
		if (style == 5) {
			printImportPartitionMessage();
		}
		if (style == 4) {
			System.err.println("         -from format des fichiers input");
			System.err.println("         -to format des fichiers output");
			System.err.println("         -stdevent format ascci standard des événements en mode texte");
			System.err.println("         -advevent format unicode avancé des événements en mode texte");
		}
		if (style == 3) {
			System.err.println("         *** paramètre pour édition de fichier TEI***");
			System.err.printf ("         -c commands:%n\t-c media=value%n\t-c mimetype=value%n\t-c docname=value%n\t-c chgtime=value%n\t-c replace%n");
		}
		if (style == 2) {
			System.err.println("         *** paramètre pour export dans TXM/Iramuteq/Le Trameur ***");
			System.err.println("         -tv \"type:valeur\" : un champ type:valeur est ajouté dans les <w> de txm ou lexico ou le trameur");
			System.err.println("         -section : ajoute un indicateur de section en fin de chaque énoncé (pour lexico/le trameur)");
			System.err.println("         -syntax nom : choix pour la syntaxe à exporter");
			System.err.println("         -sandhi : information spécifique intégrées pour l'analyse des liaisons");
		}
		if (style == 6) {
			System.err.println("         -raw : exporte le texte sans aucune marqueurs de locuteur ni marqueurs spéficiques de l'oral");
			System.err.println("         -iramuteq : headers for iramuteq");
			System.err.println("         *** paramètre pour export dans Iramuteq ***");
			System.err.println("         -tv \"type:valeur\" : un champ type:valeur est ajouté dans les <u> ou <w> de txm ou lexico ou le trameur");
			System.err.println("         -concat : concaténation des fichiers résultats for iramuteq");
			System.err.println("         -append : pas d'effacement préalable du fichier destination si concaténation");
		}
		if (style == 7) {
			System.err.println("         -syntaxformat : format de l'export syntaxique (conll / ref / w)");
			System.err.println("         -model nom : fichier modèle pour la syntaxe");
			System.err.println("         -program name : executable file for syntax");
//			System.err.println("         -syntax nom : choix pour la syntaxe à générer");
		}
		if (style == 8) {
			System.err.println("         -absolute : syntaxe de l'inclusion de la librairie tools/ en html");
		}
		System.err.println("	     -usage ou -help = affichage ce message");
		// System.exit(1);
	}

	private static void frPrintImportPartitionMessage() {
		System.err.println("         *** paramètre pour import fichiers partition ***");
		System.err.println("         -m nom/adresse du fichier média");
		System.err.println("         -e encoding (par défaut detect encoding)");
		System.err.println("         -d default UTF8 encoding ");
		System.err.println("         -t tiername type parent (describe relations between tiers)");
		System.err.println("             types autorisés: - assoc incl symbdiv timediv");
	}

	public static boolean processArgs(String[] args, TierParams options, String usage, String ext1, String ext2, int style) {
//		options.inputFormat = ext1;
		options.outputFormat = ext2.isEmpty() ? Utils.EXT : ext2;
		if (args.length == 0) {
			System.err.printf("No arguments were specified.%n%n");
			printUsageMessage(usage, ext1, ext2, style);
			return false;
		} else {
			for (int i = 0; i < args.length; i++) {
				String argument = args[i].toLowerCase();
				try {
					if (argument.equals("--noerror")) {
						options.noerror = true;
					} else if (argument.equals("-i")) {
						i++;
						continue;
					} else if (argument.equals("-o")) {
						i++;
						continue;
					} else if (argument.equals("-to")) {
						i++;
						continue;
					} else if (argument.equals("-from")) {
						i++;
						continue;
					} else if (argument.equals("-n")) {
						i++;
						continue;
					} else if (argument.equals("-a")) {
						i++;
						continue;
					} else if (argument.equals("-s")) {
						i++;
						continue;
					} else if (argument.equals("-c")) {
						i++;
						continue;
					} else if (argument.equals("-f")) {
						i++;
						continue;
					} else if (argument.equals("-tv")) {
						i++;
						continue;
					} else if (argument.equals("-m")) {
						i++;
						continue;
					} else if (argument.equals("-e")) {
						i++;
						continue;
					} else if (argument.equals("-tiernames")) {
						i++;
						continue;
					} else if (argument.equals("-tiernamescontent")) {
						i++;
						continue;
					} else if (argument.equals("-age")) {
						i++;
						continue;
					} else if (argument.equals("-syntaxformat")) {
						i++;
						continue;
//					} else if (argument.equals("-syntax")) {
//						i++;
//						continue;
					} else if (argument.equals("-model")) {
						i++;
						continue;
					} else if (argument.equals("-program")) {
						i++;
						continue;
					} else if (argument.equals("-normalize")) {
						i++;
						continue;
					} else if (argument.equals("-target")) {
						i++;
						continue;
					} else if (argument.equals("-situation")) {
						i++;
						continue;
					} else if (argument.equals("-t")) {
						i++;
						i++;
						i++;
						continue;
					} else if (argument.equals("-p")) {
						if (i+1 >= args.length) {
							printUsageMessage(usage, ext1, ext2, style);
							if (!options.noerror) return false; else break;
						}
						i++;
						getTierParams(args[i], options.ldt, options);
					} else if (argument.equals("-h") || argument.equals("-help") || argument.equals("-usage")) {
						printUsageMessage(usage, ext1, ext2, style);
						if (!options.noerror) return false;
					} else {
						continue;
					}
				} catch (Exception e) {
					printUsageMessage(usage, ext1, ext2, style);
					return false;
				}
			}
			for (int i = 0; i < args.length; i++) {
				String argument = args[i].toLowerCase();
				try {
					if (argument.equals("--noerror")) {
						continue;
					} else if (argument.equals("-i")) {
						if (i+1 >= args.length) {
							printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.input.add(args[i]);
					} else if (argument.equals("-o")) {
						if (i+1 >= args.length) {
							printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						if (i >= args.length || options.output != null) {
							printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.output = args[i];
					} else if (argument.equals("-to")) {
						if (i+1 >= args.length) {
							printUsageMessage(usage, "-", "-", style);
							return false;
						}
						i++;
						options.outputFormat = TeiCorpo.extensions(args[i]);
					} else if (argument.equals("-from")) {
						if (i+1 >= args.length) {
							printUsageMessage(usage, "-", "-", style);
							return false;
						}
						i++;
						options.inputFormat = TeiCorpo.extensions(args[i]);
					} else if (argument.equals("-n")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -n is not followed by a value");
							// printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						try {
							options.setLevel(Integer.parseInt(args[i]));
						} catch(Exception e) {
							System.err.println("the parameter -n is not followed by a number.");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							if (!options.noerror) return false; else break;
						}
					} else if (argument.equals("-c")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -c is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.addCommand(args[i]);
					} else if (argument.equals("-a")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -a is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.addDoDisplay(args[i]);
					} else if (argument.equals("-s")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -s is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.addDontDisplay(args[i]);
					} else if (argument.equals("-age")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -age is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.defaultAge = args[i];
					/*
					} else if (argument.equals("-syntax")) {
						if (i+1 >= args.length) {
							System.err.println("le parametre -syntax n'est pas suivi d'une valeur");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.syntax = args[i];
					*/
					} else if (argument.equals("-syntaxformat")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -syntaxformat is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.syntaxformat = args[i];
					} else if (argument.equals("-model")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -model is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.model = args[i];
					} else if (argument.equals("-program")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -program is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.program = args[i];
					} else if (argument.equals("-situation")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -situation is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.situation = args[i];
					} else if (argument.equals("-tv")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -tv is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						options.addTv(args[i]);
					} else if (argument.equals("-f")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -f is not followed by a value");
							// Utils.printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
						i++;
						if (argument.equals("mor")) {
							options.options = "mor";
						} else if (argument.equals("xmor")) {
							options.options = "xmor";
						} else if (argument.equals("morext")) {
							options.options = "morext";
						} else if (argument.equals("xmorext")) {
							options.options = "xmorext";
						} else {
							printUsageMessage(usage, ext1, ext2, style);
							return false;
						}
					} else if (argument.equals("-p")) {
						i++;
						continue;
					} else if (argument.equals("-stdevent")) {
						options.codes.standardCodes();
						continue;
					} else if (argument.equals("-advevent")) {
						options.codes.advancedCodes();
						continue;
					} else if (argument.equals("-rawline")) {
						options.rawLine = true;
						continue;
					} else if (argument.equals("-tiernames")) {
						options.tiernames = true;
						continue;
					} else if (argument.equals("-tiernamescontent")) {
						options.tiernamescontent = true;
						continue;
					} else if (argument.equals("-test")) {
						options.test = true;
						continue;
					} else if (argument.equals("-nospreadtime")) {
						options.nospreadtime = true;
						continue;
					} else if (argument.equals("-pure")) {
						Utils.teiStylePure = true;
						continue;
					} else if (argument.equals("--dtd")) {
						options.dtdValidation = true;
					} else if (argument.equals("-normalize")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -normalize is not followed by a value");
							return false;
						}
						i++;
						options.normalize = args[i];
						continue;
					} else if (argument.equals("-target")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -target is not followed by a value");
							return false;
						}
						i++;
						options.target = TeiCorpo.extensions(args[i]);
						continue;
					} else if (argument.equals("-noheader")) {
						options.noHeader = true;
						continue;
					} else if (argument.equals("-ipn")) {
						options.ignorePraatNumbering = true;
						continue;
					} else if (argument.equals("-v")) {
						options.verbose = true;
						continue;
					} else if (argument.equals("-raw")) {
						options.raw = true;
						continue;
					} else if (argument.equals("-concat")) {
						options.concat = true;
						continue;
					} else if (argument.equals("-short")) {
						options.shortextension = true;
						continue;
					} else if (argument.equals("-append")) {
						options.erase = false;
						continue;
					} else if (argument.equals("-sandhi")) {
						options.sandhi = true;
						continue;
					} else if (argument.equals("-absolute")) {
						options.absolute = true;
						continue;
					} else if (argument.equals("-iramuteq")) {
						options.iramuteq = true;
						options.raw = true;
						options.concat = true;
						continue;
					} else if (argument.equals("-loc")) {
						options.locutor = true;
						continue;
					} else if (argument.equals("-section")) {
						options.sectionDisplay = true;
						continue;
					} else if (argument.equals("-m")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -m is not followed by a value");
							return false;
						}
						i++;
						options.mediaName = args[i];
					} else if (argument.equals("-e")) {
						if (i+1 >= args.length) {
							System.err.println("the parameter -e is not followed by a value");
							return false;
						}
						i++;
						options.encoding = args[i];
						options.detectEncoding = false;
					} else if (argument.equals("-t")) {
						if (i+3 >= args.length) {
							System.err.println("the parameter -t is not followed by a value");
							return false;
						}
						DescTier d = new DescTier();
						i++;
						d.tier = args[i];
						i++;
						d.type = args[i];
						if (DescTier.whichType(args[i]) == null) {
							System.err.println("the parameter -t does not contain a known value: " + args[i]);
							if (!options.noerror) return false; else break;
						}
						i++;
						d.parent = args[i];
						options.ldt.add(d);
					} else if (argument.equals("-d")) {
						options.detectEncoding = false;
						options.encoding = "UTF-8";
					} else {
						options.input.add(args[i]);
						// Utils.printUsageMessage(usage, ext1, ext2, style);
						// return false;
					}
				} catch (Exception e) {
					printUsageMessage(usage, ext1, ext2, style);
					return false;
				}
			}
		}
		if (options.input == null) {
			System.out.println("No input file to process.");
			return false;
		}
		return true;
	}

	public static boolean getTierParams(String fn, ArrayList<DescTier> ldt, TierParams pr) {
		List<String> ls = null;
		try {
			ls = Utils.loadTextFile(fn);
		} catch (IOException e) {
			System.err.println("Cannot process file: " + fn);
			return false;
		}
		for (int k = 0; k < ls.size(); k++) {
			String l = ls.get(k);
			String[] p = l.split("\\s+");
			if (p.length > 0) {
				if (p[0].equals("-e") || p[0].equals("e")) {
					if (p.length > 1) {
						pr.encoding = p[1];
						pr.detectEncoding = false;
					}
				} else if (p[0].equals("-d") || p[0].equals("d")) {
					pr.detectEncoding = false;
					pr.encoding = "UTF-8";
				} else if (p[0].equals("-m") || p[0].equals("m")) {
					if (p.length > 1) {
						pr.mediaName = p[1];
					}
				} else if (p[0].equals("-t") || p[0].equals("t")) {
					DescTier d = new DescTier();
					if (p.length < 4)
						printImportPartitionMessage();
					d.tier = p[1];
					d.type = p[2];
					d.parent = p[3];
					ldt.add(d);
				} else if (p[0].equals("-n") || p[0].equals("n")) {
					if (p.length < 2) {
						System.err.println("Wrong line [" + l + "] in the parameter file: " + fn);
						return false;
					}
					pr.setLevel(Integer.parseInt(p[1]));
				} else if (p[0].equals("-s") || p[0].equals("s")) {
					if (p.length < 2) {
						System.err.println("Wrong line [" + l + "] in the parameter file: " + fn);
						return false;
					}
					pr.addDontDisplay(p[1]);
				} else if (p[0].equals("-a") || p[0].equals("a")) {
					if (p.length < 2) {
						System.err.println("Wrong line [" + l + "] in the parameter file: " + fn);
						return false;
					}
					pr.addDoDisplay(p[1]);
			} else {
					System.err.println("Unknown format in the parameter file: " + fn);
					return false;
				}
			}
		}
		return true;
	}
}
