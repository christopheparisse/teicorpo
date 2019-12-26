package fr.ortolang.teicorpo;

import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ie.util.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.*;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.ud.CoNLLUDocumentWriter;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.*;

public class SNLP {
	
	StanfordCoreNLP pipeline;

	public void init(String style, String model) {
		// set up pipeline properties
		Properties props = new Properties();
		if (model.indexOf(".properties") > 0) {
			// Setup language Properties by loading them from classpath resources
			// example: "StanfordCoreNLP-chinese.properties"
			try {
				props.load(IOUtils.readerFromString(model));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.printf("Cannot read %s file.%n%s%n", model, e.toString());
				// e.printStackTrace();
				return;
			}
		} else if (model.equals("french")) {
			// set the list of annotators to run
			if (style.equals("conll"))
				props.setProperty("annotators", "tokenize,ssplit,pos,parse,depparse");
			else
				props.setProperty("annotators", "tokenize,ssplit,pos");
			props.setProperty("tokenize.language", "fr");
			props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/french/french.tagger");
			props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz");
			props.setProperty("depparse.model", "edu/stanford/nlp/models/parser/nndep/UD_French.gz");
			props.setProperty("depparse.language", "french");

			/*
			annotators = tokenize, ssplit, pos, parse
			tokenize.language = fr
			pos.model = edu/stanford/nlp/models/pos-tagger/french/french.tagger
			parse.model = edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz
			# dependency parser
			depparse.model = edu/stanford/nlp/models/parser/nndep/UD_French.gz
			depparse.language = french
					 */
		} else { // english
			// set the list of annotators to run
			if (style.equals("conll"))
				props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse,coref,kbp,quote");
			else if (style.equals("dep"))
				props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,depparse");
			else
				props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
		}

		// set a property for an annotator, in this case the coref annotator is being set to use the neural algorithm
		props.setProperty("coref.algorithm", "neural");
		// build pipeline
		pipeline = new StanfordCoreNLP(props);
		System.out.println("Ready for SNLP processing!");
	}

	public static String textTest = "Joe Smith was born in California. " +
			"In 2017, he went to Paris, France in the summer. " +
			"His flight left at 3:00pm on July 10th, 2017. " +
			"After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
			"He sent a postcard to his sister Jane Smith. " +
			"After hearing about Joe's trip, Jane decided she might go to France one day.";
	
	public static String readFileToText(String fn) {
		String s = "";
		BufferedReader reader = null;
			String line;
			try {
				reader = new BufferedReader( new InputStreamReader(new FileInputStream(fn), "UTF-8") );
				while((line = reader.readLine()) != null) {
					// Traitement du flux de sortie de l'application si besoin est
					s += line;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return s;
	}

	public static void main(String[] args) {
		if (args[0].equals("test")) {
			//File f = new File(args[0]);
			//String text = FileUtils.readFileToString(f, "utf-8");
			SNLP snlp = new SNLP();
			snlp.init("dep", "english");
			snlp.parseCONLL(textTest, System.out);
		} else if (args[0].equals("test2")) {
			//File f = new File(args[0]);
			//String text = FileUtils.readFileToString(f, "utf-8");
			SNLP snlp = new SNLP();
			snlp.init("conll", "english");
			snlp.parseCONLL(textTest, System.out);
		} else if (args[0].equals("pos")) {
			String text = SNLP.readFileToText(args[1]);
			SNLP snlp = new SNLP();
			snlp.init("pos", "english");
			int nb = snlp.parseWPLN(text, System.out, 0);
			System.out.printf("<Total sentences=%d>%n", nb);
		} else if (args[0].equals("conll")) {
			String text = SNLP.readFileToText(args[1]);
			SNLP snlp = new SNLP();
			snlp.init("conll", "english");
			snlp.parseCONLL(text, System.out);
		} else if (args[0].equals("dep")) {
			String text = SNLP.readFileToText(args[1]);
			SNLP snlp = new SNLP();
			snlp.init("dep", "english");
			snlp.parseCONLL(text, System.out);
		}
	}
	
	public List<String[]> parseWPLN(String text) {
		text = text.trim();
		if (text.length() <= 0) return null;
		try {
			// create a document object
			CoreDocument document = new CoreDocument(text);
			// annnotate the document
			pipeline.annotate(document);
	
			// nb tokens
			int nbTokens = document.tokens().size();
			List<String[]> lls = new ArrayList<String[]>(nbTokens);
			for (int i=0; i < nbTokens; i++) {
				CoreLabel token = document.tokens().get(i);
				String[] t = new String[4];
				t[0] = token.word();
				t[1] = token.tag();
				t[2] = token.lemma();
				t[3] = token.ner();
				lls.add(t);
			}
			return lls;
		} catch (Exception e) {
			System.err.printf("Error processing: %s%n", text);
			e.printStackTrace();
			return null;
		}
	}
	
	public int parseWPLN(String text, PrintStream os, int numSent) {
		text = text.trim();
		if (text.length() <= 0) return 0;
		try {
			// create a document object
			CoreDocument document = new CoreDocument(text);
			// annnotate the document
			pipeline.annotate(document);
	
			// nb sentences
			int nbSentences = document.sentences().size();
			
			for (int i=0; i < nbSentences; i++) {
				System.out.printf("<sent n=%d>%n", ++numSent);
				CoreSentence sentence = document.sentences().get(i);
				List<CoreLabel> listTokens = sentence.tokens();
				int nbWords = listTokens.size();
				for (int j=0; j < nbWords; j++) {
					os.printf("%s\t", listTokens.get(j).word());
					os.printf("%s\t", listTokens.get(j).tag());
					os.printf("%s\t", listTokens.get(j).lemma());
					os.printf("%s\t", listTokens.get(j).ner());
					os.printf("%n");
				}
			}
			
			return numSent;
		} catch (Exception e) {
			System.err.printf("Error processing: %s%n", text);
			e.printStackTrace();
			return 0;
		}

		/*
		// examples

		// 10th token of the document
		CoreLabel token = document.tokens().get(10);
		System.out.println("Example: token");
		System.out.println(token);
		System.out.println();

		token.category()
		token.lemma()
		token.word()

		// text of the first sentence
		String sentenceText = document.sentences().get(0).text();
		System.out.println("Example: sentence");
		System.out.println(sentenceText);
		System.out.println();

		// nb sentences
		int nbSentences = document.sentences().size();

		// second sentence
		CoreSentence sentence = document.sentences().get(1);

		// list of the part-of-speech tags for the second sentence
		List<String> posTags = sentence.posTags();
		System.out.println("Example: pos tags");
		System.out.println(posTags);
		System.out.println();

		List<CoreLabel> st = sentence.tokens();

		// list of the ner tags for the second sentence
		List<String> nerTags = sentence.nerTags();
		System.out.println("Example: ner tags");
		System.out.println(nerTags);
		System.out.println();

		// constituency parse for the second sentence
		Tree constituencyParse = sentence.constituencyParse();
		System.out.println("Example: constituency parse");
		System.out.println(constituencyParse);
		System.out.println();

		// dependency parse for the second sentence
		SemanticGraph dependencyParse = sentence.dependencyParse();
		System.out.println("Example: dependency parse");
		System.out.println(dependencyParse);
		System.out.println();

		// kbp relations found in fifth sentence
		List<RelationTriple> relations =
				document.sentences().get(4).relations();
		System.out.println("Example: relation");
		System.out.println(relations.get(0));
		System.out.println();

		// entity mentions in the second sentence
		List<CoreEntityMention> entityMentions = sentence.entityMentions();
		System.out.println("Example: entity mentions");
		System.out.println(entityMentions);
		System.out.println();

		// coreference between entity mentions
		CoreEntityMention originalEntityMention = document.sentences().get(3).entityMentions().get(1);
		System.out.println("Example: original entity mention");
		System.out.println(originalEntityMention);
		System.out.println("Example: canonical entity mention");
		System.out.println(originalEntityMention.canonicalEntityMention().get());
		System.out.println();

		// get document wide coref info
		Map<Integer, CorefChain> corefChains = document.corefChains();
		System.out.println("Example: coref chains for document");
		System.out.println(corefChains);
		System.out.println();

		// get quotes in document
		List<CoreQuote> quotes = document.quotes();
		CoreQuote quote = quotes.get(0);
		System.out.println("Example: quote");
		System.out.println(quote);
		System.out.println();

		// original speaker of quote
		// note that quote.speaker() returns an Optional
		System.out.println("Example: original speaker of quote");
		System.out.println(quote.speaker().get());
		System.out.println();

		// canonical speaker of quote
		System.out.println("Example: canonical speaker of quote");
		System.out.println(quote.canonicalSpeaker().get());
		System.out.println();
		*/

	}
	
	public void parseCONLL(String text, PrintStream os) {
		text = text.trim();
		if (text.length() <= 0) return;
		try {
			// create a document object
//			System.err.printf("-:%s%n", text);
			CoreDocument document = new CoreDocument(text);
			final CoNLLUDocumentWriter conllUWriter = new CoNLLUDocumentWriter();
			
			pipeline.annotate(document);
			Annotation annotation = document.annotation();
			// CoNLLUOutputter.conllUPrint(annotation, os);
			List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//			for (CoreMap sentence : sentences) {
			for (int nb=0; nb < sentences.size(); nb++) {
				String s = getSentenceAnalysis(sentences, nb, conllUWriter);
				os.printf("<Sentence:%d>%n", nb);
				os.printf("%s%n", s);
			}
		} catch (Exception e) {
			System.err.printf("Error processing: %s%n", text);
			e.printStackTrace();
			return;
		}
	}

	String getSentenceAnalysis(List<CoreMap> sentences, int nb, CoNLLUDocumentWriter conllUWriter) {
		CoreMap sentence = sentences.get(nb);
		SemanticGraph sg = sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
		String s;
		if (sg != null) {
			s = conllUWriter.printSemanticGraph(sg);
		} else {
			s = conllUWriter.printPOSAnnotations(sentence);
		}
		return s;
	}

	public ConllUtt parseCONLL(String text) {
		ConllUtt cu = new ConllUtt();
		text = text.trim();
		if (text.length() <= 0) return null;
		try {
			// create a document object
//			System.err.printf("-:%s%n", text);
			CoreDocument document = new CoreDocument(text);
			final CoNLLUDocumentWriter conllUWriter = new CoNLLUDocumentWriter();
			
			pipeline.annotate(document);
			Annotation annotation = document.annotation();
			// CoNLLUOutputter.conllUPrint(annotation, os);
			List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			int nbs = sentences.size();
			for (int nb=0; nb < nbs; nb++) {
				String s = getSentenceAnalysis(sentences, nb, conllUWriter);
				//System.out.printf("parseCONLL: {%s}%n", s);
				String[] wls = s.split("\n");
				//System.out.printf("parseCONLL2: %d%n", wls.length);
				for (int l=0; l < wls.length; l++) {
					ConllWord cw = new ConllWord(wls[l]);
					cu.words.add(cw);
					//System.out.printf("parseCONLL3: %s%n", cu.toString());
				}
			}
			//System.out.printf("parseCONLL4: %d%n", cu.words.size());
			return cu;
		} catch (Exception e) {
			System.err.printf("Error processing: %s%n", text);
			e.printStackTrace();
			return null;
		}
	}
}