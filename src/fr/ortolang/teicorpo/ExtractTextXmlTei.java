package fr.ortolang.teicorpo;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ExtractTextXmlTei {

    public static void extractText(String cheminFichier, String cheminSortie) {

        try {
            File fichierXml = new File(cheminFichier);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fichierXml);
            doc.getDocumentElement().normalize();

            PrintWriter fileOut;
            try {
                fileOut = new PrintWriter(new FileWriter(cheminSortie));
            } catch (IOException ioe) {
                System.out.println("cannot create outputfile: " + cheminSortie);
                ioe.printStackTrace();
                return;
            }

            // --- 1. Récupération de l'attribut channel[@rend] ---
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "//profileDesc/textDesc/channel/@rend";
            String valeurRend = (String) xPath.compile(expression).evaluate(doc, XPathConstants.STRING);
            String keyElement = "p"; // default key element

            System.out.println("File: " + cheminFichier);
            // if (valeurRend != null && !valeurRend.isBlank()) {
            //     System.out.println("Valeur de channel[@rend] : " + valeurRend);
            // } else {
            //     System.out.println("L'attribut channel[@rend] n'a pas été trouvé ou est vide.");
            // }

            // System.out.println("--------------------------------------------------");

            // --- 2. Extraction des balises <xxx> situées dans <body> ---
            NodeList listeBody = doc.getElementsByTagName("body");
            if (listeBody.getLength() > 1) {
                System.out.println("Fichier TEI mal formé, plus d'un body");
                return;
            }
            // int compteurProd = 1;

            Element elemBody = (Element) listeBody.item(0);

            NodeList listeProd;
            listeProd = elemBody.getElementsByTagName("post");
            if (listeProd.getLength() > 0) {
                // System.out.println("File: " + cheminFichier);
                System.out.println("CMC file");
                keyElement = "post";
            } else {
                listeProd = elemBody.getElementsByTagName("annotationBlock");
                if (listeProd.getLength() > 0) {
                    // System.out.println("File: " + cheminFichier);
                    System.out.println("Spoken file");
                    keyElement = "annotationBlock";
                } else {
                    // System.out.println("File: " + cheminFichier);
                    System.out.println("P file");
                    listeProd = elemBody.getElementsByTagName("p");
                    keyElement = "p";
                    if (valeurRend.equals("dialog") || valeurRend.equals("dialog_ending")) {
                        // System.out.println("File: " + cheminFichier);
                        // System.out.println("Incoherence between rend and keyElement (p). Key element=p. rend set to text");
                        valeurRend = "text";
                    }
                    printTextNoDialog(listeProd, fileOut);
                    fileOut.close();
                    return;
                }
            }

            for (int j = 0; j < listeProd.getLength(); j++) {
                Element elemProd = (Element) listeProd.item(j);

                // Récupération de l'attribut "who" (renvoie "" si l'attribut n'existe pas)
                String attributWho = elemProd.getAttribute("who");
                
                // System.out.println("=== Balise <prod> n°" + compteurProd + " ===");
                // System.out.println("Attribut who : " + (attributWho.isEmpty() ? "[Aucun]" : attributWho));

                // Récupération des balises <p> enfants du keyElement courant
                NodeList listeP;
                if (keyElement.equals("post"))
                    listeP = elemProd.getElementsByTagName("p");
                else
                    listeP = elemProd.getElementsByTagName("u");
                if (valeurRend.equals("dialog") || valeurRend.equals("dialog_ending")) {
                    printTextDialog(listeP, attributWho, fileOut);
                } else {
                    printTextNoDialog(listeP, fileOut);
                }
            }

            fileOut.close();

        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du fichier XML : " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void printTextDialog(NodeList listeP, String who, PrintWriter outfile) {
        for (int k = 0; k < listeP.getLength(); k++) {
            String contenuBrut = listeP.item(k).getTextContent();
            String contenuPropre = nettoyerTexte(contenuBrut, true);

            if (!contenuPropre.isEmpty()) {
                outfile.println("[" + who + "] " + contenuPropre);
            }
        }
    }


    private static void printTextNoDialog(NodeList listeP, PrintWriter outfile) {
        for (int k = 0; k < listeP.getLength(); k++) {
            String contenuBrut = listeP.item(k).getTextContent();
            String contenuPropre = nettoyerTexte(contenuBrut, false);

            if (!contenuPropre.isEmpty()) {
                outfile.println(contenuPropre);
            }
        }
    }

    /**
     * Supprime les lignes vides, retours à la ligne superflus et espaces multiples.
     */
    private static String nettoyerTexte(String texte, boolean isDialog) {
        if (texte == null) return "";

        StringBuilder result = new StringBuilder();
        String[] lignes = texte.split("\\r?\\n");

        for (String ligne : lignes) {
            String ligneNettoyee = ligne.replaceAll("\\s+", " ").trim();
            if (!ligneNettoyee.isEmpty()) {
                if (result.length() > 0) {
                    if (isDialog)
                        result.append("\n  ");
                    else
                        result.append("\n");
                }
                result.append(ligneNettoyee);
            }
        }
        return result.toString();
    }

    public static void print_help() {
        System.err.println("Usage: java ExtractTextXmlTei input_file_name [-o output_file_name]");
        System.err.println("    If no output_file_name is provided, output_file_name will be input_file_name with .txt extension.");
    }

    public static void main(String[] args) {
        String output = "";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o")) {
                if (i< args.length-1) {
                    output = args[i+1];
                    args[i] = "";
                    args[i+1] = "";
                } else {
                    System.err.println("option -o must be followed by an argument");
                    print_help();
                    return;
                }

            }
        }
        String input = "";
        for (int i = 0; i < args.length; i++) {
            if (!args[i].isEmpty()) {
                if (!input.isEmpty()) {
                    System.err.println("the function takes only one input argument");
                    print_help();
                    return;
                }
                input = args[i];
            }
        }
        if (input.isEmpty()) {
            System.err.println("there must be one argument to the function");
            print_help();
            return;
        }
        if (output.isEmpty()) {
            if (input.contains(".")) {
                output = input.substring(0, input.lastIndexOf('.')) + ".txt";
            } else {
                output = input + ".txt";
            }
        }
        extractText(input, output);
    }
}
