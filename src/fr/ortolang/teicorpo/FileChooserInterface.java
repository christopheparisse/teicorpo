package fr.ortolang.teicorpo;

import java.awt.Color;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileChooserInterface {

	File [] inputFileNames;
	File output;
	String args;


	public FileChooserInterface(){
		JFileChooser dialog = new JFileChooser("/");
		dialog.setForeground(Color.red);
		dialog.setDialogTitle("Sélectionnez le(s) fichier(s) et/ou repertoire(s) où se trouvent les fichiers à convertir");
		dialog.setMultiSelectionEnabled(true);
		dialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		if (dialog.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
			this.inputFileNames = dialog.getSelectedFiles();
		}
		else{
			this.annulation();
			System.exit(1);
		}
		
		dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		ExtensionFilter fileFilter2 = new ExtensionFilter("All",new String[] {});
		dialog.setFileFilter(fileFilter2);
		dialog.setDialogTitle("Choisissez le dossier où seront stockés les résultats");
		if(inputFileNames[0].isFile()){
			dialog.setCurrentDirectory(new File(inputFileNames[0].getParent() + "/.."));
		}
		else{
			dialog.setSelectedFile(inputFileNames[0].getParentFile());
		}
		if (dialog.showDialog(null, "Save in")==JFileChooser.APPROVE_OPTION){
			output = dialog.getSelectedFile();
		}
		else{
			this.annulation();
			System.exit(1);
		}
	}

	public void annulation(){
		JOptionPane.showMessageDialog(null, "Vous avez annulé l'opération", "Information", JOptionPane.INFORMATION_MESSAGE);
	}
}


