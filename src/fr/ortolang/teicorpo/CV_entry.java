package fr.ortolang.teicorpo;

public class CV_entry{
	String description;
	String term;
	String lang;

	public CV_entry(String sTerm, String desc, String sLang){
		term = sTerm;
		description = desc;
		lang = sLang;
	}
}