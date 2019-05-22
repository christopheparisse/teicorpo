package fr.ortolang.teicorpo;

import java.util.ArrayList;
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
//		System.out.printf("pT/%s/ /%s/ /%s/ /%s/%n", str, originalFormat, format, target);
		if (format.equals("ca") && !target.equals(".cha")) {
			String s1 = convertSpecialCodes(str).replaceAll("\\s+", " ");
			String s2 = ConventionsToChat.clean(s1);
			String s3 = ConventionsToChat.chatToText(s2);
			firstpass = ConventionsToChat.caToText(s3);
			//System.out.printf("§%s§ §%s§ §%s§ §%s§ §%s§%n", str, s1, s2, s3, firstpass);
		} else if (format.equals("clan") && !target.equals(".cha")) {
			String s1 = convertSpecialCodes(str).replaceAll("\\s+", " ");
			String s2 = ConventionsToChat.clean(s1);
			firstpass = ConventionsToChat.chatToText(s2);
		} else if (format.equals("pfc")) {
			String s1 = convertSpecialCodes(str).replaceAll("\\s+", " ");
			String s2 = ConventionsToChat.clean(s1);
			String s4 = ConventionsToChat.chatToText(s2);
			// clean up Uppercase letters
			ArrayList<String> p = Tokenizer.splitTextTT(s4);
			String s5 = "";
			LowerCaseLexicon lx = new LowerCaseLexicon();
			for (int ti = 0; ti < p.size(); ti++) {
				String s6 = p.get(ti);
				for (int li=0; li < lx.lowerCaseLexicon.length; li++ ) 
					if (lx.lowerCaseLexicon[li].equals(s6)) {
						s6 = s6.toLowerCase();
						break;
					}
				if (ti < p.size()-1)
					s5 += s6 + " ";
				else
					s5 += s6;
			}
			firstpass = s5.replaceAll("' ", "'");
			// System.out.printf("§%s§ §%s§ §%s§ §%s§ §%s§%n", s1, s2, s3, s4, s5);
		} else {
			firstpass = str;
		}
		if (target.equals(".texgrid")) {
			secondpass = firstpass.replace("\"", "\"\"");
		} else {
			secondpass = firstpass;
		}
//		System.out.printf("{%s} {%s}%n", firstpass, secondpass);
		return secondpass.trim();
	}

	public static void main(String[] args) {
		System.out.printf("%s : %s%n", args[0], parseText(args[0], args[1], null));
	}
}
