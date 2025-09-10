package fr.ortolang.teicorpo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
// import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import javax.xml.*;
import javax.xml.namespace.NamespaceContext;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeiDocument {
    public static String ANNOTATIONBLOC = "annotationBlock";
    public static String TEI_ALL = "http://www.tei-c.org/Vault/P5/current/xml/tei/custom/schema/dtd/tei_all.dtd";
    // "https://www.tei-c.org/release/xml/tei/custom/schema/dtd/tei_all.dtd";
    // "http://www.tei-c.org/Vault/P5/current/xml/tei/schema/dtd/spoken.dtd";
    public static String TEI_SPOKEN_DTD = "http://ct3.ortolang.fr/tei-corpo/spoken.dtd";
    public Document doc; // document
    private DocumentBuilderFactory factory;
    private XPathFactory xpf; // xpath access
    public XPath path;
    public Element root; // root
    public File fileXML; // file

    TeiDocument(String uri, boolean validation) {
        // open an xml document
        try {
            factory = DocumentBuilderFactory.newInstance();
            setDTDvalidation(factory, validation);
            DocumentBuilder builder = factory.newDocumentBuilder();
            fileXML = new File(uri);
            doc = builder.parse(fileXML);
            root = doc.getDocumentElement();
            xpf = XPathFactory.newInstance();
            path = xpf.newXPath();
        } catch (Exception e) {
            // e.printStackTrace();
            System.err.println(e.toString());
            System.err.printf("cannot open uri %s or error in xml processing %n", uri);
            fileXML = null;
            doc = null;
        }
    }

    TeiDocument(boolean validation) {
        try {
            factory = DocumentBuilderFactory.newInstance();
            setDTDvalidation(factory, validation);
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.newDocument();
            xpf = XPathFactory.newInstance();
            path = xpf.newXPath();
            root = doc.createElement("TEI");
            root.setAttribute("xmlns", "http://www.tei-c.org/ns/1.0");
            doc.appendChild(root);
        } catch (Exception e) {
            // e.printStackTrace();
            System.err.println(e.toString());
            System.err.printf("cannot create xml file%n");
        }
    }

    static String childNodeContent(Element e, String nodename) {
        NodeList l = e.getElementsByTagName(nodename);
        if (l != null && l.getLength() > 0) return l.item(0).getTextContent().trim();
        return "";
    }

    static Element childElement(Element e, String nodename) {
        NodeList l = e.getElementsByTagName(nodename);
        if (l != null && l.getLength() > 0) return (Element) l.item(0);
        return null;
    }

    static Element createElement(Document doc, Element top, String s, String v) {
        Element e = doc.createElement(s);
        top.appendChild(e);
        if (!v.isEmpty())
            e.setTextContent(v);
        return e;
    }

    static Element setElement(Document doc, Element top, String s, String v) {
        Element e = childElement(top, s);
        if (e == null) {
            e = doc.createElement(s);
            top.appendChild(e);
        }
        if (!v.isEmpty())
            e.setTextContent(v);
        return e;
    }

    static Element findOrCreate(Document doc, Element father, String nodename) {
        NodeList nl = father.getElementsByTagName(nodename);
        if (nl.getLength() < 1) {
            Element n = doc.createElement(nodename);
            father.appendChild(n);
            return n;
        } else {
            return (Element)nl.item(0);
        }
    }

    /*
    static Element createNodeFromPath(TeiDocument tei, Node top, String path) {
        // isolate the head of the path: ./ or .// or / or // or nothing
        // to find the first element of the path
        String currentpath;
        String restofpath;
        Pattern pattern = Pattern.compile("([./]*)(.*)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.matches()) { // there is a path above the last node
            currentpath = matcher.group(1);
            restofpath = matcher.group(2);
        } else {
            currentpath = "./";
            restofpath = path;
        }

        if (restofpath.indexOf("//") >= 0) {
            System.err.printf("Cannot create a path that contains a // : %s%n", path);
            return null;
        }

        Node node = top;

        while (!restofpath.isEmpty()) {
            String nodetocreate = "";
            // split path
            pattern = Pattern.compile("(.*)\\/(.*)");
            matcher = pattern.matcher(restofpath);
            if (matcher.matches()) { // there is a top path and a rest of path
                nodetocreate = matcher.group(1)
                currentpath = currentpath + matcher.group(1);
                restofpath = matcher.group(2);
            } else {
                // final part
                nodetocreate = restofpath;
                currentpath = currentpath + restofpath;
                restofpath = "";
            }

            try {
                node = (Node)tei.path.evaluate(currentpath, top, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
                // not found or error ?
                System.err.printf("Error finding entry: %s %s%n", xpth.get(c).path, e.toString());
                //e.printStackTrace();
                return null;
            }
            if (node == null) {
                // create a new node.
                Element e = tei.doc.createElement(nodetocreate);
            } else {
                // node exists
                // nothing to do
            }
        }
        return (Element)node;
    }
    */

    static Element createNodeFromPath(TeiDocument tei, Node top, String path) {
        List<String> missingnodes = new ArrayList<String>();
        // first find starting point
        String currentpath = path;
        Node nodefound = null;
        while (true) {
            // test currentpath
            try {
//                System.err.printf("createNodeFromPath: look for %s%n", currentpath);
                nodefound = (Node)tei.path.evaluate(currentpath, top, XPathConstants.NODE);
            } catch (XPathExpressionException e) {
//                System.err.printf("Error finding entry: (%s)%n ERROR:[%s]%n", currentpath, e.toString());
                //e.printStackTrace();
                return null;
            }
            if (nodefound == null) {
//                System.err.printf("createNodeFromPath: NOT FOUND %s%n", currentpath);
                // not found: check above
                // split path
                Pattern pattern = Pattern.compile("(.*)[./]+(.+)");
                Matcher matcher = pattern.matcher(currentpath);
                if (matcher.matches()) { // there is a top path and a rest of path
                    currentpath = matcher.group(1);
                    missingnodes.add(matcher.group(2));
//                    System.err.printf("NEXT IS %s => %s%n", currentpath, matcher.group(2));
                    if (currentpath.equals(".") || currentpath.equals("/") || currentpath.equals("./") || currentpath.equals("//") || currentpath.equals(".//")) {
                        nodefound = top;
                        break;
                    }
                } else {
                    // final part
                    // it should be a / or a ./ or a // or a .//
//                    System.err.printf("STOP AT %s%n", currentpath);
                    if (currentpath.equals(".") || currentpath.equals("/") || currentpath.equals("./") || currentpath.equals("//") || currentpath.equals(".//")) {
                        nodefound = top;
                        break;
                    }
                    System.err.printf("Error in createNodeFromPath: %s %s%n", currentpath, path);
                    return null;
                }
            } else {
//                System.err.printf("createNodeFromPath: FOUND %s%n", currentpath);
                // node exists
                // stop and add missing nodes
                break;
            }
        }
        // add missing nodes to nodefound
        for (int i = missingnodes.size() - 1; i >= 0 ; i--) {
            // add node
            Element e = tei.doc.createElement(missingnodes.get(i));
            nodefound.appendChild(e);
            nodefound = (Node)e;
        }
        return (Element)nodefound;
    }

    void addNamespace() {
        path.setNamespaceContext(new NamespaceContext() {
            public String getNamespaceURI(String prefix) {
                System.out.println("prefix called " + prefix);
                if (prefix == null) {
                    throw new IllegalArgumentException("No prefix provided!");
                } else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                    System.out.println("default prefix called");
                    return "http://www.tei-c.org/ns/1.0";
                } else if (prefix.equals("tei")) {
                    System.out.println("tei prefix called");
                    return "http://www.tei-c.org/ns/1.0";
                } else if (prefix.equals("xsi")) {
                    return "http://www.w3.org/2001/XMLSchema-instance";
                } else {
                    return XMLConstants.NULL_NS_URI;
                }
            }

            public String getPrefix(String uri) {
                return null;
            }

            // @Override
            public Iterator getPrefixes(String arg0) {
                // TODO Auto-generated method stub
                return null;
            }
        });
    }

    public static String teiCorpoDtd() {
        // return teiStylePure == true ? TEI_ALL : TEI_CORPO_DTD;
        return TEI_ALL;
    }

    public static boolean isElement(Node n){
        return n.getNodeType() == Node.ELEMENT_NODE;
    }

    public static boolean isText(Node n){
        return n.getNodeType() == Node.TEXT_NODE;
    }

    public static void setDTDvalidation(DocumentBuilderFactory factory, boolean b) {
        try {
            if (b == true) System.out.printf("validation:%s%n",b?"yes":"no");
            factory.setValidating(b);
//			factory.setNamespaceAware(true);
            factory.setFeature("http://xml.org/sax/features/namespaces", b);
            factory.setFeature("http://xml.org/sax/features/validation", b);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",	b);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", b);
            //System.out.println("end validation");
        }
        catch (Exception e) {
            System.err.println("Votre fichier n'est pas conforme à la DTD passée en argument");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Element createDivHead(Document docTEI) {
        Element div = docTEI.createElement("div");
        Element head = docTEI.createElement("head");
        div.appendChild(head);
        return div;
    }

    public static void setDivHeadAttr(Document docTEI, Element div, String type, String value) {
        NodeList head = div.getElementsByTagName("head");
        if (head.getLength() == 0) {
            System.err.println("Div should contain Head");
            try {
                throw new Exception();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(1);
            }
        }
        Element note = docTEI.createElement("note");
        note.setAttribute("type", type);
        note.setTextContent(value);
        head.item(0).appendChild(note);
    }

    public static String getDivHeadAttr(Element annotU, String attrName) {
        String a = annotU.getAttribute(attrName);
        if (a == null || a.isEmpty())
            return getHeadAttr(annotU, attrName);
        return a;
    }

    public static String getHeadAttr(Element annotU, String attrName) {
        NodeList nl = annotU.getElementsByTagName("head");
        if (nl == null) return "";
        Element head = (Element)nl.item(0);
        if (head == null) return "";
        NodeList notes = head.getElementsByTagName("note");
        for (int i=0; i < notes.getLength(); i++) {
            Element e = (Element)(notes.item(i));
            String a = e.getAttribute("type");
            if (a.equals(attrName))
                return e.getTextContent();
        }
        return "";
    }

    public static Element createAnnotationBloc(Document docTEI) {
        Element bloc;
        if (Utils.teiStylePure == true) {
            // simulate non existing TEI element with actual existing elements
            bloc = docTEI.createElement("div");
            bloc.setAttribute("type", ANNOTATIONBLOC);
            Element head = docTEI.createElement("head");
            bloc.appendChild(head);
        } else {
            // use extensions of TEI for Oral
            bloc = docTEI.createElement(ANNOTATIONBLOC);
        }
        return bloc;
    }

    public static void setAttrAnnotationBloc(Document docTEI, Element bloc, String type, String value) {
        if (Utils.teiStylePure == true) {
            setDivHeadAttr(docTEI, bloc, type, value);
        } else {
            bloc.setAttribute(type, value);
        }
    }

    public static NodeList getAllDivs(XPath xpath, Document docTEI) throws XPathExpressionException {
        NodeList nl;
        if (Utils.teiStylePure == true) {
            XPathExpression expr = xpath.compile("//div[not(@type='" + ANNOTATIONBLOC + "')]");
            nl = (NodeList) expr.evaluate(docTEI, XPathConstants.NODESET);
        } else {
            nl = docTEI.getElementsByTagName("div");
        }
        return nl;
    }

    public static NodeList getAllAnnotationBloc(XPath xpath, Document docTEI) throws XPathExpressionException {
        NodeList nl;
        if (Utils.teiStylePure == true) {
            XPathExpression expr = xpath.compile("//div[@type='" + ANNOTATIONBLOC + "']");
            nl = (NodeList) expr.evaluate(docTEI, XPathConstants.NODESET);
        } else {
            nl = docTEI.getElementsByTagName(ANNOTATIONBLOC);
        }
        return nl;
    }

    public static NodeList getSomeAnnotationBloc(XPath xpath, Element eltTop) throws XPathExpressionException {
        NodeList nl;
        if (Utils.teiStylePure == true) {
            NodeList childs = eltTop.getChildNodes();
            return childs;
//			for (int i=0; i < childs.getLength(); i++) {
//				if (((Element)childs.item(i)).getAttribute("type") == Utils.ANNOTATIONBLOC) nl.add(childs.item(i));
//			}
//			return nl;
//	        XPathExpression expr = xpath.compile("//div[@type=\"" + Utils.ANNOTATIONBLOC + "\"]");
//	        nl = (NodeList) expr.evaluate(eltTop, XPathConstants.NODESET);
        } else {
            nl = eltTop.getElementsByTagName(ANNOTATIONBLOC);
        }
        return nl;
    }

    public static String getAttrAnnotationBloc(Element annotU, String attrName) {
        if (Utils.teiStylePure == true) {
            return getHeadAttr(annotU, attrName);
        } else {
            return annotU.getAttribute(attrName);
        }
    }

    public static boolean isAnnotationBloc(Element el) {
        if (Utils.teiStylePure == true) {
            return (el.getAttribute("type").equals(ANNOTATIONBLOC)) ? true : false;
        } else {
            return (el.getNodeName().equals(ANNOTATIONBLOC)) ? true : false;
        }
    }

    public static void setAttrAnnotationBlocSupplement(Document docTEI, Element annotatedU, String string, String attValue) {
        NodeList spanGrpList = annotatedU.getElementsByTagName("spanGrp");
        if (spanGrpList != null && spanGrpList.getLength() > 0) {
            for (int i=0; i<spanGrpList.getLength(); i++) {
                Element spanGrp = (Element) spanGrpList.item(i);
                if (spanGrp.getAttribute("type").equals("TurnInformation")) {
                    NodeList spanList = spanGrp.getElementsByTagName("span");
                    if (spanList != null && spanList.getLength() > 0) {
                        for (int j=0; j<spanList.getLength(); j++) {
                            Element span = (Element) spanList.item(j);
                            if (span.getAttribute("type").equals(string)) {
                                span.setAttribute("type", string);
                                return;
                            }
                        }
                    }
                    // si pas trouvé
                    // ou si pas de span dans le spanGrp TurnInformation
                    Element s = docTEI.createElement("span");
                    s.setAttribute("type", string);
                    s.setTextContent(attValue);
                    spanGrp.appendChild(s);
                    return;
                }
            }
        }
        // si pas de spanGrp s'appelent TurnInformation
        // ou si pas encore de spanGrp
        Element sg = docTEI.createElement("spanGrp");
        sg.setAttribute("type", "TurnInformation");
        Element s = docTEI.createElement("span");
        s.setAttribute("type", string);
        s.setTextContent(attValue);
        sg.appendChild(s);
        annotatedU.appendChild(sg);
    }

    static void addToHead(Document docTEI, Element head, String access, String name, boolean test) {
        Element pnote = docTEI.createElement("note");
        pnote.setAttribute("type", "access");
        head.appendChild(pnote);
        Element bnote = docTEI.createElement("note");
        bnote.setAttribute("type", "filename");
        head.appendChild(bnote);
        if (test == true) {
            pnote.setTextContent("test");
            bnote.setTextContent("test");
        } else {
            pnote.setTextContent(access);
            String fname = Utils.basename(name) + Utils.extname(name);
            bnote.setTextContent(fname);
        }
    }

    public static Element setDocumentAccess(Document docTEI, String access, String name, TierParams options) {
        NodeList revDesc = docTEI.getElementsByTagName("revisionDesc");
        NodeList llist = ((Element)revDesc.item(0)).getElementsByTagName("list");
        // find (unique) list element or create it
        Element list;
        if (llist.getLength() == 0) {
            list = docTEI.createElement("list");
            ((Element)revDesc.item(0)).appendChild(list);
        } else {
            list = ((Element)llist.item(0));
        }
        // put name in head
        NodeList lhead = list.getElementsByTagName("head");
        if (lhead.getLength() == 0) {
            Element head = docTEI.createElement("head");
            addToHead(docTEI, head, access, name, options.test);
            ((Element)list).appendChild(head);
        } else {
            Element head = (Element)lhead.item(0);
            NodeList notes = head.getElementsByTagName("note");
            for (int i=0; i < notes.getLength() ; i++) {
                head.removeChild(notes.item(i));
            }
            addToHead(docTEI, head, access, name, options.test);
            // removes all child nodes
            /*
            while (head.hasChildNodes())
                head.removeChild(head.getFirstChild());
            */
        }
        return list;
    }

    public static void setDocumentName(Document docTEI, String name, TierParams optionsOutput) {
        Element list = setDocumentAccess(docTEI, Utils.pathname(name) + "/", name, optionsOutput);
        Element item = docTEI.createElement("item");
        Element desc = docTEI.createElement("desc");
        item.setTextContent( (optionsOutput.test) ? "test" : name);
        desc.setTextContent("docname");
        item.appendChild(desc);

        NodeList litem = list.getElementsByTagName("item");
        for (int i=0; i < litem.getLength(); i++) {
            NodeList d = ((Element)litem.item(i)).getElementsByTagName("desc");
            if (d != null && d.getLength() > 0) {
                if (((Element)d.item(0)).getTextContent().equals("docname")) {
                    ((Element)d.item(0)).setTextContent( (optionsOutput.test) ? "test" : name);
                    return;
                }
            }
        }
        // if not done
        list.appendChild(item);
    }

    public static void setTranscriptionDesc(Document docTEI, String id, String version, String desc) {
        NodeList trDesc = docTEI.getElementsByTagName("transcriptionDesc");
        Element item;
        if (trDesc.getLength() < 1) {
            NodeList encDesc = docTEI.getElementsByTagName("encodingDesc");
            if (encDesc.getLength() < 1) {
                System.err.println("manque encoding desc: information non ajoutée");
                return;
            }
            item = docTEI.createElement("transcriptionDesc");
            ((Element)encDesc.item(0)).appendChild(item);
        } else {
            item = ((Element)trDesc.item(0));
        }
        if (!id.isEmpty()) {
            item.setAttribute("ident", id);
        }
        if (!version.isEmpty()) {
            item.setAttribute("version", version);
        }
        if (!desc.isEmpty()) {
            Element d;
            NodeList list = item.getElementsByTagName("desc");
            if (list.getLength() < 1) {
                d = docTEI.createElement("desc");
                item.appendChild(d);
            } else {
                d = ((Element)list.item(0));
            }
            d.setTextContent(desc);
        }
    }

    public static void setRevisionInfo(Document docTEI, Element revisionDesc, String input, String output, boolean test) {
        if (revisionDesc ==  null) {
            NodeList revDesc = docTEI.getElementsByTagName("revisionDesc");
            if (revDesc == null || revDesc.getLength() < 1) {
                System.err.println("cannot set revisionDesc");
                return;
            }
            revisionDesc = ((Element)revDesc.item(0));
        }
        NodeList nlist = revisionDesc.getElementsByTagName("list");
        Element list;
        if (nlist == null || nlist.getLength() < 1) {
            list = docTEI.createElement("list");
            revisionDesc.appendChild(list);
        } else
            list = ((Element)nlist.item(0));

        if (test == true) {
            Element item = docTEI.createElement("item");
            Element desc = docTEI.createElement("desc");
            item.setTextContent("test document");
            desc.setTextContent("test");
            list.appendChild(item);
            item.appendChild(desc);
        } else {
            Element item = docTEI.createElement("item");
            Element desc = docTEI.createElement("desc");
            item.setTextContent(
                    new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(Calendar.getInstance().getTime()));
            desc.setTextContent("date");
            list.appendChild(item);
            item.appendChild(desc);

            if (input != null) {
                item = docTEI.createElement("item");
                desc = docTEI.createElement("desc");
                item.setTextContent(input);
                desc.setTextContent("from");
                list.appendChild(item);
                item.appendChild(desc);
            }

            if (output != null || input != null) {
                item = docTEI.createElement("item");
                desc = docTEI.createElement("desc");
                if (output != null)
                    item.setTextContent(output);
                else
                    item.setTextContent(Utils.fullbasename(input) + Utils.EXT);
                desc.setTextContent("to");
                list.appendChild(item);
                item.appendChild(desc);
            }
        }
    }

    static String getText(Element e) {
        String s = "";
        NodeList nle = e.getChildNodes();
        for (int i = 0; i < nle.getLength(); i++) {
            // System.out.printf("-- %d %s %n", i, nle.item(i));
            Node ei = nle.item(i);
            if (isText(ei)) {
                s += ei.getTextContent();
            }
        }
        return s;
    }

}
