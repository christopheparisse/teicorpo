package fr.ortolang.teicorpo;

import edu.stanford.nlp.util.ArrayMap;
import edu.stanford.nlp.util.ArraySet;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConllDoc {
    public List<ConllUtt> doc;
    public Set<String> loc;
    ConllDoc() {
        doc = new ArrayList<ConllUtt>();
        loc = new ArraySet<String>();
    }
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
                    loc.add(cu.words.get(0).tiers[cu.words.get(0).tiers.length - 1]);
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
            // System.err.printf("FINAL %s%n", cu.toString());
            if ( cu.words.size() > 0 )
                doc.add(cu);
            if (reader != null) reader.close();
        }
    }
}
