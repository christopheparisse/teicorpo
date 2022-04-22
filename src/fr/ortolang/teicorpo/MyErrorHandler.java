package fr.ortolang.teicorpo;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class MyErrorHandler implements ErrorHandler {

	@Override
    public void warning(SAXParseException e) throws SAXException {
	       System.out.print("Warning at line " + e.getLineNumber() + ": ");
	       System.out.println(e.getMessage());
	    }

	@Override
    public void error(SAXParseException e) throws SAXException {
        System.out.print("Error at line " + e.getLineNumber() + ": ");
        System.out.println(e.getMessage());
    }

	@Override
    public void fatalError(SAXParseException e) throws SAXException {
	       System.out.print("Fatal error at line " + e.getLineNumber() + ": ");
	       System.out.println(e.getMessage());
	    }

}
