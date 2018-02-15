package fr.ortolang.teicorpo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Contient les informations sur la transcription: lieu, date, locuteurs,
 * situation, enregistrement...
 */
public class TransInfo {

	// Titre du document
	public String title;
	// Format original du document
	public String format;
	// Nom de l'enregistrement
	public String medianame;
	// Adresse du fichier
	public String fileLocation;
	// Nom de l'enregistrement si différent du nom de la transription
	public String recordName;
	// Type de l'enregistrement (video ou audio)
	public String mediatype;
	// Adresse de l'enregistrement
	// public String medialocation;
	// Date de l'enregistement
	public String date;
	// Lieu où a eu lieu l'enregistrement
	public String place;
	// Nom du transcripteur
	public String transcriber;
	// Version de la transcription
	public String version;
	// Durée de la transcription
	public String timeDuration;
	// Heure du début de la transcription
	public String startTime;

	// Liste des situations ayant eu lieu lors de l'enregistrement
	public Map<String, String> situations = new HashMap<String, String>();
	// Liste des locuteurs ayant participé à l'enregistrement
	public ArrayList<TeiParticipant> participants = new ArrayList<TeiParticipant>();
	// Liste des notes supplémentaires concernant la transcription
	public ArrayList<String> notes = new ArrayList<String>();
	// Liste ordonnée des situations
	public ArrayList<String> situationList = new ArrayList<String>();

	public TransInfo(Element docTeiHeader) {
		// Récupération des informations relatives au fichier
		Element fileDesc = (Element) docTeiHeader.getElementsByTagName("fileDesc").item(0);
		getFileDescInfo(fileDesc);
		// Récupération de informations relatives à l'enregistrement
		Element profileDesc = (Element) docTeiHeader.getElementsByTagName("profileDesc").item(0);
		getProfileDescInfo(profileDesc);
		// Récupération des information relatives aux versions de
		// l'enregistrement
		// Element revisionDesc = (Element)
		// docTeiHeader.getElementsByTagName("revisionDesc").item(0);

		// Récupération des informations relatives au logiciel d'origine de
		// la transcription
		Element application = (Element) docTeiHeader.getElementsByTagName("application").item(0);
		if (application != null) {
			format = application.getAttribute("ident");
			version = application.getAttribute("version");
		}
	}

	/**
	 * Récupération des informations relatives au fichier
	 * 
	 * @param fileDesc
	 *            Element fileDesc
	 */
	public void getFileDescInfo(Element fileDesc) {

		try {
			title = fileDesc.getElementsByTagName("title").item(0).getTextContent();
		} catch (Exception e) {
		}

		try {
			Element recording = (Element) fileDesc.getElementsByTagName("recording").item(0);
			Element media = (Element) recording.getElementsByTagName("media").item(0);
			medianame = (new File(media.getAttribute("url"))).getName();
			mediatype = media.getAttribute("mimeType");
			Element date = (Element) recording.getElementsByTagName("date").item(0);
			timeDuration = date.getAttribute("dur");
			// recordName = (new File(media.getAttribute("url"))).getName();
		} catch (Exception e) {
		}

		NodeList recordingDate = fileDesc.getElementsByTagName("date");
		if (recordingDate.getLength() != 0) {
			date = recordingDate.item(0).getTextContent();
		}

		NodeList recordingStartTime = fileDesc.getElementsByTagName("time");
		if (recordingStartTime.getLength() != 0) {
			Element time = (Element) recordingStartTime.item(0);
			startTime = time.getAttribute("when");
		}

		try {
			Element notesStmt = (Element) fileDesc.getElementsByTagName("notesStmt").item(0);
			Element addNotes = (Element) notesStmt.getElementsByTagName("note").item(0);
			NodeList allNotes = addNotes.getElementsByTagName("note");
			for (int i = 0; i < allNotes.getLength(); i++) {
				if (Utils.isElement(allNotes.item(i))) {
					Element n = (Element) allNotes.item(i);
					if (n.getAttribute("type").equals("scribe")) {
						if (n.getTextContent().toLowerCase().contains("coder -")) {
							transcriber = n.getTextContent().split("oder -")[1];
						} else if (n.getTextContent().toLowerCase().contains("coder-")) {
							transcriber = n.getTextContent().split("oder-")[1];
						} else {
							transcriber = n.getTextContent();
						}
					} else {
						if (Utils.isNotEmptyOrNull(n.getTextContent())) {
							// System.out.println("[" +
							// n.getAttribute("type")+ "] " +
							// n.getTextContent());
							notes.add("[" + n.getAttribute("type") + "] " + n.getTextContent());
						}
					}
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Récupération de informations relatives à l'enregistrement
	 * 
	 * @param profileDesc
	 *            Element profileDesc
	 */
	public void getProfileDescInfo(Element profileDesc) {
		try {
			NodeList recordingPlace = profileDesc.getElementsByTagName("placeName");
			if (recordingPlace.getLength() != 0) {
				place = recordingPlace.item(0).getTextContent();
			}
			NodeList participantsStmt = profileDesc.getElementsByTagName("particDesc");
			if (participantsStmt.getLength() != 0) {
				NodeList participantsList = ((Element) participantsStmt.item(0)).getElementsByTagName("person");
				for (int i = 0; i < participantsList.getLength(); i++) {
					Node pNode = participantsList.item(i);
					Element p = (Element) pNode;
					participants.add(new TeiParticipant(p));
				}
			}
			NodeList settingDesc = profileDesc.getElementsByTagName("settingDesc");
			if (settingDesc.getLength() != 0) {
				NodeList settings = ((Element) settingDesc.item(0)).getElementsByTagName("setting");
				for (int i = 0; i < settings.getLength(); i++) {
					Element d = (Element) settings.item(i);
					Element activity = (Element) d.getElementsByTagName("activity").item(0);
					String c = activity != null ? activity.getTextContent().trim() : d.getTextContent();
					// System.out.printf("Theme: %s %s %n",
					// d.getAttribute("xml:id"), c);
					situations.put(d.getAttribute("xml:id"), c);
					situationList.add(c);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("erreur dans le traitement des settings");
		}
	}

	// Récupération du nom d'un participant à partir de son identifiant (code)
	public String getParticipantName(String participantID) {
		for (TeiParticipant p : this.participants) {
			if (participantID.equals(p.id) && p.name != null && !p.name.isEmpty()) {
				return p.name;
			}
		}
		return participantID;
	}

	/**
	 * Impression de TransInfo
	 */
	public void print() {
		System.out.println("medianame\t" + medianame);
		System.out.println("version\t" + version);
		System.out.println("place\t" + place);
		System.out.println("date\t" + date);
		System.out.println("transcriber\t" + transcriber);

		for (TeiParticipant p : participants) {
			p.print();
		}

		for (Map.Entry<String, String> entry : situations.entrySet()) {
			System.out.println("desc\t" + entry.getKey() + "\t" + entry.getValue());
		}

		for (String note : notes) {
			System.out.println("Note: " + note);
		}
	}

	/**
	 * Impression de TransInfo dans une chaine
	 */
	public String toString() {
		String s = "";
		s += "medianame\t" + medianame + "\n";
		s += "version\t" + version + "\n";
		s += "place\t" + place + "\n";
		s += "date\t" + date + "\n";
		s += "transcriber\t" + transcriber + "\n";

		for (TeiParticipant p : participants) {
			s += p.toString() + "\n";
		}

		for (Map.Entry<String, String> entry : situations.entrySet()) {
			s += "desc\t" + entry.getKey() + "\t" + entry.getValue() + "\n";
		}

		for (String note : notes) {
			s += "Note: " + note + "\n";
		}
		return s;
	}
}
