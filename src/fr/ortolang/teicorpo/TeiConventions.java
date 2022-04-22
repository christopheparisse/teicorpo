package fr.ortolang.teicorpo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TeiConventions {

	/* ****************************************************************************************************************************************************** */
	/* ********************************************************* MOTS ORTHOGRAPHIQUES *********************************************************************** */
	/* ****************************************************************************************************************************************************** */

	/* *** Mots indistincts ou inaudibles *** */
	//Convention : $ X ?/$
	public static String indistinct(String s){
		s = s.replaceAll("(?i)x(\\s*x)+", "\\$ X ?/\\$");			
		s = s.replaceAll("\\*(\\s*\\*)+", "\\$ X ?/\\$");
		return s;
	}


	/* ***Acronymes & sigles *** */
	//Convention : $...S/$
	public static String acroEtSigles(String s){
		//Codage dans corpus: tout en majuscule
		Pattern pattern1 = Pattern.compile("(\\s|^)(([A-Z]){2,}(\\s|$))+");
		Matcher m1 = pattern1.matcher(s);
		while(m1.find()){
			String match = m1.group(0);
			s = s.replaceAll(match, m1.group(1) + "\\$" + m1.group(2) + " S/\\$ ").trim();
		}
		return s;
	}

	//Lettres, mots épelés
	//Convention : $ x x x L/$
	public static String lettres(String s){
		//Codage dans corpus: [x x x, transPhon], x x x, X X X
		Pattern pattern1 = Pattern.compile("(\\s|^|\\[)(([A-Za-z](\\s|$)){2,})");
		Matcher m1 = pattern1.matcher(s);
		while(m1.find()){
			String match = m1.group();
			s = s.replaceAll(match, m1.group(1) + "\\$" + m1.group(2).toUpperCase() + " L/\\$ ").trim();
		}
		return s;
	}

	/* ***Titres *** */
	//Convention : $...O/$
	public static String titres(String s){
		//Codage dans corpus: entre guillemets
		Pattern p = Pattern.compile("(\\s|^)(\")((\\s?[A_Za-z\\p{Punct}&&[^\"]]\\s?)+)(\")(\\s|$)");
		Matcher m = p.matcher(s);
		while (m.find()){
			String match = m.group();
			s = s.replace(match, m.group(1) + "$" + m.group(3) + " 0/$" + m.group(6));
		}
		return s;
	}


	/* *** Multi-transcription, alternance codique, mots langue étrangère *** */
	//Convention : $ ..., ..., ... X/$
	public static String codeAlternance(String s){
		//Codage:
		//Corpus COR : (...; ...)
		//Corpus GARS : /..., ..., .../
		//Pour langue étrangère: ... {lang=...} ou ...+[lang=...]
		ArrayList <Matcher> matchers = new ArrayList <Matcher>();
		//Cas (...; ...)
		Pattern p1 = Pattern.compile("(\\()([A-Za-z]+(\\s?;\\s?[A-Za-z]+)+)(\\))"); 
		//Cas /..., ..., .../
		Pattern p2 = Pattern.compile("(/)([A_Za-z\\p{Punct}&&[^,]]+(,.[^/]+)+)(/)");
		//Cas ... {lang=...}
		Pattern p3 = Pattern.compile("(\\{)(lang\\s*=\\s*[A-Za-z]+)(\\})");
		// Cas ...+[lang=...]
		Pattern p4 = Pattern.compile("(\\+\\s*\\[)(lang\\s*=\\s*[A-Za-z]+)(\\])");
		Matcher m1 = p1.matcher(s);
		Matcher m2 = p2.matcher(s);
		Matcher m3 = p3.matcher(s);
		Matcher m4 = p4.matcher(s);
		matchers.add(m1);
		matchers.add(m2);
		matchers.add(m3);
		matchers.add(m4);
		for(Matcher m : matchers){
			while(m.find()){
				//System.out.println(m.group());
				s = s.replace(m.group(0), " $ " + m.group(2).replaceAll(";", ",") + " X/$");
			}
		}			
		return s;
	}

	/* *** Amorces de mots *** */
	//Convention : ...-
	public static String amorces(String s){
		//Codage dans corpus: ...-, ...()
		Pattern pattern1 = Pattern.compile("([A-Za-z]+\\(\\)(\\s|$))");
		Matcher m1 = pattern1.matcher(s);
		while(m1.find()){
			String match = m1.group().replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
			s = s.replaceAll(match, match.replaceAll("\\\\\\([A-Za-z]*\\\\\\)", "-"));
		}
		return s;
	}

	//Ellision
	//Convention : (...)
	public static String ellision(String s){
		Pattern p = Pattern.compile("([A-Za-z]+)(`)([A-Za-z]+)");
		Matcher m = p.matcher(s);		
		while (m.find()){
			//System.out.println("Ellision => " + m.group() );
			s = s.replace(m.group(), m.group(1) + "()" + m.group(3));
		}
		return s;
	}


	/* *** Parties non transcrites *** */
	//Concention : $ xxx /$
	public static String nontrans(String s){
		//Codage dans corpus: balise nontrans dans transcriber, ICAR (( ... )), CFPP [...], GARS ###{...}
		ArrayList <Matcher> matchers = new ArrayList <Matcher> ();
		//Cas (( ... )) attention : pas possible d'avoir parenthèse fermante dans le commentaire 
		Pattern p1 = Pattern.compile("\\(\\((.[^\\)]+)\\)\\)"); 
		Matcher m1 = p1.matcher(s);
		matchers.add(m1);
		//Cas ###{...}
		Pattern p2 = Pattern.compile("###\\s*\\{(.[^\\}]+)\\}");
		Matcher m2 = p2.matcher(s);
		matchers.add(m2);
		for(Matcher m : matchers){
			while(m.find()){
				s = s.replace(m.group(), "$ " + m.group(1) + " /$");
				//System.out.println(m.group(1));
			}
		}
		return s;
	}

	//A ajouter dans cette partie : toponymes, patronymes


	/* ****************************************************************************************************************************************************** */
	/* ********************************************************* MOTS PHONETIQUES/PHONOLOGIQUES ************************************************************* */
	/* ****************************************************************************************************************************************************** */

	//Transcription API ou Sampa
	//Convention : [mot, transcription api ou sampa] ou [transcription api ou sampa]
	public static String phonTrans(String s){
		//Cadages dans corpus:
		//Transcription directe : balise pronounce dans Transcriber, LPL: [*, transcription], GARS * {pron = [gaz]}
		//Transcription + mot : balise pronounce dans Transcriber, GARS ... {pron = [...]}, LI * ... {api ou sampa}

		//Transcription directe:
		ArrayList <Matcher> matchers1 = new ArrayList<Matcher>();
		//Cas [*, transcription]
		Pattern p1 = Pattern.compile("\\[\\s*\\*\\s*,\\s*(([^\\s\\]]+\\s*)+)\\]");
		Matcher m1 = p1.matcher(s);
		matchers1.add(m1);
		//Cas * {pron = [gaz]}
		Pattern p2 = Pattern.compile("\\*\\s*\\{\\s*pron\\s*=\\s*\\[(([^\\s\\]]+\\s*)+)\\]\\s*\\}");
		Matcher m2 = p2.matcher(s);
		matchers1.add(m2);
		for(Matcher matcher1 : matchers1){
			while(matcher1.find()){
				//System.out.println(matcher1.group(1));
				s = s.replace(matcher1.group(), "[" + matcher1.group(1) + "]");
			}
		}

		//Transcription phonétique + orthographique:
		ArrayList <Matcher> matchers2 = new ArrayList<Matcher>();
		//Cas ... {pron = [...]}
		Pattern p3 = Pattern.compile("([A-Za-z]+)\\s*\\{\\s*pron\\s*=\\s*\\[(([^\\s\\]]+\\s*)+)\\]\\s*\\}");
		Matcher m3 = p3.matcher(s);
		matchers2.add(m3);
		//Cas * ... {api ou sampa}
		Pattern p4 = Pattern.compile("\\*\\s*([A-Za-z]+)\\s*\\{\\s*((\\S+\\s*)+)\\s*\\}");
		Matcher m4 = p4.matcher(s);
		matchers2.add(m4);
		for(Matcher matcher2 : matchers2){
			while(matcher2.find()){
				//System.out.println(matcher2.group(2));
				s = s.replace(matcher2.group(), "[" + matcher2.group(1) + ", " + matcher2.group(2) + "]");
			}
		}
		return s;
	}



	/* ****************************************************************************************************************************************************** */
	/* ********************************************************* ENONCE ************************************************************************************* */
	/* ****************************************************************************************************************************************************** */


	//Bruits
	//Convention : * ...  B/*
	//Fait dans TeiFile : Balise incident

	//Variantes paralinguistiques
	//Convention : * ...  P/*
	public static String paraLgq(String s){
		//Cadage: chat = [=! ... ], sinon balise transcriber desc="rire", "sourire"...
		Pattern p = Pattern.compile("0?\\s?\\[=!(\\s*([A_Za-z\\p{Punct}&&[^\\]]]\\s*)+)\\]");
		Matcher m = p.matcher(s);
		while(m.find()){
			s = s.replace(m.group(), "* " + m.group(1) + " P/* ");
			//System.out.println(m.group());
		}
		return s;
	}

	/* ****************************************************************************************************************************************************** */
	/* ********************************************************* PAUSES ET TERMINATEURS ********************************************************************* */
	/* ****************************************************************************************************************************************************** */

	//Pauses: Fait dans TranscriberToTEI et ChatToTEI

	//Terminateurs : fait dans ChatToTEI

	/* ****************************************************************************************************************************************************** */
	/* ********************************************************* PARALINGUISTIQUE *************************************************************************** */
	/* ****************************************************************************************************************************************************** */

	//Hésitations
	//Codage dans CHILDES?

	//Reprise/reformulation
	//Convention : { ... R}
	public static String reformul(String s){
		//Codage dans corpus chat : ... [//] si un mot répété, <...> [//] si un groupe de mot répété
		ArrayList <Matcher> matchers = new ArrayList<Matcher> ();
		Pattern p1 = Pattern.compile("([A-Za-z'\\-]+)\\s*\\[(/+)\\]");
		Matcher m1 = p1.matcher(s);
		matchers.add(m1);
		Pattern p2 = Pattern.compile("<([A-Za-z'\\-\\s]+)>\\s*\\[(/+)\\]");
		Matcher m2 = p2.matcher(s);
		matchers.add(m2);
		for(Matcher m : matchers){
			while(m.find()){
				String r = "";
				if(m.group(2).equals("/")){
					r = "R1";
				}
				else if(m.group(2).equals("//")){
					r = "R2";
				}
				else if(m.group(2).equals("///")){
					r = "R3";
				}
				s = s.replace(m.group(), "{ " + m.group(1) + " " + r + "}");
			}
		}
		return s;
	}

	//Contexte

	//Explication

	/* ****************************************************************************************************************************************************** */
	/* ********************************************************* SYNTAXE ************************************************************************************* */
	/* ****************************************************************************************************************************************************** */


	//Erreur

	//Omissions
	//Convention : =0 mot
	public static String omission(String s){
		//Codage dans chat: 0mot
		Pattern p = Pattern.compile("0([A-Za-z\\-\\']+)");
		Matcher m = p.matcher(s);
		while(m.find()){
			s = s.replace(m.group(), "=0 " + m.group(1));
		}
		return s;
	}	

	//Liaisons particulières
	//Convention : ... =...= ... , point entre les deux signes si on sait pas quelle son fait la liaison
	public static String liaisons(String s){
		//Codage ICAR : ...^...
		Pattern p = Pattern.compile("([A-Za-z]+)\\^([A-Za-z]+)");
		Matcher m = p.matcher(s);
		while(m.find()){
			s = s.replace(m.group(), m.group(1) + " =.= " + m.group(2));
		}
		return s;
	}

	public static String SetConventions(String s){
		s = indistinct(s);
		s = acroEtSigles(s);
		s = codeAlternance(s);
		s = amorces(s);
		s = ellision(s);
		s = nontrans(s);
		s = titres(s);
		s = lettres(s);
		s = phonTrans(s);
		s = paraLgq(s);
		s = reformul(s);
		s = omission(s);
		s = liaisons(s);
		return Utils.cleanString(s);
	}

	public static void main (String [] args){
		String s = " tiens ta^cuillère <on l'a> [/] on l' a * *** ***  mise ** à laver.";
		s = SetConventions(s);
		System.out.println(s);
	}
}
