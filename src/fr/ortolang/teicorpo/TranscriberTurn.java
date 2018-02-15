package fr.ortolang.teicorpo;

import java.util.ArrayList;
import java.util.Collections;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class TurnData {
/*
	String pcdata;
	String Sync;
	String Background;
	String Background_Type;
	String Background_Level;
	String Comment;
	String Who;
	String Vocal;
	String Event;
	String Event_Type;
	String Event_Extent;
*/	
	String type; // sync or background or comment or who or vocal or event
	String data1;
	String data2;
	String data3;
	
	public String toString() {
		return "[" + type + "]: (" + data1 + ")(" + data2 + ")(" + data3 + ")";
	}
}

public class TranscriberTurn {
	// types for data
	public static String Sync = "Sync";
	public static String Background = "Background";
	public static String Comment = "Comment";
	public static String Who = "Who";
	public static String Vocal = "Vocal";
	public static String Event = "Event";
	public static String pcdata = "pcdata";

	String startTime;
	String endTime;
	ArrayList<String> speaker;
	String mode;
	String fidelity;
	String channel;
	ArrayList<TurnData> data;

	public TranscriberTurn(String start, String end, String who) {
		startTime = start;
		endTime = end;
		speaker = new ArrayList<String>();
		speaker.add(who);
		data = new ArrayList<TurnData>();
	}

	/*
	 * valable pour type == Who, Sync, Comment, Vocal et pcdata
	 */
	public void add(String type, String val) {
		if (!(type.equals(Who) || type.equals(Sync) || type.equals(Comment) || type.equals(Vocal) || type.equals(pcdata))) {
			System.err.println("Erreur de paramètres dans TranscriberTurn.add: "+type);
			return;
		}
		TurnData td = new TurnData();
		td.type = type;
		td.data1 = val;
		data.add(td);
		
	}

	public void addEvent(String v1, String v2, String v3) {
		// econtent, etype, eextent
		TurnData td = new TurnData();
		td.type = TranscriberTurn.Event;
		td.data1 = v1;
		td.data2 = v2;
		td.data3 = v3;
		data.add(td);
	}

	public void addBackground(String v1, String v2, String v3) {
		TurnData td = new TurnData();
		td.type = TranscriberTurn.Background;
		td.data1 = v1;
		td.data2 = v2;
		td.data3 = v3;
		data.add(td);
	}
	
	public String toString() {
		String s = startTime + " ";
		s += endTime + " ";
		s += speaker.toString() + " ";
		for (TurnData td: data) {
			s += td.toString() + " ";
		}
		return s;
	}

	public void addText(String s) {
		TurnData td = new TurnData();
		td.type = TranscriberTurn.pcdata;
		td.data1 = s;
		data.add(td);
	}

	/*
	 * add a speaker if necessary and sort list
	 */
	public void addSpeaker(String s) {
		int p = speaker.indexOf(s);
		if (p != -1)
			return;
		speaker.add(s);
		Collections.sort(speaker);
	}

	/*
	 * get a speaker number (numbers start from 1)
	 */
	public String getSpeaker(String s) {
		int p = speaker.indexOf(s);
		if (p != -1)
			return Integer.toString(p+1);
		else
			return "";
	}

	public Element toElement(Document doc) {
		Element t = doc.createElement("Turn");
		String spk = speakersToString();
		if (!spk.isEmpty())
			t.setAttribute("speaker", spk);
		if (!startTime.isEmpty())
			t.setAttribute("startTime", startTime);
		if (!endTime.isEmpty())
			t.setAttribute("endTime", endTime);
		for (TurnData td: data) {
			if (td.type.equals(TranscriberTurn.pcdata)) {
				Node e = doc.createTextNode(td.data1);
				t.appendChild(e);
			} else if (td.type.equals(TranscriberTurn.Sync)) {
				Element e = doc.createElement("Sync");
				e.setAttribute("time", td.data1);
				t.appendChild(e);
			} else if (td.type.equals(TranscriberTurn.Comment)) {
				Element e = doc.createElement("Comment");
				e.setAttribute("desc", td.data1);
				t.appendChild(e);
			} else if (td.type.equals(TranscriberTurn.Who)) {
				Element e = doc.createElement("Who");
				e.setAttribute("nb", getSpeaker(td.data1));
				t.appendChild(e);
			} else if (td.type.equals(TranscriberTurn.Vocal)) {
				Element e = doc.createElement("Vocal");
				e.setAttribute("desc", td.data1);
				t.appendChild(e);
			} else if (td.type.equals(TranscriberTurn.Event)) {
				Element e = doc.createElement("Event");
				e.setAttribute("desc", td.data1);
				e.setAttribute("type", td.data2);
				e.setAttribute("extent", td.data3);
				t.appendChild(e);
			} else if (td.type.equals(TranscriberTurn.Background)) {
				Element e = doc.createElement("Background");
				e.setAttribute("time", td.data1);
				e.setAttribute("type", td.data2);
				e.setAttribute("level", td.data3);
				t.appendChild(e);
			}
		}
		return t;
	}

	public String speakersToString() {
		String[] a = speaker.toArray(new String[speaker.size()]);
		String spks = "";
		for (int i = 0; i < a.length-1; i++)
			spks += a[i] + " ";
		spks += a[a.length-1];
		return spks;
	}

	public void copyFrom(TranscriberTurn source, int start) {
		for (int i = start; i < source.data.size(); i++) {
			TurnData td = source.data.get(i);
			if (td.type.equals(TranscriberTurn.Event)) {
				this.addEvent(td.data1, td.data2, td.data3);
			} else if (td.type.equals(TranscriberTurn.Background)) {
				this.addBackground(td.data1, td.data2, td.data3);
			} else {
				this.add(td.type, td.data1);
			}
		}
	}
}
