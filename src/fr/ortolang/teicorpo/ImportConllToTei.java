/**
 * ClanToTei permet de convertir des fichiers au format Chat en un format TEI pour la représentation de données orales.
 * @author Myriam Majdoub + Christophe Parisse
 *
 */

package fr.ortolang.teicorpo;

import java.io.*;
import java.util.*;
import java.lang.String;
import javax.xml.*;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.*;

class ConllWord {
    String tiers[];
    ConllWord(String u) {
        // split line
        tiers = u.split("\\t");
    }
}

class ConllUtt {
    List <ConllWord> words;
    String id;
    String text;
    final String uid = "# sent_id";
	final String ut = "# text";
	static final int CONLL_BLANK = 1;
	static final int CONLL_COMMENT = 2;
	static final int CONLL_WORD = 3;
	/** All input will use this encoding */
	static final String inputEncoding = "UTF-8";
    ConllUtt() {
        words = new ArrayList<ConllWord>();
    }
    void fromline(String u) {
        if (u.startsWith("#")) {
            if (u.startsWith(uid)) {
                int equals = u.indexOf('=');
                if (equals < 0) {
                    id = u.substring(uid.length()+1);
                } else {
                    id = u.substring(equals+2);
                }
            } else if (u.startsWith(ut)) {
                int equals = u.indexOf('=');
                if (equals < 0) {
                    text = u.substring(ut.length()+1);
                } else {
                    text = u.substring(equals+2);
                }
            }
            return;
        }
        ConllWord cw = new ConllWord(u);
        words.add(cw);
    }
    static int linetype(String u) {
		if (u.length() < 1) return CONLL_BLANK;
		if (u.startsWith("#")) return CONLL_COMMENT;
		if (u.startsWith(" ")) return CONLL_BLANK;
		if (u.startsWith("\t")) return CONLL_BLANK;
		return CONLL_WORD;
	}
}

class conllDoc {
    public List<ConllUtt> doc;
    void load(String fn, TierParams tp) throws IOException {
		String line = "";
		ConllUtt cu = new ConllUtt();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new InputStreamReader(new FileInputStream(fn), ConllUtt.inputEncoding) );
			while((line = reader.readLine()) != null) {
				// end of an utterance ?
				if ( ConllUtt.linetype(line) == ConllUtt.CONLL_COMMENT ) {
					cu.fromline(line);
				} else if ( ConllUtt.linetype(line) == ConllUtt.CONLL_BLANK && cu.words.size() > 0) {
					doc.add(cu);
					cu = new ConllUtt();
				} else {
					cu.fromline(line);
				}
			}
		}
		catch (FileNotFoundException fnfe) {
			System.err.println("Erreur fichier : " + fn + " indisponible");
			System.exit(1);
			return;
		}
		catch(IOException ioe) {
			System.err.println("Erreur sur fichier : " + fn );
			ioe.printStackTrace();
			System.exit(1);
		}
		finally {
			if ( cu.words.size() > 0 )
				doc.add(cu);
			if (reader != null) reader.close();
		}
    }
}

public class ImportConllToTei extends ImportToTei {

	// content of the conll file
    conllDoc clDoc;

	/**
	 * Construction de l'objet conllFile à partir du fichier CONLL.
	 * 
	 * @param conllFileName
	 * @throws Exception
	 */
	// initialise et construit le conllFile et le docTEI
	public void transform(String conllFileName, TierParams tp) throws Exception {
//		System.err.printf("ClanToTei %s -- %s %n", conllFileName, tp);
		if (tp == null) tp = new TierParams();
		tparams = (tp != null) ? tp : new TierParams();
//		System.err.printf("fmt: %s%n", tparams.inputFormat);
		if (tparams.inputFormat.isEmpty()) tparams.inputFormat = ".orfeo";
        clDoc = new conllDoc();
		clDoc.load(conllFileName, tparams);
		whenId = 0;
		maxTime = 0.0;
		times = new ArrayList<String>();
		timeElements = new ArrayList<Element>();
		DocumentBuilderFactory factory = null;		

		try {
			factory = DocumentBuilderFactory.newInstance();
			Utils.setDTDvalidation(factory, tparams.dtdValidation);
			DocumentBuilder builder = factory.newDocumentBuilder();
			docTEI = builder.newDocument();
			this.xPathfactory = XPathFactory.newInstance();
			this.xpath = xPathfactory.newXPath();
			this.xpath.setNamespaceContext(new NamespaceContext() {
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

				public String getPrefix(String uri) {
					return null;
				}

				@Override
				public Iterator<String> getPrefixes(String arg0) {
					// TODO Auto-generated method stub
					return null;
				}
			});
			rootTEI = docTEI.createElement("TEI");
//			rootTEI.setAttribute("version", Utils.versionTEI);
			this.rootTEI.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
			docTEI.appendChild(rootTEI);
		} catch (Exception e) {
			e.printStackTrace();
		}
		conversion(tp, conllFileName);
		Utils.setTranscriptionDesc(docTEI, "orfeo", "1.0", "CONLL ORFEO format");
	}

	/**
	 * Conversion du fichier CONLL: création d'un TEI vide, puis mise à jour des
	 * éléments <strong>teiHeader</strong> et <strong>text</strong>.
	 * 
	 * @throws IOException
	 * @throws DOMException
	 */
	// public void conversion(String extension) throws DOMException,
	// IOException{
	public void conversion(TierParams tp, String fname) throws DOMException, IOException {
		this.buildEmptyTEI(fname);
		this.buildHeader("Fichier TEI obtenu à partir du fichier ORFEO " + fname);
		this.buildText(tp);
		addTemplateDesc(docTEI);
		/*
# sent_id = cefc-ofrom-unine11b13m-19
# text = mais euh comme mes parents ils habitaient à   ben euh
1	mais	mais	COO	COO	_	7	mark	_	_	40.740002	40.950001	unine11-ufa
2	euh	euh	INT	INT	_	1	dm	_	_	40.959999	41.009998	unine11-ufa
		 */
		insertTemplate(docTEI, "conll", LgqType.SYMB_DIV, Utils.ANNOTATIONBLOC);
		insertTemplate(docTEI, "FORM", LgqType.SYMB_ASSOC, "conll");
		insertTemplate(docTEI, "LEMMA", LgqType.SYMB_ASSOC, "conll");
		insertTemplate(docTEI, "CPOSTAG", LgqType.SYMB_ASSOC, "conll");
		insertTemplate(docTEI, "POSTAG", LgqType.SYMB_ASSOC, "conll");
		insertTemplate(docTEI, "FEATS", LgqType.SYMB_ASSOC, "conll");
		insertTemplate(docTEI, "HEAD", LgqType.SYMB_ASSOC, "conll");
		insertTemplate(docTEI, "DEPREL", LgqType.SYMB_ASSOC, "conll");
		insertTemplate(docTEI, "DEPS", LgqType.SYMB_ASSOC, "conll");
		insertTemplate(docTEI, "MISC", LgqType.SYMB_ASSOC, "conll");
		/*
		three levels in .orfeo file that are not inserted in the spanGrp span
		 */
		addTimeline();
	}

	public void buildText(TierParams tp) {
		Element div = setFirstDiv();
		for (ConllUtt cn : clDoc.doc) {
			Element utt = docTEI.createElement("annotatedBlock");
			div.appendChild(utt);
			utt.setAttribute("id", cn.id);
			utt.setAttribute("text", cn.text);
			Element u = docTEI.createElement("u");
			utt.appendChild(u);
			for (ConllWord w : cn.words) {
				Element word = docTEI.createElement("w");
				u.appendChild(word);
				word.setTextContent(word.toString());
			}
        }
	}

	public static void main(String[] args) throws Exception {
		EXT = ".orfeo";
		TierParams.printVersionMessage();
		ImportConllToTei ct = new ImportConllToTei();
		//System.err.printf("EXT(M): %s%n", EXT);
		ct.mainCommand(args, EXT, Utils.EXT, "Description: TeiImportConll converts a CONLL file to an TEI file%n", 0);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		if (options.verbose) System.out.println("CONLL: source: " + input);
		if (options.verbose && options.metadata != null) System.out.println("Metadata: source: " + options.metadata);
		if (options.verbose) System.out.println("TEI: target: " + output);
		try {
			transform(input, options);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Utils.setDocumentName(docTEI, options.test ? "testfile" : Utils.lastname(output));
		Utils.createFile(output, docTEI);
	}

}
