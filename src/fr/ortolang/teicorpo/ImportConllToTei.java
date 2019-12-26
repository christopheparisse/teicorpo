/**
 * ClanToTei permet de convertir des fichiers au format Chat en un format TEI pour la représentation de données orales.
 * @author Myriam Majdoub + Christophe Parisse
 *
 */

package fr.ortolang.teicorpo;

import java.io.*;
import java.util.*;
import java.lang.String;

import org.w3c.dom.*;

class ConllWord {
    String tiers[];
    ConllWord(String u) {
        // split line
        tiers = u.split("\\t");
    }
    public String toString() {
    	String s = "Length: " + tiers.length + " ";
    	for (int i=0; i < tiers.length - 1; i++) {
    		s += tiers[i] + "!-!";
		}
    	s += tiers[tiers.length-1];
    	return s;
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
                    id = u.substring(uid.length()).trim();
                } else {
                    id = u.substring(equals+1).trim();
                }
            } else if (u.startsWith(ut)) {
                int equals = u.indexOf('=');
                if (equals < 0) {
                    text = u.substring(ut.length()).trim();
                } else {
                    text = u.substring(equals+1).trim();
                }
            } else {
            	System.err.println("ignored comment: " + u);
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
	public String toString() {
    	String s = "ID: " + id + " (" + text + ")\n";
    	for (int i=0; i < words.size(); i++) {
    		s += words.get(i).toString() + "\n";
		}
    	return s;
	}
}

class conllDoc {
    public List<ConllUtt> doc;
    conllDoc() { doc = new ArrayList<ConllUtt>(); }
    void load(String fn, TierParams tp) throws IOException {
		String line = "";
		ConllUtt cu = new ConllUtt();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new InputStreamReader(new FileInputStream(fn), ConllUtt.inputEncoding) );
			while((line = reader.readLine()) != null) {
				// System.err.printf("On going %s%n%s%n", line, cu.toString());
				// end of an utterance ?
				if ( ConllUtt.linetype(line) == ConllUtt.CONLL_COMMENT ) {
					// System.err.println("add line comment");
					cu.fromline(line);
				} else if ( ConllUtt.linetype(line) == ConllUtt.CONLL_BLANK && cu.words.size() > 0) {
					// System.err.println("add connl");
					doc.add(cu);
					cu = new ConllUtt();
				} else {
					// System.err.println("add line");
					cu.fromline(line);
				}
			}
			// System.err.println("fin du while");
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
			System.err.printf("FINAL %s%n", cu.toString());
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
		System.err.printf("ConllToTei %s -- %s %n", conllFileName, tp);
		if (tp == null) tp = new TierParams();
		tparams = (tp != null) ? tp : new TierParams();
		System.err.printf("metadata: %s%n", tparams.metadata);
		if (tparams.inputFormat.isEmpty()) tparams.inputFormat = ".orfeo";
        clDoc = new conllDoc();
		clDoc.load(conllFileName, tparams);
		whenId = 0;
		maxTime = 0.0;
		times = new ArrayList<String>();
		timeElements = new ArrayList<Element>();

		TeiDocument xmlDoc;

		if (tparams.metadata != null && !tparams.metadata.isEmpty())
			xmlDoc = new TeiDocument(tparams.metadata, false);
		else
			xmlDoc = new TeiDocument(false);

		xmlDoc.addNamespace();

		docTEI = xmlDoc.doc;
		rootTEI = xmlDoc.root;

		conversion(tp, conllFileName);
		TeiDocument.setTranscriptionDesc(docTEI, "orfeo", "1.0", "CONLL ORFEO format");
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
		this.buildTEI(fname);
		if (tp.metadata == null || tp.metadata.isEmpty()) {
			this.buildHeader("Fichier TEI obtenu à partir du fichier ORFEO " + fname);
		}
		this.buildText(tp);
		addTemplateDesc(docTEI);
		/*
# sent_id = cefc-ofrom-unine11b13m-19
# text = mais euh comme mes parents ils habitaient à   ben euh
1	mais	mais	COO	COO	_	7	mark	_	_	40.740002	40.950001	unine11-ufa
2	euh	euh	INT	INT	_	1	dm	_	_	40.959999	41.009998	unine11-ufa
		 */
		insertTemplate(docTEI, "conll", LgqType.SYMB_DIV, TeiDocument.ANNOTATIONBLOC);
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
				word.setTextContent(w.toString());
			}
        }
	}

	public static void main(String[] args) throws Exception {
		EXT = ".orfeo";
		TierParams.printVersionMessage();
		ImportConllToTei ct = new ImportConllToTei();
		//System.err.printf("EXT(M): %s%n", EXT);
		ct.mainCommand(args, EXT, Utils.EXT, "Description: TeiImportConll converts a CONLL file to an TEI file%n", 9);
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
		TeiDocument.setDocumentName(docTEI, options.test ? "testfile" : Utils.lastname(output));
		TeiDocument.createFile(output, docTEI);
	}

}
