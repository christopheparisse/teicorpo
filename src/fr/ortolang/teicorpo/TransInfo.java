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
	// Date de version de la transcription
	public String versionDate;
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
	public String birth;

    public TransInfo(Element docTeiHeader) {
		if (docTeiHeader == null) return;
		// Récupération des informations relatives au fichier
		NodeList fileDesc = docTeiHeader.getElementsByTagName("fileDesc");
		if (fileDesc != null) getFileDescInfo((Element)fileDesc.item(0));
		// Récupération de informations relatives à l'enregistrement
		NodeList profileDesc = docTeiHeader.getElementsByTagName("profileDesc");
		if (profileDesc != null) getProfileDescInfo((Element) profileDesc.item(0));
		// Récupération des information relatives aux versions de
		// l'enregistrement
		// Element revisionDesc = (Element)
		// docTeiHeader.getElementsByTagName("revisionDesc").item(0);

		// Récupération des informations relatives au logiciel d'origine de
		// la transcription
		NodeList application = docTeiHeader.getElementsByTagName("application");
		if (application != null && application.getLength() > 0) {
			String appformat = ((Element)application.item(0)).getAttribute("ident");
			if (appformat == null) format = appformat;
			String appversion = ((Element)application.item(0)).getAttribute("version");
			if ((version == null || version.isEmpty()) && appversion != null) version = appversion;
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
			System.err.println("error on title: getFileDescInfo");
		}

		try {
			Element recording = (Element) fileDesc.getElementsByTagName("recording").item(0);
			if (recording == null) {
				System.err.println("no recording element");
				return;
			}
			Element media = (Element) recording.getElementsByTagName("media").item(0);
			if (media == null) {
				System.err.println("no media element");
				return;
			}
			medianame = (new File(media.getAttribute("url"))).getName();
			mediatype = media.getAttribute("mimeType");
			Element date = (Element) recording.getElementsByTagName("date").item(0);
			if (date != null)
				timeDuration = date.getAttribute("dur");
			// recordName = (new File(media.getAttribute("url"))).getName();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("error on recording: getFileDescInfo: " + e.toString());
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
			// first item of notesStmt
			Element notesStmt = (Element) fileDesc.getElementsByTagName("notesStmt").item(0);
			// list of main notes
			NodeList mainNotes = notesStmt.getElementsByTagName("note");
			for (int i = 0; i < mainNotes.getLength(); i++) {
				Node nmi = mainNotes.item(i);
				if (!TeiDocument.isElement(nmi)) continue;
				// list of inner notes
				Element emi = (Element) nmi;
				if (emi.getAttribute("type") != null && emi.getAttribute("type").equals("COMMENTS_DESC")) {
					// load comment desc
					NodeList allNotes = emi.getElementsByTagName("note");
					for (int j = 0; j < allNotes.getLength(); j++) {
						if (TeiDocument.isElement(allNotes.item(j))) {
							Element n = (Element) allNotes.item(j);
							if (n.getAttribute("type").equals("scribe")) {
								if (n.getTextContent().toLowerCase().contains("coder -")) {
									transcriber = n.getTextContent().split("oder -")[1];
								} else if (n.getTextContent().toLowerCase().contains("coder-")) {
									transcriber = n.getTextContent().split("oder-")[1];
								} else {
									transcriber = n.getTextContent();
								}
							} else if (n.getAttribute("type").equals("birth")) {
								birth = n.getTextContent();
								if (birth == null) birth = "";
							} else if (n.getAttribute("type").equals("version")) {
								version = n.getTextContent();
								if (version == null) version = "";
							} else if (n.getAttribute("type").equals("version_date")) {
								versionDate = n.getTextContent();
								if (versionDate == null) versionDate = "";
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
				}
			}
		} catch (Exception e) {
			System.err.println("error on note: getFileDescInfo");
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

	// Récupération du nom d'un role à partir de son identifiant (code)
	public String getParticipantRole(String participantID) {
		for (TeiParticipant p : this.participants) {
			if (participantID.equals(p.id) && p.role != null && !p.role.isEmpty()) {
				return p.role;
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
