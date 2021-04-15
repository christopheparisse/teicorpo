package fr.ortolang.teicorpo;

public class Version {

	public static String versionTEI = "0.9.1";
	public static String versionSoft = "1.40.37"; // full version with Elan, Clan, Transcriber and Praat
<<<<<<< HEAD
	public static String versionDate = "15/04/2021 15:00";
=======
	public static String versionDate = "29/03/2021 16:00";
>>>>>>> ee15b70de17cc8db2ff5616ae6a552730439a501

	public static void main(String[] args) {
		System.out.printf("Version: %s %s - Format TEI: %s%n", versionSoft, versionDate, versionTEI);
	}

}
