package fr.ortolang.teicorpo;

import org.w3c.dom.Node;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.List;
import java.io.File;

public class TeiInsertCsv {
    List<List<String>> csv;
    TeiInsertCsv() {
        csv = null;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.printf("Usage: TeiInsertCsv <csvfile> [list if TEI xpath names for each column of the csv...]");
            return;
        }

        TeiInsertCsv tic = new TeiInsertCsv();
        tic.csv = CsvUtils.load(args[0]);

        if (tic.csv == null || tic.csv.size() < 1) {
            System.err.println("File not found or only one line. Stop.");
            return;
        }

        System.out.printf("Column: 1 (%s) := file names", tic.csv.get(0).get(0));
        for (int c = 1; c < tic.csv.get(0).size(); c++) {
            System.out.printf("Column: %d (%s) := %s", c+1, tic.csv.get(0).get(c), tic.headColumn(c, args));
        }

        for (int l = 1; l < tic.csv.size(); l++) {
            tic.processLine(l, args);
        }

    }

    private String headColumn(int c, String[] args) {
        String head;
        if (args.length-2 >= c) {
            head = args[c+2];
        } else {
            head = csv.get(0).get(c);
        }
        return head;
    }

    public void processLine(int l, String[] args) {
        String fn = csv.get(l).get(0);
        System.out.printf("File %s%n", fn);
        File f = new File(fn);
        TeiFile teiFile = new TeiFile();
        teiFile.loadXml(f, false);

        for (int c = 1; c < csv.get(0).size(); c++) {
            String head = headColumn(c, args);
            // find pth in metadata
            if (!head.startsWith("/")) head = "//" + head;
            Node node;
            try {
                node = (Node)teiFile.xpath.evaluate(head, teiFile.root, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                // not found or error ?
                System.err.printf("Error finding entry: %s %s%n", head, e.toString());
                //e.printStackTrace();
                continue;
            }
            if (node != null) {
                node.setTextContent(csv.get(l).get(c));
            } else {
                System.err.printf("Cannot find entry: %s%n", head);
            }
        }
        // save tei file
        fn += ".modif.xml";
        Utils.createFile(teiFile.teiDoc, fn);
    }
}
