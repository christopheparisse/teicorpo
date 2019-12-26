/**
 * @author Christophe Parisse
 * TeiSyntacticAnalysis: faire l'analyse syntaxique sur la ligne principale
 */

package fr.ortolang.teicorpo;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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

public class TeiSNLP extends GenericMain {

	final static String SNLP_EXT = "_snlp";
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
	// racine du fichier teiDoc
	Element root;
	
	// resultat
	boolean ok;
	
	// SNLP
	SNLP snlp = new SNLP();	

	public void init(String pInputName, String pOutputName, TierParams optionsTei) {
		optionsOutput = optionsTei;
		inputName = pInputName;
		outputName = pOutputName;
		DocumentBuilderFactory factory = null;
		root = null;
		ok = false;

		File inputFile = new File(inputName);
		if (!inputFile.exists()) {
			System.err.printf("%s does not exist: cannot process%n", inputName);
			return;
		}

		try {
			factory = DocumentBuilderFactory.newInstance();
			TeiDocument.setDTDvalidation(factory, optionsTei.dtdValidation);
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

				public Iterator<String> getPrefixes(String val) {
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

	// Conversion of teicorpo file
	public boolean process() {
		snlp.init(optionsOutput.syntaxformat, optionsOutput.model);
		
        // add informations about document structure
		if (optionsOutput.syntaxformat.equals("conll") || optionsOutput.syntaxformat.equals("dep")) {
			/*
			 * version conll
			 */
			insertTemplate("conll", LgqType.SYMB_DIV, TeiDocument.ANNOTATIONBLOC);
			insertTemplate("FORM", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("LEMMA", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("CPOSTAG", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("POSTAG", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("FEATS", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("HEAD", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("DEPREL", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("DEPS", LgqType.SYMB_ASSOC, "conll");
			insertTemplate("MISC", LgqType.SYMB_ASSOC, "conll");
		} else if (optionsOutput.syntaxformat.equals("ref")) {
			/*
			 * version <ref><ref><w>
			 */
			insertTemplate("ref", LgqType.SYMB_ASSOC, TeiDocument.ANNOTATIONBLOC);
		} else {
			// format words : optionsOutput.syntaxformat === "w"
		}

		int numAU = 0;

		NodeList aBlocks = teiDoc.getElementsByTagName(TeiDocument.ANNOTATIONBLOC);
		if (aBlocks != null && aBlocks.getLength() > 0) {
			for (int i=0; i < aBlocks.getLength(); i++) {
				Element eAU = (Element) aBlocks.item(i);
				AnnotatedUtterance au = new AnnotatedUtterance();
				au.processAnnotatedU(eAU, null, null, optionsOutput, false);
				// mettre en place l'élément qui recevra l'analyse syntaxique.
				Element syntaxGrp = teiDoc.createElement("spanGrp");
				if (optionsOutput.syntaxformat.equals("conll") || optionsOutput.syntaxformat.equals("dep")) {
					syntaxGrp.setAttribute("type", "conll");
					syntaxGrp.setAttribute("inst", "SNLP");
				} else if (optionsOutput.syntaxformat.equals("ref")) {
					syntaxGrp.setAttribute("type", "ref");
					syntaxGrp.setAttribute("inst", "SNLP");
				}
				numAU++;
				syntaxGrp.setAttribute("id", "snlp" + numAU);
				syntaxGrp.setIdAttribute("id", true);
				eAU.appendChild(syntaxGrp);
				// do the syntactic analysis
				// System.err.print(u.toString());
				String utt = "";
				for (int s = 0; s < au.speeches.size(); s++) {
					utt += au.speeches.get(s).getContent(optionsOutput.rawLine);
				}
				String parsedUtt = NormalizeSpeech.parseText(utt, TeiToPartition.getOriginalFormat(teiDoc), optionsOutput);
				if (numAU % 100 == 0) System.out.printf("%d%n", numAU);
				if (optionsOutput.syntaxformat.equals("conll") || optionsOutput.syntaxformat.equals("dep")) {
					List<String[]> lc = snlp.parseCONLL(parsedUtt);
					//System.out.printf("[lc=%d]%n", lc.size());
					/*
					 * version <treetagger-conll>
					 */
					if (lc != null) {
						TaggedUtterance tu = new TaggedUtterance();
						for (int w=0; w < lc.size(); w++) {
							tu.addCONNLSNLP(lc.get(w));
						}
						tu.createSpanConllU(syntaxGrp, teiDoc);					
					}
				} else if (optionsOutput.syntaxformat.equals("ref")) {
					List<String[]> lc = snlp.parseWPLN(parsedUtt);
					/*
					 * version <treetagger-tt><ref><w>
					 */
					if (lc != null) {
						TaggedUtterance tu = new TaggedUtterance();
						for (int w=0; w < lc.size(); w++) {
							tu.addPosSNLP(lc.get(w));
						}
						Element elt = tu.createSpanW(teiDoc);
						syntaxGrp.appendChild(elt);
					}
				} else {
					List<String[]> lc = snlp.parseWPLN(parsedUtt);
					/*
					 * version <w>
					 */
					if (lc != null) {
						TaggedUtterance tu = new TaggedUtterance();
						for (int w=0; w < lc.size(); w++) {
							tu.addPosSNLP(lc.get(w));
						}
						Node parent = syntaxGrp.getParentNode();
						tu.createUWords((Element)parent, teiDoc);
					}
				}
			}
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
			//transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, Utils.teiCorpoDtd());
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
	 * option -c replace --> ne pas utiliser SNLP_EXT et mettre sameFile à vrai.
	 */

	// Programme principal
	public static void main(String args[]) throws IOException {
		TierParams.printVersionMessage();
		String usageString = "Description: TeiSNLP allows to apply the Standford Natural Language Parser on a TEI file.%nUsage: TeiSNLP -c command [-options] <"
				+ Utils.EXT + ">%n";
		TeiSNLP ttt = new TeiSNLP();
		ttt.mainCommand(args, Utils.EXT, SNLP_EXT + Utils.EXT, usageString, 7);
	}
}
