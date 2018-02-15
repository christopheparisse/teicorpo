package fr.ortolang.teicorpo;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

	public Element createSpanW(Document teiDoc) {
		Element span = teiDoc.createElement("span");
		Element ref = teiDoc.createElement("ref");
		if (twL == null) return span;
		for (int i=0; i < twL.size(); i++) {
			TaggedWord c = twL.get(i);
			Element w = teiDoc.createElement("w");
			w.setTextContent(c.word);
			w.setAttribute("ana", c.pos);
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

}
