/**
 * @author Myriam Majdoub
 * Convertit un fichier de transcription TEI en une page HTML permettant de visulaiser la transcription (média + transcription associée).
 */

package fr.ortolang.teicorpo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TeiToHtml {

	TeiFile tf;
	String inputFileName;
	String outputFileName;
	String medialocation;
	String mediaName;
	int sizepage;

	static int spanidNumber;
	static String spanID;
	static String EXT = ".visu.html";

	private PrintWriter out;

	/**
	 * Conversion du fichier TEI passé en paramètre.
	 * @param inputName	Nom du fichier à convertir.
	 * @param outputName	Nom du fichier Html.
	 * @param sp	Nombre de sous-titres par pages. Par défaut 15 pour les vidéos et 25 pour les transcriptions de fichiers audio.
	 */
	public TeiToHtml(String inputName, String outputName, int sp){
		spanidNumber = 0;
		sizepage = sp;
		inputFileName = inputName;
		File input = new File(inputFileName);
		tf = new TeiFile(input, null);
		medialocation = input.getAbsolutePath();
		mediaName = input.getName().split("\\.")[0];

		try{
			FileOutputStream of = new FileOutputStream(outputName);
			OutputStreamWriter outWriter = new OutputStreamWriter( of, "UTF-8");
			out = new PrintWriter(outWriter, true);}
		catch(Exception e){
			out = new PrintWriter(System.out, true);}
	}

	/**
	 * Création du fichier Html à partir des informations du fichier TEI et du média
	 * @param video	: Vaut <strong>true</strong> si il s'agit de la transcription d'un fichier vidéo, <strong>false</strong> sinon.
	 */
	public void createHtml(boolean video, boolean basexOpt, boolean servOpt){
		//Taille du corpus(nombre d'énoncés, sans les tiers)
		int sz = tf.mainLinesSize()-1;
		//Taille de la page
		int sp =  sizepage;
		int i = 0;
		int tmp_i = i;

		//En-tête de la page html
		out.println( "<html xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:toc=\"http://www.tei-c.org/ns/teioc\" lang=\"en\">" );
		out.println( "<head>" );
		out.println( "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>" );
		out.println( "<meta charset=\"utf-8\"/>" );
		out.printf( "<title>%s</title>%n", tf.transInfo.medianame.split("\\.")[0] );
		//Feuille de style pour les différents tiers
		out.println("<style>");
		out.println(".phon{");
		out.println("display:none;");
		out.println("}");
		out.println(".com{");
		out.println("display:none;");
		out.println("}");
		out.println(".sit{");
		out.println("display:none;");
		out.println("}");
		out.println(".act{");
		out.println("display:none;");
		out.println("}");
		out.println(".other{");
		out.println("display:none;");
		out.println("}");
		out.println("</style>");
		//Feuilles de styles
		if(!basexOpt && !servOpt){
			out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/layout.css\"/>" );
			out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"stylesheets/video.css\"/>" );
			out.println( "<link rel=\"stylesheet\" href=\"stylesheets/jquery-ui.css\"/>");
			//Liens vers les scripts utilisés
			out.println( "<script type=\"text/javascript\" src=\"scripts/popcorn-complete.js\"></script>" );
			out.println("<script src=\"scripts/jquery.min.js\"></script>" );
			out.println("<script src=\"scripts/jquery-ui.min.js\"></script>" );
			//Script qui permet l'adapation du code pour Internet Explorer (versions antérieures à IE8)
			out.println("<!--[if lt IE 8]>");
			out.println("<script src=\"scripts/IE8.js\" type=\"text/javascript\"/>");
			out.println("<![endif]-->");
		}
		else{
			out.println( "<link rel=\"icon\" href=\"/images/favicon.ico\"/>" );
			out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/stylesheets/layout.css\"/>" );
			out.println( "<link rel=\"stylesheet\" type=\"text/css\" href=\"/stylesheets/video.css\"/>" );
			out.println( "<link rel=\"stylesheet\" href=\"/stylesheets/jquery-ui.css\"/>");
			//Liens vers les scripts utilisés
			out.println( "<script type=\"text/javascript\" src=\"/scripts/popcorn-complete.js\"></script>" );
			out.println("<script src=\"/scripts/jquery.min.js\"></script>" );
			out.println("<script src=\"/scripts/jquery-ui.min.js\"></script>" );
			//Scripts qui permet l'adapation du code pour Internet Explorer (versions antérieures à IE8)
			out.println("<!--[if lt IE 8]>");
			out.println("<script src=\"/scripts/IE8.js\" type=\"text/javascript\"/>");
			out.println("<![endif]-->");
		}

		out.println( "</head>" );
		out.println( "<body>" );	

		//Nom du fichier à visualiser
		out.printf("<h1>%s</h1>", mediaName);
 
		tiersForm();

		//Script pour l'affichage des locuteurs
		out.println( "<script>");
		out.println( "$(function() {");
		out.println( "$('#speakers').tabs();");
		out.println( "});");
		out.println( "</script>");

		//Fonction pour afficher les fichiers à télécharger
		out.println( "<script type=\"text/javascript\">");

		out.println("function printListFiles(){");
		out.println("listFile = document.getElementById(\'fileList\');");
		out.println("if(listFile.style.display==\'block\'){");
		out.println("listFile.style.display=\"none\";");
		out.println("}");
		out.println("else if(listFile.style.display=\'none\'){");
		out.println("listFile.style.display=\"block\";");
		out.println("}");
		out.println("}");
		
		out.println("window.addEventListener('blur', function() {");
		out.println("var mediaPlayer = document.getElementById(\"popvideo\");");
		out.println("mediaPlayer.pause();");
		out.println("});");
		
		
		out.println("</script>");

		String medianame1 = medialocation.split("\\.")[0];
		String medianame2 = medianame1 + "-240p";
		String medianame3 = medianame1 + "-480p";

		if(servOpt){
			medianame1 = "http://modyco.inist.fr" + medialocation.substring(7).split("\\.")[0];
			medianame2 = medianame1 + "-240p" ;
			medianame3 = medianame1 + "-480p" ;
		}

		//Vidéo
		if (video){
			out.printf( "<div id=\"media\" class=\"highlight\" style=\"height:%dpx;\">%n", 405 + 16 + sp*24 );
			out.println( "<video id=\"popvideo\" class=\"video-js vjs-default-skin\" width=\"640\" height=\"385\" controls=\"controls\"  preload=\"auto\" >" );
			out.printf( "<source type=\"video/mp4\" src=\"%s.mp4\"></source><br/>%n", medianame3);
			out.printf( "<source type=\"video/mp4\" src=\"%s.mp4\"></source><br/>%n", medianame1);			
			out.printf( "<source type=\"video/webm\" src=\"%s.webm\"></source>%n", medianame3 );	
			out.printf( "<source type=\"video/webm\" src=\"%s.webm\"></source>%n", medianame1 );	
			out.printf( "<source type=\"video/ogg\" src=\"%s.ogv\"></source>%n", medianame2);							
			out.println( "This page requires <strong>&lt;video&gt;</strong> support:<br/>" );
			out.println( "best viewed with <a href=\"http://www.mozilla.org/fr/firefox/fx/\">Firefox&nbsp;3.5+</a>," +
					" <a href=\"http://www.mozilla.org/fr/firefox/fx/\">Safari&nbsp;4+</a>," +
					" <a href=\"http://www.mozilla.org/fr/firefox/fx/\">Chrome&nbsp;5+</a>," +
					" <a href=\"http://www.mozilla.org/fr/firefox/fx/\">Opera&nbsp;10.60+</a> or " +
					" <a href=\"http://www.mozilla.org/fr/firefox/fx/\">IE9</a>.<br/><br/>" );
			out.println( "Internet Explorer users, please enable Flash or Silverlight." );
			out.println("</video>");

		} else {
			out.printf("<div id=\"media\" class=\"highlight\" style=\"height:%dpx;\">%n", 75 + sp*24);
			out.println("<div style='text-align:center;'>");
			out.println("<audio id=\"popvideo\" width=\"640\" height=\"75\" controls=\"controls\" preload=\"auto\" >");
			out.printf("<source type=\"audio/ogg\" src=\"%s.ogg\"></source>%n", medianame1);
			out.printf("<source type=\"audio/mpeg\" src=\"%s.mp3\"></source><br/>%n", medianame1);
			out.printf("<source type=\"audio/mpeg\" src=\"%s.mp4\"></source><br/>%n", medianame1);
			out.printf("<source type=\"audio/wav\" src=\"%s.wav\"></source><br/>%n", medianame1);
			out.println("This page requires <strong>&lt;audio&gt;</strong> support:<br/>");
			out.println("best viewed with <a href=\"http://www.mozilla.org/fr/firefox/fx/\">Firefox&nbsp;3.5+</a>," +
					" <a href=\"http://www.mozilla.org/fr/firefox/fx/\">Safari&nbsp;4+</a>," +
					" <a href=\"http://www.mozilla.org/fr/firefox/fx/\">Chrome&nbsp;5+</a>," +
					" <a href=\"http://www.mozilla.org/fr/firefox/fx/\">Opera&nbsp;10.60+</a> or " +
					" <a href=\"http://www.mozilla.org/fr/firefox/fx/\">IE9</a>.<br/><br/>");
			out.println("Internet Explorer users, please enable Flash or Silverlight.");
			out.println("</audio>");	
			out.println("</div>");
		}

		String id = "";
		while (i <= sz && i>=0) {
			sp = sizepage;

			if(i+sp > sz){
				sp = sz - i;
			}

			float start = getStartTime(tf.mainLines, i); 
			float end;
			try{
				end = getStartTime(tf.mainLines, i+sp+1);
			}
			catch(Exception e){
				end = getEndTime(tf.mainLines, i+sp);
			}

			id = "id" + start;

			out.printf( "<p id=\"%s\" style=\"height:%dpx;\">%n", id, sizepage*24);

			out.printf("<span class=\"time\">%02d:%02d -- %02d:%02d</span><br/>%n", Utils.toXMinutes(start), Utils.toSeconds(start), Utils.toXMinutes(end), Utils.toSeconds(end));

			out.printf("<script>%n");
			out.printf("var p = document.getElementById( \"%s\" );", id);
			out.printf("document.addEventListener( \"DOMContentLoaded\", function() {%n");
			out.printf("var popcorn = Popcorn( \"#popvideo\" );%n");
			out.printf("popcorn.code({ %n");
			out.printf("start: %s,%n", start);
			out.printf("end: %s,%n", end);
			out.printf("onStart: function( options ) {%n");
			out.printf("document.getElementById( \"%s\" ).style.display = 'block';%n", id);
			out.printf("},%n");
			out.printf("onEnd: function( options ) {%n");
			out.printf("document.getElementById( \"%s\" ).style.display = 'none';%n", id);
			out.printf("}%n");
			out.printf("});%n");
			out.printf("}, true );%n");
			out.printf("</script>%n");

			float start0 = -1;
			float end0 = -1;

			for (int j = i; j<sp+i+1; j++) {
				tmp_i = i;
				AnnotatedUtterance u2 =  tf.mainLines.get(j);
				String spkIdentifier = "";
				if(tf.transInfo.format.equals("Clan")){
					spkIdentifier = u2.speakerCode.replaceAll(":", "");
				}
				else if(tf.transInfo.format.equals("Transcriber")){
					spkIdentifier = u2.speakerName.replaceAll(":", "");
				}
				String speech =TeiConventions.SetConventions(u2.speech).replaceAll("<", "&lt;").replaceAll(">", "&gt;");

				if(Utils.isNotEmptyOrNull(speech)){
					if(tf.trans.tierTypes.contains("morpho")){
						String morpho = u2.getTier("morpho").get(0).getContent();
						String [] morphoSplit = morpho.split(" ");
						String [] speechSplit = speech.split("\\s+|\\-'|\\_");
						for (String desc : morphoSplit){
							String [] descSplit = desc.split("\\|");
							if(descSplit.length>1){
								String tok = descSplit[0];
								String info = "";
								for(int p = 1; p<descSplit.length; p++){
									String [] infoSplit = descSplit[p].split(":");
									try{
										String newIn = "<span class=\"infoName\">"+infoSplit[0]+ "</span>:" + infoSplit[1];
										info += newIn + "<br/>";}
									catch(ArrayIndexOutOfBoundsException e){
										info += descSplit[p] + "<br/>";
									}
								}
								String replace = "<span class=\"infobulle\">" + tok +"<span class=\"infobulle-hidden\">" + info + "</span></span>";
								for(int u = 0; u<speechSplit.length; u++){
									if(speechSplit[u].replaceAll("\\+", "").equals(tok)){
										speechSplit[u] = replace;
									}
								}
							}
						}
						speech = Utils.join(speechSplit);
					}

					String id0 = "";

					if ( (u2.start != "-1" && !u2.start.isEmpty()) || (u2.end != "-1" && !u2.end.isEmpty()) ) {
						if((u2.start != "-1" && !u2.start.isEmpty()) && (u2.end != "-1" && !u2.end.isEmpty())){					
							start0 = Float.parseFloat(u2.start);
							end0 = Float.parseFloat(u2.end);
						}
						else if(u2.start != "-1" && !u2.start.isEmpty()){
							start0 = Float.parseFloat(u2.start);
							try{
								//Début de l'utterance suivante
								end0 = Float.parseFloat(tf.mainLines.get(j+1).start);
							}
							catch(Exception e){
								//+ 2 secondes
								end0 = Float.parseFloat(u2.start) + (float)2;
							}
						}
						else if(u2.end != "-1" && !u2.end.isEmpty()){
							end0 = Float.parseFloat(u2.end);
							try{
								//Fin de l'utterance précédente
								start0 = Float.parseFloat(tf.mainLines.get(j-1).end);
							}
							catch(Exception e){
								// - 2 secondes
								start0 = Float.parseFloat(u2.end) - (float)2;
							}
						}

						if (u2.speech.length()>72){
							int dif = speech.length()/72;
							if (dif >= sp){
								sp -= dif-sp;
							}
							else{
								sp-= dif;
							}
						}

						if(start0==0.0){
							start0=(float) 0.001;
						}

						id0 = "sid" + start0;
						spanID = id0;

						out.printf( "<a class='jump' " );
						out.printf( "onClick=\"document.getElementById( 'popvideo' ).currentTime = %s;\" >%n",start0 );
						out.printf( " <span class=\"ptr\">&bull;&nbsp;</span><span id=\"%s\" class=\"utt\"><span class=\"nutt\">%s</span> %s</span><br/>%n", id0, spkIdentifier + " : ", speech);
						addInfo(u2, id0);
						out.printf( "</a>%n" );
					}

					else {
						id0 ="spanid" + spanidNumber;
						spanidNumber ++;
						out.printf( "<a>");
						out.printf( "<span class=\"nobullet\">&bull;&nbsp;</span><span id=\"%s\" class=\"utt\"><span class=\"nutt\" >%s</span> %s</span><br/>%n", id0, spkIdentifier + " : ",speech);
						addInfo(u2, id0);
						out.printf( "</a>%n" );
					}

					out.printf( "<script>%n" );
					out.printf( "document.addEventListener( \"DOMContentLoaded\", function() {%n" );
					out.printf( "var span = document.getElementById( \"%s\" );", id0);
					out.printf( "var popcorn = Popcorn( \"#popvideo\" );%n" );
					out.printf( "popcorn.code({ %n" );
					out.printf( "start: %s,%n", start0 );
					out.printf( "end: %s,%n", end0 );
					out.printf( "onStart: function( options ) {%n" );
					getTiers();
					out.printf("if(span.id==\"%s\"){", "sid"+start0);

					out.printf("var elem = document.getElementById('%s');", id);
					if (video){
						out.printf("if(span.offsetTop > elem.offsetTop*3){");
					}
					else{
						out.printf("if(span.offsetTop > elem.offsetTop*1.2){");
					}
					out.printf("elem.scrollTop = elem.scrollHeight;");
					out.println("}");

					out.printf( "span.style.color = 'mediumVioletRed';%n" );
					out.println( "}" );
					out.printf( "},%n" );
					out.printf( "onEnd: function( options ) {%n" );
					out.printf( "span.style.color = 'black';%n" );
					out.printf( "}%n" );
					out.printf( "})%n" );
					out.printf( "}, false );%n" );
					out.printf( "</script>%n" );
				}
			}
			out.printf( "</p>%n" );

			if (tmp_i == i){
				i++;
			}
			if(sp>0){
				i+=sp;
			}
		}
		out.println("</div>");
		addParticipantDivs();		
		out.println( "</body></html>" );
	}

	public void getTiers(){
		out.println("var inputs = document.getElementsByTagName(\"input\");");
		out.println("for (var i = 0; i<inputs.length; i++){");
		out.println("if (inputs[i].name=='phon'){");
		out.println( "var searchedRule = document.styleSheets[0].cssRules[0];");
		out.println("}");
		out.println("if (inputs[i].name=='com'){");
		out.println( "var searchedRule = document.styleSheets[0].cssRules[1];");
		out.println("}");
		out.println("else if (inputs[i].name=='sit'){");
		out.println( "var searchedRule = document.styleSheets[0].cssRules[2];");
		out.println("}");
		out.println("else if (inputs[i].name=='act'){");
		out.println( "var searchedRule = document.styleSheets[0].cssRules[3];");
		out.println("}");
		out.println("else if (inputs[i].name=='other'){");
		out.println( "var searchedRule = document.styleSheets[0].cssRules[4];");
		out.println("}");
		out.println("if (inputs[i].checked ) {");
		out.println( "searchedRule.style.display='block';" );
		out.println("p.style.height = '20px';");
		out.println("}");
		out.println("else{");
		out.println( "searchedRule.style.display='none';" );
		out.println("}");
		out.println("}");
	}

	public void addInfo(AnnotatedUtterance u, String id){
		////////////////PHON
		ArrayList<Annot> phons = u.getTier("pho");		
		if(!phons.isEmpty()){
			for(Annot phon: phons){
				out.printf("<span class=\"phon\" >%n" );
				addTierNodes(phon.getContent());
				out.println("</span>");
			}
		}

		///////////////COMMENTS
		ArrayList<Annot> coms = u.getTier("com");
		if(!coms.isEmpty()){
			for(Annot com: coms){
				out.printf("<span class=\"com\" >%n");
				addTierNodes(com.getContent());
				out.println("</span>");
			}
		}

		////////////////SITUATION
		ArrayList<Annot> sits = u.getTier("sit");
		if(!sits.isEmpty()){
			for(Annot sit: sits){
				out.printf("<span class=\"sit\" >%n");
				addTierNodes(sit.getContent());
				out.println("</span>");
			}
		}

		//////////////////ACTION
		ArrayList<Annot> acts = u.getTier("act");
		if(!acts.isEmpty()){
			for(Annot act: acts){
				out.printf("<span class=\"act\" >%n");
				addTierNodes(act.getContent());
				out.println("</span>");
			}
		}
		///////////////////OTHER
		ArrayList<Annot> others = u.getTier("other");
		if(!others.isEmpty()){
			for(Annot other: others){
				out.printf("<span class=\"other\" >%n");
				addTierNodes(other.getContent());
				out.println("</span>");
			}
		}
	}

	public void tiersForm(){		
		out.println("<form id = \"form\" >");
		out.print("<span class = \"form\">");
		out.println("<span class=\"prop\">&nbsp;&nbsp;Informations à afficher</span><br/><br/>");
		if (tf.trans.tierTypes.contains("pho")){
			out.println("&nbsp;&nbsp;<input type=\"checkbox\" name=\"phon\" id=\"phon\"/><label for=\"phon\"><span class=\"boxName\">Transcription phonétique</span></label><br />");
		}
		if (tf.trans.tierTypes.contains("com")){
			out.println("&nbsp;&nbsp;<input type=\"checkbox\" name=\"com\" id=\"com\"/><label for=\"com\"><span class=\"boxName\">Commentaires</span></label><br />");
		}
		if (tf.trans.tierTypes.contains("sit")){
			out.println("&nbsp;&nbsp;<input type=\"checkbox\" name=\"sit\" id=\"sit\"/><label for=\"sit\"><span class=\"boxName\">Situation</span></label><br />");
		}
		if (tf.trans.tierTypes.contains("act")){
			out.println("&nbsp;&nbsp;<input type=\"checkbox\" name=\"act\" id=\"act\"/><label for=\"act\"><span class=\"boxName\">Action</span></label><br />");
		}
		if (tf.trans.tierTypes.contains("other")){
			out.println("&nbsp;&nbsp;<input type=\"checkbox\" name=\"other\" id=\"other\"/><label for=\"other\"><span class=\"boxName\">Divers</span></label><br/><br/>");
		}
		if (tf.trans.tierTypes.isEmpty()){
			out.println("&nbsp;&nbsp;<span class=\"boxName\">Aucune information supplémentaire à afficher pour ce document.</span><br/><br/>");
		}

		out.println("<hr/>");
		out.println("<a href=\"http://modyco.inist.fr/sources/Ortolang/ConventionsAffichagePheno.pdf\" target=\"_blank\">Lien vers les conventions d'affichage utilisées</a>");
		//Ajout download après vérification de la fonction de compression et téléchargement des fichiers
		//Méthode php après vérif ...
		//out.println("<hr/>");		
		//download();
		out.println("</span>");
		out.println("</form>");
	}

	public void download(){
		out.println("<input type=\"button\" value=\"Télécharger la transcription\" onclick=\"printListFiles();\" /> ");
		out.println("<span id=\"fileList\">");
		File rep = new File(medialocation).getParentFile();
		File [] listFiles = rep.listFiles();
		int nbFile = 0;
		for(File f : listFiles){			
			if(f.getName().startsWith(mediaName) && ! f.getName().endsWith(".html")){
				nbFile++;
				out.printf("<input name=\"filename\" type=\"checkbox\" id=\"%s\"/><label><span class=\"boxName\">%s</span></label><br/>", f.getName(), f.getName());
			}			
		}
		if(nbFile>0){
			out.println("<input type=\"button\" value=\"Télécharger les fichiers sélectionnés\" onclick=\"\" /> ");
		}
		out.println("</span>");
	}

	public void addTierNodes(String tier){
		try{
			out.println("<span class=\"nobullet\" >&bull;&nbsp;</span>");
			out.printf("<span class=\"tierType\" >%s</span>%n ", tier.split("\t")[0]+"\t:\t");
			out.printf("<span class=\"tierText\" >%s</span>%n ", tier.split("\t")[1]);
		}
		catch(Exception e){}
	}

	public void addParticipantDivs(){
		ArrayList<TeiParticipant> speakers = tf.transInfo.participants;
		out.println( "<div id=\"speakers\">");
		out.println("<ul>");
		for (TeiParticipant p: speakers){
			out.printf( "<li><a href=\"#%s\">%s</a></li>%n", p.id, p.name);
		}
		out.println("</ul>");
		for (TeiParticipant p: speakers){
			out.printf( "<div id=\"%s\" style=\"background-color:#F5F5F5;\">%n", p.id);
			out.println("<table>");
			if (p.id != null && !p.id.isEmpty()){
				out.println("<tr>");
				out.println( "<td><span class=\"prop\">Identifiant&nbsp;&nbsp;</span></td>");
				out.printf( "<td><span class=\"val\">%s</span></td>%n", p.id);
				out.println("</tr>");
			}
			if (p.name != null && !p.name.isEmpty()){
				out.println( "<tr>");
				out.println( "<td><span class=\"prop\">Nom</span></td>");
				out.printf( "<td><span class=\"val\">%s</span></td>%n", p.name);
				out.println( "</tr>");
			}
			if (p.sex != null && !p.sex.isEmpty()){
				out.println( "<tr>");
				out.println( "<td><span class=\"prop\">Sexe</span></td>");
				out.printf( "<td><span class=\"val\">%s</span></td>%n", p.sex);
				out.println( "</tr>");
			}
			if (p.language != null && !p.language.isEmpty()){
				out.println( "<tr>");
				out.println( "<td><span class=\"prop\">Langue</span></td>");
				out.printf( "<td><span class=\"val\">%s</span></td>%n", p.language);
				out.println( "</tr>");
			}
			if (p.role != null && !p.role.isEmpty()){
				out.println( "<tr>");
				out.println( "<td><span class=\"prop\">Role</span></td>");
				out.printf( "<td><span class=\"val\">%s</span></td>%n", p.role);
				out.println( "</tr>");
			}
			for (String prop:p.adds.keySet()){
				out.println( "<tr>");
				out.printf( "<td><span class=\"prop\">%s</span></td>%n", prop);
				out.printf( "<td><span class=\"val\">%s</span></td>%n", p.adds.get(prop));
				out.println( "</tr>");
			}
			out.println( "</table>");
			out.println("</div>");
		}
		out.println("</div>");
	}
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	public static float getStartTime(ArrayList<AnnotatedUtterance> mainlines, int i){
		AnnotatedUtterance u = mainlines.get(i);
		if (!u.start.isEmpty() && u.start != null){
			return Float.parseFloat(u.start);
		}
		else{
			int nb=0;
			for (int j = i; j>0; j--){
				nb ++;
				AnnotatedUtterance utt = mainlines.get(j);
				if (!utt.end.isEmpty() && utt.end != null){
					return Float.parseFloat(utt.end)+((Float.parseFloat(utt.end)-Float.parseFloat(utt.start))*nb);
				}
			}
		}
		return -1;
	}

	public static float getEndTime(ArrayList<AnnotatedUtterance> mainlines, int i){
		AnnotatedUtterance u = mainlines.get(i);
		if (!u.end.isEmpty() && u.end != null){
			return Float.parseFloat(u.end);
		}
		else{
			int nb=0;
			for (int j = i; j<mainlines.size(); j++){
				nb ++;
				AnnotatedUtterance utt = mainlines.get(j);
				if (!utt.start.isEmpty() && utt.start != null){
					return Float.parseFloat(utt.start)-((Float.parseFloat(utt.end)-Float.parseFloat(utt.start))*nb);
				}
			}
		}
		return -1;
	}

	public static String tiersToString(ArrayList<String> tiersTypes, AnnotatedUtterance u){
		String tiersToString = "";
		for (Annot t : u.tiers){
			tiersToString += t.name + " " + t.getContent() + "\n";
		}
		return tiersToString;
	}

	public static String newDirName(String path){
		return "";
	}

	public static String basexVersion(String s){
		String [] split = s.split("/");
		return( "/" + split[split.length-1]);
	}

	public static void main (String [] args){
		int sizepage = 15;
		boolean video = true;
		boolean basexOpt = false;
		boolean servOpt = false;

		String inputName = args[0];
		File input = new File(inputName);


		boolean isDir = false;
		if (input.isDirectory()){
			isDir = true;
		}

		if (args.length>1){
			for (int i = 1; i<args.length; i++){
				if (args[i].equals("audio")){
					video = false;
					sizepage = 25;
				}
				if (args[i].equals("basex")){
					basexOpt = true;
				}
				if (args[i].equals("serv")){
					servOpt = true;
				}
			}
		}

		if (isDir){
			File[] teiFiles = input.listFiles();
			for (File file : teiFiles){
				if(file.getName().endsWith(Utils.EXT)){
					String outputFilename = "";
					if(video == false){
						outputFilename = file.getAbsolutePath().split("\\.")[0]+"_audio.html";
					}
					else{
						outputFilename = file.getAbsolutePath().split("\\.")[0]+".html";
					}
					TeiToHtml tth = new TeiToHtml(file.getAbsolutePath(), outputFilename, sizepage);
					tth.createHtml(video, basexOpt, servOpt);
					System.out.println("New HTML file created : " + outputFilename);
				}
				else if(file.isDirectory()){
					args[0] = file.getAbsolutePath();
					main(args);
				}
			}
		}
		else{
			String outputFilename = "";

			if(video == false){
				outputFilename = inputName.split("\\.")[0]+"_audio.html";
			}
			else{
				outputFilename = inputName.split("\\.")[0]+".html";
			}

			TeiToHtml tth = new TeiToHtml(inputName, outputFilename, sizepage);
			tth.createHtml(video, basexOpt, servOpt);
			System.out.println("New HTML file created : " + outputFilename);
		}
	}
}
