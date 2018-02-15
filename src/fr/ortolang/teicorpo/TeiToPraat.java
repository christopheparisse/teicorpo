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

	// Document TEI à lire
	public Document teiDoc;
	// acces Xpath
	public XPathFactory xPathfactory;
	public XPath xpath;

	// Validation du document Tei par la dtd
	boolean validation = false;

	// Constructeur à partir du nom du fichier TEI et du nom du fichier de
	// sortie au format Elan
	public boolean transform(String inputName, String outputName, TierParams optionsTei) throws FileNotFoundException {
		if (optionsTei == null) optionsTei = new TierParams();
		ttp = new TeiToPartition();
		DocumentBuilderFactory factory = null;
		try {
			File teiFile = new File(inputName);
			factory = DocumentBuilderFactory.newInstance();
			Utils.setDTDvalidation(factory, validation);
			try {
				DocumentBuilder builder = factory.newDocumentBuilder();
				teiDoc = builder.parse(teiFile);
			} catch(FileNotFoundException e) {
				System.err.println("Le fichier " + inputName + " n'existe pas.");
				return false;
			} catch(Exception e) {
				System.err.println("Impossible de traiter le fichier: " + inputName);
				System.err.println("Erreur logicielle " + e.toString());
				e.printStackTrace();
				return false;
			}
			xPathfactory = XPathFactory.newInstance();
			xpath = xPathfactory.newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
				public String getNamespaceURI(String prefix) {
					System.out.println("prefix called " + prefix);
					if (prefix == null) {
						throw new IllegalArgumentException("No prefix provided!");
					} else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
						System.out.println("default prefix called");
						return "http://www.tei-c.org/ns/1.0";
					} else if (prefix.equals("tei")) {
						System.out.println("tei prefix called");
						return "http://www.tei-c.org/ns/1.0";
					} else if (prefix.equals("xsi")) {
						return "http://www.w3.org/2001/XMLSchema-instance";
					} else {
						return XMLConstants.NULL_NS_URI;
					}
				}

				public Iterator<?> getPrefixes(String val) {
					return null;
				}

				public String getPrefix(String uri) {
					return null;
				}
			});
			ttp.init(xpath, teiDoc, optionsTei);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		this.inputName = inputName;
		this.outputName = outputName;
		outputWriter();
		conversion();
		return true;
	}

	// Ecriture du fichier de sortie
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
		int nth = 1;
		out.println("item []:");
		for (Map.Entry<String, ArrayList<Annot>> entry : ttp.tiers.entrySet()) {
			out.printf("    item [%d]:%n", nth++);
			out.println("        class = \"IntervalTier\"");
			out.printf("        name = \"%s\"%n", entry.getKey());
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
					System.err.println("Attention: fichier praat probablement corrompu");
					continue;
				}
			}

			out.printf("        xmax = %s%n", printDouble(kmax));
			out.printf("        intervals: size = %d%n", entry.getValue().size());

			int nk = 1;
			for (Annot a : entry.getValue()) {
				try {
					double start = Double.parseDouble(a.start);
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
					String strNorm = NormalizeSpeech.parseText(str, ttp.originalFormat(), ttp.optionsOutput);
					out.printf("            text = \"%s\"%n", strNorm);
					nk++;
				} catch(java.lang.NumberFormatException e) {
					System.err.println("Attention: fichier praat probablement corrompu");
					continue;
				}
			}
		}
	}

	public void createOutput() {
		out.close();
	}

	public static void main(String args[]) throws IOException {
		TierParams.printVersionMessage();
		String usage = "Description: TeiToPraat convertit un fichier au format Tei en un fichier au format Praat%nUsage: TeiToPraat [-options] <file"
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
