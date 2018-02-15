package fr.ortolang.teicorpo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

public class MoissonnageEslo {

	/** Document issu du fichier Moissonnage Eslo. */
	private Document docMetadatas;
	/** Racine du document issu du moissonnage d'Eslo. */
	private Element rootMoissonnage;
	
	/** store groups of data related together  (isKindOf & isPartOf) **/
	private Map<String, Set<String>> groups;

	public MoissonnageEslo() {
		/** Document issu du fichier Moissonnage Eslo. */
		docMetadatas = null;
		/** Racine du document issu du moissonnage d'Eslo. */
		rootMoissonnage = null;
		/** initialize groups **/
		groups = new TreeMap<String, Set<String>>();
	}

	/**
	 * décrit par la DTD "tei_corpo.dtd" Utils.TEI_CORPO_DTD .
	 * 
	 * @param File
	 *            : fichier à convertir, au format Transcriber
	 * @throws ParserConfigurationException
	 */
	public void getDocument(File inputFile) throws ParserConfigurationException {
		// Création du document moissonnage
		this.docMetadatas = null;
		this.rootMoissonnage = null;
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			this.docMetadatas = builder.parse(inputFile);
			this.rootMoissonnage = this.docMetadatas.getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// createMetaFiles();
	}

	/**
	 * décrit par la DTD "tei_corpo.dtd" Utils.TEI_CORPO_DTD .
	 * 
	 * @param InputStream
	 *            : fichier à convertir, au format Transcriber
	 * @throws ParserConfigurationException
	 */
	public void getDocument(InputStream is) throws ParserConfigurationException {
		// Création du document moissonnage
		this.docMetadatas = null;
		this.rootMoissonnage = null;
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			this.docMetadatas = builder.parse(is);
			this.rootMoissonnage = this.docMetadatas.getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// createMetaFiles();
	}

	/**
	 * décrit par la DTD "tei_corpo.dtd" Utils.TEI_CORPO_DTD .
	 * 
	 * @param String
	 *            : fichier à convertir, au format Transcriber
	 * @throws ParserConfigurationException
	 */
	public void getDocument(String uri) throws ParserConfigurationException {
		// Création du document moissonnage
		this.docMetadatas = null;
		this.rootMoissonnage = null;
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			this.docMetadatas = builder.parse(uri);
			this.rootMoissonnage = this.docMetadatas.getDocumentElement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// createMetaFiles();
	}

	public void getDataFiles(String user) throws ParserConfigurationException {
		String head  = "curl ";
		if (user != null) head += "--user " + user + " "; 
		NodeList records = this.docMetadatas.getElementsByTagName("record");
		for (int i = 0; i < records.getLength(); i++) {
			Element record = (Element) records.item(i);
			String recordName = record.getElementsByTagName("identifier").item(0).getTextContent();
			//		.split("oai:crdo.vjf.cnrs.fr:crdo-")[1];
			NodeList identifiers = record.getElementsByTagName("dc:identifier");
			for (int j = 0; j < identifiers.getLength(); j++) {
				Element identifier = (Element) identifiers.item(j);
				String identifierURL = identifier.getTextContent();
				if (identifierURL.endsWith(".wav")
						|| identifierURL.endsWith(".xml")
						|| identifierURL.endsWith(".mp3")
						|| identifierURL.endsWith(".trs")
						|| identifierURL.endsWith(".mp4")
					) {
					if (identifierURL.contains("/exist/crdo/eslo/private_eslo/"))
						identifierURL = identifierURL.replaceFirst("/exist/crdo/eslo/private_eslo/", "/exist/rest/db/corpus/eslo/private_eslo/");
					System.out.println(head + identifierURL + " > " + Utils.lastname(identifierURL) + " # " + recordName);
				}
			}

			
			NodeList dcTerms = record.getElementsByTagName("dcterms:isFormatOf");
			for(int z = 0; z<dcTerms.getLength(); z++){ 
				Element dcTerm = (Element)dcTerms.item(z);
				String dcTermURL = dcTerm.getTextContent();
				if (dcTermURL.contains("/exist/crdo/eslo/private_eslo/"))
					dcTermURL = dcTermURL.replaceFirst("/exist/crdo/eslo/private_eslo/", "/exist/rest/db/corpus/eslo/private_eslo/");
				if(dcTermURL.endsWith(".wav") 
					|| dcTermURL.endsWith(".mp3")
					) { 
					System.out.println(head + dcTermURL + " > " + Utils.lastname(dcTermURL) + " # " + recordName);
				}
			}
		}
	}

	public void createMetaFiles(String output) {
		NodeList records = this.docMetadatas.getElementsByTagName("record");
		for (int i = 0; i < records.getLength(); i++) {
			Element record = (Element) records.item(i);
			//// Ecriture des fichiers metadonnées
			record.setAttribute("xmlns", "http://www.openarchives.org/OAI/2.0/");
			record.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			record.setAttribute("xsi:schemaLocation",
					"http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd");

			String recordName = record.getElementsByTagName("identifier").item(0).getTextContent()
					.split("oai:crdo.vjf.cnrs.fr:crdo-")[1];
			String outputFileName = output + "/" + recordName + ".meta.xml";
			System.out.println("# metadata: " + outputFileName);

			Source source = new DOMSource(record);
			Result resultat = new StreamResult(outputFileName);

			try {
				// Configuration du transformer
				TransformerFactory fabrique2 = TransformerFactory.newInstance();
				Transformer transformer = fabrique2.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

				// Transformation
				transformer.transform(source, resultat);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getResumptionToken() {
		NodeList records = this.docMetadatas.getElementsByTagName("resumptionToken");
		if (records != null && records.getLength() > 0) {
			Element rt = (Element) records.item(0);
			return rt.getTextContent();
		}
		return null;
	}

	public static void main(String args[]) throws IOException, ParserConfigurationException {
		String inputName = null;
		String outputName = ".";
		String user = null;

		// parcours des arguments
		if (args.length == 0) {
			System.err.println("Vous n'avez spécifié aucun argument -: java -cp conversions.jar fr.ortolang.teicorpo.MoissonnageEslo -url URL -user USER -o output-dir.\n");
		} else {
			for (int i = 0; i < args.length; i++) {
				try {
					if (args[i].equals("-url")) {
						i++;
						inputName = args[i];
					} else if (args[i].equals("-o")) {
						i++;
						outputName = args[i];
					} else if (args[i].equals("-user")) {
						i++;
						user = args[i];
					}
				} catch (Exception e) {
					System.err.println("Problème arguments.\n");
				}
			}
		}
		
		if (inputName == null || inputName.isEmpty()) {
			System.out.printf("# Utilisation de l'adresse par défaut%n# http://cocoon.huma-num.fr/crdo_servlet/oai-pmh?verb=ListRecords&metadataPrefix=olac&set=Eslo%n");
			inputName = "http://cocoon.huma-num.fr/crdo_servlet/oai-pmh?verb=ListRecords&metadataPrefix=olac&set=Eslo";
		}

		File outFile = new File(outputName);
		if (outFile.exists()) {
			if (!outFile.isDirectory()) {
				System.out.println("\n Erreur :" + outputName
						+ " est un fichier, vous devez spécifier un nom de dossier pour le stockage des résultats. \n");
				System.exit(1);
			}
		} else {
			new File(outputName).mkdir();
		}

		int p = inputName.indexOf("ListRecords");
		String urlBase = inputName.substring(0, p + 11);
		String urlName = inputName;
		while (true) {
			MoissonnageEslo me = new MoissonnageEslo();
			System.out.println("# URL: " + urlName);
			me.getDocument(urlName);
			me.createMetaFiles(outputName);
			me.getDataFiles(user);
			
			String rt = me.getResumptionToken();
			if (rt != null) {
				System.out.println("# resumptionToken: " + rt);
				urlName = urlBase + "&resumptionToken=" + rt;
			} else {
				System.out.println("# non resumptionToken: end");
				break;
			}
		}
	}
}
