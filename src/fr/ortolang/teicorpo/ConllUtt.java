package fr.ortolang.teicorpo;

import java.util.ArrayList;
import java.util.List;

public class ConllUtt {
    List<ConllWord> words;
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
