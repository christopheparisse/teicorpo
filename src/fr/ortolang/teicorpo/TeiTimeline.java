package fr.ortolang.teicorpo;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TeiTimeline {
	// Map pour représenter la timeline
	public HashMap<String, String> timeline;
	double xmaxTime;

	public String toString() {
		return timeline != null ? "TeiTimeline: nb elements: " + timeline.size() + " xmaxTime = " + xmaxTime : "TeiTimeline was not created.";
	}

	TeiTimeline() {
		timeline = new HashMap<String, String>();
		xmaxTime = 0.0;
	}
	public void buildTimeline(Document teiDoc) {
//		System.err.println("build timeline");
		timeline.put("T0", "0");
		Element tl = (Element) teiDoc.getElementsByTagName("timeline").item(0);
		if (tl == null) return; // No timeline in TEI
		String unit = tl.getAttribute("unit");
		double ratio = 1.0;
		if (unit.equals("ms"))
			ratio = 1.0 / 1000.0;
		else if (unit.equals("s"))
			ratio = 1.0;
		else {
			System.out.println("Unknown unit for timeline: " + unit);
			System.out.println("No conversion performed.");
			ratio = 1.0;
		}
		NodeList whens = tl.getElementsByTagName("when");
		for (int i = 0; i < whens.getLength(); i++) {
			Element when = (Element) whens.item(i);
			if (when.hasAttribute("interval")) {
				String tms = when.getAttribute("interval");
				// tester le type de format des données
				double vald = parseTime(tms);
				vald *= ratio;
				if (vald > xmaxTime)
					xmaxTime = vald;
				tms = Utils.printDouble(vald, 10);
//				System.out.println(tms + " --> " + when.getAttribute("xml:id"));
				timeline.put(when.getAttribute("xml:id"), tms);
			} else if (when.hasAttribute("absolute")) {
				String tms = when.getAttribute("absolute");
				double vald = parseTime(tms);
				vald *= ratio;
				tms = Utils.printDouble(vald, 10);
//				System.out.println(tms + " (abs)--> " + when.getAttribute("xml:id"));
				timeline.put(when.getAttribute("xml:id"), tms);
			}
		}
	}

	double parseTime(String tms) {
		if (tms.indexOf(":") >= 0) {
			// parse xx:yy or xx:yy:zz
			Pattern pattern2x = Pattern.compile("(\\d+):(.+)");
			Pattern pattern3x = Pattern.compile("(\\d+):(\\d+):(.+)");
			Pattern pattern4x = Pattern.compile("(\\d+):(\\d+):(\\d+):(.+)");
			Matcher matcher = pattern4x.matcher(tms);
			if (matcher.matches()) {
				double h = Double.parseDouble(matcher.group(1));
				double m = Double.parseDouble(matcher.group(2));
				double s = Double.parseDouble(matcher.group(3));
				double ms = Double.parseDouble(matcher.group(4));
				return s + m * 60.0 + h * 3600.0 + ms/1000;
			}
			matcher = pattern3x.matcher(tms);
			if (matcher.matches()) {
				double h = Double.parseDouble(matcher.group(1));
				double m = Double.parseDouble(matcher.group(2));
				double s = Double.parseDouble(matcher.group(3));
				return s + m * 60.0 + h * 3600.0;
			}
			matcher = pattern2x.matcher(tms);
			if (matcher.matches()) {
				double m = Double.parseDouble(matcher.group(1));
				double s = Double.parseDouble(matcher.group(2));
				return s + m * 60.0;
			}
			return 0.0;
		} else {
			return Double.parseDouble(tms);
		}
	}

	public String getTimeValue(String timeId) {
		if (Utils.isNotEmptyOrNull(timeId)) {
			String spl = Utils.refID(timeId);
			return timeline.get(spl);
		}
		return "";
	}

}
