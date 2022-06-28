package fr.ortolang.teicorpo;

public class Version {

	public static String versionTEI = "0.9.1";
	private static String versionSoftStr = "1.41.05"; // full version with Elan, Clan, Transcriber and Praat
	private static String versionDateStr = "29/06/2022 18:00";

	public static String versionSoft(boolean isTest) {
		return isTest ? "testing-version" : versionSoftStr;
	}

	public static String versionDate(boolean isTest) {
		return isTest ? "testing-version" : versionDateStr;
	}

	public static void main(String[] args) {
		System.out.printf("Version: %s %s - Format TEI: %s%n", versionSoft(false), versionDate(false), versionTEI);
	}

}
