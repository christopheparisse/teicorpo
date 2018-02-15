/**
 * @author Myriam Majdoub
 * TeiConverter: conversion des fichiers teiml en d'autres formats , regroupe les méthodes communes à toutes les conversions possibles.
 */

package fr.ortolang.teicorpo;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class TeiConverter extends GenericMain {

	// Représentation du fichier teiml à convertir
	TeiFile tf;
	// Nom du fichier teiml à convertir
	String inputName;
	// Nom du fichier de sortie
	String outputName;
	// options de production du résultat
	TierParams optionsOutput;

	/**
	 * Conversion du fichier teiml: initialisation des variables d'instances
	 * puis création du nouveau fichier.
	 * 
	 * @param inputName
	 *            Nom du fichier à convertir
	 * @param outputName
	 *            Nom du fichier de sortie
	 */
	public void init(String inputName, String outputName, TierParams options) {
		if (options == null) options = new TierParams();
		File inputFile = new File(inputName);
		if (!inputFile.exists()) {
			System.out.printf("%s n'existe pas: pas de conversion%n", inputName);
			this.tf = null;
			return;
		}
		this.tf = new TeiFile(new File(inputName), options);
		this.inputName = inputName;
		this.outputName = outputName;
		this.optionsOutput = options;
	}

	// Initialisation du fichier de sortie
	public abstract void outputWriter();

	// Conversion des données
	public abstract void conversion();

	// Finalisation du fichier de sortie
	public abstract void createOutput();

	public abstract void writeSpeech(String loc, String speechContent, String startTime, String endTime);

	public abstract void writeAddInfo(AnnotatedUtterance u);
	
	public abstract void writeTier(AnnotatedUtterance u, Annot tier);

	// Récupération des informations générales sur la transcription
	public TransInfo getTransInfo() {
		return this.tf.transInfo;
	}

	// Récupération de la liste des locuteurs
	public ArrayList<TeiParticipant> getParticipants() {
		return tf.transInfo.participants;
	}

	// Récupération de la transcription
	public TeiFile.Trans getTrans() {
		return this.tf.trans;
	}

	// Renvoie la chaîne de caractère passée en argument si elle n'est pas vide,
	// sinon renvoir une chaîne de caractère vide.
	public static String toString(String s) {
		if (!Utils.isNotEmptyOrNull(s)) {
			s = "";
		}
		return s;
	}

	// Convertit des secondes en millisecondes
	public static int toMilliseconds(float t) {
		return (int) (t * 1000);
	}

	// Convertit le format de date YY en YYYY
	public static String convertYear(String year) {
		if (Integer.parseInt(year) < 50) {
			return "20" + year;
		} else {
			return "19" + year;
		}
	}

	/**
	 * Ecriture des utterances
	 * 
	 * @param u
	 *            L'utterance à écrire
	 */
	public void writeUtterance(AnnotatedUtterance u) {
		// u.codes = optionsOutput.codes; // done in TeiFile.
		/*
		 * Chaque utterance a une liste d'énoncé, dans un format spécifique:
		 * start;end__speech
		 */
		// System.err.print(u.toString());
		for (int s = 0; s < u.speeches.size(); s++) {
			String start = null;
			String end = null;
			start = u.speeches.get(s).start;
			end = u.speeches.get(s).end;

			// Si le temps de début n'est pas renseigné, on prend le temps de
			// fin de l'énoncé précédent(si présent)
			if (!Utils.isNotEmptyOrNull(start)) {
				if (s < 1)
					start = "";
				else
					start = u.speeches.get(s - 1).end;
			}

			// Si le temps de fin n'est pas renseigné, on prend le temps de
			// début de l'énoncé suivant(si présent)
			if (!Utils.isNotEmptyOrNull(end)) {
				if (s < u.speeches.size() - 1)
					end = u.speeches.get(s + 1).start;
				else
					end = "";
			}

			// Si l'énoncé est le premier de la liste de l'utterance, son temps
			// de début est égal au temps de début de l'utterance
			if (s == 0 && !Utils.isNotEmptyOrNull(start)) {
				start = u.start;
			}

			// Si l'énoncé est le dernier de la liste de l'utterance, son temps
			// de fin est égal au temps de fin de l'utterance
			if (s == u.speeches.size() - 1 && !Utils.isNotEmptyOrNull(end)) {
				end = u.end;
			}

			String str = u.speeches.get(s).getContent(optionsOutput.rawLine);
			String speech = NormalizeSpeech.parseText(str, tf.originalFormat(), optionsOutput);

			// Ecriture de l'énoncé
			writeSpeech(u.speakerCode, speech, start, end);
		}
		// écriture des tiers
		for (Annot tier : u.tiers) {
			writeTier(u, tier);
		}
		writeAddInfo(u);
	}
}