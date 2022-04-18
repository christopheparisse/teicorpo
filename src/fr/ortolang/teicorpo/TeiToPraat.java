package fr.ortolang.teicorpo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TeiToPraat extends GenericMain {

	TeiToPartition ttp = null;

	// Permet d'écrire le fichier de sortie
	private PrintWriter out;
	// Encodage du fichier de sortie
	final static String outputEncoding = "UTF-8";
	// Extension du fichier de sortie: .textgrid
	final static String EXT = ".textgrid";

	// Nom du fichier teiml à convertir
	String inputName;
	// Nom du fichier de sortie
	String outputName;

	// Document TEI to be read
	TeiDocument teidoc;

	// Validation du document Tei par la dtd
	boolean validation = false;

	// Constructeur à partir du nom du fichier TEI et du nom du fichier de
	// sortie au format Elan
	public boolean transform(String inputName, String outputName, TierParams optionsTei) throws FileNotFoundException {
		if (optionsTei == null) optionsTei = new TierParams();
		ttp = new TeiToPartition();
		teidoc = new TeiDocument(inputName, optionsTei.dtdValidation);
		ttp.init(teidoc.path, teidoc.doc, optionsTei);
		this.inputName = inputName;
		this.outputName = outputName;
		outputWriter();
		conversion();
		return true;
	}

	// Ecriture du fichier de sortie
	public void outputWriter() {
		out = Utils.openOutputStream(outputName, false, outputEncoding);
	}

	// Conversion du fichier TEI vers Elan
	public void conversion() {
		// Construction de l'en-tête
		buildHeader();
		// Construction des tiers
		buildTiers();
	}

	// Information contenues dans l'élément annotation_doc et dans l'élément
	// header
	public void buildHeader() {
		out.println("File type = \"ooTextFile\"");
		out.println("Object class = \"TextGrid\"");
		out.println("");
		out.println("xmin = 0");
		out.printf("xmax = %s%n", printDouble(ttp.timeline.xmaxTime));
		out.println("tiers? <exists>");
		out.printf("size = %d%n", ttp.tiers.size());
	}

	private String printDouble(double value) {
		if (value <= 0.0)
			return "0";
		double intpart = Math.floor(value);
		BigDecimal bd = new BigDecimal(value);
		BigDecimal bdintpart = new BigDecimal(intpart);
		bd = bd.setScale(15, RoundingMode.HALF_UP);
		String decpart = (bd.subtract(bdintpart)).toString().substring(2);
		if (decpart.equals("-15"))
			decpart = "000000000000000";
		return bdintpart.toString() + "." + decpart;
	}

	// Tiers...
	void buildTiers() {
		TransInfo transInfo = new TransInfo((Element) teidoc.root.getElementsByTagName("teiHeader").item(0));
		int nth = 1;
		out.println("item []:");
		for (Map.Entry<String, ArrayList<Annot>> entry : ttp.tiers.entrySet()) {
			out.printf("    item [%d]:%n", nth++);
			out.println("        class = \"IntervalTier\"");
			String sp = (ttp.optionsOutput.spknamerole.equals("pers")) ? transInfo.getParticipantName(entry.getKey()) : entry.getKey();
			out.printf("        name = \"%s\"%n", sp);
			out.println("        xmin = 0");

			double kmax = 0.0;
			for (Annot a : entry.getValue()) {
				try {
					if (a.timereftype.equals("time")) {
						if (a.start == null || a.start.isEmpty()) continue;
						double start = Double.parseDouble(a.start);
						if (start > kmax)
							kmax = start;
						if (a.end == null || a.end.isEmpty()) continue;
						double end = Double.parseDouble(a.end);
						if (end > kmax)
							kmax = end;
					}
				} catch(java.lang.NumberFormatException e) {
					System.err.println("Warning: Praat file might be corrupted (1)");
					System.err.println(a.toString());
					e.printStackTrace();
					continue;
				}
			}

			out.printf("        xmax = %s%n", printDouble(kmax));
			out.printf("        intervals: size = %d%n", entry.getValue().size());

			int nk = 1;
			String origformat = ttp.originalFormat();
			for (Annot a : entry.getValue()) {
				try {
					if (a.start == null || a.start.isEmpty()) {
						System.err.println("Warning: Praat file with empty value");
						System.err.println(a.toString());
						continue;
					}
					double start = Double.parseDouble(a.start);
					if (a.end == null || a.end.isEmpty()) {
						System.err.println("Warning: Praat file with empty value");
						System.err.println(a.toString());
						continue;
					}
					double end = Double.parseDouble(a.end);
					/*
					if (nk==1 && start != 0.0) {
						// insert first an empty bloc
						out.printf("        intervals [%d]:%n", nk);
						out.printf("            xmin = %s%n", printDouble(0.0));
						out.printf("            xmax = %s%n", printDouble(start));
						out.printf("            text = \"\"%n");
						nk++;
					}
					*/
					out.printf("        intervals [%d]:%n", nk);
					out.printf("            xmin = %s%n", printDouble(start));
					out.printf("            xmax = %s%n", printDouble(end));
					String str = a.getContent(ttp.optionsOutput.rawLine);
					// is it a top tier ?
					String strNorm = (a.topParent == "-") ? NormalizeSpeech.parseText(str, origformat, ttp.optionsOutput) : str;
					out.printf("            text = \"%s\"%n", strNorm);
					nk++;
				} catch(java.lang.NumberFormatException e) {
					System.err.println("Warning: Praat file might be corrupted (2)");
					System.err.println(a.toString());
					e.printStackTrace();
					continue;
				}
			}
		}
	}

	public void createOutput() {
		out.close();
	}

	public static void main(String args[]) throws IOException {
		TierParams.printVersionMessage(false);
		String usage = "Description: TeiToPraat converts a TEI file to a PRAAT file%nUsage: TeiToPraat [-options] <file"
				+ Utils.EXT + ">%n";
		TeiToPraat tte = new TeiToPraat();
		tte.mainCommand(args, Utils.EXT, Utils.EXT_PUBLISH + EXT, usage, 0);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) throws FileNotFoundException {
		options.target = ".texgrid";
		if (!transform(input, output, options)) return;
		createOutput();
	}
}
