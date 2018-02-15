package fr.ortolang.teicorpo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MetaInf{
	public String time_units;
	public String author;
	public String format;
	public String version;
	public ArrayList<Media> medias;
	public String date;
	public ArrayList<Participant> participants;
	public HashSet<String> transcribers;
	public HashMap<String, String> additionnalNotes;

	MetaInf(){
		medias = new ArrayList<Media>();
		participants = new ArrayList<Participant>();
		transcribers = new HashSet<String> ();
		additionnalNotes = new HashMap<String, String>();
	}
}
