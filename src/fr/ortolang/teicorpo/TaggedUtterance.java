package fr.ortolang.teicorpo;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TaggedUtterance {
	ArrayList<TaggedWord> twL;
	
	TaggedUtterance() {
		twL = new ArrayList<TaggedWord>();
	}
	
	void reset() {
		twL = new ArrayList<TaggedWord>();
	}
	
	void add(String[] wcl) {
		TaggedWord tw = new TaggedWord(wcl);
		twL.add(tw);
	}
	
	void addPosSNLP(String[] wcl) {
		TaggedWord tw = new TaggedWord();
		tw.posSNLP(wcl);
		twL.add(tw);
	}
	
	void addCONNLSNLP(String[] wcl) {
		//System.out.printf("addCONNLSNLP: %d%s%n", wcl.length, Utils.join2(wcl));
		TaggedWord tw = new TaggedWord();
		tw.conllSNLP(wcl);
		twL.add(tw);
	}

	public Element createSpanW(Document teiDoc) {
		Element span = teiDoc.createElement("span");
		Element ref = teiDoc.createElement("ref");
		if (twL == null) return span;
		for (int i=0; i < twL.size(); i++) {
			TaggedWord c = twL.get(i);
			Element w = teiDoc.createElement("w");
			w.setTextContent(c.word);
			w.setAttribute("pos", c.pos);
			w.setAttribute("lemma", c.lemma);
			ref.appendChild(w);
		}
		span.appendChild(ref);
		return span;
	}

	public String toString() {
		String s ="";
		if (twL == null) return s;
		for (int i=0; i < twL.size(); i++) {
			TaggedWord c = twL.get(i);
			s += c.toString() + "\n";
		}
		return s;
	}

	public Element createSpanConll(Element spG, Document teiDoc) {
		if (twL == null) return null;
		for (int i=0; i < twL.size(); i++) {
			TaggedWord c = twL.get(i);
			Element span = teiDoc.createElement("span");
			span.setTextContent(String.valueOf(i+1));
			Element c1 = teiDoc.createElement("spanGrp");
			Element c1sp = teiDoc.createElement("span");
			c1sp.setTextContent(c.word);
			c1.appendChild(c1sp);
			c1.setAttribute("type", "word");
			span.appendChild(c1);
			Element c2 = teiDoc.createElement("spanGrp");
			Element c2sp = teiDoc.createElement("span");
			c2sp.setTextContent(c.pos);
			c2.appendChild(c2sp);
			c2.setAttribute("type", "pos");
			span.appendChild(c2);
			Element c3 = teiDoc.createElement("spanGrp");
			Element c3sp = teiDoc.createElement("span");
			c3sp.setTextContent(c.lemma);
			c3.appendChild(c3sp);
			c3.setAttribute("type", "lemma");
			span.appendChild(c3);
			spG.appendChild(span);
		}
		return spG;
	}

	public Element createSpanConllU(Element syntaxGrp, Document teiDoc) {
		if (twL == null) return null;
		for (int i=0; i < twL.size(); i++) {
			//System.out.printf("createSpanConllU: %d: %s%n", i, twL.get(i).toString());
			TaggedWord c = twL.get(i);
			Element span = teiDoc.createElement("span");
			span.setTextContent(c.ID);
			Element c1 = teiDoc.createElement("spanGrp");
			Element c1sp = teiDoc.createElement("span");
			c1sp.setTextContent(c.FORM);
			c1.appendChild(c1sp);
			c1.setAttribute("type", "FORM");
			span.appendChild(c1);
			Element c2 = teiDoc.createElement("spanGrp");
			Element c2sp = teiDoc.createElement("span");
			c2sp.setTextContent(c.LEMMA);
			c2.appendChild(c2sp);
			c2.setAttribute("type", "LEMMA");
			span.appendChild(c2);
			Element c3 = teiDoc.createElement("spanGrp");
			Element c3sp = teiDoc.createElement("span");
			c3sp.setTextContent(c.CPOSTAG);
			c3.appendChild(c3sp);
			c3.setAttribute("type", "CPOSTAG");
			span.appendChild(c3);
			Element c4 = teiDoc.createElement("spanGrp");
			Element c4sp = teiDoc.createElement("span");
			c4sp.setTextContent(c.POSTAG);
			c4.appendChild(c4sp);
			c4.setAttribute("type", "POSTAG");
			span.appendChild(c4);
			Element c5 = teiDoc.createElement("spanGrp");
			Element c5sp = teiDoc.createElement("span");
			c5sp.setTextContent(c.FEATS);
			c5.appendChild(c5sp);
			c5.setAttribute("type", "FEATS");
			span.appendChild(c5);
			Element c6 = teiDoc.createElement("spanGrp");
			Element c6sp = teiDoc.createElement("span");
			c6sp.setTextContent(c.HEAD);
			c6.appendChild(c6sp);
			c6.setAttribute("type", "HEAD");
			span.appendChild(c6);
			Element c7 = teiDoc.createElement("spanGrp");
			Element c7sp = teiDoc.createElement("span");
			c7sp.setTextContent(c.DEPREL);
			c7.appendChild(c7sp);
			c7.setAttribute("type", "DEPREL");
			span.appendChild(c7);
			Element c8 = teiDoc.createElement("spanGrp");
			Element c8sp = teiDoc.createElement("span");
			c8sp.setTextContent(c.DEPS);
			c8.appendChild(c8sp);
			c8.setAttribute("type", "DEPS");
			span.appendChild(c8);
			Element c9 = teiDoc.createElement("spanGrp");
			Element c9sp = teiDoc.createElement("span");
			c9sp.setTextContent(c.MISC);
			c9.appendChild(c9sp);
			c9.setAttribute("type", "MISC");
			span.appendChild(c9);
			syntaxGrp.appendChild(span);
		}
		return syntaxGrp;
		/*
		ID = wcl[0];
		FORM = wcl[1];
		LEMMA = wcl[2];
		CPOSTAG = wcl[3];
		POSTAG = wcl[4];
		FEATS = wcl[5];
		HEAD = wcl[6];
		DEPREL = wcl[7];
		DEPS = wcl[8];
		MISC = wcl[9];
		*/
	}

	public Element createUWords(Element spG, Document teiDoc) {
		if (twL == null) return null;
		// chercher l'élément <u>
		NodeList nl = spG.getElementsByTagName("u");
		if (nl.getLength() != 1) {
			System.err.println("no unique <u> in " + spG.toString());
			return null;
		}
		Element u = (Element)nl.item(0);
		// look for <seg> element
		NodeList nlseg = u.getElementsByTagName("seg");
		if (nlseg.getLength() != 1) {
			/*
			System.err.println("no unique <seg> in " + u.toString());
			return null;
			*/
			// in this case we fill the <u> directly
			u.setTextContent("");
		} else {
			nl.item(0).removeChild(nlseg.item(0));
		}
		for (int i=0; i < twL.size(); i++) {
			TaggedWord c = twL.get(i);
			Element w = teiDoc.createElement("w");
			w.setTextContent(c.word);
			w.setAttribute("pos", c.pos);
			w.setAttribute("lemma", c.lemma);
			u.appendChild(w);
		}
		return spG;
	}

}
