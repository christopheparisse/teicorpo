<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<!-- Début copie header -->
		<title>Ortolang - Nanterre/Orléans</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="">
		<meta http-equiv="content-language" content="en">

		<link rel="icon" href="images/favicon.ico"/>
		<link href="/stylesheets/bootstrap.min.css" rel="stylesheet">
		<link href="/stylesheets/bootstrap-responsive.min.css" rel="stylesheet">
		<link href="/stylesheets/docs.css" rel="stylesheet">
		<link href="/stylesheets/reflow-skin.css" rel="stylesheet">
		<link href="/styles/github.min.css" rel="stylesheet">
		<link href="/stylesheets/site.css" rel="stylesheet">
		<link href="/stylesheets/print.css" rel="stylesheet" media="print">

		<link rel="stylesheet" type="text/css" href="/stylesheets/indexStyle.css"/>
		<link rel="stylesheet" type="text/css" href="/stylesheets/font-awesome-4.3.0/css/font-awesome.min.css"/>

		<?php
				if(isset($HEADER_CSS)) {
					foreach($HEADER_CSS as $CSS) {
						print("<link rel=\"stylesheet\" type=\"text/css\" href=\"$CSS\"></link>");
						}
				}


				if(isset($HEADER_JS)) {
				        foreach($HEADER_JS as $JS) {
						print("<script language=\"javascript\" type=\"text/javascript\" src=\"$JS\"></script>");
					}
				}

		?>


		<!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
		<!--[if lt IE 9]>
			<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
		<![endif]-->

		<!-- Google Analytics -->
		<script type="text/javascript" async="" src="http://www.google-analytics.com/ga.js"></script><script type="text/javascript">

			var _gaq = _gaq || [];
			_gaq.push(['_setAccount', 'UA-2086251-10']);
			_gaq.push(['_trackPageview']);

			(function() {
				var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
				ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
				var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
			})();

		</script>
		<style type="text/css">
            p { line-height : 1.2; }
        </style>


<!-- Fin copie header -->
</head>

<body>

<!--Début copie header corps -->

<div class="navbar navbar-fixed-top">
			<div class="navbar-inner">
				<div class="container">
					<a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</a>
					<a class="brand" href="/index.php?page=accueil"><img src="/images/ortolang-logo.png" style="height:40px; margin-right:10px;"><span class="color-highlight">ORTOLANG</span> Nanterre/Orléans</a>

					<div class="nav-collapse">
						<ul class="nav pull-right">
							<li class="dropdown <?php if($menu=="ortolang") { print("active"); }?>">
								<a href="/#" class="dropdown-toggle" data-toggle="dropdown">Ortolang <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li <?php if($submenu=="accueil") { print("class=\"active\""); }?>>
										<a href="http://portail.ortolang.fr/index.php?page=accueil" title="Accueil">Accueil </a>
								        </li>

									<li <?php if($submenu=="presentation") { print("class=\"active\""); }?>>
										<a href="http://portail.ortolang.fr/index.php?page=presentation" title="Présentation">Présentation </a>
								        </li>

									<li <?php if($submenu=="partenaires") { print("class=\"active\""); }?>>
										<a href="http://portail.ortolang.fr/index.php?page=partenaires" title="Partenaires">Partenaires </a>
								        </li>


								        <li <?php if($submenu=="roadmap") { print("class=\"active\""); }?>>
										<a href="portail.ortolang.fr/index.php?page=roadmap" title="Roadmap">Roadmap </a>
								        </li>

									<li <?php if($submenu=="mentionslegales") { print("class=\"active\""); }?>>
										<a href="http://portail.ortolang.fr/index.php?page=mentionslegales" title="Mentions Légales">Mentions Légales </a>
								        </li>
								</ul>
							</li>
							<li class="dropdown <?php if($menu=="ressources") { print("active"); }?>">
								<a href="/search.php" class="dropdown-toggle" data-toggle="dropdown">Ressources <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li <?php if($submenu=="facetedsearch") { print("class=\"active\""); }?>><a href="http://portail.ortolang.fr/search/index.php?search=&rightsOfUse=http%3A%2F%2Fwww.ortolang.fr%2Fontology%2Ffree&rightsOfUse_operator=equal&lang=fr" ltitle="Recherche globale">Recherche globale </a></li>
									<li><a href="http://www.cnrtl.fr" title="Recherche sur le CNRTL" target="_BLANK">Recherche sur le CNRTL </a></li>
									<li><a href="http://www.sldr.fr" title="Recherche sur le SLDR" target="_BLANK">Recherche sur le SLDR </a></li>
								</ul>
							</li>
							<li class="dropdown <?php if($menu=="documentation") { print("active"); }?>">
								<a href="http://dev.ortolang.fr/#" class="dropdown-toggle" data-toggle="dropdown">Documentation<b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="http://portail.ortolang.fr/index.php?page=documentation" title="Documentation Utilisateur">Documentation Utilisateur</a></li>
									<li><a href="http://dev.ortolang.fr/" title="Site Communautaire">Site Communautaire</a></li>
								</ul>
							</li>
						</ul>
					</div>
				</div>
			</div>
		</div>

<!-- Fin copie header corps-->



<div class="bodyDiv">


<h2>Ortolang<br/>Nanterre-Orléans<br/></h2>
<h1 id="tei-corpo">tei-corpo</h1>
<p>Conversion tool from Elan, Clan, Transcriber and Praat files to TEI files and back</p>
<h3 id="java-library-and-swing-user-interface">Java library and Swing user interface</h3>
<p>Les conversions peuvent être faites en ligne à cette adresse : <a href="http://ct3.ortolang.fr/teiconvert/">http://ct3.ortolang.fr/teiconvert/</a></p>
<p>L&#39;outil Java avec interface utilisateur de conversion de formats (TEI_CORPO, CLAN, ELAN, Transcriber, Praat) peut être téléchargé ici :<br><a href="http://ct3.ortolang.fr/tei-corpo/conversions.jar">conversions.jar</a><br>Le site github ne contient que les fichiers sources du projet.</p>
<p><strong>Attention</strong> : il faut avoir installé Java sur son ordinateur pour exécuter les commandes : <a href="http://www.java.com/fr/">Télécharger Java</a></p>
<h3 id="utilisation-de-l-outil-de-conversion-avec-interface-graphique">Utilisation de l&#39;outil de conversion avec interface graphique</h3>
<p>L&#39;outil est à utiliser directement depuis le Finder ou Bureau (faire double clic sur le nom de fichier).</p>
<p>  Cet outil est assez simple d&#39;utilisation et permet de faire des conversions en grande quantité sur des fichiers ou des répertoires, en utilisant les paramètres par défaut des commandes. Pour une interface utilisateur plus conviviale et plus puissante, allez sur la version en ligne (<a href="http://ct3.ortolang.fr/teiconvert/">http://ct3.ortolang.fr/teiconvert/</a> ou testez le logiciel AEEC qui est en développement (voir <a href="https://github.com/christopheparisse/aeec">https://github.com/christopheparisse/aeec</a>).</p>
<h3 id="utilisation-de-l-outil-de-conversion-en-ligne-de-commande">Utilisation de l&#39;outil de conversion en ligne de commande</h3>
<p>L&#39;outil est utilisable en ligne de commande. Il existe plusieurs commandes qui peuvent être exécutées. Les commandes principales sont regroupées dans une commande générale TeiCorpo.<br>Les paramètres complémentaires ont la même forme pour toutes les commandes, mais certains paramètres ne s&#39;appliquent qu&#39;à certaines commandes.</p>
<p><strong>java -cp conversions.jar fr.ortolang.teicorpo.TeiCorpo -from format-entree -to format-sortie fichiers_input -o output [paramètres]</strong></p>
<p>Toutes les commandes utilisent les mêmes paramètres d&#39;entrée sortie:</p>
<ul>
<li>nom du fichier ou répertoire où se trouvent les fichiers à convertir (peut être précédé de -i)</li>
<li>-o nom du fichier de sortie des fichiers ou répertoire des fichiers résultats</li>
<li>-from format d&#39;entrée (si -from est omis, le format d&#39;entrée est déduit de l&#39;extension de fichier)</li>
<li>-to format de sortie (si -to est omis, le format de sortie est déduit de l&#39;extension de fichier)  </li>
</ul>
<p>Le nombre d&#39;éléments à convertir n&#39;est pas limité. Par contre un seul paramètre de sortie peut être donné avec -o. Si l&#39;option -o n&#39;est pas spécifié, ou s&#39;il y a plus d&#39;un fichier entrée, le fichier de sortie aura le même nom que le fichier d&#39;entrée, avec une autre extension, et sera stocké au même endroit.<br>Les paramètres entrée et sortie peuvent être des noms de répertoire.<br>En entrée, tous les fichiers de l&#39;arborescence correspondant au format de l&#39;option -from (ou tous les fichiers de type connus si pas d&#39;option -from) seront convertis.<br>En sortie, un nom de répertoire servira d&#39;emplacement pour les fichiers produits.</p>
<p>L&#39;usage de -from et -to est prioritaire sur les informations données par les extensions de fichier.<br>Les options -from et -to peuvent accepter les valeurs suivantes:</p>
<ul>
<li>clan</li>
<li>elan</li>
<li>praat</li>
<li>transcriber</li>
</ul>
<p>L&#39;option -to peut accepter les valeurs complémentaires suivantes:</p>
<ul>
<li>txm (fichiers pour TXM)</li>
<li>lexico (lexico ou le trameur)</li>
<li>text (texte UTF8)</li>
<li>srt (sous-titres)</li>
</ul>
<p>Paramètres complémentaires s&#39;appliquant à toutes les commandes</p>
<ul>
<li>-n niveau: niveau d&#39;imbrication (1 pour lignes principales)</li>
<li>-a name : le locuteur/champ name est produit en sortie (caractères génériques acceptés)</li>
<li>-s name : le locuteur/champ name est suprimé de la sortie (caractères génériques acceptés)</li>
<li>-cleanline : produit des fichiers sans codes spécifiques de transcription orale</li>
<li>-clearchat : produit des fichiers sans codes spécifiques de transcription orale</li>
</ul>
<p>Paramètres supplémentaires pour les exports vers Txm et vers Lexico</p>
<ul>
<li>-tv &quot;type:valeur&quot; : un champ type:valeur est ajouté dans les &lt;w&gt; de txm ou lexico ou le trameur</li>
<li>-section : ajoute un indicateur de section en fin de chaque énoncé (pour lexico/le trameur)</li>
</ul>
<p>Paramètres supplémentaires pour les exports vers du texte</p>
<ul>
<li>-raw seules les transcriptions sont produites sans information de locuteur ou autre</li>
<li>-iramuteq les fichiers texte sont complétés par les entêtes pour l&#39;usage d&#39;Iramuteq</li>
<li>-concat tous les fichiers sortie sont ajoutés dans un seul fichier output</li>
<li>-append les fichiers sont ajoutés au fichier original</li>
</ul>
<p>La conversion depuis Praat dispose de paramètres supplémentaires</p>
<ul>
<li>-p fichier_de_paramètres: contient les paramètres sous leur format ci-dessous, un jeu de paramètre par ligne.</li>
<li>-m nom/adresse du fichier média</li>
<li>-e encoding (par défaut detect encoding)</li>
<li>-d default UTF8 encoding</li>
<li>-t tiername type parent (descriptions des relations entre tiers)<ul>
<li>types autorisés: - assoc incl symbdiv timediv</li>
</ul>
</li>
</ul>
<p>Commandes complémentaires (faisant partie de TeiCorpo) :</p>
<ul>
<li>java -cp conversions.jar fr.ortolang.teicorpo.ClanToTei [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TranscriberToTei [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.PraatToTei [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.ElanToTei [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TeiToClan [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TeiToTranscriber [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TeiToElan [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TeiToPraat [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TeiToTxm [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TeiToLexico [paramètres]</li>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TeiToSrt [paramètres]</li>
</ul>
<p>Commande supplémentaire pour éditer automatiquement les fichiers TEI</p>
<ul>
<li>java -cp conversions.jar fr.ortolang.teicorpo.TeiEdit [paramètres]<br>Cette commande permet de modifier les valeurs des champs media, mediamime, docname et les valeurs temporelles dans la timeline<br>Pour cela utiliser l&#39;option -c commande=valeur<ul>
<li>-c media=nom_de_fichier</li>
<li>-c mediamime=valeur</li>
<li>-c docname=nom-de_fichier (nom interne du document utilisé pour l&#39;interrogation en xml)</li>
<li>-c chgtime=valeur (décale tous les repères temporels de &#39;valeur&#39;)</li>
<li>-c replace (ne crée pas un nouveau fichier mais remplace l&#39;ancien)</li>
</ul>
</li>
</ul>

<p>Il est possible de télécharger la DTD: <a href="http://ct3.ortolang.fr/tei-corpo/tei_all.dtd">[DTD TEI Oral](http://ct3.ortolang.fr/tei-corpo/tei_all.dtd)</a></p>

<p>Les sources du programme sont téléchargeables ici: <a href="https://github.com/christopheparisse/tei-corpo">Site Github TEI-CORPO</a></p>

	<br/>
<?php

	function endsWith($haystack, $needle){
    	return $needle === "" || substr($haystack, -strlen($needle)) === $needle;
 	}

	function getHTMLfiles($dirname){
		$dir = opendir($dirname);
		while($file = readdir($dir)) {
			if($file != '.' && $file != '..' && !is_dir($dirname.$file) && endsWith($file,'teiml')){
				$containVisu = true;
				echo '<span class="diams">&diams;&nbsp;</span><a class="visuLink" href="'.$dirname.'">'.basename($file, '.teiml').'</a>'.'<br/><br/>';
			}
		}
		closedir($dir);
	}

	function explorer($chemin){
    		$lstat    = lstat($chemin);
    		$mtime    = date('d/m/Y H:i:s', $lstat['mtime']);
    		$filetype = filetype($chemin);

    		getHTMLfiles($chemin);
    		// Si $chemin est un dossier => on appelle la fonction explorer() pour chaque élément (fichier ou dossier) du dossier $chemin
    		if( is_dir($chemin) ){
        		$me = opendir($chemin);;
        		while( $child = readdir($me) ){
            			if( $child != '.' && $child != '..' ){
                			explorer( $chemin.DIRECTORY_SEPARATOR.$child );
            			}
        		}
    		}
	}

	function buildLinks($dirname){
		$corpus = array_diff(scandir($dirname), array('.', '..', 'bilingue', 'exemples'));
		foreach($corpus as $c){
			explorer($dirname.'/'.$c);
		}
	}

?>
</div>

<!-- Début copie footer -->

<footer class="well">
		<div class="container">
			<div class="row">
				<div class="span2 bottom-nav">
					<ul class="nav nav-list">
						<li class="nav-header">Ortolang</li>

						<li <?php if($submenu=="accueil") { print("class=\"active\""); }?>>
							<a href="http://portail.ortolang.fr/index.php?page=accueil" title="Accueil">Accueil </a>
						</li>
						<li <?php if($submenu=="presentation") { print("class=\"active\""); }?>>
							<a href="http://portail.ortolang.fr/index.php?page=presentation" title="Présentation">Présentation </a>
						</li>
						<li <?php if($submenu=="partenaires") { print("class=\"active\""); }?>>
							<a href="http://portail.ortolang.fr/index.php?page=partenaires" title="Partenaires">Partenaires </a>
						</li>
						<li <?php if($submenu=="roadmap") { print("class=\"active\""); }?>>
							<a href="http://portail.ortolang.fr/index.php?page=roadmap" title="Roadmap">Roadmap </a>
						</li>
						<li <?php if($submenu=="mentionslegales") { print("class=\"active\""); }?>>
							<a href="http://portail.ortolang.fr/index.php?page=mentionslegales" title="Mentions Légales">Mentions Légales </a>
						</li>
					</ul>
				</div>
				<div class="span2 bottom-nav">
					<ul class="nav nav-list">
						<li class="nav-header">Ressources</li>
						<li <?php if($submenu=="facetedsearch") { print("class=\"active\""); }?>>
							<a href="http://portail.ortolang.fr/search/index.php?search=&rightsOfUse=http%3A%2F%2Fwww.ortolang.fr%2Fontology%2Ffree&rightsOfUse_operator=equal&lang=fr" title="Recherche globale">Recherche globale </a>
						</li>
						<li>
							<a href="http://www.cnrtl.fr/" title="Rechercher sur le CNRTL" target="_BLANK">CNRTL </a>
						</li>
						<li>
							<a href="http://www.sldr.fr" title="Rechercher sur le SLDR" target="_BLANK">SLDR </a>
						</li>
					</ul>
				</div>
				<div class="span3 bottom-nav">
					<ul class="nav nav-list">
						<li class="nav-header">Documentation</li>
						<li>
							<a href="http://portail.ortolang.fr/index.php?page=documentation" title="Documentation Utilisateur">Documentation Utilisateur</a>
						</li>
						<li>
							<a href="http://dev.ortolang.fr/" title="Site Communautaire">Site Communautaire</a>
						</li>
					</ul>
				</div>
				<div class="span2 bottom-nav">
				</div>
				<div class="span3 bottom-description">
					<blockquote><img src="/images/mariane-avenir.png"><br><span class="color-highlight">ORTOLANG</span> <span class="infoOrto">bénéficie d'une aide de l’Etat au titre du programme <a href="http://investissement-avenir.gouvernement.fr/" target="_BLANK" >« Investissements d’avenir »</a> (ANR-­‐11-­‐EQPX-­‐0032) </span> </blockquote>
				</div>
			</div>
		</div>
	</footer>

	<div class="container subfooter">
		<div class="row">
			<div class="span12">
				<p class="pull-right"><a href="/#">Back to top</a></p>
				<p class="copyright">Copyright ©2014 <a href="http://dev.ortolang.fr/team-list.html">&Eacute;quipe Ortolang</a>. All Rights Reserved.</p>
			</div>
		</div>
	</div>

	<!-- Le javascript
	================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->

	<!-- Fallback jQuery loading from Google CDN:
	     http://stackoverflow.com/questions/1014203/best-way-to-use-googles-hosted-jquery-but-fall-back-to-my-hosted-library-on-go -->
	<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js"></script>
	<script type="text/javascript">
		if (typeof jQuery == 'undefined')
		{
			document.write(unescape("%3Cscript src='./scripts/js/jquery-1.8.3.min.js' type='text/javascript'%3E%3C/script%3E"));
		}
	</script>

	<script src="/scripts/js/bootstrap.min.js"></script>

	<script src="/scripts/js/jquery.smooth-scroll.min.js"></script>
	<!-- back button support for smooth scroll -->
	<script src="/scripts/js/jquery.ba-bbq.min.js"></script>
	<script src="<?php print("{$HIGHLIGHTJS_HOST}{$HIGHLIGHTJS_JS_HOST}") ?>highlight.min.js"></script>

	<script src="/scripts/js/reflow-skin.js"></script>

	<?php
		if(isset($FOOTER_JS)) {
			foreach($FOOTER_JS as $JS) {
				print("<script language=\"javascript\" type=\"text/javascript\" src=\"$JS\"></script>");
			}
		}

	?>

<!-- Fin copie footer -->
</body>
</html>
