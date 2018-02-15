/**
 * Conversion d'un fichier TEI en un fichier SRT.
 * @author Myriam Majdoub
 */
package fr.ortolang.teicorpo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
//import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.ortolang.teicorpo.AnnotatedUtterance;
import fr.ortolang.teicorpo.TeiFile.Div;

public class TeiToSubtHtml extends TeiConverter{

	//Permet d'écrire le fichier de sortie
	private PrintWriter out;
	//Encodage du fichier de sortie
	final static String outputEncoding = "UTF-8";
	//Extension du fichier de sortie
	final static String EXT = ".subt.html";

	/**
	 * Convertit le fichier TEI donné en argument en un fichier Srt.
	 * @param inputName	Nom du fichier d'entrée (fichier TEI, a donc l'extenstion .teiml)
	 * @param outputName	Nom du fichier de sortie (fichier SRT, a donc l'extenson .srt)
	 */
	public void transform(String inputName, String outputName, TierParams optionsTei) {
		init(inputName, outputName, optionsTei);
		if (this.tf == null) return;
		optionsOutput = optionsTei;
		outputWriter();
		conversion();
	}

	/**
	 * Ecriture de l'output
	 */
	public void outputWriter(){
		out = null;
		try{
			FileOutputStream of = new FileOutputStream(outputName);
			OutputStreamWriter outWriter = new OutputStreamWriter(of, outputEncoding );
			out = new PrintWriter(outWriter, true);
		}
		catch(Exception e){
			out = new PrintWriter(System.out, true);
		}
	}

	/**
	 * Conversion
	 */
	public void conversion() {
		//System.out.println("Conversion (" + (Params.forceEmpty?"true":"false") + ") (" + Params.partDisplay + ") (" + Params.tierDisplay + ")");
		//Etapes de conversion
	    // Remove the extension.
		// System.out.println(tf.transInfo.recordName);
		String filename;
		String inputFileName = new File(inputName).getName();
	    int extensionIndex = inputFileName.lastIndexOf(".");
	    if (extensionIndex == -1)
	        filename = inputFileName;
	    else
	    	filename = inputFileName.substring(0, extensionIndex);
		if (filename.endsWith("-240p"))
			filename = filename.substring(0, filename.length()-5);
		else if (filename.endsWith("-480p"))
			filename = filename.substring(0, filename.length()-5);
		else if (filename.endsWith("-480p"))
			filename = filename.substring(0, filename.length()-5);
		else if (filename.endsWith("-720p"))
			filename = filename.substring(0, filename.length()-5);
		else if (filename.endsWith("-1020p"))
			filename = filename.substring(0, filename.length()-6);
		buildHeader(inputName, filename);
		buildText();
        /* terminate file */
        out.println( "</p></div></div></body></html>" );
       /* end of terminate file */
	}

	/**
	 * ecriture de l'entete des fichier html en fonction du fichier TEI à convertir
	 */
	public void buildHeader(String title, String media) {
        String cssLayout = "/tools/subt/layout.css";
        String jsLocation = "/tools/subt/timesheets.js";

        /* initialise head file */
        out.println( "<html xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:toc=\"http://www.tei-c.org/ns/teioc\" lang=\"en\">" );
        out.println( "<head>" );
        out.println( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" );
        out.println( "<meta charset=\"utf-8\">" );
        out.printf( "<title>%s</title>%n", title );
        out.printf( "<link rel=\"stylesheet\" type=\"text/css\" href=\"%s\">%n", cssLayout );
        out.println( "<script type=\"text/javascript\" src=\"" + jsLocation + "\"></script>" );
        out.println( "<script type=\"text/javascript\">" );
        out.println( "EVENTS.onSMILReady(function() {" );
        out.println( "var mediaContainer  = document.getElementById(\"media\");" );
        out.println( "var mediaController = document.getElementById(\"mediaController\");" );
        out.println( "var mediaPlayer = mediaContainer.timing.mediaSyncNode;" );
        out.println( "if (mediaPlayer && mediaPlayer.mediaAPI) {" );
        out.println( "// the video is displayed in a Flash/Silverlight element" );
        out.println( "mediaPlayer = mediaPlayer.mediaAPI;" );
        out.println( "mediaContainer.onclick = function() { // single click = play/pause" );
        out.println( "if (mediaPlayer.paused)" );
        out.println( "mediaPlayer.play();" );
        out.println( "else" );
        out.println( "mediaPlayer.pause(); };" );
        out.println( "mediaContainer.ondblclick = function() { // double click = restart" );
        out.println( "mediaPlayer.setCurrentTime(0);" );
        out.println( "mediaPlayer.play(); }; }" );
        out.println( "else {" );
        out.println( "// the video is displayed in a native HTML element" );
        out.println( "mediaController.style.display = \"none\"; } });" );
        out.println( "window.addEventListener('blur', function() {" );
        out.println( "	//alert('lost focus');" );
        out.println( "	var mediaContainer  = document.getElementById(\"media\");" );
        out.println( "	var mediaController = document.getElementById(\"mediaController\");" );
        out.println( "	var mediaPlayer = mediaContainer.timing.mediaSyncNode;" );
        out.println( "	mediaPlayer.pause();" );
        out.println( "});" );
        out.println( "</script>" );
        out.println( "</head>" );
        out.println( "<body>" );
        out.println( "<div id=\"demo\">" );
        out.println( "<div id=\"media\" class=\"highlight\" data-timecontainer=\"excl\" data-timeaction=\"display\">" );
        out.println( "<video data-syncmaster=\"true\" data-timeaction=\"none\" controls=\"controls\" preload=\"auto\">" );
        out.printf( "<source type=\"video/mp4\" src=\"%s-480p.mp4\"></source><br/>%n", media );
        out.printf( "<source type=\"video/webm\" src=\"%s-480p.webm\"></source>%n", media );
        out.printf( "<source type=\"video/mp4\" src=\"%s-240p.mp4\"></source><br/>%n", media );
        out.printf( "<source type=\"video/ogg\" src=\"%s-240p.ogv\"></source>%n", media );
        out.printf( "<source type=\"video/mp4\" src=\"%s.mp4\"></source><br/>%n", media );
        out.printf( "<source type=\"video/webm\" src=\"%s.webm\"></source>%n", media );
        out.printf( "<source type=\"video/ogg\" src=\"%s.ogv\"></source>%n", media );
        out.println( "This page requires <strong>&lt;video&gt;</strong> support:<br/>" );
        out.println( "best viewed with Firefox&nbsp;3.5+, Safari&nbsp;4+, Chrome&nbsp;5+, Opera&nbsp;10.60+ or IE9.<br/><br/>" );
        out.println( "Internet Explorer users, please enable Flash or Silverlight." );
        out.println( "</video>" );
        /* end of head file */
        printHeader("0.0", "1.0", inputName, media);
	}
	
	public void printLine(String startTime, String endTime, String loc, String speechContent) {
        if ( Utils.isNotEmptyOrNull(startTime) ) {
   			out.println("</p>");
            if ( speechContent.length() > 75 )
                out.printf( "<p class=\"twolines\" data-begin=\"%d:%d.%d\" data-end=\"%d:%d.%d\"><span class=\"nutt\">%s:</span> %s%n",
                	TimeDivision.toMinutes(startTime), TimeDivision.toSeconds(startTime), TimeDivision.toCentiSeconds(startTime), 
                	TimeDivision.toMinutes(endTime), TimeDivision.toSeconds(endTime), TimeDivision.toCentiSeconds(endTime),
                	loc, speechContent );
            else                          
                out.printf((optionsOutput.doDisplay.size()==0?"<p " : "<p class=\"twolines\"") + "data-begin=\"%d:%d.%d\" data-end=\"%d:%d.%d\"><span class=\"nutt\">%s:</span> %s%n",
                	TimeDivision.toMinutes(startTime), TimeDivision.toSeconds(startTime), TimeDivision.toCentiSeconds(startTime), 
                	TimeDivision.toMinutes(endTime), TimeDivision.toSeconds(endTime), TimeDivision.toCentiSeconds(endTime),
                    loc, speechContent );
        } else {
        	printContinuation( loc, speechContent );
        }
	}

	public void printHeader(String startTime, String endTime, String loc, String speechContent) {
        if ( Utils.isNotEmptyOrNull(startTime) ) {
   			out.println("</p>");
            out.printf( "<p class=\"header\" data-begin=\"%d:%d.%d\" data-end=\"%d:%d.%d\"><span class=\"nutt\">%s</span> %s%n",
            	TimeDivision.toMinutes(startTime), TimeDivision.toSeconds(startTime), TimeDivision.toCentiSeconds(startTime), 
            	TimeDivision.toMinutes(endTime), TimeDivision.toSeconds(endTime), TimeDivision.toCentiSeconds(endTime),
            	loc, speechContent );
        } else {
        	printContinuation( loc, speechContent );
        }
	}
	
	public void printContinuation(String loc, String speechContent) {
        out.printf( "</br><span class=\"nutt\">%s</span>: %s%n", loc, speechContent );
	}
	
	/**
	 * Ecriture de la partie transcriptions: énoncés + tiers
	 */
	public void buildText(){
		ArrayList<TeiFile.Div> divs = tf.trans.divs;
		for(Div d : divs){
			for(AnnotatedUtterance u : d.utterances){
				if(Utils.isNotEmptyOrNull(u.type)){
					if (!u.start.isEmpty()) {
						float start = Float.parseFloat(u.start);
						if (start < 1.0) // c'est avant l'affichage du titre du morceau
							start = (float)1.0;
						String [] splitType = u.type.split("\t");
						try{
							String theme = Utils.cleanString(tf.transInfo.situations.get(splitType[1]));
							printHeader(Float.toString(start), Float.toString(start+1), splitType[0], theme);
						}
						catch(ArrayIndexOutOfBoundsException e){
							printHeader(Float.toString(start), Float.toString(start+1), splitType[0], "");
						}
					}
				}
				writeUtterance(u);
			}
		}
	}

	/**
	 * Ecriture d'un énonce: lignes qui commencent par le symbole étoile *
	 * @param loc	Locuteur
	 * @param speechContent	Contenu de l'énoncé
	 * @param startTime	Temps de début de l'énoncé
	 * @param endTime	Temps de fin de l'énoncé
	 */
	public void writeSpeech(String loc, String speechContent, String startTime, String endTime){
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(loc)) return;
			if (!optionsOutput.isDoDisplay(loc)) return;
		}
		//System.out.println(loc + ' ' + startTime + ' ' + endTime +' ' + speechContent);
		//Si le temps de début n'est pas renseigné, on mettra par défaut le temps de fin (s'il est renseigné) moins une seconde.
		if(!Utils.isNotEmptyOrNull(startTime)){
			if(Utils.isNotEmptyOrNull(endTime)){
				float start = Float.parseFloat(endTime) - 1;
				startTime = Float.toString(start);
			}
		}
		//Si le temps de fin n'est pas renseigné, on mettra par défaut le temps de début (s'il est renseigné) plus une seconde.
		else if(!Utils.isNotEmptyOrNull(endTime)){
			if(Utils.isNotEmptyOrNull(startTime)){
				float end = Float.parseFloat(startTime) + 1;
				endTime = Float.toString(end);
			}
		}

		//On ajoute les informations temporelles seulement si on a un temps de début et un temps de fin 
		if(Utils.isNotEmptyOrNull(endTime) && Utils.isNotEmptyOrNull(startTime)){
			printLine(startTime, endTime, loc, speechContent);
		} else if (optionsOutput.forceEmpty) {
			printContinuation(loc, speechContent);
		}
	}

	/**
	 * Ajout des info additionnelles (hors-tiers)
	 * @param u
	 */
	public void writeAddInfo(AnnotatedUtterance u){
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay("com")) return;
			if (!optionsOutput.isDoDisplay("com")) return;
		}
		//Ajout des informations additionnelles présents dans les fichiers srt
		for(String s : u.coms){
			String infoType = Utils.getInfoType(s);
			String infoContent = Utils.getInfo(s);
			printContinuation(infoType, infoContent);
		}
	}

	/**
	 * Ecriture des tiers: lignes qui commencent par le signe pourcent %
	 * @param tier	Le tier à écrire, au format : Nom du tier \t Contenu du tier
	 */
	public void writeTier(AnnotatedUtterance u, Annot tier) {
		if (optionsOutput != null) {
			if (optionsOutput.isDontDisplay(tier.name)) return;
			if (!optionsOutput.isDoDisplay(tier.name)) return;
		}
		String type = tier.name;
		String tierContent = tier.getContent();
		String tierLine = "%" + type + ": " + tierContent.trim();
		printContinuation("%"+type+":", tierLine);	
	}

	public void createOutput() {
	}

	public static void main(String args[]) throws IOException {
		String usage = "Description: TeiToSubtHtml convertit un fichier au format TEI en un fichier au format Sous-titre HTML%nUsage: TeiToSubtHtml [-options] <file.subt.html>%n";
		TeiToSubtHtml ttc = new TeiToSubtHtml();
		ttc.mainCommand(args, Utils.EXT, EXT, usage, 0);
	}

	@Override
	public void mainProcess(String input, String output, TierParams options) {
		transform(new File(input).getAbsolutePath(), output, options);
		// System.out.println("Reading " + input);
		createOutput();
		// System.out.println("New file created " + output);
	}

}
