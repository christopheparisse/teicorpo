/**
 * @author Christophe Parisse
 * TeiSyntacticAnalysis: faire l'analyse syntaxique sur la ligne principale
 */

package fr.ortolang.teicorpo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.ortolang.teicorpo.TeiFile.Div;

public class TeiTreeTagger extends GenericMain {

	final static String TT_EXT = "_ttg";
	// Document TEI à lire
	public Document teiDoc;
	// acces Xpath
	public XPathFactory xPathfactory;
	public XPath xpath;
	// paramètres
	public TierParams optionsOutput;
	// Nom du fichier teiml à convertir
	String inputName;
	// Nom du fichier de sortie
	String outputName;
	// Validation du document Tei par la dtd
	boolean validation = false;
	// racine du fichier teiDoc
	Element root;
	
	// resultat
	boolean ok;

	public void init(String pInputName, String pOutputName, TierParams optionsTei) {
		optionsOutput = optionsTei;
		inputName = pInputName;
		outputName = pOutputName;
		DocumentBuilderFactory factory = null;
		root = null;
		ok = false;

		File inputFile = new File(inputName);
		if (!inputFile.exists()) {
			System.err.printf("%s n'existe pas: pas de traitement%n", inputName);
			return;
		}

		try {
			factory = DocumentBuilderFactory.newInstance();
			Utils.setDTDvalidation(factory, validation);
			DocumentBuilder builder = factory.newDocumentBuilder();
			teiDoc = builder.parse(inputFile);
			root = teiDoc.getDocumentElement();
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
		} catch (Exception e) {
			e.printStackTrace();
		}
		process();
		createOutput();
	}
	
	public String getTreeTaggerLocation() {
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("mac") >= 0) {
			String p = ExternalCommand.getLocation("tree-tagger-macos","TREE_TAGGER");
			if (p != null) return p;
			p = ExternalCommand.getLocation("bin/tree-tagger-macos","TREE_TAGGER");
			if (p != null) return p;
			System.err.println("Cannot find tree-tagger-macos program");
			return null;
		} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
			String p = ExternalCommand.getLocation("tree-tagger","TREE_TAGGER");
			if (p != null) return p;
			p = ExternalCommand.getLocation("bin/tree-tagger","TREE_TAGGER");
			if (p != null) return p;
			System.err.println("Cannot find tree-tagger program");
			return null;
		} else {
			String p = ExternalCommand.getLocation("tree-tagger.exe","TREE_TAGGER");
			if (p != null) return p;
			p = ExternalCommand.getLocation("bin/tree-tagger.exe","TREE_TAGGER");
			if (p != null) return p;
			System.err.println("Cannot find tree-tagger.exe program");
			return null;
		}
	}

	public String getTreeTaggerModel() {
		String model = (!optionsOutput.model.isEmpty()) ? optionsOutput.model : "spoken-french.par";
		String p = ExternalCommand.getLocation(model,"TREE_TAGGER");
		if (p != null) return p;
		p = ExternalCommand.getLocation("model" + model,"TREE_TAGGER");
		if (p != null) return p;
		System.err.println("Cannot find spoken-french.par model");
		return null;
	}

	// Conversion du fichier teiml
	public boolean process() {
		Tokenizer.init("fr", null);
		int numAU = 0;
		for (String cmd: optionsOutput.commands) {
			if (cmd.equals("replace"))
				continue;
			if (cmd.startsWith("model=")) {
				String modelfile = cmd.substring(6);
				System.out.printf("Utilisation du modèle : %s%n", modelfile);
			}
		}
		PrintWriter out = null;
		/*
		 * CREATION OF A VRT FILE (one line per word)
		 */
		String outputNameTemp = outputName + "_tmp.vrt";
		try {
			FileOutputStream of = new FileOutputStream(outputNameTemp);
			OutputStreamWriter outWriter = new OutputStreamWriter(of, "UTF-8");
			out = new PrintWriter(outWriter, true);
		} catch (Exception e) {
			return false;
		}
		NodeList aBlocks = teiDoc.getElementsByTagName(Utils.ANNOTATIONBLOC);
		if (aBlocks != null && aBlocks.getLength() > 0) {
			for (int i=0; i < aBlocks.getLength(); i++) {
				Element eAU = (Element) aBlocks.item(i);
				AnnotatedUtterance au = new AnnotatedUtterance();
				au.process(eAU, null, null, optionsOutput, false);
				// mettre en place l'élément qui recevra l'analyse syntaxique.
				Element syntaxGrp = teiDoc.createElement("spanGrp");
				if (optionsOutput.syntaxformat.equals("conll")) {
					syntaxGrp.setAttribute("type", "conll");
				} else if (optionsOutput.syntaxformat.equals("ref")) {
					syntaxGrp.setAttribute("type", "treetagger");
				}
				numAU++;
				syntaxGrp.setAttribute("id", "tt" + numAU);
				syntaxGrp.setIdAttribute("id", true);
				eAU.appendChild(syntaxGrp);
				// préparer le fichier d'analyse syntaxique
				out.printf("<%s>%n", "tt" + numAU);
				// System.err.print(u.toString());
				String utt = "";
				for (int s = 0; s < au.speeches.size(); s++) {
					utt += au.speeches.get(s).getContent(optionsOutput.rawLine);
				}
				String parsedUtt = NormalizeSpeech.parseText(utt, TeiToPartition.getOriginalFormat(teiDoc), optionsOutput);
				// decouper au.nomarkerSpeech
				ArrayList<String> p = Tokenizer.splitTextTT(parsedUtt);
				for (int ti = 0; ti < p.size(); ti++)
					out.printf("%s%n", p.get(ti));
			}
		}
		out.close();
		/*
		 * TRAITEMENT AVEC TREETAGGER : démarrer l'analyse
		 */
		String outputNameResults = outputName + "_tmp.conll";
        String[] commande = { getTreeTaggerLocation(), "-token",
        		"-lemma", "-sgml", getTreeTaggerModel(),
        		outputNameTemp };
        if (commande[0] == null || commande[4] == null) {
        	// cannot parse
        	System.err.println("tree-tagger files not found: stop.");
        	return false;
        } else {
        	System.out.printf("using model %s.%n", commande[4]);
        	System.out.printf("treetagger located at %s.%n", commande[0]);
        }
        ExternalCommand.command(commande, outputNameResults, optionsOutput.verbose);
		/*
		 * Le fichier résultat de TREETAGGER existe
		 * récupérer les résultats
		 */
        // ajouter les informations de structure du document
		if (optionsOutput.syntaxformat.equals("conll")) {
			/*
			 * version conll
			 */
			insertTemplate("conll", LgqType.SYMB_DIV, Utils.ANNOTATIONBLOC);
			insertTemplate("word", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("pos", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("lemma", LgqType.SYMB_ASSOC, "conll");
		} else if (optionsOutput.syntaxformat.equals("ref")) {
			/*
			 * version <treetagger><ref><w>
			 */
			insertTemplate("treetagger", LgqType.SYMB_ASSOC, Utils.ANNOTATIONBLOC);
		} else {
			// format words : optionsOutput.syntaxformat === "w"
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(outputNameResults), "UTF-8"));
			String line = reader.readLine();
			String lastID = "";
			TaggedUtterance tu = new TaggedUtterance();
			while (line != null) {
				line = line.trim();
				if (line.startsWith("<") && line.endsWith(">")) {
					if (!lastID.isEmpty() && !flush(lastID, tu)) return true;
					lastID = line.substring(1, line.length()-1);
					tu.reset();
				} else {
					String[] wcl = line.split("\t");
					if (wcl.length != 3) {
						System.err.println("not enough elements: " + line);
						return false;
					}
					tu.add(wcl);
				}
				line = reader.readLine();
			}
			if (!lastID.isEmpty())
				flush(lastID, tu); // insère la derniere phrase (depuis le dernier id) dans la structure XML teiDoc
				// voir TaggedUtterance pour le détail de ce format qui sera inséré sous la forme d'un span
			if (reader != null)
				reader.close();
		} catch (FileNotFoundException fnfe) {
			System.err.println("Erreur fichier : " + outputNameResults + " indisponible");
			return false;
		} catch (IOException ioe) {
			System.err.println("Erreur sur fichier : " + outputNameResults);
			ioe.printStackTrace();
			return false;
		}
		return true;
	}

	public void insertTemplate(String code, String type, String parent) {
		Element templateNote = getTemplate(teiDoc);
		if (templateNote == null) {
			System.err.println("serious error: no template");
			return;
		}
		
		Element note = teiDoc.createElement("note");

		Element noteCode = teiDoc.createElement("note");
		noteCode.setAttribute("type", "code");
		noteCode.setTextContent(code);
		note.appendChild(noteCode);

		Element noteType = teiDoc.createElement("note");
		noteType.setAttribute("type", "type");
		noteType.setTextContent(type);
		note.appendChild(noteType);

		Element noteParent = teiDoc.createElement("note");
		noteParent.setAttribute("type", "parent");
		noteParent.setTextContent(parent);
		note.appendChild(noteParent);

		templateNote.appendChild(note);
	}

	public Element getTemplate(Document doc) {
		NodeList nlNotesStmt = doc.getElementsByTagName("notesStmt");
		if (nlNotesStmt.getLength() < 1) return null;
		NodeList notesStmt = nlNotesStmt.item(0).getChildNodes();
		for (int i=0; i < notesStmt.getLength(); i++) {
			Node n = notesStmt.item(i);
			if (n.getNodeName().equals("note")) {
				Element e = (Element)n;
				if (e.getAttribute("type").equals("TEMPLATE_DESC")) {
					return e;
				}
			}
		}
		return null;
	}

	private boolean flush(String lastID, TaggedUtterance tu) {
		Element spG = teiDoc.getElementById(lastID);
		if (spG == null) {
			System.err.println("cannot find element: " + lastID);
			return false;

		}
		spG.removeAttribute("id");
		if (optionsOutput.syntaxformat.equals("conll")) {
			/*
			 * version <treetagger-conll>
			 */
			tu.createSpanConll(spG, teiDoc);
		} else if (optionsOutput.syntaxformat.equals("ref")) {
			/*
			 * version <treetagger-tt><ref><w>
			 */
			Element elt = tu.createSpanW(teiDoc);
			spG.appendChild(elt);
		} else {
			/*
			 * version <w>
			 */
			Node parent = spG.getParentNode();
			tu.createUWords((Element)parent, teiDoc);
		}
		return true;
	}

	// Création du fichier de sortie à partir de teiDoc
	public void createOutput() {
		File itest = new File(inputName);
		File otest = new File(outputName);
		String itname;
		String otname;
		try {
			itname = itest.getCanonicalPath();
			otname = otest.getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		if (itname.equals(otname) && !optionsOutput.commands.contains("replace")) {
			System.err.println("Le fichier sortie est le même que le fichier entrée: utiliser le paramètre -c replace pour remplacer le fichier");
			return;
		}

		Source source = new DOMSource(teiDoc);
		Result resultat = new StreamResult(outputName);

		try {
			TransformerFactory fabrique2 = TransformerFactory.newInstance();
			Transformer transformer = fabrique2.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, Utils.teiCorpoDtd());
			transformer.transform(source, resultat);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (optionsOutput.verbose == true) System.out.println("Result in " + outputName);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		if (options.verbose == true) System.out.println("Reading " + input);
		init(input, output, options);
	}

	/*
	 * remplacement d'un fichier par lui-même
	 * option -c replace --> ne pas utiliser TT_EXT et mettre sameFile à vrai.
	 */

	// Programme principal
	public static void main(String args[]) throws IOException {
		TierParams.printVersionMessage();
		String usageString = "Description: TeiTreeTagger permet d'appliquer le programme TreeTagger sur un fichier Tei.%nUsage: TeiTreeTagger -c command [-options] <"
				+ Utils.EXT + ">%n";
		TeiTreeTagger ttt = new TeiTreeTagger();
		ttt.mainCommand(args, Utils.EXT, TT_EXT + Utils.EXT, usageString, 7);
	}
}
