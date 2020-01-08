package fr.ortolang.teicorpo;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class modifMetaEslo {


	Document docMETA;
	Element rootMETA;

	public modifMetaEslo (File inputMetaFile){
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			this.docMETA = builder.parse(inputMetaFile);
			this.rootMETA = this.docMETA.getDocumentElement();						
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		modifNodeURL("dc:identifier");
		modifNodeURL("dcterms:isFormatOf");
		
		Utils.createFile(docMETA,"MetadataEsloModif/" + inputMetaFile.getName());
			
	}
	
	public void modifNodeURL(String nodeName){
		NodeList nl = this.docMETA.getElementsByTagName(nodeName);
		for (int i = 0; i<nl.getLength(); i++){
			Element nodeToModif = (Element) nl.item(i);
			String url = nodeToModif.getTextContent();
			String [] urlSplit = url.split("/");
			String baseNameWithExtension = urlSplit[urlSplit.length-1].replaceAll("_22km", "");
			String baseNameWithoutExtension = baseNameWithExtension.split("\\.")[0];
			String corpus = baseNameWithExtension.split("_")[0].toLowerCase();
			nodeToModif.setTextContent("http://modyco.inist.fr/data/eslo/" + corpus + "/" + baseNameWithoutExtension + "/" + baseNameWithExtension);
			//System.out.println("http://modyco.inist.fr/data/eslo/" + corpus + "/" + baseNameWithoutExtension + "/" + baseNameWithExtension);
		}
	}

	public static void main (String args[]) {
		File metaDir = new File("MetadataEslo");
		File [] listMeta = metaDir.listFiles();
		for(File metaFile : listMeta){
			new modifMetaEslo(metaFile);
			System.out.println(metaFile.getName());
		}
	}

}
