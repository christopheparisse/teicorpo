package fr.ortolang.teicorpo;

import java.util.HashMap;

public class Participant{		
	//Code donné au locuteur
	public String id;
	//Nom du locuteur
	public String name;
	//Rôle du locuteur
	public String role;
	//Sexe du locuteur
	public String sex;
	//Langue parlée par le locuteur
	public String language;
	//Corpus
	public String corpus;
	//Age du locuteur
	public String age;
	//Informations supplémentaires concernant le locuteur
	public HashMap<String, String> adds = new HashMap<String, String>();

	Participant(){
		adds = new HashMap<String, String>();
	}
}
