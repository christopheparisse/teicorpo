package fr.ortolang.teicorpo;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TcofInsertMeta {
    /** file Metadata TCOF. */
    private TeiDocument tcof; // Xml doc

    /** file TEICORPO. */
    private TeiDocument teicorpo; // xml document

    public void process(String tcofmetaName, String teicorpofileName, String teicorporesultName, String purpose) {
        System.out.printf("insert tcof meta from %s into %s and save as %s%n", tcofmetaName, teicorpofileName, teicorporesultName);
        tcof = new TeiDocument(tcofmetaName, false);
        teicorpo = new TeiDocument(teicorpofileName, false);

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
            String age = TeiDocument.childNodeContent(n,"age");
            String fage = normalizeAge(age);

            String sexe = TeiDocument.childNodeContent(n,"sexe");
            String role = TeiDocument.childNodeContent(n,"role");

            // supplements
            String educ = TeiDocument.childNodeContent(n, "etude");
            String nivlang = TeiDocument.childNodeContent(n, "statut_francais");

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
                TeiDocument.setElement(teicorpo.doc, part, "role", role);

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
                Element teiage = TeiDocument.setElement(teicorpo.doc, part, "age", age);
                teiage.setAttribute("value", fage);

                // the supplements
                part.setAttribute("education", educ);
                Element lgk = TeiDocument.setElement(teicorpo.doc, part, "langKnowledge", "");
                Element lgkn = TeiDocument.setElement(teicorpo.doc, lgk, "langKnown", "");
                lgkn.setAttribute("tag", "fra");
                lgkn.setAttribute("level", nivlang);

                // altGrp + alt
                Element altGrp = TeiDocument.childElement(part, "altGrp");
                if (altGrp == null) {
                    altGrp = TeiDocument.setElement(teicorpo.doc, part, "altGrp", "");
                }
                Element alt = TeiDocument.childElement(altGrp, "alt");
                if (alt == null) {
                    TeiDocument.setElement(teicorpo.doc, altGrp, "alt", tag);
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
            String canal = TeiDocument.childNodeContent(n,"canal");
            String genre = TeiDocument.childNodeContent(n,"genre");
            String degre = TeiDocument.childNodeContent(n,"degre");
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
                Element p = TeiDocument.setElement(teicorpo.doc, elt, "preparedness", "");
                p.setAttribute("ana", degre);
                p = TeiDocument.setElement(teicorpo.doc, elt, "channel", "");
                p.setAttribute("subtype", canal);
                p.setAttribute("mode", "s");
                p = TeiDocument.setElement(teicorpo.doc, elt,"domain", "");
                p.setAttribute("nature", genre);

                if (!purpose.isEmpty()) {
                    p = TeiDocument.setElement(teicorpo.doc, elt,"purpose", "");
                    p.setAttribute("ana", purpose);
                }
            }
            String cadre = TeiDocument.childNodeContent(n,"cadre");
            find = "/TEI/teiHeader/profileDesc/settingDesc";
            try {
                elt = (Element) teicorpo.path.evaluate(find, teicorpo.root, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return;
            }
            if (elt != null) {
                Element p = TeiDocument.setElement(teicorpo.doc, elt, "p", cadre);
            }
        }


        // save result
        TeiDocument.createFile(teicorporesultName, teicorpo.doc);
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
        String usageString = "Usage: java -cp teicorpo.jar fr.ortolang.teicorpo.TcofInsertMeta teicorpo_xml_file -metadata tcof_metadata_file -o teicorpo_result_file -p purpose"
            + "Insert the content of 'tcof_metadata_file' into 'teicorpo_xml_file' and save it as 'teicorpo_result_file' - purpose is an optional addition to the metadata.";
        TierParams options = new TierParams();
        // Parcours des arguments
        if (!TierParams.processArgs(args, options, usageString, ".tei_corpo.xml", ".tei_corpo.xml", 10)) {
            if (!options.noerror) return;
        }
        if (options.input.size() != 1 || options.metadata == null || options.metadata.isEmpty()) {
            System.err.println("No input file or more than one input file. One metadata file is also required.");
            return;
        }
        if (options.output == null) {
            options.output = options.input.get(0) + ".tei_corpo2.xml";
        }
        System.out.printf("Insertion of %s in %s := results in %s%n", options.metadata, options.input.get(0), options.output);
        TcofInsertMeta tim = new TcofInsertMeta();
        tim.process( options.input.get(0), options.metadata, options.output, options.purpose);
    }
}
