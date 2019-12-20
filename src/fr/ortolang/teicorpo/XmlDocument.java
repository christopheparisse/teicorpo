package fr.ortolang.teicorpo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;

public class XmlDocument {
    public Document doc; // document
    private DocumentBuilderFactory factory;
    private XPathFactory xpf; // xpath access
    public XPath path;
    public Element root; // root
    public File fileXML; // file

    public XmlDocument(String uri) {
        // open an xml document
        try {
            factory = DocumentBuilderFactory.newInstance();
            Utils.setDTDvalidation(factory, false);
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
        }
    }

}
