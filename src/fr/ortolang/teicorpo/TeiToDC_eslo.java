package fr.ortolang.teicorpo;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import fr.ortolang.teicorpo.TeiParticipant;

/**
 DC Format:
1. TITLE  
The name given to the resource by the CREATOR or PUBLISHER.  
2. CREATOR 
The person(s) or organization(s) primarily responsible for the intellectual content of the resource; the author.  
3. SUBJECT 
The topic of the resource; also keywords, phrases or classification descriptors that describe the subject or content of the resource.  
4. DESCRIPTION 
A textual description of the content of the resource, including abstracts in the case of document-like objects; also may be a content description in the case of visual resources.  
5. PUBLISHER 
The entity responsible for making the resource available in its present form, such as a publisher, university department or corporate entity.  
6. CONTRIBUTORS 
Person(s) or organization(s) in addition to those specified in the CREATOR element, who have made significant intellectual contributions to the resource but on a secondary basis.  
7. DATE 
The date the resource was made available in its present form.  
8. TYPE 
The resource type, such as home page, novel, poem, working paper, technical report, essay or dictionary. It is expected that TYPE will be chosen from an enumerated list of types.  
9. FORMAT 
The data representation of the resource, such as text/html, ASCII, Postscript file, executable application or JPG image. FORMAT will be assigned from enumerated 
lists such as registered Internet Media Types (MIME types). MIME types are defined according to the RFC2046 standard. 
10. IDENTIFIER 
A string or number used to uniquely identify the resource. Examples from networked resources include URLs and URNs (when implemented).  
11. SOURCE 
The work, either print or electronic, from which the resource is delivered (if applicable).  
12. LANGUAGE 
The language(s) of the intellectual content of the resource.  
13. RELATION 
The relationship to other resources. Formal specification of RELATION is currently under development.  
14. COVERAGE 
The spatial locations and temporal duration characteristics of the resource. Formal specification of COVERAGE is also now being developed.  
15. RIGHTS MANAGEMENT 
A link (URL or other suitable URI as appropriate) to a copyright notice, a rights-management statement or perhaps a server that would provide such information in a dynamic way.
 */

public class TeiToDC_eslo{

	TeiFile tf;
	File teiFile;
	Document dcDoc;
	Element dcRoot;
	DC dc;

	public static String EXT = ".xml";

	TeiToDC_eslo(String inputFileName, String outputFileName){
		teiFile = new File(inputFileName);
		tf = new TeiFile(teiFile, null);
		dc = new DC();

		//Remplissage DC
		////TITLE
		dc.title = tf.transInfo.medianame.split("\\.")[0];

		////CREATORS
		dc.creators.add("Projet Eslo");
		dc.creators.add("http://eslo.huma-num.fr/");
		//String titleToLowerCase = dc.title.toLowerCase(); // Dans cas où distinction eslo1 / eslo2
		
		////DESCRIPTIONS
		dc.descriptions.addAll(tf.transInfo.situationList);

		////SUBJECT
		dc.subject = "Spontaneous Language";

		////PUBLISHERS
		dc.publishers.add("Projet Eslo");
		dc.publishers.add("Centre Orléanais de Recherche en Anthropologie et Linguistique");

		/////CONTRIBUTORS
		dc.contributors = tf.transInfo.participants;

		////DATE
		dc.date = dateConversion(TeiToClan.trsDateToChatDate(tf.transInfo.date));

		////TYPES
		dc.types.add("Spontaneous language");
		dc.types.add("Linked Transcription");
		dc.types.add("Audio Recording");

		////FORMATS
		dc.formats.add("TRANSCRIBER/trs");
		dc.formats.add("TEI/teiml");
		dc.formats.add("audio/wav");

		////IDENTIFIER
		try{
			//URL du dossier contenant les données
			dc.identifier = "http://modyco.inist.fr/" + new File(tf.transInfo.fileLocation).getParent().split("/applis/")[1];
		}
		catch(Exception e){
			dc.identifier = "";
		}

		////SOURCE
		dc.source = "Projet Eslo";

		////LANGUAGE
		dc.language = "fr";

		////COVERAGE
		dc.coverage = "Orléans";
		
		////RIGHTS
		dc.right.add("Utilisation libre dans le cadre de recherches scientifiques.");
		dc.right.add("Licence non modifiable.");
		

		//Création de l'objet Document dcDoc
		DocumentBuilderFactory factory = null;
		try {
			factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Utils.setDTDvalidation(factory, true);
			dcDoc = builder.newDocument();
			dcRoot = dcDoc.createElement("metadata");
			dcRoot.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
			dcDoc.appendChild(dcRoot);
		}catch (Exception e) {
			e.printStackTrace();
		}

		//Construction du document
		buildDC();

		//Ecriture du fichier xml
		createFile(outputFileName);
	}

	public void buildDC(){
		//Construction des éléménts inhérents au format Dublin Core
		//////Title
		buildDcNode("dc:title", dc.title);

		//////Creators		
		buildDcNodes("dc:creator", dc.creators);		

		////Subject
		buildDcNode("dc:subject", dc.subject);

		//////Descriptions		
		buildDcNodes("dc:description", dc.descriptions);		

		//////Publishers
		buildDcNodes("dc:publisher", dc.publishers);

		/////CONTRIBUTOR
		for(int i = 0; i<dc.contributors.size(); i++){
			buildDcNode("dc:contributor", dc.contributors.get(i).name);
		}

		/////DATE
		buildDcNode("dc:date", dc.date);

		/////TYPE
		buildDcNodes("dc:type", dc.types);

		/////FORMAT
		buildDcNodes("dc:format", dc.formats);

		/////IDENTIFIER
		buildDcNode("dc:identifier", dc.identifier);

		/////SOURCE
		buildDcNode("dc:source", dc.source);

		/////LANGUAGE
		buildDcNode("dc:language", dc.language);

		/////COVERAGE
		buildDcNode("dc:coverage", dc.coverage);
		
		/////RIGHTS
		buildDcNodes("dc:rights", dc.right);
	}	

	public void buildDcNode(String dcNodeName, String dcNodeContent){
		if(Utils.isNotEmptyOrNull(dcNodeContent)){
			Element dcElement = dcDoc.createElement(dcNodeName);
			dcElement.setTextContent(dcNodeContent);
			dcRoot.appendChild(dcElement);
		}
		System.out.println(dcNodeName.toUpperCase() + " ::: " + dcNodeContent);
	}

	public void buildDcNodes(String dcNodeName, ArrayList<String> contents){
		for(int i = 0; i<contents.size(); i++){
			buildDcNode(dcNodeName, contents.get(i));
		}
	}

	public class DC{
		String title;
		ArrayList<String> creators = new ArrayList<String>();
		ArrayList<String> descriptions = new ArrayList<String>();
		String subject;
		ArrayList<String> publishers = new ArrayList<String>();
		ArrayList<TeiParticipant> contributors = new ArrayList<TeiParticipant>();
		String date;
		ArrayList<String> types = new ArrayList<String>();
		ArrayList<String> formats = new ArrayList<String>();
		String identifier;
		String source;
		String language;
		String relation;
		String coverage;
		ArrayList<String> right = new ArrayList<String>();
	}

	public void createFile(String outputFileName) {
		Source source = new DOMSource(this.dcDoc);
		Result result = new StreamResult(outputFileName);
		try {
			// Configuration du transformer 
			TransformerFactory fabrique2 = TransformerFactory.newInstance();
			Transformer transformer = fabrique2.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			// Transformation
			transformer.transform(source, result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//Conversion de date du format Chat : JJ-MMM-AAAA au format imposé par DC : AAAA‑MM‑JJ
	public static String dateConversion(String date){
		Pattern p = Pattern.compile("([0-9]{2})-([A-Z]{3})-([0-9]{4})");
		Matcher m = p.matcher(date);		
		if(m.find()){
			date = m.group(3) + "-" + Utils.convertMonthStringToInt(m.group(2)) + "-" + m.group(1);
		}
		return date;
	}

	public static void usage() {
		System.err.println("Description: TeiToDC_eslo génère un fichier de métadonnées au format DublinCore à partir du format TEI des transcriptions issues des corpus ESLO");
		System.err.println("Usage: TeiToDC_eslo [-options] <file.teiml>");
		System.err.println("	     :-i nom du fichier ou repertoire où se trouvent les fichiers Tei (les fichiers ont pour extension .teiml");
		System.err.println("	     :-o nom du fichier de sortie au format DC (.xml) ou du repertoire de résultats");
		System.err.println("	     	si cette option n'est pas spécifiée, le fichier de sortie aura le même nom que le fichier d'entrée, avec l'extension .xml;");
		System.err.println("	     	si on donne un repertoire comme input et que cette option n'est pas spécifiée, les résultats seront stockées dans le même dossier que l'entrée.");
		System.err.println("	     :-usage ou -help = affichage de ce message");
		System.exit(1);
	}


	public static void main (String[] args) throws Exception{
		String input = null;
		String output = null;
		// parcours des arguments
		if (args.length == 0) {
			System.err.println("Vous n'avez spécifié aucun argument.\n");
			usage();
		}
		else {
			for (int i = 0; i < args.length; i++) {
				try {
					if (args[i].equals("-i")) {
						i++;
						if (i >= args.length) usage();
						input = args[i];
					} else if (args[i].equals("-o")) {
						i++;
						if (i >= args.length) usage();
						output = args[i];
					}
				}
				catch(Exception e){
					usage();
				}
			}
		}
		
		File f = new File(input);
		input = f.getCanonicalPath();

		if (f.isDirectory()){
			File[] chatFiles = f.listFiles();
			if (output == null){
				if (input.endsWith("/")){
					output= input.substring(0, input.length()-1);
				}
				else{
					output = input;
				}
			}

			File outFile = new File(output);
			if(outFile.exists()){
				if(!outFile.isDirectory()){
					System.out.println("\n Erreur :"+ output + " est un fichier, vous devez spécifier un nom de dossier pour le stockage des résultats. \n");
					usage();
					System.exit(1);
				}
			}

			if(!output.endsWith("/")){
				output += "/";
			}
			new File(output).mkdir();
			
			for (File file : chatFiles){
				if(file.getName().endsWith(".teiml")){
					String outputFileName = file.getName().split("\\.")[0] + EXT;
					System.out.println("Nouveau fichier de métadonnées: " + output + outputFileName);
					new TeiToDC_eslo(file.getAbsolutePath(), output + outputFileName);
				}
				else if(file.isDirectory()){
					args[0] = "-i";
					args[1] = file.getAbsolutePath();
					main(args);
				}
			}
		}
		else{
			if (output == null) {
				output = input.split("\\.")[0] + EXT;
			}
			else if(new File(output).isDirectory()){
				if(output.endsWith("/")){
					output = output + input.split("\\.")[0] + EXT;
				}
				else{
					output = output + "/" + input.split("\\.")[0] + EXT;
				}
			}

			if (!(Utils.validFileFormat(input, "teiml")) && Utils.validFileFormat(output, EXT)) {
				System.err.println("Le fichier d'entrée du programme doit avoir l'extension .teiml"
						+ "\nLe fichier de sortie du programme doit avoir l'extension "+ EXT);
			}
			System.out.println(input);
			new TeiToDC_eslo(input, output);
			System.out.println("Nouveau fichier de métadonnées " + output);
		}
	}
}
