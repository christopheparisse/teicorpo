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

    private Element childElement(Element e, String nodename) {
        NodeList l = e.getElementsByTagName(nodename);
        if (l != null && l.getLength() > 0) return (Element) l.item(0);
        return null;
    }

    private Element setElement(Element top, String s, String v) {
        Element e = childElement(top, s);
        if (e == null) {
            e = teicorpo.doc.createElement(s);
            top.appendChild(e);
        }
        e.setTextContent(v);
        return e;
    }

    public void process(String tcofmetaName, String teicorpofileName, String teicorporesultName, String purpose) {
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
            String fage = normalizeAge(age);

            String sexe = chidNodeContent(n,"sexe");
            String role = chidNodeContent(n,"role");

            // supplements
            String educ = chidNodeContent(n, "etude");
            String nivlang = chidNodeContent(n, "statut_francais");

            String id = n.getAttribute("identifiant");
            if (id == null) id = "x"; else id = id.trim();
            String locp = n.getAttribute("locuteurPrincipal");
            if (locp == null) locp = ""; else locp = locp.trim();

            System.out.printf(":=|%d|%s|%s|%s|%s|%s|%n", i, n.getNodeName(), age, sexe, role, id, locp);

            String findloc = "//listPerson/person[persName='" + id + "']";
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

                // the supplements
                part.setAttribute("education", educ);
                Element lgk = setElement(part, "langKnowledge", "");
                Element lgkn = setElement(lgk, "langKnown", "");
                lgkn.setAttribute("tag", "fra");
                lgkn.setAttribute("level", nivlang);

                // altGrp + alt
                Element altGrp = childElement(part, "altGrp");
                if (altGrp == null) {
                    altGrp = setElement(part, "altGrp", "");
                }
                Element alt = childElement(altGrp, "alt");
                if (alt == null) {
                    setElement(altGrp, "alt", tag);
                    System.out.println("name (but not used in transcription): " + id + " tag: " + tag + " age: " + age + " (" + fage + ")");
                } else {
                    String oldtag = alt.getAttribute("type");
                    alt.setAttribute("type", tag);
                    System.out.println("name: " + id + " tag: " + tag + " age: " + age + " (" + fage + ") - Oldtag: " + oldtag);
                    // replace all tags
                    String findtag = "//annotationBlock[@who='" + oldtag + "']";
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
                    // replace in TEMPLATE_DESC
                    findtag = "//note[@type='code' and text()='" + oldtag + "']";
                    try {
                        tlist = (NodeList)teicorpo.path.evaluate(findtag, teicorpo.root, XPathConstants.NODESET);
                    } catch (XPathExpressionException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (tlist.getLength()>0) {
                        Element e = (Element)tlist.item(0);
                        e.setTextContent(tag);
                    } else {
                        System.err.printf("cannot find %s%n", findtag);
                    }
                }
            } else {
                System.out.println("cannot find path: " + findloc);
            }
        }

        // element for all the recording
        expression = "//general";
        try {
            nodelist = (NodeList)tcof.path.evaluate(expression, tcof.root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
            return;
        }

        if (nodelist.getLength() > 0) {
            // should be only one element
            Element n = (Element)nodelist.item(0);
            String canal = chidNodeContent(n,"canal");
            String genre = chidNodeContent(n,"genre");
            String degre = chidNodeContent(n,"degre");
            String find = "//textDesc";
            Element elt;
            try {
                elt = (Element) teicorpo.path.evaluate(find, teicorpo.root, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return;
            }
            if (elt == null) {
                find = "//teiHeader";
                try {
                    elt = (Element) teicorpo.path.evaluate(find, teicorpo.root, XPathConstants.NODE);
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                    return;
                }
                if (elt != null) {
                    Element e = teicorpo.doc.createElement("textDesc");
                    elt.appendChild(e);
                    elt = e;
                } else {
                    find = "/TEI";
                    try {
                        elt = (Element) teicorpo.path.evaluate(find, teicorpo.root, XPathConstants.NODE);
                    } catch (XPathExpressionException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (elt == null) {
                        System.err.println("Not a TEI FILE: stop.");
                        return;
                    }
                    Element h = teicorpo.doc.createElement("teiHeader");
                    Element e = teicorpo.doc.createElement("textDesc");
                    elt.appendChild(h);
                    h.appendChild(e);
                    elt = e;
                }
            }
            if (elt != null) {
                // add element
                Element p = setElement(elt, "preparedness", "");
                p.setAttribute("ana", degre);
                p = setElement(elt, "channel", "");
                p.setAttribute("subtype", canal);
                p.setAttribute("mode", "s");
                p = setElement(elt,"domain", "");
                p.setAttribute("nature", genre);

                if (!purpose.isEmpty()) {
                    p = setElement(elt,"purpose", "");
                    p.setAttribute("ana", purpose);
                }
            }
            String cadre = chidNodeContent(n,"cadre");
            find = "/TEI/teiHeader/profileDesc/settingDesc";
            try {
                elt = (Element) teicorpo.path.evaluate(find, teicorpo.root, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return;
            }
            if (elt != null) {
                Element p = setElement(elt, "p", cadre);
            }
        }


        // save result
        Utils.createFile(teicorporesultName, teicorpo.doc);
    }

    private String normalizeAge(String age) {
        Pattern pattern = Pattern.compile("(\\d+)[;.,](\\d+)[;.,](.*)");
        Matcher matcher = pattern.matcher(age);
        if (matcher.matches()) {
            return matcher.group(1) + '.' + (matcher.group(2));
        }
        pattern = Pattern.compile("(\\d+)[;.,](\\d+)");
        matcher = pattern.matcher(age);
        if (matcher.matches()) {
            return matcher.group(1) + '.' + (matcher.group(2));
        }
        pattern = Pattern.compile("(\\d+)(.*)");
        matcher = pattern.matcher(age);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return age;
    }

    public static void main(String args[]) {
        // parcours des arguments
        if (args.length < 3) {
            System.err.println("Usage: java -cp teicorpo.jar fr.ortolang.teicorpo.TcofInsertMeta tcof_metadata_file teicorpo_xml_file teicorpo_result_file [purpose]");
            System.err.println("Insert the content of 'tcof_metadata_file' into 'teicorpo_xml_file' and save it as 'teicorpo_result_file' - purpose is an optional addition to the metadata.");
            return;
        }
        TcofInsertMeta tim = new TcofInsertMeta();
        tim.process( args[0], args[1], args[2], args.length > 3 ? args[3] : "");
    }
}
