/**
 * @author Christophe Parisse
 * TeiEdit : change some elements in the TEI file
**/

package fr.ortolang.teicorpo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.ortolang.teicorpo.TeiFile.Div;

public class TeiEdit extends GenericMain {

	// Document TEI à lire
	public Document teiDoc;
	// acces Xpath
	public XPathFactory xPathfactory;
	public XPath xpath;
	// paramètres
	public TierParams optionsOutput;
	// Nom du fichier teiml à convertir
	String inputName;
	// Nom du fichier de sortie
	String outputName;
	// Validation du document Tei par la dtd
	boolean validation = false;
	// racine du fichier teiDoc
	Element root;
	
	// resultat
	boolean ok;

	public void transform(String pInputName, String pOutputName, TierParams optionsTei) {
		optionsOutput = optionsTei;
		inputName = pInputName;
		outputName = pOutputName;
		DocumentBuilderFactory factory = null;
		root = null;
		ok = false;

		File inputFile = new File(inputName);
		if (!inputFile.exists()) {
			System.err.printf("%s n'existe pas: pas de traitement%n", inputName);
			return;
		}

		try {
			factory = DocumentBuilderFactory.newInstance();
			Utils.setDTDvalidation(factory, validation);
			DocumentBuilder builder = factory.newDocumentBuilder();
			teiDoc = builder.parse(inputFile);
			root = teiDoc.getDocumentElement();
			xPathfactory = XPathFactory.newInstance();
			xpath = xPathfactory.newXPath();
			xpath.setNamespaceContext(new NamespaceContext() {
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

				public Iterator<?> getPrefixes(String val) {
					return null;
				}

				public String getPrefix(String uri) {
					return null;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		process();
	}

	// Conversion du fichier teiml
	public void process() {
		for (String cmd: optionsOutput.commands) {
			if (cmd.equals("replace"))
				continue;
			if (cmd.startsWith("media=")) {
				String media = cmd.substring(6);
				int nth = 0;
				if (media.startsWith(":")) {
					int p = media.substring(1).indexOf(":");
					if (p>0) {
						String num = media.substring(1, p+1);
						int numint = Integer.parseInt(num);
						if (numint >= 0 && numint <= 10) {
							System.out.printf("Changement du media n°:  %d%n", numint);
							nth = numint;
							media = media.substring(p+2);
						}
					}
				}
				System.out.printf("Changement du media : %s%n", media);
				NodeList recStmt = teiDoc.getElementsByTagName("recordingStmt");
				if (recStmt != null && recStmt.getLength() > 0) {
					NodeList medias = ((Element)recStmt.item(0)).getElementsByTagName("media");
					if (medias != null && medias.getLength() > 0) {
						if (medias.getLength()-1 < nth)
							System.out.printf("Pas de changement - le media n°:  %d n'existe pas%n", nth);
						else {
							Element m = (Element) medias.item(nth);
							m.setAttribute("url", media);
						}
					}
				}
			}
			if (cmd.startsWith("mimetype=")) {
				String media = cmd.substring(9);
				int nth = 0;
				if (media.startsWith(":")) {
					int p = media.substring(1).indexOf(":");
					if (p>0) {
						String num = media.substring(1, p+1);
						int numint = Integer.parseInt(num);
						if (numint >= 0 && numint <= 10) {
							System.out.printf("Changement du media type n°:  %d%n", numint);
							nth = numint;
							media = media.substring(p+2);
						}
					}
				}
				System.out.printf("Changement du media type : %s%n", media);
				NodeList recStmt = teiDoc.getElementsByTagName("recordingStmt");
				if (recStmt != null && recStmt.getLength() > 0) {
					NodeList medias = ((Element)recStmt.item(0)).getElementsByTagName("media");
					if (medias != null && medias.getLength() > 0) {
						if (medias.getLength()-1 < nth)
							System.out.printf("Pas de changement - le media n°:  %d n'existe pas%n", nth);
						else {
							Element m = (Element) medias.item(nth);
							m.setAttribute("mimeType", media);
							m.removeAttribute("mediaType"); // si cet attribut existe.
						}
					}
				}
			}
			if (cmd.startsWith("docname=")) {
				String name = cmd.substring(8);
				Utils.setDocumentName(teiDoc, name);
			}
			if (cmd.startsWith("chgtime=")) {
				String param = cmd.substring(8);
				double difftime;
				try {
					difftime = Double.parseDouble(param);
				} catch(Exception e) {
					System.err.printf("chgtime: mauvais valeur de secondes : %s>%n", param);
					continue;
				}
				if (difftime < -100.0 && difftime > 100.0) {
					System.out.printf("Trop grande modification de temps %f%n", difftime);
					continue;
				}
				System.out.printf("Modification du temps de %f%n", difftime);
				NodeList timeline = teiDoc.getElementsByTagName("timeline");
				if (timeline != null && timeline.getLength() > 0) {
					String unit = ((Element)timeline.item(0)).getAttribute("unit");
					double ratio = 1.0;
					if (unit != null && !unit.equals("s")) {
						if (unit.equals("ms"))
							ratio = 1000.0;
						else {
							System.out.println("ATTENTION: Unité inconnue dans le fichier TEI: attention à ajuster la valeur utilisée dans l'appel de la commande");
						}
					}
					difftime = difftime * ratio;
					NodeList whens = ((Element)timeline.item(0)).getElementsByTagName("when");
					if (whens != null && whens.getLength() > 0) {
						/*
						Element m = (Element) whens.item(0);
						m.setAttribute("absolute", Double.toString(difftime));
						*/
						for (int i=0; i < whens.getLength(); i++) {
							Element m = (Element) whens.item(i);
							String attr = m.getAttribute("interval");
							if (attr != null) {
								double drel;
								try {
									drel = Double.parseDouble(attr);
								} catch(Exception e) {
									continue;
								}
								m.setAttribute("interval", Double.toString(drel + difftime));
							} else {
								attr = m.getAttribute("absolute");
								if (attr != null) {
									double dabs;
									try {
										dabs = Double.parseDouble(attr);
									} catch(Exception e) {
										continue;
									}
									if (dabs != 0.0)
										m.setAttribute("absolute", Double.toString(dabs + difftime));
								}
							}
						}
					}
				}
			}
		}
		ok = true;
	}

	// Création du fichier de sortie
	public void createOutput() {
		File itest = new File(inputName);
		File otest = new File(outputName);
		// System.out.printf("Edit: %s %s%n", inputName, outputName);
		// return;
		String itname;
		String otname;
		try {
			itname = itest.getCanonicalPath();
			otname = otest.getCanonicalPath();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		System.out.printf("Edit: %s %s%n", itname, otname);
		
		if (itname.equals(otname) && !optionsOutput.commands.contains("replace")) {
			System.err.println("Le fichier sortie est le même que le fichier entrée: utiliser le paramètre -c replace pour remplacer le fichier");
			return;
		}

		Source source = new DOMSource(teiDoc);
		Result resultat = new StreamResult(outputName);

		try {
			TransformerFactory fabrique2 = TransformerFactory.newInstance();
			Transformer transformer = fabrique2.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, Utils.teiCorpoDtd());
			transformer.transform(source, resultat);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * remplacement d'un fichier par lui-même
	 * option -c replace --> ne pas utiliser _chg et mettre sameFile à vrai.
	 */

	// Programme principal
	public static void main(String args[]) throws IOException {
		TierParams.printVersionMessage();

		String usageString = "Description: TeiEdit permet de modifier des éléments d'un fichier Tei.%nUsage: TeiEdit -c command [-options] <"
				+ Utils.EXT + ">%n";
		TeiEdit ttc = new TeiEdit();
		ttc.mainCommand(args, Utils.EXT, ".chg.xml", usageString, 3);
	}
//			System.out.println("Reading " + input);
//			TeiEdit ttt = new TeiEdit(new File(input).getAbsolutePath(), output, options);
//			if (ttt.ok) {
//				ttt.createOutput();
//				System.out.println("File modified " + output);
//			}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		transform(input, output, options);
		if (ok) {
			System.out.println(output);
			createOutput();
		}
	}
}
