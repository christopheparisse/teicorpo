package fr.ortolang.teicorpo;

import java.util.ArrayList;

import org.w3c.dom.NodeList;

public class Annot {
    String id;
	String name;
	String start;
	String end;
	String startStamp;
	String endStamp;
	ArrayList<Annot> dependantAnnotations;
	private String content;
	String link;
	String previous;
	String timereftype; // time ou ref
	private String cleanedContent = null; // the content without oral markers
	public String topParent;
	public NodeList pptRef; // pointer of a list of <w> tags
	
	public String getContent(boolean style) {
		return (style == true && cleanedContent != null) ? cleanedContent : content;
	}

	public String getContent() {
		return getContent(false);
	}

	public void setContent(String str) {
		content = str;
	}

	public Annot() {
		id = "";
		name = "";
		start = "";
		end = "";
		dependantAnnotations = null; // new ArrayList<Annot>();
		content = "";
		link = "";
		timereftype = "";
		cleanedContent = null;
		topParent = "";
		startStamp = "---";
		endStamp = "---";
	}

	public Annot(String tierName, String value) {
		id = "";
		name = tierName;
		start = "";
		end = "";
		dependantAnnotations = null;
		content = value;
		link = "";
		timereftype = "";
		cleanedContent = null;
		topParent = "";
	}

	public void setTime(long beginTime, long endTime) {
		/*
		 * correct values if end time before begin time
		 */
		if (endTime < beginTime && endTime >= 0) {
			long sw = endTime;
			endTime = beginTime;
			beginTime = sw;
		}
		start = Long.toString(beginTime, 10);
		end = Long.toString(endTime, 10);
	}

	public Annot(String tierName, String value, long beginTime, long endTime) {
		id = "";
		name = tierName;
		/*
		 * correct values if end time before begin time
		 */
		if (endTime < beginTime && endTime >= 0) {
			long sw = endTime;
			endTime = beginTime;
			beginTime = sw;
		}
		start = Long.toString(beginTime, 10);
		end = Long.toString(endTime, 10);
		dependantAnnotations = new ArrayList<Annot>();
		content = value;
		link = "";
		timereftype = "time";
	}

	public Annot(String tierName, String pstart, String pstartStamp, String pend, String pendStamp, String value, String cleanedValue) {
		id = "";
		name = tierName;
		start = pstart;
		end = pend;
		startStamp = pstartStamp;
		endStamp = pendStamp;
		dependantAnnotations = null;
		content = value;
		cleanedContent = cleanedValue;
		link = "";
		timereftype = "time";
	}

	public Annot(String tierName, String value, String beginTime, String endTime) {
		id = "";
		name = tierName;
		/*
		 * correct values if end time before begin time
		 */
		float beginTimeFloat = (float) -1.0;
		float endTimeFloat = (float) -1.0;
		try {
			beginTimeFloat = Float.parseFloat(beginTime);
			endTimeFloat = Float.parseFloat(endTime);
		} catch(Exception e) {
			;
		}
		if (endTimeFloat < beginTimeFloat && endTime != "-1") {
			String sw = endTime;
			endTime = beginTime;
			beginTime = sw;
		}

		start = beginTime;
		end = endTime;
		dependantAnnotations = new ArrayList<Annot>();
		content = value;
		link = "";
		timereftype = "time";
	}

	public Annot(String tierName, String value, NodeList refs) {
		this(tierName, value);
		pptRef = refs;
	}

	public String toString() {
		String s = "NAME = (" + name + "); ID = (" + id;
		if (timereftype.equals("ref")) {
			s += "); LINK = (" + link + "); ";
			s += "START = (" + start + "); END = (" + end + "); ";
		} else {
			s += "); START = (" + start + "); END = (" + end + "); ";
			s += "STARTSTAMP = (" + startStamp + "); ENDSTAMP = (" + endStamp + "); ";
		}
		s += "CONTENT = (" + content.trim() + ")";
		int i = 0;
		if (dependantAnnotations != null) {
			while (i < dependantAnnotations.size()) {
				s += "\nDA: " + String.valueOf(i) + ". " + dependantAnnotations.get(i).toString();
				i++;
				if (i >= 10) {
					s += "\n\t...";
					break;
				}
			}
//		} else {
//			s += "\nnoDA";
		}
		return s;
	}

	public String tierToString() {
		String s = "\tTIER: " + name + " ";
		if (timereftype.equals("ref")) {
			s += "; LINK: " + link;
		} else {
			s += "; START: " + start + "; END: " + end;
			s += "; STARTSTAMP = " + startStamp + "; ENDSTAMP = " + endStamp;
		}
		s += "; CONTENT: " + content;
		return s;
	}

	public String AnnotToString(String s) {
		s += "NAME = " + name + "; ID = " + id;
		if (timereftype.equals("ref")) {
			s += "; LINK = " + link;
		} else {
			s += "; START = " + start + "; END = " + end;
			s += "; STARTSTAMP = " + startStamp + "; ENDSTAMP = " + endStamp;
		}
		s += "; CONTENT = " + content;
		int i = 0;
		while (i < dependantAnnotations.size()) {
			s += "\n\t" + dependantAnnotations.get(i).AnnotToString(s);
			i++;
		}
		return s;
	}
}