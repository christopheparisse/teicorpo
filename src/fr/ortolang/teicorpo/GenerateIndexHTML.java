package fr.ortolang.teicorpo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GenerateIndexHTML {

	File inputDir;
	PrintWriter out;
	String urlComplements = "complements.txt";

	public GenerateIndexHTML(File dirName) throws IOException {

		inputDir = dirName;
		try {
			FileOutputStream of = new FileOutputStream(inputDir.getCanonicalPath() + "/index.php");
			OutputStreamWriter outWriter = new OutputStreamWriter(of, "UTF-8");
			out = new PrintWriter(outWriter, true);
		} catch (Exception e) {
			out = new PrintWriter(System.out, true);
		}

		writeIndexHTML();
		System.out.println(inputDir.getCanonicalPath());
	}

	public void writeIndexHTML() throws IOException {

		String title = new File(inputDir.getCanonicalPath()).getName();
		out.println(
				"<html xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:toc=\"http://www.tei-c.org/ns/teioc\" lang=\"en\">");
		out.println("<head>");

		out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
		out.printf("<title>%s</title>%n", title);

		out.println("<!-- Début copie header -->");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		out.println("<meta name=\"description\" content=\"\">");
		out.println("<meta http-equiv=\"content-language\" content=\"en\">");

		out.println("<link rel=\"icon\" href=\"http://ct3.ortolang.fr/images/favicon.ico\"/>");

		out.println(
				"<link rel=\"stylesheet\" type=\"text/css\" href=\"http://ct3.ortolang.fr/stylesheets/indexStyle.css\"/>");

		out.println(
				"<link href=\"<?php print($BOOTSTRAP_HOST) ?>http://ct3.ortolang.fr/stylesheets/bootstrap.min.css\" rel=\"stylesheet\">");
		out.println(
				"<link href=\"<?php print($BOOTSTRAP_HOST) ?>http://ct3.ortolang.fr/stylesheets/bootstrap-responsive.min.css\" rel=\"stylesheet\">");
		out.println(
				"<link href=\"<?php print($SITE_HOST) ?>http://ct3.ortolang.fr/stylesheets/docs.css\" rel=\"stylesheet\">");
		out.println(
				"<link href=\"<?php print($SITE_HOST) ?>http://ct3.ortolang.fr/stylesheets/reflow-skin.css\" rel=\"stylesheet\">");
		out.println(
				"<link href=\"<?php print($HIGHLIGHTJS_HOST) ?>http://ct3.ortolang.fr/styles/github.min.css\" rel=\"stylesheet\">");
		out.println(
				"<link href=\"<?php print($SITE_HOST) ?>http://ct3.ortolang.fr/stylesheets/site.css\" rel=\"stylesheet\">");
		out.println(
				"<link href=\"<?php print($SITE_HOST) ?>http://ct3.ortolang.fr/stylesheets/print.css\" rel=\"stylesheet\" media=\"print\">");
		out.println();
		out.println("<link rel=\"stylesheet\" href=\"http://ct3.ortolang.fr/stylesheets/dialog-style.css\">");
		out.println(
				"<link rel=\"stylesheet\" href=\"http://ct3.ortolang.fr/stylesheets/font-awesome-4.3.0/css/font-awesome.min.css\">");
		out.println("<script src=\"http://ct3.ortolang.fr/scripts/dialog-polyfill.js\"></script>");
		out.println("<script src=\"http://ct3.ortolang.fr/scripts/jquery.min.js\"></script>");
		out.println("<script src=\"http://ct3.ortolang.fr/scripts/dialog-index.js\"></script>");
		out.println();
		out.println("<?php");
		out.println("if(isset($HEADER_CSS)) {");
		out.println("foreach($HEADER_CSS as $CSS) {");
		out.println(
				"print(\"<link rel=\\\"stylesheet\\\" type=\\\"text/css\\\" href=\\\"http://ct3.ortolang.fr/$CSS\\\"></link>\");");
		out.println("}");
		out.println("}");

		out.println("if(isset($HEADER_JS)) {");
		out.println("foreach($HEADER_JS as $JS) {");
		out.println(
				"print(\"<script language=\\\"javascript\\\" type=\\\"text/javascript\\\" src=\\\"http://ct3.ortolang.fr/$JS\\\"></script>\");");
		out.println("}		");
		out.println("}");

		out.println("?>");

		out.println("<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->");
		out.println("<!--[if lt IE 9]>");
		out.println("<script src=\"http://html5shim.googlecode.com/svn/trunk/html5.js\"></script>");
		out.println("<![endif]-->");

		out.println("<!-- Google Analytics -->");
		out.println(
				"<script type=\"text/javascript\" async=\"\" src=\"http://www.google-analytics.com/ga.js\"></script><script type=\"text/javascript\">");

		out.println("var _gaq = _gaq || [];");
		out.println("_gaq.push(['_setAccount', 'UA-2086251-10']);");
		out.println("_gaq.push(['_trackPageview']);");

		out.println("(function() {");
		out.println("var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;");
		out.println(
				"ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';");
		out.println("var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);");
		out.println("})();");

		out.println("</script>");
		out.println("<style type=\"text/css\"></style><style type=\"text/css\"></style>");

		out.println("<!-- Fin copie header -->");

		out.println("</head>");
		out.println("<body>");

		out.println("<?php");
		out.println("function getSize($file){");
		out.println("$stat = stat($file);");
		out.println("$size = $stat['size'];");
		out.println("if($size > 1000000000){");
		out.println("$sizeConv = round($size/1000000000,1);");
		out.println("$sizeStr = \"($sizeConv Go)\";");
		out.println("}");
		out.println("elseif($size > 1000000){");
		out.println("$sizeConv = round($size/1000000);");
		out.println("$sizeStr = \"($sizeConv Mo)\";");
		out.println("}");
		out.println("elseif($size > 1000){");
		out.println("$sizeConv = round($size/1000);");
		out.println("$sizeStr = \"($sizeConv ko)\";");
		out.println("}");
		out.println("return $sizeStr;");
		out.println("}");
		out.println("?>");

		out.println("<!--Début copie header corps -->");

		out.println("<div class=\"navbar navbar-fixed-top\">");
		out.println("<div class=\"navbar-inner\">");
		out.println("<div class=\"container\">");
		out.println("<a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">");
		out.println("<span class=\"icon-bar\"></span>");
		out.println("<span class=\"icon-bar\"></span>");
		out.println("<span class=\"icon-bar\"></span>");
		out.println("</a>");
		out.println(
				"<a class=\"brand\" href=\"<?php print($SITE_HOST) ?>http://ct3.ortolang.fr/index.php?page=accueil\"><img src=\"<?php print($SITE_HOST) ?>http://ct3.ortolang.fr/images/ortolang-logo.png\" style=\"height:40px; margin-right:10px;\"><span class=\"color-highlight\">ORTOLANG</span> Nanterre/Orléans</a>");
		out.println("</div>");
		out.println("</div>");
		out.println("</div>");

		out.println("<!-- Fin copie header corps-->");

		out.println("<div class=\"bodyDiv\">");
		out.printf("<h2 style=\"color:white;\">%s</h2>%n", title);

		File[] listFilesHTML = inputDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".tei_corpo.xml") || name.endsWith("subt.html");
			}
		});

		File[] listFilesTranscriptions = inputDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".cha") || name.endsWith(".trs") || name.endsWith(".tei_corpo.xml");
			}
		});

		File[] listFilesMedias = inputDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".mov") || name.endsWith(".mp4") || name.endsWith(".webm") || name.endsWith(".ogv")
						|| name.endsWith("ogg") || name.endsWith(".mp3") || name.endsWith(".wav");
			}
		});

		File[] listFilesXML = inputDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".metadata.xml");
			}
		});

		File[] listComplements = inputDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.equals(urlComplements);
			}
		});

		if (listFilesHTML.length > 0) {
			out.println("<h3>Visualiser la transcription</h3>");
			out.println("<ul>");
			for (File htmlFile : listFilesHTML) {
				String htmlFileName = htmlFile.getName();
				if (htmlFileName.endsWith(".tei_corpo.xml")) {
					String htmlDir = inputDir.getCanonicalPath().substring("/applis".length());
					String textVisuAdress = "/tools/trjsread/trjsread.html?t=" + htmlDir + "/" + htmlFileName;
					// out.printf("<li>Visualisation classique : <a
					// href=\"%s\">%s</a></li>%n", htmlFileName,
					// htmlFileNameBase);
					out.printf("<li>Visualisation texte : <a href=\"%s\">%s</a></li>%n", textVisuAdress, htmlFileName);
				} else {
					out.printf("<li>Visualisation sous-titres : <a href=\"%s\">%s</a></li>%n", htmlFileName,
							htmlFileName);
				}

				/*
				 * out.
				 * printf("<li>Visualisation classique : <button id=\"bclassic\">%s</button></li>%n"
				 * , baseName); out.
				 * printf("<li>Visualisation sous-titres : <button id=\"bsubt\">%s</button></li>%n"
				 * , htmlSubtFileName); out.
				 * printf("<li>Visualisation texte : <button id=\"btrjs\">%s</button></li>%n"
				 * , baseName);
				 * 
				 * out.println("</ul>");
				 * 
				 * out.println( "<dialog id=\"trjs\">"); out.println( "<div>");
				 * out.printf(
				 * "<iframe onload=\"this.width=window.innerWidth*0.95;this.height=window.innerHeight*0.95;\" id=\"idtrjs\" src=\"%s\"></iframe>%n"
				 * , textVisuAdress); out.println( "<button id=\"oktrjs\">");
				 * out.println( "<i class=\"fa fa-times\"></i>"); out.println(
				 * "</button>"); out.println( "</div>"); out.println(
				 * "</dialog>");
				 * 
				 * out.println( "<dialog id=\"classic\">"); out.println(
				 * "<div>"); out.println( "<!--"); out.println(
				 * "<div style='position: relative; width: 100%; height: 0px; padding-bottom: 60%;'>"
				 * ); String style =
				 * "position: absolute; left: 0px; top: 0px; width: 100%; height: 100%;"
				 * ; out.printf(
				 * "<iframe style='%s' id=\"idclassic\" src=\"%s\"></iframe>%n",
				 * style, htmlFileName); out.println( "</div>"); out.println(
				 * "-->"); out.printf(
				 * "<iframe onload=\"this.width=window.innerWidth*0.9;this.height=window.innerHeight*0.9;\" id=\"idclassic\" src=\"%s\"></iframe>%n"
				 * , htmlFileName); out.println( "<button id=\"okclassic\">");
				 * out.println( "<i class=\"fa fa-times\"></i>");
				 * 
				 * out.println( "</button>"); out.println( "</div>");
				 * out.println( "</dialog>");
				 * 
				 * out.println( "<dialog id=\"vid\">"); out.println( "<div>");
				 * out.println( "<video id=\"idvid\" controls>"); out.printf(
				 * "<source src=\"%s-240p.mp4\">%n", baseName); out.println(
				 * "Votre navigateur ne gère pas l'élément <code>video</code>."
				 * ); out.println( "</video>"); out.println(
				 * "<button id=\"okvid\">"); out.println(
				 * "<i class=\"fa fa-times\"></i>"); out.println( "</button>");
				 * out.println( "</div>"); out.println( "</dialog>");
				 * 
				 * out.println( "<dialog id=\"subt\">"); out.println( "<div>");
				 * out.println( "<!--"); out.println( "-->"); out.printf(
				 * "<iframe onload=\"this.width=window.innerWidth*0.8;this.height=window.innerHeight*0.6;\" id=\"idsubt\" src=\"%s\"></iframe>%n"
				 * , htmlSubtFileName); out.println( "<button id=\"oksubt\">");
				 * out.println( "<i class=\"fa fa-times\"></i>"); out.println(
				 * "</button>"); out.println( "</div>"); out.println(
				 * "</dialog>");
				 */
			}
			out.println("</ul>");
		}

		if (listFilesTranscriptions.length > 0 && listFilesMedias.length > 0) {
			out.println("<h3>Téléchargement des données </h3>");
		}

		if (listFilesTranscriptions.length > 0) {
			out.println("<h4>Transcriptions</h4>");
			out.println("<ul>");
			for (File transFile : listFilesTranscriptions) {
				String fileName = transFile.getName();
				if (fileName.endsWith(".cha")) {
					out.println("<li>Transcription au format CLAN : ");
					out.printf("<a href=\"%s\">%s</a> <?php echo getSize(\"%s\"); ?></li>%n", fileName, fileName,
							fileName);
				} else if (fileName.endsWith(".trs")) {
					out.println("<li>Transcription au format Transcriber : ");
					out.printf("<a href=\"%s\">%s</a> <?php echo getSize(\"%s\"); ?></li>%n", fileName, fileName,
							fileName);
				} else if (fileName.endsWith(".tei_corpo.xml")) {
					out.println("<li>Transcription au format TEI/Ortolang : ");
					out.printf("<a href=\"%s\">%s</a> <?php echo getSize(\"%s\"); ?></li>%n", fileName, fileName,
							fileName);
				}
			}
			out.println("</ul>");
		}

		if (listFilesMedias.length > 0) {
			out.println("<h4>Enregistrements</h4>");
			out.println("<ul>");
			for (File mediaFile : listFilesMedias) {
				String fileName = mediaFile.getName();
				out.printf("<a href=\"%s\">%s</a> <?php echo getSize(\"%s\"); ?><br/></li>%n", fileName, fileName,
						fileName);
			}
			if (listComplements.length == 0)
				out.println("</ul>");
		}

		// essaye de completer avec le fichier complements.txt
		if (listComplements.length > 0) {
			// read complements file
			List<String> lines = Files.readAllLines(listComplements[0].toPath(),
					StandardCharsets.UTF_8);
			if (listFilesMedias.length == 0) {
				out.println("<h4>Enregistrements</h4>");
				out.println("<ul>");
			}
			// create real address
			for (String s : lines) {
				s = s.trim();
				int p = s.indexOf(" ");
				if (p >= 1)
					out.printf("<a href=\"%s/%s\">%s</a> (serveur www.ortolang.fr)<br/></li>%n",
							s.substring(0, p), s.substring(p + 1), s.substring(p + 1));
			}
			out.println("</ul>");
		}

		out.println("<script>");
		out.println("initdial(\"trjs\", \"btrjs\", \"oktrjs\");");
		out.println("initdial(\"subt\", \"bsubt\", \"oksubt\");");
		out.println("initdial(\"classic\", \"bclassic\", \"okclassic\");");
		out.println("initdial(\"vid\", \"bvid\", \"okvid\", \"idvid\");");
		out.println("</script>");

		if (listFilesXML.length > 0) {
			out.println("<h4>Métadonnées</h4>");
			out.println("<ul>");
			for (File metaFile : listFilesXML) {
				String fileName = metaFile.getName();
				out.printf("<a href=\"%s\">%s</a> <?php echo getSize(\"%s\"); ?></li>%n", fileName, fileName, fileName);
			}
			out.println("</ul>");
		}
		out.println("</div>");

		out.println("<!-- Début copie footer -->");

		out.println("<footer class=\"well\">");
		out.println("<div class=\"container\">");
		out.println("<div class=\"row\">");
		out.println("<div class=\"span2 bottom-nav\">");
		out.println("<ul class=\"nav nav-list\">");
		out.println("<li class=\"nav-header\">Ortolang</li>");

		out.println("<li <?php if($submenu==\"accueil\") { print(\"class=\\\"active\\\"\"); }?>>");
		out.println("<a href=\"http://portail.ortolang.fr/index.php?page=accueil\" title=\"Accueil\">Accueil </a>");
		out.println("</li>");
		out.println("<li <?php if($submenu==\"presentation\") { print(\"class=\\\"active\\\"\"); }?>>");
		out.println(
				"<a href=\"http://portail.ortolang.fr/index.php?page=presentation\" title=\"Présentation\">Présentation </a>");
		out.println("</li>");
		out.println("<li <?php if($submenu==\"partenaires\") { print(\"class=\\\"active\\\"\"); }?>>");
		out.println(
				"<a href=\"http://portail.ortolang.fr/index.php?page=partenaires\" title=\"Partenaires\">Partenaires </a>");
		out.println("</li>");
		out.println("<li <?php if($submenu==\"roadmap\") { print(\"class=\\\"active\\\"\"); }?>>");
		out.println("<a href=\"http://portail.ortolang.fr/index.php?page=roadmap\" title=\"Roadmap\">Roadmap </a>");
		out.println("</li>");
		out.println("<li <?php if($submenu==\"mentionslegales\") { print(\"class=\\\"active\\\"\"); }?>>");
		out.println(
				"<a href=\"http://portail.ortolang.fr/index.php?page=mentionslegales\" title=\"Mentions Légales\">Mentions Légales </a>");
		out.println("</li>");
		out.println("</ul>");
		out.println("</div>");
		out.println("<div class=\"span2 bottom-nav\">");
		out.println("<ul class=\"nav nav-list\">");
		out.println("<li class=\"nav-header\">Ressources</li>");
		out.println("<li <?php if($submenu==\"facetedsearch\") { print(\"class=\\\"active\\\"\"); }?>>");
		out.println(
				"<a href=\"http://portail.ortolang.fr/search/index.php?search=&rightsOfUse=http%3A%2F%2Fwww.ortolang.fr%2Fontology%2Ffree&rightsOfUse_operator=equal&lang=fr\" title=\"Recherche globale\">Recherche globale </a>");
		out.println("</li>");
		out.println("<li>");
		out.println("<a href=\"http://www.cnrtl.fr/\" title=\"Rechercher sur le CNRTL\" target=\"_BLANK\">CNRTL</a>");
		out.println("</li>");
		out.println("<li>");
		out.println("<a href=\"http://www.sldr.fr\" title=\"Rechercher sur le SLDR\" target=\"_BLANK\">SLDR </a>");
		out.println("</li>");
		out.println("</ul>");
		out.println("</div>");
		out.println("<div class=\"span3 bottom-nav\">");
		out.println("<ul class=\"nav nav-list\">");
		out.println("<li class=\"nav-header\">Documentation</li>");
		out.println("<li>");
		out.println(
				"<a href=\"http://portail.ortolang.fr/index.php?page=documentation\" title=\"Documentation Utilisateur\">Documentation Utilisateur</a>");
		out.println("</li>");
		out.println("<li>");
		out.println("<a href=\"http://dev.ortolang.fr/\" title=\"Site Communautaire\">Site Communautaire</a>");
		out.println("</li>");
		out.println("</ul>");
		out.println("</div>");
		out.println("<div class=\"span2 bottom-nav\">");
		out.println("</div>");
		out.println("<div class=\"span3 bottom-description\">");
		out.println(
				"<blockquote><img src=\"<?php print($SITE_HOST) ?>http://ct3.ortolang.fr/images/mariane-avenir.png\"><br><span class=\"color-highlight\">ORTOLANG</span> <span class=\"infoOrto\">bénéficie d'une aide de l’Etat au titre du programme <a href=\"http://investissement-avenir.gouvernement.fr/\" target=\"_BLANK\" >« Investissements d’avenir »</a> (ANR-­‐11-­‐EQPX-­‐0032) </span> </blockquote>");
		out.println("</div>");
		out.println("</div>");
		out.println("</div>");
		out.println("</footer>");
		out.println("<div class=\"container subfooter\">");
		out.println("<div class=\"row\">");
		out.println("<div class=\"span12\">");
		out.println("<p class=\"pull-right\"><a href=\"<?php print($SITE_HOST) ?>#\">Back to top</a></p>");
		out.println(
				"<p class=\"copyright\">Copyright ©2014 <a href=\"http://dev.ortolang.fr/team-list.html\">&Eacute;quipe Ortolang</a>. All Rights Reserved.</p>");
		out.println("</div>");
		out.println("</div>");
		out.println("</div>");

		out.println("<!-- Le javascript");
		out.println("================================================== -->");
		out.println("<!-- Placed at the end of the document so the pages load faster -->");

		out.println("<!-- Fallback jQuery loading from Google CDN:");
		out.println(
				"http://stackoverflow.com/questions/1014203/best-way-to-use-googles-hosted-jquery-but-fall-back-to-my-hosted-library-on-go -->");
		out.println(
				"<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js\"></script>");
		out.println("<script type=\"text/javascript\">");
		out.println("if (typeof jQuery == 'undefined')");
		out.println("{");
		out.println(
				"document.write(unescape(\"%3Cscript src='./scripts/js/jquery-1.8.3.min.js' type='text/javascript'%3E%3C/script%3E\"));");
		out.println("}");
		out.println("</script>");

		out.println("<script src=\"<?php print($BOOTSTRAP_HOST) ?>scripts/js/bootstrap.min.js\"></script>");

		out.println("<script src=\"<?php print($BOOTSTRAP_HOST) ?>scripts/js/jquery.smooth-scroll.min.js\"></script>");
		out.println("<!-- back button support for smooth scroll -->");
		out.println("<script src=\"<?php print($BOOTSTRAP_HOST) ?>scripts/js/jquery.ba-bbq.min.js\"></script>");
		out.println(
				"<script src=\"<?php print(\"{$HIGHLIGHTJS_HOST}{$HIGHLIGHTJS_JS_HOST}\") ?>highlight.min.js\"></script>");

		out.println("<script src=\"<?php print($SITE_HOST) ?>scripts/js/reflow-skin.js\"></script>");

		out.println("<?php");
		out.println("if(isset($FOOTER_JS)) {");
		out.println("foreach($FOOTER_JS as $JS) {");
		out.println(
				"print(\"<script language=\\\"javascript\\\" type=\\\"text/javascript\\\" src=\\\"$JS\\\"></script>\");");
		out.println("}");
		out.println("}");

		out.println("?>");

		out.println("<!-- Fin copie footer -->");
		out.println("</body></html>");
	}

	public static boolean containsTeimlFile(File dir) {
		File[] listFiles = dir.listFiles();
		for (File f : listFiles) {
			if (f.isFile() && f.getName().endsWith(".tei_corpo.xml")) {
				return true;
			}
		}
		return false;
	}

	public static void main(String args[]) throws IOException {

		if (args.length < 1) {
			System.out.println("Usage: GenerateIndexHTML <dir>");
			return;
		}

		File dir = new File(args[0]);

		if (dir.isDirectory() && containsTeimlFile(dir)) {
			new GenerateIndexHTML(dir);
		}

		String currentDir = args[0];
		for (File f : dir.listFiles()) {
			if (f.isDirectory() && containsTeimlFile(f)) {
				new GenerateIndexHTML(f);
			}
		}
	}
}
