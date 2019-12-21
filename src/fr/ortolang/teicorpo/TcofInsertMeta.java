package fr.ortolang.teicorpo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcofInsertMeta {
    /** file Metadata TCOF. */
    private XmlDocument tcof; // Xml doc

    /** file TEICORPO. */
    private XmlDocument teicorpo; // xml document

    private String chidNodeContent(Element e, String nodename) {
        NodeList l = e.getElementsByTagName(nodename);
        if (l != null && l.getLength() > 0) return l.item(0).getTextContent().trim();
        return "";
    }

    private Element chidElement(Element e, String nodename) {
        NodeList l = e.getElementsByTagName(nodename);
        if (l != null && l.getLength() > 0) return (Element) l.item(0);
        return null;
    }

    private Element setElement(Element top, String s, String v) {
        Element e = chidElement(top, s);
        if (e == null) {
            e = teicorpo.doc.createElement(s);
            top.appendChild(e);
        }
        e.setTextContent(v);
        return e;
    }

    public void process(String tcofmetaName, String teicorpofileName, String teicorporesultName) {
        System.out.printf("insert tcof meta from %s into %s and save as %s%n", tcofmetaName, teicorpofileName, teicorporesultName);
        tcof = new XmlDocument(tcofmetaName);
        teicorpo = new XmlDocument(teicorpofileName);

        if (tcof.doc == null || teicorpo.doc == null) {
            System.err.println("cannot process: stop.");
            return;
        }

        // first node locuteur
        String expression = "//locuteur/locuteur";
        NodeList nodelist;
        try {
            nodelist = (NodeList)tcof.path.evaluate(expression, tcof.root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return;
        }
        boolean firstchifound = false;
        boolean firstadufound = false;
        // process
        for (int i = 0 ; i < nodelist.getLength(); i++) {
            Element n = (Element)nodelist.item(i);
            String age = chidNodeContent(n,"age");
            Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(.*)");
            Matcher matcher = pattern.matcher(age);
            String fage;
            if (matcher.matches()) {
                fage = matcher.group(1) + '.' + (matcher.group(2));
            } else {
                fage = "40.2";
            }

            String sexe = chidNodeContent(n,"sexe");
            String role = chidNodeContent(n,"role");
            String id = n.getAttribute("identifiant");
            if (id == null) id = "x"; else id = id.trim();
            String locp = n.getAttribute("locuteurPrincipal");
            if (locp == null) locp = ""; else locp = locp.trim();

            System.out.printf(":=|%d|%s|%s|%s|%s|%s|%n", i, n.getNodeName(), age, sexe, role, id, locp);

            String findloc = "//listPerson/person[persName=\"" + id + "\"]";
            Element part;
            try {
                part = (Element) teicorpo.path.evaluate(findloc, teicorpo.root, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return;
            }
            if (part != null) {
                // System.out.printf("?=|%s|%s|%n", part.getNodeName(), part.getTextContent());
                part.setAttribute("source", "TCOF");
                if (sexe.equals("M"))
                    part.setAttribute("sex", "1");
                else if (sexe.equals("F"))
                    part.setAttribute("sex", "2");
                else
                    part.setAttribute("sex", "9");
                setElement(part, "role", role);

                String tag = "XXX";
                if (role.equals("Apprenant")) {
                    if (firstchifound == false) {
                        firstchifound = true;
                        tag = "CHI";
                    } else {
                        tag = "CHI" + (i+1);
                    }
                    if (age.isEmpty()) {
                        age = "10 ans ?";
                        fage = "10.0";
                    }
                } else {
                    if (firstadufound == false) {
                        firstadufound = true;
                        tag = "ADU";
                    } else {
                        tag = "ADU" + (i+1);
                    }
                    if (age.isEmpty()) {
                        age = "40 ans ?";
                        fage = "40.2";
                    }
                }
                part.setAttribute("age", fage);
                Element teiage = setElement(part, "age", age);
                teiage.setAttribute("value", fage);
                // altGrp + alt
                Element altGrp = setElement(part, "altGrp", "");
                setElement(altGrp, "alt", tag);

                System.out.println("name: " + id + " tag: " + tag + " age: " + age + " (" + fage + ")");
                // replace all tags
                String oldtag = "spk" + (i + 1);
                String findtag = "//annotationBlock[@who=\"" + oldtag + "\"]";
                NodeList tlist;
                try {
                    tlist = (NodeList)teicorpo.path.evaluate(findtag, teicorpo.root, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                    return;
                }
                // process
                for (int k = 0 ; k < tlist.getLength(); k++) {
                    Element e = (Element)tlist.item(k);
                    e.setAttribute("who", tag);
                }

            } else {
                System.out.println("cannot find path: " + findloc);
            }
        }

        // save result
        Utils.createFile(teicorporesultName, teicorpo.doc);
    }

    public static void main(String args[]) {
        // parcours des arguments
        if (args.length != 3) {
            System.err.println("Usage: java -cp teicorpo.jar fr.ortolang.teicorpo.TcofInsertMeta tcof_metadata_file teicorpo_xml_file teicorpo_result_file");
            System.err.println("Insert the content of 'tcof_metadata_file' into 'teicorpo_xml_file' and save it as 'teicorpo_result_file'");
            return;
        }
        TcofInsertMeta tim = new TcofInsertMeta();
        tim.process( args[0], args[1], args[2]);
    }
}