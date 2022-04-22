package fr.ortolang.teicorpo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

/**
 * Validate a XML with INTERNAL DTD and use of the standard DOM and SAX parser of Sun implementation
 */
public class validateDTD {

	public static void main(String[] args) {
		if (args.length<1) {
			System.err.println("one argument: validateDTD <fichier.xml>");
		}
		try {
			String xmlFileNameWithExternalDTD = args[0];
			System.out.println(validateXMLWithDTDAndDOM(xmlFileNameWithExternalDTD) ? args[0] + " is valid !" :  args[0] + " is NOT valid !");
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Validate a XML with DTD and use of the standard DOM parser of Sun implementation
	 * 
	 * @param xmlFileName
	 * @return
	 */
	public static boolean validateXMLWithDTDAndDOM(String xmlFileName) {
		try {
	         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	         factory.setValidating(true);
	         factory.setNamespaceAware(true);

	         DocumentBuilder builder = factory.newDocumentBuilder();
	         builder.setErrorHandler(new MyErrorHandler());
	         // Generates a Document object tree
	         builder.parse(new InputSource(xmlFileName));

	         return true;
	         
	      } catch (Throwable e) {
	         // System.out.println(e.getMessage());
	         return false;
	      }
	}
}