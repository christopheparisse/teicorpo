package fr.ortolang.teicorpo;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Représentation d'un locuteur.
 */
public class TeiParticipant {
	// Code donné au locuteur
	public String id;
	// Nom du locuteur
	public String name;
	// Rôle du locuteur
	public String role;
	// Sexe du locuteur
	public String sex;
	// Langue parlée par le locuteur
	public String language;
	// Corpus
	public String corpus;
	// Age du locuteur
	public String age;
	// Informations supplémentaires concernant le locuteur
	public Map<String, String> adds = new HashMap<String, String>();

	public TeiParticipant() {
		id = "";
		name = "";
		role = "";
		sex = "";
		language = "";
		corpus = "";
		age = "";
	}

	public TeiParticipant(Element participant) {
		NamedNodeMap attrs = participant.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node att = attrs.item(i);
			String attName = att.getNodeName();
			String attValue = att.getNodeValue();
			/*
			 * if (attName.equals("code")){ id = attValue; } else
			 */
			/*
			 * if(attName.equals("name")){ name = attValue; } else
			 */
			if (attName.equals("role")) {
				role = attValue;
			} else if (attName.equals("sex")) {
				if (attValue.equals("1")) {
					sex = "male";
				} else if (attValue.equals("2")) {
					sex = "female";
				} else {
					sex = "Unknown";
				}
			}
			/*
			 * else if(attName.equals("xml:lang")){ language = attValue; }
			 */
			else if (attName.equals("source")) {
				corpus = attValue;
			} else if (attName.equals("age")) {
				age = attValue;
			} else {
				adds.put(attName, attValue);
			}
		}
		NodeList partEl = participant.getChildNodes();
		for (int i = 0; i < partEl.getLength(); i++) {
			if (Utils.isElement(partEl.item(i))) {
				Element el = (Element) partEl.item(i);
				if (el.getNodeName().equals("note")) {
					// System.out.printf("adds: %s %s %n",
					// el.getAttribute("type"), el.getTextContent());
					adds.put(el.getAttribute("type"), el.getTextContent());
				} else if (el.getNodeName().equals("altGrp")) {
					NodeList alts = el.getElementsByTagName("alt");
					String codes = "";
					for (int r = 0; r < alts.getLength(); r++) {
						Element alt = (Element) alts.item(r);
						codes += alt.getAttribute("type");
						if (r + 1 < alts.getLength()) {
							codes += ";";
						}
					}
					id = codes;
				} else if (el.getNodeName().equals("langKnowledge")) {
					language = el.getTextContent();
				} else if (el.getNodeName().equals("persName")) {
					name = el.getTextContent();
				} else {
					adds.put(el.getNodeName(), el.getTextContent());
				}
			}
		}
	}

	/**
	 * Impression de Participant.
	 */
	public void print() {
		System.out.print("XParticipant");
		System.out.print(
				"\tid " + id + "\tname " + name + "\trole " + role + "\tsex " + sex + "\tlang " + language + "\t");
		for (Map.Entry<String, String> e : adds.entrySet()) {
			System.out.print(e.getKey() + " " + e.getValue() + "\t");
		}
		System.out.println();
	}

	/**
	 * Impression de Participant.
	 */
	public String toString() {
		String s = "Participant";
		s += "\tid " + Utils.justSpaces(id) + "\tname " + Utils.justSpaces(name) + "\trole " + Utils.justSpaces(role) + "\tsex " + Utils.justSpaces(sex) + "\tlang " + Utils.justSpaces(language) + "\t";
		for (Map.Entry<String, String> e : adds.entrySet()) {
			s += Utils.justSpaces(e.getKey()) + " " + Utils.justSpaces(e.getValue()) + "\t";
		}
		return s;
	}
}
