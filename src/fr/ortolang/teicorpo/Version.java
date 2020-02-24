package fr.ortolang.teicorpo;

public class Version {

	public static String versionTEI = "0.9.1";
	public static String versionSoft = "1.40.29"; // full version with Elan, Clan, Transcriber and Praat
	public static String versionDate = "24/02/2020 18:00";

	public static void main(String[] args) {
		System.out.printf("Version: %s %s - Format TEI: %s%n", versionSoft, versionDate, versionTEI);
	}

}
