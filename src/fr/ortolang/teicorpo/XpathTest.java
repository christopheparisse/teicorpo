package fr.ortolang.teicorpo;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.StringWriter;

public class XpathTest {
    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("Usage: XpathTest xml/tei-filename xpath-query -details");
        }
        TeiDocument teidoc = new TeiDocument(args[0], false);
        XPathExpression expr = null;
        try {
            expr = teidoc.path.compile(args[1]);
        } catch (XPathExpressionException e) {
            System.err.printf("xpath syntax error.%n%s%n", e.toString());
            e.printStackTrace();
        }
        NodeList nodes;
        try {
            nodes = (NodeList) expr.evaluate(teidoc.root, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            System.err.printf("xpath processing error.%n%s%n", e.toString());
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            if (args.length > 2)
                System.out.printf("Node %d: %s%n", i, prettyprint(nodes.item(i)));
            else
                System.out.printf("Node %d: %s%n", i, prettyprint0(nodes.item(i)));
        }
    }

    private static String prettyprint0(Node item) {
        NamedNodeMap nnm = item.getAttributes();
        String s = "<" + item.getNodeName() + " ";
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            s += n.getNodeName() + "=" + n.getTextContent() + " ";
        }
        return s + ">";
    }

    private static String prettyprint(Node item) {
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
//initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
//        DOMSource source = new DOMSource(createDocFromNode(item));
        DOMSource source = new DOMSource(item);
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return result.getWriter().toString();
    }

    private static Document createDocFromNode(Node item) {
        DocumentBuilderFactory factory = null;
        try {
            factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            doc.appendChild(item);
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
