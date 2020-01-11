package fr.ortolang.teicorpo;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class XpathAddress {
    String path;
    String pathnode;
    String namenode;
    String attr;
    String type;
    String format;
    String fullform;

    String addref(String val, boolean absolute) {
        if (val.startsWith("/") || val.startsWith(".")) return val;
        return (absolute == true) ? "//" + val : ".//" + val ;
    }

    XpathAddress(String info, boolean absolute) {
        // three cases.
        // xpath ends with a @name --> value is put in the attribute name ... <xxx name='value'> ...
        // xpath ends with a [@name=type] --> <node name='type'>value</node>
        // xpath ends in a simple word --> value is put in the node text content ... <name>value</name>

        fullform = info;
        Pattern pattern = Pattern.compile("(.*)[/](.*?)[/]?\\[@(.*?)=\\'(.*)\\'\\]");
        Matcher matcher = pattern.matcher(info);
        //System.out.println(line);
        if (matcher.matches()) { // option with [@name=type]
            // create an new entry
            path = addref(info, absolute);
            pathnode = addref(matcher.group(1), absolute);
            namenode = matcher.group(2);
            attr = matcher.group(3);
            type = matcher.group(4);
            format = "nodeinfo";
            return;
        }
        pattern = Pattern.compile("(.*?)[/]?@(.*)");
        matcher = pattern.matcher(info);
        //System.out.println(line);
        if (matcher.matches()) { // option with @name
            // create an new entry
            path = addref(matcher.group(1), absolute);
            attr = matcher.group(2);
            format = "attr";
            type = "";
            pathnode = "";
            namenode = "";
            return;
        }
        path = addref(info, absolute);
        format = "node";
        attr = "";
        type = "";
        pathnode = "";
        namenode = "";
    }

    public String toString() {
//        System.err.println("XpathAddress: path=" + path + " pathnode=" + pathnode  + " namenode=" + namenode + " attr=" + attr + " type=" + type + " format=" + format);
//            return "NODEINFO: XA(" + fullform + ")= path:" + path + " pathnode:" + pathnode  + " namenode:" + namenode + " attr:" + attr + " type:" + type;
//            return "ATTR XA(" + fullform + ")= path:" + path + " attr:" + attr;
//            return "NODE XA(" + fullform + ")= path:" + path;

        if (format.equals("nodeinfo"))
            return "NODEINFO: pathnode:" + pathnode  + " namenode:" + namenode + " attr:" + attr + " type:" + type;
        else if (format.equals("attr"))
            return "ATTR path:" + path + " attr:" + attr;
        else
            return "NODE path:" + path;
    }
}

public class TeiInsertCsv {
    private List<String[]> csv;
    TeiInsertCsv() {
        csv = null;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.printf("Usage: TeiInsertCsv <csvfile> [list if TEI xpath names for each column of the csv...]");
            return;
        }

        TeiInsertCsv tic = new TeiInsertCsv();
        tic.csv = CsvReader.load(args[0]);

        if (tic.csv == null || tic.csv.size() < 1) {
            System.err.printf("File %s not found or only one line. Stop.", args[0]);
            return;
        }

        List<XpathAddress> lxa = new ArrayList<XpathAddress>();
        System.out.printf("Column: 1 (%s) := file names%n", tic.csv.get(1)[0]);
        for (int c = 1; c < tic.csv.get(0).length; c++) {
            String h = tic.headColumn(c, args);  // extracted from the second lines or from the arguments
            System.out.printf("Column: %d (%s) := %s%n", c+1, tic.csv.get(0)[c], h);
            XpathAddress xa = new XpathAddress(h, true); // true for absolute xpath position
            System.out.println(xa.toString());
            lxa.add(xa);
        }

        int l = args.length > 1 ? 1 : 2; // one or two lines to be ignored at the top of the file
        for (; l < tic.csv.size(); l++) {
            tic.processLine(l, lxa);
        }

    }

    private String headColumn(int c, String[] args) {
        String head;
        if (c < args.length) {
            head = args[c];
        } else {
            head = csv.get(1)[c];
        }
        return head;
    }

    public void processLine(int l, List<XpathAddress> xpth) {
        String fn = csv.get(l)[0];
        System.out.printf("File %s%n", fn);
        TeiDocument tei = new TeiDocument(fn, false);
        if (tei.fileXML == null) return; // skip file and line
        insertInfo(tei, tei.root, l, xpth);
        // save tei file
        fn += ".modif.xml";
        Utils.createFile(tei.doc, fn);
    }

    public void insertInfo(TeiDocument tei, Node top, int l, List<XpathAddress> xpth) {
        String[] line = csv.get(l);
        for (int c = 1; c < line.length; c++) {
            if (line[c].isEmpty() || line[c].equals("NULL")) continue; // do not insert empty information ?
            // find pth in metadata
            Node node;
            XpathAddress xa = xpth.get(c-1);
//            System.out.printf("Test: %s %s%n", xa.path, line.get(c));
            try {
                node = (Node)tei.path.evaluate(xa.path, top, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                // not found or error ?
                System.err.printf("Error finding entry: %s %s%n", xa.path, e.toString());
                //e.printStackTrace();
                continue;
            }
            if (node != null) {
//                System.out.printf("Modify %s%n", xa.toString());
                // the node exists: update it
                if (xa.format.equals("node")) {
                    node.setTextContent(line[c]);
                } else if (xa.format.equals("attr")) {
                    ((Element)node).setAttribute(xa.attr, line[c]);
                } else {
                    // case of node with an attribute for the type of info and the info in the content
                    // case nodeinfo
                    node.setTextContent(line[c]);
                }
            } else {
                // it is necessary to create the node.
//                System.out.printf("Cannot find entry: %s%n", xa.path);
//                System.out.printf("Addition of %s%n", xa.toString());
                if (xa.format.equals("node")) {
                    Node newnode = TeiDocument.createNodeFromPath(tei, top, xa.path);
                    newnode.setTextContent(line[c]);
                } else if (xa.format.equals("attr")) {
                    Node newnode = TeiDocument.createNodeFromPath(tei, top, xa.path);
                    ((Element)newnode).setAttribute(xa.attr, line[c]);
                } else {
                    // case of node with an attribute for the type of info and the info in the content
                    // case nodeinfo
                    Node newnode = TeiDocument.createNodeFromPath(tei, top, xa.pathnode);
                    Element e = tei.doc.createElement(xa.namenode);
                    e.setAttribute(xa.attr, xa.type);
                    e.setTextContent(line[c]);
                    newnode.appendChild(e);
                }
            }
        }
    }
}
