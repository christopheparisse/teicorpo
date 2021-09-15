/**
 * TeiCorpo permet de convertir des fichiers de transcriptio au format TEI et inversement
 * @author Christophe Parisse
 *
 */

package fr.ortolang.teicorpo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TeiCorpo extends GenericMain {
	public static boolean containsExtension(String fn, String ext) {
		if (fn.endsWith(ext)) {
			return true;
		}
		return false;
	}
	
	public static void process(String extIn, String extOut, String fileIn, String fileOut, TierParams tp)
			throws FileNotFoundException {
		// System.err.printf("Process: extIn: <%s> extOut: <%s> fileIn: <%s> fileOut: <%s>%n", extIn, extOut, fileIn, fileOut);
		
		// on regarde le type de la sortie
		if (fileOut != null && !fileOut.isEmpty()) {
			// avoid conversions on the same file
			if (fileIn.equals(fileOut)) {
				System.err.printf("same file ignored : %s%n", fileIn);
				return;
			}
			extOut = extensions(extOut);
			if (extOut.equals(".none")) {
				// format output est déterminé par l'extension du fichier
				String ext = Utils.extname(fileOut);
				if (!ext.isEmpty()) {
					extOut = extensions(ext.substring(1));
				}
				if (extOut.equals(".none")) {
					System.err.printf("cannot guess output format for %s %s%n", extOut, fileOut);
					return;
				}
			}
		} else if (extOut != null && !extOut.isEmpty()) {
			extOut = extensions(extOut);
		} else
			extOut = Utils.EXT;
		
		// on regarde le type de l'entrée
		if (fileIn != null && !fileIn.isEmpty()) {
			extIn = extensions(extIn);
			if (extIn.equals(".none")) {
				// format input est déterminé par l'extension du fichier
				String ext = Utils.extname(fileIn);
				if (!ext.isEmpty()) {
					extIn = extensions(ext.substring(1));
				}
				if (extIn.equals(".none")) {
					System.err.printf("cannot guess input format for %s %s%n", extIn, fileIn);
					return;
				}
			}
		} else if (extIn != null && !extIn.isEmpty()) {
			extIn = extensions(extIn);
		} else
			extIn = Utils.EXT;
		
		if (fileOut == null || fileOut.isEmpty()) {
			if (extIn.equals(extOut)) {
				System.err.printf("ignored %s%n", fileIn);
				return;
			}
		}
		
		String outputTEI = Utils.fullbasename(fileIn) + Utils.EXT;
		tp.outputTEIName = outputTEI;
		// creates a true temp file.
		String tempTEI;
		try {
			//create a temp file
			File temp = File.createTempFile("teitempfile", ".tei_corpo.xml");
			tempTEI = temp.getAbsolutePath();
    	} catch(IOException e){
    		tempTEI = Utils.pathname(fileIn) + "/tei" + Long.valueOf(new Date().getTime()) + Utils.EXT;
    	}

		// first from input to TEI
		if (extIn.equals(Utils.EXT)) {
			// nothing but rename file to tempTEI if different names
			// only necessary to change value of tempTEI variable
			tempTEI = fileIn;
		} else {
			switch(extIn) {
			case ".cha":
			case ".srt":
			case ".txt":
				ClanToTei tc = new ClanToTei();
				tc.mainProcess(fileIn, tempTEI, tp);
				break;
			case ".eaf":
				ElanToTei te = new ElanToTei();
				te.mainProcess(fileIn, tempTEI, tp);
				break;
			case ".trs":
				TranscriberToTei tt = new TranscriberToTei();
				tt.mainProcess(fileIn, tempTEI, tp);
				break;
			case ".textgrid":
				PraatToTei tpr = new PraatToTei();
				tpr.mainProcess(fileIn, tempTEI, tp);
				break;
			default:
				// file ignored
				System.err.printf("Unknown input format: internal error.%n");
				return;
			}
		}
		
		// then from TEI to output
		if (extOut.equals(Utils.EXT)) {
			// nothing but rename temp file
			if (fileOut == null || fileOut.isEmpty() || fileOut == outputTEI) {
				try {
					Path of = Paths.get(outputTEI);
					Path tmp = Paths.get(tempTEI);
				    Files.move(tmp, of,
				            StandardCopyOption.REPLACE_EXISTING);
				    if (tp.verbose) System.out.println("Moved to: " + outputTEI);
				} catch (IOException e) {
				    //moving file failed.
					System.out.println("Cannot move to: " + outputTEI);
				}
				/*
				File of = new File(outputTEI);
				// of.delete(); // if exist before.
				File tmp = new File(tempTEI);
				if (tmp.renameTo(of)) {
					if (tp.verbose) System.out.println("Renamed to: " + outputTEI);
				} else {
					System.out.println("Cannot rename to: " + outputTEI);
				}
				*/
			} else {
				try {
					Path of = Paths.get(fileOut);
					Path tmp = Paths.get(tempTEI);
				    Files.move(tmp, of,
				            StandardCopyOption.REPLACE_EXISTING);
					if (tp.verbose) System.out.println("Moved to: " + fileOut);
				} catch (IOException e) {
				    //moving file failed.
					System.out.println("Cannot move to: " + fileOut);
				}
				/*
				File of = new File(fileOut);
				// of.delete(); // if exist before.
				File tmp = new File(tempTEI);
				if (tmp.renameTo(of)) {
					if (tp.verbose) System.out.println("Renamed to: " + fileOut);
				} else {
					System.out.println("Cannot rename to: " + fileOut);
				}
				*/
			}
		} else {
			// System.out.println("toLexico: " + extOut);
			switch(extOut) {
			case ".cha":
				TeiToClan tc = new TeiToClan();
				tc.mainProcess(tempTEI, fileOut, tp);
				break;
			case ".eaf":
				TeiToElan te = new TeiToElan();
				te.mainProcess(tempTEI, fileOut, tp);
				break;
			case ".trs":
				TeiToTranscriber tt = new TeiToTranscriber();
				tt.mainProcess(tempTEI, fileOut, tp);
				break;
			case ".textgrid":
				TeiToPraat tpr = new TeiToPraat();
				tpr.mainProcess(tempTEI, fileOut, tp);
				break;
			case ".txm.xml":
				TeiToTxm txm = new TeiToTxm();
				txm.mainProcess(tempTEI, fileOut, tp);
				break;
			case ".lex.txt":
				TeiToLexico tlx = new TeiToLexico();
				tlx.mainProcess(tempTEI, fileOut, tp);
				break;
			case ".srt":
				TeiToSrt tsr = new TeiToSrt();
				tsr.mainProcess(tempTEI, fileOut, tp);
				break;
			case ".subt.html":
				TeiToSubtHtml tsh = new TeiToSubtHtml();
				tsh.mainProcess(tempTEI, fileOut, tp);
				break;
			case ".txt":
				TeiToText txt = new TeiToText();
				txt.mainProcess(tempTEI, fileOut, tp);
				break;
			default:
				System.err.printf("Format not implemented in TeiCorpo: %s%n", extOut);
				break;
			}
			File of = new File(tempTEI);
			// of.delete(); // delete temp file
		}
	}

	public static String extensions(String format) {
		//System.err.printf("ext: (%s)%n", format);
		String lcFormat = format.toLowerCase();
		if (lcFormat.startsWith(".")) lcFormat = lcFormat.substring(1);
		switch(lcFormat) {
			case "cha":
			case "chat":
			case "clan":
			case "cex":
				return ".cha";
			case "trs":
			case "trs.xml":
			case "transcriber":
				return ".trs";
			case "irq.txt":
			case "iramuteq":
				return ".irq.txt";
			case "txm.xml":
			case "txm":
				return ".txm.xml";
			case "srt":
			case "soustitres":
				return ".srt";
			case "subthtml":
			case "subt.html":
				return ".subt.html";
			case "lex.txt":
			case "tmr.txt":
			case "letrameur":
			case "lexico":
				return ".lex.txt";
			case "txt":
			case "text":
				return ".txt";
			case "eaf":
			case "elan":
				return ".eaf";
			case "textgrid":
			case "praat":
				return ".textgrid";
			case "teicorpo.xml":
			case "teicorpo":
			case "tei_corpo.xml":
			case "tei_corpo":
			case "tei":
			case "xml":
			case "trjs":
				return Utils.EXT;
			case "ttg":
			case "perceo":
				return lcFormat;
			case "dinlang":
				return lcFormat;
		}
		return ".none";
	}

	public static void main(String[] args) throws Exception {
		TierParams.printVersionMessage();
		String usageString = "Description: TeiCorpo converts one file from one format to another%n"
				+ "Usage: TeiCorpo [-options] <file>%n"
				+ "Known formats: TEI_CORPO, Clan, Elan, Praat, Transcriber%n"
				+ "Output format TEI_CORPO by default%n";
		TeiCorpo tc = new TeiCorpo();
		tc.mainCommand(args, "", "", usageString, 4);
		System.exit(0);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
//		System.err.println("mainProcess: " + input + " | " + output);
		try {
			process(options.inputFormat, options.outputFormat, input, output, options);
		} catch(FileNotFoundException e) {
			System.err.println("Le fichier " + input + " n'existe pas.");
			System.exit(1);
		} catch(Exception e) {
			System.err.println("Impossible de traiter le fichier: " + input);
			System.err.println("Erreur logicielle " + e.toString());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
