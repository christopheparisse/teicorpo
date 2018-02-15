/**
 * @version:0.6
 */

package fr.ortolang.teicorpo;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ConversionUI extends JFrame {

	File[] inputFileNames;
	File output;
	JButton exec;
	JButton stop;
	JTextArea jt;
	FormatJCheckBoxes outputFcb;
	FormatJCheckBoxes inputFcb;
	ButtonField bf1;
	ButtonField bf2;
	boolean nullOutput = false;
	String arguments = "";  // mettre à null pour utiliser cette fonction
	String argumentsOutput = "";  // mettre à null pour utiliser cette fonction
	TierParams options;

	static int nbConv = 0;
	static final long serialVersionUID = 1L;

	public ConversionUI() throws URISyntaxException {
		options = new TierParams();

		this.getContentPane().setLayout(null);

		this.setTitle("Conversions (version " + Utils.versionSoft + ") " + Utils.versionDate + " Version TEI_CORPO: "
				+ Utils.versionTEI);

		inputFcb = new FormatJCheckBoxes();
		inputFcb.setVisible(true);
		this.add(inputFcb);

		inputFcb.trs.setBounds(30, 30, 200, 50);
		inputFcb.chat.setBounds(180, 30, 200, 50);
		inputFcb.tei.setBounds(220, 30, 200, 50);
		inputFcb.elan.setBounds(250, 30, 200, 50);
		inputFcb.praat.setBounds(280, 30, 200, 50);
		inputFcb.setBounds(50, 80, 500, 20);

		outputFcb = new FormatJCheckBoxes();
		outputFcb.setVisible(true);
		this.add(outputFcb);

		outputFcb.trs.setBounds(30, 110, 200, 50);
		outputFcb.chat.setBounds(180, 110, 200, 50);
		outputFcb.tei.setBounds(220, 110, 200, 50);
		outputFcb.elan.setBounds(220, 110, 200, 50);
		outputFcb.praat.setBounds(220, 110, 200, 50);
		outputFcb.setBounds(50, 140, 500, 20);
/*
		JLabel j0 = new JLabel("Pour un interface utilisateur plus détaillée, utiliser le web service:http:");
		j0.setBounds(32, 60, 1000, 300);
		this.add(j0);

		JLabel j01 = new JLabel("http://ct3.ortolang.fr/teiconvert/");
		j01.setBounds(32, 50, 1000, 300);
		this.add(j01);
*/
		JLabel jl = new JLabel("Convertir de");
		jl.setBounds(32, 40, 300, 50);

		JLabel jl2 = new JLabel("Vers");
		jl2.setBounds(32, 100, 300, 50);

		this.add(jl);
		this.add(jl2);

		bf1 = new ButtonField("Document(s) à convertir");
		bf2 = new ButtonField("Dossier où seront enregistrées les conversions");
		bf1.field.setEditable(false);
		bf2.field.setEditable(false);

		bf1.button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Chooser ch = new Chooser(true);
				bf1.field.setText(bf1.listFilesToString(ch.fileNames));
				inputFileNames = ch.fileNames;
			}
		});

		bf2.button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Chooser ch = new Chooser(false);
				bf2.field.setText(bf2.listFilesToString(ch.fileNames));
				output = ch.fileNames[0];
			}
		});

		bf1.button.setBounds(340, 200, 100, 25);
		bf1.field.setBounds(30, 200, 300, 25);
		bf1.jl.setBounds(32, 140, 300, 100);
		this.add(bf1.jl);
		this.add(bf1.button);
		this.add(bf1.field);

		bf2.button.setBounds(340, 260, 100, 25);
		bf2.field.setBounds(30, 260, 300, 25);
		bf2.jl.setBounds(32, 200, 300, 100);
		this.add(bf2.jl);
		this.add(bf2.button);
		this.add(bf2.field);

		exec = new JButton("Convertir!");
		exec.setBounds(32, 320, 100, 50);
		this.add(exec);

		////////////// Affichage des résultats //////////////
		jt = new JTextArea(10, 40);
		add(jt);
		jt.setText("Résultats :\n");
		jt.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(jt);
		add(scrollPane);
		scrollPane.setBounds(32, 400, 536, 400);

		setLocation(40, 40);
		setSize(600, 900);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	}

	public void run() {
		exec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				final Thread t = new Thread() {
					public void run() {
						/////////////////////////////////////// Vérification des
						/////////////////////////////////////// inputs
						/////////////////////////////////////// utilisateur///////////////////////////////////////////////////////////////
						if (outputFcb.formats.size() == 0) {
							JOptionPane.showMessageDialog(null, "Veuillez choisir le(s) format(s) de conversion.",
									"Information", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						if (inputFileNames == null) {
							JOptionPane.showMessageDialog(null, "Veuillez choisir le(s) fichier(s) à convertir.",
									"Information", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						if (output == null) {
							nullOutput = true;
							if (inputFileNames[0].isFile() || inputFileNames.length > 1) {
								output = new File(inputFileNames[0].getParent());
							} else {
								output = new File(inputFileNames[0].getAbsolutePath());
							}
							output.mkdir();
							bf2.field.setText(output.getName());
						}

						///////////////////////////////////////////////////////////// Conversions
						///////////////////////////////////////////////////////////// ///////////////////////////////////////////////////////////////

						for (File f : inputFileNames) {
							Conversion(f);
						}
						showMsg();
						printResults("\nNombre total de conversions: " + nbConv + ".\n");
						// Réinitialisation des comptes
						reinit();
						output = null;
					}
				};
				t.start();
			}
		});
	}

	// Conversion d'un fichier
	public void Conversion(final File f) {
		if (f.isFile()) {
			if (acceptFile(f, inputFcb.formats)) {
				runConversion(f, output.getAbsolutePath(), outputFcb.formats);
			}
		} else {
			File[] files = f.listFiles();
			for (File cFile : files) {
				if (acceptFile(cFile, inputFcb.formats)) {
					runConversion(cFile, output.getAbsolutePath(), outputFcb.formats);
				} else if (cFile.isDirectory()) {
					File[] subFiles = cFile.listFiles();
					for (File sf : subFiles) {
						if (acceptFile(sf, inputFcb.formats)) {
							if (nullOutput) {
								output = sf.getParentFile();
							}
							runConversion(sf, output.getAbsolutePath(), outputFcb.formats);
						}
					}
				}
			}
		}
	}

	// Affichage informations à la fin des conversations
	public void showMsg() {
		if (nbConv == 0) {
			JOptionPane.showMessageDialog(null, "Aucune conversion à effectuer.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		} else if (nbConv == 1) {
			JOptionPane.showMessageDialog(null, "1 conversion a été effectuée.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, nbConv + " conversions ont été effectuées.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	// Conversion d'un fichier dans le(s) format(s) spécifié(s)
	public void runConversion(File inputFile, String outputDir, HashSet<String> formats) {

		String inputName = inputFile.getName();
		String inputAbsPath = inputFile.getAbsolutePath();

		boolean existTeiFile = false;

		String outputTeiFileName = outputDir + "/" + Utils.basename(inputName) + Utils.EXT;

		if (new File(outputTeiFileName).exists()) {
			existTeiFile = true;
		}
		File outputTeiFile = new File(outputTeiFileName);

		try {
			// Création du fichier tei
			// System.err.printf("C-UI: %s %s%n", inputName, outputTeiFileName);
			if (!inputName.endsWith(Utils.EXT)) {
				// Création des fichiers TEI
				if (inputName.toLowerCase().endsWith(".cha")) {
					ClanToTei cf = new ClanToTei();
					cf.mainProcess(inputAbsPath, outputTeiFileName, null);
					if (formats.contains("tei")) {
						nbConv++;
						printResults("New TEI file created from " + inputAbsPath + " to " + outputTeiFileName);
					}
				} else if (inputName.toLowerCase().endsWith(".trs") || inputName.toLowerCase().endsWith(".trs.xml")) {
					TranscriberToTei cf = new TranscriberToTei();
					cf.mainProcess(inputAbsPath, outputTeiFileName, null);
					if (formats.contains("tei")) {
						nbConv++;
						printResults("New TEI file created from " + inputAbsPath + " to " + outputTeiFileName);
					}
				} else if (inputName.toLowerCase().endsWith(".eaf")) {
					ElanToTei cf = new ElanToTei();
					cf.mainProcess(inputAbsPath, outputTeiFileName,null);
					if (formats.contains("tei")) {
						nbConv++;
						printResults("New TEI file created from " + inputAbsPath + " to " + outputTeiFileName);
					}
				} else if (inputName.toLowerCase().endsWith(".textgrid")) {
					if (arguments == null) // different from Empty
						arguments = JOptionPane.showInputDialog(null,
								"Veuillez ajouter les éventuelles options à utiliser ou validez:\n"
										+ "-e encoding (par défaut UTF8)\n"
										+ "-t tiername type parent (type de relation entre les tiers)\n"
										+ "-m adresse du media\n" + "types autorisés: assoc incl timediv"
										+ "-p ficher_parametres",
								"Conversion de Praat vers TEI", JOptionPane.QUESTION_MESSAGE);
					String[] allArgs = null;
					if (Utils.isNotEmptyOrNull(arguments)) {
						String[] addArgs = arguments.split("\\s+");
						if (Utils.isNotEmptyOrNull(outputTeiFileName))
							allArgs = new String[5 + addArgs.length];
						else
							allArgs = new String[3 + addArgs.length];
						allArgs[0] = "-i";
						allArgs[1] = inputFile.getAbsolutePath();
						allArgs[2] = "-x";
						int j = 3;
						if (Utils.isNotEmptyOrNull(outputTeiFileName)) {
							allArgs[j++] = "-o";
							allArgs[j++] = outputTeiFileName;
						}
						for (int i = 0; i < addArgs.length; i++) {
							allArgs[j] = addArgs[i];
							j++;
						}
					} else {
						if (Utils.isNotEmptyOrNull(outputTeiFileName))
							allArgs = new String[4];
						else
							allArgs = new String[2];
						allArgs[0] = "-i";
						allArgs[1] = inputFile.getAbsolutePath();
						if (Utils.isNotEmptyOrNull(outputTeiFileName)) {
							allArgs[2] = "-o";
							allArgs[3] = outputTeiFileName;
						}
					}

					PraatToTei.main(allArgs);

					if (formats.contains("tei")) {
						nbConv++;
						printResults("New TEI file created from " + inputAbsPath + " to " + outputTeiFileName);
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur fichier " + inputName,
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}

		try {
			for (String f : formats) {
				if (f.equals("chat") || f.equals("trs") || f.equals("eaf") || f.equals("textgrid")) {
					if (argumentsOutput == null) // different from Empty
						argumentsOutput = JOptionPane.showInputDialog(null,
								"Veuillez ajouter les éventuelles options à utiliser ou validez:\n"
										+ "-n niveau: niveau d'imbrication (1 pour lignes principales)\n"
										+ "-a name : le locuteur/champ name est produit en sortie (caractères génériques acceptés)\n"
										+ "-s name : le locuteur/champ name est suprimé de la sortie (caractères génériques acceptés)"
										+ "-p fichier_parametres",
								"Conversion depuis la TEI", JOptionPane.QUESTION_MESSAGE);
					if (Utils.isNotEmptyOrNull(argumentsOutput)) {
						String usage = "Description: Conversions convertit un fichier au format TEI en un fichier au format Chat, Praat, Elan, Transcriber\n";
						String[] addArgs = argumentsOutput.split("\\s+");
						if (TierParams.processArgs(addArgs, options, usage, Utils.EXT, f, 2) == false) {
							JOptionPane.showMessageDialog(null, "Erreur sur les paramètres.", "Erreur",
									JOptionPane.WARNING_MESSAGE);
						}
					}
					File teiFile;
					if (inputName.endsWith(Utils.EXT)) {
						teiFile = inputFile;
					} else {
						teiFile = new File(outputDir + "/" + Utils.basename(inputName) + Utils.EXT);
					}
					if (teiFile.exists()) {
						if (f.equals("chat") && !inputName.endsWith(".cha")) {
							String chatDirName = outputDir + "/";
							String outputName = chatDirName + inputName.split("\\.")[0] + ".cha";
							TeiToClan ttc = new TeiToClan();
							ttc.mainProcess(teiFile.getAbsolutePath(), outputName, null);
							nbConv++;
							printResults("New " + f.toUpperCase() + " file created from " + inputAbsPath + " to "
									+ outputName);
						} else if (f.equals("trs") && !(inputName.endsWith(".trs") || inputName.endsWith(".trs.xml"))) {
							String trsDirName = outputDir + "/";
							String outputName = trsDirName + inputName.split("\\.")[0] + ".trs";
							TeiToTranscriber ttt = new TeiToTranscriber();
							ttt.mainProcess(teiFile.getAbsolutePath(), outputName, null);
							nbConv++;
							printResults("New " + f.toUpperCase() + " file created from " + inputAbsPath + " to "
									+ outputName);
						} else if (f.equals("eaf") && !(inputName.endsWith(".eaf"))) {
							String eafDirName = outputDir + "/";
							String outputName = eafDirName + inputName.split("\\.")[0] + ".eaf";
							TeiToElan tte = new TeiToElan();
							tte.mainProcess(teiFile.getAbsolutePath(), outputName, null);
							nbConv++;
							printResults("New " + f.toUpperCase() + " file created from " + inputAbsPath + " to "
									+ outputName);
						} else if (f.equals("textgrid") && !(inputName.toLowerCase().endsWith(".textgrid"))) {
							String praatDirName = outputDir + "/";
							String outputName = praatDirName + inputName.split("\\.")[0] + ".textgrid";
							TeiToPraat ttp = new TeiToPraat();
							ttp.mainProcess(teiFile.getAbsolutePath(), outputName, null);
							nbConv++;
							printResults("New " + f.toUpperCase() + " file created from " + inputAbsPath + " to "
									+ outputName);
						}
					} else {
						JOptionPane.showMessageDialog(null,
								"Erreur rencontrée sur le fichier " + inputName
										+ ", format non pris en charge pour la conversion vers le format "
										+ f.toUpperCase() + ".",
								"Erreur", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erreur rencontrée sur le fichier " + inputName, "Erreur",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}

		// Si l'utilisateur ne demande pas la conversion tei
		if (!formats.contains("tei") && !existTeiFile) {
			outputTeiFile.delete();
		}
	}

	// Affichage des résultats dans la fenêtre "résultats"
	public void printResults(String s) {
		System.out.println(s);
		jt.append("\n" + s);
		jt.selectAll();
	}

	// Réinitialisation des données après conversion
	public void reinit() {
		nbConv = 0;
		outputFcb.formats = new HashSet<String>();
		outputFcb.tei.setSelected(false);
		outputFcb.chat.setSelected(false);
		outputFcb.trs.setSelected(false);
		outputFcb.elan.setSelected(false);
		outputFcb.praat.setSelected(false);

		inputFcb.formats = new HashSet<String>();
		inputFcb.tei.setSelected(false);
		inputFcb.chat.setSelected(false);
		inputFcb.trs.setSelected(false);
		inputFcb.elan.setSelected(false);
		inputFcb.praat.setSelected(false);

		bf1.field.setText("");
		bf2.field.setText("");

		inputFileNames = null;
		output = null;
	}

	public static boolean acceptFile(File f, HashSet<String> formats) {
		String fName = f.getName();
		for (String format : formats) {
			if (format.equals("chat") && fName.toLowerCase().endsWith(".cha")) {
				return true;
			} else if (format.equals("trs")
					&& (fName.toLowerCase().endsWith(".trs") || fName.toLowerCase().endsWith(".trs.xml"))) {
				return true;
			} else if (format.equals("eaf") && (fName.toLowerCase().endsWith(".eaf"))) {
				return true;
			} else if (format.equals("tei") && fName.toLowerCase().endsWith(Utils.EXT)) {
				return true;
			} else if (format.equals("textgrid") && fName.toLowerCase().endsWith(".textgrid")) {
				return true;
			}
		}
		return false;
	}

	class FormatJCheckBoxes extends JPanel implements ItemListener {

		private static final long serialVersionUID = 1L;

		JCheckBox tei;
		JCheckBox chat;
		JCheckBox trs;
		JCheckBox elan;
		JCheckBox praat;

		HashSet<String> formats = new HashSet<String>();

		public FormatJCheckBoxes() {
			this.setLayout(new GridLayout());

			tei = new JCheckBox("TEI");
			chat = new JCheckBox("CHAT");
			trs = new JCheckBox("TRANSCRIBER");
			elan = new JCheckBox("ELAN");
			praat = new JCheckBox("PRAAT");

			tei.addItemListener(this);
			chat.addItemListener(this);
			trs.addItemListener(this);
			elan.addItemListener(this);
			praat.addItemListener(this);

			add(trs);
			add(chat);
			add(tei);
			add(elan);
			add(praat);
		}

		public void itemStateChanged(ItemEvent e) {
			if (tei.isSelected()) {
				formats.add("tei");
			} else {
				formats.remove("tei");
			}
			if (chat.isSelected()) {
				formats.add("chat");
			} else {
				formats.remove("chat");
			}
			if (trs.isSelected()) {
				formats.add("trs");
			} else {
				formats.remove("trs");
			}
			if (elan.isSelected()) {
				formats.add("eaf");
			} else {
				formats.remove("eaf");
			}
			if (praat.isSelected()) {
				formats.add("textgrid");
			} else {
				formats.remove("textGrid");
			}
		}
	}

	class ButtonField {

		JButton button;
		JTextField field;
		JLabel jl;
		File[] filenames;

		public ButtonField(String title) {
			button = new JButton("Parcourir");
			field = new JTextField();
			jl = new JLabel();
			jl.setText(title);
		}

		public String listFilesToString(File[] files) {
			String filesList = "";
			if (files != null)
				for (File f : files) {
					filesList += f.getName();
					filesList += "; ";
				}
			return filesList;
		}
	}

	class Chooser extends JFrame {

		private static final long serialVersionUID = 1L;

		JFileChooser dialog;
		File[] fileNames;

		public Chooser(boolean dirAndFiles) {

			Preferences userPrefs = Preferences.userRoot().node("/fr/ortolang/tools/imports");
			File suggestedFile = new File(userPrefs.get("SAVEDIR", "/") + "/.", "");
			// System.out.println("GET *SAVEDIR " + suggestedFile);

			JFileChooser dialog = new JFileChooser();
			dialog.setSelectedFile(suggestedFile);
			dialog.setMultiSelectionEnabled(true);
			if (dirAndFiles) {
				dialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				if (dialog.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					fileNames = dialog.getSelectedFiles();
					// System.out.println("PUT SAVEDIR " + dialog.getCurrentDirectory() + " -- " + dialog.getCurrentDirectory().getAbsolutePath());
					userPrefs.put("SAVEDIR", dialog.getCurrentDirectory().getAbsolutePath() + '/');
				} else {
					this.annulation();
					return;
				}
			} else {
				dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (dialog.showDialog(null, "Save in") == JFileChooser.APPROVE_OPTION) {
					fileNames = dialog.getSelectedFiles();
					userPrefs.put("SAVEDIR", new File(fileNames[0].getParent()).getAbsolutePath());
				} else {
					this.annulation();
					return;
				}
			}
		}

		public void annulation() {
			JOptionPane.showMessageDialog(null, "Vous avez annulé l'opération.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public static void main(String[] args) throws URISyntaxException {
		ConversionUI ui = new ConversionUI();
		try {
			ui.run();
		} catch (NullPointerException n) {
		}
	}
}
