# tei-corpo
Conversion tool from Elan, Clan, Transcriber and Praat files to TEI files and back

### Java library and Swing user interface

Les conversions peuvent être faites en ligne à cette adresse : [http://ct3.ortolang.fr/teiconvert/](http://ct3.ortolang.fr/teiconvert/)

L'outil Java avec interface utilisateur de conversion de formats (TEI_CORPO, CLAN, ELAN, Transcriber, Praat) peut être téléchargé ici :
[conversions.jar](http://ct3.ortolang.fr/tei-corpo/conversions.jar)
Le site github ne contient que les fichiers sources du projet.

__Attention__ : il faut avoir installé Java sur son ordinateur pour exécuter les commandes : [Télécharger Java](http://www.java.com/fr/)

### Utilisation de l'outil de conversion avec interface graphique
L'outil est à utiliser directement depuis le Finder ou Bureau (faire double clic sur le nom de fichier).

  Cet outil est assez simple d'utilisation et permet de faire des conversions en grande quantité sur des fichiers ou des répertoires, en utilisant les paramètres par défaut des commandes. Pour une interface utilisateur plus conviviale et plus puissante, allez sur la version en ligne ([http://ct3.ortolang.fr/teiconvert/](http://ct3.ortolang.fr/teiconvert/) ou testez le logiciel AEEC qui est en développement (voir https://github.com/christopheparisse/aeec).

### Utilisation de l'outil de conversion en ligne de commande
L'outil est utilisable en ligne de commande. Il existe plusieurs commandes qui peuvent être exécutées. Les commandes principales sont regroupées dans une commande générale TeiCorpo.
Les paramètres complémentaires ont la même forme pour toutes les commandes, mais certains paramètres ne s'appliquent qu'à certaines commandes.

__java -cp conversions.jar fr.ortolang.teicorpo.TeiCorpo -from format-entree -to format-sortie fichiers_input -o output [paramètres]__

Toutes les commandes utilisent les mêmes paramètres d'entrée sortie:
  * nom du fichier ou répertoire où se trouvent les fichiers à convertir (peut être précédé de -i)
  * -o nom du fichier de sortie des fichiers ou répertoire des fichiers résultats
  * -from format d'entrée (si -from est omis, le format d'entrée est déduit de l'extension de fichier)
  * -to format de sortie (si -to est omis, le format de sortie est déduit de l'extension de fichier)  

Le nombre d'éléments à convertir n'est pas limité. Par contre un seul paramètre de sortie peut être donné avec -o. Si l'option -o n'est pas spécifié, ou s'il y a plus d'un fichier entrée, le fichier de sortie aura le même nom que le fichier d'entrée, avec une autre extension, et sera stocké au même endroit.
Les paramètres entrée et sortie peuvent être des noms de répertoire.
En entrée, tous les fichiers de l'arborescence correspondant au format de l'option -from (ou tous les fichiers de type connus si pas d'option -from) seront convertis.
En sortie, un nom de répertoire servira d'emplacement pour les fichiers produits.

L'usage de -from et -to est prioritaire sur les informations données par les extensions de fichier.
Les options -from et -to peuvent accepter les valeurs suivantes:
  * clan
  * elan
  * praat
  * transcriber

L'option -to peut accepter les valeurs complémentaires suivantes:
  * txm (fichiers pour TXM)
  * lexico (lexico ou le trameur)
  * text (texte UTF8)
  * srt (sous-titres)

Paramètres complémentaires s'appliquant à toutes les commandes
  * -n niveau: niveau d'imbrication (1 pour lignes principales)
  * -a name : le locuteur/champ name est produit en sortie (caractères génériques acceptés)
  * -s name : le locuteur/champ name est suprimé de la sortie (caractères génériques acceptés)
  * -rawline : produit des fichiers sans codes spécifiques de transcription orale
  * -normalize format : produit des fichiers à partir de sources "format" - format peut valoir "clan" (autres sources à venir)
  * -target format : produit des fichiers vers le "format" - format peut valoir "praat" (autres destinations à venir)

Paramètres supplémentaires pour les exports vers Txm et vers Lexico
  * -tv "type:valeur" : un champ type:valeur est ajouté dans les &lt;w&gt; de txm ou lexico ou le trameur
  * -section : ajoute un indicateur de section en fin de chaque énoncé (pour lexico/le trameur)
  * -sandhi : information spécifique intégrées pour l'analyse des liaisons

Paramètres supplémentaires pour les exports vers du texte
  * -raw seules les transcriptions sont produites sans information de locuteur ou autre
  * -iramuteq les fichiers texte sont complétés par les entêtes pour l'usage d'Iramuteq
  * -concat tous les fichiers sortie sont ajoutés dans un seul fichier output
  * -append les fichiers sont ajoutés au fichier original

La conversion depuis Praat dispose de paramètres supplémentaires
  * -p fichier_de_paramètres: contient les paramètres sous leur format ci-dessous, un jeu de paramètre par ligne.
  * -m nom/adresse du fichier média
  * -e encoding (par défaut detect encoding)
  * -d default UTF8 encoding
  * -t tiername type parent (descriptions des relations entre tiers)
    * types autorisés: - assoc incl symbdiv timediv

Commandes complémentaires (faisant partie de TeiCorpo) :
  * java -cp conversions.jar fr.ortolang.teicorpo.ClanToTei [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.TranscriberToTei [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.PraatToTei [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.ElanToTei [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.TeiToClan [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.TeiToTranscriber [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.TeiToElan [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.TeiToPraat [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.TeiToTxm [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.TeiToLexico [paramètres]
  * java -cp conversions.jar fr.ortolang.teicorpo.TeiToSrt [paramètres]

Commande supplémentaire pour éditer automatiquement les fichiers TEI
  * java -cp conversions.jar fr.ortolang.teicorpo.TeiEdit [paramètres]
  Cette commande permet de modifier les valeurs des champs media, mediamime, docname et les valeurs temporelles dans la timeline
  Pour cela utiliser l'option -c commande=valeur
    * -c media=nom_de_fichier
    * -c mediamime=valeur
    * -c docname=nom-de_fichier (nom interne du document utilisé pour l'interrogation en xml)
    * -c chgtime=valeur (décale tous les repères temporels de 'valeur')
    * -c replace (ne crée pas un nouveau fichier mais remplace l'ancien)
