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

public class ImportConllToTei extends ImportToTei {

	// content of the conll file
    private ConllDoc clDoc;

	/**
	 * Construction de l'objet conllFile à partir du fichier CONLL.
	 * 
	 * @param conllFileName
	 * @throws Exception
	 */
	// initialise et construit le conllFile et le docTEI
	public void transform(String conllFileName, TierParams tp) throws Exception {
		// System.err.printf("ConllToTei %s -- %s %n", conllFileName, tp);
		if (tp == null) tp = new TierParams();
		optionsTEI = (tp != null) ? tp : new TierParams();
		if (optionsTEI.metadata != null) System.out.printf("metadata: %s%n", optionsTEI.metadata);
		if (optionsTEI.inputFormat.isEmpty()) optionsTEI.inputFormat = ".orfeo";
        clDoc = new ConllDoc();
		clDoc.load(conllFileName, optionsTEI);
		whenId = 0;
		maxTime = 0.0;
		times = new ArrayList<String>();
		timeElements = new ArrayList<Element>();

		TeiDocument xmlDoc;

		if (optionsTEI.metadata != null && !optionsTEI.metadata.isEmpty()) {
			xmlDoc = new TeiDocument(optionsTEI.metadata, false);
			// change elements in listPerson
			NodeList l = xmlDoc.doc.getElementsByTagName("person");
			if (l != null) {
				for (int i = 0; i < l.getLength(); i++) {
					Element e = (Element)l.item(i);
					String personName = e.getAttribute("xml:id").trim();
					String age = TeiDocument.childNodeContent(e, "age").trim();
					String vage = "40.3";
					if (age.equals("inconnu")) vage = "40";
					else if (age.equals("21-60")) vage = "40";
					else if (age.equals("60+")) vage = "65";
					else vage = age;
					e.setAttribute("age", vage);
					Element ag = TeiDocument.setElement(xmlDoc.doc, e, "altGrp", "");
					Element a = TeiDocument.setElement(xmlDoc.doc, ag, "alt", "");
					a.setAttribute("type", personName);
				}
			}
		} else {
			xmlDoc = new TeiDocument(false);
		}

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
			this.buildHeader("Fichier TEI obtenu à partir du fichier ORFEO " + fname, tp.writtentext);
			Element l = TeiDocument.childElement(rootTEI, "listPerson");
			for (String t: clDoc.loc) {
				Element e = TeiDocument.createElement(docTEI, l, "person", "");
				e.setAttribute("xml:id", t);
				Element ag = TeiDocument.createElement(docTEI, e, "altGrp", "");
				Element a = TeiDocument.createElement(docTEI, ag, "alt", "");
				a.setAttribute("type", t);
				e.setAttribute("age", "40");
				TeiDocument.createElement(docTEI, e, "age", "40");
			}
		}
		this.buildText(tp);
		addTemplateDesc(docTEI);
/*
# sent_id = cefc-ofrom-unine11b13m-19
# text = mais euh comme mes parents ils habitaient à   ben euh
1	mais	mais	COO	COO	_	7	mark	_	_	40.740002	40.950001	unine11-ufa
2	euh	euh	INT	INT	_	1	dm	_	_	40.959999	41.009998	unine11-ufa
1	voilà	voilà	INT	INT	_	3	dm	_	_	0.920000	1.350000	Sonia_Branca-Rosoff
3	crois	croire	VRB	VRB	_	0	root	_	_	1.520000	1.750000	Sonia_Branca-Rosoff

    ID
    FORM
    LEMMA
    POS
    POS
    FEATS (always void)
    HEAD
    DEPREL
    DEPS (always void)
    MISC (always void)
    BTIMESTAMP: begin timestamp in ms
    ETIMESTAMP: end timestamp in ms
    SPEAKERID: the id of the speaker (xml:id attribute value of the person element in metadata file)
 */
		insertTemplate(docTEI, "conll", LgqType.TIME_DIV, TeiDocument.ANNOTATIONBLOC);
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
			Element utt = docTEI.createElement("annotationBlock");
			div.appendChild(utt);
			utt.setAttribute("id", cn.id);
			Element u = docTEI.createElement("u");
			utt.appendChild(u);
			u.setTextContent(cn.text);
			if (cn.words.size() < 1) continue;
			try {
				utt.setAttribute("who", cn.words.get(0).tiers[12]);
				if (!cn.words.get(0).tiers[10].equals("_") && !cn.words.get(0).tiers[10].isEmpty())
					utt.setAttribute("from", addTimeToTimeline(cn.words.get(0).tiers[10]));
				if (!cn.words.get(0).tiers[11].equals("_") && !cn.words.get(0).tiers[11].isEmpty())
					utt.setAttribute("to", addTimeToTimeline(cn.words.get(cn.words.size()-1).tiers[11]));
				/*
				for (ConllWord w : cn.words) {
					Element word = docTEI.createElement("w");
					u.appendChild(word);
					word.setTextContent(w.toString());
				}
				*/
				Element syntaxGrp = docTEI.createElement("spanGrp");
				utt.appendChild(syntaxGrp);
				syntaxGrp.setAttribute("type", "conll");
				syntaxGrp.setAttribute("inst", "ORFEO");
				if (cn.words != null) {
					TaggedUtterance tu = new TaggedUtterance();
					for (int w=0; w < cn.words.size(); w++) {
						String start = (cn.words.get(0).tiers[10].equals("_") && !cn.words.get(0).tiers[10].isEmpty()) ? "" : addTimeToTimeline(cn.words.get(w).tiers[10]);
						String end = (cn.words.get(0).tiers[11].equals("_") && !cn.words.get(0).tiers[11].isEmpty()) ? "" : addTimeToTimeline(cn.words.get(w).tiers[11]);
						tu.addCONNLSNLP(cn.words.get(w).tiers, start, end);
					}
					tu.createSpanConllU(syntaxGrp, docTEI);
				}

			} catch(Exception e) {
				System.err.printf("CONLL error: %s%n", e.toString());
				e.printStackTrace();
				continue;
			}
        }
	}

	public static void main(String[] args) throws Exception {
		EXT = ".orfeo";
		TierParams.printVersionMessage();
		ImportConllToTei ct = new ImportConllToTei();
		//System.err.printf("EXT(M): %s%n", EXT);
		ct.mainCommand(args, EXT, Utils.EXT, "Description: ImportConllToTei converts a CONLL file to an TEI file%n", 8);
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
		Utils.createFile(docTEI, output);
	}

}
