package fr.ortolang.teicorpo;

import java.io.File;
import java.io.IOException;

public class ElanToTei extends GenericMain {

	/** Encodage des fichiers de sortie et d'entrée. */
	static final String outputEncoding = "UTF-8";
	/** Extension Elan **/
	static String EXT = ".eaf";

	ElanToHT ElanToHT;
	HT_ToTei ht;

	public void transform(String inputFile, String outputName, TierParams options) throws IOException {
		ElanToHT = new ElanToHT(new File(inputFile));
		ht = new HT_ToTei(ElanToHT.ht, options);
		Utils.setTranscriptionDesc(ht.docTEI, "elan", "0.1", "no information on format");

		Utils.setDocumentName(ht.docTEI, Utils.lastname(outputName));
		Utils.createFile(outputName, ht.docTEI);
	}

	/**
	 * Programme principal: convertit un fichier au format Elan en un fichier au
	 * format TEI.
	 * 
	 * @param args
	 *            Liste des aruments du programme.
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		TierParams.printVersionMessage();
		ElanToTei tr = new ElanToTei();
		tr.mainCommand(args, EXT, Utils.EXT, "Description: ElanToTei convertit un fichier au format Elan en un fichier au format TEI", 0);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		try {
			transform(input, output, options);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}