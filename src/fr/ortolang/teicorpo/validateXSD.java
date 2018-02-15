package fr.ortolang.teicorpo;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class validateXSD {

	public static void main(String[] args) {
		if (args.length<2) {
			System.err.println("two arguments: validateXSD <fichier.xml> <ficher.xsd>");
		}

	    DocumentBuilder parser = null;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
			System.exit(1);
		}
	    Document document = null;
		try {
			document = parser.parse(new File(args[0]));
		} catch (SAXException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			System.exit(1);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

	    // create a SchemaFactory capable of understanding WXS schemas
	    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	    // load a WXS schema, represented by a Schema instance
	    Source schemaFile = new StreamSource(new File(args[1]));
	    Schema schema = null;
		try {
			schema = factory.newSchema(schemaFile);
		} catch (SAXException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}

	    // create a Validator instance, which can be used to validate an instance document
	    Validator validator = schema.newValidator();

	    // validate the DOM tree
	    try {
	        try {
				validator.validate(new DOMSource(document));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
	    } catch (SAXException e) {
	        // instance document is invalid!
	    	System.err.println("Document is invalid: " + e.getMessage());
			System.exit(1);
	    }

	}

}
