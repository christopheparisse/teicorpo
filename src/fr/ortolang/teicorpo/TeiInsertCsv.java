package fr.ortolang.teicorpo;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        return (absolute == true) ? "//" + val : "./" + val ;
    }

    XpathAddress(String info, boolean absolute) {
        // three cases.
        // xpath ends with a @name --> value is put in the attribute name ... <xxx name='value'> ...
        // xpath ends with a [@name=type] --> <node name='type'>value</node>
        // xpath ends in a simple word --> value is put in the node text content ... <name>value</name>

        fullform = info;
        if (fullform.equals("none")) {
            path = "";
            format = "ignore";
            attr = "";
            type = "";
            pathnode = "";
            namenode = "";
            return;
        }
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
    private Map<String, String[]> speakers;
    TeiInsertCsv() {
        csv = null;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.printf("Usage: TeiInsertCsv <csvfile> [-userinfo teifile] [-o outputDirectory] [list if TEI xpath names for each column of the csv...]");
            return;
        }

        String userinfo = "";
        String outputDir = "";

        TeiInsertCsv tic = new TeiInsertCsv();
        tic.csv = CsvReader.load(args[0]);

        if (tic.csv == null || tic.csv.size() < 1) {
            System.err.printf("File %s not found or only one line. Stop.", args[0]);
            return;
        }

        List<String> largs = new ArrayList<String>();
        for (int c = 1; c < args.length; c++) {
            if (args[c].startsWith("-")) {
                if (!args[c].equals("-userinfo") && !args[c].equals("-o")) {
                    System.err.println("Optional arguments are only -userinfo teifile and -o outputDirectory. Stop.");
                    return;
                }
                if (c+1 >= args.length) {
                    System.err.println("Missing argument after -userinfo or -o. Stop.");
                    return;
                }
                if (args[c].equals("-userinfo")) {
                    userinfo = args[c+1];
                }
                if (args[c].equals("-o")) {
                    outputDir = args[c+1];
                }
                c++;
                continue;
            }
            largs.add(args[c]);
        }

        if (!outputDir.isEmpty() && !Utils.testAndCreateDir(outputDir)) {
            return;
        }

//        System.out.printf("userinfo(%s) outputDir(%s)%n", userinfo, outputDir);
        List<XpathAddress> lxa = new ArrayList<XpathAddress>();
//        System.out.printf("Column: 1 (%s) := file names or IDs%n", tic.csv.get(1)[0]);
        for (int c = 1; c < tic.csv.get(0).length; c++) {
            String h = tic.headColumn(c, largs);  // extracted from the second lines or from the arguments
//            System.out.printf("Column: %d (%s) := %s%n", c+1, tic.csv.get(0)[c], h);
            XpathAddress xa = new XpathAddress(h, userinfo.isEmpty() ? true : false); // true for absolute xpath position
//            System.out.println(xa.toString());
            lxa.add(xa);
        }

        if (userinfo.isEmpty()) {
            System.out.printf("Insert metadata from %s in all files%n", args[0]);
            int l = largs.size() > 1 ? 1 : 2; // one or two lines to be ignored at the top of the file
            for (; l < tic.csv.size(); l++) {
                tic.processLine(l, lxa, outputDir);
            }
        } else {
            System.out.printf("Insert metadata from %s in the participants in %s%n", args[0], userinfo);
            tic.initializeSpeakers(tic.csv, largs.size() > 0);
            tic.processParticipants(userinfo, lxa, outputDir);
        }
    }

    private void processParticipants(String userinfo, List<XpathAddress> lxa, String outputDir) {
        TeiDocument tei = new TeiDocument(userinfo, false);
        if (tei.fileXML == null) return; // skip file and line
        // find all users and insert corresponding metadata information
        NodeList nodelist = null;
        String upath = "//person";
        try {
            nodelist = (NodeList)tei.path.evaluate(upath, tei.root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            // not found or error ?
            System.err.printf("Error finding users: %s %s%n", upath, e.toString());
            return;
            //e.printStackTrace();
        }
        if (nodelist != null) {
            for (int i = 0; i < nodelist.getLength(); i++) {
                String name = null;
                upath = ".//persName";
                try {
                    name = (String)tei.path.evaluate(upath, nodelist.item(i), XPathConstants.STRING);
                } catch (XPathExpressionException e) {
                    // not found or error ?
                    System.err.printf("Error finding user: %s %s%n", upath, e.toString());
                    return;
                    //e.printStackTrace();
                }
                if (name != null && !name.isEmpty()) {
                    if (speakers.containsKey(name)) {
                        System.out.printf("Insert info for %s%n", name);
                        insertInfo(tei, nodelist.item(i), speakers.get(name), lxa);
                    } else {
                        System.out.printf("Cannot find speaker %s in csv file%n", name);
                    }
                }
            }
        }
        // save tei file
        if (!outputDir.isEmpty())
            userinfo = outputDir + "/" + Utils.lastname(userinfo);
        else
            userinfo += ".modif.xml";
        System.out.println("Write file: " + userinfo);
        Utils.createFile(tei.doc, userinfo);
    }

    private void initializeSpeakers(List<String[]> csv, boolean b) {
        // create a hash list for the speaker
        speakers = new HashMap<String, String[]>();
        for (int l = (b ? 1 : 2) ; l < csv.size(); l++) {
            speakers.put(csv.get(l)[1], csv.get(l)); // speakers is the second column in the csv file
        }
    }

    private String headColumn(int c, List<String> args) {
        String head;
        if (args.size() > 0) {
            head = args.get(c);
        } else {
            head = csv.get(1)[c];
        }
        return head;
    }

    public void processLine(int l, List<XpathAddress> xpth, String outputDir) {
        String fn = csv.get(l)[0];
//        System.out.printf("File %s%n", fn);
        TeiDocument tei = new TeiDocument(fn, false);
        if (tei.fileXML == null) return; // skip file and line
        insertInfo(tei, tei.root, csv.get(l), xpth);
        // save tei file
        if (!outputDir.isEmpty())
            fn = outputDir + "/" + Utils.lastname(fn);
        else
            fn += ".modif.xml";
        System.out.println("Write file: " + fn);
        Utils.createFile(tei.doc, fn);
    }

    public void insertInfo(TeiDocument tei, Node top, String[] line, List<XpathAddress> xpth) {
        for (int c = 1; c < line.length; c++) {
            if (line[c].isEmpty() || line[c].equals("NULL")) continue; // do not insert empty information ?
            // find pth in metadata
            Node node;
            XpathAddress xa = xpth.get(c-1);
            if (xa.format.equals("ignore")) continue; // ignore this column
//            System.out.printf("Test: %s %s%n", xa.path, line[c]);
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
