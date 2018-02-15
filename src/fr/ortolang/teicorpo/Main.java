package fr.ortolang.teicorpo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.FilenameFilter;

import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JOptionPane;


public class Main {

	static FileChooserInterface dlg;
	static int nbConv = 0;

	public static void runConversion(File inputFile, String outputDir, HashSet<String> formats){
		
		String inputName = inputFile.getName();
		String inputAbsPath = inputFile.getAbsolutePath();
		
		try{
			//Création du dossier tei où seront stockés tous les fichiers tei crées
			if(!inputName.endsWith(Utils.EXT)){		
				String teiDirName = outputDir + "/tei/";
				String outputTeiFileName = teiDirName +inputName.split("\\.")[0] + Utils.EXT;
				File teiDir = new File(teiDirName);
				teiDir.mkdir();
				//Création des fichiers TEI
				if(inputName.endsWith(".cha")){
					ClanToTei cf = new ClanToTei(inputAbsPath, null);
					Utils.createFile(outputTeiFileName, cf.docTEI);
					nbConv ++;
					System.out.printf("New TEI file created from %s to " + outputTeiFileName + "\n", inputAbsPath);
				}
				else if(inputName.endsWith(".trs") || inputName.endsWith(".trs.xml")){
					TranscriberToTei tr = new TranscriberToTei(inputFile, false);
					Utils.createFile(outputTeiFileName, tr.docTEI);
					nbConv++;
					System.out.printf("New TEI file created from %s to " + outputTeiFileName + "\n", inputAbsPath);
				}
			}
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null, e.getMessage(), "Erreur fichier " + inputName, JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}

		try{
			for(String f : formats){
				if(f.equals("chat") || f.equals("trs")){
					File teiFile;
					if(inputName.endsWith(Utils.EXT)){
						teiFile = inputFile;
					}
					else{
						teiFile = new File(outputDir + "/tei/" +inputName.split("\\.")[0] + Utils.EXT);
					}
					if(teiFile.exists()){
						if(f.equals("chat") && !inputName.endsWith(".cha")){
							String chatDirName = outputDir + "/chat/";
							String outputName = chatDirName +inputName.split("\\.")[0] + ".cha";
							TeiToClan ttc = new TeiToClan(teiFile.getAbsolutePath(), outputName, null);
							ttc.createOutput();
							nbConv ++;
							System.out.printf("New %s file created from %s to " + outputName + "\n", f.toUpperCase(), inputAbsPath);
						}
						else if(f.equals("trs") && !(inputName.endsWith(".trs") || inputName.endsWith(".trs.xml"))){
							String trsDirName = outputDir + "/trs/";
							String outputName = trsDirName +inputName.split("\\.")[0] + ".trs";
							TeiToTranscriber ttt = new TeiToTranscriber(teiFile.getAbsolutePath(), outputName, null);
							nbConv ++;
							ttt.createOutput();
							System.out.printf("New %s file created from %s to " + outputName + "\n", f.toUpperCase(), inputAbsPath);						
						}
					}
					else{
						JOptionPane.showMessageDialog(null, "Erreur rencontrée sur le fichier " + inputName + 
								", format non pris en charge pour la conversion vers le format "+ f.toUpperCase() + ".",
								"Erreur", JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null, "Erreur rencontrée sur le fichier " + inputName, "Erreur", JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
		}
	}

	public static void main (String[] args) throws Exception{
		dlg = new FileChooserInterface();
		final HandleJCheckBoxEvent cb= new HandleJCheckBoxEvent();
		JButton button = cb.button;

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {	
				cb.setVisible(false);

				if(cb.formats.contains("chat")){
					String chatDirName = dlg.output + "/chat/";
					File chatDir = new File(chatDirName);
					chatDir.mkdir();
				}

				if(cb.formats.contains("trs")){
					String trsDirName = dlg.output + "/trs/";
					File trsDir = new File(trsDirName);
					trsDir.mkdir();
				}
				
				//Conversions
				//Multiple choix possible: dossiers, fichiers, ou les deux
				for (File f : dlg.inputFileNames){
					if (f.isFile()){
						runConversion(f, dlg.output.getAbsolutePath(), cb.formats);
					}
					else{
						File[] files = f.listFiles(new FilenameFilter(){
							public boolean accept(File dir, String name) {
								return name.endsWith(".cha") || name.endsWith(".trs") || name.endsWith(".trs.xml") || name.endsWith(Utils.EXT);
							}
						});
						for (File cFile : files){
							runConversion(cFile, dlg.output.getAbsolutePath(), cb.formats);
						}
					}
				}
				if(nbConv == 0){
					JOptionPane.showMessageDialog(null, "Aucune conversion à effectuer.", "Information", JOptionPane.INFORMATION_MESSAGE);
				}
				else if(nbConv == 1){
					JOptionPane.showMessageDialog(null, "1 conversion a été effectuée.", "Information", JOptionPane.INFORMATION_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(null, nbConv + " conversions ont été effectuées.", "Information", JOptionPane.INFORMATION_MESSAGE);
				}
				System.out.println("Nombre total de conversions: " + nbConv + ".");
				System.exit(1);
			}
		});
	}
}
