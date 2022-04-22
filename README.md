# TEI-CORPO
Conversion tool from Elan, Clan, Transcriber and Praat files to TEI files and back

### Java library and Swing user interface

Conversions can be made at this address without using the commande line interface: [http://ct3.ortolang.fr/teiconvert/](http://ct3.ortolang.fr/teiconvert/)

The Java conversion tool (formats TEI_CORPO, CLAN, ELAN, Transcriber, Praat) can be downloaded here:
[teicorpo.jar](http://ct3.ortolang.fr/tei-corpo/teicorpo.jar)

(Note: The filename *teicorpo.jar* has changed since version 1.40. Previous name was conversion.jar)

__Warning__ : Java (version >= 8) has to be installed first on your computer to execute commands: [Download Java](http://www.java.com/fr/)

The source code can be found here [https://github.com/christopheparisse/teicorpo](https://github.com/christopheparisse/teicorpo)
The github website contains only the source of the project, not the compiled jar file.

### Using the command line conversion tool
The tool can be used as a command line tool. There are several subprograms in the jar file. The main commands are grouped together in a general command which is called TeiCorpo. Other specific command can be useful to execute part of speech tagging or to edit the TEI files. The same general set of parameters applies to all command. Some parameters are command specific, however. The general command has the following form:

__java -cp teicorpo.jar fr.ortolang.teicorpo.TeiCorpo -from input-format -to output-format input_files ... -o output [parameters]__

All commands use the same input and output parameters:
  * name of the file or directory where all files to be converted are (might be preceded by -i for more clarity)
  * -o name of the output file or name of the output directory
  * -from input format (if -from is omitted, the input format is deduced from the file extension)
  * -to output format (if -to is omitted, the output format is deduced from the file extension)  

The number of files to be converted (input) is not limited. However, only one output parameter can be set. If -o is not set, or if there are more than one input file, the name of the output file will be derived from the name of the input. If no output directory is specified, the output files will be in the same repertory as the input files.
The input and output parameters can be repertory names. If the input parameter is a repertory, all files in the file subtree will be converted and placed accordingly in the output file tree.

The use of -from and -to takes precedence on information provided by file extensions.
These options (-from and -to) can take the following arguments (all these options correspond to the default format used by the tools):
  * clan
  * elan
  * praat
  * transcriber
  * text (raw text files)
  * srt (subtiles files)

The -to option can also take the following arguments:
  * txm (files for TXM)
  * lexico (lexico or le trameur)
  * text (UTF8 text)
  * srt (subtitles)

Other parameters that apply to all commands:
  * -n level: imbrication level (1 for main lines)
  * -a name : speaker or field produced as output (wildcard characters can be used)
  * -s name : speaker or field removed as output (wildcard characters can be used)
  * -rawline : produce files without codes specific to spoken language transcription
  * -normalize format : produce files from sources of "format" - format can be "clan"
  * -target format : produce files towards "format" - format can be "praat"
  * -m name/path for the media file (useful for Praat, Text and Srt files)

Other parameters for exports towards Txm and Lexico
  * -tv "type:value" : a field type:value is added to the &lt;w&gt; of Txm or Lexico or Le Trameur
  * -section : add section marker at the end of an utterance (for Lexico/Le Trameur)
  * -sandhi : information specific for the study of liaisons

Other parameters for exports towards text files
  * -raw transcriptions are produced without speaker or any information
  * -iramuteq text files takes supplementary information for Iramuteq (headers)
  * -concat all output files are concatenated into a single output file
  * -append files are append to the original output file
  * -tiernames : print the value of the locutors and tiernames in the transcriptions
  * -tierxmlid : insert an xml id after the tiernames (can be used to find the tier in the xml file)

Parameter for import from text
  * -normalize noparticipant : the first word of a paragraph is not considered as the name of the speaker

Conversion from Praat can use some specific parameters
  * -p parameters_file: contains parameters with the format below, one parameter per line.
  * -m name/path for the media file
  * -e encoding (by default detect encoding)
  * -d default UTF8 encoding
  * -t tiername type parent (description of the relationship between tiers)
    * possible types: - assoc incl symbdiv timediv (same as ELAN linguistic types: assoc = Symbolic Association, incl = Included In, symbdiv = Symbolic Subdivision, timediv = Time Subdivision)

Other commands (all are part of TeiCorpo command) :
  * java -cp teicorpo.jar fr.ortolang.teicorpo.ClanToTei [parameters] (process Chat files, Srt files and Text files)
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TranscriberToTei [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.PraatToTei [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.ElanToTei [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiToClan [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiToTranscriber [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiToElan [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiToPraat [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiToTxm [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiToLexico [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiToSrt [parameters]

Other commands for editing/changing TEI files
  * java -cp teicorpo.jar fr.ortolang.teicorpo.ImportConllToTei [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TcofInserMeta [parameters]
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiInsertCsv [parameters]

Jar files to download for the TeiInsertCsv command: 
[commons-lang3-3.9.jar](http://ct3.ortolang.fr/tei-corpo/commons-lang3-3.9.jar) et 
[opencsv-5.0.jar](http://ct3.ortolang.fr/tei-corpo/opencsv-5.0.jar)

Other commands to edit automatically TEI files
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiEdit [parameters]
  This command allows modifying the values of the fields media, mediamime, docname and the temporal values in the timeline

  To do this, use option -c command=value
    * -c media=filename
    * -c mediamime=value
    * -c docname=filename (internal name of the document used for xml queries)
    * -c chgtime=value (shift all temporal information by value)
    * -c replace (do not create a file but replace the old one)

#### Use of TreeTagger to tag in part of speech a TEI file

  * put the tree-tagger software (to download here: http://www.cis.uni-muenchen.de/~schmid/tools/TreeTagger/) and the parameter file in the current directory (model for example english-utf8.par or spoken-french.par)
  * or use the environment variable TREE_TAGGER to give the location of the TreeTagger files
  * COMMAND TO USE: **java -cp teicorpo.jar fr.ortolang.teicorpo.TeiTreeTagger -syntaxformat conll|ref|w -model name_of_modele**
  * The parameter -program allows to give directly the address of the TreeTagger on your computer
  * The parameter -model allows to give directly the address of the TreeTagger language model on your computer
  * FOR EXAMPLE: **java -cp teicorpo.jar fr.ortolang.teicorpo.TeiTreeTagger  -program "tree-tagger.exe" -model english-bnc.par myfile.tei_corpo.xml -syntaxformat conll**
  * The -syntaxformat conll parameter allows an efficient export to ELAN or PRAAT by creating a CONLL structure that do not modify the orginal main line
  * The optional parameter -rawline allows cleaning the orthographic line for specific codes (as best as possible)

  * Use example with TreeTagger module and PERCEO model installed in the directory /projets/syntax . The shell command analyse.sh contains:

```
TREE_TAGGER=/projets/syntax
export TREE_TAGGER
java -cp /projets/plceforlibraries/teicorpo.jar fr.ortolang.teicorpo.TeiTreeTagger -syntaxformat conll -model perceo_oral/spoken-french.par -rawline $1
```

  * The soft is executed with the command `sh analyse.sh filename`
  * The result is found in a file with the extension `.tei_corpo_ttg.tei_corpo.xml`
  * This result file can be easily converted to ELAN or PRAAT formats
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiCorpo -to elan filename.tei_corpo_ttg.tei_corpo.xml
  * java -cp teicorpo.jar fr.ortolang.teicorpo.TeiCorpo -to praat filename.tei_corpo_ttg.tei_corpo.xml

#### Stanford Natural Language Processing (SNLP)

The Stanford parser, part of speech tagger, and other tools can be called to process the content of the TEI file. The results, as for the TreeTagger program come in three formats.

* first download the jar files from SNLP including the language models that you can to use from this page: (https://stanfordnlp.github.io/CoreNLP/index.html#download)
* COMMAND TO USE: **java -cp "teicorpo.jar:directory_for_SNLP/*" fr.ortolang.teicorpo.TeiSNLP -syntaxformat conll|dep|ref|w -model english|french|filename.properties filename.tei_corpo.xml**
* jar files for CoreNlp and the jar language models can be found here [https://stanfordnlp.github.io/CoreNLP/](https://stanfordnlp.github.io/CoreNLP/).
* valid properties files can be found here [http://ct3.ortolang.fr/tei-corpo/properties/](http://ct3.ortolang.fr/tei-corpo/properties/).
* the conll and dep parameters provide the result in conll format (columns x lines). The pos paramter provides the result in "ref" format (see TreeTagger)
* as for TreeTagger, conll format can be converted to Praat and Elan, and ref format to TXM and Lexico.

#### Specific options - Conversion of metadata from ORFEO and TCOF
ORFEO (CEFC) and TCOF are two corpora found on Ortolang (www.ortolang.fr). They contains both transcriptions in a specific format (CONLL for ORFEO and
Transcriber for TCOF) plus some metadata in XML format (TEI format for ORFEO and proprietary for TCOF). Both corpora can be converted using TEICORPO.

##### ORFEO
The command is :
```
java -cp teicorpo.jar fr.ortolang.teicorpo.ImportConllToTei conllfilename -metadata metadatafilename -o outputfilename
```

##### TCOF
The command is :
```
java -cp teicorpo.jar fr.ortolang.teicorpo.TcofInserMeta trsfilename -metadata metadatafilename -o outputfilename
```

### History version
  - 1.00  Initial fully functional version
  - 1.40  Rename jar file. English version.
  - 1.45.00 Jar includes main class. Automatic tests added to the software generation.
