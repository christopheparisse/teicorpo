package fr.ortolang.teicorpo;

public class TaggedWord {

	String	word;
	String	pos;
	String	lemma;

	public TaggedWord() {
		word = "";
		pos = "";
		lemma = "";
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

	public String toString() {
		return "tw: " + word + " pos: " + pos + " lm: " + lemma;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
