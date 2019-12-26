package fr.ortolang.teicorpo;

public class TaggedWord {

	String word;
	String pos;
	String lemma;
	String ner;
	String ID;
	String FORM;
	String LEMMA;
	String CPOSTAG;
	String POSTAG;
	String FEATS;
	String HEAD;
	String DEPREL;
	String DEPS;
	String MISC;
	String start;
	String end;

	public TaggedWord() {
		word = "";
		pos = "";
		lemma = "";
		ner = "";
		start = "";
		end = "";
	}
	
	public TaggedWord(String[] wcl) {
		if (wcl == null || wcl.length != 3) {
			System.err.println("incorrect tagged word creation");
			return;
		}
		word = wcl[0];
		pos = wcl[1];
		lemma = wcl[2];
	}

	public String toString0() {
		return "tw: " + word + " pos: " + pos + " lm: " + lemma;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void posSNLP(String[] wcl) {
		if (wcl == null || wcl.length < 4) {
			System.err.println("incorrect tagged word creation");
			return;
		}
		word = wcl[0];
		pos = wcl[1];
		lemma = wcl[2];
		ner = wcl[3];
	}

	public void conllSNLP(String[] wcl) {
		if (wcl == null || wcl.length < 9) {
			System.err.printf("incorrect CONLLU creation [%s]%d", wcl==null ? "NULL" : wcl.length + " " + wcl.toString());
			return;
		}
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
	}

	public String toString() {
		return "tw: " + ID + " form: " + FORM + " lemma: " + LEMMA
				+ " cpostag: " + CPOSTAG + " postag: " + POSTAG + " feats: " + FEATS 
				+ " head: " + HEAD + " deprel: " + DEPREL + " deps: " + DEPS + " misc: " + MISC;
	}
}
