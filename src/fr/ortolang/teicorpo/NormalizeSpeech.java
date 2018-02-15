package fr.ortolang.teicorpo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NormalizeSpeech {

	public static String convertSpecialCodes(String initial) {
		initial = initial.replaceAll("\\byy(\\s|$)\\b", "yyy ");
		initial = initial.replaceAll("\\bxx(\\s|$)\\b", "xxx ");
		initial = initial.replaceAll("\\bww(\\s|$)\\b", "www ");
		initial = initial.replaceAll("\\*\\*\\*", "xxx");
		return ConventionsToChat.setConv(initial);
	}

	/**
	 * Dans Chat, les lignes principales doivent se terminer par un symbole de
	 * fin de ligne (spécifié dans le fichier depfile.cut). Cette méthode
	 * vérifie si c'est bien le cas et si ça ne l'est pas elle rajoute le signe
	 * point (par défaut) à la fin de la ligne à écrire.
	 * 
	 * @param line
	 *            Ligne à renvoyer.
	 * @return
	 */
	public static String toChatLine(String line) {
		String patternStr = "(\\+\\.\\.\\.|\\+/\\.|\\+!\\?|\\+//\\.|\\+/\\?|\\+\"/\\.|\\+\"\\.|\\+//\\?|\\+\\.\\.\\?|\\+\\.|\\.|\\?|!)\\s*$";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(line);
		if (!matcher.find()) {
			line += ".";
		}
		return line;
	}

	
	public static String parseText(String str, String originalFormat, TierParams optionsOutput) {
		String firstpass, secondpass;
		String format = (optionsOutput == null || optionsOutput.normalize.isEmpty()) ? originalFormat : optionsOutput.normalize;
		String target = (optionsOutput == null || optionsOutput.target.isEmpty()) ? optionsOutput.outputFormat : optionsOutput.target;
		//System.out.printf("/%s/ /%s/ /%s/%n", str, originalFormat, format);
		if (format.equals("clan") && !target.equals(".cha")) {
			String s1 = toChatLine(str).trim();
			String s2 = ConventionsToChat.clean(s1);
			String s3 = convertSpecialCodes(s2).replaceAll("\\s+", " ");
			firstpass = ConventionsToChat.chatToText(s3);
			//System.out.printf("§%s§ §%s§ §%s§%n", s1, s2, s3);
		} else {
			firstpass = str;
		}
		if (target.equals(".texgrid")) {
			secondpass = firstpass.replace("\"", "\"\"");
		} else {
			secondpass = firstpass;
		}
		//System.out.printf("{%s} {%s}%n", firstpass, secondpass);
		return secondpass;
	}

	public static void main(String[] args) {
		System.out.printf("%s : %s%n", args[0], parseText(args[0], args[1], null));
	}
}
